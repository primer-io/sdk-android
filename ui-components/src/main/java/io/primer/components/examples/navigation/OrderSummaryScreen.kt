package io.primer.components.examples.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OrderSummaryScreen(
    onSelectPayment: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        // Order details...

        Button(
            onClick = onSelectPayment,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Choose Payment Method")
        }
    }
}
