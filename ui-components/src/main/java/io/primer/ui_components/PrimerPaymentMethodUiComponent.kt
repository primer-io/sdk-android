package io.primer.ui_components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.klarna.PrimerHeadlessUniversalCheckoutKlarnaManager
import io.primer.android.klarna.api.component.KlarnaComponent
import io.primer.android.klarna.api.composable.KlarnaPaymentCollectableData
import io.primer.android.klarna.api.composable.KlarnaPaymentStep
import io.primer.android.klarna.api.ui.PrimerKlarnaPaymentView
import io.primer.android.klarna.implementation.session.domain.models.KlarnaPaymentCategory
import kotlinx.coroutines.cancelChildren

fun PrimerHeadlessUniversalCheckoutPaymentMethod.hasCustomUi() = this.paymentMethodManagerCategories.any {
    it != PrimerPaymentMethodManagerCategory.NATIVE_UI
}

@Composable
fun PrimerPaymentMethodDynamicComponent(
    modifier: Modifier = Modifier,
    component: KlarnaComponent
) {

    val steps by component.componentStep.collectAsStateWithLifecycle(null)
    val context = LocalContext.current

    var paymentCategories by remember { mutableStateOf<List<KlarnaPaymentCategory>>(emptyList()) }

    LaunchedEffect("") {
        component.start()
    }

    component.addCloseable {
        println("semirz")
    }

    Column {
        when (val step = steps) {
            is KlarnaPaymentStep.PaymentSessionAuthorized -> Unit

            is KlarnaPaymentStep.PaymentSessionCreated -> {
                RadioGroupExample(
                    options = step.paymentCategories.map { it.name },
                    modifier = modifier
                ) { selected ->
                    component.updateCollectedData(
                        KlarnaPaymentCollectableData.PaymentOptions(
                            context = context,
                            paymentCategory = step.paymentCategories.first { it.name == selected },
                            returnIntentUrl = "primer://io.primer"
                        )
                    )
                }.also {
                    paymentCategories = step.paymentCategories
                }
            }

            KlarnaPaymentStep.PaymentSessionFinalized -> Unit
            is KlarnaPaymentStep.PaymentViewLoaded -> {
                RadioGroupExample(options = paymentCategories.map { it.name }, modifier = modifier) { selected ->
                    component.updateCollectedData(
                        KlarnaPaymentCollectableData.PaymentOptions(
                            context = context,
                            paymentCategory = paymentCategories.first { it.name == selected },
                            returnIntentUrl = "primer://io.primer"
                        )
                    )
                }
                CustomViewInComposable(step.paymentView)
            }

            null -> Unit
        }
    }
}

@Composable
fun CustomViewInComposable(view: PrimerKlarnaPaymentView) {
    AndroidView(
        factory = { _ ->
            // Create and configure your regular View
            view
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
fun RadioGroupExample(options: List<String>, modifier: Modifier, onOptionSelected: (String) -> Unit) {
    // State to keep track of the selected option
    var selectedOption by remember { mutableStateOf(options.firstOrNull()) }

    Column(modifier = modifier.border(border = BorderStroke(2.dp, Color.Blue), shape = CutCornerShape(8.dp))) {
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (option == selectedOption),
                        onClick = {
                            selectedOption = option
                            onOptionSelected(option)
                        }
                    )
                    .padding(8.dp)
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = {
                        selectedOption = option
                        onOptionSelected(option)
                    }
                )
                Text(
                    text = option,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
