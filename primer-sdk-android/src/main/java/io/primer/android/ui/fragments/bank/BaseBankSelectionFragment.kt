package io.primer.android.ui.fragments.bank

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
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
import io.primer.android.components.manager.banks.composable.BanksCollectableData
import io.primer.android.components.manager.banks.composable.BanksStep
import io.primer.android.components.manager.componentWithRedirect.PrimerHeadlessUniversalCheckoutComponentWithRedirectManager
import io.primer.android.components.manager.componentWithRedirect.component.BanksComponent
import io.primer.android.components.manager.core.composable.PrimerValidationStatus
import io.primer.android.di.DISdkContext
import io.primer.android.di.RpcContainer
import io.primer.android.di.extension.inject
import io.primer.android.ui.BankItem
import io.primer.android.ui.BankSelectionAdapter
import io.primer.android.ui.BankSelectionAdapterListener
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.extensions.getCollapsedSheetHeight
import io.primer.android.ui.fragments.bank.binding.BaseBankSelectionBinding
import io.primer.android.ui.fragments.base.BaseFragment
import io.primer.android.utils.ImageLoader
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@Suppress("detekt:TooManyFunctions")
@ExperimentalCoroutinesApi
internal abstract class BaseBankSelectionFragment :
    BaseFragment(),
    BankSelectionAdapterListener {

    private val imageLoader: ImageLoader by inject()

    private var adapter: BankSelectionAdapter by autoCleaned {
        BankSelectionAdapter(
            this,
            imageLoader,
            theme
        )
    }

    private val paymentMethodType: String
        get() = primerViewModel.selectedPaymentMethod.value?.config?.type.orEmpty()

    protected abstract val baseBinding: BaseBankSelectionBinding

    private var banks: List<BankItem>? = null

    protected val component: BanksComponent by lazy {
        PrimerHeadlessUniversalCheckoutComponentWithRedirectManager(viewModelStoreOwner = this)
            .provide(paymentMethodType = paymentMethodType)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DISdkContext.sdkContainer?.let {
            it.registerContainer(RpcContainer(it))
        }
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
        DISdkContext.sdkContainer?.unregisterContainer<RpcContainer>()
    }

    override fun onBankSelected(issuerId: String) {
        component.updateCollectedData(BanksCollectableData.BankId(issuerId))
    }

    protected open fun setupViews() {
        baseBinding.chooseBankTitle.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )

        baseBinding.paymentMethodBack.isVisible =
            primerViewModel.selectedPaymentMethod.value?.localConfig
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
                val issuerId = it.collectableData.id

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
            logAnalyticsBackPressed()
            parentFragmentManager.popBackStack()
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
            primerViewModel.selectedPaymentMethod.value?.config?.type?.let {
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
            primerViewModel.selectedPaymentMethod.value?.config?.type?.let {
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
}
