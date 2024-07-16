package io.primer.android.data.error

import io.mockk.junit5.MockKExtension
import io.primer.android.components.data.payments.paymentMethods.nativeUi.stripe.ach.error.StripeErrorMapper
import io.primer.android.domain.error.ErrorMapperType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertIs

@ExtendWith(MockKExtension::class)
class DefaultErrorMapperFactoryTest {
    private val instance = DefaultErrorMapperFactory()

    @Test
    fun `buildErrorMapper should return StripeErrorMapper when called with STRIPE`() {
        val mapper = instance.buildErrorMapper(ErrorMapperType.STRIPE)
        assertIs<StripeErrorMapper>(mapper)
    }
}
