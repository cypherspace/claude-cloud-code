package io.bubblymarble.fitness.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.bubblymarble.fitness.core.designsystem.theme.MonoNumericStyle
import io.bubblymarble.fitness.core.designsystem.theme.Shapes
import io.bubblymarble.fitness.core.designsystem.theme.Spacing

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(top = Spacing.lg, bottom = Spacing.sm),
    )
}

/**
 * Stat card per the design specimens: uppercase muted label (label-medium) above a
 * monospace numeric value (headline-medium). Tonal-elevation surface, no shadow.
 * Optional accent dot ties the card to a feature colour.
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(Shapes.card)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(Spacing.lg),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (accent != null) {
                    Box(
                        Modifier
                            .padding(end = Spacing.sm)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accent),
                    )
                }
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.merge(MonoNumericStyle),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }
    }
}

@Composable
fun KeyValueRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.merge(MonoNumericStyle),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Thin divider that matches outline-variant. Width-respecting, no padding. */
@Composable
fun OldFitDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

/**
 * Tonal surface card. Replaces M3 `Card` in places where dropshadow is wrong
 * for the dark-first palette. `elevation` selects which surfaceContainer tier
 * to use (0 = surface, 1 = container, 2 = high, 3 = highest).
 */
@Composable
fun TonalCard(
    modifier: Modifier = Modifier,
    elevation: Int = 1,
    content: @Composable () -> Unit,
) {
    val bg = when (elevation) {
        0 -> MaterialTheme.colorScheme.surface
        2 -> MaterialTheme.colorScheme.surfaceContainerHigh
        3 -> MaterialTheme.colorScheme.surfaceContainerHighest
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    Box(modifier.clip(Shapes.card).background(bg)) { content() }
}
