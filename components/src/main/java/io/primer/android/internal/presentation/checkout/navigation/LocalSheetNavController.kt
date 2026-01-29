package io.primer.android.internal.presentation.checkout.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

internal val LocalSheetNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController not provided.")
}
