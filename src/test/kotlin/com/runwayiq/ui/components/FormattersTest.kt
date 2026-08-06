package com.runwayiq.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals

class FormattersTest {

    @Test
    fun `formatDollars formats small amounts`() {
        assertEquals("$500", formatDollars(50_000))
    }

    @Test
    fun `formatDollars formats thousands`() {
        assertEquals("$5k", formatDollars(500_000))
    }

    @Test
    fun `formatDollars formats millions`() {
        assertEquals("$1.5M", formatDollars(150_000_000))
    }

    @Test
    fun `signedDollars prefixes positive values`() {
        assertEquals("+$100", signedDollars(10_000))
    }

    @Test
    fun `signedDollars prefixes negative values without a double sign`() {
        assertEquals("-$100", signedDollars(-10_000))
    }

    @Test
    fun `signedDollars treats zero as positive`() {
        assertEquals("+$0", signedDollars(0))
    }

    @Test
    fun `formatRunway shows infinite runway as a symbol`() {
        assertEquals("∞", formatRunway(Double.MAX_VALUE))
    }

    @Test
    fun `formatRunway shows months under two years`() {
        assertEquals("6.0mo", formatRunway(6.0))
    }

    @Test
    fun `formatRunway shows years at or above 24 months`() {
        assertEquals("2y", formatRunway(24.0))
    }
}
