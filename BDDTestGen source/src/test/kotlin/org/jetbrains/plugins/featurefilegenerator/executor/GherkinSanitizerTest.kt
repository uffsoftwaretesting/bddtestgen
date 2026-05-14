package org.jetbrains.plugins.featurefilegenerator.executor


import org.junit.Assert.assertEquals
import org.junit.Test

class GherkinSanitizerTest {

    @Test
    fun `test stripGherkinFormatting removes markdown blocks`() {
        val executor = LLMExecutor(Any())
        val input = """
            ```gherkin
            Feature: Test
              Scenario: Hello
            ```
        """.trimIndent()
        
        val expected = "Feature: Test\n  Scenario: Hello"
        assertEquals(expected, executor.stripGherkinFormatting(input).trim())
    }

    @Test
    fun `test stripGherkinFormatting works with plain text`() {
        val executor = LLMExecutor(Any())
        val input = "Feature: Only text"
        assertEquals("Feature: Only text", executor.stripGherkinFormatting(input))
    }

    @Test
    fun `test stripGherkinFormatting removes generic code blocks`() {
        val executor = LLMExecutor(Any())
        val input = "```\nFeature: Generic\n```"
        assertEquals("Feature: Generic", executor.stripGherkinFormatting(input).trim())
    }
}
