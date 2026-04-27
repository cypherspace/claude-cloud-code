package io.bubblymarble.fitness.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** 4 dp grid. Use these instead of raw `.dp` values for spacing/padding. */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val screenPadding = 16.dp
}

/** Material 3 shape scale. Cards 12dp; buttons stadium. */
object Radii {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 28.dp
}

/** Convenience shapes for common surfaces. */
object Shapes {
    val card = RoundedCornerShape(Radii.md)
    val sheet = RoundedCornerShape(Radii.lg)
    val pill = RoundedCornerShape(Radii.xl)
    val input = RoundedCornerShape(Radii.xs)
}
