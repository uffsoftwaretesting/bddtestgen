package org.jetbrains.plugins.featurefilegenerator.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class ModelsTest {

    @Test
    fun testLLMModelConfigInitialization() {
        val config = LLMModelConfig(
            name = "Test Model",
            scriptFilePath = "script.py",
            parameterSpecFilePath = "spec.json",
            command = "python"
        )
        
        assertEquals("Test Model", config.name)
        assertEquals("script.py", config.scriptFilePath)
        assertEquals("spec.json", config.parameterSpecFilePath)
        assertEquals("python", config.command)
        assertEquals(null, config.apiUrl)
        assertEquals(null, config.apiBodyTemplate)
        assertEquals(null, config.apiResultPath)
        assertTrue(config.namedParameters.isEmpty())
    }

    @Test
    fun testStringParam() {
        val param = StringParam("key", "argName", true, "desc", "value")
        assertEquals("key", param.key)
        assertEquals("argName", param.argName)
        assertTrue(param.required)
        assertEquals("desc", param.description)
        assertEquals("value", param.value)
    }

    @Test
    fun testBooleanParam() {
        val param = BooleanParam("k", "arg", false, "d", true)
        assertFalse(param.required)
        assertTrue(param.value)
    }

    @Test
    fun testDoubleParam() {
        val param = DoubleParam("k", "arg", true, "d", 0.75)
        assertEquals(0.75, param.value, 0.001)
    }

    @Test
    fun testListParam() {
        val options = listOf("A", "B", "C")
        val param = ListParam("k", "arg", true, "d", "B", options)
        assertEquals("B", param.value)
        assertEquals(options, param.options)
    }
}
