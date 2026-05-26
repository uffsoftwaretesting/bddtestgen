package org.jetbrains.plugins.featurefilegenerator.executor

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import kotlinx.coroutines.*
import org.jetbrains.plugins.featurefilegenerator.domain.*
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlinx.serialization.json.*

/**
 * Motor de Execução (Domain/Application Service).
 * Agora ele é totalmente independente de COMO as configurações são salvas.
 * Ele depende apenas da interface LLMConfigProvider.
 */
class LLMExecutor(private val configProvider: LLMConfigProvider) {

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    fun execute(llmName: String, filePath: String, onResult: (String, String) -> Unit) {
        val config = configProvider.getConfiguration(llmName)
            ?: throw IllegalArgumentException("LLM '$llmName' not found.")

        // If inside IntelliJ (simple environment detection)
        if (isInsideIntellij()) {
            ProgressManager.getInstance().run(object : Task.Backgroundable(null, "Generating BDD ($llmName)", true) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.text = "Calling LLM: $llmName..."
                    val result = runProcessSync(config, filePath)
                    onResult(llmName, result)
                }
            })
        } else {
            // CLI ou outros ambientes
            val result = runProcessSync(config, filePath)
            onResult(llmName, result)
        }
    }

    fun executeBatchAsync(filePath: String, onResult: (String, String) -> Unit) {
        configProvider.getAllConfigurationNames().forEach { llmName ->
            execute(llmName, filePath, onResult)
        }
    }

    suspend fun executeBatchCli(filePath: String, onResult: (String, String) -> Unit) = coroutineScope {
        configProvider.getAllConfigurationNames().map { llmName ->
            async {
                val result = runProcessSync(configProvider.getConfiguration(llmName)!!, filePath)
                onResult(llmName, result)
            }
        }.awaitAll()
    }

    private fun runProcessSync(config: LLMModelConfig, filePath: String): String {
        return try {
            if (!config.apiUrl.isNullOrBlank()) {
                executeGenericApi(config, filePath)
            } else if (config.scriptFilePath == "native") {
                executeNative(config, filePath)
            } else {
                executeExternalScript(config, filePath)
            }
        } catch (e: Exception) {
            "❌ Error: ${e.message}"
        }
    }

    private fun executeGenericApi(config: LLMModelConfig, filePath: String): String {
        val storyText = File(filePath).readText()
            .replace("\"", "\\\"") // Escape quotes for JSON
            .replace("\n", "\\n")
        
        var body = config.apiBodyTemplate ?: "{}"
        var url = config.apiUrl ?: ""
        
        // Template replacement for Body and URL
        body = body.replace("{{story}}", storyText)
        url = url.replace("{{story}}", storyText)

        config.namedParameters.forEach { param ->
            val placeholder = "{{${param.key}}}"
            val value = param.getValueAsString()
            
            body = body.replace(placeholder, value)
            url = url.replace(placeholder, value)
        }

        val payload = Json.parseToJsonElement(body).jsonObject
        val path = config.apiResultPath?.split(".")?.toTypedArray() ?: arrayOf("text")
        
        // Smart Authorization: Only send Header if the key wasn't already used in the URL
        val apiVariable = config.namedParameters.find { it.key == "api_key" || it.key == "apiKey" }
        val apiKey = if (apiVariable != null && !url.contains(apiVariable.getValueAsString())) {
            apiVariable.getValueAsString()
        } else ""

        return callHttpApi(url, apiKey, payload, *path)
    }

    private fun executeNative(config: LLMModelConfig, filePath: String): String {
        val paramsMap = config.namedParameters.associate { it.argName to it.getValueAsString() }
        val storyText = File(filePath).readText()
        val promptPath = paramsMap["--prompt_instruction_path"] ?: paramsMap["--instruction_file"]
        val instructionPrompt = promptPath?.let { readResourceOrFile(it) } ?: ""

        val result = when (config.name.lowercase()) {
            "chatgpt", "openai" -> executeOpenAI(paramsMap, instructionPrompt, storyText)
            "gemini" -> executeGemini(paramsMap, instructionPrompt, storyText)
            "deepseek" -> executeDeepSeek(paramsMap, instructionPrompt, storyText)
            else -> throw IllegalArgumentException("Native model '${config.name}' not supported.")
        }

        val strippedResult = stripGherkinFormatting(result)
        
        // Saving logic (Infrastructure)
        val outputDir = paramsMap["--output_dir_path"]
        if (!outputDir.isNullOrBlank() && !strippedResult.startsWith("❌ Error")) {
            saveResultToFile(outputDir, config.name, strippedResult)
        }
        
        return strippedResult
    }

    private fun executeExternalScript(config: LLMModelConfig, filePath: String): String {
        val commandList = mutableListOf<String>()
        commandList.add(config.command)
        commandList.add(config.scriptFilePath)

        config.namedParameters.forEach { param ->
            commandList.add(param.argName)
            commandList.add(param.getValueAsString())
        }
        commandList.add(filePath)

        val process = ProcessBuilder(commandList).redirectErrorStream(true).start()
        val result = process.inputStream.bufferedReader().readText()
        process.waitFor()
        return stripGherkinFormatting(result)
    }

    // --- Métodos de Apoio (Poderiam ser extraídos para um LLMClient) ---

    private fun executeOpenAI(paramsMap: Map<String, String>, instruction: String, story: String): String {
        val apiKey = paramsMap["--api_key"] ?: throw IllegalArgumentException("API Key missing.")
        val model = paramsMap["--model"] ?: "gpt-5.5"
        val temp = paramsMap["--temperature"]?.toDoubleOrNull() ?: 0.7

        val payload = buildJsonObject {
            put("model", model)
            put("messages", buildJsonArray {
                add(buildJsonObject { put("role", "system"); put("content", instruction) })
                add(buildJsonObject { put("role", "user"); put("content", story) })
            })
            put("temperature", temp)
        }

        return callHttpApi("https://api.openai.com/v1/chat/completions", apiKey, payload, "choices", "message", "content")
    }

    private fun executeGemini(paramsMap: Map<String, String>, instruction: String, story: String): String {
        val apiKey = paramsMap["--api_key"] ?: throw IllegalArgumentException("API Key missing.")
        val model = paramsMap["--model"] ?: "gemini-3.5-flash"
        
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        
        val payload = buildJsonObject {
            put("contents", buildJsonArray {
                add(buildJsonObject {
                    put("parts", buildJsonArray {
                        add(buildJsonObject { put("text", "$instruction\n\nUser Story:\n$story") })
                    })
                })
            })
        }

        return callHttpApi(url, "", payload, "candidates", "0", "content", "parts", "0", "text")
    }

    private fun executeDeepSeek(paramsMap: Map<String, String>, instruction: String, story: String): String {
        val apiKey = paramsMap["--api_key"] ?: throw IllegalArgumentException("API Key missing.")
        val model = paramsMap["--model"] ?: "deepseek-v4-flash"
        val temp = paramsMap["--temperature"]?.toDoubleOrNull() ?: 0.7

        val payload = buildJsonObject {
            put("model", model)
            put("messages", buildJsonArray {
                add(buildJsonObject { put("role", "system"); put("content", instruction) })
                add(buildJsonObject { put("role", "user"); put("content", story) })
            })
            put("temperature", temp)
        }

        return callHttpApi("https://api.deepseek.com/chat/completions", apiKey, payload, "choices", "message", "content")
    }

    private fun callHttpApi(url: String, apiKey: String, payload: JsonObject, vararg path: String): String {
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))

        if (apiKey.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer $apiKey")
        }

        val response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
        
        if (response.statusCode() != 200) {
            return "❌ API Error (${response.statusCode()}): ${response.body()}"
        }

        return try {
            val json = Json.parseToJsonElement(response.body()).jsonObject
            var current: JsonElement = json
            for (segment in path) {
                current = when (current) {
                    is JsonObject -> current[segment] ?: return "❌ Error: Field '$segment' not found in response."
                    is JsonArray -> {
                        val index = segment.toIntOrNull() ?: return "❌ Error: Invalid index '$segment'."
                        current[index]
                    }
                    else -> return "❌ Error: Unexpected JSON structure."
                }
            }
            current.jsonPrimitive.content
        } catch (e: Exception) {
            "❌ Error processing JSON: ${e.message}"
        }
    }

    private fun saveResultToFile(dir: String, modelName: String, content: String) {
        try {
            val directory = File(dir)
            if (!directory.exists()) directory.mkdirs()
            val fileName = "${modelName.lowercase().replace(" ", "_")}_output.feature"
            File(directory, fileName).writeText(content)
            println("✅ Response saved at: ${File(directory, fileName).absolutePath}")
        } catch (e: Exception) {
            println("❌ Failed to save file: ${e.message}")
        }
    }

    internal fun stripGherkinFormatting(string: String): String {
        val leftStripGherkin = "```gherkin"
        val leftStripGeneric = "```"
        val rightStrip = "```"
        var stripped = string.trim()
        
        if (stripped.startsWith(leftStripGherkin) && stripped.endsWith(rightStrip)) {
            stripped = stripped.removePrefix(leftStripGherkin).removeSuffix(rightStrip)
        } else if (stripped.startsWith(leftStripGeneric) && stripped.endsWith(rightStrip)) {
            stripped = stripped.removePrefix(leftStripGeneric).removeSuffix(rightStrip)
        }
        
        return stripped.trim()
    }

    private fun readResourceOrFile(path: String): String {
        val file = File(path)
        if (file.exists()) return file.readText()
        
        val inputStream = javaClass.classLoader.getResourceAsStream(path)
            ?: return ""
        return inputStream.bufferedReader().readText()
    }

    private fun isInsideIntellij(): Boolean {
        return try {
            Class.forName("com.intellij.openapi.application.ApplicationManager")
            true
        } catch (e: Exception) {
            false
        }
    }

    // Extensões auxiliares para o domínio
    private fun ModelParameter.getValueAsString(): String = when (this) {
        is StringParam -> value
        is BooleanParam -> value.toString()
        is DoubleParam -> value.toString()
        is ListParam -> value
    }
}
