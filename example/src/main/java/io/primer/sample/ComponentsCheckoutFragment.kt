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
import androidx.lifecycle.lifecycleScope
import io.primer.android.PrimerCheckout
import io.primer.android.scope.PrimerCheckoutScope
import io.primer.sample.viewmodels.MainViewModel
import kotlinx.coroutines.launch

class ComponentsCheckoutFragment : Fragment() {

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
                    PrimerCheckout(clientToken = token)
                    {

                        // State observation
                        lifecycleScope.launch {
                            it.state.collect { state ->
                                if (state is PrimerCheckoutScope.State.Dismissed) {
                                    activity?.onBackPressedDispatcher?.onBackPressed()
                                }
                            }
                        }

//                        //Checkout customization
//                        it.container = { content ->
//                            content()
//                        }
//
//                        // Sub scope access
//                        it.paymentMethodSelection.paymentMethodCard = { modifier, onClick ->
//                            Text("smth")
//                        }
//
//                        it.cardForm.submitButton = { modifier, text ->
//                            Button(onClick = { it.cardForm.onSubmit() }, modifier = Modifier.size(200.dp)) {
//                                Text(text = text)
//                            }
//                        }
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
