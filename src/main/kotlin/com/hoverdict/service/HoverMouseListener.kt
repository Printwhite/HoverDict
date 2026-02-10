package com.hoverdict.service

import com.hoverdict.settings.HoverDictSettings
import com.hoverdict.ui.TranslationPopup
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseEventArea
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.editor.event.EditorMouseMotionListener
import java.awt.Point
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class HoverMouseListener : EditorMouseListener, EditorMouseMotionListener {

    private val scheduler = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "HoverDict-Scheduler").apply { isDaemon = true }
    }

    @Volatile
    private var pendingTask: Future<*>? = null

    @Volatile
    private var lastWord: String? = null

    @Volatile
    private var lastPoint: Point? = null

    override fun mouseMoved(e: EditorMouseEvent) {
        val settings = HoverDictSettings.getInstance().state
        if (!settings.enabled) return
        if (e.area != EditorMouseEventArea.EDITING_AREA) {
            cancelAndHide()
            return
        }

        val editor = e.editor
        val point = e.mouseEvent.point
        val offset = editor.logicalPositionToOffset(editor.xyToLogicalPosition(point))
        val document = editor.document
        val text = document.charsSequence

        if (offset < 0 || offset >= text.length) {
            cancelAndHide()
            return
        }

        val word = extractWord(text, offset)
        if (word.isNullOrEmpty() || word.length < 2) {
            cancelAndHide()
            return
        }

        if (word == lastWord && TranslationPopup.getInstance().isVisible()) {
            return
        }

        pendingTask?.cancel(false)
        lastWord = word
        lastPoint = Point(point)

        pendingTask = scheduler.schedule({
            val translation = if (settings.splitIdentifiers && hasMultipleParts(word)) {
                DictionaryService.translateIdentifier(word)
            } else {
                DictionaryService.translate(word)
            }
            if (translation != null) {
                val displayText = if (hasMultipleParts(word) && settings.splitIdentifiers) {
                    translation
                } else {
                    "$word → $translation"
                }
                TranslationPopup.getInstance().show(editor, displayText, lastPoint!!)
            }
        }, settings.hoverDelayMs.toLong(), TimeUnit.MILLISECONDS)
    }

    override fun mouseExited(event: EditorMouseEvent) {
        cancelAndHide()
    }

    private fun cancelAndHide() {
        pendingTask?.cancel(false)
        pendingTask = null
        lastWord = null
        lastPoint = null
        TranslationPopup.getInstance().hideImmediately()
    }

    private fun extractWord(text: CharSequence, offset: Int): String? {
        if (offset < 0 || offset >= text.length) return null
        val c = text[offset]
        if (!c.isLetterOrDigit() && c != '_') return null

        var start = offset
        while (start > 0 && isWordChar(text[start - 1])) {
            start--
        }

        var end = offset
        while (end < text.length - 1 && isWordChar(text[end + 1])) {
            end++
        }

        val word = text.subSequence(start, end + 1).toString()
        if (word.all { it.isDigit() }) return null
        return word
    }

    private fun isWordChar(c: Char): Boolean {
        return c.isLetterOrDigit() || c == '_'
    }

    private fun hasMultipleParts(word: String): Boolean {
        if (word.contains('_')) return true
        var hasLower = false
        for (c in word) {
            if (c.isLowerCase()) hasLower = true
            if (c.isUpperCase() && hasLower) return true
        }
        return false
    }
}
