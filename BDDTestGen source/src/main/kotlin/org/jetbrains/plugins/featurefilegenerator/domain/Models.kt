package org.jetbrains.plugins.featurefilegenerator.domain

/**
 * Represents the Pure Domain of an LLM configuration.
 * This layer knows nothing about IntelliJ, CLI, or files - only business logic.
 */
data class LLMModelConfig(
    val name: String,
    val scriptFilePath: String,
    val parameterSpecFilePath: String,
    val command: String,
    val namedParameters: List<ModelParameter> = emptyList()
)

sealed class ModelParameter(
    val key: String,
    val argName: String,
    val required: Boolean,
    val description: String
)

data class StringParam(val k: String, val arg: String, val req: Boolean, val desc: String, val value: String) : ModelParameter(k, arg, req, desc)
data class BooleanParam(val k: String, val arg: String, val req: Boolean, val desc: String, val value: Boolean) : ModelParameter(k, arg, req, desc)
data class DoubleParam(val k: String, val arg: String, val req: Boolean, val desc: String, val value: Double) : ModelParameter(k, arg, req, desc)
data class ListParam(val k: String, val arg: String, val req: Boolean, val desc: String, val value: String, val options: List<String>) : ModelParameter(k, arg, req, desc)

/**
 * Interface defining how the system retrieves configurations.
 * Abstracts whether it comes from IntelliJ Service or CLI JSON.
 */
interface LLMConfigProvider {
    fun getConfiguration(name: String): LLMModelConfig?
    fun getAllConfigurationNames(): List<String>
}
