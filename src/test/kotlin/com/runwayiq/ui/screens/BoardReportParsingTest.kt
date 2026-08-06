package com.runwayiq.ui.screens

import androidx.compose.ui.text.font.FontWeight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoardReportParsingTest {

    @Test
    fun `parses sections with well-formed newlines`() {
        val text = """
            ## Key Metrics
            Cash is healthy.
            ## Highlights
            Growing fast.
            ## Risks
            Burn is high.
            ## The Ask
            We need funding.
        """.trimIndent()

        val sections = parseBoardReportSections(text)

        assertEquals(4, sections.size)
        assertEquals("Key Metrics", sections[0].title)
        assertEquals("Cash is healthy.", sections[0].body)
        assertEquals("The Ask", sections[3].title)
        assertEquals("We need funding.", sections[3].body)
    }

    @Test
    fun `parses sections even when headers are glued to the previous sentence with no newline`() {
        // Regression test: streamed model responses sometimes land without a real
        // newline before the next "## Header", e.g. "...## Key MetricsOur cash
        // balance is $900,000...## Highlights..." — the header text itself is used
        // as the delimiter so this still splits correctly.
        val text = "## Key MetricsOur cash balance is \$900,000." +
            "## HighlightsRevenue is up 12%." +
            "## RisksBurn exceeds revenue." +
            "## The AskWe are raising a seed round."

        val sections = parseBoardReportSections(text)

        assertEquals(4, sections.size)
        assertEquals("Key Metrics", sections[0].title)
        assertEquals("Our cash balance is \$900,000.", sections[0].body)
        assertEquals("Highlights", sections[1].title)
        assertEquals("Revenue is up 12%.", sections[1].body)
        assertEquals("Risks", sections[2].title)
        assertEquals("Burn exceeds revenue.", sections[2].body)
        assertEquals("The Ask", sections[3].title)
        assertEquals("We are raising a seed round.", sections[3].body)
    }

    @Test
    fun `falls back to a single section when no known headers are present`() {
        val text = "Just some freeform text with no headers."

        val sections = parseBoardReportSections(text)

        assertEquals(1, sections.size)
        assertEquals("Board Report", sections[0].title)
        assertEquals(text, sections[0].body)
    }

    @Test
    fun `renders bold markdown spans without literal asterisks`() {
        val annotated = renderInlineMarkdown("Runway is **12.5 months** at current burn.")

        assertEquals("Runway is 12.5 months at current burn.", annotated.text)
        assertTrue(annotated.spanStyles.any { it.item.fontWeight == FontWeight.SemiBold })
    }

    @Test
    fun `renders plain text unchanged when there is no bold markdown`() {
        val annotated = renderInlineMarkdown("No markdown here.")

        assertEquals("No markdown here.", annotated.text)
        assertTrue(annotated.spanStyles.isEmpty())
    }
}
