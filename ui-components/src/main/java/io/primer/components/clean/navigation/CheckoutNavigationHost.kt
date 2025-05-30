package io.primer.components.clean.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.primer.components.PrimerCheckout

@Composable
fun PrimerCheckout.NavigationHost(
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = rememberNavController(),
        startDestination = "",
        modifier = modifier
    ) {

    }
}
