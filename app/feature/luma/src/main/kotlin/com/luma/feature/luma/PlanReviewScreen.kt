package com.luma.feature.luma

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.luma.model.ChangeAction
import com.luma.model.PlanChange
import com.luma.model.PlanItem
import com.luma.model.PlanProposal

@Composable
fun PlanReviewScreen(
    proposal: PlanProposal,
    selectedChangeIds: Set<String>,
    onToggleChange: (String) -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onEdit: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("YOUR PLAN.\nYOUR CALL.", style = MaterialTheme.typography.displayMedium)
            Text(
                "Choose every change, some changes, or none. Luma never skips this step.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            LumaCard(
                containerColor = if (proposal.isFeasible) {
                    LumaColors.Lime
                } else {
                    LumaColors.Coral
                },
            ) {
                Text("VALIDATION", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(7.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = if (proposal.isFeasible) Icons.Rounded.CheckCircle
                        else Icons.Rounded.WarningAmber,
                        contentDescription = null,
                    )
                    Column {
                        Text(
                            if (proposal.isFeasible) "NO OVERLAPS.\nREADY TO MOVE." else "THIS NEEDS\nA DECISION.",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(proposal.summary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        if (proposal.assumptions.isNotEmpty()) {
            item {
                LumaCard {
                    Text("Assumptions", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    proposal.assumptions.forEach { assumption ->
                        Text("• $assumption", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item {
            SectionHeader("Proposed changes (${proposal.changes.size})")
        }
        items(proposal.changes, key = PlanChange::id) { change ->
            ChangeCard(
                change = change,
                selected = change.id in selectedChangeIds,
                onToggle = { onToggleChange(change.id) },
            )
        }
        proposal.unresolvedQuestions.takeIf { it.isNotEmpty() }?.let { questions ->
            item {
                LumaCard(containerColor = LumaColors.Coral.copy(alpha = 0.10f)) {
                    Text("Unresolved questions", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    questions.forEach { question ->
                        Text("• $question", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item {
            LumaCard(containerColor = LumaColors.Violet.copy(alpha = 0.11f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null)
                    Text(
                        "Protected events remain unchanged. External calendar writes require this approval.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        item {
            PrimaryLumaButton(
                text = "Approve selected (${selectedChangeIds.size})",
                onClick = onApprove,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedChangeIds.isNotEmpty() && proposal.isFeasible,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Reject", color = MaterialTheme.colorScheme.error)
                }
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = null)
                    Spacer(Modifier.padding(3.dp))
                    Text("Edit plan")
                }
            }
        }
    }
}

@Composable
private fun ChangeCard(
    change: PlanChange,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    LumaCard(onClick = onToggle) {
        Row(verticalAlignment = Alignment.Top) {
            Checkbox(checked = selected, onCheckedChange = { onToggle() })
            Column(Modifier.weight(1f)) {
                Text(change.action.displayName(), style = MaterialTheme.typography.labelLarge)
                Text(
                    change.after?.title ?: change.before?.title.orEmpty(),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(10.dp))
                BeforeAfter(change.before, change.after)
                Spacer(Modifier.height(10.dp))
                Text("Why", style = MaterialTheme.typography.labelLarge)
                Text(
                    change.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    change.affectedLifeAreas.forEach { LifeAreaPill(it, compact = true) }
                    change.after?.let { FlexibilityChip(it.flexibility) }
                }
            }
        }
    }
}

@Composable
private fun BeforeAfter(
    before: PlanItem?,
    after: PlanItem?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LumaCard(
            modifier = Modifier.weight(1f),
            containerColor = LumaColors.Coral.copy(alpha = 0.08f),
            showOffsetShadow = false,
        ) {
            Text("Before", style = MaterialTheme.typography.labelLarge)
            Text(before?.startMinute?.formatTime() ?: "Not scheduled")
        }
        LumaCard(
            modifier = Modifier.weight(1f),
            containerColor = LumaColors.Lime.copy(alpha = 0.13f),
            showOffsetShadow = false,
        ) {
            Text("After", style = MaterialTheme.typography.labelLarge)
            Text(after?.startMinute?.formatTime() ?: "Removed")
        }
    }
}

private fun ChangeAction.displayName(): String = when (this) {
    ChangeAction.CREATE -> "Add"
    ChangeAction.MOVE -> "Move"
    ChangeAction.RESIZE -> "Resize"
    ChangeAction.DELETE -> "Remove"
    ChangeAction.PROTECT -> "Protect"
}

private fun Int.formatTime(): String {
    val hour = this / 60
    val minute = this % 60
    val suffix = if (hour < 12) "AM" else "PM"
    val displayHour = (hour % 12).let { if (it == 0) 12 else it }
    return "%d:%02d %s".format(displayHour, minute, suffix)
}
