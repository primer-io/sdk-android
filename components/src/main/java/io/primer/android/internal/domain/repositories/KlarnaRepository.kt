package io.primer.android.internal.domain.repositories

import android.content.Context
import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.internal.domain.Cleanable
import io.primer.android.internal.domain.models.KlarnaCategory
import io.primer.android.internal.domain.models.KlarnaStep
import kotlinx.coroutines.flow.Flow

internal interface KlarnaRepository : Cleanable {

    suspend fun start(viewModelStoreOwner: ViewModelStoreOwner)

    suspend fun selectPaymentCategory(context: Context, category: KlarnaCategory)

    suspend fun authorizePayment()

    suspend fun finalizePayment()

    val stepFlow: Flow<KlarnaStep>

    val errorFlow: Flow<String>
}
