package com.runwayiq.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import com.runwayiq.data.PdfExporter
import com.runwayiq.data.model.Alert
import com.runwayiq.data.model.AlertSeverity
import com.runwayiq.data.model.BudgetLine
import com.runwayiq.data.model.FinancialSummary
import com.runwayiq.ui.AppState
import com.runwayiq.ui.NavScreen
import com.runwayiq.ui.components.*
import com.runwayiq.ui.theme.*
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun DashboardScreen(
    state: AppState,
    onSendMessage: (String) -> Unit,
    onDismissAlert: (Long) -> Unit,
    onGenerateBoardReport: () -> Unit,
    onDismissBoardReport: () -> Unit,
    onGenerateInsight: () -> Unit,
    onNavigate: (NavScreen) -> Unit,
) {
    val summary = state.summary
    Row(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SectionHeader(state.activeScenario?.name ?: "Financial Home") {
                OutlinedButton(
                    onClick = onGenerateBoardReport,
                    enabled = !state.isGeneratingBoardReport && summary != null && state.apiKey.isNotBlank(),
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (state.isGeneratingBoardReport) "Generating…" else "Generate board report")
                }
            }

            if (state.isLoading) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(3) { ShimmerBox(Modifier.weight(1f), height = 90.dp) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(3) { ShimmerBox(Modifier.weight(1f), height = 90.dp) }
                    }
                    ShimmerBox(height = 280.dp)
                }
            } else if (summary != null) {
                FinancialHomeCards(state = state, summary = summary)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Surface2,
                    border = BorderStroke(0.5.dp, BorderDefault),
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Revenue vs. expenses", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(16.dp))
                        RunwayChart(
                            monthlyData = summary.monthlyData,
                            startingCashCents = state.activeScenario?.cashBalanceCents ?: summary.cashBalanceCents,
                        )
                    }
                }

                RecentTransactionsSection(state)
                FinancialAlertsSection(state.alerts, onDismissAlert)
                GoalProgressSection(state.budgetLines, onViewAll = { onNavigate(NavScreen.BUDGET) })
                TopInsightSection(
                    insightText = state.insightText,
                    isGenerating = state.isGeneratingInsight,
                    hasApiKey = state.apiKey.isNotBlank(),
                    onGenerate = onGenerateInsight,
                )
            } else {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Add revenue and expense entries to see your dashboard.", color = TextMuted)
                }
            }
        }

        VerticalDivider(thickness = 0.5.dp, color = BorderDefault)
        CfoChatPanel(state, onSendMessage)
    }

    if (state.showBoardReport) {
        BoardReportDialog(
            content = state.boardReportContent,
            scenarioName = state.activeScenario?.name ?: "RunwayIQ",
            isLoading = state.isGeneratingBoardReport,
            onDismiss = onDismissBoardReport,
        )
    }
}

private data class HomeCardSpec(
    val title: String,
    val value: String,
    val deltaText: String?,
    val deltaIncreased: Boolean,
    val deltaFavorable: Boolean,
    val sparkline: List<Float>,
    val sparklineColor: Color,
)

