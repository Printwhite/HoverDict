package com.hovertranslate.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.IconLoader

class HoverTranslateToolbarAction : AnAction(
    "Hover Translate",
    "Hover Translate",
    IconLoader.getIcon("/icons/hovertranslate16.svg", HoverTranslateToolbarAction::class.java)
) {
    override fun actionPerformed(e: AnActionEvent) {
        val am = ActionManager.getInstance()
        val group = DefaultActionGroup().apply {
            add(am.getAction("HoverTranslate.Settings"))
            addSeparator()
            add(am.getAction("HoverTranslate.Toggle"))
        }
        val popup = JBPopupFactory.getInstance()
            .createActionGroupPopup("Hover Translate", group, e.dataContext,
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true)
        val comp = e.inputEvent?.component
        if (comp != null) popup.showUnderneathOf(comp)
        else popup.showInBestPositionFor(e.dataContext)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
        val on = com.hovertranslate.settings.HoverTranslateSettings.getInstance().state.enabled
        e.presentation.text = "Hover Translate [${if (on) "ON" else "OFF"}]"
    }
}
