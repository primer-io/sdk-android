package io.primer.android.threeds.data.repository

import io.primer.android.model.Model
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import io.primer.android.threeds.domain.respository.ThreeDsConfigurationRepository
import kotlinx.coroutines.flow.map

internal class ThreeDsConfigurationDataRepository(private val model: Model) :
    ThreeDsConfigurationRepository {

    override fun getConfiguration() =
        model.getClientSession()
            .map {
                ThreeDsKeysParams(
                    it.environment,
                    it.keys?.netceteraLicenseKey,
                    it.keys?.threeDSecureIoCertificates
                )
            }
}
