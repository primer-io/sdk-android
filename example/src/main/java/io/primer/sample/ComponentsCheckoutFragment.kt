package io.primer.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.findNavController
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.sample.viewmodels.MainViewModel

/**
 *
 * Navigate here from MerchantSettingsFragment to test the new Compose API.
 */
class ComponentsCheckoutFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    @OptIn(ExperimentalPrimerApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        viewModel.fetchClientSession()

        return ComposeView(requireContext()).apply {
            setContent {
                val clientToken by viewModel.clientToken.observeAsState()
                clientToken?.let { token ->
                    CheckoutComponentsSelection(
                        clientToken = token,
                        settings = viewModel.settings,
                        onBackPress = {
                            findNavController().popBackStack()
                        },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.setClientToken(null)
    }
}
