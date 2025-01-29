package io.primer.android.ui.fragments.klarna

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.primer.android.R
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.extensions.getParcelableArrayListCompat
import io.primer.android.core.extensions.getParcelableCompat
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.databinding.PrimerFragmentKlarnaPaymentCategorySelectionBinding
import io.primer.android.klarna.PrimerHeadlessUniversalCheckoutKlarnaManager
import io.primer.android.klarna.api.component.KlarnaComponent
import io.primer.android.klarna.api.composable.KlarnaPaymentCollectableData
import io.primer.android.klarna.api.composable.KlarnaPaymentStep
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.extensions.getCollapsedSheetHeight
import io.primer.android.ui.fragments.base.BaseFragment
import io.primer.android.ui.fragments.klarna.model.KlarnaPaymentCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import io.primer.android.klarna.implementation.session.domain.models.KlarnaPaymentCategory as DomainKlarnaPaymentCategory

private val VISIBILITY_UPDATE_DELAY = 1.seconds

@Suppress("TooManyFunctions")
internal class KlarnaPaymentCategorySelectionFragment : BaseFragment() {
    private var binding: PrimerFragmentKlarnaPaymentCategorySelectionBinding by autoCleaned()

    private val component: KlarnaComponent by lazy {
        PrimerHeadlessUniversalCheckoutKlarnaManager(
            viewModelStoreOwner = this,
        ).provideKlarnaComponent(primerSessionIntent = primerSessionIntent)
    }

    private val primerConfig get() = resolve<PrimerConfig>()

    private val primerSessionIntent
        get() = primerConfig.paymentMethodIntent

    private val returnIntentUrl
        get() =
            primerConfig.settings.paymentMethodOptions.klarnaOptions.returnIntentUrl
                .orEmpty()

    private var categories: ArrayList<DomainKlarnaPaymentCategory>? = null
    private var selectedCategory: DomainKlarnaPaymentCategory? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        categories = savedInstanceState?.getParcelableArrayListCompat(CATEGORIES_KEY)
        selectedCategory = savedInstanceState?.getParcelableCompat(SELECTED_CATEGORY_KEY)
        binding =
            PrimerFragmentKlarnaPaymentCategorySelectionBinding.inflate(
                inflater,
                container,
                false,
            ).apply {
                authorize.isEnabled = false
                paymentCategories.setOnItemClickListener { index ->
                    selectedCategory = requireNotNull(categories?.get(index))
                    updatePaymentOptions(requireNotNull(selectedCategory))
                }
            }
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
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

        getToolbar()?.showOnlyLogo(R.drawable.ic_logo_klarna)

        getToolbar()?.getBackButton()?.visibility = View.INVISIBLE

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
        getToolbar()?.getBackButton()?.isVisible =
            isVisible ?: primerConfig.isStandalonePaymentMethod.not()
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
                    getToolbar()?.getBackButton()?.setOnClickListener {
                        parentFragmentManager.popBackStack()
                    }
                    binding.paymentCategories.klarnaPaymentCategories =
                        categories.orEmpty().map { category ->
                            category.toKlarnaPaymentCategory(
                                if (category == selectedCategory) {
                                    klarnaStep.paymentView
                                } else {
                                    null
                                },
                            )
                        }
                    if (categories.orEmpty().size == 1) {
                        submit()
                    } else if (categories.orEmpty().size > 1) {
                        binding.authorize.isEnabled = true
                    }
                }

                is KlarnaPaymentStep.PaymentSessionAuthorized -> {
                    // no-op
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
            viewLifecycleOwner.lifecycleScope.launch {
                delay(VISIBILITY_UPDATE_DELAY)
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
                paymentCategory = selectedCategory,
            ),
        )
    }

    private fun DomainKlarnaPaymentCategory.toKlarnaPaymentCategory(
        klarnaPaymentView: View? = null,
    ): KlarnaPaymentCategory =
        if (klarnaPaymentView == null) {
            KlarnaPaymentCategory.UnselectedKlarnaPaymentCategory(
                id = identifier,
                name = name,
                iconUrl = standardAssetUrl,
            )
        } else {
            KlarnaPaymentCategory.SelectedKlarnaPaymentCategory(
                id = identifier,
                name = name,
                iconUrl = standardAssetUrl,
                view = klarnaPaymentView,
            )
        }

    companion object {
        private const val CATEGORIES_KEY = "categories"
        private const val SELECTED_CATEGORY_KEY = "selected_category"

        fun newInstance() = KlarnaPaymentCategorySelectionFragment()
    }
}
