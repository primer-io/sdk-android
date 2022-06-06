package io.primer.android.ui.fragments.bank

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.primer.android.R
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.PaymentMethodContextParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.di.BANK_SELECTOR_SCOPE
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.BankSelectionAdapter
import io.primer.android.ui.BankSelectionAdapterListener
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.extensions.getCollapsedSheetHeight
import io.primer.android.ui.fragments.bank.binding.BaseBankSelectionBinding
import io.primer.android.ui.fragments.base.BaseFragment
import io.primer.android.utils.ImageLoader
import io.primer.android.viewmodel.TokenizationViewModel
import io.primer.android.viewmodel.bank.BankSelectionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import org.koin.core.qualifier.named

@ExperimentalCoroutinesApi
@KoinApiExtension
internal abstract class BaseBankSelectionFragment :
    BaseFragment(),
    BankSelectionAdapterListener {

    protected val tokenizationViewModel by viewModel<TokenizationViewModel>()

    private val imageLoader: ImageLoader by inject()

    private var adapter: BankSelectionAdapter by autoCleaned {
        BankSelectionAdapter(
            this,
            imageLoader,
            theme
        )
    }

    protected abstract val baseBinding: BaseBankSelectionBinding
    protected abstract val viewModel: BankSelectionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getKoin().getOrCreateScope(BANK_SELECTOR_SCOPE, named(BANK_SELECTOR_SCOPE))
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
        getKoin().getOrCreateScope(BANK_SELECTOR_SCOPE, named(BANK_SELECTOR_SCOPE))
            .close()
    }

    override fun onBankSelected(issuerId: String) {
        viewModel.onBankSelected(issuerId)
        val descriptor = primerViewModel.selectedPaymentMethod.value as AsyncPaymentMethodDescriptor
        descriptor.appendTokenizableValue("sessionInfo", "issuer", issuerId)
        primerViewModel.executeBehaviour(descriptor.behaviours.first())
    }

    protected open fun setupViews() {
        baseBinding.chooseBankTitle.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )

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

        setupErrorViews()
        adjustBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED)
    }

    protected open fun setupObservers() {
        viewModel.itemsLiveData.observe(viewLifecycleOwner) {
            adapter.items = it
            onLoadingSuccess()
        }
        viewModel.errorLiveData.observe(viewLifecycleOwner) {
            onLoadingError()
        }

        viewModel.loadingLiveData.observe(viewLifecycleOwner) {
            onLoading(it)
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
        viewModel.loadData(
            primerViewModel.selectedPaymentMethod.value as? AsyncPaymentMethodDescriptor ?: return
        )
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

    private fun logAnalyticsViewed() = viewModel.addAnalyticsEvent(
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

    private fun logAnalyticsBackPressed() = viewModel.addAnalyticsEvent(
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
}
