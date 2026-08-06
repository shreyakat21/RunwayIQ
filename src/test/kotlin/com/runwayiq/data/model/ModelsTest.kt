package com.runwayiq.data.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModelsTest {

    @Test
    fun `expense budget line is over budget when actual exceeds target`() {
        val line = BudgetLine(category = "cloud", entryType = "expense", targetCents = 1_000_00, actualCents = 1_200_00)

        assertEquals(200_00, line.varianceCents)
        assertTrue(line.isOverBudget)
    }

    @Test
    fun `expense budget line is under budget when actual is below target`() {
        val line = BudgetLine(category = "cloud", entryType = "expense", targetCents = 1_000_00, actualCents = 800_00)

        assertEquals(-200_00, line.varianceCents)
        assertFalse(line.isOverBudget)
    }

    @Test
    fun `revenue budget line is over budget when actual falls short of target`() {
        // For revenue, "over budget" means missing the goal, not exceeding it.
        val line = BudgetLine(category = "mrr", entryType = "revenue", targetCents = 15_000_00, actualCents = 12_000_00)

        assertTrue(line.isOverBudget)
    }

    @Test
    fun `revenue budget line is not over budget when actual meets or exceeds target`() {
        val line = BudgetLine(category = "mrr", entryType = "revenue", targetCents = 15_000_00, actualCents = 16_000_00)

        assertFalse(line.isOverBudget)
    }

    @Test
    fun `holding gain-loss reflects current price versus cost basis`() {
        val holding = Holding(id = 1, ticker = "AAPL", shares = 10.0, costBasisCents = 1_000_00, purchaseDate = "2025-01-01")
        val quote = StockQuote(ticker = "AAPL", priceCents = 12_000, changePct = 1.5) // $120/share
        val row = HoldingWithQuote(holding, quote)

        assertEquals(120_000, row.currentValueCents) // 10 shares * $120
        assertEquals(20_000, row.gainLossCents)
        assertEquals(20.0, row.gainLossPct, 0.001)
    }

    @Test
    fun `holding without a quote falls back to cost basis with zero gain-loss`() {
        val holding = Holding(id = 1, ticker = "AAPL", shares = 10.0, costBasisCents = 1_000_00, purchaseDate = "2025-01-01")
        val row = HoldingWithQuote(holding, quote = null)

        assertEquals(holding.costBasisCents, row.currentValueCents)
        assertEquals(0, row.gainLossCents)
        assertEquals(0.0, row.gainLossPct, 0.001)
    }
}
