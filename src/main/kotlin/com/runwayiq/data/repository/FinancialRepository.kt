package com.runwayiq.data.repository

import com.runwayiq.data.db.RunwayDatabase
import com.runwayiq.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FinancialRepository(private val db: RunwayDatabase) {

    suspend fun getAllRevenue(): List<RevenueEntry> = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.getAllRevenue().executeAsList().map {
            RevenueEntry(it.id, it.month, it.amount_cents, it.label, it.category)
        }
    }

    suspend fun insertRevenue(month: String, amountCents: Long, label: String, category: String) =
        withContext(Dispatchers.IO) {
            db.runwayDatabaseQueries.insertRevenue(month, amountCents, label, category)
        }

    suspend fun deleteRevenue(id: Long) = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.deleteRevenue(id)
    }

    suspend fun getAllExpenses(): List<ExpenseEntry> = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.getAllExpenses().executeAsList().map {
            ExpenseEntry(it.id, it.month, it.amount_cents, it.label, it.category)
        }
    }

    suspend fun insertExpense(month: String, amountCents: Long, label: String, category: String) =
        withContext(Dispatchers.IO) {
            db.runwayDatabaseQueries.insertExpense(month, amountCents, label, category)
        }

    suspend fun deleteExpense(id: Long) = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.deleteExpense(id)
    }

    suspend fun getAllScenarios(): List<Scenario> = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.getAllScenarios().executeAsList().map {
            Scenario(it.id, it.name, it.cash_balance_cents, it.is_active == 1L)
        }
    }

    suspend fun getActiveScenario(): Scenario? = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.getActiveScenario().executeAsOneOrNull()?.let {
            Scenario(it.id, it.name, it.cash_balance_cents, true)
        }
    }

    suspend fun insertScenario(name: String, cashBalanceCents: Long) = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.insertScenario(name, cashBalanceCents, 0)
    }

    suspend fun setActiveScenario(id: Long) = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.setActiveScenario(id)
    }

    suspend fun updateScenarioCashBalance(id: Long, cashBalanceCents: Long) = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.updateScenarioCashBalance(cashBalanceCents, id)
    }

    suspend fun isFinancialDataEmpty(): Boolean = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.getAllRevenue().executeAsList().isEmpty() &&
            db.runwayDatabaseQueries.getAllExpenses().executeAsList().isEmpty()
    }

    suspend fun getMessages(scenarioId: Long): List<ChatMessage> = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.getMessagesForScenario(scenarioId).executeAsList().map {
            ChatMessage(it.id, it.role, it.content, it.scenario_id)
        }
    }

    suspend fun insertMessage(role: String, content: String, scenarioId: Long) =
        withContext(Dispatchers.IO) {
            db.runwayDatabaseQueries.insertMessage(role, content, scenarioId)
        }

    suspend fun clearChat(scenarioId: Long) = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.clearChatForScenario(scenarioId)
    }

    suspend fun getActiveAlerts(): List<Alert> = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.getActiveAlerts().executeAsList().map {
            Alert(
                id = it.id,
                scenarioId = it.scenario_id,
                alertType = it.alert_type,
                severity = if (it.severity == "red") AlertSeverity.RED else AlertSeverity.AMBER,
                message = it.message,
            )
        }
    }

    suspend fun hasActiveAlert(scenarioId: Long, alertType: String): Boolean = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.getActiveAlertByType(scenarioId, alertType).executeAsOneOrNull() != null
    }

    suspend fun insertAlert(scenarioId: Long, alertType: String, severity: AlertSeverity, message: String) =
        withContext(Dispatchers.IO) {
            db.runwayDatabaseQueries.insertAlert(
                scenarioId,
                alertType,
                if (severity == AlertSeverity.RED) "red" else "amber",
                message,
            )
        }

    suspend fun dismissAlert(id: Long) = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.dismissAlert(id)
    }

    suspend fun insertRevenueBatch(rows: List<CsvImportRow>) = withContext(Dispatchers.IO) {
        db.transaction {
            rows.forEach { row ->
                db.runwayDatabaseQueries.insertRevenue(row.month, row.amountCents, row.label, row.category)
            }
        }
    }

    suspend fun insertExpenseBatch(rows: List<CsvImportRow>) = withContext(Dispatchers.IO) {
        db.transaction {
            rows.forEach { row ->
                db.runwayDatabaseQueries.insertExpense(row.month, row.amountCents, row.label, row.category)
            }
        }
    }

    suspend fun getAllBudgets(): List<Budget> = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.getAllBudgets().executeAsList().map {
            Budget(it.id, it.category, it.entry_type, it.monthly_target_cents)
        }
    }

    suspend fun setBudget(category: String, entryType: String, monthlyTargetCents: Long) = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.upsertBudget(category, entryType, monthlyTargetCents)
    }

    suspend fun deleteBudget(id: Long) = withContext(Dispatchers.IO) {
        db.runwayDatabaseQueries.deleteBudget(id)
    }
}
