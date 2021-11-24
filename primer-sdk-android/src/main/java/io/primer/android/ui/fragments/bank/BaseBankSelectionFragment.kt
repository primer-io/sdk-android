package io.primer.android.ui.fragments.bank

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.primer.android.PrimerTheme
import io.primer.android.R
import io.primer.android.di.BANK_SELECTOR_SCOPE
import io.primer.android.di.DIAppComponent
import io.primer.android.extensions.getCollapsedSheetHeight
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.BankSelectionAdapter
import io.primer.android.ui.BankSelectionAdapterListener
import io.primer.android.ui.fragments.CheckoutSheetFragment
import io.primer.android.utils.ImageLoader
import io.primer.android.viewmodel.PrimerViewModel
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
    Fragment(),
    BankSelectionAdapterListener,
    DIAppComponent {

    protected val tokenizationViewModel by viewModel<TokenizationViewModel>()
    protected val primerViewModel by activityViewModels<PrimerViewModel>()

    protected val theme: PrimerTheme by inject()
    private val imageLoader: ImageLoader by inject()

    private lateinit var adapter: BankSelectionAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var chooseBankParent: RelativeLayout
    private lateinit var errorLayout: ConstraintLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var space: Space

    protected abstract val layoutId: Int
    protected abstract val viewModel: BankSelectionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getKoin().getOrCreateScope(BANK_SELECTOR_SCOPE, named(BANK_SELECTOR_SCOPE))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupListeners(view)
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

    protected open fun setupViews(view: View) {
        val title = view.findViewById<TextView>(R.id.choose_bank_title)
        title.setTextColor(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )

        val backIcon = view.findViewById<ImageView>(R.id.payment_method_back)
        backIcon.setColorFilter(
            theme.titleText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )

        progressBar = view.findViewById(R.id.progress_bar)
        progressBar.indeterminateDrawable.setTint(
            theme.primaryColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
        )

        progressBar.updateLayoutParams<RelativeLayout.LayoutParams> {
            this.height = view.context.getCollapsedSheetHeight()
        }
        chooseBankParent = view.findViewById(R.id.choose_bank_parent)
        errorLayout = view.findViewById(R.id.error_layout_parent)
        space = view.findViewById(R.id.spacer)

        recyclerView = view.findViewById(R.id.choose_bank_recycler_view)
        recyclerView.addItemDecoration(
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
        recyclerView.adapter = adapter

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

    private fun setupListeners(view: View) {
        view.findViewById<ImageView>(R.id.payment_method_back).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        view.findViewById<Button>(R.id.try_again).setOnClickListener {
            loadData()
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                view.findViewById<View>(R.id.choose_bank_divider_bottom).visibility =
                    when (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        true -> View.INVISIBLE
                        false -> View.VISIBLE
                    }
            }
        })
    }

    protected fun adjustBottomSheetState(state: Int) {
        val parent = (parentFragment as CheckoutSheetFragment).view?.parent as View
        val behaviour =
            (parent.layoutParams as CoordinatorLayout.LayoutParams).behavior as BottomSheetBehavior
        behaviour.state = state
    }

    private fun loadData() {
        viewModel.loadData(
            primerViewModel.selectedPaymentMethod.value as? AsyncPaymentMethodDescriptor ?: return
        )
    }

    private fun onLoadingSuccess() {
        chooseBankParent.isVisible = true
        errorLayout.isVisible = false
        space.isVisible = true
    }

    private fun onLoadingError() {
        chooseBankParent.isVisible = false
        errorLayout.isVisible = true
        space.isVisible = false
        adjustBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED)
    }

    private fun onLoading(showLoader: Boolean) {
        progressBar.isVisible = showLoader
        errorLayout.isVisible = false
    }

    private fun setupErrorViews() {
        errorLayout.findViewById<ImageView>(R.id.error_icon).imageTintList =
            ColorStateList.valueOf(
                theme.titleText.defaultColor.getColor(
                    requireContext(),
                    theme.isDarkMode
                )
            )

        errorLayout.findViewById<TextView>(R.id.error_message).setTextColor(
            ColorStateList.valueOf(
                theme.titleText.defaultColor.getColor(
                    requireContext(),
                    theme.isDarkMode
                )
            )
        )
    }
}
