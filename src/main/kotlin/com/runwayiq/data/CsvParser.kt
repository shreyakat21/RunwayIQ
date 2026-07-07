package com.runwayiq.data

import com.runwayiq.data.model.CsvImportRow
import java.io.File

object CsvParser {
    fun parse(file: File): List<CsvImportRow> {
        val lines = file.readLines().filter { it.isNotBlank() }
        val dataLines = if (lines.firstOrNull()?.lowercase()?.startsWith("month") == true) {
            lines.drop(1)
        } else {
            lines
        }

        return dataLines.mapNotNull { line ->
            val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
            if (parts.size < 4) return@mapNotNull null
            val amount = parts[1].replace("$", "").toDoubleOrNull() ?: return@mapNotNull null
            CsvImportRow(
                month = parts[0],
                amountCents = (amount * 100).toLong(),
                label = parts[2],
                category = parts[3],
            )
        }
    }
}
