package io.primer.android.threeds.domain.respository

import android.app.Activity
import com.netcetera.threeds.sdk.api.transaction.Transaction
import io.primer.android.threeds.data.models.BeginAuthResponse
import io.primer.android.threeds.data.models.CardNetwork
import io.primer.android.threeds.domain.models.ChallengeStatusData
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import io.primer.android.threeds.helpers.ProtocolVersion
import kotlinx.coroutines.flow.Flow
import java.util.Locale

internal interface ThreeDsServiceRepository {

    suspend fun initializeProvider(
        is3DSSanityCheckEnabled: Boolean,
        locale: Locale,
        threeDsKeysParams: ThreeDsKeysParams?,
    ): Flow<Unit>

    fun performProviderAuth(
        cardNetwork: CardNetwork,
        protocolVersion: ProtocolVersion,
    ): Flow<Transaction>

    fun performChallenge(
        activity: Activity,
        transaction: Transaction,
        authResponse: BeginAuthResponse,
        threeDSAppURL: String,
    ): Flow<ChallengeStatusData>

    fun performCleanup()
}
