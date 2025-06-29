package io.primer.android.threeds.data.configuration

import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.primer.android.configuration.data.model.Environment
import io.primer.android.configuration.data.model.ThreeDsSecureCertificateDataResponse
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertNotNull

internal class NetceteraThreeDsConfigurationProviderTest {

    private lateinit var configurationProvider: NetceteraThreeDsConfigParametersProvider

    @BeforeEach
    fun setUp() {
        configurationProvider = NetceteraThreeDsConfigParametersProvider()
    }

    @Test
    @Disabled
    fun `createConfiguration should return valid ConfigParameters with API key`() {
        val threeDsKeysParams = ThreeDsKeysParams(
            environment = Environment.PRODUCTION,
            apiKey = "test-api-key",
            threeDsCertificates = null,
        )

        val configuration = configurationProvider.createConfigParameters(threeDsKeysParams)

        assertNotNull(configuration)
    }

    @Test
    @Disabled
    fun `createConfiguration should not configure schemes for production environment`() {
        val threeDsKeysParams = ThreeDsKeysParams(
            environment = Environment.PRODUCTION,
            apiKey = "test-api-key",
            threeDsCertificates = listOf(
                ThreeDsSecureCertificateDataResponse(
                    cardNetwork = "VISA",
                    rootCertificate = "test-root-cert",
                    encryptionKey = "test-encryption-key",
                ),
            ),
        )

        val configuration = configurationProvider.createConfigParameters(threeDsKeysParams)

        assertNotNull(configuration)
        // In production, schemes should not be configured, so this should work without mocking scheme builders
    }

    @ParameterizedTest
    @MethodSource("schemeConfigurations")
    @Disabled
    fun `createConfiguration should use correct scheme configuration for card networks`(
        cardNetwork: String,
        schemeFunction: () -> SchemeConfiguration.Builder,
    ) {
        mockkStatic("com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration")

        val mockSchemeBuilder = mockk<SchemeConfiguration.Builder>(relaxed = true)
        val mockScheme = mockk<SchemeConfiguration>(relaxed = true)

        every { schemeFunction.invoke() } returns mockSchemeBuilder
        every { mockSchemeBuilder.encryptionPublicKey(any()) } returns mockSchemeBuilder
        every { mockSchemeBuilder.rootPublicKey(any()) } returns mockSchemeBuilder
        every { mockSchemeBuilder.build() } returns mockScheme

        val threeDsKeysParams = ThreeDsKeysParams(
            environment = Environment.DEV,
            apiKey = "test-api-key",
            threeDsCertificates = listOf(
                ThreeDsSecureCertificateDataResponse(
                    cardNetwork = cardNetwork,
                    rootCertificate = "test-root-cert",
                    encryptionKey = "test-encryption-key",
                ),
            ),
        )

        configurationProvider.createConfigParameters(threeDsKeysParams)

        verify { schemeFunction.invoke() }
        verify { mockSchemeBuilder.encryptionPublicKey("test-encryption-key") }
        verify { mockSchemeBuilder.rootPublicKey("test-root-cert") }
        verify { mockSchemeBuilder.build() }
    }

    @Test
    @Disabled
    fun `createConfiguration should use test scheme for unknown card network`() {
        mockkStatic("com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration")

        val mockSchemeBuilder = mockk<SchemeConfiguration.Builder>(relaxed = true)
        val mockScheme = mockk<SchemeConfiguration>(relaxed = true)

        every {
            com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration.newSchemeConfiguration(any())
        } returns mockSchemeBuilder
        every { mockSchemeBuilder.logo(any()) } returns mockSchemeBuilder
        every { mockSchemeBuilder.logoDark(any()) } returns mockSchemeBuilder
        every { mockSchemeBuilder.ids(any()) } returns mockSchemeBuilder
        every { mockSchemeBuilder.encryptionPublicKey(any()) } returns mockSchemeBuilder
        every { mockSchemeBuilder.rootPublicKey(any()) } returns mockSchemeBuilder
        every { mockSchemeBuilder.build() } returns mockScheme

        val threeDsKeysParams = ThreeDsKeysParams(
            environment = Environment.DEV,
            apiKey = "test-api-key",
            threeDsCertificates = listOf(
                ThreeDsSecureCertificateDataResponse(
                    cardNetwork = "UNKNOWN_NETWORK",
                    rootCertificate = "test-root-cert",
                    encryptionKey = "test-encryption-key",
                ),
            ),
        )

        configurationProvider.createConfigParameters(threeDsKeysParams)

        verify {
            SchemeConfiguration.newSchemeConfiguration(
                "test_schema",
            )
        }
        verify { mockSchemeBuilder.ids(listOf(NetceteraThreeDsConfigParametersProvider.TEST_SCHEME_ID)) }
    }

    private companion object {
        @JvmStatic
        fun schemeConfigurations() = listOf(
            Arguments.of(
                "VISA",
                SchemeConfiguration::visaSchemeConfiguration,
            ),
            Arguments.of(
                "MASTERCARD",
                SchemeConfiguration::mastercardSchemeConfiguration,
            ),
            Arguments.of(
                "AMEX",
                SchemeConfiguration::amexConfiguration,
            ),
            Arguments.of(
                "DINERS_CLUB",
                SchemeConfiguration::dinersSchemeConfiguration,
            ),
            Arguments.of(
                "UNIONPAY",
                SchemeConfiguration::unionSchemeConfiguration,
            ),
            Arguments.of(
                "JCB",
                SchemeConfiguration::jcbConfiguration,
            ),
            Arguments.of(
                "CARTES_BANCAIRES",
                SchemeConfiguration::cbConfiguration,
            ),
        )
    }
}
