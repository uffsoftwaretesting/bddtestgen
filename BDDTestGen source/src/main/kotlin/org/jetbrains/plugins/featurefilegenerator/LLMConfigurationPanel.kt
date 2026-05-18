package org.jetbrains.plugins.featurefilegenerator

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class LLMConfigurationPanel(private val project: Project?) : JPanel(BorderLayout()) {

    private val llmSettings = LLMSettings.getInstance()
    val configurationComboBox = ComboBox<String>()
    private val dynamicPanel = JPanel(GridBagLayout())
    private val addNewLabel = "Insert new"
    val parameterFieldMap = mutableMapOf<String, JComponent>()

    init {
        setupUI()
    }

    private fun updateComboBoxItems() {
        val configurations = llmSettings.getConfigurations().map { it.name } + addNewLabel
        val selectedLLM = llmSettings.getSelectedLLM() ?: configurations.firstOrNull()

        configurationComboBox.model = DefaultComboBoxModel(configurations.toTypedArray())
        configurationComboBox.selectedItem = selectedLLM
    }

    private fun setupUI() {
        updateComboBoxItems()

        configurationComboBox.addActionListener {
            val selected = configurationComboBox.selectedItem as? String
            llmSettings.setSelectedLLM(selected) // Save selection on change
            onConfigurationSelected()
        }

        val topPanel = JPanel(GridBagLayout()).apply {
            val gbc = GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                anchor = GridBagConstraints.WEST
                insets = Insets(5, 5, 5, 5)
            }
            add(JBLabel("Select or insert a new LLM:"), gbc)

            gbc.gridx = 1
            gbc.fill = GridBagConstraints.HORIZONTAL
            gbc.weightx = 1.0
            add(configurationComboBox, gbc)
        }

        add(topPanel, BorderLayout.NORTH)
        add(JScrollPane(dynamicPanel), BorderLayout.CENTER)

        onConfigurationSelected()
    }

    private fun onConfigurationSelected() {
        dynamicPanel.removeAll()
        val selected = configurationComboBox.selectedItem as? String ?: return

        if (selected == addNewLabel) {
            renderNewConfigurationFields()
        } else {
            val configuration = llmSettings.getConfigurationByName(selected)
            renderExistingConfigurationFields(configuration)
        }

        dynamicPanel.revalidate()
        dynamicPanel.repaint()
    }

    private fun renderExistingConfigurationFields(existingConfig: LLMSettings.LLMConfiguration?) {
        dynamicPanel.removeAll()
        parameterFieldMap.clear()

        if (existingConfig == null) {
            dynamicPanel.add(JBLabel("Configuration not found."), GridBagConstraints())
            return
        }

        val gbc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            gridy = 0
            insets = Insets(5, 5, 5, 5)
        }

        fun addRow(label: String, component: JComponent) {
            gbc.gridx = 0
            gbc.weightx = 0.0
            dynamicPanel.add(JBLabel(label), gbc)

            gbc.gridx = 1
            gbc.weightx = 1.0
            dynamicPanel.add(component, gbc)
            gbc.gridy++

            parameterFieldMap[label] = component
        }

        val nameField = JBTextField(existingConfig.name)
        addRow("LLM Name:", nameField)

        if (existingConfig.scriptFilePath != "native") {
            val apiUrlField = JBTextField(existingConfig.apiUrl)
            addRow("API URL:", apiUrlField)
            
            val apiBodyField = JBTextField(existingConfig.apiBodyTemplate)
            addRow("API Body Template:", apiBodyField)
            
            val apiPathField = JBTextField(existingConfig.apiResultPath)
            addRow("API Result JSON Path:", apiPathField)
        }

        // --- Auto-Discovery of Parameters from Template ---
        val template = existingConfig.apiBodyTemplate + existingConfig.apiUrl
        val discoveredVars = Regex("\\{\\{([a-zA-Z0-9_]+)\\}\\}").findAll(template)
            .map { it.groupValues[1] }
            .filter { it != "story" } // Story is reserved
            .distinct()
            .toList()

        for (varName in discoveredVars) {
            val paramValue = existingConfig.namedParameters.find { it.key == varName }
            val labelName = varName.replace("_", " ").capitalize()
            val field = JBTextField(paramValue?.getValueAsString() ?: "")
            addRow("$labelName:", field)
            parameterFieldMap[varName] = field // Map by variable key
        }

        // --- Load parameters from spec file only if provided and exists ---
        val configPath = existingConfig.parameterSpecFilePath
        if (configPath.isNotBlank() && File(configPath).exists()) {
            val specifications = loadParameterSpecifications(configPath)
            for (spec in specifications) {
                val paramName = spec["name"]?.toString() ?: "Unnamed"
                
                // For native configurations, hide hardcoded/natively handled fields (e.g. prompt path, output dir, debug)
                if (existingConfig.scriptFilePath == "native") {
                    if (paramName == "Instruction Prompt Path" || paramName == "Output Directory" || paramName == "Debug") {
                        continue
                    }
                }
                
                val paramValue = existingConfig.namedParameters.find { it.key == paramName }
                val component = createUIComponentForParameter(spec, paramValue)
                addRow(paramName, component)
            }
        }

        dynamicPanel.revalidate()
        dynamicPanel.repaint()
    }

    private fun createUIComponentForParameter(
        spec: Map<String, Any>,
        existingValue: LLMSettings.NamedParameter?
    ): JComponent {
        val paramName = spec["name"]?.toString() ?: "Unnamed"

        val isInstructionPrompt = paramName == "Instruction Prompt Path"
        val isOutputDirectory = paramName == "Output Directory"

        val dynamicDefault = when {
            isInstructionPrompt -> getRuntimeInstructionFilePath()
            isOutputDirectory -> project?.basePath ?: System.getProperty("user.home")
            else -> null
        }

        val defaultValue = existingValue?.let {
            when (it) {
                is LLMSettings.StringParam -> it.value
                is LLMSettings.BooleanParam -> it.value.toString()
                is LLMSettings.ListParam -> it.value
                is LLMSettings.DoubleParam -> it.value.toString()
                else -> spec["default_value"]?.toString() ?: dynamicDefault
            }
        } ?: dynamicDefault ?: spec["default_value"]?.toString()

        return when (spec["ui_element"]?.toString()) {
            "textfield", "filePicker" -> JBTextField(defaultValue ?: "")
            "checkbox" -> JCheckBox(paramName).apply {
                isSelected = defaultValue?.toBoolean() ?: false
            }
            "combobox" -> {
                val allowedValues = (spec["allowed_values"] as? List<*>)?.map { it.toString() } ?: emptyList()
                ComboBox(allowedValues.toTypedArray()).apply {
                    selectedItem = defaultValue
                }
            }
            "spinner" -> {
                val allowedValues = spec["allowed_values"] as? Map<*, *>
                val min = (allowedValues?.get("min") as? Number)?.toDouble() ?: 0.0
                val max = (allowedValues?.get("max") as? Number)?.toDouble() ?: 1.0
                val step = (spec["step"] as? Number)?.toDouble() ?: 0.1
                val value = defaultValue?.toDoubleOrNull() ?: min
                JSpinner(SpinnerNumberModel(value, min, max, step))
            }
            else -> JBTextField(defaultValue ?: "")
        }
    }



    private fun getRuntimeInstructionFilePath(): String {
        val resourcePath = "python/message_1_response=user.txt"
        val inputStream = javaClass.classLoader.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")

        val tempFile = File.createTempFile("message_1_response_user", ".txt")
        tempFile.deleteOnExit()

        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        println("DEBUG: Instruction file copied to ${tempFile.absolutePath}")
        return tempFile.absolutePath
    }


    fun loadParameterSpecifications(path: String): List<Map<String, Any>> {
        val file = File(path)
        return if (file.exists()) {
            ObjectMapper().readValue(file)
        } else {
            emptyList()
        }
    }

    private fun createHorizontalPanel(field: JBTextField, button: JButton): JPanel {
        return JPanel(BorderLayout()).apply {
            add(field, BorderLayout.CENTER)
            add(button, BorderLayout.EAST)
        }
    }

    private fun renderNewConfigurationFields() {
        dynamicPanel.removeAll()
        parameterFieldMap.clear()

        val gbc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            gridy = 0
            insets = Insets(5, 5, 5, 5)
        }

        fun addRow(label: String, component: JComponent) {
            gbc.gridx = 0
            gbc.weightx = 0.0
            dynamicPanel.add(JBLabel(label), gbc)

            gbc.gridx = 1
            gbc.weightx = 1.0
            dynamicPanel.add(component, gbc)
            gbc.gridy++

            parameterFieldMap[label] = component
        }

        val nameField = JBTextField()
        addRow("LLM Name:", nameField)

        val apiUrlField = JBTextField()
        addRow("API URL:", apiUrlField)
        
        val apiBodyField = JBTextField()
        addRow("API Body Template:", apiBodyField)
        
        val apiPathField = JBTextField()
        addRow("API Result JSON Path:", apiPathField)

        gbc.gridwidth = 2
        val saveButton = JButton("Save").apply {
            addActionListener {
                saveNewConfiguration(
                    nameField.text,
                    apiUrlField.text,
                    apiBodyField.text,
                    apiPathField.text
                )
            }
        }
        dynamicPanel.add(saveButton, gbc)

        dynamicPanel.revalidate()
        dynamicPanel.repaint()
    }

    private fun saveNewConfiguration(name: String, apiUrl: String, body: String, path: String) {
        if (name.isBlank() || apiUrl.isBlank() || body.isBlank()) {
            JOptionPane.showMessageDialog(this, "Name, API URL and Body Template are required!", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }

        // Check if a configuration with the same name already exists
        val existingConfig = llmSettings.getConfigurationByName(name)
        if (existingConfig != null) {
            JOptionPane.showMessageDialog(this, "A configuration with this name already exists!", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }

        // Create new configuration
        val newConfiguration = LLMSettings.LLMConfiguration(
            name = name,
            scriptFilePath = "", // Legacy field
            parameterSpecFilePath = "", // Legacy field
            command = "", // Legacy field
            apiUrl = apiUrl,
            apiBodyTemplate = body,
            apiResultPath = path
        )

        try {
            // Add the new configuration without removing existing ones
            llmSettings.addConfiguration(newConfiguration)

            // Only update the ComboBox portion
            val updatedConfigurations = llmSettings.getConfigurations().map { it.name } + addNewLabel
            configurationComboBox.model = DefaultComboBoxModel(updatedConfigurations.toTypedArray())

            JOptionPane.showMessageDialog(this, "New configuration added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE)
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(this, "Error saving configuration: ${e.message}", "Error", JOptionPane.ERROR_MESSAGE)
        }
    }

}
