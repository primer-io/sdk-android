package io.primer.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.primer.android.PrimerCheckout
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.sample.demos.ButtonedInputFieldsDemo
import io.primer.sample.demos.CheckoutDemo
import io.primer.sample.demos.CheckoutDemo.Companion.getBackground
import io.primer.sample.demos.CustomCardComponentDemo
import io.primer.sample.demos.CustomCardFormLayoutDemo
import io.primer.sample.demos.CustomSuccessDemo
import io.primer.sample.demos.DatePickerExpiryDemo
import io.primer.sample.demos.FullscreenDemo
import io.primer.sample.demos.HorizontalPaymentMethodsDemo
import io.primer.sample.demos.PrimerDemo
import io.primer.sample.demos.SingleInputFieldDemo
import io.primer.sample.demos.SubmitOverrideDemo

@OptIn(ExperimentalPrimerApi::class)
@Composable
fun CheckoutComponentsSelection(clientToken: String?) {
    var selectedDemo by remember { mutableStateOf<CheckoutDemo?>(null) }

    when (val current = selectedDemo) {
        null -> DemoSelectionGrid(
            onDemoSelected = { selectedDemo = it }
        )

        else -> {
            clientToken?.let {
                PrimerCheckout(
                    clientToken = it,
                    scope = current.render
                )
            }
        }
    }
}

@Composable
private fun DemoSelectionGrid(
    onDemoSelected: (CheckoutDemo) -> Unit
) {
    val demos = remember { getAllDemos() }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 320.dp)
    ) {
        items(demos) {
            DemoCard(
                demo = it,
                onClick = { onDemoSelected(it) }
            )
        }
    }
}

@Composable
private fun DemoCard(
    demo: CheckoutDemo,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = demo.title)
            CustomizationIndicator(demo = demo)
        }
    }
}

@Composable
private fun CustomizationIndicator(demo: CheckoutDemo) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Customization Level:")
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .size(width = 30.dp, height = 12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (index < demo.customizationLevel) demo.getBackground()
                        else demo.getBackground().copy(alpha = 0.5f)
                    )
            )
        }
    }
}


private fun getAllDemos(): List<CheckoutDemo> = listOf(
    PrimerDemo,
    FullscreenDemo,
    CustomSuccessDemo,
    CustomCardComponentDemo,
    SubmitOverrideDemo,
    HorizontalPaymentMethodsDemo,
    DatePickerExpiryDemo,
    CustomCardFormLayoutDemo,
    SingleInputFieldDemo,
    ButtonedInputFieldsDemo
)
