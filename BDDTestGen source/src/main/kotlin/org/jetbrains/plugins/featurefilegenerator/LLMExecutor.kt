package org.jetbrains.plugins.featurefilegenerator.executor

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import kotlinx.coroutines.runBlocking
import org.jetbrains.plugins.featurefilegenerator.LLMSettings
import org.jetbrains.plugins.featurefilegenerator.cli.LLMSettingsCLI
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlinx.serialization.json.*

class LLMExecutor(private val llmSettings: Any) {

    fun execute(llmName: String, filePath: String, onResult: (String, String) -> Unit) {
        val config = when (llmSettings) {
            is LLMSettings -> llmSettings.getConfigurationByName(llmName)
            is LLMSettingsCLI -> llmSettings.getConfigurationByName(llmName)
            else -> null
        } ?: throw IllegalArgumentException("LLM '$llmName' not found.")

        if (llmSettings is LLMSettings) {
            ProgressManager.getInstance().run(object : Task.Backgroundable(null, "Generating .feature File ($llmName)", true) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.text = "Running LLM: $llmName..."
                    indicator.isIndeterminate = true
                    val result = runProcess(config, filePath)
                    onResult(llmName, result)
                }
            })
        } else {
            val result = runProcess(config, filePath)
            onResult(llmName, result)
        }
    }

    fun executeBatchAsync(filePath: String, onResult: (String, String) -> Unit) {
        if (llmSettings !is LLMSettings) {
            throw IllegalStateException("Plugin mode requires LLMSettings, not LLMSettingsCLI.")
        }
        val configurations = llmSettings.getConfigurations()
        if (configurations.isEmpty()) {
            throw IllegalStateException("No LLM configuration found.")
        }

        ProgressManager.getInstance().run(object : Task.Backgroundable(null, "Running LLMs", true) {
            override fun run(indicator: ProgressIndicator) {
                for (config in configurations) {
                    indicator.text = "Running ${config.name}..."
                    val result = runProcess(config, filePath)
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                        onResult(config.name, result)
                    }
                }
            }
        })
    }

    private fun readResourceOrFile(path: String): String {
        val file = File(path)
        if (file.exists()) return file.readText(Charsets.UTF_8)
        
        val cleanPath = path.removePrefix("src/main/resources/").removePrefix("/")
        val inputStream = javaClass.classLoader.getResourceAsStream(cleanPath)
            ?: throw IllegalArgumentException("Resource not found: $path")
            
        return inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    private fun stripGherkinFormatting(string: String): String {
        val leftStrip = "```gherkin"
        val rightStrip = "```"
        var stripped = string.trim()
        if (stripped.startsWith(leftStrip)) {
            stripped = stripped.removePrefix(leftStrip).trim()
        }
        if (stripped.endsWith(rightStrip)) {
            stripped = stripped.removeSuffix(rightStrip).trim()
        }
        return stripped
    }

    private fun runProcess(config: Any, filePath: String): String {
        return try {
            val providerName = (config as? LLMSettings.LLMConfiguration)?.name 
                ?: (config as? LLMSettingsCLI.LLMConfiguration)?.name 
                ?: "Unknown"

            val paramsMap = mutableMapOf<String, String>()
            
            // Extract parameters
            when (config) {
                is LLMSettings.LLMConfiguration -> {
                    config.namedParameters.forEach { param ->
                        if (param.argName.isNotBlank()) {
                            val cleanName = param.argName.removePrefix("--")
                            val value = when (param) {
                                is LLMSettings.StringParam -> param.value
                                is LLMSettings.ListParam -> param.value
                                is LLMSettings.IntParam -> param.value.toString()
                                is LLMSettings.DoubleParam -> param.value.toString()
                                is LLMSettings.BooleanParam -> param.value.toString()
                                else -> ""
                            }
                            paramsMap[cleanName] = value
                        }
                    }
                }
                is LLMSettingsCLI.LLMConfiguration -> {
                    config.namedParameters.forEach { param ->
                        if (param.argName.isNotBlank()) {
                            val cleanName = param.argName.removePrefix("--")
                            val value = when (param) {
                                is LLMSettingsCLI.NamedParameter.StringParam -> param.value
                                is LLMSettingsCLI.NamedParameter.IntParam -> param.value.toString()
                                is LLMSettingsCLI.NamedParameter.DoubleParam -> param.value.toString()
                                is LLMSettingsCLI.NamedParameter.BooleanParam -> param.value.toString()
                                else -> ""
                            }
                            paramsMap[cleanName] = value
                        }
                    }
                }
            }

            val apiKey = paramsMap["api_key"]?.takeIf { it.isNotBlank() } ?: return "Error: API Key (--api_key) not provided in configuration."
            val model = paramsMap["model"] ?: if (providerName.contains("gemini", true)) "gemini-3-flash" else "gpt-5.5-instant"
            val temperature = paramsMap["temperature"]?.toDoubleOrNull() ?: 0.7
            val promptPath = paramsMap["prompt_instruction_path"] ?: paramsMap["instruction_file"]
            val customEndpoint = paramsMap["endpoint"] ?: paramsMap["api_base"]
            
            val instructionPrompt = promptPath?.let { readResourceOrFile(it) } ?: ""
            val userStory = File(filePath).readText(Charsets.UTF_8)

            println("🔍 Sending API Request to $providerName (Model: $model)")

            val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build()
            
            val result = if (providerName.contains("gemini", ignoreCase = true)) {
                executeGemini(client, apiKey, model, temperature, instructionPrompt, userStory)
            } else {
                executeOpenAI(client, providerName, apiKey, model, temperature, instructionPrompt, userStory, customEndpoint)
            }
            val strippedResult = stripGherkinFormatting(result)
            
            val outputDir = paramsMap["output_dir_path"]
            if (!outputDir.isNullOrBlank() && !strippedResult.startsWith("❌ Error")) {
                val dir = File(outputDir)
                if (!dir.exists()) dir.mkdirs()
                
                val safeProviderName = providerName.replace(Regex("[^a-zA-Z0-9.-]"), "_").lowercase()
                val outputFile = File(dir, "${safeProviderName}_output.feature")
                outputFile.writeText(strippedResult, Charsets.UTF_8)
                println("✅ Response saved at: ${outputFile.absolutePath}")
            }
            
            strippedResult
        } catch (e: Exception) {
            "❌ Error executing the API call: ${e.message}"
        }
    }

    private fun executeGemini(client: HttpClient, apiKey: String, model: String, temperature: Double, instruction: String, story: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        
        val jsonBody = buildJsonObject {
            putJsonArray("contents") {
                addJsonObject {
                    putJsonArray("parts") {
                        addJsonObject { put("text", instruction) }
                        addJsonObject { put("text", story) }
                    }
                }
            }
            putJsonObject("generationConfig") {
                put("temperature", temperature)
            }
        }.toString()

        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        
        if (response.statusCode() !in 200..299) {
            return "❌ Gemini API Error (${response.statusCode()}): ${response.body()}"
        }

        val json = Json.parseToJsonElement(response.body()).jsonObject
        return json["candidates"]?.jsonArray?.get(0)?.jsonObject
            ?.get("content")?.jsonObject
            ?.get("parts")?.jsonArray?.get(0)?.jsonObject
            ?.get("text")?.jsonPrimitive?.content 
            ?: "❌ Error: Could not parse Gemini response."
    }

    private fun executeOpenAI(client: HttpClient, providerName: String, apiKey: String, model: String, temperature: Double, instruction: String, story: String, customEndpoint: String?): String {
        val url = customEndpoint ?: if (providerName.contains("deepseek", ignoreCase = true)) {
            "https://api.deepseek.com/chat/completions"
        } else {
            "https://api.openai.com/v1/chat/completions"
        }

        val jsonBody = buildJsonObject {
            put("model", model)
            put("temperature", temperature)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "system")
                    put("content", instruction)
                }
                addJsonObject {
                    put("role", "user")
                    put("content", story)
                }
            }
        }.toString()

        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $apiKey")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        
        if (response.statusCode() !in 200..299) {
            return "❌ API Error (${response.statusCode()}): ${response.body()}"
        }

        val json = Json.parseToJsonElement(response.body()).jsonObject
        return json["choices"]?.jsonArray?.get(0)?.jsonObject
            ?.get("message")?.jsonObject
            ?.get("content")?.jsonPrimitive?.content 
            ?: "❌ Error: Could not parse OpenAI/DeepSeek response."
    }

    fun executeBatchCli(filePath: String, onResult: (String, String) -> Unit) = runBlocking {
        if (llmSettings !is LLMSettingsCLI) {
            throw IllegalStateException("CLI mode requires LLMSettingsCLI, not ${llmSettings::class.simpleName}.")
        }
        val configurations = llmSettings.getConfigurations()
        if (configurations.isEmpty()) {
            throw IllegalStateException("No LLM configuration found.")
        }
        configurations.forEach { config ->
            val result = runProcess(config, filePath)
            onResult(config.name, result)
        }
    }
}
