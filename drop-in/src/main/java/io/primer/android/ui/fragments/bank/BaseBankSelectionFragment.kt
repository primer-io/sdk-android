package io.primer.android.ui.fragments.bank

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.primer.android.R
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.BankIssuerContextParams
import io.primer.android.analytics.domain.models.PaymentMethodContextParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.components.componentWithRedirect.PrimerHeadlessUniversalCheckoutComponentWithRedirectManager
import io.primer.android.components.manager.banks.composable.BanksCollectableData
import io.primer.android.components.manager.banks.composable.BanksStep
import io.primer.android.components.manager.componentWithRedirect.component.BanksComponent
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.components.utils.ImageLoader
import io.primer.android.core.di.extensions.inject
import io.primer.android.ui.BankItem
import io.primer.android.ui.BankSelectionAdapter
import io.primer.android.ui.BankSelectionAdapterListener
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.extensions.getCollapsedSheetHeight
import io.primer.android.ui.extensions.getParentDialogOrNull
import io.primer.android.ui.extensions.popBackStackToRoot
import io.primer.android.ui.fragments.CheckoutSheetFragment
import io.primer.android.ui.fragments.bank.binding.BaseBankSelectionBinding
import io.primer.android.ui.fragments.base.BaseFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@Suppress("detekt:TooManyFunctions")
@ExperimentalCoroutinesApi
internal abstract class BaseBankSelectionFragment : BaseFragment(), BankSelectionAdapterListener {

    private val imageLoader: ImageLoader by inject()

    private val isStandalonePaymentMethod
        get() = primerViewModel.selectedPaymentMethod.value?.uiOptions?.isStandalonePaymentMethod

    private var adapter: BankSelectionAdapter by autoCleaned {
        BankSelectionAdapter(
            listener = this,
            imageLoader = imageLoader,
            theme = theme
        )
    }

    private val paymentMethodType: String
        get() = primerViewModel.selectedPaymentMethod.value?.paymentMethodType.orEmpty()

    protected abstract val baseBinding: BaseBankSelectionBinding

    private var banks: List<BankItem>? = null

    private var isBankSelected = false

    protected val component: BanksComponent by lazy {
        PrimerHeadlessUniversalCheckoutComponentWithRedirectManager(viewModelStoreOwner = this)
            .provide(paymentMethodType = paymentMethodType)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isBankSelected = savedInstanceState?.getBoolean(IS_BANK_SELECTED_KEY) ?: false
        getParentDialogOrNull()?.onBackPressedDispatcher?.addCallback {
            popBackStack()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_BANK_SELECTED_KEY, isBankSelected)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logAnalyticsViewed()
        setupViews()
        setupListeners()

        setupObservers()
        loadData()
    }

    override fun onDestroyView() {
        imageLoader.clearAll()
        super.onDestroyView()
    }

    override fun onBankSelected(issuerId: String) {
        component.updateCollectedData(
            BanksCollectableData.BankId(issuerId)
        )
    }

    protected open fun setupViews() {
        baseBinding.chooseBankTitle.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )

        baseBinding.paymentMethodBack.isVisible =
            primerViewModel.selectedPaymentMethod.value?.uiOptions
                ?.isStandalonePaymentMethod?.not()
                ?: false

        baseBinding.paymentMethodBack.setColorFilter(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )

        baseBinding.progressBar.indeterminateDrawable.setTint(
            theme.primaryColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )

        baseBinding.progressBar.updateLayoutParams<RelativeLayout.LayoutParams> {
            this.height = requireContext().getCollapsedSheetHeight()
        }

