package io.primer.composable.internal.presentation.checkout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.primer.composable.PrimerCheckout
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionScreen
import io.primer.composable.model.PrimerPaymentMethod

@Composable
internal fun PrimerCheckout.NavigationHost(
    modifier: Modifier = Modifier
) {
    val state by state.collectAsStateWithLifecycle()

    when (val current = state) {
        is PrimerCheckout.State.Error -> ErrorContent(
            message = current.message
        )
        PrimerCheckout.State.Loading -> LoadingContent()
        is PrimerCheckout.State.Ready -> ReadyContent(
            paymentMethods = current.paymentMethods,
            modifier = modifier
        )
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text(
            text = "Loading payment methods...",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun ErrorContent(
    message: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ReadyContent(
    paymentMethods: List<PrimerPaymentMethod>,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.PaymentsList.route,
        modifier = modifier
    ) {
        composable(Screen.PaymentsList.route) {
            PaymentMethodSelectionScreen(
                paymentMethods = paymentMethods
            ) {
                navController.navigate(Screen.CardForm.route)
            }
        }

        composable(Screen.CardForm.route) {
            Text("Card Form Screen")
        }
    }
}

internal sealed class Screen(val route: String) {
    data object PaymentsList : Screen("payments_list")
    data object CardForm : Screen("card_form")
}
