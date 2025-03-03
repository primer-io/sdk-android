package io.primer.components.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.primer.components.Primer
import io.primer.components.models.render

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Default(scope: Primer.Scope.Checkout) {
    val paymentMethods by scope.paymentMethods.collectAsState()
    val selectedMethod by scope.selectedPaymentMethod.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = {},
        sheetState = sheetState
    ) {
        selectedMethod?.let { method ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                IconButton(onClick = {
                    scope.selectPaymentMethod(null)
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }

                method.render()
            }
        } ?: LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(paymentMethods) { method ->
                Button(
                    onClick = { scope.selectPaymentMethod(method) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(method.name)
                }
            }
        }
    }
}
