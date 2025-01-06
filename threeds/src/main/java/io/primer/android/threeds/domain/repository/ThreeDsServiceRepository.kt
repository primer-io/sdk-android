package io.primer.android.threeds.domain.repository

import android.app.Activity
import com.netcetera.threeds.sdk.api.transaction.Transaction
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.data.model.Environment
import io.primer.android.threeds.data.exception.ThreeDsConfigurationException
import io.primer.android.threeds.data.exception.ThreeDsMissingDirectoryServerException
import io.primer.android.threeds.data.models.auth.BeginAuthResponse
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import io.primer.android.threeds.helpers.ProtocolVersion
import kotlinx.coroutines.flow.Flow
import java.util.Locale

internal interface ThreeDsServiceRepository {
    val threeDsSdkVersion: String?

    @Throws(ThreeDsConfigurationException::class)
    suspend fun initializeProvider(
        is3DSSanityCheckEnabled: Boolean,
        locale: Locale,
        threeDsKeysParams: ThreeDsKeysParams?,
    ): Result<Unit>

    @Throws(ThreeDsMissingDirectoryServerException::class)
    suspend fun performProviderAuth(
        cardNetwork: CardNetwork.Type,
        protocolVersion: ProtocolVersion,
        environment: Environment,
    ): Result<Transaction>

    fun performChallenge(
        activity: Activity,
        transaction: Transaction,
        authResponse: BeginAuthResponse,
        threeDSAppURL: String?,
        initProtocolVersion: String,
    ): Flow<ChallengeStatusData>

    fun performCleanup()
}
