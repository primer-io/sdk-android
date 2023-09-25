package io.primer.android.components.manager.core.composable

import kotlinx.coroutines.flow.Flow

interface PrimerHeadlessStep

internal interface PrimerHeadlessStepable<T : PrimerHeadlessStep> {

    val componentStep: Flow<T>
}
