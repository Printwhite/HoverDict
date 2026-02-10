package com.hoverdict.ui

import com.hoverdict.SponsorSessionState
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.*

class SponsorDialog : DialogWrapper(true) {

    init {
        title = "HoverDict"
        setOKButtonText("OK")
        setCancelButtonText("")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = JBUI.Borders.empty(20, 28, 16, 28)
        panel.preferredSize = Dimension(480, 560)

        val isDark = !JBColor.isBright()
        val accent = Color(59, 130, 246)
        val sub = if (isDark) Color(150, 150, 160) else Color(120, 120, 130)
        val text = if (isDark) Color(210, 210, 220) else Color(50, 50, 55)

        // Title
        val title = JLabel("HoverDict")
        title.font = Font("SansSerif", Font.BOLD, 22)
        title.foreground = accent
        title.alignmentX = Component.CENTER_ALIGNMENT
        panel.add(title)

        panel.add(Box.createVerticalStrut(12))

        // Intro
        val intro = JLabel(
            "<html><div style='text-align:center;width:380px;font-size:12px;color:${hex(text)};line-height:1.6;'>" +
            "Offline hover translation for JetBrains IDEs.<br>" +
            "Powered by ECDICT · 700K+ entries · No network required." +
            "</div></html>"
        )
        intro.alignmentX = Component.CENTER_ALIGNMENT
        panel.add(intro)

        panel.add(Box.createVerticalStrut(18))
        panel.add(divider())
        panel.add(Box.createVerticalStrut(18))

        // QR codes - 原始分辨率，不压缩
        val qrRow = JPanel(FlowLayout(FlowLayout.CENTER, 28, 0))
        qrRow.isOpaque = false
        qrRow.alignmentX = Component.CENTER_ALIGNMENT

        qrRow.add(qrBlock(loadImg("/images/sponsor_qr.jpg"), "WeChat Pay", sub))
        qrRow.add(qrBlock(loadImg("/images/wechat_contact_qr.png"), "Contact", sub))

        panel.add(qrRow)

        panel.add(Box.createVerticalStrut(18))
        panel.add(divider())
        panel.add(Box.createVerticalStrut(12))

        // Email
        val email = JLabel("helloxiaojii@gmail.com")
        email.font = Font("SansSerif", Font.PLAIN, 12)
        email.foreground = sub
        email.alignmentX = Component.CENTER_ALIGNMENT
        panel.add(email)

        panel.add(Box.createVerticalStrut(14))

        // Don't show checkbox
        val cb = JCheckBox("Don't show again this session")
        cb.font = Font("SansSerif", Font.PLAIN, 11)
        cb.isOpaque = false
        cb.isSelected = SponsorSessionState.isDismissedThisSession
        cb.alignmentX = Component.CENTER_ALIGNMENT
        cb.addActionListener { SponsorSessionState.isDismissedThisSession = cb.isSelected }
        panel.add(cb)

        return panel
    }

    private fun qrBlock(img: BufferedImage?, label: String, labelColor: Color): JComponent {
        val box = JPanel()
        box.layout = BoxLayout(box, BoxLayout.Y_AXIS)
        box.isOpaque = false

        val w = 180
        if (img != null) {
            val h = (img.height.toDouble() / img.width * w).toInt()
            val icon = JLabel(ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH)))
            icon.alignmentX = Component.CENTER_ALIGNMENT
            box.add(icon)
        }

        box.add(Box.createVerticalStrut(4))
        val lbl = JLabel(label)
        lbl.font = Font("SansSerif", Font.PLAIN, 11)
        lbl.foreground = labelColor
        lbl.alignmentX = Component.CENTER_ALIGNMENT
        box.add(lbl)

        return box
    }

    private fun divider(): JComponent {
        val s = JSeparator()
        s.maximumSize = Dimension(Int.MAX_VALUE, 1)
        s.foreground = JBColor(Color(225, 225, 230), Color(60, 60, 65))
        s.alignmentX = Component.LEFT_ALIGNMENT
        return s
    }

    private fun loadImg(path: String): BufferedImage? = try {
        javaClass.getResourceAsStream(path)?.let { ImageIO.read(it) }
    } catch (_: Exception) { null }

    private fun hex(c: Color) = "#%02x%02x%02x".format(c.red, c.green, c.blue)

    override fun createActions(): Array<Action> = arrayOf(okAction)
}
