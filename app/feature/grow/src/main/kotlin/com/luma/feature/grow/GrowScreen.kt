package com.luma.feature.grow

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.luma.designsystem.LifeAreaPill
import com.luma.designsystem.LumaCard
import com.luma.designsystem.LumaColors
import com.luma.designsystem.PrimaryLumaButton
import com.luma.designsystem.SectionHeader
import com.luma.model.LifeArea
import com.luma.model.PlanStatus
import com.luma.model.Reflection
import com.luma.model.SkillRoadmap

@Composable
fun GrowScreen(
    roadmap: SkillRoadmap,
    reflections: List<Reflection>,
    onPlanNextEvidence: () -> Unit,
    onAddEvidence: () -> Unit,
    onReflect: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("TURN GROWTH\nINTO PROOF.", style = MaterialTheme.typography.displayMedium)
            Text(
                "Projects, decisions and evidence—not decorative badges.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            LumaCard(containerColor = LumaColors.Aqua) {
                Text("ACTIVE ROADMAP", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(LumaColors.Aqua, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                    }
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(roadmap.title, style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "Target: ${roadmap.targetRole}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Text("${roadmap.progressPercent}%", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { roadmap.progressPercent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = LumaColors.Ink,
                    trackColor = Color.White.copy(alpha = 0.7f),
                )
            }
        }
        item {
            SectionHeader("Roadmap evidence", action = "Add", onAction = onAddEvidence)
        }
        roadmap.milestones.forEach { milestone ->
            item(key = milestone.id) {
                LumaCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = if (milestone.status == PlanStatus.COMPLETED) {
                                Icons.Rounded.CheckCircle
                            } else {
                                Icons.Rounded.RadioButtonUnchecked
                            },
                            contentDescription = null,
                            tint = if (milestone.status == PlanStatus.COMPLETED) LumaColors.Aqua else LumaColors.Muted,
                        )
                        Column(Modifier.weight(1f)) {
                            Text(milestone.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                milestone.evidence,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(Icons.Rounded.ArrowForward, contentDescription = "Open milestone")
                    }
                }
            }
        }
        item {
            PrimaryLumaButton(
                text = "Plan the next evidence",
                onClick = onPlanNextEvidence,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            SectionHeader("Whole-life signal")
        }
        item {
            LumaCard(containerColor = LumaColors.Violet.copy(alpha = 0.12f)) {
                Text("Skills are moving. Recovery needs protection.", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    "You finished two project blocks this week. Thursday evening is intentionally open.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LifeAreaPill(LifeArea.SKILLS, compact = true)
                    LifeAreaPill(LifeArea.HEALTH, compact = true)
                    LifeAreaPill(LifeArea.FUN, compact = true)
                }
            }
        }
        item {
            SectionHeader("Reflections")
        }
        if (reflections.isEmpty()) {
            item {
                LumaCard {
                    Text("No reflection yet", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "A 60-second check-in helps Luma adjust tomorrow without judging today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            reflections.take(3).forEach { reflection ->
                item(key = reflection.id) {
                    LumaCard {
                        Text(reflection.date, style = MaterialTheme.typography.labelLarge)
                        Text(reflection.workedWell, style = MaterialTheme.typography.titleMedium)
                        Text(
                            reflection.needsAdjustment,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item {
            PrimaryLumaButton(
                text = "Reflect on today",
                onClick = onReflect,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
