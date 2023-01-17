package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalCheckoutConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository.PaypalCheckoutConfigurationRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.extensions.doOnError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

internal class PaypalCheckoutConfigurationInteractor(
    private val paypalConfigurationRepository: PaypalCheckoutConfigurationRepository,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : BaseFlowInteractor<PaypalCheckoutConfiguration, None>() {
    override fun execute(params: None): Flow<PaypalCheckoutConfiguration> {
        return paypalConfigurationRepository.getPaypalConfiguration()
            .doOnError { baseErrorEventResolver.resolve(it, ErrorMapperType.DEFAULT) }
            .flowOn(dispatcher)
    }
}
