package io.primer.android.paymentmethods.manager.composable

import kotlinx.coroutines.flow.Flow

interface PrimerHeadlessStep

interface PrimerHeadlessSteppable<T : PrimerHeadlessStep> {
    val componentStep: Flow<T>
}
