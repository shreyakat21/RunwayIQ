package com.runwayiq.data.model

data class RevenueEntry(
    val id: Long,
    val month: String,
    val amountCents: Long,
    val label: String,
    val category: String
)

data class ExpenseEntry(
    val id: Long,
    val month: String,
    val amountCents: Long,
    val label: String,
    val category: String
)

data class Scenario(
    val id: Long,
    val name: String,
    val cashBalanceCents: Long,
    val isActive: Boolean
)

data class ChatMessage(
    val id: Long,
    val role: String,
    val content: String,
    val scenarioId: Long?
)

data class FinancialSummary(
    val totalRevenueCents: Long,
    val totalExpensesCents: Long,
    val burnRateCents: Long,
    val cashBalanceCents: Long,
    val runwayMonths: Double,
    val mrrCents: Long,
    val mrrGrowthPct: Double,
    val monthlyData: List<MonthlyData>
)

data class MonthlyData(
    val month: String,
    val revenueCents: Long,
    val expensesCents: Long,
    val netCents: Long
)

enum class EntryCategory(val displayName: String) {
    MRR("MRR"),
    ONE_TIME("One-time"),
    GRANT("Grant"),
    SALARIES("Salaries"),
    CLOUD("Cloud / Infra"),
    MARKETING("Marketing"),
    OFFICE("Office"),
    SOFTWARE("Software"),
    OPEX("Other OpEx")
}

data class Alert(
    val id: Long,
    val scenarioId: Long,
    val alertType: String,
    val severity: AlertSeverity,
    val message: String,
)

enum class AlertSeverity { RED, AMBER }

data class CsvImportRow(
    val month: String,
    val amountCents: Long,
    val label: String,
    val category: String,
)

data class Budget(
    val id: Long,
    val category: String,
    val entryType: String,
    val monthlyTargetCents: Long,
)

data class BudgetLine(
    val category: String,
    val entryType: String,
    val targetCents: Long,
    val actualCents: Long,
) {
    val varianceCents: Long get() = actualCents - targetCents
    val isOverBudget: Boolean get() = if (entryType == "expense") actualCents > targetCents else actualCents < targetCents
}
