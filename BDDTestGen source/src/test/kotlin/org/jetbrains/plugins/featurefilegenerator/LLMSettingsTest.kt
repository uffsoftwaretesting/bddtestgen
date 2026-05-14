package org.jetbrains.plugins.featurefilegenerator

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class LLMSettingsTest {

    private lateinit var settings: LLMSettings

    @Before
    fun setUp() {
        settings = LLMSettings()
        settings.loadState(LLMSettings.State()) // Initialize with default empty state
    }

    @Test
    fun `test default configurations are added`() {
        // When loadState is called, it should populate the default configs
        val configs = settings.getConfigurations()
        
        assertNotNull("Configurations should not be null", configs)
        assertTrue("Configurations should not be empty", configs.isNotEmpty())
        
        val chatGPTConfig = configs.find { it.name == "ChatGPT" }
        assertNotNull("ChatGPT default configuration should exist", chatGPTConfig)
        
        val geminiConfig = configs.find { it.name == "Gemini" }
        assertNotNull("Gemini default configuration should exist", geminiConfig)
    }

    @Test
    fun `test temp file is created for specs`() {
        val chatGPTConfig = settings.getConfigurations().find { it.name == "ChatGPT" }!!
        
        val specPath = chatGPTConfig.parameterSpecFilePath
        assertTrue("Spec path should be set", specPath.isNotBlank())
        
        val specFile = File(specPath)
        assertTrue("Spec file should exist in the filesystem", specFile.exists())
        assertTrue("Spec file should be a temporary bddtestgen file", specFile.name.startsWith("bddtestgen_"))
    }
}
