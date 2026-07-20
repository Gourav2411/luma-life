package com.luma.feature.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.luma.designsystem.FlexibilityChip
import com.luma.designsystem.LifeAreaPill
import com.luma.designsystem.LumaCard
import com.luma.designsystem.LumaColors
import com.luma.designsystem.PrimaryLumaButton
import com.luma.designsystem.SectionHeader
import com.luma.model.Direction
import com.luma.model.FocusSession
import com.luma.model.LifeArea
import com.luma.model.PlanHorizon
import com.luma.model.PlanItem
import com.luma.model.SemesterPlan
import com.luma.model.WeekPlan

@Composable
fun PlansScreen(
    direction: Direction,
    semester: SemesterPlan,
    week: WeekPlan,
    dayItems: List<PlanItem>,
    session: FocusSession,
    onAskLuma: (PlanHorizon) -> Unit,
    onStartSession: () -> Unit,
) {
    var horizonName by rememberSaveable { mutableStateOf(PlanHorizon.WEEK.name) }
    val horizon = PlanHorizon.valueOf(horizonName)

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("PLAN\nTHE ARC.", style = MaterialTheme.typography.displayMedium)
            Text(
                "Direction → semester → week → today → right now.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PlanHorizon.entries) { option ->
                    HorizonChip(
                        horizon = option,
                        selected = option == horizon,
                        onClick = { horizonName = option.name },
                    )
                }
            }
        }
        item {
            when (horizon) {
                PlanHorizon.DIRECTION -> DirectionContent(direction, onAskLuma)
                PlanHorizon.SEMESTER -> SemesterContent(semester, onAskLuma)
                PlanHorizon.WEEK -> WeekContent(week, onAskLuma)
                PlanHorizon.DAY -> DayContent(dayItems, onAskLuma)
                PlanHorizon.SESSION -> SessionContent(session, onStartSession)
            }
        }
    }
}

