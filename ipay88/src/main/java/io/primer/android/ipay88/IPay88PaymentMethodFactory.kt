package io.primer.android.ipay88

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.utils.Either
import io.primer.android.core.utils.Failure
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodFactory
import io.primer.android.core.utils.Success
import io.primer.android.ipay88.implementation.helpers.IPay88SdkClassValidator

class IPay88PaymentMethodFactory(
    private val type: String,
    val configurationDataSource: CacheConfigurationDataSource
) : PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        val iPay88 = IPay88PaymentMethod(type)

        if (IPay88SdkClassValidator().isIPaySdkIncluded().not()) {
            return Failure(
                IllegalStateException(
                    IPay88SdkClassValidator.I_PAY_CLASS_NOT_LOADED_ERROR
                        .format(
                            type,
                            configurationDataSource.get().clientSession.order?.countryCode?.name?.lowercase(),
                            configurationDataSource.get().clientSession.order?.countryCode?.name?.lowercase(),
                            type
                        )
                )
            )
        }
        return Success(iPay88)
    }
}
