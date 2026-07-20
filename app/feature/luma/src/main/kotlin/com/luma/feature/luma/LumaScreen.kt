package com.luma.feature.luma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.luma.ai.AssistantTurnResponse
import com.luma.designsystem.LifeAreaPill
import com.luma.designsystem.LumaCard
import com.luma.designsystem.LumaColors
import com.luma.designsystem.PrimaryLumaButton
import com.luma.designsystem.VoiceWaveform
import com.luma.model.ChatMessage
import com.luma.model.PlanHorizon

@Composable
fun LumaScreen(
    messages: List<ChatMessage>,
    input: String,
    isListening: Boolean,
    isThinking: Boolean,
    lastResponse: AssistantTurnResponse?,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onToggleVoice: () -> Unit,
    onReviewProposal: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("ASK\nLUMA.", style = MaterialTheme.typography.displayMedium)
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Rounded.Settings, contentDescription = "Open settings")
                }
            }
            Text(
                "Messy context in. A clear, realistic proposal out.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item {
            VoicePanel(isListening, onToggleVoice)
        }

        items(messages, key = ChatMessage::id) { message ->
            ChatBubble(message)
        }

        lastResponse?.extractedContext?.takeIf { it.isNotEmpty() }?.let { contexts ->
            item {
                LumaCard {
                    Text("WHAT I UNDERSTOOD", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        contexts.forEach { context ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                context.lifeArea?.let {
                                    LifeAreaPill(it, compact = true)
                                    Spacer(Modifier.size(8.dp))
                                }
                                Column {
                                    Text(context.label, style = MaterialTheme.typography.labelLarge)
                                    Text(
                                        context.value,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isThinking) {
            item {
                LumaCard(containerColor = LumaColors.Lime.copy(alpha = 0.16f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = LumaColors.Ink,
                            strokeWidth = 3.dp,
                        )
                        Text(
                            "Checking fixed plans, buffers and free time…",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }

        lastResponse?.proposal?.let { proposal ->
            item {
                LumaCard(
                    containerColor = if (proposal.isFeasible) LumaColors.Lime else LumaColors.Coral,
                    onClick = onReviewProposal,
                ) {
                    Text("READY FOR REVIEW", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (proposal.isFeasible) "NO OVERLAPS.\nNO SURPRISES." else "A TRADE-OFF\nNEEDS YOU.",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        proposal.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${proposal.changes.size} proposed changes",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Icon(Icons.Rounded.ArrowForward, contentDescription = "Review plan")
                    }
                }
            }
            item {
                PrimaryLumaButton(
                    text = "Review Luma’s plan",
                    onClick = onReviewProposal,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = proposal.changes.isNotEmpty(),
                )
            }
        }

        item {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tell Luma what changed…") },
                minLines = 2,
                maxLines = 4,
                shape = MaterialTheme.shapes.medium,
                leadingIcon = {
                    Icon(Icons.Rounded.Keyboard, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = onSend, enabled = input.isNotBlank() && !isThinking) {
                        Icon(Icons.Rounded.Send, contentDescription = "Send")
                    }
                },
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    "Audio is not stored · Offline fallback is visible",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun VoicePanel(
    isListening: Boolean,
    onToggleVoice: () -> Unit,
) {
    LumaCard(
        containerColor = Color.White,
        onClick = onToggleVoice,
    ) {
        Text(
            text = if (isListening) "LISTENING LIVE" else "VOICE MODE",
            style = MaterialTheme.typography.labelMedium,
            color = LumaColors.Cobalt,
        )
        VoiceWaveform(
            isActive = isListening,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            color = LumaColors.Cobalt,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(LumaColors.Lime, LumaColors.Aqua, LumaColors.Cobalt),
                        ),
                        shape = CircleShape,
                    )
                    .border(2.dp, LumaColors.Ink, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Rounded.Pause else Icons.Rounded.Mic,
                    contentDescription = if (isListening) "Pause voice input" else "Start voice input",
                    tint = LumaColors.Ink,
                    modifier = Modifier.size(40.dp),
                )
            }
            Spacer(Modifier.size(12.dp))
            Column {
                Text(
                    if (isListening) "I’M WITH YOU." else "TAP. TALK.",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    if (isListening) "Speak naturally. Pause anytime." else "Voice or text—your choice.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
    ) {
        LumaCard(
            modifier = Modifier.fillMaxWidth(0.86f),
            containerColor = if (message.isUser) LumaColors.Cobalt.copy(alpha = 0.14f)
            else MaterialTheme.colorScheme.surface,
            showOffsetShadow = false,
        ) {
            Text(message.text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