@Composable
private fun HorizonChip(
    horizon: PlanHorizon,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        color = if (selected) LumaColors.Lime else MaterialTheme.colorScheme.surface,
        contentColor = LumaColors.Ink,
        shape = CircleShape,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = horizon.displayName(),
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun DirectionContent(
    direction: Direction,
    onAskLuma: (PlanHorizon) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        LumaCard(containerColor = LumaColors.Cobalt.copy(alpha = 0.18f)) {
            Text("DIRECTION · 6–12 MONTHS", style = MaterialTheme.typography.labelMedium)
            Icon(Icons.Rounded.Flag, contentDescription = null)
            Spacer(Modifier.height(12.dp))
            Text("The direction I’m exploring", style = MaterialTheme.typography.labelLarge)
            Text(direction.statement, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                direction.lifeAreas.take(3).forEach { LifeAreaPill(it, compact = true) }
            }
        }
        SectionHeader("What a balanced version looks like")
        val statements = listOf(
            LifeArea.ACADEMICS to "Understand the fundamentals, not only pass.",
            LifeArea.SKILLS to "Ship useful work and explain my choices.",
            LifeArea.HEALTH to "Keep energy for the life I’m building.",
            LifeArea.RELATIONSHIPS to "Stay present with people who matter.",
            LifeArea.FUN to "Play without treating it as a failure.",
            LifeArea.SELF_DIRECTION to "Review whether this direction still fits.",
        )
        statements.forEach { (area, statement) ->
            LumaCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    LifeAreaPill(area, compact = true)
                    Text(statement, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        PrimaryLumaButton(
            text = "Refine this direction with Luma",
            onClick = { onAskLuma(PlanHorizon.DIRECTION) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SemesterContent(
    semester: SemesterPlan,
    onAskLuma: (PlanHorizon) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        LumaCard(containerColor = LumaColors.Violet.copy(alpha = 0.2f)) {
            Text("SEMESTER MAP", style = MaterialTheme.typography.labelMedium)
            Text(semester.label, style = MaterialTheme.typography.headlineMedium)
            Text(
                "${semester.startDate} — ${semester.endDate}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        SectionHeader("Semester outcomes")
        semester.outcomes.forEach { outcome ->
            LumaCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LifeAreaPill(outcome.lifeArea, compact = true)
                    Spacer(Modifier.size(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(outcome.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            outcome.evidenceTarget,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text("${outcome.progressPercent}%", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { outcome.progressPercent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = LumaColors.Cobalt,
                    trackColor = LumaColors.Cobalt.copy(alpha = 0.12f),
                )
            }
        }
        PrimaryLumaButton(
            text = "Rebalance this semester",
            onClick = { onAskLuma(PlanHorizon.SEMESTER) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun WeekContent(
    week: WeekPlan,
    onAskLuma: (PlanHorizon) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        LumaCard(containerColor = LumaColors.Lime) {
            Text("CAPACITY, NOT FANTASY", style = MaterialTheme.typography.labelMedium)
            Text("This week", style = MaterialTheme.typography.headlineMedium)
            Text(
                "${week.committedMinutes / 60}h committed of ${week.capacityMinutes / 60}h available",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { week.committedMinutes.toFloat() / week.capacityMinutes.coerceAtLeast(1) },
                modifier = Modifier.fillMaxWidth(),
                color = LumaColors.Ink,
                trackColor = Color.White.copy(alpha = 0.6f),
            )
        }
        SectionHeader("Weekly priorities")
        week.priorities.forEachIndexed { index, priority ->
            LumaCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(LumaColors.Lime, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${index + 1}", style = MaterialTheme.typography.labelLarge)
                    }
                    Text(priority, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        SectionHeader("Days")
        week.days.forEach { day ->
            LumaCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.size(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(day.date, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${day.items.size} blocks · ${day.items.sumOf { it.durationMinutes } / 60}h planned",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Rounded.ArrowForward, contentDescription = "Open day")
                }
            }
        }
        PrimaryLumaButton(
            text = "Plan this week with Luma",
            onClick = { onAskLuma(PlanHorizon.WEEK) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DayContent(
    items: List<PlanItem>,
    onAskLuma: (PlanHorizon) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Day blocks", action = "Add")
        items.sortedBy { it.startMinute }.forEach { item ->
            LumaCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${item.startMinute?.formatTime() ?: "Anytime"} · ${item.durationMinutes} min · ${item.energy.name.lowercase()} energy",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        LifeAreaPill(item.lifeArea, compact = true)
                    }
                    FlexibilityChip(item.flexibility)
                }
            }
        }
        PrimaryLumaButton(
            text = "Replan this day",
            onClick = { onAskLuma(PlanHorizon.DAY) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SessionContent(
    session: FocusSession,
    onStartSession: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        LumaCard(containerColor = LumaColors.Ink) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = LumaColors.Lime)
            Spacer(Modifier.height(12.dp))
            Text(
                "Do now",
                style = MaterialTheme.typography.labelLarge,
                color = LumaColors.Lime,
            )
            Text(
                session.outcome,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
            )
            Text(
                "${session.durationMinutes} focused minutes",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.75f),
            )
        }
        SectionHeader("Microsteps")
        session.steps.forEach { step ->
            LumaCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        if (step.isComplete) Icons.Rounded.Check else Icons.Rounded.Add,
                        contentDescription = null,
                        tint = if (step.isComplete) LumaColors.Aqua else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(step.title, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        LumaCard {
            Text("Evidence after the session", style = MaterialTheme.typography.titleMedium)
            Text(
                session.evidencePrompt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        PrimaryLumaButton(
            text = "Start focus session",
            onClick = onStartSession,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun PlanHorizon.displayName(): String = when (this) {
    PlanHorizon.DIRECTION -> "Direction"
    PlanHorizon.SEMESTER -> "Semester"
    PlanHorizon.WEEK -> "Week"
    PlanHorizon.DAY -> "Day"
    PlanHorizon.SESSION -> "Session"
}

private fun Int.formatTime(): String {
    val hour = this / 60
    val minute = this % 60
    val suffix = if (hour < 12) "AM" else "PM"
    val displayHour = (hour % 12).let { if (it == 0) 12 else it }
    return "%d:%02d %s".format(displayHour, minute, suffix)
}
