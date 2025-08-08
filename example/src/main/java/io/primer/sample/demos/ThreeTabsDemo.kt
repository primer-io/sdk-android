package io.primer.sample.demos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.primer.android.scope.PrimerCheckoutScope

object ThreeTabsDemo : CheckoutDemo(
    title = "Three Tabs Demo",
    description = "Three tabs: Payment Methods, Card Form, and Country Selection",
    customizationLevel = 5,
    render = {

        components.container = {

            val state by state.collectAsState()

            if (state is PrimerCheckoutScope.State.Ready) {
                var selectedTabIndex by remember { mutableIntStateOf(0) }

                val tabs = listOf("Payment Methods", "Card Form", "Country")

                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        when (selectedTabIndex) {
                            0 -> components.paymentMethodSelection.Screen()
                            1 -> components.cardForm.Screen()
                            2 -> components.cardForm.selectCountry.Screen()
                        }
                    }
                }
            }

        }

    }
)
