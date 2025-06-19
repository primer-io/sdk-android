package io.primer.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.primer.composable.Primer
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
                        val scope = showCheckout()

                        // Override container with custom implementation
                        scope.container = { content ->
                            // Custom container implementation
                            content() // Contains navigation
                        }
                        
                        // Override main screens  
                        scope.splashScreen = {
                            // Custom splash screen - 'this' is PrimerCheckoutScope
                            // Custom implementation here
                        }
                        
                        scope.loadingScreen = {
                            // Custom loading screen
                        }
                        
                        scope.successScreen = {
                            // Custom success screen
                        }
                        
                        scope.errorScreen = { message ->
                            // Custom error screen with message
                        }

                        // Override nested scope screens using property access
                        scope.cardForm().screen = {
                            // Custom card form - access scope methods directly
                            // onSubmit = scope.cardFormScope.onSubmit()
                            // isValid = scope.cardFormScope.state.collectAsState().value.isSubmitEnabled
                        }
                        
                        scope.paymentSelection().screen = {
                            // Custom payment selection
                            // onSelect = { method -> scope.paymentSelectionScope.onPaymentMethodSelected(method) }
                        }
                        
                        // Override individual input components
                        scope.cardForm().cardNumberInput = { modifier ->
                            // Custom card number input
                            // onValueChange = { scope.cardFormScope.updateCardNumber(it) }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.setClientToken(null)
    }
}
