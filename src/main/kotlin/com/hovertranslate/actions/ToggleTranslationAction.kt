package com.hovertranslate.actions

import com.hovertranslate.settings.HoverTranslateSettings
import com.hovertranslate.ui.TranslationPopup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType

class ToggleTranslationAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val settings = HoverTranslateSettings.getInstance().state
        settings.enabled = !settings.enabled

        if (!settings.enabled) {
            TranslationPopup.getInstance().hideImmediately()
        }

        val isZh = settings.preferredLanguage == "zh"
        val status = if (settings.enabled) {
            if (isZh) "已启用" else "Enabled"
        } else {
            if (isZh) "已禁用" else "Disabled"
        }

        try {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("HoverTranslate.Notifications")
                .createNotification("Hover Translate: $status", NotificationType.INFORMATION)
                .notify(e.project)
        } catch (_: Exception) {
        }
    }

    override fun update(e: AnActionEvent) {
        val s = HoverTranslateSettings.getInstance().state
        val isZh = s.preferredLanguage == "zh"
        e.presentation.text = if (s.enabled) {
            if (isZh) "关闭翻译" else "Disable Translation"
        } else {
            if (isZh) "开启翻译" else "Enable Translation"
        }
    }
}
