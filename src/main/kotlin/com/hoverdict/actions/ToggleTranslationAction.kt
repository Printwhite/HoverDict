package com.hoverdict.actions

import com.hoverdict.settings.HoverDictSettings
import com.hoverdict.ui.TranslationPopup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType

class ToggleTranslationAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val settings = HoverDictSettings.getInstance().state
        settings.enabled = !settings.enabled

        if (!settings.enabled) {
            TranslationPopup.getInstance().hideImmediately()
        }

        val status = if (settings.enabled) "已启用 / Enabled" else "已禁用 / Disabled"
        try {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("HoverDict.Notifications")
                .createNotification("HoverDict: $status", NotificationType.INFORMATION)
                .notify(e.project)
        } catch (_: Exception) {
        }
    }

    override fun update(e: AnActionEvent) {
        val enabled = HoverDictSettings.getInstance().state.enabled
        e.presentation.text = if (enabled) "Disable Translation" else "Enable Translation"
    }
}
