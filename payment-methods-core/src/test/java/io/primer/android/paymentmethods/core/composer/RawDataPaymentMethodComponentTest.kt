package io.primer.android.paymentmethods.core.composer

import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodBinData
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadataState
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.paymentmethods.manager.composable.PrimerCollectableData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class RawDataPaymentMethodComponentTest {

    private val binDataSharedFlow = MutableSharedFlow<PrimerPaymentMethodBinData>()

    private val binDataComponent = object : RawDataPaymentMethodComponent<PrimerCollectableData>() {
        override val binDataFlow: Flow<PrimerPaymentMethodBinData> = binDataSharedFlow
        override val componentInputValidations: Flow<List<PrimerInputValidationError>> = emptyFlow()
        override fun updateCollectedData(collectedData: PrimerCollectableData) = Unit
        override val metadataStateFlow: Flow<PrimerPaymentMethodMetadataState> = emptyFlow()
        override val metadataFlow: Flow<PrimerPaymentMethodMetadata> = emptyFlow()
        override fun submit() = Unit
        override fun start(paymentMethodType: String, sessionIntent: PrimerSessionIntent) = Unit
        override fun getSdkContainer() = throw UnsupportedOperationException()
    }

    @Test
    fun `binDataFlow should emit values when overridden`() = runTest {
        val binData = mockk<PrimerPaymentMethodBinData>()
        val collected = mutableListOf<PrimerPaymentMethodBinData>()

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            binDataComponent.binDataFlow.toList(collected)
        }
        binDataSharedFlow.emit(binData)
        job.cancel()

        assertEquals(listOf(binData), collected)
    }

    @Test
    fun `binDataFlow should emit multiple values in order when overridden`() = runTest {
        val binData1 = mockk<PrimerPaymentMethodBinData>()
        val binData2 = mockk<PrimerPaymentMethodBinData>()
        val collected = mutableListOf<PrimerPaymentMethodBinData>()

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            binDataComponent.binDataFlow.toList(collected)
        }
        binDataSharedFlow.emit(binData1)
        binDataSharedFlow.emit(binData2)
        job.cancel()

        assertEquals(listOf(binData1, binData2), collected)
    }
}
