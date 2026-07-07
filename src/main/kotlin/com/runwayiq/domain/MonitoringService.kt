package com.runwayiq.domain

import com.runwayiq.ai.GroqClient
import com.runwayiq.data.model.AlertSeverity
import com.runwayiq.data.repository.FinancialRepository
import com.runwayiq.domain.usecase.FinancialSummaryUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MonitoringService(
    private val repo: FinancialRepository,
    private val summaryUseCase: FinancialSummaryUseCase,
    private val groqProvider: () -> GroqClient?,
    private val scope: CoroutineScope,
    private val onAlertsUpdated: suspend () -> Unit,
) {
    fun start() {
        scope.launch {
            while (isActive) {
                runChecks()
                delay(30_000)
            }
        }
    }

    suspend fun runChecks() {
        val scenario = repo.getActiveScenario() ?: return
        val summary = summaryUseCase.compute(scenario)
        val client = groqProvider()

        val triggers = buildList {
            if (summary.runwayMonths < 6 && summary.runwayMonths != Double.MAX_VALUE) {
                add(Triple("low_runway", AlertSeverity.RED, "Runway is ${"%.1f".format(summary.runwayMonths)} months, below the 6-month threshold."))
            }
            if (summary.mrrGrowthPct < 0) {
                add(Triple("mrr_decline", AlertSeverity.AMBER, "MRR growth is ${"%.1f".format(summary.mrrGrowthPct)}% month-over-month."))
            }
            val monthly = summary.monthlyData
            if (monthly.size >= 2) {
                val current = monthly.last()
                val prior = monthly[monthly.size - 2]
                val currentBurn = maxOf(0L, current.expensesCents - current.revenueCents)
                val priorBurn = maxOf(0L, prior.expensesCents - prior.revenueCents)
                if (priorBurn > 0) {
                    val increase = (currentBurn - priorBurn).toDouble() / priorBurn
                    if (increase > 0.15) {
                        add(
                            Triple(
                                "burn_spike",
                                AlertSeverity.AMBER,
                                "Burn increased ${"%.0f".format(increase * 100)}% from ${prior.month} (${priorBurn / 100}) to ${current.month} (${currentBurn / 100}).",
                            ),
                        )
                    }
                }
            }
        }

        var created = false
        for ((type, severity, context) in triggers) {
            if (repo.hasActiveAlert(scenario.id, type)) continue

            val message = if (client != null) {
                try {
                    client.complete(
                        systemPrompt = "You are a startup CFO alert system. Write exactly one concise sentence alerting the founder about a financial issue. Be specific with numbers. No greeting, no bullet points.",
                        messages = listOf(GroqClient.Message("user", context)),
                    ).trim().ifBlank { fallbackMessage(type, context) }
                } catch (_: Exception) {
                    fallbackMessage(type, context)
                }
            } else {
                fallbackMessage(type, context)
            }

            repo.insertAlert(scenario.id, type, severity, message)
            created = true
        }

        if (created) onAlertsUpdated()
    }

    private fun fallbackMessage(type: String, context: String): String = when (type) {
        "low_runway" -> "Warning: $context"
        "mrr_decline" -> "MRR is declining: $context"
        "burn_spike" -> "Burn rate spike detected: $context"
        else -> context
    }
}
