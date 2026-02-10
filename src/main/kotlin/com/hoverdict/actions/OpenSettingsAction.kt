package com.hoverdict.actions

import com.hoverdict.ui.SettingsDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class OpenSettingsAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        SettingsDialog().show()
    }
}
