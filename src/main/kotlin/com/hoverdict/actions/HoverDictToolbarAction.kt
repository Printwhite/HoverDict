package com.hoverdict.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.IconLoader

class HoverDictToolbarAction : AnAction(
    "HoverDict",
    "HoverDict",
    IconLoader.getIcon("/icons/hoverdict16.svg", HoverDictToolbarAction::class.java)
) {
    override fun actionPerformed(e: AnActionEvent) {
        val am = ActionManager.getInstance()
        val group = DefaultActionGroup().apply {
            add(am.getAction("HoverDict.Settings"))
            addSeparator()
            add(am.getAction("HoverDict.Toggle"))
        }
        val popup = JBPopupFactory.getInstance()
            .createActionGroupPopup("HoverDict", group, e.dataContext,
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true)
        val comp = e.inputEvent?.component
        if (comp != null) popup.showUnderneathOf(comp)
        else popup.showInBestPositionFor(e.dataContext)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
        val on = com.hoverdict.settings.HoverDictSettings.getInstance().state.enabled
        e.presentation.text = "HoverDict [${if (on) "ON" else "OFF"}]"
    }
}
