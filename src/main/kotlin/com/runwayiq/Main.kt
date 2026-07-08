package com.runwayiq

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.runwayiq.data.db.DatabaseFactory
import com.runwayiq.data.repository.FinancialRepository
import com.runwayiq.domain.usecase.FinancialSummaryUseCase
import com.runwayiq.ui.*
import com.runwayiq.ui.components.SideNav
import com.runwayiq.ui.screens.*
import com.runwayiq.ui.theme.RunwayIQTheme

fun main() = application {
    val db = remember { DatabaseFactory.create() }
    val repo = remember { FinancialRepository(db) }
    val useCase = remember { FinancialSummaryUseCase(repo) }
    val viewModel = remember { AppViewModel(repo, useCase) }
    val state by viewModel.state.collectAsState()
    val snackbarState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.loadApiKey()
        viewModel.loadThemePreference()
        focusRequester.requestFocus()
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Window(
        onCloseRequest = { viewModel.onDestroy(); exitApplication() },
        title = "RunwayIQ",
        state = rememberWindowState(width = 1100.dp, height = 720.dp),
    ) {
        RunwayIQTheme(darkTheme = state.isDarkTheme) {
            Box(
                Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester)
                    .focusable()
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                        if (state.showOnboarding) return@onPreviewKeyEvent false
                        val mod = event.isMetaPressed || event.isCtrlPressed
                        if (!mod) return@onPreviewKeyEvent false
                        when (event.key) {
                            Key.One -> { viewModel.navigate(NavScreen.DASHBOARD); true }
                            Key.Two -> { viewModel.navigate(NavScreen.REVENUE); true }
                            Key.Three -> { viewModel.navigate(NavScreen.EXPENSES); true }
                            Key.Four -> { viewModel.navigate(NavScreen.BUDGET); true }
                            Key.Five -> { viewModel.navigate(NavScreen.WHATIF); true }
                            Key.Six -> { viewModel.navigate(NavScreen.SCENARIOS); true }
                            Key.Seven -> { viewModel.navigate(NavScreen.SETTINGS); true }
                            Key.N -> { viewModel.triggerAddEntry(); true }
                            Key.Enter -> { viewModel.triggerChatSend(); true }
                            else -> false
                        }
                    },
            ) {
                if (state.showOnboarding) {
                    OnboardingScreen(
                        onSetupScenario = viewModel::setupInitialScenario,
                        onFinish = viewModel::finishOnboarding,
                        onSkip = viewModel::skipOnboarding,
                    )
                } else {
                    Row(Modifier.fillMaxSize()) {
                        SideNav(
                            current = state.screen,
                            onNavigate = viewModel::navigate,
                            isDarkTheme = state.isDarkTheme,
                            onToggleTheme = viewModel::toggleTheme,
                        )

                        Surface(Modifier.weight(1f).fillMaxHeight()) {
                            when (state.screen) {
                                NavScreen.DASHBOARD -> DashboardScreen(
                                    state = state,
                                    onSendMessage = viewModel::sendChatMessage,
                                    onDismissAlert = viewModel::dismissAlert,
                                    onGenerateBoardReport = viewModel::generateBoardReport,
                                    onDismissBoardReport = viewModel::dismissBoardReport,
                                    onGenerateInsight = viewModel::generateInsight,
                                    onNavigate = viewModel::navigate,
                                )
                                NavScreen.REVENUE -> RevenueScreen(
                                    state = state,
                                    onAdd = viewModel::addRevenue,
                                    onDelete = viewModel::deleteRevenue,
                                    onImportCsv = viewModel::importRevenueCsv,
                                    onError = viewModel::showError,
                                )
                                NavScreen.EXPENSES -> ExpenseScreen(
                                    state = state,
                                    onAdd = viewModel::addExpense,
                                    onDelete = viewModel::deleteExpense,
                                    onImportCsv = viewModel::importExpenseCsv,
                                    onError = viewModel::showError,
                                )
                                NavScreen.BUDGET -> BudgetScreen(
                                    state = state,
                                    onSetBudget = viewModel::setBudget,
                                    onDeleteBudget = viewModel::deleteBudget,
                                )
                                NavScreen.WHATIF -> WhatIfScreen(state = state)
                                NavScreen.SCENARIOS -> ScenariosScreen(
                                    state = state,
                                    onAdd = viewModel::addScenario,
                                    onSwitch = viewModel::switchScenario,
                                    onCompareWithAi = viewModel::compareScenariosWithAi,
                                    onDismissComparison = viewModel::dismissScenarioComparison,
                                )
                                NavScreen.SETTINGS -> SettingsScreen(
                                    state = state,
                                    onSaveApiKey = viewModel::setApiKey,
                                    onClearApiKey = viewModel::clearApiKey,
                                )
                            }
                        }
                    }
                }
                SnackbarHost(snackbarState, Modifier.align(Alignment.BottomCenter).padding(16.dp))
            }
        }
    }
}
