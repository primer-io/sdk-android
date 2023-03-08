package io.primer.android.threeds.domain.respository

import android.app.Activity
import com.netcetera.threeds.sdk.api.transaction.Transaction
import io.primer.android.threeds.data.exception.ThreeDsConfigurationException
import io.primer.android.data.configuration.models.Environment
import io.primer.android.threeds.data.exception.ThreeDsMissingDirectoryServerException
import io.primer.android.threeds.data.models.BeginAuthResponse
import io.primer.android.threeds.data.models.CardNetwork
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import io.primer.android.threeds.helpers.ProtocolVersion
import kotlinx.coroutines.flow.Flow
import java.util.Locale

internal interface ThreeDsServiceRepository {

    @Throws(ThreeDsConfigurationException::class)
    suspend fun initializeProvider(
        is3DSSanityCheckEnabled: Boolean,
        locale: Locale,
        threeDsKeysParams: ThreeDsKeysParams?,
    ): Flow<Unit>

    @Throws(ThreeDsMissingDirectoryServerException::class)
    fun performProviderAuth(
        cardNetwork: CardNetwork,
        protocolVersion: ProtocolVersion,
        environment: Environment
    ): Flow<Transaction>

    fun performChallenge(
        activity: Activity,
        transaction: Transaction,
        authResponse: BeginAuthResponse,
        threeDSAppURL: String,
    ): Flow<ChallengeStatusData>

    fun performCleanup()
}
