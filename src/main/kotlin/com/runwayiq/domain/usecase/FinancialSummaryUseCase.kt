package com.runwayiq.domain.usecase

import com.runwayiq.data.model.*
import com.runwayiq.data.repository.FinancialRepository

class FinancialSummaryUseCase(private val repo: FinancialRepository) {

    suspend fun compute(scenario: Scenario): FinancialSummary {
        val revenues = repo.getAllRevenue()
        val expenses = repo.getAllExpenses()

        val allMonths = (revenues.map { it.month } + expenses.map { it.month })
            .distinct()
            .sortedDescending()

        val monthlyData = allMonths.map { month ->
            val rev = revenues.filter { it.month == month }.sumOf { it.amountCents }
            val exp = expenses.filter { it.month == month }.sumOf { it.amountCents }
            MonthlyData(month, rev, exp, rev - exp)
        }

        val recentMonths = monthlyData.take(3)
        val avgBurn = if (recentMonths.isEmpty()) 0L else
            recentMonths.map { maxOf(0L, it.expensesCents - it.revenueCents) }.average().toLong()

        val latestRevMonth = monthlyData.firstOrNull()
        val prevRevMonth = monthlyData.getOrNull(1)
        val mrrCents = revenues
            .filter { it.month == latestRevMonth?.month && it.category == "mrr" }
            .sumOf { it.amountCents }

        val prevMrrCents = revenues
            .filter { it.month == prevRevMonth?.month && it.category == "mrr" }
            .sumOf { it.amountCents }

        val mrrGrowth = if (prevMrrCents > 0)
            ((mrrCents - prevMrrCents).toDouble() / prevMrrCents) * 100.0
        else 0.0

        val runwayMonths = if (avgBurn > 0)
            scenario.cashBalanceCents.toDouble() / avgBurn.toDouble()
        else Double.MAX_VALUE

        val totalRevenue = revenues.sumOf { it.amountCents }
        val totalExpenses = expenses.sumOf { it.amountCents }

        return FinancialSummary(
            totalRevenueCents = totalRevenue,
            totalExpensesCents = totalExpenses,
            burnRateCents = avgBurn,
            cashBalanceCents = scenario.cashBalanceCents,
            runwayMonths = runwayMonths,
            mrrCents = mrrCents,
            mrrGrowthPct = mrrGrowth,
            monthlyData = monthlyData.reversed()
        )
    }

    fun buildCfoContext(summary: FinancialSummary, scenarioName: String): String {
        val recent = summary.monthlyData.takeLast(6)
        val monthsTable = recent.joinToString("\n") { m ->
            "  ${m.month}: revenue \$${m.revenueCents / 100}, expenses \$${m.expensesCents / 100}, net \$${m.netCents / 100}"
        }
        val runway = if (summary.runwayMonths == Double.MAX_VALUE) "∞ (cash flow positive)"
        else "%.1f months".format(summary.runwayMonths)

        return """
You are an experienced CFO advisor for an early-stage startup. You have access to the following real financial data for the scenario "${scenarioName}":

CURRENT SNAPSHOT:
- Cash balance: ${"$"}${summary.cashBalanceCents / 100}
- Monthly burn rate (3-mo avg): ${"$"}${summary.burnRateCents / 100}/mo
- Runway: $runway
- MRR: ${"$"}${summary.mrrCents / 100}
- MRR growth (last month): ${"%.1f".format(summary.mrrGrowthPct)}%

MONTHLY BREAKDOWN (last 6 months):
$monthsTable

Answer questions about this data with specific numbers, trade-offs, and actionable recommendations. Be direct and concise — like a real CFO in a 1:1 meeting. When you don't have enough data to answer precisely, say so and explain what information would help.
        """.trimIndent()
    }

    fun buildScenarioComparisonContext(scenarios: List<Pair<Scenario, FinancialSummary>>): String {
        val blocks = scenarios.joinToString("\n\n") { (scenario, summary) ->
            val runway = if (summary.runwayMonths == Double.MAX_VALUE) "∞ (cash flow positive)"
            else "%.1f months".format(summary.runwayMonths)
            """
Scenario: ${scenario.name}
- Cash balance: ${"$"}${summary.cashBalanceCents / 100}
- Monthly burn (3-mo avg): ${"$"}${summary.burnRateCents / 100}/mo
- Runway: $runway
- MRR: ${"$"}${summary.mrrCents / 100}
- MRR growth: ${"%.1f".format(summary.mrrGrowthPct)}%
- Total revenue: ${"$"}${summary.totalRevenueCents / 100}
- Total expenses: ${"$"}${summary.totalExpensesCents / 100}
            """.trimIndent()
        }

        return """
You are an experienced CFO comparing financial scenarios for an early-stage startup.

$blocks

Compare these scenarios: which has better runway, what the key tradeoffs are between them, and which you recommend pursuing and why. Be specific with numbers. Use clear paragraphs, not bullet points.
        """.trimIndent()
    }

    fun buildBoardReportContext(summary: FinancialSummary, scenarioName: String): String {
        val monthsTable = summary.monthlyData.joinToString("\n") { m ->
            "  ${m.month}: revenue \$${m.revenueCents / 100}, expenses \$${m.expensesCents / 100}, net \$${m.netCents / 100}"
        }
        val runway = if (summary.runwayMonths == Double.MAX_VALUE) "∞ (cash flow positive)"
        else "%.1f months".format(summary.runwayMonths)

        return """
You are an experienced CFO writing a concise investor board update for the scenario "${scenarioName}".

FINANCIAL DATA:
- Cash balance: ${"$"}${summary.cashBalanceCents / 100}
- Monthly burn rate (3-mo avg): ${"$"}${summary.burnRateCents / 100}/mo
- Runway: $runway
- MRR: ${"$"}${summary.mrrCents / 100}
- MRR growth (last month): ${"%.1f".format(summary.mrrGrowthPct)}%
- Total revenue to date: ${"$"}${summary.totalRevenueCents / 100}
- Total expenses to date: ${"$"}${summary.totalExpensesCents / 100}

MONTHLY BREAKDOWN:
$monthsTable

Write a structured investor update using exactly these markdown section headers:

## Key Metrics
## Highlights
## Risks
## The Ask

Under each header, write 2–4 sentences with specific numbers. Be direct and professional — this goes to a board deck. Do not include any other sections or preamble.
        """.trimIndent()
    }
}
