package org.jetbrains.plugins.featurefilegenerator.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import org.jetbrains.plugins.featurefilegenerator.LLMConfigurationPanel
import org.jetbrains.plugins.featurefilegenerator.LLMSettings
import java.awt.BorderLayout
import java.io.File
import javax.swing.*

class ChangeConfigsAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        val dialog = object : DialogWrapper(project) {
            private val configurationPanel = LLMConfigurationPanel(project)
            private val llmSettings = LLMSettings.getInstance()
            private val deleteButton = JButton("Delete Configuration")

            init {
                title = "LLM Settings"
                init()
            }

            override fun createCenterPanel(): JComponent {
                val panel = JPanel(BorderLayout())
                panel.add(configurationPanel, BorderLayout.CENTER)

                val bottomPanel = JPanel()
                bottomPanel.add(deleteButton)
                panel.add(bottomPanel, BorderLayout.SOUTH)

                deleteButton.addActionListener {
                    deleteSelectedConfiguration()
                }

                return panel
            }

            override fun doOKAction() {
                val selectedConfigName = configurationPanel.configurationComboBox.selectedItem as? String ?: return
                val existingConfig = llmSettings.getConfigurationByName(selectedConfigName)

                if (existingConfig == null) {
                    JOptionPane.showMessageDialog(null, "Configuration not found!", "Error", JOptionPane.ERROR_MESSAGE)
                    return
                }

                val updatedParams = mutableListOf<LLMSettings.NamedParameter>()

                for ((key, component) in configurationPanel.parameterFieldMap) {
                    // Try to find in spec for extra metadata (required, description)
                    val paramSpec = if (existingConfig.parameterSpecFilePath.isNotBlank() && File(existingConfig.parameterSpecFilePath).exists()) {
                        configurationPanel.loadParameterSpecifications(existingConfig.parameterSpecFilePath)
                            .find { it["name"]?.toString() == key }
                    } else null

                    val required = paramSpec?.get("required") as? Boolean ?: false
                    val description = paramSpec?.get("description")?.toString() ?: ""
                    val argName = paramSpec?.get("argName")?.toString() ?: key // Default to key if no spec

                    val param: LLMSettings.NamedParameter? = when {
                        component is JBTextField -> {
                            val textValue = component.text.trim()
                            if (required && textValue.isEmpty()) {
                                showError("The field '$key' is required.")
                                return
                            }
                            LLMSettings.StringParam(key, argName, required, description, textValue)
                        }
                        component is JCheckBox -> {
                            LLMSettings.BooleanParam(key, argName, required, description, component.isSelected)
                        }
                        component is ComboBox<*> -> {
                            val selectedValue = component.selectedItem?.toString() ?: ""
                            val allowedValues = (paramSpec?.get("allowed_values") as? List<*>)?.map { it.toString() } ?: emptyList()
                            LLMSettings.ListParam(key, argName, required, description, selectedValue, allowedValues)
                        }
                        component is JSpinner -> {
                            val value = (component.value as Number).toDouble()
                            LLMSettings.DoubleParam(key, argName, required, description, value)
                        }
                        else -> null
                    }

                    param?.let { updatedParams.add(it) }
                }

                // ✅ Extract script file, config file, and command
                val apiUrlField = configurationPanel.parameterFieldMap["API URL:"] as? JBTextField
                val apiBodyField = configurationPanel.parameterFieldMap["API Body Template:"] as? JBTextField
                val apiPathField = configurationPanel.parameterFieldMap["API Result JSON Path:"] as? JBTextField
                val outputDirField = configurationPanel.parameterFieldMap["Output Directory:"] as? JBTextField

                val updatedApiUrl = apiUrlField?.text?.trim() ?: existingConfig.apiUrl
                val updatedApiBody = apiBodyField?.text?.trim() ?: existingConfig.apiBodyTemplate
                val updatedApiPath = apiPathField?.text?.trim() ?: existingConfig.apiResultPath
                val updatedOutputDir = outputDirField?.text?.trim()?.ifBlank {
                    project.basePath ?: System.getProperty("user.home")
                } ?: existingConfig.outputDirectory

                if (existingConfig.scriptFilePath != "native" && updatedApiUrl.isEmpty()) {
                    showError("API URL is required for custom configurations.")
                    return
                }

                // ✅ Preserve hidden parameters that the UI didn't render
                // (e.g. native configs hide "Instruction Prompt Path", "Debug",
                // and "Output Directory" — losing those StringParams on save
                // silently breaks features like the BDD instruction prompt that
                // tells the model to emit a .feature file).
                //
                // For booleans, unrendered means "unchecked" (default to false).
                // For everything else, keep whatever was already persisted.
                for (param in existingConfig.namedParameters) {
                    if (updatedParams.any { it.key == param.key }) continue
                    when (param) {
                        is LLMSettings.BooleanParam -> updatedParams.add(
                            LLMSettings.BooleanParam(param.key, param.argName, param.required, param.description, false)
                        )
                        else -> updatedParams.add(param)
                    }
                }

                // ✅ Build and save updated config
                val updatedConfig = LLMSettings.LLMConfiguration(
                    name = existingConfig.name,
                    scriptFilePath = existingConfig.scriptFilePath,
                    parameterSpecFilePath = existingConfig.parameterSpecFilePath,
                    command = existingConfig.command,
                    apiUrl = updatedApiUrl,
                    apiBodyTemplate = updatedApiBody,
                    apiResultPath = updatedApiPath,
                    outputDirectory = updatedOutputDir,
                    namedParameters = updatedParams
                )

                llmSettings.updateConfiguration(existingConfig, updatedConfig)
                llmSettings.setSelectedLLM(selectedConfigName)

                super.doOKAction()
            }


            private fun deleteSelectedConfiguration() {
                val selectedConfigName = configurationPanel.configurationComboBox.selectedItem as? String ?: return
                val existingConfig = llmSettings.getConfigurationByName(selectedConfigName)

                if (existingConfig != null) {
                    val confirmation = JOptionPane.showConfirmDialog(
                        null,
                        "Are you sure you want to delete the configuration '$selectedConfigName'?",
                        "Confirmation",
                        JOptionPane.YES_NO_OPTION
                    )

                    if (confirmation == JOptionPane.YES_OPTION) {
                        llmSettings.removeConfiguration(existingConfig)
                        configurationPanel.configurationComboBox.removeItem(selectedConfigName)

                        JOptionPane.showMessageDialog(null, "Configuration '$selectedConfigName' deleted successfully!")
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Configuration not found!", "Error", JOptionPane.ERROR_MESSAGE)
                }
            }

            private fun showError(message: String) {
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE)
            }
        }
        dialog.show()
    }
}
