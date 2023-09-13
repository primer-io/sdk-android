package io.primer.android.components.manager.core.composable

import io.primer.android.domain.error.models.PrimerError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface PrimerHeadlessErrorable {

    val errorFlow: Flow<PrimerError>
}
