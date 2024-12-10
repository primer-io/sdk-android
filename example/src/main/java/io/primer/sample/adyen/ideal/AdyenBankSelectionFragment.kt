package io.primer.sample.adyen.ideal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import io.primer.android.R
import io.primer.android.components.componentWithRedirect.PrimerHeadlessUniversalCheckoutComponentWithRedirectManager
import io.primer.android.components.manager.banks.composable.BanksCollectableData
import io.primer.android.components.manager.banks.composable.BanksStep
import io.primer.android.components.manager.componentWithRedirect.component.BanksComponent
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.sample.databinding.FragmentAdyenBankSelectionBinding
import io.primer.sample.repositories.AppApiKeyRepository
import io.primer.sample.utils.ImageLoader
import io.primer.sample.utils.requireApplication
import io.primer.sample.viewmodels.HeadlessManagerViewModel
import io.primer.sample.viewmodels.HeadlessManagerViewModelFactory
import io.primer.sample.viewmodels.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class AdyenBankSelectionFragment : Fragment() {

    private val headlessManagerViewModel by activityViewModels<HeadlessManagerViewModel> {
        HeadlessManagerViewModelFactory(AppApiKeyRepository(), requireApplication())
    }

    private val imageLoader by lazy { ImageLoader() }

    private val adapter: BankSelectionAdapter by lazy {
        BankSelectionAdapter(
            onClick = {
                component.updateCollectedData(BanksCollectableData.BankId(it.id))
            },
            imageLoader = imageLoader,
        )
    }

    private var binding: FragmentAdyenBankSelectionBinding? = null

    private var banks: List<BankItem>? = null

    private val component: BanksComponent by lazy {
        PrimerHeadlessUniversalCheckoutComponentWithRedirectManager(viewModelStoreOwner = this)
            .provide(paymentMethodType = requireNotNull(arguments?.getString("paymentMethodType")))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentAdyenBankSelectionBinding.inflate(inflater).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
        setupObservers()
        loadData()
    }

    override fun onDestroyView() {
        imageLoader.clearAll()
        super.onDestroyView()
    }

    private fun setupViews() {
        binding?.banksList?.let { recyclerView ->
            recyclerView.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                ).apply {
                    ContextCompat.getDrawable(requireContext(), R.drawable.divider_bank_selection)
                        ?.let { setDrawable(it) }
                }
            )
            recyclerView.adapter = adapter
        }

        binding?.searchBar?.doAfterTextChanged { newText ->
            component.updateCollectedData(
                BanksCollectableData.Filter(newText.toString())
            )
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectBankSteps() }

                launch { collectComponentErrors() }

                launch { collectValidationStatuses() }
            }
        }

        headlessManagerViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.ShowError,
                is UiState.CheckoutCompleted -> {
                    findNavController().navigate(
                        io.primer.sample.R.id.action_AdyenBankSelectionFragment_to_HeadlessFragment
                    )
                }

                else -> Unit
            }
        }
    }

    private suspend fun collectBankSteps() {
        component.componentStep.collect { bankStep ->
            when (bankStep) {
                is BanksStep.Loading -> {
                    banks = null
                    onLoading(true)
                }

                is BanksStep.BanksRetrieved -> {
                    onLoading(false)
                    adapter.items = bankStep.banks.map { issuingBank ->
                        BankItem(
                            id = issuingBank.id,
                            name = issuingBank.name,
                            logoUrl = issuingBank.iconUrl,
                            isLoading = false,
                            isDisabled = false,
                        )
                    }.also { banks = it }
                    onLoadingSuccess()
                }
            }
        }
    }

    private suspend fun collectComponentErrors() {
        component.componentError.collect {
            onLoading(false)
            onLoadingError()
        }
    }

    private suspend fun collectValidationStatuses() {
        component.componentValidationStatus.collect {
            if (it is PrimerValidationStatus.Valid &&
                it.collectableData is BanksCollectableData.BankId
            ) {
                val issuerId = (it.collectableData as BanksCollectableData.BankId).id

                adapter.items = banks.orEmpty().map { bankItem ->
                    if (bankItem.id == issuerId) {
                        bankItem.copy(isLoading = true)
                    } else {
                        bankItem.copy(isDisabled = true)
                    }
                }
                onLoadingSuccess()

                component.submit()
            } else {
                // no-op
            }
        }
    }

    private fun setupListeners() {
        binding?.tryAgain?.setOnClickListener { loadData() }
    }

    private fun loadData() {
        component.start()
    }

    private fun onLoadingSuccess() {
        binding?.errorLayout?.isVisible = false
    }

    private fun onLoadingError() {
        binding?.progressBar?.isVisible = false
        binding?.errorLayout?.isVisible = true
    }

    private fun onLoading(showLoader: Boolean) {
        binding?.progressBar?.isVisible = showLoader
        binding?.errorLayout?.isVisible = false
    }
}
