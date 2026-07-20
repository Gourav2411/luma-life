package com.luma.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luma.model.Flexibility
import com.luma.model.LifeArea
import kotlin.math.sin

@Composable
fun LumaCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    showOffsetShadow: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    Box(modifier = modifier) {
        if (showOffsetShadow) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 3.dp, y = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f),
                        shape = shape,
                    ),
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = BorderStroke(1.25.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(17.dp),
                content = content,
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        if (action != null && onAction != null) {
            Text(
                text = action,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clickable(onClick = onAction)
                    .padding(8.dp),
            )
        }
    }
}

@Composable
fun LifeAreaPill(
    area: LifeArea,
    compact: Boolean = false,
) {
    val color = lifeAreaColor(area)
    Surface(
        color = color.copy(alpha = if (compact) 0.2f else 0.92f),
        shape = CircleShape,
        border = BorderStroke(
            width = 1.dp,
            color = if (compact) color.copy(alpha = 0.55f) else MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 8.dp else 11.dp,
                vertical = if (compact) 5.dp else 7.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = lifeAreaIcon(area),
                contentDescription = null,
                modifier = Modifier.size(if (compact) 14.dp else 17.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = area.displayName(),
                style = MaterialTheme.typography.labelLarge,
                fontSize = if (compact) 11.sp else 13.sp,
            )
        }
    }
}

@Composable
fun FlexibilityChip(
    flexibility: Flexibility,
    selected: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val base = when (flexibility) {
        Flexibility.FIXED -> LumaColors.Coral
        Flexibility.FLEXIBLE -> LumaColors.Lime
        Flexibility.PROTECTED -> LumaColors.Violet
    }
    val animated by animateColorAsState(
        targetValue = if (selected) base.copy(alpha = 0.92f) else Color.Transparent,
        label = "flexibility-color",
    )
    Box(
        modifier = Modifier
            .background(animated, CircleShape)
            .border(1.25.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f), CircleShape)
            .then(if (onClick != null) Modifier.clickable(role = Role.Checkbox, onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = flexibility.displayName(),
            style = MaterialTheme.typography.labelLarge,
            fontSize = 11.sp,
        )
    }
}

@Composable
fun PrimaryLumaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(modifier = modifier.height(58.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 4.dp)
                .background(LumaColors.Lime, MaterialTheme.shapes.medium),
        )
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.matchParentSize(),
            colors = ButtonDefaults.buttonColors(
                containerColor = LumaColors.Ink,
                contentColor = Color.White,
                disabledContainerColor = LumaColors.Ink.copy(alpha = 0.35f),
            ),
            border = BorderStroke(1.dp, LumaColors.Ink),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = LumaColors.Lime,
            )
        }
    }
}

@Composable
fun VoiceWaveform(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    color: Color = LumaColors.Lime,
) {
    val transition = rememberInfiniteTransition(label = "voice-wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Restart,
        ),
        label = "voice-wave-phase",
    )
    Canvas(
        modifier = modifier
            .semantics { contentDescription = if (isActive) "Voice input active" else "Voice input idle" },
    ) {
        val center = size.height / 2
        val bars = 24
        val gap = size.width / bars
        repeat(bars) { index ->
            val barPhase = index / bars.toFloat() * Math.PI * 3 + phase
            val activity = if (isActive) 1f else 0.35f
            val amplitude = (size.height * 0.18f + size.height * 0.28f * kotlin.math.abs(sin(barPhase)).toFloat()) * activity
            val x = gap * index + gap / 2
            drawLine(
                color = color,
                start = Offset(x, center - amplitude),
                end = Offset(x, center + amplitude),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
fun Metric(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun RowScope.CapacitySegment(
    color: Color,
    weight: Float,
    label: String,
) {
    Box(
        modifier = Modifier
            .weight(weight.coerceAtLeast(0.05f))
            .height(8.dp)
            .background(color, CircleShape)
            .semantics { contentDescription = label },
    )
}

fun lifeAreaColor(area: LifeArea): Color = when (area) {
    LifeArea.ACADEMICS -> LumaColors.Violet
    LifeArea.SKILLS -> LumaColors.Aqua
    LifeArea.HEALTH -> LumaColors.Lime
    LifeArea.RELATIONSHIPS -> LumaColors.Coral
    LifeArea.FUN -> LumaColors.Cobalt
    LifeArea.SELF_DIRECTION -> Color(0xFFFFC857)
}

fun lifeAreaIcon(area: LifeArea): ImageVector = when (area) {
    LifeArea.ACADEMICS -> Icons.Rounded.MenuBook
    LifeArea.SKILLS -> Icons.Rounded.Bolt
    LifeArea.HEALTH -> Icons.Rounded.FitnessCenter
    LifeArea.RELATIONSHIPS -> Icons.Rounded.People
    LifeArea.FUN -> Icons.Rounded.Gamepad
    LifeArea.SELF_DIRECTION -> Icons.Rounded.Psychology
}

fun LifeArea.displayName(): String = when (this) {
    LifeArea.ACADEMICS -> "Academics"
    LifeArea.SKILLS -> "Skills"
    LifeArea.HEALTH -> "Health"
    LifeArea.RELATIONSHIPS -> "Relationships"
    LifeArea.FUN -> "Fun"
    LifeArea.SELF_DIRECTION -> "Direction"
}

fun Flexibility.displayName(): String = name.lowercase().replaceFirstChar(Char::uppercase)
