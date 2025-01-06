import android.nfc.Tag
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.nolpay.api.manager.core.composable.NolPayCollectableData
import io.primer.android.nolpay.api.manager.linkCard.composable.NolPayLinkCollectableData
import io.primer.android.nolpay.implementation.linkCard.domain.validation.NolPayLinkDataValidatorRegistry
import io.primer.android.nolpay.implementation.validation.validator.NolPayLinkMobileNumberDataValidator
import io.primer.android.nolpay.implementation.validation.validator.NolPayLinkOtpDataValidator
import io.primer.android.nolpay.implementation.validation.validator.NolPayLinkTagDataValidator
import io.primer.android.phoneMetadata.domain.repository.PhoneMetadataRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class NolPayLinkDataValidatorRegistryTest {
    private lateinit var registry: NolPayLinkDataValidatorRegistry

    @BeforeEach
    fun setUp() {
        DISdkContext.headlessSdkContainer =
            mockk<SdkContainer>(relaxed = true).also { sdkContainer ->
                val cont =
                    spyk<DependencyContainer>().also { container ->
                        container.registerFactory<PhoneMetadataRepository> { mockk(relaxed = true) }
                    }

                every { sdkContainer.containers }.returns(mutableMapOf(cont::class.simpleName.orEmpty() to cont))
            }
        registry = spyk(NolPayLinkDataValidatorRegistry())
    }

    @Test
    fun `getValidator should return NolPayLinkOtpDataValidator for NolPayOtpData`() {
        // Given
        val data = NolPayLinkCollectableData.NolPayOtpData("123456")

        // When
        val validator = registry.getValidator(data)

        // Then
        assertEquals(NolPayLinkOtpDataValidator::class, validator::class)
    }

    @Test
    fun `getValidator should return NolPayLinkMobileNumberDataValidator for NolPayPhoneData`() {
        // Given
        val data = NolPayLinkCollectableData.NolPayPhoneData("1234567890")

        // When
        val validator = registry.getValidator(data)

        // Then
        assertEquals(NolPayLinkMobileNumberDataValidator::class, validator::class)
    }

    @Test
    fun `getValidator should return NolPayLinkTagDataValidator for NolPayTagData`() {
        // Given
        val data = NolPayLinkCollectableData.NolPayTagData(mockk<Tag>())

        // When
        val validator = registry.getValidator(data)

        // Then
        assertEquals(NolPayLinkTagDataValidator::class, validator::class)
    }

    @Test
    fun `getValidator should throw IllegalArgumentException for unsupported data`() {
        // Given
        val unsupportedData = mockk<NolPayCollectableData>()

        // When/Then
        assertThrows(IllegalArgumentException::class.java) {
            registry.getValidator(unsupportedData)
        }
    }
}
