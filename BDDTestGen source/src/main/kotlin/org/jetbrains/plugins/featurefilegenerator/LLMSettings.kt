package org.jetbrains.plugins.featurefilegenerator

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XCollection
import org.jetbrains.plugins.featurefilegenerator.domain.*
import java.io.File

@State(
    name = "LLMSettings",
    storages = [Storage("LLMSettings.xml")]
)
class LLMSettings : PersistentStateComponent<LLMSettings.State>, LLMConfigProvider {
    private var myState: State = State()

    class State {
        @XCollection(propertyElementName = "configurations")
        var configurations: MutableList<LLMConfiguration> = mutableListOf()

        @Attribute("selectedLLMName")
        var selectedLLMName: String? = null
    }
    override fun noStateLoaded() {
        println("DEBUG: No state loaded, adding default configurations.")
        addDefaultConfigurationsIfMissing()
    }

    fun getSelectedLLM(): String? = myState.selectedLLMName

    fun setSelectedLLM(name: String?) {
        myState.selectedLLMName = name
    }

    @Tag("LLMConfiguration")
    data class LLMConfiguration(
        @Attribute("name")
        var name: String = "",

        @Attribute("scriptFilePath")
        var scriptFilePath: String = "",

        @Attribute("parameterSpecFilePath")
        var parameterSpecFilePath: String = "",

        @Attribute("command")
        var command: String = "",

        @XCollection(
            propertyElementName = "parameters",
            style = XCollection.Style.v2,
            elementTypes = [StringParam::class, IntParam::class, DoubleParam::class, BooleanParam::class, ListParam::class]
        )
        var namedParameters: MutableList<NamedParameter> = mutableListOf()
    ) {
        init {
            namedParameters = namedParameters.filterNotNull().toMutableList()
        }
    }

    @Tag("NamedParameter")
    abstract class NamedParameter(
        @Attribute("key")
        open var key: String = "",

        @Attribute("argName")
        open var argName: String = "",

        @Attribute("required")
        open var required: Boolean = false,

        @Attribute("description")
        open var description: String = ""
    )

    @Tag("StringParam")
    class StringParam(
        key: String = "",
        argName: String = "",
        required: Boolean = false,
        description: String = "",
        @Attribute("value")
        var value: String = ""
    ) : NamedParameter(key, argName, required, description)

    @Tag("IntParam")
    class IntParam(
        key: String = "",
        argName: String = "",
        required: Boolean = false,
        description: String = "",
        @Attribute("value")
        var value: Int = 0
    ) : NamedParameter(key, argName, required, description)

    @Tag("DoubleParam")
    class DoubleParam(
        key: String = "",
        argName: String = "",
        required: Boolean = false,
        description: String = "",
        @Attribute("value")
        var value: Double = 0.0
    ) : NamedParameter(key, argName, required, description)

    @Tag("BooleanParam")
    class BooleanParam(
        key: String = "",
        argName: String = "",
        required: Boolean = false,
        description: String = "",
        @Attribute("value")
        var value: Boolean = false
    ) : NamedParameter(key, argName, required, description)

    @Tag("ListParam")
    class ListParam(
        key: String = "",
        argName: String = "",
        required: Boolean = false,
        description: String = "",
        @Attribute("value")
        var value: String = "",

        @XCollection(propertyElementName = "allowedValues", elementName = "option")
        var allowedValues: List<String> = emptyList()
    ) : NamedParameter(key, argName, required, description)

    companion object {
        fun getInstance(): LLMSettings {
            return ApplicationManager.getApplication().getService(LLMSettings::class.java)
        }

        // The problematic 'init' block has been removed from here.
    }

    override fun getState(): State = myState

    override fun getAllConfigurationNames(): List<String> {
        return myState.configurations.map { it.name }
    }

    override fun getConfiguration(name: String): LLMModelConfig? {
        return getConfigurationByName(name)?.toDomain()
    }

    override fun loadState(state: State) {
        myState = state
        println("DEBUG: Loading LLMSettings state")

        myState.configurations.forEach { config ->
            println("DEBUG: Loaded configuration -> ${config.name}")

            val specResource = when (config.name.lowercase()) {
                "chatgpt" -> "python/gpt_specifications.json"
                "gemini" -> "python/gemini_specifications.json"
                "deepseek" -> "python/deepseek_specifications.json"
                else -> null
            }

            val tempDir = System.getProperty("java.io.tmpdir")

            if (specResource != null) {
                // Only overwrite if it's already pointing to a temp file or if it's empty
                if (config.parameterSpecFilePath.isBlank() || config.parameterSpecFilePath.startsWith(tempDir)) {
                    config.parameterSpecFilePath = copyResourceToTempFile(specResource, ".json")
                }
                
                // Force native for default models if they are currently pointing to legacy scripts
                if (config.scriptFilePath.isBlank() || config.scriptFilePath.startsWith(tempDir)) {
                    config.scriptFilePath = "native"
                }
                if (config.command.isBlank() || config.command.lowercase().contains("python")) {
                    config.command = "native"
                }
            }

            config.namedParameters = ensureNamedParameters(config.namedParameters)

            config.namedParameters.forEach { param ->
                if (param is StringParam && param.key == "Instruction Prompt Path") {
                    // Only update if it points to our managed temp space
                    if (param.value.isBlank() || param.value.contains("bddtestgen_")) {
                        println("DEBUG: Updating default instruction prompt content...")
                        param.value = copyResourceToTempFile(
                            "python/message_1_response=user.txt",
                            ".txt"
                        )
                    }
                }
            }

            println("DEBUG: Fixed parameters -> ${config.namedParameters}")
        }

        addDefaultConfigurationsIfMissing()
    }


