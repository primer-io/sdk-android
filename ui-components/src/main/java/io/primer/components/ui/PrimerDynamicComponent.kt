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
import io.primer.android.paymentmethods.manager.component.PrimerHeadlessCollectDataComponent

/**
 * A composable that dynamically renders UI for [PrimerHeadlessCollectDataComponent] implementations.
 *
 * @param modifier The [Modifier] to be applied to the component.
 * @param component The [PrimerHeadlessCollectDataComponent] managing the data collection flow.
 */
// TODO TWS: create internal implementation for all relevant payment methods
@Composable
fun PrimerDynamicComponent(
    modifier: Modifier = Modifier,
    component: KlarnaComponent,
) {
    val steps by component.componentStep.collectAsStateWithLifecycle(null)
    val context = LocalContext.current

    var paymentCategories by remember { mutableStateOf<List<KlarnaPaymentCategory>>(emptyList()) }

    LaunchedEffect("") { // TODO TWS: we likely want to restart every time the component instance changes
        component.start()
    }

    Column {
        when (val step = steps) {
            is KlarnaPaymentStep.PaymentSessionAuthorized -> Unit

            is KlarnaPaymentStep.PaymentSessionCreated -> {
                PrimerRadioGroupComponent(
                    modifier = modifier,
                    options = step.paymentCategories.map { it.name },
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
                    modifier = modifier,
                    options = paymentCategories.map { it.name },
                ) { selected ->
                    component.updateCollectedData(
                        KlarnaPaymentCollectableData.PaymentOptions(
                            context = context,
                            paymentCategory = paymentCategories.first { it.name == selected },
                            returnIntentUrl = "primer://io.primer",
                        ),
                    )
                }
                PrimerAndroidViewComponent(view = step.paymentView)
            }

            null -> Unit
        }
    }
}
