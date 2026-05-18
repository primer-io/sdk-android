package io.primer.statetransport.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.core.utils.BaseDataProvider
import io.primer.statetransport.data.datasource.RemoteInstructionsDataSource
import io.primer.statetransport.data.datasource.RemotePayDataSource
import io.primer.statetransport.data.model.ClientSessionInfoDataRequest
import io.primer.statetransport.data.model.ClientSessionMerchantDataRequest
import io.primer.statetransport.data.model.ClientSessionPayDataRequest
import io.primer.statetransport.data.model.toInstructions
import io.primer.statetransport.domain.model.ClientInstructions
import io.primer.statetransport.domain.repository.StateTransportRepository
import java.util.Locale

internal class DefaultStateTransportRepository(
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    private val remoteStartDataSource: RemotePayDataSource,
    private val remoteInstructionsDataSource: RemoteInstructionsDataSource,
    private val applicationIdProvider: BaseDataProvider<String>,
) : StateTransportRepository {

    override suspend fun start(
        clientSessionId: String,
        paymentMethodType: String,
        returnUri: String,
    ): Result<ClientInstructions> = runSuspendCatching {
        val url = "${configurationDataSource.get().pciUrl}/client-session/$clientSessionId:pay"
        val paymentMethodConfig = configurationDataSource.get()
            .paymentMethods.find { it.type == paymentMethodType }
            ?: error("Payment method config not found for type: $paymentMethodType")

        remoteStartDataSource.execute(
            BaseRemoteHostRequest(
                host = url,
                data = ClientSessionPayDataRequest(
                    paymentMethodConfigId = paymentMethodConfig.id,
                    processorMerchantAccountId = paymentMethodConfig.options?.merchantAccountId,
                    paymentMethodType = paymentMethodType,
                    clientInfo = ClientSessionInfoDataRequest(
                        locale = Locale.getDefault().toLanguageTag(),
                        platform = PLATFORM,
                        returnUri = returnUri,
                        merchant = ClientSessionMerchantDataRequest(
                            applicationId = applicationIdProvider.provide(),
                        ),
                    ),
                ),
            ),
        ).body.clientInstruction.toInstructions()
    }

    override suspend fun fetchInstructions(
        clientSessionId: String,
    ): Result<ClientInstructions> = runSuspendCatching {
        val url = "${configurationDataSource.get().pciUrl}/client-session/$clientSessionId" +
            "?expand=clientInstruction"
        remoteInstructionsDataSource.execute(url)
            .body.clientInstruction.toInstructions()
    }

    companion object {
        private const val PLATFORM = "ANDROID"
    }
}
