package io.primer.android.ui.fragments.klarna

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.manager.klarna.PrimerHeadlessUniversalCheckoutKlarnaManager
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.composable.KlarnaComponent
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models.KlarnaPaymentCollectableData
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.models.KlarnaPaymentStep
import io.primer.android.databinding.FragmentKlarnaPaymentCategorySelectionBinding
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.extensions.getCollapsedSheetHeight
import io.primer.android.ui.fragments.base.BaseFragment
import io.primer.android.ui.fragments.klarna.model.KlarnaPaymentCategory
import io.primer.android.utils.getParcelableArrayListCompat
import io.primer.android.utils.getParcelableCompat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaPaymentCategory as DomainKlarnaPaymentCategory

@OptIn(ExperimentalCoroutinesApi::class)
internal class KlarnaPaymentCategorySelectionFragment : BaseFragment() {
    private var binding: FragmentKlarnaPaymentCategorySelectionBinding by autoCleaned()

    private val component: KlarnaComponent by lazy {
        PrimerHeadlessUniversalCheckoutKlarnaManager(
            viewModelStoreOwner = this
        ).provideKlarnaComponent(primerSessionIntent = primerSessionIntent)
    }

    private val primerConfig get() = primerViewModel.selectedPaymentMethod.value?.localConfig

    private val primerSessionIntent
        get() = primerConfig?.paymentMethodIntent ?: PrimerSessionIntent.CHECKOUT

    private val returnIntentUrl
        get() = primerConfig?.settings?.paymentMethodOptions?.klarnaOptions?.returnIntentUrl
            .orEmpty()

    private var categories: ArrayList<DomainKlarnaPaymentCategory>? = null
    private var selectedCategory: DomainKlarnaPaymentCategory? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        categories = savedInstanceState?.getParcelableArrayListCompat(CATEGORIES_KEY)
        selectedCategory = savedInstanceState?.getParcelableCompat(SELECTED_CATEGORY_KEY)
        binding = FragmentKlarnaPaymentCategorySelectionBinding.inflate(
            inflater,
            container,
            false
        ).apply {
            authorize.isEnabled = false
            paymentCategories.setOnItemClickListener { index ->
                selectedCategory = requireNotNull(categories?.get(index))
                updatePaymentOptions(requireNotNull(selectedCategory))
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.paymentCategories.dummyKlarnaPaymentViewContainer =
            binding.dummyKlarnaPaymentViewContainer
        binding.paymentCategoryGroup.isVisible = false
        updatePaymentMethodBackVisibility(isVisible = false)
        binding.progressGroup.isVisible = true

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { collectKlarnaComponentSteps() }
                launch { collectComponentErrors() }
                launch { collectValidationStatuses() }
            }
        }

        binding.paymentMethodBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.authorize.setOnClickListener {
            it.isEnabled = false
            submit()
        }

        binding.progressBar.updateLayoutParams<ConstraintLayout.LayoutParams> {
            this.height = requireContext().getCollapsedSheetHeight()
        }

        adjustBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
    }

    private fun updatePaymentMethodBackVisibility(isVisible: Boolean? = null) {
        binding.paymentMethodBack.isVisible =
            isVisible ?: primerConfig?.isStandalonePaymentMethod?.not() ?: false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(CATEGORIES_KEY, categories)
        outState.putParcelable(SELECTED_CATEGORY_KEY, selectedCategory)
    }

    override fun onStart() {
        super.onStart()

        if (categories == null) {
            component.start()
        }
    }

    private suspend fun collectKlarnaComponentSteps() {
        component.componentStep.collectLatest { klarnaStep ->
            when (klarnaStep) {
                is KlarnaPaymentStep.PaymentSessionCreated -> {
                    categories = ArrayList(klarnaStep.paymentCategories)

                    /*
                    Automatically pick payment category if only one is available as per the
                    design.
                    */
                    if (klarnaStep.paymentCategories.size == 1) {
                        updatePaymentOptions(klarnaStep.paymentCategories.single())
                    } else {
                        binding.paymentCategories.klarnaPaymentCategories =
                            klarnaStep.paymentCategories.map {
                                it.toKlarnaPaymentCategory()
                            }
                        binding.progressGroup.isVisible = false
                        binding.paymentCategoryGroup.isVisible = true
                        updatePaymentMethodBackVisibility()
                    }
                }

                is KlarnaPaymentStep.PaymentViewLoaded -> {
                    binding.paymentCategories.klarnaPaymentCategories =
                        categories.orEmpty().map { category ->
                            category.toKlarnaPaymentCategory(
                                if (category == selectedCategory) {
                                    klarnaStep.paymentView
                                } else {
                                    null
                                }
                            )
                        }
                    if (categories.orEmpty().size == 1) {
                        submit()
                    } else if (categories.orEmpty().size > 1) {
                        binding.authorize.isEnabled = true
                    }
                }

                is KlarnaPaymentStep.PaymentSessionAuthorized -> {
                    if (klarnaStep.isFinalized) {
                        // no-op
                    } else {
                        // no-op
                    }
                }

                is KlarnaPaymentStep.PaymentSessionFinalized -> {
                    // no-op
                }
            }
        }
    }

    private suspend fun collectComponentErrors() {
        component.componentError.collectLatest {
            // no-op: For drop-in, errors are dispatched as via CheckoutErrorEventResolver
        }
    }

    private suspend fun collectValidationStatuses() {
        component.componentValidationStatus.collectLatest {
            // no-op
        }
    }

    private fun submit() {
        component.submit()

        if ((categories?.size ?: 0) > 1) {
            // Show loading indicator after 1 second
            binding.root.postDelayed(1000) {
                binding.paymentCategoryGroup.isVisible = false
                updatePaymentMethodBackVisibility(isVisible = false)
                binding.progressGroup.isVisible = true
            }
        }
    }

    private fun updatePaymentOptions(selectedCategory: DomainKlarnaPaymentCategory) {
        component.updateCollectedData(
            KlarnaPaymentCollectableData.PaymentOptions(
                requireContext(),
                returnIntentUrl = returnIntentUrl,
                paymentCategory = selectedCategory
            )
        )
    }

    private fun DomainKlarnaPaymentCategory.toKlarnaPaymentCategory(
        klarnaPaymentView: View? = null
    ): KlarnaPaymentCategory = if (klarnaPaymentView == null) {
        KlarnaPaymentCategory.UnselectedKlarnaPaymentCategory(
            id = identifier,
            name = name,
            iconUrl = standardAssetUrl
        )
    } else {
        KlarnaPaymentCategory.SelectedKlarnaPaymentCategory(
            id = identifier,
            name = name,
            iconUrl = standardAssetUrl,
            view = klarnaPaymentView
        )
    }

    companion object {
        private const val CATEGORIES_KEY = "categories"
        private const val SELECTED_CATEGORY_KEY = "selected_category"

        fun newInstance() = KlarnaPaymentCategorySelectionFragment()
    }
}
