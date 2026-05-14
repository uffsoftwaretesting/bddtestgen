package org.jetbrains.plugins.featurefilegenerator.executor

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import kotlinx.coroutines.runBlocking
import org.jetbrains.plugins.featurefilegenerator.LLMSettings
import org.jetbrains.plugins.featurefilegenerator.cli.LLMSettingsCLI
import java.io.File

class LLMExecutor(private val llmSettings: Any) {

    /**
     * Executes a single LLM based on the name and file path.
     * This version is used in both CLI mode and the plugin, but in the plugin we use ProgressManager.
     */
    fun execute(llmName: String, filePath: String, onResult: (String, String) -> Unit) {
        val config = when (llmSettings) {
            is LLMSettings -> llmSettings.getConfigurationByName(llmName)
            is LLMSettingsCLI -> llmSettings.getConfigurationByName(llmName)
            else -> null
        } ?: throw IllegalArgumentException("LLM '$llmName' not found.")

        // If we're in plugin mode, we use ProgressManager
        if (llmSettings is LLMSettings) {
            ProgressManager.getInstance().run(object : Task.Backgroundable(null, "Generating .feature File ($llmName)", true) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.text = "Running LLM script: $llmName..."
                    indicator.isIndeterminate = true
                    val result = runProcess(config, filePath)
                    onResult(llmName, result)
                }
            })
        } else {
            // CLI Mode: direct execution
            val result = runProcess(config, filePath)
            onResult(llmName, result)
        }
    }

    /**
     * Executes all configured LLMs sequentially in asynchronous fashion (in plugin mode).
     * Each execution will occur within a single background task.
     */
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
                    // The onResult callback may be called outside the progress thread, so we use invokeLater
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                        onResult(config.name, result)
                    }
                }
            }
        })
    }

    private fun extractResourceToTempFile(resourcePath: String, prefix: String, suffix: String): String {
        // Remove "src/main/resources/" if it's there to support the raw relative path in the example
        val cleanPath = resourcePath.removePrefix("src/main/resources/").removePrefix("/")
        val inputStream = javaClass.classLoader.getResourceAsStream(cleanPath)
            ?: throw IllegalArgumentException("Script or resource not found locally nor in classpath: $resourcePath")
        
        val tempFile = File.createTempFile(prefix, suffix)
        tempFile.deleteOnExit()
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile.absolutePath
    }

    private fun resolveFilePath(filePath: String, isPython: Boolean = false): String {
        val file = File(filePath)
        if (file.exists()) return file.absolutePath
        // Try to resolve from classpath
        return extractResourceToTempFile(filePath, "bddtestgen_script_", if (isPython) ".py" else ".txt")
    }

    /**
     * Builds and runs the LLM process based on configuration and input file.
     */
    private fun runProcess(config: Any, filePath: String): String {
        return try {
            val commandList = mutableListOf<String>()

            when (config) {
                is LLMSettings.LLMConfiguration -> {
                    commandList.add(config.command)
                    commandList.add(resolveFilePath(config.scriptFilePath, isPython = true))
                    config.namedParameters.forEach { param ->
                        if (param.argName.isNotBlank()) {
                            when (param) {
                                is LLMSettings.StringParam -> {
                                    var value = param.value.trim()
                                    // If this is the instruction prompt, we might need to extract it
                                    if (param.argName == "--prompt_instruction_path") {
                                        value = resolveFilePath(value)
                                    }
                                    if (value.isNotBlank()) {
                                        commandList.add(param.argName)
                                        commandList.add(value)
                                    }
                                }
                                is LLMSettings.ListParam -> {
                                    val value = param.value.trim()
                                    if (value.isNotBlank()) {
                                        commandList.add(param.argName)
                                        commandList.add(value)
                                    }
                                }
                                is LLMSettings.IntParam -> {
                                    commandList.add(param.argName)
                                    commandList.add(param.value.toString())
                                }
                                is LLMSettings.DoubleParam -> {
                                    commandList.add(param.argName)
                                    commandList.add(param.value.toString().replace(',', '.'))
                                }
                                is LLMSettings.BooleanParam -> {
                                    if (param.value) commandList.add(param.argName)
                                }
                            }
                        }
                    }
                }
                is LLMSettingsCLI.LLMConfiguration -> {
                    commandList.add(config.command)
                    commandList.add(resolveFilePath(config.scriptFilePath, isPython = true))
                    config.namedParameters.forEach { param ->
                        if (param.argName.isNotBlank()) {
                            when (param) {
                                is LLMSettingsCLI.NamedParameter.StringParam -> {
                                    var value = param.value.trim()
                                    if (param.argName == "--prompt_instruction_path") {
                                        value = resolveFilePath(value)
                                    }
                                    if (value.isNotBlank()) {
                                        commandList.add(param.argName)
                                        commandList.add(value)
                                    }
                                }
                                is LLMSettingsCLI.NamedParameter.IntParam -> {
                                    commandList.add(param.argName)
                                    commandList.add(param.value.toString())
                                }
                                is LLMSettingsCLI.NamedParameter.DoubleParam -> {
                                    commandList.add(param.argName)
                                    commandList.add(param.value.toString().replace(',', '.'))
                                }
                                is LLMSettingsCLI.NamedParameter.BooleanParam -> {
                                    if (param.value) commandList.add(param.argName)
                                }
                            }
                        }
                    }
                }
            }

            // Adds the user story file path parameter
            commandList.add("--user_story_path")
            commandList.add(filePath)

            println("🔍 Executing command: ${commandList.joinToString(" ")}")

            val process = ProcessBuilder(commandList)
                .directory(File("."))
                .redirectErrorStream(false) // Handle stderr separately for better CLI feedback
                .start()

            val output = process.inputStream.bufferedReader().use { it.readText() }
            val errorOutput = process.errorStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()

            if (exitCode != 0) {
                println("❌ Process failed with code: $exitCode")
                if (errorOutput.isNotBlank()) println("❌ Error Output:\n$errorOutput")
                return "Error (Code $exitCode):\n$errorOutput"
            }

            output
        } catch (e: Exception) {
            "Error executing the process: ${e.message}"
        }
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
            // Executes each LLM synchronously
            val result = runProcess(config, filePath)
            onResult(config.name, result)
        }
    }
}
