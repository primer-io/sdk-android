package io.primer.sample.demos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.scope.PrimerKlarnaScope

object CustomKlarnaDemo : CheckoutDemo(
    title = "Custom Klarna (Segmented)",
    description = "Material 3 segmented buttons for category selection",
    customizationLevel = 4,
    render = {
        components.klarna.screen = {
            CustomKlarnaScreen()
        }
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PrimerKlarnaScope.CustomKlarnaScreen() {
    val state by state.collectAsStateWithLifecycle()
    val showFab = state.step == PrimerKlarnaScope.Step.AwaitingFinalization

    Box {
        Column(
            modifier = Modifier.padding(bottom = if (showFab) 80.dp else 0.dp)
        ) {
            if (state.categories.isNotEmpty()) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    state.categories.forEachIndexed { index, category ->
                        SegmentedButton(
                            selected = category.id == state.selectedCategoryId,
                            onClick = { selectPaymentCategory(category.id) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = state.categories.size
                            ),
                            label = {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }
            }

            when (state.step) {
                PrimerKlarnaScope.Step.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        with(components) {
                            LoadingIndicator()
                        }
                    }
                }

                PrimerKlarnaScope.Step.CategorySelection -> {
                    if (state.categories.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            with(components) {
                                LoadingIndicator()
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Select a payment category above",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                PrimerKlarnaScope.Step.ViewReady -> {
                    with(components) {
                        PaymentViewContainer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(16.dp)
                        )
                    }
                }

                PrimerKlarnaScope.Step.AwaitingFinalization -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ready to finalize",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (showFab) {
            FloatingActionButton(
                onClick = { finalizePayment() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text("Finalize")
            }
        }
    }
}

