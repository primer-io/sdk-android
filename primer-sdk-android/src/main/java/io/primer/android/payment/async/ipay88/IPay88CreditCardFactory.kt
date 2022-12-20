package io.primer.android.payment.async.ipay88

import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.models.CountryCode
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.ipay88.helpers.IPay88SdkClassValidator
import io.primer.android.utils.Either
import io.primer.android.utils.Failure
import io.primer.android.utils.Success
import java.util.Currency

internal class IPay88CreditCardFactory(private val type: String, val settings: PrimerSettings) :
    PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        val iPay88 = AsyncPaymentMethod(
            type,
        )

        if (IPay88SdkClassValidator().isIPaySdkIncluded().not()) {
            return Failure(
                IllegalStateException(
                    IPay88SdkClassValidator.I_PAY_CLASS_NOT_LOADED_ERROR
                )
            )
        }

        val amount: Int
        try {
            amount = settings.currentAmount
        } catch (e: IllegalArgumentException) {
            return Failure(Exception(e.message))
        }

        if (amount == 0) {
            return Failure(Exception("Invalid client session value for 'amount' with value '0'"))
        }

        if (settings.order.countryCode != CountryCode.MY) {
            return Failure(
                Exception(
                    "Invalid client session value for 'countryCode' with value" +
                        " '${settings.order.countryCode}'"
                )
            )
        }

        if (
            settings.order.let {
                it.lineItems.joinToString { lineItem -> lineItem.name.orEmpty() }
                    .ifEmpty {
                        it.lineItems.joinToString { lineItem ->
                            lineItem.description.orEmpty()
                        }
                    }
            }.isBlank()
        ) {
            return Failure(
                Exception(
                    """
                     "Invalid client session value for lineItems 'name, description'
                      with value 'null'"   
                    """.trimIndent()
                )
            )
        }

        if (
            settings.customer.let {
                "${it.firstName.orEmpty()} ${it.lastName.orEmpty()}}"
            }.isBlank()
        ) {
            return Failure(
                Exception(
                    "Invalid client session value for 'firstName, lastName' with value 'null'"
                )
            )
        }

        if (settings.customer.emailAddress.isNullOrBlank()) {
            return Failure(
                Exception(
                    "Invalid client session value for 'emailAddress' with value 'null'"
                )
            )
        }

        try {
            val currency = Currency.getInstance(settings.currency)
            if (currency.currencyCode != MYR_CURRENCY_CODE)
                return Failure(
                    Exception(
                        "Invalid client session value for 'currencyCode'" +
                            " with value '${currency.currencyCode}'"
                    )
                )
        } catch (e: IllegalArgumentException) {
            return Failure(Exception(e.message))
        }

        return Success(iPay88)
    }

    private companion object {
        private const val MYR_CURRENCY_CODE = "MYR"
    }
}
