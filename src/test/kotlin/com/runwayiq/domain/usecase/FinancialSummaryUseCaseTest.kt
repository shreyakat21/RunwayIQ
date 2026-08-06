package com.runwayiq.domain.usecase

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.runwayiq.data.db.RunwayDatabase
import com.runwayiq.data.repository.FinancialRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FinancialSummaryUseCaseTest {

    private fun inMemoryRepository(): FinancialRepository {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        RunwayDatabase.Schema.create(driver)
        return FinancialRepository(RunwayDatabase(driver))
    }

    @Test
    fun `computes burn rate and runway from the trailing three months`() = runTest {
        val repo = inMemoryRepository()
        val useCase = FinancialSummaryUseCase(repo)

        repo.insertScenario("Base Case", 30_000_00)
        val scenario = repo.getAllScenarios().first()

        // 3 months of $2,000 revenue and $5,000 expenses => $3,000/mo net burn
        listOf("2026-05", "2026-06", "2026-07").forEach { month ->
            repo.insertRevenue(month, 2_000_00, "MRR", "mrr")
            repo.insertExpense(month, 5_000_00, "Payroll", "salaries")
        }

        val summary = useCase.compute(scenario)

        assertEquals(3_000_00, summary.burnRateCents)
        assertEquals(10.0, summary.runwayMonths, 0.001) // $30,000 / $3,000 per month
        assertEquals(2_000_00, summary.mrrCents)
    }

    @Test
    fun `runway is infinite when there is no net burn`() = runTest {
        val repo = inMemoryRepository()
        val useCase = FinancialSummaryUseCase(repo)

        repo.insertScenario("Profitable", 10_000_00)
        val scenario = repo.getAllScenarios().first()

        repo.insertRevenue("2026-07", 10_000_00, "MRR", "mrr")
        repo.insertExpense("2026-07", 2_000_00, "Payroll", "salaries")

        val summary = useCase.compute(scenario)

        assertEquals(Double.MAX_VALUE, summary.runwayMonths)
    }

    @Test
    fun `mrr growth percentage compares the two most recent months`() = runTest {
        val repo = inMemoryRepository()
        val useCase = FinancialSummaryUseCase(repo)

        repo.insertScenario("Growing", 10_000_00)
        val scenario = repo.getAllScenarios().first()

        repo.insertRevenue("2026-06", 10_000_00, "MRR", "mrr")
        repo.insertRevenue("2026-07", 11_000_00, "MRR", "mrr")

        val summary = useCase.compute(scenario)

        assertEquals(11_000_00, summary.mrrCents)
        assertEquals(10.0, summary.mrrGrowthPct, 0.001)
    }
}
