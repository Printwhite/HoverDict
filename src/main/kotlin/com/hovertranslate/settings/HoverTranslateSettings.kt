package com.hovertranslate.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "HoverTranslateSettings", storages = [Storage("HoverTranslateSettings.xml")])
class HoverTranslateSettings : PersistentStateComponent<HoverTranslateSettings.State> {

    data class State(
        var enabled: Boolean = true,
        var hoverDelayMs: Int = 300,
        var preferredLanguage: String = "zh",
        var toggleShortcut: String = "ctrl shift T",
        var settingsShortcut: String = "ctrl shift D",
        var popupOpacity: Int = 95,
        var splitIdentifiers: Boolean = true,
        var fontSize: Int = 15
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(): HoverTranslateSettings {
            return ApplicationManager.getApplication().getService(HoverTranslateSettings::class.java)
        }
    }
}
