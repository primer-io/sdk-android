package io.primer.android.components

import android.content.Context
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.exception.UnsupportedPaymentMethodManagerException
import io.primer.android.components.implementation.core.presentation.PaymentMethodInitializer
import io.primer.android.components.implementation.core.presentation.PaymentMethodStarter
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

interface PaymentMethodManagerDelegate {
    @Throws(
        SdkUninitializedException::class,
        UnsupportedPaymentMethodManagerException::class,
        UnsupportedPaymentMethodException::class
    )
    fun init(paymentMethodType: String, category: PrimerPaymentMethodManagerCategory)

    fun start(
        context: Context,
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
        category: PrimerPaymentMethodManagerCategory,
        onPostStart: () -> Unit = {}
    )
}

internal open class DefaultPaymentMethodManagerDelegate(
    private val paymentMethodInitializer: PaymentMethodInitializer,
    private val paymentMethodStarter: PaymentMethodStarter
) : PaymentMethodManagerDelegate {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun init(paymentMethodType: String, category: PrimerPaymentMethodManagerCategory) {
        scope.launch {
            paymentMethodInitializer.init(paymentMethodType = paymentMethodType, category = category)
        }
    }

    override fun start(
        context: Context,
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
        category: PrimerPaymentMethodManagerCategory,
        onPostStart: () -> Unit
    ) {
        scope.launch {
            paymentMethodStarter.start(
                context = context,
                paymentMethodType = paymentMethodType,
                sessionIntent = sessionIntent,
                category = category,
                onPostStart = onPostStart
            )
        }
    }
}
