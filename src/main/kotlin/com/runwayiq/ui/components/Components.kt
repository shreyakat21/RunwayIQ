package com.runwayiq.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.runwayiq.data.model.MonthlyData
import com.runwayiq.ui.NavScreen
import com.runwayiq.ui.theme.*

@Composable
fun MetricCard(
    label: String,
    value: String,
    subtitle: String = "",
    accentColor: Color = Purple,
    modifier: Modifier = Modifier,
    pulseWarning: Boolean = false,
) {
    MetricCardShell(label, value, subtitle, accentColor, modifier, pulseWarning)
}

@Composable
fun AnimatedDollarMetricCard(
    label: String,
    cents: Long,
    subtitle: String,
    accentColor: Color = Purple,
    modifier: Modifier = Modifier,
) {
    val animated by animateFloatAsState(
        targetValue = cents.toFloat(),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = label,
    )
    MetricCardShell(label, formatDollars(animated.toLong()), subtitle, accentColor, modifier)
}

@Composable
fun AnimatedRunwayMetricCard(
    runwayMonths: Double,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val isLow = runwayMonths < 6 && runwayMonths != Double.MAX_VALUE
    val animated by animateFloatAsState(
        targetValue = if (runwayMonths == Double.MAX_VALUE) 0f else runwayMonths.toFloat(),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "runway",
    )
    val display = if (runwayMonths == Double.MAX_VALUE) "∞" else formatRunway(animated.toDouble())
    MetricCardShell(
        label = "Runway",
        value = display,
        subtitle = subtitle,
        accentColor = if (isLow) Coral else Teal,
        modifier = modifier,
        pulseWarning = isLow,
    )
}

@Composable
private fun MetricCardShell(
    label: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    pulseWarning: Boolean = false,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseAlpha",
    )
    val borderColor = if (pulseWarning) Coral.copy(alpha = pulseAlpha) else BorderDefault
    val borderWidth = if (pulseWarning) 1.5.dp else 0.5.dp

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (pulseWarning) Coral.copy(alpha = 0.06f) else Surface2,
        border = BorderStroke(borderWidth, borderColor),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, letterSpacing = 0.2.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                fontSize = 23.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = NumericFontFamily,
                color = TextPrimary,
            )
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = accentColor)
            }
        }
    }
}

/** A minimal, axis-less line chart for trend indicators inside a summary card. */
@Composable
fun Sparkline(values: List<Float>, color: Color, modifier: Modifier = Modifier) {
    if (values.size < 2) return
    val min = values.min()
    val max = values.max()
    val range = (max - min).let { if (it == 0f) 1f else it }

    Canvas(modifier) {
        val stepX = size.width / (values.size - 1)
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = stepX * i
            val y = size.height * (1f - (v - min) / range)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(width = 1.5.dp.toPx()))
    }
}

/**
 * A summary card for the Financial Home dashboard: a headline value, an
 * optional month-over-month delta (colored by whether the change is
 * favorable, not just by its sign — e.g. a spending increase is unfavorable
 * even though it's a positive delta), and an optional trend sparkline.
 */
@Composable
fun SummaryCard(
    title: String,
    value: String,
    deltaText: String?,
    deltaIncreased: Boolean = false,
    deltaFavorable: Boolean = true,
    sparklineValues: List<Float> = emptyList(),
    sparklineColor: Color = Purple,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Surface2,
        border = BorderStroke(0.5.dp, BorderDefault),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, letterSpacing = 0.2.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = NumericFontFamily,
                color = TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            if (deltaText != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(
                        if (deltaIncreased) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (deltaFavorable) Teal else Coral,
                        modifier = Modifier.size(11.dp),
                    )
                    Text(
                        deltaText,
                        fontSize = 12.sp,
                        fontFamily = NumericFontFamily,
                        color = if (deltaFavorable) Teal else Coral,
                    )
                }
            } else {
                Spacer(Modifier.height(14.dp))
            }
            if (sparklineValues.size >= 2) {
                Spacer(Modifier.height(8.dp))
                Sparkline(sparklineValues, sparklineColor, Modifier.fillMaxWidth().height(24.dp))
            }
        }
    }
}

