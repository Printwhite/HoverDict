package com.hovertranslate.service

import com.hovertranslate.settings.HoverTranslateSettings
import com.intellij.openapi.diagnostic.Logger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

object DictionaryService {

    private val LOG = Logger.getInstance(DictionaryService::class.java)
    private val enToZh = ConcurrentHashMap<String, String>(200000, 0.75f, 1)
    private val zhToEn = ConcurrentHashMap<String, String>(50000, 0.75f, 1)

    @Volatile
    private var loaded = false

    fun ensureLoaded() {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val t = System.currentTimeMillis()
            loadDictionary()
            loaded = true
            LOG.info("Hover Translate: dictionary loaded in ${System.currentTimeMillis() - t}ms, entries=${enToZh.size}")
        }
    }

    private fun loadDictionary() {
        val stream = DictionaryService::class.java.getResourceAsStream("/dictionary/en_zh.dict")
        if (stream == null) { LOG.warn("Hover Translate: en_zh.dict not found!"); return }
        BufferedReader(InputStreamReader(stream, Charsets.UTF_8), 1024 * 64).use { reader ->
            reader.forEachLine { line ->
                val tab = line.indexOf('\t')
                if (tab > 0 && tab < line.length - 1) {
                    val en = line.substring(0, tab).trim().lowercase()
                    val zh = line.substring(tab + 1).trim()
                    if (en.isNotEmpty() && zh.isNotEmpty()) {
                        enToZh[en] = zh
                        val first = zh.split(';', '；', '，').first().trim()
                        if (first.isNotEmpty() && first.length <= 10) zhToEn.putIfAbsent(first, en)
                    }
                }
            }
        }
    }

    fun translate(word: String): String? {
        ensureLoaded()
        val lower = word.trim().lowercase()
        val s = HoverTranslateSettings.getInstance().state
        if (s.preferredLanguage == "zh") {
            enToZh[lower]?.let { return it }
            zhToEn[word.trim()]?.let { return it }
        } else {
            zhToEn[word.trim()]?.let { return it }
            enToZh[lower]?.let { return it }
        }
        return null
    }

    fun translateIdentifier(identifier: String): String? {
        ensureLoaded()
        val parts = splitIdentifier(identifier)
        if (parts.size <= 1) return translate(identifier)
        val t = parts.mapNotNull { p -> translate(p)?.let { "$p → $it" } }
        return if (t.isNotEmpty()) t.joinToString("\n") else null
    }

    fun splitIdentifier(identifier: String): List<String> {
        if (identifier.contains('_')) return identifier.split('_').filter { it.isNotEmpty() }
        val parts = mutableListOf<String>()
        val cur = StringBuilder()
        for (c in identifier) {
            if (c.isUpperCase() && cur.isNotEmpty()) { parts.add(cur.toString()); cur.clear() }
            cur.append(c.lowercaseChar())
        }
        if (cur.isNotEmpty()) parts.add(cur.toString())
        return parts
    }

    fun getDictionarySize(): Int { ensureLoaded(); return enToZh.size }
}
