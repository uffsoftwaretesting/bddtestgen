package org.jetbrains.plugins.featurefilegenerator.domain

/**
 * Representa o Domínio Puro das configurações de um LLM.
 * Esta camada não conhece IntelliJ, CLI ou arquivos - apenas a lógica de negócio.
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
 * Interface que define como o sistema recupera configurações.
 * Abstrai se vem do IntelliJ Service ou do CLI JSON.
 */
interface LLMConfigProvider {
    fun getConfiguration(name: String): LLMModelConfig?
    fun getAllConfigurationNames(): List<String>
}