private fun buildHomeCards(
    state: AppState,
    summary: FinancialSummary,
    purple: Color,
    teal: Color,
    coral: Color,
): List<HomeCardSpec> {
    val months = summary.monthlyData // ascending: oldest -> latest
    val latest = months.lastOrNull()
    val prev = months.getOrNull(months.size - 2)

    fun pctChange(current: Long, previous: Long): Double? =
        if (previous == 0L) null else ((current - previous).toDouble() / previous) * 100.0

    fun pctLabel(pct: Double?): String? = pct?.let { "${if (it >= 0) "+" else ""}${"%.1f".format(it)}% vs last mo." }

    val currentCash = state.activeScenario?.cashBalanceCents ?: summary.cashBalanceCents
    val cashChangeCents = latest?.netCents ?: 0L
    var runningCash = currentCash - months.sumOf { it.netCents }
    val cashSeries = months.map { m -> runningCash += m.netCents; runningCash.toFloat() }

    val mrrSeries = months.map { m ->
        state.revenues.filter { it.month == m.month && it.category == "mrr" }.sumOf { it.amountCents }.toFloat()
    }

    val incomeSeries = months.map { it.revenueCents.toFloat() }
    val spendingSeries = months.map { it.expensesCents.toFloat() }
    val netSeries = months.map { it.netCents.toFloat() }
    val incomePct = pctChange(latest?.revenueCents ?: 0L, prev?.revenueCents ?: 0L)
    val spendingPct = pctChange(latest?.expensesCents ?: 0L, prev?.expensesCents ?: 0L)

    val savingsCents = latest?.netCents ?: 0L
    val prevSavingsCents = prev?.netCents ?: 0L

    val last3 = months.takeLast(3)
    val prior3 = months.dropLast(3).takeLast(3)
    val cashFlowCents = if (last3.isEmpty()) 0L else last3.map { it.netCents }.average().toLong()
    val priorCashFlowCents = if (prior3.isEmpty()) null else prior3.map { it.netCents }.average().toLong()
    val cashFlowPct = priorCashFlowCents?.let { pctChange(cashFlowCents, it) }

    return listOf(
        HomeCardSpec(
            title = "Cash",
            value = formatDollars(currentCash),
            deltaText = "${signedDollars(cashChangeCents)} vs last mo.",
            deltaIncreased = cashChangeCents >= 0,
            deltaFavorable = cashChangeCents >= 0,
            sparkline = cashSeries,
            sparklineColor = purple,
        ),
        HomeCardSpec(
            title = "Runway",
            value = if (summary.runwayMonths == Double.MAX_VALUE) "∞" else formatRunway(summary.runwayMonths),
            deltaText = "3-mo avg burn ${formatDollars(summary.burnRateCents)}/mo",
            deltaIncreased = false,
            deltaFavorable = summary.runwayMonths >= 6 || summary.runwayMonths == Double.MAX_VALUE,
            sparkline = netSeries,
            sparklineColor = if (summary.runwayMonths < 6) coral else teal,
        ),
        HomeCardSpec(
            title = "MRR",
            value = formatDollars(summary.mrrCents),
            deltaText = "${if (summary.mrrGrowthPct >= 0) "+" else ""}${"%.1f".format(summary.mrrGrowthPct)}% vs last mo.",
            deltaIncreased = summary.mrrGrowthPct >= 0,
            deltaFavorable = summary.mrrGrowthPct >= 0,
            sparkline = mrrSeries,
            sparklineColor = teal,
        ),
        HomeCardSpec(
            title = "Monthly income",
            value = formatDollars(latest?.revenueCents ?: 0L),
            deltaText = pctLabel(incomePct),
            deltaIncreased = (incomePct ?: 0.0) >= 0,
            deltaFavorable = (incomePct ?: 0.0) >= 0,
            sparkline = incomeSeries,
            sparklineColor = teal,
        ),
        HomeCardSpec(
            title = "Monthly spending",
            value = formatDollars(latest?.expensesCents ?: 0L),
            deltaText = pctLabel(spendingPct),
            deltaIncreased = (spendingPct ?: 0.0) >= 0,
            deltaFavorable = (spendingPct ?: 0.0) < 0,
            sparkline = spendingSeries,
            sparklineColor = coral,
        ),
        HomeCardSpec(
            title = "Monthly savings",
            value = signedDollars(savingsCents),
            deltaText = "${signedDollars(savingsCents - prevSavingsCents)} vs last mo.",
            deltaIncreased = savingsCents >= prevSavingsCents,
            deltaFavorable = savingsCents >= prevSavingsCents,
            sparkline = netSeries,
            sparklineColor = if (savingsCents >= 0) teal else coral,
        ),
        HomeCardSpec(
            title = "Cash flow",
            value = "${signedDollars(cashFlowCents)}/mo",
            deltaText = if (cashFlowPct != null) pctLabel(cashFlowPct) else "3-month average",
            deltaIncreased = cashFlowPct?.let { it >= 0 } ?: (cashFlowCents >= 0),
            deltaFavorable = cashFlowPct?.let { it >= 0 } ?: (cashFlowCents >= 0),
            sparkline = netSeries,
            sparklineColor = if (cashFlowCents >= 0) teal else coral,
        ),
    )
}

