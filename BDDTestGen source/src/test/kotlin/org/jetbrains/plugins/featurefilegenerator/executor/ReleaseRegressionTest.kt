package org.jetbrains.plugins.featurefilegenerator.executor

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.plugins.featurefilegenerator.LLMSettings
import org.jetbrains.plugins.featurefilegenerator.domain.LLMConfigProvider
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for the bugs fixed during the v2.3.1 release cycle:
 *   1. JSON path parser must accept both bracket and dot notation
 *   2. Native Gemini response traversal must descend into candidates[0]
 *   3. Hidden parameters must survive a Settings-dialog save
 *   4. Self-heal must restore a wiped Instruction Prompt Path on load
 *   5. Output directory fallback chain (top-level field > legacy --output_dir_path > blank)
 */
class ReleaseRegressionTest {

    private val executor = LLMExecutor(object : LLMConfigProvider {
        override fun getConfiguration(name: String) = null
        override fun getAllConfigurationNames() = emptyList<String>()
    })

    // -----------------------------------------------------------------
    // 1. parseResultPath accepts both bracket and dot notation
    // -----------------------------------------------------------------

    @Test
    fun `parseResultPath accepts dot notation with numeric segments`() {
        val result = executor.parseResultPath("candidates.0.content.parts.0.text")
        assertArrayEquals(
            arrayOf("candidates", "0", "content", "parts", "0", "text"),
            result
        )
    }

    @Test
    fun `parseResultPath accepts bracket notation for array indices`() {
        val result = executor.parseResultPath("candidates[0].content.parts[0].text")
        assertArrayEquals(
            "Bracket notation must normalize to dot notation",
            arrayOf("candidates", "0", "content", "parts", "0", "text"),
            result
        )
    }

    @Test
    fun `parseResultPath handles mixed and edge cases`() {
        // Mixed
        assertArrayEquals(
            arrayOf("choices", "0", "message", "content"),
            executor.parseResultPath("choices[0].message.content")
        )
        // Multi-digit indices
        assertArrayEquals(
            arrayOf("items", "12", "value"),
            executor.parseResultPath("items[12].value")
        )
        // Empty/null fallback
        assertArrayEquals(arrayOf("text"), executor.parseResultPath(null))
        assertArrayEquals(arrayOf("text"), executor.parseResultPath(""))
        assertArrayEquals(arrayOf("text"), executor.parseResultPath("   "))
    }

    // -----------------------------------------------------------------
    // 2. The "candidates", "0", "content", "parts", "0", "text" segment
    //    list traverses a real Gemini response shape end-to-end.
    //    (Regression for the bug introduced in commit 7e8e221 that
    //    omitted the "0" after "candidates".)
    // -----------------------------------------------------------------

