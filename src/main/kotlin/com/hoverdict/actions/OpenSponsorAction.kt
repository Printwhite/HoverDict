package com.hoverdict.actions

import com.hoverdict.ui.SponsorDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class OpenSponsorAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        SponsorDialog().show()
    }
}
