package com.hoverdict.ui

import com.hoverdict.settings.HoverDictSettings
import com.hoverdict.service.DictionaryService
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*

class SettingsDialog : DialogWrapper(true) {

    private val enabledCb = JCheckBox()
    private val splitCb = JCheckBox()
    private val showOnStartupCb = JCheckBox()
    private val delaySpinner = JSpinner(SpinnerNumberModel(300, 50, 2000, 50))
    private val fontSpinner = JSpinner(SpinnerNumberModel(15, 10, 28, 1))
    private val opacitySpinner = JSpinner(SpinnerNumberModel(95, 50, 100, 5))
    private val langCombo = JComboBox(arrayOf("中文", "English"))
    private val toggleKeyField = JTextField(12)
    private val settingsKeyField = JTextField(12)

    init {
        title = "HoverDict Settings"
        init()
        loadSettings()
    }

    private fun loadSettings() {
        val s = HoverDictSettings.getInstance().state
        enabledCb.isSelected = s.enabled
        splitCb.isSelected = s.splitIdentifiers
        showOnStartupCb.isSelected = s.showSponsorOnStartup
        delaySpinner.value = s.hoverDelayMs
        fontSpinner.value = s.fontSize
        opacitySpinner.value = s.popupOpacity
        langCombo.selectedIndex = if (s.preferredLanguage == "zh") 0 else 1
        toggleKeyField.text = s.toggleShortcut
        settingsKeyField.text = s.settingsShortcut
    }

    override fun createCenterPanel(): JComponent {
        val p = JPanel(GridBagLayout())
        p.preferredSize = Dimension(440, 380)
        p.border = JBUI.Borders.empty(12, 20, 8, 20)

        val g = GridBagConstraints()
        g.anchor = GridBagConstraints.WEST
        var r = 0
        val accent = Color(59, 130, 246)

        r = section(p, g, r, "General", accent)
        r = row(p, g, r, "Enable Translation", enabledCb)
        r = row(p, g, r, "Split Identifiers", splitCb)
        r = row(p, g, r, "Show Intro on Startup", showOnStartupCb)

        r = section(p, g, r, "Display", accent)
        r = row(p, g, r, "Hover Delay (ms)", delaySpinner)
        r = row(p, g, r, "Font Size", fontSpinner)
        r = row(p, g, r, "Popup Opacity %", opacitySpinner)

        r = section(p, g, r, "Language", accent)
        r = row(p, g, r, "Preferred Language", langCombo)

        r = section(p, g, r, "Shortcuts", accent)
        r = row(p, g, r, "Toggle Translation", toggleKeyField)
        r = row(p, g, r, "Open Settings", settingsKeyField)

        // Footer
        g.gridx = 0; g.gridy = r; g.gridwidth = 2
        g.insets = JBUI.insets(14, 0, 0, 0)
        g.fill = GridBagConstraints.HORIZONTAL
        val footer = JPanel(BorderLayout())
        footer.isOpaque = false
        val info = JLabel("Dictionary: ${DictionaryService.getDictionarySize()} entries")
        info.font = Font("SansSerif", Font.PLAIN, 11)
        info.foreground = JBColor.GRAY
        footer.add(info, BorderLayout.WEST)
        val btn = JButton("About")
        btn.font = Font("SansSerif", Font.PLAIN, 12)
        btn.addActionListener { SponsorDialog().show() }
        footer.add(btn, BorderLayout.EAST)
        p.add(footer, g)

        return p
    }

    private fun section(p: JPanel, g: GridBagConstraints, r: Int, text: String, color: Color): Int {
        g.gridx = 0; g.gridy = r; g.gridwidth = 2
        g.fill = GridBagConstraints.HORIZONTAL
        g.insets = if (r == 0) JBUI.insets(0, 0, 4, 0) else JBUI.insets(12, 0, 4, 0)
        val l = JLabel(text)
        l.font = Font("SansSerif", Font.BOLD, 13)
        l.foreground = color
        p.add(l, g)
        g.gridwidth = 1; g.fill = GridBagConstraints.NONE
        return r + 1
    }

    private fun row(p: JPanel, g: GridBagConstraints, r: Int, label: String, comp: JComponent): Int {
        g.insets = JBUI.insets(3, 0)
        g.gridx = 0; g.gridy = r; g.weightx = 1.0; g.fill = GridBagConstraints.HORIZONTAL
        p.add(JLabel(label).apply { font = Font("SansSerif", Font.PLAIN, 13) }, g)
        g.gridx = 1; g.weightx = 0.0; g.fill = GridBagConstraints.NONE
        p.add(comp, g)
        return r + 1
    }

    override fun doOKAction() {
        val s = HoverDictSettings.getInstance().state
        s.enabled = enabledCb.isSelected
        s.splitIdentifiers = splitCb.isSelected
        s.showSponsorOnStartup = showOnStartupCb.isSelected
        s.hoverDelayMs = delaySpinner.value as Int
        s.fontSize = fontSpinner.value as Int
        s.popupOpacity = opacitySpinner.value as Int
        s.preferredLanguage = if (langCombo.selectedIndex == 0) "zh" else "en"
        s.toggleShortcut = toggleKeyField.text
        s.settingsShortcut = settingsKeyField.text
        super.doOKAction()
    }
}
