package io.primer.android.threeds.data.configuration

import com.netcetera.threeds.sdk.api.configparameters.ConfigParameters
import com.netcetera.threeds.sdk.api.configparameters.builder.ConfigurationBuilder
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration.amexConfiguration
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration.cbConfiguration
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration.dinersSchemeConfiguration
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration.jcbConfiguration
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration.mastercardSchemeConfiguration
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration.newSchemeConfiguration
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration.unionSchemeConfiguration
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration.visaSchemeConfiguration
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.data.model.Environment
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import io.primer.android.threeds.main.R

/**
 * Provides configuration creation logic for Netcetera 3DS SDK initialization.
 * Handles scheme configuration based on card network types and certificates.
 */
internal class NetceteraThreeDsConfigParametersProvider {

    /**
     * Creates a ConfigParameters object for 3DS initialization.
     *
     * @param threeDsKeysParams The 3DS keys and configuration parameters
     * @return ConfigParameters object ready for SDK initialization
     */
    fun createConfigParameters(threeDsKeysParams: ThreeDsKeysParams): ConfigParameters {
        val configurationBuilder = ConfigurationBuilder()
            .apiKey(threeDsKeysParams.apiKey)

        // Only configure schemes for non-production environments
        if (threeDsKeysParams.environment != Environment.PRODUCTION) {
            threeDsKeysParams.threeDsCertificates?.forEach { certificate ->
                val schemeBuilder = createSchemeBuilder(certificate.cardNetwork)

                configurationBuilder.configureScheme(
                    schemeBuilder
                        .encryptionPublicKey(certificate.encryptionKey)
                        .rootPublicKey(certificate.rootCertificate)
                        .build(),
                )
            }
        }

        return configurationBuilder.build()
    }

    /**
     * Creates appropriate scheme configuration builder based on card network.
     *
     * @param cardNetwork The card network name (e.g., "VISA", "MASTERCARD")
     * @return SchemeConfiguration.Builder for the specified card network
     */
    private fun createSchemeBuilder(cardNetwork: String): SchemeConfiguration.Builder {
        return when (cardNetwork.uppercase()) {
            CardNetwork.Type.MASTERCARD.name -> mastercardSchemeConfiguration()
            CardNetwork.Type.VISA.name -> visaSchemeConfiguration()
            CardNetwork.Type.AMEX.name -> amexConfiguration()
            CardNetwork.Type.DINERS_CLUB.name -> dinersSchemeConfiguration()
            CardNetwork.Type.UNIONPAY.name -> unionSchemeConfiguration()
            CardNetwork.Type.JCB.name -> jcbConfiguration()
            CardNetwork.Type.CARTES_BANCAIRES.name -> cbConfiguration()
            else -> createTestSchemeBuilder()
        }
    }

    /**
     * Creates a test scheme configuration for unknown or test card networks.
     *
     * @return SchemeConfiguration.Builder configured for testing
     */
    private fun createTestSchemeBuilder(): SchemeConfiguration.Builder {
        return newSchemeConfiguration(TEST_SCHEME_NAME)
            .logo(R.drawable.ds_logo_visa.toString())
            .logoDark(R.drawable.ds_logo_visa.toString())
            .ids(listOf(TEST_SCHEME_ID))
    }

    companion object {
        private const val TEST_SCHEME_NAME = "test_scheme"
        const val TEST_SCHEME_ID = "A999999999"
    }
}
