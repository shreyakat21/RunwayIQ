package com.runwayiq.ui

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

object FilePicker {
    fun pickCsvFile(): File? {
        val chooser = JFileChooser()
        chooser.fileFilter = FileNameExtensionFilter("CSV files", "csv")
        return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile
        } else {
            null
        }
    }
}
