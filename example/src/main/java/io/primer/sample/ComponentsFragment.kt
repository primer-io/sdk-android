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
import io.primer.android.data.settings.PrimerSettings
import io.primer.components.clean.ui.ComposableCheckout
import io.primer.sample.viewmodels.MainViewModel

class ComponentsFragment : Fragment() {

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
                clientToken?.let { ComposableCheckout(it, PrimerSettings()) }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.setClientToken(null)
    }
}
