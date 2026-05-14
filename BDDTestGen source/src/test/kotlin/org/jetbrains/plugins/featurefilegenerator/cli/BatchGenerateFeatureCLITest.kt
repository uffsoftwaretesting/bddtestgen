package org.jetbrains.plugins.featurefilegenerator.cli

import org.junit.Test
import org.junit.Assert.*

class BatchGenerateFeatureCLITest {

    @Test
    fun `test valid arguments parsing`() {
        // Just instantiate to ensure no immediate crash and test basic Clikt properties if needed
        val cli = BatchGenerateFeatureCLI()
        assertNotNull(cli)
        assertEquals("bddtestgen", cli.commandName)
    }
}
