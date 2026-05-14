package org.jetbrains.plugins.featurefilegenerator.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.coroutines.runBlocking
import org.jetbrains.plugins.featurefilegenerator.executor.LLMExecutor
import kotlin.system.exitProcess

class BatchGenerateFeatureCLI : CliktCommand(
    help = "Generates BDD .feature files using configured LLMs.",
    name = "bddtestgen"
) {
    val configFile by option("--config", "-c", help = "Path to the JSON configuration file defining the LLMs")
        .file(mustExist = true, canBeDir = false)
        .required()

    val inputFile by argument(help = "Path to the plain-text user story file")
        .file(mustExist = true, canBeDir = false)

    override fun run() {
        try {
            val llmSettings = LLMSettingsCLI(configFile.absolutePath)
            val executor = LLMExecutor(llmSettings)

            echo("🚀 Executing all configured LLMs...")
            
            runBlocking {
                executor.executeBatchCli(inputFile.absolutePath) { llmName, result ->
                    echo("\n=== LLM: \$llmName returned ===")
                    echo(result)
                    echo("===============================\n")
                }
            }
        } catch (e: Exception) {
            echo("❌ Error: \${e.message}", err = true)
            exitProcess(1)
        }
    }
}

fun main(args: Array<String>) = BatchGenerateFeatureCLI().main(args)
