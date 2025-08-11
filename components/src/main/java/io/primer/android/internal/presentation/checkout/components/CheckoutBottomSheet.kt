package io.primer.android.internal.presentation.checkout.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme

private const val ANIMATION_DURATION_MS = 300
private const val OVERLAY_ALPHA = 0.5f

@Composable
internal fun CheckoutBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    navHost: @Composable () -> Unit,
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { isVisible = true }

    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(ANIMATION_DURATION_MS),
        label = "slideOffset",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = OVERLAY_ALPHA))
            .clickable { onDismiss() },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset(y = (slideOffset * 1000).dp)
                .clip(
                    RoundedCornerShape(
                        topEnd = LocalPrimerTheme.current.radiusTokens.large,
                        topStart = LocalPrimerTheme.current.radiusTokens.large,
                    ),
                )
                .background(LocalPrimerTheme.current.colorTokens().primerColorBackground),
        ) {
            navHost()
        }
    }
}
