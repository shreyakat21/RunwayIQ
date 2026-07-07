package com.runwayiq.data

import com.runwayiq.data.repository.FinancialRepository

object SeedData {

    private val months = listOf("2024-07", "2024-08", "2024-09", "2024-10", "2024-11", "2024-12")

    // MRR ramp: $8k → $14k over 6 months
    private val mrrByMonth = mapOf(
        "2024-07" to 8_000_00L,
        "2024-08" to 9_000_00L,
        "2024-09" to 10_000_00L,
        "2024-10" to 11_000_00L,
        "2024-11" to 12_500_00L,
        "2024-12" to 14_000_00L,
    )

    // Marketing spend ramps through H2, with a year-end push in December
    private val marketingByMonth = mapOf(
        "2024-07" to 2_000_00L,
        "2024-08" to 2_500_00L,
        "2024-09" to 3_000_00L,
        "2024-10" to 4_000_00L,
        "2024-11" to 3_500_00L,
        "2024-12" to 5_000_00L,
    )

    suspend fun seedIfEmpty(repo: FinancialRepository) {
        if (!repo.isFinancialDataEmpty()) return

        months.forEach { month ->
            repo.insertRevenue(month, mrrByMonth.getValue(month), "SaaS subscriptions (MRR)", "mrr")
        }

        repo.insertRevenue(
            month = "2024-09",
            amountCents = 22_000_00L,
            label = "Enterprise onboarding — Meridian Health",
            category = "one_time",
        )

        months.forEach { month ->
            repo.insertExpense(month, 18_000_00L, "Engineering & GTM payroll", "salaries")
            repo.insertExpense(month, 1_200_00L, "AWS + Vercel infrastructure", "cloud")
            repo.insertExpense(month, 800_00L, "Figma, GitHub, Slack, Linear", "software")
            repo.insertExpense(
                month,
                marketingByMonth.getValue(month),
                "Google Ads + LinkedIn campaigns",
                "marketing",
            )
        }

        repo.getActiveScenario()?.let { scenario ->
            repo.updateScenarioCashBalance(scenario.id, 900_000_00L)
        }
    }
}
