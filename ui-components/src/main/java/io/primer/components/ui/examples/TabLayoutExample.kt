package io.primer.components.ui.examples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.components.PrimerCheckoutScope

@Composable
fun PrimerCheckoutScope.TabLayoutExample() {

    val state by state.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {

        (state as? PrimerCheckoutScope.State.Ready)?.paymentMethods?.let {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
            ) {
                it.forEachIndexed { index, method ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            selectPaymentMethod(method)
                        },
                        text = { Text(method.name.orEmpty()) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

        }

        Spacer(modifier = Modifier.height(16.dp))

        (state as? PrimerCheckoutScope.State.Selected)?.paymentMethod?.Render()
    }
}
