package io.primer.components.ui.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CardScope.CardComponent(modifier: Modifier = Modifier) {

    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    val state by state.collectAsStateWithLifecycle()

    when(state) {
        CardScope.State.Loading -> {
            // TODO: add loading state
        }
        CardScope.State.Error -> {
            // TODO: add error state
        }
        is CardScope.State.Loaded -> {
            // TODO: add loaded state
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextField(
            value = cardNumber,
            onValueChange = { cardNumber = it },
            label = { Text("Card number") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("1234 1234 1234 1234") },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextField(
                value = expiry,
                onValueChange = { expiry = it },
                label = { Text("Expiry (MM/YY)") },
                modifier = Modifier.weight(1f),
                placeholder = { Text("12/23") },
            )

            TextField(
                value = cvv,
                onValueChange = { cvv = it },
                label = { Text("CVV") },
                modifier = Modifier.weight(1f),
                placeholder = { Text("123") },
            )
        }

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name on card") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Full name") },
        )
    }
}
