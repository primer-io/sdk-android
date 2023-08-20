package io.primer.android.components.manager.core.composable

import io.primer.android.domain.error.models.PrimerError
import kotlinx.coroutines.flow.SharedFlow

internal interface Errorable {

    val errorFlow: SharedFlow<PrimerError>
}