    fun addDefaultConfigurationsIfMissing() {
        val configs = listOf(
            Pair("ChatGPT", "gpt_specifications.json"),
            Pair("Gemini", "gemini_specifications.json"),
            Pair("DeepSeek", "deepseek_specifications.json")
        )

        val instructionFilePath = getDefaultInstructionFilePath()

        configs.forEach { (name, specFile) ->
            if (myState.configurations.none { it.name == name }) {
                val specPath = copyResourceToTempFile("python/$specFile", ".json")

                val config = LLMConfiguration(
                    name = name,
                    scriptFilePath = "native",
                    parameterSpecFilePath = specPath,
                    command = "native",
                    namedParameters = mutableListOf(
                        StringParam(
                            key = "Instruction Prompt Path",
                            argName = "--instruction_file",
                            required = true,
                            description = "Path to the instruction prompt file",
                            value = instructionFilePath
                        )
                    )
                )

                println("DEBUG: Adding default config -> $name")
                myState.configurations.add(config)
            }
        }
    }

    private fun getDefaultInstructionFilePath(): String {
        return copyResourceToTempFile("python/message_1_response=user.txt", ".txt")
    }

    private fun copyResourceToTempFile(resourcePath: String, suffix: String = ""): String {
        val inputStream = javaClass.classLoader.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")

        val fileName = resourcePath.substringAfterLast("/")
        val tempDir = System.getProperty("java.io.tmpdir")
        val tempFile = File(tempDir, "bddtestgen_$fileName")
        
        // Always overwrite to ensure updates are reflected
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        println("DEBUG: Updated resource $resourcePath at ${tempFile.absolutePath}")
        return tempFile.absolutePath
    }

    fun addConfiguration(config: LLMConfiguration) {
        if (!isValidFilePath(config.parameterSpecFilePath)) {
            throw IllegalArgumentException("Invalid spec file path provided.")
        }

        val existingConfig = myState.configurations.find { it.name == config.name }

        if (existingConfig == null) {
            myState.configurations.add(config)
        } else {
            throw IllegalArgumentException("Configuration with the same name already exists.")
        }
    }

    fun removeConfiguration(config: LLMConfiguration) {
        myState.configurations.remove(config)
    }

    fun getConfigurations(): List<LLMConfiguration> = myState.configurations

    fun updateConfiguration(oldConfig: LLMConfiguration, newConfig: LLMConfiguration) {
        val index = myState.configurations.indexOf(oldConfig)
        if (index != -1) {
            myState.configurations[index] = newConfig
        }
    }

    fun getConfigurationByName(name: String): LLMConfiguration? {
        val config = myState.configurations.find { it.name == name }

        if (config == null) {
            println("DEBUG: No configuration found with name '$name'")
        } else {
            println("DEBUG: Configuration found -> ${config.name}")
            config.namedParameters = ensureNamedParameters(config.namedParameters)
            println("DEBUG: Fixed parameters -> ${config.namedParameters}")
        }

        return config
    }

    internal fun ensureNamedParameters(parameters: MutableList<NamedParameter>?): MutableList<NamedParameter> {
        if (parameters == null) return mutableListOf()

        val fixedParameters = mutableListOf<NamedParameter>()

        parameters.forEach { param ->
            if (param != null) {
                fixedParameters.add(param)
            }
        }

        return fixedParameters
    }

    private fun isValidFilePath(path: String): Boolean = File(path).exists()
}

// Extension Mappers para separar Domínio de Infraestrutura
fun LLMSettings.LLMConfiguration.toDomain() = LLMModelConfig(
    name = this.name,
    scriptFilePath = this.scriptFilePath,
    parameterSpecFilePath = this.parameterSpecFilePath,
    command = this.command,
    namedParameters = this.namedParameters.map { it.toDomain() }
)

fun LLMSettings.NamedParameter.toDomain(): ModelParameter = when (this) {
    is LLMSettings.StringParam -> StringParam(key, argName, required, description, value)
    is LLMSettings.BooleanParam -> BooleanParam(key, argName, required, description, value)
    is LLMSettings.DoubleParam -> DoubleParam(key, argName, required, description, value)
    is LLMSettings.ListParam -> ListParam(key, argName, required, description, value, allowedValues)
}