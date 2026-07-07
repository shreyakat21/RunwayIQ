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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import com.runwayiq.data.model.Alert
import com.runwayiq.data.model.AlertSeverity
import com.runwayiq.ui.AppState
import com.runwayiq.ui.components.*
import com.runwayiq.ui.theme.*
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
fun DashboardScreen(
    state: AppState,
    onSendMessage: (String) -> Unit,
    onDismissAlert: (Long) -> Unit,
    onGenerateBoardReport: () -> Unit,
    onDismissBoardReport: () -> Unit,
) {
    val summary = state.summary
    Row(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SectionHeader(state.activeScenario?.name ?: "Dashboard") {
                OutlinedButton(
                    onClick = onGenerateBoardReport,
                    enabled = !state.isGeneratingBoardReport && summary != null && state.apiKey.isNotBlank(),
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (state.isGeneratingBoardReport) "Generating…" else "Generate board report")
                }
            }

            state.alerts.forEach { alert ->
                AlertBanner(alert = alert, onDismiss = { onDismissAlert(alert.id) })
            }

            if (state.isLoading) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(4) { ShimmerBox(Modifier.weight(1f), height = 90.dp) }
                    }
                    ShimmerBox(height = 280.dp)
                }
            } else if (summary != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnimatedRunwayMetricCard(
                        runwayMonths = summary.runwayMonths,
                        subtitle = if (summary.runwayMonths < 6) "Warning: < 6 months" else "At current burn",
                        modifier = Modifier.weight(1f),
                    )
                    AnimatedDollarMetricCard(
                        label = "Monthly burn",
                        cents = summary.burnRateCents,
                        subtitle = "3-month average",
                        modifier = Modifier.weight(1f),
                    )
                    AnimatedDollarMetricCard(
                        label = "MRR",
                        cents = summary.mrrCents,
                        subtitle = "${if (summary.mrrGrowthPct >= 0) "+" else ""}${"%.1f".format(summary.mrrGrowthPct)}% last month",
                        accentColor = if (summary.mrrGrowthPct >= 0) Teal else Coral,
                        modifier = Modifier.weight(1f),
                    )
                    AnimatedDollarMetricCard(
                        label = "Cash",
                        cents = summary.cashBalanceCents,
                        subtitle = "Current balance",
                        modifier = Modifier.weight(1f),
                    )
                }

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
            isLoading = state.isGeneratingBoardReport,
            onDismiss = onDismissBoardReport,
        )
    }
}

data class BoardReportSection(val title: String, val body: String)

fun parseBoardReportSections(text: String): List<BoardReportSection> {
    val sections = mutableListOf<BoardReportSection>()
    var currentTitle = "Overview"
    val currentBody = StringBuilder()

    for (line in text.lines()) {
        if (line.startsWith("## ")) {
            if (currentBody.isNotBlank()) {
                sections.add(BoardReportSection(currentTitle, currentBody.toString().trim()))
            }
            currentTitle = line.removePrefix("## ").trim()
            currentBody.clear()
        } else {
            currentBody.appendLine(line)
        }
    }
    if (currentBody.isNotBlank()) {
        sections.add(BoardReportSection(currentTitle, currentBody.toString().trim()))
    }
    if (sections.isEmpty()) {
        sections.add(BoardReportSection("Board Report", text.trim()))
    }
    return sections
}

@Composable
fun BoardReportDialog(
    content: String?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
) {
    var copied by remember { mutableStateOf(false) }

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
                    TextButton(
                        onClick = {
                            Toolkit.getDefaultToolkit().systemClipboard
                                .setContents(StringSelection(content), null)
                            copied = true
                        },
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (copied) "Copied!" else "Copy to clipboard", fontSize = 13.sp)
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
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = PurpleDark,
                                )
                                HorizontalDivider(color = BorderDefault, thickness = 0.5.dp)
                                Text(
                                    section.body,
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
    val bgColor = if (alert.severity == AlertSeverity.RED) Coral.copy(alpha = 0.12f) else Color(0xFFF59E0B).copy(alpha = 0.12f)
    val accent = if (alert.severity == AlertSeverity.RED) Coral else Color(0xFFD97706)

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
