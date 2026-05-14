package org.jetbrains.plugins.featurefilegenerator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParameterMergingTest {

    @Test
    fun `test ensureNamedParameters preserves existing values`() {
        val settings = LLMSettings()
        val existingParams = mutableListOf<LLMSettings.NamedParameter>(
            LLMSettings.StringParam("API Key", "--api_key", true, "", "OLD_KEY"),
            LLMSettings.DoubleParam("Temperature", "--temp", false, "", 0.5)
        )

        // Simulating loading a spec that has the same parameters but maybe different defaults
        val result = settings.ensureNamedParameters(existingParams)

        val apiKey = result.find { it.key == "API Key" } as LLMSettings.StringParam
        val temp = result.find { it.key == "Temperature" } as LLMSettings.DoubleParam

        assertEquals("OLD_KEY", apiKey.value)
        assertEquals(0.5, temp.value, 0.0)
    }

    @Test
    fun `test ensureNamedParameters handles empty list`() {
        val settings = LLMSettings()
        val result = settings.ensureNamedParameters(null)
        assertTrue(result.isEmpty())
    }
}
