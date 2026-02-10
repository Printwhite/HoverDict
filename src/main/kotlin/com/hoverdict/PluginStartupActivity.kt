package com.hoverdict

import com.hoverdict.service.DictionaryService
import com.hoverdict.service.HoverMouseListener
import com.hoverdict.settings.HoverDictSettings
import com.hoverdict.ui.SponsorDialog
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
        LOG.info("HoverDict: startup for project: ${project.name}")

        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                DictionaryService.ensureLoaded()
                LOG.info("HoverDict: dictionary loaded, size=${DictionaryService.getDictionarySize()}")
            } catch (e: Exception) {
                LOG.error("HoverDict: failed to load dictionary", e)
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
                LOG.error("HoverDict: failed to register listeners", e)
            }
        }

        val settings = HoverDictSettings.getInstance().state
        if (settings.showSponsorOnStartup && !SponsorSessionState.isDismissedThisSession) {
            invokeLater {
                SponsorDialog().show()
            }
        }
    }
}

object SponsorSessionState {
    @Volatile
    var isDismissedThisSession: Boolean = false
}
