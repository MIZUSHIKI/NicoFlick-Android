package com.mizushiki.nicoflick_a

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Guard the editor's separation from normal-game navigation and score persistence. */
class EditorGameIsolationTest {
    private fun source(name: String): String {
        val relative = "src/main/java/com/mizushiki/nicoflick_a/$name.kt"
        val file = listOf(File(relative), File("app/$relative")).firstOrNull { it.isFile }
        return requireNotNull(file) { "Production source not found: $name" }.readText()
    }

    @Test fun editorGameHasNoResultsOrPersistenceDependencies() {
        val game = source("EditorGame")
        for (forbidden in listOf("Activity_GameView", "Activity_Result", "ServerDataHandler",
            "ScoreDataLists", "USERDATA", "GLOBAL", "startActivity", "EditorApi")) {
            assertFalse("EditorGame must not depend on $forbidden", game.contains(forbidden))
        }
    }

    @Test fun editorUsesEmbeddedSessionAndNormalGameHasNoEditorMode() {
        val editor = source("Activity_Editor")
        assertTrue(editor.contains("EditorGame(this,"))
        assertFalse(editor.contains("Activity_GameView"))
        assertFalse(source("Activity_GameView").contains("editorPreview"))
    }
}