        baseBinding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            ).apply {
                ContextCompat.getDrawable(requireContext(), R.drawable.divider_bank_selection)
                    ?.let {
                        setDrawable(it)
                    }
            }
        )
        adapter = BankSelectionAdapter(this, imageLoader, theme)
        baseBinding.recyclerView.adapter = adapter

        baseBinding.searchBar.setTextColor(
            theme.input.text.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )

        baseBinding.searchBar.doAfterTextChanged { newText ->
            component.updateCollectedData(
                BanksCollectableData.Filter(newText.toString())
            )
            baseBinding.chooseBankDividerBottom.visibility =
                when (newText.isNullOrBlank()) {
                    false -> View.INVISIBLE
                    true -> View.VISIBLE
                }
        }

        setupErrorViews()
        adjustBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED)
    }

    protected open fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectBankSteps() }

                launch { collectComponentErrors() }

                launch { collectValidationStatuses() }
            }
        }

        primerViewModel.keyboardVisible.observe(viewLifecycleOwner, ::onKeyboardVisibilityChanged)
    }

    private fun onKeyboardVisibilityChanged(visible: Boolean) {
        if (visible) {
            adjustBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
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
                            logoUrl = issuingBank.iconUrl
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
                isBankSelected = true

                val issuerId = (it.collectableData as BanksCollectableData.BankId).id

                adapter.items = banks.orEmpty().map { bankItem ->
                    if (bankItem.id == issuerId) {
                        bankItem.toLoadingBankItem()
                    } else {
                        bankItem.toDisabledBankItem()
                    }
                }
                onLoadingSuccess()
                logAnalyticsBankSelected(issuerId)

                component.submit()
            } else {
                // no-op
            }
        }
    }

    private fun setupListeners() {
        baseBinding.paymentMethodBack.setOnClickListener {
            popBackStack()
        }
        baseBinding.errorLayout.tryAgain.setOnClickListener {
            loadData()
        }
        baseBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                baseBinding.chooseBankDividerBottom.visibility =
                    when (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        true -> View.INVISIBLE
                        false -> View.VISIBLE
                    }
            }
        })
    }

    private fun popBackStack() {
        logAnalyticsBackPressed()
        if (isStandalonePaymentMethod == true) {
            if (isBankSelected) {
                /*
                Bank was selected, this is considered a cancellation. This fragment isn't on the backstack,
                remove it (along with any other fragments) to trigger the cancellation code in the component.
                */
                with(requireParentFragment().childFragmentManager) {
                    commit { fragments.forEach(::remove) }
                }
            } else {
                (parentFragment as? CheckoutSheetFragment)?.dismiss()
            }
        } else {
            if (isBankSelected) {
                /*
                Bank was selected, this is considered a cancellation. Pop everything all fragments in the
                parent fragment.
                */
                (parentFragment as? CheckoutSheetFragment)?.popBackStackToRoot()
            } else {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun loadData() {
        component.start()
    }

    private fun onLoadingSuccess() {
        baseBinding.chooseBankParent.isVisible = true
        baseBinding.errorLayout.errorLayoutParent.isVisible = false
        baseBinding.spacer.isVisible = true
    }

    private fun onLoadingError() {
        baseBinding.chooseBankParent.isVisible = false
        baseBinding.errorLayout.errorLayoutParent.isVisible = true
        baseBinding.spacer.isVisible = false
        adjustBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED)
    }

    private fun onLoading(showLoader: Boolean) {
        baseBinding.progressBar.isVisible = showLoader
        baseBinding.errorLayout.errorLayoutParent.isVisible = false
    }

    private fun setupErrorViews() {
        baseBinding.errorLayout.errorIcon.imageTintList =
            ColorStateList.valueOf(
                theme.titleText.defaultColor.getColor(
                    requireContext(),
                    theme.isDarkMode
                )
            )

        baseBinding.errorLayout.errorMessage.setTextColor(
            ColorStateList.valueOf(
                theme.titleText.defaultColor.getColor(
                    requireContext(),
                    theme.isDarkMode
                )
            )
        )
    }

    private fun logAnalyticsViewed() = addAnalyticsEvent(
        UIAnalyticsParams(
            AnalyticsAction.VIEW,
            ObjectType.VIEW,
            Place.BANK_SELECTION_LIST,
            ObjectId.VIEW,
            primerViewModel.selectedPaymentMethod.value?.paymentMethodType?.let {
                PaymentMethodContextParams(it)
            }
        )
    )

    private fun logAnalyticsBackPressed() = addAnalyticsEvent(
        UIAnalyticsParams(
            AnalyticsAction.CLICK,
            ObjectType.BUTTON,
            Place.BANK_SELECTION_LIST,
            ObjectId.BACK,
            primerViewModel.selectedPaymentMethod.value?.paymentMethodType?.let {
                PaymentMethodContextParams(it)
            }
        )
    )

    private fun logAnalyticsBankSelected(issuerId: String) = addAnalyticsEvent(
        UIAnalyticsParams(
            AnalyticsAction.CLICK,
            ObjectType.LIST_ITEM,
            Place.BANK_SELECTION_LIST,
            ObjectId.SELECT,
            BankIssuerContextParams(issuerId)
        )
    )

    private fun addAnalyticsEvent(params: UIAnalyticsParams) {
        primerViewModel.addAnalyticsEvent(params)
    }

    private companion object {
        const val IS_BANK_SELECTED_KEY = "is_bank_selected"
    }
}