@Composable
private fun FinancialHomeCards(state: AppState, summary: FinancialSummary) {
    // Resolved here (composable context) since buildHomeCards runs inside a
    // remember{} calculation block, which can't call @Composable color getters.
    val purple = Purple
    val teal = Teal
    val coral = Coral
    val cards = remember(state.revenues, state.expenses, state.activeScenario, summary, purple, teal, coral) {
        buildHomeCards(state, summary, purple, teal, coral)
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        cards.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { card ->
                    SummaryCard(
                        title = card.title,
                        value = card.value,
                        deltaText = card.deltaText,
                        deltaIncreased = card.deltaIncreased,
                        deltaFavorable = card.deltaFavorable,
                        sparklineValues = card.sparkline,
                        sparklineColor = card.sparklineColor,
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private data class TransactionRow(
    val label: String,
    val month: String,
    val category: String,
    val amountCents: Long,
    val isIncome: Boolean,
    val sortKey: Long,
)

@Composable
private fun RecentTransactionsSection(state: AppState) {
    val rows = remember(state.revenues, state.expenses) {
        val income = state.revenues.map {
            TransactionRow(it.label, it.month, it.category, it.amountCents, true, it.id)
        }
        val spending = state.expenses.map {
            TransactionRow(it.label, it.month, it.category, it.amountCents, false, it.id)
        }
        (income + spending).sortedByDescending { it.sortKey }.take(6)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Recent transactions", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        if (rows.isEmpty()) {
            Text("No transactions yet.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        } else {
            Surface(shape = RoundedCornerShape(12.dp), color = Surface2, border = BorderStroke(0.5.dp, BorderDefault)) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    rows.forEachIndexed { index, row ->
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(row.label, fontSize = 14.sp, color = TextPrimary)
                                Text("${row.month} · ${row.category}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                "${if (row.isIncome) "+" else "-"}${formatDollars(row.amountCents)}",
                                fontFamily = NumericFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (row.isIncome) Teal else Coral,
                            )
                        }
                        if (index < rows.lastIndex) HorizontalDivider(color = BorderDefault, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialAlertsSection(alerts: List<Alert>, onDismissAlert: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Financial alerts", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        if (alerts.isEmpty()) {
            Text("No active alerts.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                alerts.forEach { alert ->
                    AlertBanner(alert = alert, onDismiss = { onDismissAlert(alert.id) })
                }
            }
        }
    }
}

@Composable
private fun GoalProgressSection(budgetLines: List<BudgetLine>, onViewAll: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Goal progress", fontWeight = FontWeight.SemiBold, color = TextPrimary)
            TextButton(onClick = onViewAll) { Text("View all budgets", fontSize = 13.sp) }
        }
        val targeted = budgetLines.filter { it.targetCents > 0 }
        if (targeted.isEmpty()) {
            Text(
                "Set a budget target to track progress toward your goals.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                targeted.take(3).forEach { line ->
                    val fraction = (line.actualCents.toFloat() / line.targetCents.toFloat()).coerceIn(0f, 1f)
                    val barColor = if (line.isOverBudget) Coral else Teal
                    Column {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(line.category.replace('_', ' '), fontSize = 13.sp, color = TextPrimary)
                            Text(
                                "${formatDollars(line.actualCents)} / ${formatDollars(line.targetCents)}",
                                fontFamily = NumericFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = fraction,
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = barColor,
                            trackColor = Surface1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopInsightSection(
    insightText: String?,
    isGenerating: Boolean,
    hasApiKey: Boolean,
    onGenerate: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(12.dp), color = Surface2, border = BorderStroke(0.5.dp, BorderDefault)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Amber, modifier = Modifier.size(20.dp))
            Column(Modifier.weight(1f)) {
                Text("Top insight", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                when {
                    !hasApiKey -> Text(
                        "Add your Groq API key in Settings to generate AI-powered insights.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                    isGenerating -> Text("Thinking…", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    insightText != null -> Text(insightText, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    else -> Text(
                        "Generate a one-line, data-driven insight about your current financials.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                }
            }
            if (hasApiKey) {
                TextButton(onClick = onGenerate, enabled = !isGenerating) {
                    Text(if (insightText == null) "Generate" else "Refresh", fontSize = 13.sp)
                }
            }
        }
    }
}

data class BoardReportSection(val title: String, val body: String)

private val KNOWN_BOARD_REPORT_HEADERS = listOf("Key Metrics", "Highlights", "Risks", "The Ask")

/**
 * The model is asked for exact "## <Header>" markers, but streamed responses
 * don't always come back with real newlines around them (they can land mid
 * paragraph). Splitting on the known header text itself — rather than on
 * line boundaries — is robust to that either way.
 */
fun parseBoardReportSections(text: String): List<BoardReportSection> {
    val cleaned = text.trim()
    val headerPattern = KNOWN_BOARD_REPORT_HEADERS.joinToString("|") { Regex.escape(it) }
    val headerRegex = Regex("#{1,3}\\s*($headerPattern)\\s*:?")
    val matches = headerRegex.findAll(cleaned).toList()

    if (matches.isEmpty()) {
        return listOf(BoardReportSection("Board Report", cleaned))
    }

    return matches.mapIndexed { index, match ->
        val bodyStart = match.range.last + 1
        val bodyEnd = matches.getOrNull(index + 1)?.range?.first ?: cleaned.length
        BoardReportSection(match.groupValues[1], cleaned.substring(bodyStart, bodyEnd).trim())
    }
}

private val BOLD_PATTERN = Regex("\\*\\*(.+?)\\*\\*")

/** Renders "**bold**" markdown spans as actual bold text instead of showing the literal asterisks. */
fun renderInlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    var lastIndex = 0
    for (match in BOLD_PATTERN.findAll(text)) {
        if (match.range.first > lastIndex) {
            append(text.substring(lastIndex, match.range.first))
        }
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
            append(match.groupValues[1])
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < text.length) {
        append(text.substring(lastIndex))
    }
}

@Composable
fun BoardReportDialog(
    content: String?,
    scenarioName: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
) {
    var copied by remember { mutableStateOf(false) }
    var saveStatus by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 560.dp),
        title = {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Board Report", fontWeight = FontWeight.Medium)
                if (!isLoading && content != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = {
                                Toolkit.getDefaultToolkit().systemClipboard
                                    .setContents(StringSelection(content), null)
                                copied = true
                            },
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (copied) "Copied!" else "Copy", fontSize = 13.sp)
                        }
                        TextButton(
                            onClick = {
                                val sections = parseBoardReportSections(content)
                                val chooser = JFileChooser().apply {
                                    dialogTitle = "Save board report"
                                    fileFilter = FileNameExtensionFilter("PDF document", "pdf")
                                    selectedFile = File("RunwayIQ Board Report - $scenarioName.pdf")
                                }
                                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                                    val target = chooser.selectedFile.let {
                                        if (it.extension.equals("pdf", ignoreCase = true)) it else File(it.path + ".pdf")
                                    }
                                    saveStatus = try {
                                        PdfExporter.exportBoardReport(scenarioName, sections, target)
                                        "Saved!"
                                    } catch (e: Exception) {
                                        "Failed to save: ${e.message ?: "unknown error"}"
                                    }
                                }
                            },
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(saveStatus ?: "Save as PDF", fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        text = {
            Box(Modifier.heightIn(min = 200.dp, max = 480.dp).verticalScroll(rememberScrollState())) {
                if (isLoading || content == null) {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator(color = Purple, modifier = Modifier.size(32.dp))
                        Text("Writing your investor update…", color = TextMuted)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        parseBoardReportSections(content).forEach { section ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    section.title,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = PurpleDark,
                                )
                                HorizontalDivider(color = BorderDefault, thickness = 0.5.dp)
                                Text(
                                    renderInlineMarkdown(section.body),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    lineHeight = 22.sp,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Close") }
        },
    )
}

@Composable
fun AlertBanner(alert: Alert, onDismiss: () -> Unit) {
    val accent = if (alert.severity == AlertSeverity.RED) Coral else Amber
    val bgColor = accent.copy(alpha = 0.12f)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp, 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            Text(alert.message, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun CfoChatPanel(state: AppState, onSendMessage: (String) -> Unit) {
    val listState = rememberLazyListState()
    var input by remember { mutableStateOf("") }
    val messages = state.chatMessages
    val streaming = state.streamingResponse
    val hasApiKey = state.apiKey.isNotBlank()

    LaunchedEffect(messages.size, streaming) {
        val count = messages.size + if (streaming.isNotBlank()) 1 else 0
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    LaunchedEffect(state.chatSendTrigger) {
        if (state.chatSendTrigger > 0 && input.isNotBlank() && !state.isStreaming && hasApiKey) {
            onSendMessage(input.trim())
            input = ""
        }
    }

    Column(Modifier.width(340.dp).fillMaxHeight()) {
        Surface(color = Surface1, border = BorderStroke(0.5.dp, BorderDefault)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("CFO chat", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                if (!hasApiKey) {
                    Text("Add API key in Settings", style = MaterialTheme.typography.bodySmall, color = Coral)
                }
            }
        }

        LazyColumn(
            Modifier.weight(1f).padding(horizontal = 12.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            if (messages.isEmpty() && streaming.isBlank()) {
                item {
                    Text(
                        "Ask your CFO anything.\n\nTry: \"How long is my runway if burn increases 20%?\" or \"What's my path to default alive?\"\n\n⌘/Ctrl+Enter to send",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
            items(messages) { msg -> ChatBubble(msg.role, msg.content) }
            if (streaming.isNotBlank()) {
                item { ChatBubble("assistant", streaming, isStreaming = true) }
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = BorderDefault)
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask your CFO...", style = MaterialTheme.typography.bodyMedium) },
                enabled = hasApiKey && !state.isStreaming,
                maxLines = 4,
                textStyle = MaterialTheme.typography.bodyMedium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (input.isNotBlank() && !state.isStreaming) {
                        onSendMessage(input.trim())
                        input = ""
                    }
                }),
            )
            Button(
                onClick = {
                    if (input.isNotBlank() && !state.isStreaming) {
                        onSendMessage(input.trim())
                        input = ""
                    }
                },
                enabled = hasApiKey && input.isNotBlank() && !state.isStreaming,
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
            ) {
                Text("Send", fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ChatBubble(role: String, content: String, isStreaming: Boolean = false) {
    val isUser = role == "user"
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 12.dp, topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 12.dp,
            ),
            color = if (isUser) PurpleLight else Surface2,
            border = if (!isUser) BorderStroke(0.5.dp, BorderDefault) else null,
            modifier = Modifier.widthIn(max = 280.dp),
        ) {
            Text(
                text = content + if (isStreaming) "▋" else "",
                modifier = Modifier.padding(10.dp, 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) PurpleDark else TextPrimary,
            )
        }
    }
}
