package org.jetbrains.plugins.featurefilegenerator.executor

import org.jetbrains.plugins.featurefilegenerator.cli.LLMSettingsCLI
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class LLMExecutorTest {

    @Test
    fun `test runProcess returns error when API key is missing`() {
        val settings = org.jetbrains.plugins.featurefilegenerator.LLMSettings()
        settings.loadState(org.jetbrains.plugins.featurefilegenerator.LLMSettings.State())
        
        val dummySpec = File.createTempFile("dummy_spec", ".json")
        dummySpec.writeText("[]")
        dummySpec.deleteOnExit()

        val emptyConfig = org.jetbrains.plugins.featurefilegenerator.LLMSettings.LLMConfiguration(
            name = "TestLLM",
            scriptFilePath = "native",
            parameterSpecFilePath = dummySpec.absolutePath,
            command = "native",
            namedParameters = mutableListOf()
        )
        settings.addConfiguration(emptyConfig)
        
        val executor = LLMExecutor(settings)
        
        // Create a dummy file
        val tempFile = File.createTempFile("dummy_story", ".txt")
        tempFile.writeText("User story content")
        tempFile.deleteOnExit()
        
        // When we call runProcess (which is private but executed via execute or we can call execute and check the callback)
        var capturedResult = ""
        
        // Since execute runs in a ProgressManager Task (which fails outside IntelliJ), 
        // we can test the internal runProcess by reflection, or we can test parsing behavior.
        
        val method = LLMExecutor::class.java.getDeclaredMethod(
            "runProcess", 
            Any::class.java,
            String::class.java
        )
        method.isAccessible = true
        
        val result = method.invoke(executor, emptyConfig, tempFile.absolutePath) as String
        
        // Then it should return the specific error message
        assertTrue("Result should indicate missing API key, but was: $result", 
            result.contains("API Key (--api_key) not provided"))
    }
}
