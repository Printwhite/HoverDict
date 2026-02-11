package com.hovertranslate.ui

import com.hovertranslate.settings.HoverTranslateSettings
import com.hovertranslate.service.DictionaryService
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*

class SettingsDialog : DialogWrapper(true) {

    private val enabledCb = JCheckBox()
    private val splitCb = JCheckBox()
    private val delaySpinner = JSpinner(SpinnerNumberModel(300, 50, 2000, 50))
    private val fontSpinner = JSpinner(SpinnerNumberModel(15, 10, 28, 1))
    private val opacitySpinner = JSpinner(SpinnerNumberModel(95, 50, 100, 5))
    private val langCombo = JComboBox(arrayOf("中文", "English"))
    private val toggleKeyField = JTextField(12)
    private val settingsKeyField = JTextField(12)

    private var mainPanel: JPanel? = null

    private fun isZh(): Boolean = langCombo.selectedIndex == 0

    private fun t(zh: String, en: String): String = if (isZh()) zh else en

    init {
        title = "Hover Translate"
        init()
        loadSettings()
        langCombo.addActionListener { rebuildUI() }
    }

    private fun loadSettings() {
        val s = HoverTranslateSettings.getInstance().state
        enabledCb.isSelected = s.enabled
        splitCb.isSelected = s.splitIdentifiers
        delaySpinner.value = s.hoverDelayMs
        fontSpinner.value = s.fontSize
        opacitySpinner.value = s.popupOpacity
        langCombo.selectedIndex = if (s.preferredLanguage == "zh") 0 else 1
        toggleKeyField.text = s.toggleShortcut
        settingsKeyField.text = s.settingsShortcut
    }

    private fun rebuildUI() {
        val panel = mainPanel ?: return
        panel.removeAll()
        buildContent(panel)
        panel.revalidate()
        panel.repaint()
    }

    override fun createCenterPanel(): JComponent {
        val p = JPanel(GridBagLayout())
        p.preferredSize = Dimension(440, 380)
        p.border = JBUI.Borders.empty(12, 20, 8, 20)
        mainPanel = p
        buildContent(p)
        return p
    }

    private fun buildContent(p: JPanel) {
        val g = GridBagConstraints()
        g.anchor = GridBagConstraints.WEST
        var r = 0
        val accent = Color(59, 130, 246)

        r = section(p, g, r, t("常规", "General"), accent)
        r = row(p, g, r, t("启用翻译", "Enable Translation"), enabledCb)
        r = row(p, g, r, t("拆分标识符", "Split Identifiers"), splitCb)

        r = section(p, g, r, t("显示", "Display"), accent)
        r = row(p, g, r, t("悬停延迟 (ms)", "Hover Delay (ms)"), delaySpinner)
        r = row(p, g, r, t("字体大小", "Font Size"), fontSpinner)
        r = row(p, g, r, t("弹窗透明度 %", "Popup Opacity %"), opacitySpinner)

        r = section(p, g, r, t("语言", "Language"), accent)
        r = row(p, g, r, t("首选语言", "Preferred Language"), langCombo)

        r = section(p, g, r, t("快捷键", "Shortcuts"), accent)
        r = row(p, g, r, t("开关翻译", "Toggle Translation"), toggleKeyField)
        r = row(p, g, r, t("打开设置", "Open Settings"), settingsKeyField)

        // Footer
        g.gridx = 0; g.gridy = r; g.gridwidth = 2
        g.insets = JBUI.insets(14, 0, 0, 0)
        g.fill = GridBagConstraints.HORIZONTAL
        val footer = JPanel(BorderLayout())
        footer.isOpaque = false
        val dictSize = DictionaryService.getDictionarySize()
        val info = JLabel(t("词典：${dictSize} 条", "Dictionary: ${dictSize} entries"))
        info.font = Font("SansSerif", Font.PLAIN, 11)
        info.foreground = JBColor.GRAY
        footer.add(info, BorderLayout.WEST)
        p.add(footer, g)
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
        val s = HoverTranslateSettings.getInstance().state
        s.enabled = enabledCb.isSelected
        s.splitIdentifiers = splitCb.isSelected
        s.hoverDelayMs = delaySpinner.value as Int
        s.fontSize = fontSpinner.value as Int
        s.popupOpacity = opacitySpinner.value as Int
        s.preferredLanguage = if (langCombo.selectedIndex == 0) "zh" else "en"
        s.toggleShortcut = toggleKeyField.text
        s.settingsShortcut = settingsKeyField.text
        super.doOKAction()
    }
}
