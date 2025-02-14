package io.primer.components.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.klarna.api.component.KlarnaComponent
import io.primer.android.klarna.api.composable.KlarnaPaymentCollectableData
import io.primer.android.klarna.api.composable.KlarnaPaymentStep
import io.primer.android.klarna.implementation.session.domain.models.KlarnaPaymentCategory

@Composable
fun PrimerDynamicComponent(
    modifier: Modifier = Modifier,
    component: KlarnaComponent,
) {
    val steps by component.componentStep.collectAsStateWithLifecycle(null)
    val context = LocalContext.current

    var paymentCategories by remember { mutableStateOf<List<KlarnaPaymentCategory>>(emptyList()) }

    LaunchedEffect("") {
        component.start()
    }

    Column {
        when (val step = steps) {
            is KlarnaPaymentStep.PaymentSessionAuthorized -> Unit

            is KlarnaPaymentStep.PaymentSessionCreated -> {
                PrimerRadioGroupComponent(
                    options = step.paymentCategories.map { it.name },
                    modifier = modifier,
                ) { selected ->
                    component.updateCollectedData(
                        KlarnaPaymentCollectableData.PaymentOptions(
                            context = context,
                            paymentCategory = step.paymentCategories.first { it.name == selected },
                            returnIntentUrl = "primer://io.primer",
                        ),
                    )
                }.also {
                    paymentCategories = step.paymentCategories
                }
            }

            KlarnaPaymentStep.PaymentSessionFinalized -> Unit
            is KlarnaPaymentStep.PaymentViewLoaded -> {
                PrimerRadioGroupComponent(
                    options = paymentCategories.map { it.name },
                    modifier = modifier,
                ) { selected ->
                    component.updateCollectedData(
                        KlarnaPaymentCollectableData.PaymentOptions(
                            context = context,
                            paymentCategory = paymentCategories.first { it.name == selected },
                            returnIntentUrl = "primer://io.primer",
                        ),
                    )
                }
                PrimerAndroidViewComponent(step.paymentView)
            }

            null -> Unit
        }
    }
}
