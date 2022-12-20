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

    @Suppress("LongMethod")
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

        val errors = mutableListOf<String>()

        var amount = 0
        try {
            amount = settings.currentAmount
        } catch (e: IllegalArgumentException) {
            errors.add(e.message.toString())
        }

        if (amount == 0) {
            errors.add(
                """
                Invalid client session value for 'amount' with value '0' | 
                Check if you have provided a valid value for 'amount' in your client session.
                """.trimIndent()
            )
        }

        try {
            val currency = Currency.getInstance(settings.currency)
            if (currency.currencyCode != MYR_CURRENCY_CODE) {
                errors.add(
                    """
                    Invalid client session value for 'currency' with value '${settings.currency}' | 
                    Allowed values are [MYR].
                    """.trimIndent()
                )
            }
        } catch (e: IllegalArgumentException) {
            errors.add(e.message.toString())
        }

        if (settings.order.countryCode != CountryCode.MY) {
            errors.add(
                """
                Invalid client session value for 'countryCode' with value
                 '${settings.order.countryCode}' |  Allowed values are [MY].
                """.trimIndent()
            )
        }

        val lineItemsNamesJoined = settings.order.let {
            it.lineItems.map { lineItem -> lineItem.name }
        }

        val lineItemsDescriptionJoined = settings.order.let {
            it.lineItems.map { lineItem -> lineItem.description }
        }

        if (
            lineItemsNamesJoined.all { it.isNullOrBlank() } &&
            lineItemsDescriptionJoined.all { it.isNullOrBlank() }
        ) {
            errors.add(
                """
                Invalid client session value for 'order.lineItems.name' or 
                'order.lineItems.description' with value 'null' | 
                Check if you have provided a valid value for 'order.lineItems.name' or 
                'order.lineItems.description' in your client session."
                """.trimIndent()
            )
        }

        if (settings.customer.firstName.isNullOrBlank()) {
            errors.add(
                """
                Invalid client session value for 'customer.firstName' with value 
                '${settings.customer.firstName}' | 
                Check if you have provided a valid value for 'customer.firstName' in your client session."
                """.trimIndent()
            )
        }

        if (settings.customer.lastName.isNullOrBlank()) {
            errors.add(
                """
                Invalid client session value for 'customer.lastName' with value 
                '${settings.customer.lastName}' | 
                Check if you have provided a valid value for 'customer.lastName' in your client session."
                """.trimIndent()
            )
        }

        if (settings.customer.emailAddress.isNullOrBlank()) {
            errors.add(
                """
                Invalid client session value for 'customer.emailAddress' with value 
                '${settings.customer.emailAddress}' | 
                Check if you have provided a valid value for 'customer.emailAddress' in your client session."
                """.trimIndent()
            )
        }

        if (errors.isNotEmpty()) return Failure(Exception(errors.joinToString("\n")))
        return Success(iPay88)
    }

    private companion object {
        private const val MYR_CURRENCY_CODE = "MYR"
    }
}
