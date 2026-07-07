package com.runwayiq.ui

import com.runwayiq.ai.GroqClient
import com.runwayiq.data.model.*
import com.runwayiq.data.repository.FinancialRepository
import com.runwayiq.domain.MonitoringService
import com.runwayiq.domain.usecase.FinancialSummaryUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class NavScreen { DASHBOARD, REVENUE, EXPENSES, SCENARIOS, SETTINGS }

data class AppState(
    val screen: NavScreen = NavScreen.DASHBOARD,
    val scenarios: List<Scenario> = emptyList(),
    val activeScenario: Scenario? = null,
    val summary: FinancialSummary? = null,
    val revenues: List<RevenueEntry> = emptyList(),
    val expenses: List<ExpenseEntry> = emptyList(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val alerts: List<Alert> = emptyList(),
    val streamingResponse: String = "",
    val isStreaming: Boolean = false,
    val isLoading: Boolean = true,
    val isComparingScenarios: Boolean = false,
    val showScenarioComparison: Boolean = false,
    val scenarioComparisonResult: String? = null,
    val showBoardReport: Boolean = false,
    val boardReportContent: String? = null,
    val isGeneratingBoardReport: Boolean = false,
    val showOnboarding: Boolean = false,
    val addEntryTrigger: Int = 0,
    val chatSendTrigger: Int = 0,
    val apiKey: String = "",
    val errorMessage: String? = null,
)

class AppViewModel(
    private val repo: FinancialRepository,
    private val summaryUseCase: FinancialSummaryUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private var groqClient: GroqClient? = null

    private val monitoringService = MonitoringService(
        repo = repo,
        summaryUseCase = summaryUseCase,
        groqProvider = { groqClient },
        scope = scope,
        onAlertsUpdated = { refreshAlerts() },
    )

    init {
        scope.launch { loadAll() }
        monitoringService.start()
    }

    fun setApiKey(key: String) {
        val trimmed = key.trim()
        groqClient?.close()
        groqClient = if (trimmed.isNotBlank()) GroqClient(trimmed) else null
        _state.update { it.copy(apiKey = trimmed) }
        persistApiKey(trimmed)
    }

    private fun persistApiKey(key: String) {
        prefs().put("api_key", key)
    }

    fun loadApiKey() {
        val key = prefs().get("api_key", "")
        if (key.isNotBlank()) setApiKey(key)
    }

    private fun prefs() = java.util.prefs.Preferences.userRoot().node("runwayiq")

    private fun launchSafely(block: suspend () -> Unit) {
        scope.launch {
            try {
                block()
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Something went wrong: ${e.message ?: "unknown error"}") }
            }
        }
    }

    private fun markOnboardingComplete() {
        prefs().putBoolean("onboarding_complete", true)
    }

    fun navigate(screen: NavScreen) = _state.update { it.copy(screen = screen) }

    fun triggerAddEntry() {
        val screen = _state.value.screen
        if (screen == NavScreen.REVENUE || screen == NavScreen.EXPENSES) {
            _state.update { it.copy(addEntryTrigger = it.addEntryTrigger + 1) }
        }
    }

    fun triggerChatSend() {
        if (_state.value.screen == NavScreen.DASHBOARD) {
            _state.update { it.copy(chatSendTrigger = it.chatSendTrigger + 1) }
        }
    }

    fun setupInitialScenario(name: String, cash: Double) {
        launchSafely {
            repo.insertScenario(name, (cash * 100).toLong())
            repo.setActiveScenario(repo.getAllScenarios().first().id)
            loadAll()
        }
    }

    fun finishOnboarding() {
        markOnboardingComplete()
        _state.update {
            it.copy(
                showOnboarding = false,
                screen = NavScreen.REVENUE,
                addEntryTrigger = it.addEntryTrigger + 1,
            )
        }
    }

    fun skipOnboarding() {
        markOnboardingComplete()
        _state.update {
            it.copy(
                showOnboarding = false,
                screen = NavScreen.DASHBOARD,
            )
        }
    }

    suspend fun loadAll() {
        _state.update { it.copy(isLoading = true) }
        val scenarios = repo.getAllScenarios()
        val active = repo.getActiveScenario() ?: scenarios.firstOrNull()
        val revenues = repo.getAllRevenue()
        val expenses = repo.getAllExpenses()

        if (scenarios.isEmpty() && revenues.isEmpty() && expenses.isEmpty()) {
            _state.update {
                it.copy(
                    isLoading = false,
                    showOnboarding = true,
                    scenarios = emptyList(),
                    activeScenario = null,
                    summary = null,
                    revenues = emptyList(),
                    expenses = emptyList(),
                    chatMessages = emptyList(),
                    alerts = emptyList(),
                )
            }
            return
        }

        markOnboardingComplete()

        val resolvedActive = active ?: scenarios.firstOrNull()
        val summary = if (resolvedActive != null && (revenues.isNotEmpty() || expenses.isNotEmpty())) {
            summaryUseCase.compute(resolvedActive)
        } else null
        val messages = resolvedActive?.let { repo.getMessages(it.id) } ?: emptyList()
        val alerts = repo.getActiveAlerts()

        _state.update {
            it.copy(
                scenarios = scenarios,
                activeScenario = resolvedActive,
                summary = summary,
                revenues = revenues,
                expenses = expenses,
                chatMessages = messages,
                alerts = alerts,
                isLoading = false,
            )
        }
    }

    private suspend fun refreshAlerts() {
        val alerts = repo.getActiveAlerts()
        _state.update { it.copy(alerts = alerts) }
    }

    fun dismissAlert(id: Long) {
        launchSafely {
            repo.dismissAlert(id)
            refreshAlerts()
        }
    }

    fun addRevenue(month: String, amountDollars: Double, label: String, category: String) {
        launchSafely {
            repo.insertRevenue(month, (amountDollars * 100).toLong(), label, category)
            loadAll()
        }
    }

    fun deleteRevenue(id: Long) {
        launchSafely { repo.deleteRevenue(id); loadAll() }
    }

    fun importRevenueCsv(rows: List<CsvImportRow>) {
        launchSafely {
            repo.insertRevenueBatch(rows)
            loadAll()
        }
    }

    fun addExpense(month: String, amountDollars: Double, label: String, category: String) {
        launchSafely {
            repo.insertExpense(month, (amountDollars * 100).toLong(), label, category)
            loadAll()
        }
    }

    fun deleteExpense(id: Long) {
        launchSafely { repo.deleteExpense(id); loadAll() }
    }

    fun importExpenseCsv(rows: List<CsvImportRow>) {
        launchSafely {
            repo.insertExpenseBatch(rows)
            loadAll()
        }
    }

    fun addScenario(name: String, cashBalance: Double) {
        launchSafely {
            repo.insertScenario(name, (cashBalance * 100).toLong())
            loadAll()
        }
    }

    fun switchScenario(id: Long) {
        launchSafely {
            repo.setActiveScenario(id)
            loadAll()
        }
    }

    fun compareScenariosWithAi() {
        val client = groqClient
        if (client == null) {
            _state.update { it.copy(errorMessage = "Add your Groq API key in Settings first.") }
            return
        }
        if (_state.value.scenarios.size < 2) return

        scope.launch {
            _state.update {
                it.copy(isComparingScenarios = true, showScenarioComparison = true, scenarioComparisonResult = null)
            }
            try {
                val scenarios = repo.getAllScenarios()
                val summaries = scenarios.map { it to summaryUseCase.compute(it) }
                val systemPrompt = summaryUseCase.buildScenarioComparisonContext(summaries)
                val result = client.complete(
                    systemPrompt = systemPrompt,
                    messages = listOf(GroqClient.Message("user", "Compare these scenarios and recommend which to pursue.")),
                )
                _state.update {
                    it.copy(isComparingScenarios = false, scenarioComparisonResult = result)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isComparingScenarios = false,
                        showScenarioComparison = false,
                        errorMessage = "API error: ${e.message}",
                    )
                }
            }
        }
    }

    fun dismissScenarioComparison() {
        _state.update {
            it.copy(showScenarioComparison = false, scenarioComparisonResult = null, isComparingScenarios = false)
        }
    }

    fun generateBoardReport() {
        val client = groqClient
        if (client == null) {
            _state.update { it.copy(errorMessage = "Add your Groq API key in Settings first.") }
            return
        }
        val scenario = _state.value.activeScenario
        val summary = _state.value.summary
        if (scenario == null || summary == null) {
            _state.update { it.copy(errorMessage = "Add revenue and expense data before generating a report.") }
            return
        }

        scope.launch {
            _state.update {
                it.copy(showBoardReport = true, boardReportContent = null, isGeneratingBoardReport = true)
            }
            try {
                val systemPrompt = summaryUseCase.buildBoardReportContext(summary, scenario.name)
                val result = client.complete(
                    systemPrompt = systemPrompt,
                    messages = listOf(GroqClient.Message("user", "Write the board report now.")),
                )
                _state.update {
                    it.copy(isGeneratingBoardReport = false, boardReportContent = result)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isGeneratingBoardReport = false,
                        showBoardReport = false,
                        errorMessage = "API error: ${e.message}",
                    )
                }
            }
        }
    }

    fun dismissBoardReport() {
        _state.update {
            it.copy(showBoardReport = false, boardReportContent = null, isGeneratingBoardReport = false)
        }
    }

    fun sendChatMessage(userMessage: String) {
        val client = groqClient
        if (client == null) {
            _state.update { it.copy(errorMessage = "Add your Groq API key in Settings first.") }
            return
        }
        val scenario = _state.value.activeScenario ?: return
        val summary = _state.value.summary ?: return

        scope.launch {
            try {
                repo.insertMessage("user", userMessage, scenario.id)
                val history = repo.getMessages(scenario.id)
                _state.update { it.copy(chatMessages = history, streamingResponse = "", isStreaming = true) }

                val systemPrompt = summaryUseCase.buildCfoContext(summary, scenario.name)
                val apiMessages = history
                    .filter { it.content.isNotBlank() }
                    .map { GroqClient.Message(it.role, it.content) }

                val sb = StringBuilder()
                client.streamResponse(systemPrompt, apiMessages).collect { chunk ->
                    sb.append(chunk)
                    _state.update { it.copy(streamingResponse = sb.toString()) }
                }
                val fullResponse = sb.toString()
                if (fullResponse.isBlank()) {
                    _state.update {
                        it.copy(isStreaming = false, errorMessage = "Groq returned an empty response.")
                    }
                    return@launch
                }
                repo.insertMessage("assistant", fullResponse, scenario.id)
                val updatedHistory = repo.getMessages(scenario.id)
                _state.update { it.copy(chatMessages = updatedHistory, streamingResponse = "", isStreaming = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isStreaming = false, errorMessage = "API error: ${e.message}") }
            }
        }
    }

    fun dismissError() = _state.update { it.copy(errorMessage = null) }

    fun showError(message: String) = _state.update { it.copy(errorMessage = message) }

    fun clearChat() {
        launchSafely {
            val id = _state.value.activeScenario?.id ?: return@launchSafely
            repo.clearChat(id)
            loadAll()
        }
    }

    fun onDestroy() {
        groqClient?.close()
        scope.cancel()
    }
}
