package io.primer.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.primer.composable.PrimerLoadingScreen
import io.primer.composable.internal.Primer
import io.primer.composable.scope.PaymentMethodSelectionScope
import io.primer.sample.viewmodels.MainViewModel

class ComposableCheckoutFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        viewModel.fetchClientSession()
        
        return ComposeView(requireContext()).apply {
            setContent {
                val clientToken by viewModel.clientToken.observeAsState()
                clientToken?.let { token ->
                    with(Primer) {
                        configure(token)
                        ComposableCheckout(
                            loadingScreen = { PrimerLoadingScreen() },
                            paymentSelectionScreen = { CustomPaymentMethodSelection() },
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun PaymentMethodSelectionScope.CustomPaymentMethodSelection() {
        val state by state.collectAsState()

        when(state) {
            is PaymentMethodSelectionScope.State.Error -> {

            }
            PaymentMethodSelectionScope.State.Loading -> {

            }
            is PaymentMethodSelectionScope.State.Ready -> {

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.setClientToken(null)
    }
}
