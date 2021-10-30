package org.bundleproject.bundle.gui

import org.bundleproject.bundle.Bundle
import org.bundleproject.bundle.entities.Mod
import org.bundleproject.bundle.entities.RemoteMod
import org.bundleproject.bundle.utils.getResourceImage
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.net.URL
import java.util.concurrent.locks.Condition
import javax.swing.*

/**
 * Allows the user to pick which mods they
 * would like to update and verify that
 * it is updating correctly.
 *
 * @since 0.0.2
 */
class UpdateOverviewGui(private val bundle: Bundle, mods: MutableList<Pair<Mod, RemoteMod>>, condition: Condition? = null) : JFrame("Bundle") {
    
    init {
        iconImage = getResourceImage("/bundle.png")
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        val gbl = GridBagLayout()
        val gbc = GridBagConstraints()
        layout = gbl

        gbc.fill = GridBagConstraints.HORIZONTAL

        val rows = mutableListOf<Array<Any>>()
        for ((local, remote) in mods) {
            rows.add(arrayOf(
                JCheckBox("", true).also { it.addActionListener { remote.enabled = false } },
                remote.name,
                local.version.toString(),
                remote.version.toString(),
                URL(remote.downloadUrl).host,
            ))
        }
        val table = JTable(rows.toTypedArray(), arrayOf("", "Mod", "Current", "Remote", "Host"))
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.gridwidth = 2
        gbc.gridheight = 4
        add(table, gbc)

        val skipButton = JButton("Skip")
        skipButton.addActionListener {
            mods.clear()
            dispose()
            condition?.signal()
        }
        gbc.gridx = 0
        gbc.gridy = 1
        gbc.gridwidth = 2
        gbc.gridheight = 1
        add(skipButton, gbc)

        val downloadButton = JButton("Update")
        downloadButton.addActionListener {
            bundle.updateMods(mods.filter { it.second.enabled })
            dispose()
            condition?.signal()
        }
        gbc.gridx = 1
        gbc.gridy = 1
        gbc.gridwidth = 2
        gbc.gridheight = 1
        downloadButton.requestFocus()
        add(downloadButton, gbc)

        pack()
    }
    
}