package io.primer.android.components.domain.payments

import io.primer.android.components.domain.core.mapper.PrimerHeadlessUniversalCheckoutPaymentMethodMapper
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.payments.methods.PaymentMethodModulesInteractor
import io.primer.android.domain.session.ConfigurationInteractor
import io.primer.android.domain.session.models.ConfigurationParams
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.logging.Logger
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.model.dto.toPrimerPaymentMethod
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach

internal class PaymentsTypesInteractor(
    private val configurationInteractor: ConfigurationInteractor,
    private val paymentMethodModulesInteractor: PaymentMethodModulesInteractor,
    private val paymentMethodMapper: PrimerHeadlessUniversalCheckoutPaymentMethodMapper,
    private val errorEventResolver: BaseErrorEventResolver,
    private val eventDispatcher: EventDispatcher,
    private val logger: Logger,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseInteractor<Unit, None>() {

    override fun execute(params: None) = configurationInteractor(
        ConfigurationParams(false)
    ).flatMapLatest {
        paymentMethodModulesInteractor.execute(None())
            .mapLatest { it.descriptors.map { it.config.type } }
            .mapLatest {
                it.map { it.takeIfAvailable() }.filterNotNull()
            }
    }
        .flowOn(dispatcher)
        .onEach { paymentMethodType ->
            eventDispatcher.dispatchEvent(
                CheckoutEvent.ConfigurationSuccess(
                    paymentMethodType.map {
                        paymentMethodMapper.getPrimerHeadlessUniversalCheckoutPaymentMethod(
                            it
                        )
                    }
                )
            )
        }
        .catch {
            logger.error(CONFIGURATION_ERROR, it)
            errorEventResolver.resolve(it, ErrorMapperType.HUC)
        }
        .mapLatest { }

    private fun PaymentMethodType.takeIfAvailable() =
        takeIf {
            try {
                it.toPrimerPaymentMethod()
                true
            } catch (ignored: IllegalStateException) {
                false
            }
        }

    private companion object {
        const val CONFIGURATION_ERROR =
            "Failed to initialise due to a configuration missing. Please ensure" +
                "that you have called PrimerHeadlessUniversalCheckout start method" +
                " and you have received onClientSessionSetupSuccessfully callback before" +
                " calling this method."
    }
}