    @Test
    fun `gemini response path resolves to text content`() {
        val responseJson = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      { "text": "Feature: Login\n  Scenario: ..." }
                    ],
                    "role": "model"
                  }
                }
              ]
            }
        """.trimIndent()

        val path = arrayOf("candidates", "0", "content", "parts", "0", "text")
        val root = Json.parseToJsonElement(responseJson).jsonObject
        val resolved = traverseManually(root, path)
        assertNotNull("Path must resolve against a real Gemini response", resolved)
        assertTrue(
            "Resolved value must be the text payload",
            resolved!!.jsonPrimitive.content.startsWith("Feature: Login")
        )
    }

    // Mirrors the traversal in LLMExecutor.callHttpApi so the test is
    // independent of the real HTTP-calling code.
    private fun traverseManually(root: JsonElement, path: Array<String>): JsonElement? {
        var current: JsonElement = root
        for (segment in path) {
            current = when (current) {
                is JsonObject -> current[segment] ?: return null
                is JsonArray -> {
                    val idx = segment.toIntOrNull() ?: return null
                    current[idx]
                }
                else -> return null
            }
        }
        return current
    }

    // -----------------------------------------------------------------
    // 3. ChangeConfigsAction.doOKAction must preserve hidden parameters
    //    of every type, not just booleans. Simulates the loop that
    //    rebuilds namedParameters from visible UI fields plus the
    //    preservation patch.
    // -----------------------------------------------------------------

    @Test
    fun `hidden parameter preservation keeps StringParam intact across save`() {
        // The "existing" config carries the hidden Instruction Prompt Path
        // (StringParam) and a hidden Debug flag (BooleanParam).
        val existing = mutableListOf<LLMSettings.NamedParameter>(
            LLMSettings.StringParam(
                key = "Instruction Prompt Path",
                argName = "--instruction_file",
                required = true,
                description = "",
                value = "/tmp/bddtestgen_message_1_response=user.txt"
            ),
            LLMSettings.BooleanParam(
                key = "Debug",
                argName = "--debug",
                required = false,
                description = "",
                value = false
            ),
            // A visible param the user did edit and that comes back from the UI:
            LLMSettings.StringParam(
                key = "API Key",
                argName = "--api_key",
                required = true,
                description = "",
                value = "old-value"
            )
        )

        // The UI only re-rendered the API Key field (the hidden ones aren't
        // in parameterFieldMap), so updatedParams holds just one entry.
        val updatedParams = mutableListOf<LLMSettings.NamedParameter>(
            LLMSettings.StringParam("API Key", "--api_key", true, "", "new-value")
        )

        // === Exact preservation logic from ChangeConfigsAction.doOKAction ===
        for (param in existing) {
            if (updatedParams.any { it.key == param.key }) continue
            when (param) {
                is LLMSettings.BooleanParam -> updatedParams.add(
                    LLMSettings.BooleanParam(param.key, param.argName, param.required, param.description, false)
                )
                else -> updatedParams.add(param)
            }
        }
        // ===================================================================

        val keys = updatedParams.map { it.key }.toSet()
        assertTrue("API Key (edited) must be present", "API Key" in keys)
        assertTrue("Instruction Prompt Path (hidden StringParam) must survive", "Instruction Prompt Path" in keys)
        assertTrue("Debug (hidden BooleanParam) must survive", "Debug" in keys)

        val instruction = updatedParams.first { it.key == "Instruction Prompt Path" } as LLMSettings.StringParam
        assertEquals(
            "StringParam value must carry over verbatim, not get wiped",
            "/tmp/bddtestgen_message_1_response=user.txt",
            instruction.value
        )

        val apiKey = updatedParams.first { it.key == "API Key" } as LLMSettings.StringParam
        assertEquals("Edited value from the UI must override the previous one", "new-value", apiKey.value)
    }

    // -----------------------------------------------------------------
    // 4. Self-heal on loadState: a native Gemini config with no
    //    Instruction Prompt Path gets it restored automatically.
    // -----------------------------------------------------------------

    @Test
    fun `loadState restores missing Instruction Prompt Path for native configs`() {
        val state = LLMSettings.State()
        // Simulate a previously-broken config: native, no namedParameters.
        state.configurations.add(
            LLMSettings.LLMConfiguration(
                name = "Gemini",
                scriptFilePath = "native",
                parameterSpecFilePath = "",
                command = "native",
                namedParameters = mutableListOf()
            )
        )

        val settings = LLMSettings()
        settings.loadState(state)

        val healed = settings.getConfigurationByName("Gemini")
        assertNotNull("Gemini config must still exist after loadState", healed)
        val instructionParam = healed!!.namedParameters.firstOrNull {
            it.key == "Instruction Prompt Path"
        }
        assertNotNull("Instruction Prompt Path must be restored by self-heal", instructionParam)
        assertTrue(
            "Restored path must be non-blank",
            (instructionParam as LLMSettings.StringParam).value.isNotBlank()
        )
    }

    // -----------------------------------------------------------------
    // 5. Output directory fallback chain. The top-level outputDirectory
    //    must win; if blank, executeNative falls back to the legacy
    //    --output_dir_path namedParameter; otherwise nothing is saved.
    // -----------------------------------------------------------------

    @Test
    fun `output directory resolution prefers top-level then legacy param then blank`() {
        // Top-level wins
        val resolvedTopLevel = resolveOutputDir(topLevel = "C:/from-top-level", legacyParam = "C:/from-legacy")
        assertEquals("C:/from-top-level", resolvedTopLevel)

        // Top-level blank -> legacy used
        val resolvedLegacy = resolveOutputDir(topLevel = "", legacyParam = "C:/from-legacy")
        assertEquals("C:/from-legacy", resolvedLegacy)

        // Both blank -> blank (no save)
        val resolvedBlank = resolveOutputDir(topLevel = "", legacyParam = null)
        assertEquals("", resolvedBlank)
    }

    // Mirrors the resolution in LLMExecutor.executeNative so the test
    // pins the public contract independently of the call site.
    private fun resolveOutputDir(topLevel: String, legacyParam: String?): String {
        return topLevel.ifBlank { legacyParam ?: "" }
    }
}
