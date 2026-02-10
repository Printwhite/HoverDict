package com.hoverdict.ui

import com.intellij.openapi.editor.Editor
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.RoundRectangle2D
import javax.swing.*

class TranslationPopup private constructor() {

    private var popup: JWindow? = null
    private var contentLabel: JLabel? = null

    companion object {
        @Volatile
        private var instance: TranslationPopup? = null

        fun getInstance(): TranslationPopup {
            return instance ?: synchronized(this) {
                instance ?: TranslationPopup().also { instance = it }
            }
        }
    }

    fun show(editor: Editor, text: String, point: Point) {
        hideImmediately()
        SwingUtilities.invokeLater {
            createAndShow(editor, text, point)
        }
    }

    private fun createAndShow(editor: Editor, text: String, point: Point) {
        val editorComponent = editor.contentComponent
        if (!editorComponent.isShowing) return

        val window = SwingUtilities.getWindowAncestor(editorComponent) ?: return
        val popup = JWindow(window)
        this.popup = popup

        val isDark = JBColor.isBright().not()
        val bgColor = if (isDark) Color(50, 50, 54, 248) else Color(255, 255, 255, 248)
        val fgColor = if (isDark) Color(230, 230, 235) else Color(35, 35, 40)
        val borderColor = if (isDark) Color(85, 85, 95, 220) else Color(195, 195, 205, 220)
        val accentColor = Color(108, 92, 231)
        val subTextColor = if (isDark) Color(160, 160, 170) else Color(120, 120, 130)

        val arc = 18f

        val panel = object : JPanel(BorderLayout()) {
            override fun paintComponent(g: Graphics) {
                val g2 = g.create() as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)

                g2.color = Color(0, 0, 0, 30)
                g2.fill(RoundRectangle2D.Float(2f, 3f, width.toFloat() - 2f, height.toFloat() - 2f, arc, arc))

                g2.color = bgColor
                g2.fill(RoundRectangle2D.Float(0f, 0f, width.toFloat(), height.toFloat(), arc, arc))

                g2.color = borderColor
                g2.stroke = BasicStroke(1.2f)
                g2.draw(RoundRectangle2D.Float(0.6f, 0.6f, width - 1.2f, height - 1.2f, arc, arc))

                g2.color = accentColor
                g2.fillRoundRect(0, 0, 4, height, 4, 4)

                g2.dispose()
            }
        }
        panel.isOpaque = false
        panel.border = JBUI.Borders.empty(12, 18, 14, 18)

        val topBar = JPanel(BorderLayout())
        topBar.isOpaque = false
        topBar.border = JBUI.Borders.emptyBottom(8)

        val badgePanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        badgePanel.isOpaque = false
        val badge = JLabel("HoverDict")
        badge.font = Font("SansSerif", Font.BOLD, 11)
        badge.foreground = accentColor
        badgePanel.add(badge)
        topBar.add(badgePanel, BorderLayout.WEST)

        val closeBtn = JLabel("  ✕  ")
        closeBtn.font = Font("SansSerif", Font.PLAIN, 13)
        closeBtn.foreground = subTextColor
        closeBtn.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        closeBtn.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                hideImmediately()
            }
            override fun mouseEntered(e: MouseEvent) {
                closeBtn.foreground = fgColor
            }
            override fun mouseExited(e: MouseEvent) {
                closeBtn.foreground = subTextColor
            }
        })
        topBar.add(closeBtn, BorderLayout.EAST)

        val fontSize = com.hoverdict.settings.HoverDictSettings.getInstance().state.fontSize
        val scaledSize = (fontSize * 1.15).toInt().coerceAtLeast(14)

        val htmlText = text.replace("\n", "<br style='margin:4px 0;'>")
        val label = JLabel("<html><body style='width:auto; line-height:1.5;'>$htmlText</body></html>")
        label.font = Font("Microsoft YaHei", Font.PLAIN, scaledSize)
        label.foreground = fgColor
        label.border = JBUI.Borders.empty(2, 4, 2, 4)
        this.contentLabel = label

        val contentWrapper = JPanel(BorderLayout())
        contentWrapper.isOpaque = false
        contentWrapper.add(label, BorderLayout.CENTER)

        panel.add(topBar, BorderLayout.NORTH)
        panel.add(contentWrapper, BorderLayout.CENTER)

        popup.contentPane = panel
        popup.background = Color(0, 0, 0, 0)

        val minWidth = 260
        val minHeight = 70
        popup.pack()
        val currentSize = popup.size
        popup.setSize(
            currentSize.width.coerceAtLeast(minWidth),
            currentSize.height.coerceAtLeast(minHeight)
        )

        val screenPoint = Point(point)
        SwingUtilities.convertPointToScreen(screenPoint, editorComponent)

        val popupSize = popup.size
        val screenBounds = getScreenBoundsAt(screenPoint)

        var x = screenPoint.x
        var y = screenPoint.y + 24

        if (x + popupSize.width > screenBounds.x + screenBounds.width) {
            x = screenBounds.x + screenBounds.width - popupSize.width - 8
        }
        if (x < screenBounds.x) {
            x = screenBounds.x + 8
        }

        if (y + popupSize.height > screenBounds.y + screenBounds.height) {
            y = screenPoint.y - popupSize.height - 8
        }
        if (y < screenBounds.y) {
            y = screenBounds.y + 8
        }

        popup.setLocation(x, y)
        popup.isVisible = true
    }

    private fun getScreenBoundsAt(point: Point): Rectangle {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        for (gd in ge.screenDevices) {
            val bounds = gd.defaultConfiguration.bounds
            if (bounds.contains(point)) {
                val insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.defaultConfiguration)
                return Rectangle(
                    bounds.x + insets.left,
                    bounds.y + insets.top,
                    bounds.width - insets.left - insets.right,
                    bounds.height - insets.top - insets.bottom
                )
            }
        }
        val defaultBounds = ge.defaultScreenDevice.defaultConfiguration.bounds
        val defaultInsets = Toolkit.getDefaultToolkit().getScreenInsets(ge.defaultScreenDevice.defaultConfiguration)
        return Rectangle(
            defaultBounds.x + defaultInsets.left,
            defaultBounds.y + defaultInsets.top,
            defaultBounds.width - defaultInsets.left - defaultInsets.right,
            defaultBounds.height - defaultInsets.top - defaultInsets.bottom
        )
    }

    fun hideImmediately() {
        SwingUtilities.invokeLater {
            popup?.isVisible = false
            popup?.dispose()
            popup = null
            contentLabel = null
        }
    }

    fun isVisible(): Boolean = popup?.isVisible == true
}
