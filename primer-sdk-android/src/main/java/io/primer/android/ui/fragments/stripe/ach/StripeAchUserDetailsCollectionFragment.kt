package io.primer.android.ui.fragments.stripe.ach

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.primer.android.R
import io.primer.android.components.manager.ach.PrimerHeadlessUniversalCheckoutAchManager
import io.primer.android.components.manager.core.composable.PrimerCollectableData
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.StripeAchUserDetailsComponent
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.composable.AchUserDetailsCollectableData
import io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.composable.AchUserDetailsStep
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.databinding.FragmentStripeAchUserDetailsCollectionBinding
import io.primer.android.domain.payments.additionalInfo.AchAdditionalInfo
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.extensions.getCollapsedSheetHeight
import io.primer.android.ui.fragments.base.BaseFragment
import io.primer.android.utils.hideKeyboard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
@OptIn(ExperimentalCoroutinesApi::class)
internal class StripeAchUserDetailsCollectionFragment : BaseFragment() {
    private var isComponentStarted = false
    private var subscription: EventBus.SubscriptionHandle? = null
    private var binding: FragmentStripeAchUserDetailsCollectionBinding by autoCleaned()

    private val primerConfig get() = primerViewModel.selectedPaymentMethod.value?.localConfig
    private val isStandalonePaymentMethod get() = primerConfig?.isStandalonePaymentMethod ?: false

    private val component: StripeAchUserDetailsComponent by lazy {
        PrimerHeadlessUniversalCheckoutAchManager(viewModelStoreOwner = this).provide(PaymentMethodType.STRIPE_ACH.name)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscription = EventBus.subscribe {
            when (it) {
                is CheckoutEvent.OnAdditionalInfoReceived -> {
                    when (it.paymentMethodInfo) {
                        is AchAdditionalInfo.ProvideActivityResultRegistry -> {
                            it.paymentMethodInfo.provide(requireActivity().activityResultRegistry)
                        }
                    }
                }

                else -> {
                    // no-op
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentStripeAchUserDetailsCollectionBinding.inflate(inflater, container, false).apply {
        progressGroup.isVisible = true
        inputGroup.isVisible = false
        binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { component.collectSteps() }
                launch { component.collectComponentErrors() }
                launch { component.collectValidationStatuses() }
            }
        }

        binding.paymentMethodBack.setOnClickListener {
            parentFragment?.childFragmentManager?.popBackStack()
        }

        binding.progressBar.updateLayoutParams<ConstraintLayout.LayoutParams> {
            this.height = requireContext().getCollapsedSheetHeight()
        }

        updatePaymentMethodBackVisibility(isVisible = false)
        adjustBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
    }

    override fun onStart() {
        super.onStart()
        if (!isComponentStarted) {
            isComponentStarted = true
            component.start()
        }
    }

    private fun setupListeners() {
        binding.submit.setOnClickListener {
            binding.submit.isEnabled = false
            it.hideKeyboard()
            component.submit()
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun StripeAchUserDetailsComponent.collectSteps() {
        componentStep.collectLatest { step ->
            when (step) {
                is AchUserDetailsStep.UserDetailsRetrieved -> {
                    val (firstName, lastName, emailAddress) = step
                    binding.firstNameInput.setText(firstName)
                    binding.lastNameInput.setText(lastName)
                    binding.emailAddressInput.setText(emailAddress)
                    binding.progressGroup.isVisible = false
                    binding.inputGroup.isVisible = true

                    binding.firstNameInput.doAfterTextChanged {
                        component.updateCollectedData(
                            AchUserDetailsCollectableData.FirstName(it.toString())
                        )
                    }
                    binding.lastNameInput.doAfterTextChanged {
                        component.updateCollectedData(
                            AchUserDetailsCollectableData.LastName(it.toString())
                        )
                    }
                    binding.emailAddressInput.doAfterTextChanged {
                        component.updateCollectedData(
                            AchUserDetailsCollectableData.EmailAddress(it.toString().orEmpty())
                        )
                    }

                    updatePaymentMethodBackVisibility()
                }

                is AchUserDetailsStep.UserDetailsCollected -> {
                    binding.inputGroup.isVisible = false
                    binding.progressGroup.isVisible = true
                }
            }
        }
    }

    private suspend fun StripeAchUserDetailsComponent.collectComponentErrors() {
        componentError.collectLatest {
            // no-op: For drop-in, errors are dispatched as via CheckoutErrorEventResolver
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
                            // no-op
                        }
                    }
                }

                is PrimerValidationStatus.Invalid -> {
                    when (status.collectableData) {
                        is AchUserDetailsCollectableData.FirstName -> updateInputErrors(
                            status.collectableData,
                            getString(R.string.stripe_ach_user_details_collection_invalid_first_name)
                        )

                        is AchUserDetailsCollectableData.LastName -> updateInputErrors(
                            status.collectableData,
                            getString(R.string.stripe_ach_user_details_collection_invalid_last_name)
                        )

                        is AchUserDetailsCollectableData.EmailAddress -> updateInputErrors(
                            status.collectableData,
                            getString(R.string.stripe_ach_user_details_collection_invalid_email_address)
                        )

                        else -> {
                            // no-op
                        }
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

            binding.submit.isEnabled = !hasInputErrors()
        }
    }

    private fun updateInputErrors(
        collectibleData: PrimerCollectableData,
        error: String?
    ) {
        when (collectibleData) {
            is AchUserDetailsCollectableData.FirstName -> binding.firstName
            is AchUserDetailsCollectableData.LastName -> binding.lastName
            is AchUserDetailsCollectableData.EmailAddress -> binding.emailAddress
            else -> return
        }.let { field ->
            if (error != null) {
                field.error = error
            } else {
                field.removeError()
            }
        }
    }

    private fun hasInputErrors(): Boolean =
        binding.firstName.error != null ||
            binding.firstName.error != null ||
            binding.emailAddress.error != null

    private fun updatePaymentMethodBackVisibility(isVisible: Boolean? = null) {
        binding.paymentMethodBack.isVisible = isVisible ?: !isStandalonePaymentMethod
    }

    override fun onDestroy() {
        super.onDestroy()
        subscription?.unregister()
    }

    companion object {
        fun newInstance() = StripeAchUserDetailsCollectionFragment()
    }
}
