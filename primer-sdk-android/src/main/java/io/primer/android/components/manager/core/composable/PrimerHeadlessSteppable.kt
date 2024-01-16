package io.primer.android.components.manager.core.composable

import kotlinx.coroutines.flow.Flow

interface PrimerHeadlessStep

interface PrimerHeadlessSteppable<T : PrimerHeadlessStep> {

    val componentStep: Flow<T>
}
