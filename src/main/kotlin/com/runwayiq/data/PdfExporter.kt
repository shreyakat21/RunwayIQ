package com.runwayiq.data

import com.runwayiq.ui.screens.BoardReportSection
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import java.io.File

/**
 * Renders a board report as a simple text PDF using only the standard 14
 * PDF fonts, so no font files need to be bundled with the app.
 */
object PdfExporter {
    private const val MARGIN = 56f
    private const val TITLE_FONT_SIZE = 18f
    private const val HEADER_FONT_SIZE = 13f
    private const val BODY_FONT_SIZE = 11f
    private const val LINE_HEIGHT = 15f

    fun exportBoardReport(scenarioName: String, sections: List<BoardReportSection>, file: File) {
        PDDocument().use { document ->
            var page = PDPage(PDRectangle.LETTER)
            document.addPage(page)
            var contentStream = PDPageContentStream(document, page)
            var y = page.mediaBox.height - MARGIN
            val maxWidth = page.mediaBox.width - 2 * MARGIN

            fun newPage() {
                contentStream.close()
                page = PDPage(PDRectangle.LETTER)
                document.addPage(page)
                contentStream = PDPageContentStream(document, page)
                y = page.mediaBox.height - MARGIN
            }

            fun ensureSpace(needed: Float) {
                if (y - needed < MARGIN) newPage()
            }

            fun writeLine(text: String, font: PDFont, size: Float) {
                ensureSpace(LINE_HEIGHT)
                contentStream.beginText()
                contentStream.setFont(font, size)
                contentStream.newLineAtOffset(MARGIN, y)
                contentStream.showText(text)
                contentStream.endText()
                y -= LINE_HEIGHT
            }

            fun wrapText(text: String, font: PDFont, size: Float): List<String> {
                val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
                val lines = mutableListOf<String>()
                var current = StringBuilder()
                for (word in words) {
                    val candidate = if (current.isEmpty()) word else "$current $word"
                    val width = font.getStringWidth(candidate) / 1000f * size
                    if (width > maxWidth && current.isNotEmpty()) {
                        lines.add(current.toString())
                        current = StringBuilder(word)
                    } else {
                        current = StringBuilder(candidate)
                    }
                }
                if (current.isNotEmpty()) lines.add(current.toString())
                return lines
            }

            writeLine(sanitizeForPdf("Board Report - $scenarioName"), PDType1Font.HELVETICA_BOLD, TITLE_FONT_SIZE)
            y -= 10f

            sections.forEach { section ->
                ensureSpace(LINE_HEIGHT * 2)
                y -= 8f
                writeLine(sanitizeForPdf(section.title), PDType1Font.HELVETICA_BOLD, HEADER_FONT_SIZE)
                wrapText(sanitizeForPdf(section.body), PDType1Font.HELVETICA, BODY_FONT_SIZE).forEach { line ->
                    writeLine(line, PDType1Font.HELVETICA, BODY_FONT_SIZE)
                }
            }

            contentStream.close()
            document.save(file)
        }
    }

    private fun sanitizeForPdf(text: String): String {
        val boldStripped = Regex("\\*\\*(.+?)\\*\\*").replace(text) { it.groupValues[1] }
        val sb = StringBuilder(boldStripped.length)
        for (ch in boldStripped) {
            when (ch.code) {
                0x2018, 0x2019 -> sb.append('\'')
                0x201C, 0x201D -> sb.append('"')
                0x2013, 0x2014 -> sb.append('-')
                0x2022 -> sb.append('-')
                0x2026 -> sb.append("...")
                0x00A0 -> sb.append(' ')
                '#'.code -> {}
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }
}
