package org.bundleproject.bundle.gui

import org.bundleproject.bundle.utils.getResourceImage
import java.awt.Dimension
import java.awt.Rectangle
import javax.swing.JFrame
import javax.swing.JProgressBar
import javax.swing.WindowConstants

/**
 * Loading bar for updating mods
 *
 * @since 0.0.4
 */
class LoadingGui(private val updateCount: Int) : JFrame("Updating Mods") {
    private val progressBar: JProgressBar

    init {
        setSize(400, 20)
        iconImage = getResourceImage("/bundle.png")
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        isResizable = false
        isUndecorated = true

        progressBar = JProgressBar(0, updateCount)
        progressBar.value = 0
        progressBar.bounds = Rectangle(0, 0, 400, 20)
        progressBar.preferredSize = Dimension(400, 20)
        add(progressBar)

        pack()
    }

    /**
     * Indicates to gui that another mod has finished downloading
     * and the progress bar should progress or complete.
     */
    fun finish() {
        if (progressBar.value + 1 == updateCount) {
            dispose()
        } else {
            progressBar.value += 1
        }
    }
}