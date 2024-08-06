package io.primer.sample.stripe.ach

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import io.primer.android.components.manager.ach.PrimerHeadlessUniversalCheckoutAchManager
import io.primer.android.components.manager.core.composable.PrimerCollectableData
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.StripeAchUserDetailsComponent
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.composable.AchUserDetailsCollectableData
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.composable.AchUserDetailsStep
import io.primer.sample.R
import io.primer.sample.databinding.FragmentStripeAchBinding
import io.primer.sample.repositories.AppApiKeyRepository
import io.primer.sample.utils.hideKeyboard
import io.primer.sample.viewmodels.HeadlessManagerViewModel
import io.primer.sample.viewmodels.HeadlessManagerViewModelFactory
import io.primer.sample.viewmodels.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StripeAchFragment : Fragment() {
    private var isComponentStarted = false
    private var binding: FragmentStripeAchBinding? = null
    private val headlessManagerViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            HeadlessManagerViewModelFactory(AppApiKeyRepository()),
        )[HeadlessManagerViewModel::class.java]
    }

    private val component: StripeAchUserDetailsComponent by lazy {
        PrimerHeadlessUniversalCheckoutAchManager(
            viewModelStoreOwner = requireActivity()
        ).provide("STRIPE_ACH")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentStripeAchBinding.inflate(inflater, container, false).apply {
        submit.isEnabled = false
        binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setupObservers()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { component.collectSteps() }
                launch { component.collectComponentErrors() }
                launch { component.collectValidationStatuses() }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isComponentStarted) {
            isComponentStarted = true
            component.start()
        }
    }

    private fun setupListeners() {
        binding?.submit?.setOnClickListener {
            it.hideKeyboard()
            component.submit()
        }

        binding?.firstName?.doAfterTextChanged {
            component.updateCollectedData(
                AchUserDetailsCollectableData.FirstName(it.toString())
            )
        }

        binding?.lastName?.doAfterTextChanged {
            component.updateCollectedData(
                AchUserDetailsCollectableData.LastName(it.toString())
            )
        }

        binding?.emailAddress?.doAfterTextChanged {
            component.updateCollectedData(
                AchUserDetailsCollectableData.EmailAddress(it?.toString().orEmpty())
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun StripeAchUserDetailsComponent.collectSteps() {
        componentStep.collectLatest { step ->
            when (step) {
                is AchUserDetailsStep.UserDetailsRetrieved -> {
                    val (firstName, lastName, emailAddress) = step
                    binding?.firstName?.setText(firstName)
                    binding?.lastName?.setText(lastName)
                    binding?.emailAddress?.setText(emailAddress)
                }

                is AchUserDetailsStep.UserDetailsCollected -> {
                    popBackStack()
                }
            }
        }
    }

    private suspend fun StripeAchUserDetailsComponent.collectComponentErrors() {
        componentError.collectLatest {
            val error = if (it.errorId == "stripe-invalid-publishable-key") {
                "Stripe publishable key is invalid"
            } else {
                it.description
            }
            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun StripeAchUserDetailsComponent.collectValidationStatuses() {
        componentValidationStatus.collectLatest { status ->
            when (status) {
                is PrimerValidationStatus.Validating -> {
                    // no-op
                }

                is PrimerValidationStatus.Error -> {
                    when (status.collectableData) {
                        is AchUserDetailsCollectableData.FirstName,
                        is AchUserDetailsCollectableData.LastName,
                        is AchUserDetailsCollectableData.EmailAddress -> updateInputErrors(
                            status.collectableData,
                            error = status.toString()
                        )

                        else -> {
                            Toast.makeText(
                                requireContext(),
                                status.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                is PrimerValidationStatus.Invalid -> {
                    when (status.collectableData) {
                        is AchUserDetailsCollectableData.FirstName,
                        is AchUserDetailsCollectableData.LastName,
                        is AchUserDetailsCollectableData.EmailAddress -> updateInputErrors(
                            status.collectableData,
                            error = status.validationErrors.joinToString(separator = "\n") {
                                it.description
                            }
                        )

                        else -> Toast.makeText(
                            requireContext(),
                            status.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                is PrimerValidationStatus.Valid -> {
                    when (status.collectableData) {
                        is AchUserDetailsCollectableData.FirstName,
                        is AchUserDetailsCollectableData.LastName,
                        is AchUserDetailsCollectableData.EmailAddress -> updateInputErrors(
                            status.collectableData,
                            error = null
                        )

                        else -> {
                            // no-op
                        }
                    }
                }
            }

            binding?.submit?.isEnabled = !hasInputErrors() && !hasEmptyInput()
        }
    }

    private fun setupObservers() {
        headlessManagerViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.ShowError,
                is UiState.TokenizationSuccessReceived,
                is UiState.CheckoutCompleted -> {
                    findNavController().navigate(
                        R.id.action_StripeAchFragment_to_HeadlessFragment
                    )
                }

                else -> Unit
            }
        }
    }

    private fun updateInputErrors(
        collectibleData: PrimerCollectableData,
        error: String?
    ) {
        when (collectibleData) {
            is AchUserDetailsCollectableData.FirstName -> binding?.firstName
            is AchUserDetailsCollectableData.LastName -> binding?.lastName
            is AchUserDetailsCollectableData.EmailAddress -> binding?.emailAddress
            else -> return
        }?.error = error
    }

    private fun hasInputErrors(): Boolean =
        binding?.firstName?.error != null
            || binding?.lastName?.error != null
            || binding?.emailAddress?.error != null

    private fun hasEmptyInput(): Boolean =
        binding?.firstName?.text.isNullOrEmpty()
            || binding?.lastName?.text.isNullOrEmpty()
            || binding?.emailAddress?.text.isNullOrEmpty()

    private fun popBackStack() {
        requireActivity().supportFragmentManager.popBackStack()
    }
}
