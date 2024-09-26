package io.primer.android.components.domain.payments.paymentMethods.nativeUi.stripe.validation.rules

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.stripe.exception.StripeIllegalValueKey
import io.primer.android.data.base.exceptions.IllegalValueException
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.PrimerStripeOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class ValidStripeMandateDataRuleTest {
    @MockK
    private lateinit var primerSettings: PrimerSettings

    @InjectMockKs
    private lateinit var rule: ValidStripeMandateDataRule

    @AfterEach
    fun tearDown() {
        confirmVerified(primerSettings)
    }

    @Test
    fun `validate() should return success when the input is FullMandateStringData and SDK integration is DROP-IN`() {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.DROP_IN

        val result = rule.validate(
            mockk {
                every { mandateData } returns mockk<PrimerStripeOptions.MandateData.FullMandateStringData>()
            }
        )

        assertEquals(ValidationResult.Success, result)
        verify {
            primerSettings.sdkIntegrationType
        }
    }

    @Test
    fun `validate() should return success when the input is FullMandateData and SDK integration is DROP-IN`() {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.DROP_IN

        val result = rule.validate(
            mockk {
                every { mandateData } returns mockk<PrimerStripeOptions.MandateData.FullMandateData>()
            }
        )

        assertEquals(ValidationResult.Success, result)
        verify {
            primerSettings.sdkIntegrationType
        }
    }

    @Test
    fun `validate() should return success when the input is TemplateMandateData and SDK integration is DROP-IN`() {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.DROP_IN

        val result = rule.validate(
            mockk {
                every { mandateData } returns mockk<PrimerStripeOptions.MandateData.TemplateMandateData>()
            }
        )

        assertEquals(ValidationResult.Success, result)
        verify {
            primerSettings.sdkIntegrationType
        }
    }

    @Test
    fun `validate() should return failure when the input is null and SDK integration is DROP-IN`() {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.DROP_IN

        val result = rule.validate(mockk { every { mandateData } returns null })

        assertEquals(
            ValidationResult.Failure(
                IllegalValueException(
                    key = StripeIllegalValueKey.STRIPE_MANDATE_DATA,
                    message = "Required value for " +
                        "${StripeIllegalValueKey.STRIPE_MANDATE_DATA.key} was null."
                )
            ),
            result
        )
        verify {
            primerSettings.sdkIntegrationType
        }
    }

    @Test
    fun `validate() should return success when the input is FullMandateData and SDK integration is HEADLESS`() {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS

        val result = rule.validate(
            mockk {
                every { mandateData } returns mockk<PrimerStripeOptions.MandateData.FullMandateData>()
            }
        )

        assertEquals(ValidationResult.Success, result)
        verify {
            primerSettings.sdkIntegrationType
        }
    }

    @Test
    fun `validate() should return success when the input is TemplateMandateData and SDK integration is HEADLESS`() {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS

        val result = rule.validate(
            mockk {
                every { mandateData } returns mockk<PrimerStripeOptions.MandateData.TemplateMandateData>()
            }
        )

        assertEquals(ValidationResult.Success, result)
        verify {
            primerSettings.sdkIntegrationType
        }
    }

    @Test
    fun `validate() should return success when the input is null and SDK integration is HEADLESS`() {
        every { primerSettings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS

        val result = rule.validate(mockk { every { mandateData } returns null })

        assertEquals(ValidationResult.Success, result)
        verify {
            primerSettings.sdkIntegrationType
        }
    }
}
