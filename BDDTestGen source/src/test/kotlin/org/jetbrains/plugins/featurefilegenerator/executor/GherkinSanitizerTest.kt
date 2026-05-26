package org.jetbrains.plugins.featurefilegenerator.executor


import org.junit.Assert.assertEquals
import org.junit.Test

class GherkinSanitizerTest {

    private val mockProvider = object : org.jetbrains.plugins.featurefilegenerator.domain.LLMConfigProvider {
        override fun getConfiguration(name: String) = null
        override fun getAllConfigurationNames() = emptyList<String>()
    }

    @Test
    fun `test stripGherkinFormatting removes markdown blocks`() {
        val executor = LLMExecutor(mockProvider)
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
        val executor = LLMExecutor(mockProvider)
        val input = "Feature: Only text"
        assertEquals("Feature: Only text", executor.stripGherkinFormatting(input))
    }

    @Test
    fun `test stripGherkinFormatting removes generic code blocks`() {
        val executor = LLMExecutor(mockProvider)
        val input = "```\nFeature: Generic\n```"
        assertEquals("Feature: Generic", executor.stripGherkinFormatting(input).trim())
    }

    @Test
    fun `extracts gherkin block embedded in mixed markdown prose`() {
        // Real-shaped Gemini response: surrounding prose, fenced gherkin in
        // the middle, more prose / tables after. Regression for the v2.3.1
        // smoke-test finding where the entire markdown blob was being saved
        // to the .feature file because no top-level fence was detected.
        val executor = LLMExecutor(mockProvider)
        val input = """
            Based on your user story, here are the BDD scenarios.

            ### Acceptance Criteria
            * AC 1: Successful Login

            ### BDD / Gherkin Scenarios

            ```gherkin
            Feature: Administrator Login

              Scenario: Successful login
                Given the user is on the login page
                When they enter valid credentials
                Then they are redirected to the dashboard
            ```

            ### Functional Test Cases (for QA)

            | TC | Description |
            | -- | ----------- |
            | 1  | …           |
        """.trimIndent()

        val output = executor.stripGherkinFormatting(input)
        // The result must be ONLY the Gherkin body — no prose, no markdown
        // headings, no tables.
        assertEquals(
            "Feature: Administrator Login\n\n  Scenario: Successful login\n    Given the user is on the login page\n    When they enter valid credentials\n    Then they are redirected to the dashboard",
            output
        )
    }

    @Test
    fun `extracts plain fenced block when body starts with Feature`() {
        // Some models emit a plain ``` fence without the "gherkin" hint, but
        // the body still starts with "Feature:" — treat it as gherkin.
        val executor = LLMExecutor(mockProvider)
        val input = """
            Here is the file:

            ```
            Feature: Login
              Scenario: ok
            ```

            Hope that helps!
        """.trimIndent()

        val output = executor.stripGherkinFormatting(input)
        assertEquals("Feature: Login\n  Scenario: ok", output)
    }
}
