package io.primer.android.domain.payments.displayMetadata

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.repository.AnalyticsRepository
import io.primer.android.data.payments.displayMetadata.model.IconDisplayMetadata
import io.primer.android.domain.base.None
import io.primer.android.domain.payments.displayMetadata.models.PaymentMethodImplementation
import io.primer.android.domain.payments.displayMetadata.repository.PaymentMethodImplementationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.random.Random

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class PaymentMethodsImplementationInteractorTest {

    @RelaxedMockK
    internal lateinit var implementationRepository: PaymentMethodImplementationRepository

    @RelaxedMockK
    internal lateinit var analyticsRepository: AnalyticsRepository

    private lateinit var interactor: PaymentMethodsImplementationInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor = PaymentMethodsImplementationInteractor(
            implementationRepository,
            analyticsRepository
        )
    }

    @Test
    fun `execute() should filter payment methods when icon metadata is empty`() {
        val paymentMethodImplementation = mockk<PaymentMethodImplementation>(relaxed = true)
        val buttonMetadata = mockk<PaymentMethodImplementation.ButtonMetadata>(relaxed = true)

        every { paymentMethodImplementation.buttonMetadata }.returns(buttonMetadata)
        every { buttonMetadata.iconDisplayMetadata }.returns(listOf())
        every { implementationRepository.getPaymentMethodsImplementation() }.returns(
            listOf(
                paymentMethodImplementation
            )
        )
        runTest {
            val filteredPaymentMethodsImplementation = interactor(None())
            assert(filteredPaymentMethodsImplementation.isEmpty())
        }

        verify { implementationRepository.getPaymentMethodsImplementation() }
    }

    @Test
    fun `execute() should filter payment methods when icon metadata is invalid`() {
        val paymentMethodImplementation = mockk<PaymentMethodImplementation>(relaxed = true)
        val buttonMetadata = mockk<PaymentMethodImplementation.ButtonMetadata>(relaxed = true)
        val iconDisplayMetadata = mockk<IconDisplayMetadata>(relaxed = true)

        every { paymentMethodImplementation.buttonMetadata }.returns(buttonMetadata)
        every { buttonMetadata.iconDisplayMetadata }.returns(listOf(iconDisplayMetadata))
        every { implementationRepository.getPaymentMethodsImplementation() }.returns(
            listOf(
                paymentMethodImplementation
            )
        )
        runTest {
            val filteredPaymentMethodsImplementation = interactor(None())
            assert(filteredPaymentMethodsImplementation.isEmpty())
        }

        verify { implementationRepository.getPaymentMethodsImplementation() }
    }

    @Test
    fun `execute() should not filter payment methods when icon metadata iconResId is valid`() {
        val paymentMethodImplementation = mockk<PaymentMethodImplementation>(relaxed = true)
        val buttonMetadata = mockk<PaymentMethodImplementation.ButtonMetadata>(relaxed = true)
        val iconDisplayMetadata = mockk<IconDisplayMetadata>(relaxed = true)

        every { paymentMethodImplementation.buttonMetadata }.returns(buttonMetadata)
        every { buttonMetadata.iconDisplayMetadata }.returns(listOf(iconDisplayMetadata))
        every { iconDisplayMetadata.iconResId }.returns(Random.nextInt(100))
        every { implementationRepository.getPaymentMethodsImplementation() }.returns(
            listOf(
                paymentMethodImplementation
            )
        )
        runTest {
            val filteredPaymentMethodsImplementation = interactor(None())
            assertEquals(
                implementationRepository.getPaymentMethodsImplementation(),
                filteredPaymentMethodsImplementation
            )
        }

        verify { implementationRepository.getPaymentMethodsImplementation() }
    }

    @Test
    fun `execute() should not filter payment methods when icon metadata filePath is valid`() {
        val paymentMethodImplementation = mockk<PaymentMethodImplementation>(relaxed = true)
        val buttonMetadata = mockk<PaymentMethodImplementation.ButtonMetadata>(relaxed = true)
        val iconDisplayMetadata = mockk<IconDisplayMetadata>(relaxed = true)

        every { paymentMethodImplementation.buttonMetadata }.returns(buttonMetadata)
        every { buttonMetadata.iconDisplayMetadata }.returns(listOf(iconDisplayMetadata))
        every { iconDisplayMetadata.filePath }.returns("/images/")
        every { implementationRepository.getPaymentMethodsImplementation() }.returns(
            listOf(
                paymentMethodImplementation
            )
        )
        runTest {
            val filteredPaymentMethodsImplementation = interactor(None())
            assertEquals(
                implementationRepository.getPaymentMethodsImplementation(),
                filteredPaymentMethodsImplementation
            )
        }

        verify { implementationRepository.getPaymentMethodsImplementation() }
    }

    @Test
    fun `execute() should call analyticsRepository addEvent payment methods when icon metadata is invalid`() {
        val paymentMethodImplementation = mockk<PaymentMethodImplementation>(relaxed = true)
        val buttonMetadata = mockk<PaymentMethodImplementation.ButtonMetadata>(relaxed = true)
        val iconDisplayMetadata = mockk<IconDisplayMetadata>(relaxed = true)

        every { paymentMethodImplementation.buttonMetadata }.returns(buttonMetadata)
        every { buttonMetadata.iconDisplayMetadata }.returns(listOf(iconDisplayMetadata))
        every { implementationRepository.getPaymentMethodsImplementation() }.returns(
            listOf(
                paymentMethodImplementation
            )
        )

        val messageAnalyticsParamsSlot = slot<MessageAnalyticsParams>()
        runTest {
            interactor(None())
            verify { analyticsRepository.addEvent(capture(messageAnalyticsParamsSlot)) }
            assertEquals(
                MessageType.PM_IMAGE_LOADING_FAILED,
                messageAnalyticsParamsSlot.captured.messageType
            )
        }

        verify { implementationRepository.getPaymentMethodsImplementation() }
    }
}
