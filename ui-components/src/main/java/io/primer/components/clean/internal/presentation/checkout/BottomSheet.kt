package io.primer.components.clean.internal.presentation.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.components.Primer
import io.primer.components.clean.internal.di.ComposableManager
import io.primer.components.clean.ui.PrimerPaymentMethodItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Primer.BottomSheet(
    modifier: Modifier = Modifier,
) {

    val state by state.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = { ComposableManager.cleanup() },
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            when (val current = state) {
                Primer.State.Loading -> {
                    // Loading State
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

                is Primer.State.Error -> {
                    // Error State
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = current.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                is Primer.State.Ready -> {
                    Text(
                        text = "Select Payment Method",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    // Payment Methods List
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(current.paymentMethods) { paymentMethod ->
                            PrimerPaymentMethodItem(
                                paymentMethod = paymentMethod,
                                onSelect = {
                                    selectPaymentMethod(paymentMethod)
                                }
                            )
                        }
                    }
                }

                is Primer.State.Selected -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Selected: ${current.paymentMethod}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
