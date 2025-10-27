package io.primer.android.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.core.di.DISdkComponent
import io.primer.android.internal.presentation.components.PrimerLoading
import io.primer.android.internal.presentation.screens.klarna.DefaultKlarnaScreen
import io.primer.android.internal.presentation.screens.klarna.KlarnaViewModelFactory
import io.primer.android.internal.presentation.screens.klarna.components.DefaultKlarnaCategorySelector
import io.primer.android.internal.presentation.screens.klarna.components.DefaultKlarnaPaymentViewContainer
import io.primer.android.scope.PrimerKlarnaScope

class PrimerKlarnaComponents : DISdkComponent {

    @Composable
    fun Screen() {
        val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }

        screen(
            viewModel(
                factory = KlarnaViewModelFactory(viewModelStoreOwner),
            ),
        )
    }

    var screen: @Composable PrimerKlarnaScope.() -> Unit = {
        DefaultKlarnaScreen()
    }

    @Composable
    fun PrimerKlarnaScope.CategorySelector(
        modifier: Modifier = Modifier,
    ) {
        DefaultKlarnaCategorySelector(modifier)
    }

    @Composable
    fun PrimerKlarnaScope.PaymentViewContainer(
        modifier: Modifier = Modifier,
    ) {
        DefaultKlarnaPaymentViewContainer(modifier)
    }

    @Composable
    fun PrimerKlarnaScope.AuthorizeButton(
        modifier: Modifier = Modifier,
    ) {
        Button(modifier = modifier, onClick = {
            authorizePayment()
        }) {
            Text(stringResource(R.string.primer_components_klarna_authorize))
        }
    }

    @Composable
    fun PrimerKlarnaScope.FinalizeButton(
        modifier: Modifier = Modifier,
    ) {
        Button(modifier = modifier, onClick = {
            finalizePayment()
        }) {
            Text(stringResource(R.string.primer_components_klarna_finalize))
        }
    }

    @Composable
    fun PrimerKlarnaScope.LoadingIndicator(
        modifier: Modifier = Modifier,
    ) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            PrimerLoading()
        }
    }
}