@Composable
fun RunwayChart(
    monthlyData: List<MonthlyData>,
    startingCashCents: Long,
    modifier: Modifier = Modifier,
) {
    if (monthlyData.isEmpty()) return

    // Resolved here (composable context) since Canvas's draw lambda below is a
    // plain DrawScope block and can't call the theme's @Composable color getters.
    val gridColor = BorderDefault
    val revenueLineColor = Teal
    val expenseLineColor = Coral
    val cashLineColor = Purple

    val drawProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "chartProgress",
    )

    val cumulativeCash = remember(monthlyData, startingCashCents) {
        var balance = startingCashCents
        monthlyData.map { month ->
            balance += month.netCents
            balance
        }
    }

    val maxVal = remember(monthlyData, cumulativeCash) {
        monthlyData.indices.maxOf { i ->
            maxOf(
                monthlyData[i].revenueCents,
                monthlyData[i].expensesCents,
                cumulativeCash[i],
            )
        }.toFloat().coerceAtLeast(1f)
    }

    val yTicks = remember(maxVal) {
        (0..4).map { i -> (maxVal * i / 4f).toLong() }
    }

    Column(modifier) {
        Row(Modifier.fillMaxWidth().height(200.dp)) {
            Column(
                Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                yTicks.reversed().forEach { tick ->
                    Text(
                        formatDollars(tick),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        fontFamily = NumericFontFamily,
                        color = TextMuted,
                    )
                }
            }

            Canvas(Modifier.weight(1f).fillMaxHeight()) {
                val w = size.width
                val h = size.height
                val padTop = 8.dp.toPx()
                val padBottom = 8.dp.toPx()
                val chartW = w
                val chartH = h - padTop - padBottom
                val n = monthlyData.size
                val baseline = padTop + chartH

                repeat(5) { i ->
                    val y = padTop + chartH * i / 4f
                    drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 0.5.dp.toPx())
                }

                fun yFor(value: Long): Float {
                    val normalized = value / maxVal
                    val targetY = padTop + chartH * (1f - normalized)
                    return baseline - (baseline - targetY) * drawProgress
                }

                fun xFor(i: Int): Float = chartW * i / (n - 1).coerceAtLeast(1).toFloat()

                val revPath = Path()
                monthlyData.forEachIndexed { i, d ->
                    val x = xFor(i)
                    val y = yFor(d.revenueCents)
                    if (i == 0) revPath.moveTo(x, y) else revPath.lineTo(x, y)
                }
                drawPath(revPath, revenueLineColor, style = Stroke(width = 2.dp.toPx()))

                val expPath = Path()
                monthlyData.forEachIndexed { i, d ->
                    val x = xFor(i)
                    val y = yFor(d.expensesCents)
                    if (i == 0) expPath.moveTo(x, y) else expPath.lineTo(x, y)
                }
                drawPath(
                    expPath,
                    expenseLineColor,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 4f)),
                    ),
                )

                val cashPath = Path()
                cumulativeCash.forEachIndexed { i, cash ->
                    val x = xFor(i)
                    val y = yFor(cash)
                    if (i == 0) cashPath.moveTo(x, y) else cashPath.lineTo(x, y)
                }
                drawPath(cashPath, cashLineColor, style = Stroke(width = 2.dp.toPx()))

                if (drawProgress > 0.95f) {
                    monthlyData.forEachIndexed { i, d ->
                        val x = xFor(i)
                        drawCircle(revenueLineColor, radius = 3.dp.toPx(), center = Offset(x, yFor(d.revenueCents)))
                        drawCircle(expenseLineColor, radius = 3.dp.toPx(), center = Offset(x, yFor(d.expensesCents)))
                        drawCircle(cashLineColor, radius = 3.dp.toPx(), center = Offset(x, yFor(cumulativeCash[i])))
                    }
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 48.dp, top = 4.dp),
        ) {
            monthlyData.forEach { d ->
                Text(
                    formatMonthLabel(d.month),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }

        Row(Modifier.padding(start = 48.dp, top = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendDot(Teal, "Revenue")
            LegendDot(Coral, "Expenses")
            LegendDot(Purple, "Cash balance")
        }
    }
}

private fun formatMonthLabel(month: String): String {
    val parts = month.split("-")
    if (parts.size != 2) return month
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val idx = parts[1].toIntOrNull()?.minus(1) ?: return month
    return if (idx in monthNames.indices) monthNames[idx] else month
}

@Composable
fun ShimmerBox(modifier: Modifier = Modifier, height: Dp = 80.dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "shimmerOffset",
    )
    val brush = Brush.linearGradient(
        colors = listOf(Surface2, Surface1, Surface2),
        start = Offset(offset - 200f, 0f),
        end = Offset(offset, 0f),
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(10.dp))
            .background(brush),
    )
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SideNav(
    current: NavScreen,
    onNavigate: (NavScreen) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    Surface(
        modifier = Modifier.width(200.dp).fillMaxHeight(),
        color = Surface1,
        border = BorderStroke(0.5.dp, BorderDefault),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "RunwayIQ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp,
                    color = Purple,
                )
                IconButton(onClick = onToggleTheme, modifier = Modifier.size(28.dp)) {
                    Icon(
                        if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = if (isDarkTheme) "Switch to light mode" else "Switch to dark mode",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            NavItem(NavScreen.DASHBOARD, "Dashboard", current, onNavigate)
            NavItem(NavScreen.REVENUE, "Revenue", current, onNavigate)
            NavItem(NavScreen.EXPENSES, "Expenses", current, onNavigate)
            NavItem(NavScreen.BUDGET, "Budget", current, onNavigate)
            NavItem(NavScreen.WHATIF, "What-If", current, onNavigate)
            NavItem(NavScreen.SCENARIOS, "Scenarios", current, onNavigate)
            Spacer(Modifier.weight(1f))
            NavItem(NavScreen.SETTINGS, "Settings", current, onNavigate)
        }
    }
}

@Composable
fun NavItem(screen: NavScreen, label: String, current: NavScreen, onNavigate: (NavScreen) -> Unit) {
    val active = screen == current
    Surface(
        onClick = { onNavigate(screen) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (active) PurpleLight else Color.Transparent,
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (active) PurpleDark else TextSecondary,
            fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
            fontSize = 14.sp,
        )
    }
}

@Composable
fun SectionHeader(title: String, action: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        action?.invoke()
    }
}

fun formatDollars(cents: Long): String {
    val dollars = cents / 100
    return when {
        dollars >= 1_000_000 -> "${"$"}${"%.1f".format(dollars / 1_000_000.0)}M"
        dollars >= 1_000 -> "${"$"}${"%.0f".format(dollars / 1_000.0)}k"
        else -> "${"$"}$dollars"
    }
}

/** Formats cents with an explicit +/- sign, safe for values that can go negative (deltas, variances). */
fun signedDollars(cents: Long): String {
    val sign = if (cents >= 0) "+" else "-"
    return "$sign${formatDollars(kotlin.math.abs(cents))}"
}

fun formatRunway(months: Double): String {
    return when {
        months == Double.MAX_VALUE -> "∞"
        months >= 24 -> "${"%.0f".format(months / 12)}y"
        else -> "${"%.1f".format(months)}mo"
    }
}
