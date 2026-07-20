package com.luma.feature.today

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.luma.designsystem.CapacitySegment
import com.luma.designsystem.FlexibilityChip
import com.luma.designsystem.LifeAreaPill
import com.luma.designsystem.LumaCard
import com.luma.designsystem.LumaColors
import com.luma.designsystem.SectionHeader
import com.luma.designsystem.lifeAreaColor
import com.luma.designsystem.lifeAreaIcon
import com.luma.model.EnergyLevel
import com.luma.model.Flexibility
import com.luma.model.PlanItem
import com.luma.model.PlanStatus

@Composable
fun TodayScreen(
    name: String,
    formattedDate: String,
    energy: EnergyLevel,
    items: List<PlanItem>,
    isOffline: Boolean,
    onEnergyChange: (EnergyLevel) -> Unit,
    onAskLuma: () -> Unit,
    onQuickCapture: () -> Unit,
    onToggleComplete: (String) -> Unit,
) {
    val activeItems = items.filter { it.status != PlanStatus.ARCHIVED }.sortedBy { it.startMinute }
    val topOutcomes = activeItems.filter { it.flexibility != Flexibility.PROTECTED }.take(3)
    val conflicts = findConflicts(activeItems)

    LazyColumn(
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 128.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(LumaColors.Lime, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = "Luma", tint = LumaColors.Ink)
                }
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                ) {
                    Text(
                        text = "HELLO, ${name.substringBefore(" ").uppercase()}",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = formattedDate.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(LumaColors.Coral, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = LumaColors.Ink,
                    )
                }
            }
        }

        item {
            Text(
                text = "MAKE\nTODAY FIT.",
                style = MaterialTheme.typography.displayLarge,
            )
        }

        if (isOffline) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LumaColors.Aqua.copy(alpha = 0.22f), CircleShape)
                        .padding(horizontal = 13.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(8.dp).background(LumaColors.Cobalt, CircleShape))
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "OFFLINE-SAFE · PLANS STAY EDITABLE",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        item {
            LumaCard(showOffsetShadow = false) {
                Text("ENERGY CHECK", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    EnergyLevel.entries.forEach { level ->
                        val selected = level == energy
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(MaterialTheme.shapes.small)
                                .background(if (selected) LumaColors.Ink else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onEnergyChange(level) }
                                .padding(vertical = 11.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = if (selected) LumaColors.Lime else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(17.dp),
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                level.shortName(),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Intensity adapts. Your worth does not.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            HeroPlanCard(activeItems, topOutcomes.size, onAskLuma)
        }

        item {
            LumaCard(showOffsetShadow = false) {
                Text("TOP OUTCOMES", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                topOutcomes.forEachIndexed { index, outcome ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleComplete(outcome.id) }
                            .padding(vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "0${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = lifeAreaColor(outcome.lifeArea),
                        )
                        Spacer(Modifier.size(10.dp))
                        Text(outcome.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Open outcome",
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
            }
        }

        if (conflicts.isNotEmpty()) {
            item {
                LumaCard(containerColor = LumaColors.Coral.copy(alpha = 0.12f), onClick = onAskLuma) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Column(Modifier.weight(1f)) {
                            Text("Potential conflict", style = MaterialTheme.typography.titleMedium)
                            Text(
                                conflicts.first(),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Text(
                            "Resolve",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }

        item { SectionHeader("TODAY’S PLAN", action = "ASK LUMA", onAction = onAskLuma) }

        items(activeItems, key = PlanItem::id) { item ->
            TimelineItem(item, onToggleComplete)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickAction(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Mic,
                    title = "Ask Luma",
                    subtitle = "Tell me what changed",
                    highlight = true,
                    onClick = onAskLuma,
                )
                QuickAction(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Add,
                    title = "Quick capture",
                    subtitle = "Add a task or note",
                    highlight = false,
                    onClick = onQuickCapture,
                )
            }
        }
    }
}

@Composable
private fun HeroPlanCard(
    items: List<PlanItem>,
    outcomeCount: Int,
    onReplan: () -> Unit,
) {
    val fixed = items.filter { it.flexibility == Flexibility.FIXED }.sumOf { it.durationMinutes }
    val flexible = items.filter { it.flexibility == Flexibility.FLEXIBLE }.sumOf { it.durationMinutes }
    val protected = items.filter { it.flexibility == Flexibility.PROTECTED }.sumOf { it.durationMinutes }
    val total = (fixed + flexible + protected).coerceAtLeast(1)

    LumaCard(containerColor = LumaColors.Lime, onClick = onReplan) {
        Text("TODAY AT A GLANCE", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(0.7f)) {
                Text("$outcomeCount", style = MaterialTheme.typography.displayMedium)
                Text("outcomes", style = MaterialTheme.typography.labelLarge)
            }
            Box(
                modifier = Modifier
                    .height(62.dp)
                    .padding(horizontal = 12.dp)
                    .background(LumaColors.Ink.copy(alpha = 0.25f)),
            )
            Column(Modifier.weight(1.15f)) {
                Text(formatDuration(total), style = MaterialTheme.typography.headlineLarge)
                Text("planned", style = MaterialTheme.typography.labelLarge)
            }
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(LumaColors.Ink)
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("REPLAN", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    Spacer(Modifier.size(6.dp))
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = LumaColors.Lime,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            BalanceMetric("FIXED", fixed, total, LumaColors.Cobalt, Modifier.weight(1f))
            BalanceMetric("FLEX", flexible, total, LumaColors.Coral, Modifier.weight(1f))
            BalanceMetric("LIFE", protected, total, LumaColors.Violet, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            CapacitySegment(LumaColors.Cobalt, fixed.toFloat() / total, "Fixed capacity")
            CapacitySegment(LumaColors.Coral, flexible.toFloat() / total, "Flexible capacity")
            CapacitySegment(LumaColors.Violet, protected.toFloat() / total, "Protected capacity")
        }
    }
}

@Composable
private fun BalanceMetric(label: String, minutes: Int, total: Int, color: Color, modifier: Modifier) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(Color.White.copy(alpha = 0.62f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.size(5.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("${minutes * 100 / total}%", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun TimelineItem(
    item: PlanItem,
    onToggleComplete: (String) -> Unit,
) {
    LumaCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = lifeAreaColor(item.lifeArea).copy(alpha = 0.08f),
        onClick = { onToggleComplete(item.id) },
        showOffsetShadow = false,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = item.startMinute?.formatTime() ?: "Anytime",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(0.27f),
            )
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(lifeAreaColor(item.lifeArea), MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = lifeAreaIcon(item.lifeArea),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (item.status == PlanStatus.COMPLETED) {
                        androidx.compose.ui.text.style.TextDecoration.LineThrough
                    } else null,
                )
                Text(
                    "${item.durationMinutes} min · ${item.source.name.lowercase().replaceFirstChar(Char::uppercase)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FlexibilityChip(item.flexibility)
        }
    }
}

@Composable
private fun QuickAction(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    highlight: Boolean,
    onClick: () -> Unit,
) {
    LumaCard(
        modifier = modifier,
        containerColor = if (highlight) LumaColors.Ink else MaterialTheme.colorScheme.surface,
        onClick = onClick,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (highlight) LumaColors.Lime else MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = if (highlight) Color.White else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun findConflicts(items: List<PlanItem>): List<String> =
    items.filter { it.startMinute != null }
        .sortedBy { it.startMinute }
        .zipWithNext()
        .mapNotNull { (first, second) ->
            if ((first.endMinute ?: 0) + first.travelAfterMinutes > (second.startMinute ?: Int.MAX_VALUE)) {
                "${first.title} overlaps ${second.title} including travel."
            } else null
        }

private fun Int.formatTime(): String {
    val hour = this / 60
    val minute = this % 60
    val suffix = if (hour < 12) "AM" else "PM"
    val displayHour = when (val value = hour % 12) {
        0 -> 12
        else -> value
    }
    return "%d:%02d %s".format(displayHour, minute, suffix)
}

private fun formatDuration(minutes: Int): String = "${minutes / 60}h ${minutes % 60}m"

private fun EnergyLevel.displayName(): String =
    name.lowercase().replaceFirstChar(Char::uppercase)

private fun EnergyLevel.shortName(): String = when (this) {
    EnergyLevel.LOW -> "LOW"
    EnergyLevel.STEADY -> "STEADY"
    EnergyLevel.HIGH -> "HIGH"
    EnergyLevel.OVERWHELMED -> "RESET"
}
