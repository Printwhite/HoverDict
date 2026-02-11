package com.hovertranslate

import com.hovertranslate.service.DictionaryService
import com.hovertranslate.service.HoverMouseListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class PluginStartupActivity : ProjectActivity {

    private val LOG = Logger.getInstance(PluginStartupActivity::class.java)

    override suspend fun execute(project: Project) {
        LOG.info("Hover Translate: startup for project: ${project.name}")

        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                DictionaryService.ensureLoaded()
                LOG.info("Hover Translate: dictionary loaded, size=${DictionaryService.getDictionarySize()}")
            } catch (e: Exception) {
                LOG.error("Hover Translate: failed to load dictionary", e)
            }
        }

        invokeLater {
            try {
                val listener = HoverMouseListener()
                val factory = EditorFactory.getInstance()
                for (editor in factory.allEditors) {
                    editor.addEditorMouseMotionListener(listener)
                    editor.addEditorMouseListener(listener)
                }
                factory.addEditorFactoryListener(object : EditorFactoryListener {
                    override fun editorCreated(event: EditorFactoryEvent) {
                        event.editor.addEditorMouseMotionListener(listener)
                        event.editor.addEditorMouseListener(listener)
                    }
                }, project)
            } catch (e: Exception) {
                LOG.error("Hover Translate: failed to register listeners", e)
            }
        }
    }
}
