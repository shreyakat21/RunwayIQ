package com.runwayiq.domain.usecase

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.runwayiq.data.db.RunwayDatabase
import com.runwayiq.data.repository.FinancialRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BudgetUseCaseTest {

    private fun inMemoryRepository(): FinancialRepository {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        RunwayDatabase.Schema.create(driver)
        return FinancialRepository(RunwayDatabase(driver))
    }

    @Test
    fun `computes actual spend against the monthly target for the current month`() = runTest {
        val repo = inMemoryRepository()
        val useCase = BudgetUseCase(repo)
        val month = BudgetUseCase.currentMonth()

        repo.setBudget("cloud", "expense", 1_500_00)
        repo.insertExpense(month, 900_00, "AWS", "cloud")
        repo.insertExpense(month, 300_00, "GCP", "cloud")

        val lines = useCase.computeBudgetLines()

        assertEquals(1, lines.size)
        val line = lines.first()
        assertEquals("cloud", line.category)
        assertEquals(1_500_00, line.targetCents)
        assertEquals(1_200_00, line.actualCents)
        assertFalse(line.isOverBudget)
    }

    @Test
    fun `ignores entries from other months`() = runTest {
        val repo = inMemoryRepository()
        val useCase = BudgetUseCase(repo)
        val month = BudgetUseCase.currentMonth()

        repo.setBudget("marketing", "expense", 1_000_00)
        repo.insertExpense(month, 500_00, "Ads", "marketing")
        repo.insertExpense("2020-01", 5_000_00, "Old ads", "marketing")

        val lines = useCase.computeBudgetLines()

        assertEquals(500_00, lines.first().actualCents)
    }

    @Test
    fun `returns empty list when no budgets are set`() = runTest {
        val repo = inMemoryRepository()
        val useCase = BudgetUseCase(repo)

        val lines = useCase.computeBudgetLines()

        assertTrue(lines.isEmpty())
    }
}
