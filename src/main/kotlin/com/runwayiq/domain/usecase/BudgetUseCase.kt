package com.runwayiq.domain.usecase

import com.runwayiq.data.model.BudgetLine
import com.runwayiq.data.repository.FinancialRepository
import java.time.YearMonth

class BudgetUseCase(private val repo: FinancialRepository) {

    suspend fun computeBudgetLines(month: String = currentMonth()): List<BudgetLine> {
        val budgets = repo.getAllBudgets()
        if (budgets.isEmpty()) return emptyList()

        val revenues = repo.getAllRevenue().filter { it.month == month }
        val expenses = repo.getAllExpenses().filter { it.month == month }

        return budgets.map { budget ->
            val actual = if (budget.entryType == "revenue") {
                revenues.filter { it.category == budget.category }.sumOf { it.amountCents }
            } else {
                expenses.filter { it.category == budget.category }.sumOf { it.amountCents }
            }
            BudgetLine(budget.category, budget.entryType, budget.monthlyTargetCents, actual)
        }.sortedWith(compareBy({ it.entryType }, { it.category }))
    }

    companion object {
        fun currentMonth(): String {
            val now = YearMonth.now()
            return "%04d-%02d".format(now.year, now.monthValue)
        }
    }
}
