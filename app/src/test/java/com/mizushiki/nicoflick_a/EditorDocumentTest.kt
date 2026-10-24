package com.mizushiki.nicoflick_a

import org.junit.Assert.*
import org.junit.Test

class EditorDocumentTest {
    @Test fun publishedOffsetsAreRemovedOnImportAndAppliedExactlyOnceOnExport() {
        val doc = EditorDocument("[00:01.20]あ[00:02:20]い\n@NicoFlick=1\n@OffsetWhenPosted=200\n")
        assertEquals(1000, doc.characters[0].timeMs)
        assertEquals(200, doc.offsetMs)
        assertTrue(doc.characters[0].note)
        val export = doc.export(EditorDocument.Export.PUBLISHED)
        assertTrue(export.startsWith("[00:01.20]あ[00:02:20]い"))
        assertTrue(export.contains("@OffsetWhenPosted=200"))
        assertEquals(1000, EditorDocument(export).characters[0].timeMs)
    }
    @Test fun localAndDraftFormatsPreserveUnassignedNoteAndStripPrivateMetadataOnPost() {
        val doc = EditorDocument("[**:**.**]あ\n@Creator=作成者\n@Description=説明\n@Level=3\n@Speed=200\n@Offset=50\n")
        val local = doc.export(EditorDocument.Export.LOCAL)
        val restored = EditorDocument(local)
        assertEquals(-1, restored.characters[0].timeMs); assertTrue(restored.characters[0].note)
        assertTrue(local.contains("@NicoFlick=3"))
        val draft = doc.export(EditorDocument.Export.DRAFT)
        assertTrue(draft.contains("@NicoFlick=2")); assertTrue(draft.contains("@Offset=50"))
        assertFalse(draft.contains("@Creator=")); assertFalse(draft.contains("@Level="))
        val post = doc.export(EditorDocument.Export.PUBLISHED)
        assertTrue(post.contains("@NicoFlick=1")); assertFalse(post.contains("@Description="))
    }
    @Test fun undoRedoRestoresInsertionDeletionCursorAndMetadataAndNewEditClearsRedo() {
        val doc = EditorDocument("あい\n")
        doc.cursor = 1; doc.insertText("う")
        assertEquals("あうい\n", doc.characters.joinToString("") { it.word })
        assertTrue(doc.undo()); assertEquals(1, doc.cursor)
        assertEquals("あい\n", doc.characters.joinToString("") { it.word })
        assertTrue(doc.redo()); doc.backDelete()
        assertEquals("あい\n", doc.characters.joinToString("") { it.word })
        doc.setMetadata(mapOf("Speed" to "220")); assertEquals(220, doc.speed)
        doc.undo(); assertEquals(150, doc.speed)
        doc.stamp(1200); assertFalse(doc.canRedo)
    }
    @Test fun stampingAdvancesPastSmallKanaAndPunctuation() {
        val doc = EditorDocument("きゃー、く\n")
        doc.stamp(1000)
        assertEquals(1000, doc.characters[0].timeMs); assertEquals(4, doc.cursor)
        assertFalse(doc.characters[1].checkable); assertFalse(doc.characters[2].checkable)
        assertTrue(EditorCharacter("ガ").checkable)
    }
    @Test fun realTimeNoteMarkingUsesNearestTimeAndRejectsOutOfRange() {
        val doc = EditorDocument("[00:02:00]あ[00:03:00]い\n")
        assertFalse(doc.markAt(0, true))
        assertTrue(doc.markAt(2800, true)); assertEquals(1, doc.cursor)
        assertTrue(doc.characters[1].note)
        assertEquals(3000, doc.removePrevious(true)); assertFalse(doc.characters[1].note)
        assertEquals(3000, doc.removePrevious(false)); assertEquals(-1, doc.characters[1].timeMs)
    }
    @Test fun draftAndPublishedExportsProduceEquivalentGameplayTimings() {
        val doc = EditorDocument("[00:01.00]あ[00:02:00]い\n@Offset=120\n@Level=3\n")
        fun notes(mode: EditorDocument.Export) = Notes().apply { noteAnalyze(doc.export(mode), 150, 3) }
        val draft = notes(EditorDocument.Export.DRAFT); val post = notes(EditorDocument.Export.PUBLISHED)
        assertEquals(draft.notes.size, post.notes.size)
        for (i in draft.notes.indices) {
            assertEquals(draft.notes[i].time, post.notes[i].time, 0.00001)
            assertEquals(draft.notes[i].isFlickable, post.notes[i].isFlickable)
        }
    }
    @Test fun publicationRejectsMissingTimesReversedTimesAndNegativeOffset() {
        assertNotNull(EditorDocument("あいう\n").publicationProblem())
        assertNotNull(EditorDocument("[**:**.**]あ[00:01.00]い\n").publicationProblem())
        assertNotNull(EditorDocument("[00:02.00]あ[00:01.00]い\n").publicationProblem())
        assertNotNull(EditorDocument("[00:00.10]あ\n@Offset=-200\n").publicationProblem())
        assertNull(EditorDocument("[00:01.00]あ[00:02.00]い\n@Level=2\n").publicationProblem())
    }
    @Test fun playbackRateAndStampOffsetUseIPhoneCompensationFormula() {
        assertEquals(4885, EditorDocument.correctedStamp(5000, 1f, -115, 0.07))
        assertEquals(4920, EditorDocument.correctedStamp(5000, 0.5f, -115, 0.07))
        assertEquals(0, EditorDocument.correctedStamp(10, 1f, -115, 0.07))
    }
    @Test fun apiEncodingKeepsPasswordAndJapaneseTextWithinTheirFields() {
        val fields = linkedMapOf("userPASS" to "a&b=+?#日本", "notes" to "[00:01.00]あ\n@Offset=0")
        val encoded = EditorApi.form(fields)
        val decoded = encoded.split('&').associate {
            val parts = it.split('=', limit = 2)
            java.net.URLDecoder.decode(parts[0], "UTF-8") to java.net.URLDecoder.decode(parts[1], "UTF-8")
        }
        assertEquals(fields, decoded)
        assertTrue(EditorApi.succeeded("<html><!--EchoHtmlMes_OK--></html>"))
        assertFalse(EditorApi.succeeded(null))
        assertEquals("違います", EditorApi.errorMessage("<!--echo--><p>違います</p><!--message=x--><!--/echo-->"))
    }
    @Test fun offlineReadingConversionPreservesTimetagsMetadataAndLineBreaks() {
        val text = "[00:01.00]今日は歌う\n@Creator=作成者\n"
        val converted = EditorLyrics.hiragana(text)
        assertTrue(converted.startsWith("[00:01.00]きょうはうたう\n"))
        assertTrue(converted.contains("@Creator=作成者"))
        assertTrue(converted.endsWith("\n"))
    }
    @Test fun wikiExtractionUsesRubyReadingAndStopsBeforeNextSection() {
        val html = "<h3 id=\"id_abc\">歌詞</h3><div><ruby><rb>歌</rb><rt>うた</rt></ruby><br />あ</div><h3>コメント</h3>不要"
        val result = EditorLyrics.wikiSection(html)!!
        assertTrue(result.contains("うた")); assertFalse(result.contains("<rb>")); assertFalse(result.contains("不要"))
    }
}
