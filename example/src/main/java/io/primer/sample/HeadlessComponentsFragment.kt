package io.primer.sample

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.GsonBuilder
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.manager.nativeUi.PrimerHeadlessUniversalCheckoutNativeUiManager
import io.primer.android.components.ui.assets.PrimerHeadlessUniversalCheckoutAssetsManager
import io.primer.android.domain.exception.UnsupportedPaymentIntentException
import io.primer.android.domain.payments.additionalInfo.AchAdditionalInfo
import io.primer.android.domain.payments.additionalInfo.MultibancoCheckoutAdditionalInfo
import io.primer.android.domain.payments.additionalInfo.PromptPayCheckoutAdditionalInfo
import io.primer.sample.constants.PrimerHeadlessCallbacks
import io.primer.sample.databinding.FragmentHeadlessBinding
import io.primer.sample.datamodels.CheckoutDataWithError
import io.primer.sample.datamodels.TransactionState
import io.primer.sample.datamodels.toMappedError
import io.primer.sample.klarna.KlarnaPaymentFragment.Companion.PRIMER_SESSION_INTENT_ARG
import io.primer.sample.repositories.AppApiKeyRepository
import io.primer.sample.utils.showMandateDialog
import io.primer.sample.viewmodels.HeadlessManagerViewModel
import io.primer.sample.viewmodels.HeadlessManagerViewModelFactory
import io.primer.sample.viewmodels.MainViewModel
import io.primer.sample.viewmodels.UiState
import kotlinx.coroutines.launch

class HeadlessComponentsFragment : Fragment() {

    private val callbacks: ArrayList<String> = arrayListOf()
    private var checkoutDataWithError: CheckoutDataWithError? = null

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var headlessManagerViewModel: HeadlessManagerViewModel

    private lateinit var binding: FragmentHeadlessBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentHeadlessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        observeClientToken()
        observePaymentMethodsLoaded()
        observeUiState()
        configureTypeToggleViews()

        if (savedInstanceState == null) {
            viewModel.fetchClientSession()
            showLoading("Loading client token.")
        }

        observeTransactionState()
    }

    override fun onDestroy() {
        super.onDestroy()
        headlessManagerViewModel.isLaunched = false
        viewModel.setClientToken(null)
    }

    private fun initViewModel() {
        headlessManagerViewModel = ViewModelProvider(
            requireActivity(),
            HeadlessManagerViewModelFactory(AppApiKeyRepository()),
        )[HeadlessManagerViewModel::class.java]
    }

    private fun observeClientToken() {
        viewModel.clientToken.observe(viewLifecycleOwner) { token ->
            if (headlessManagerViewModel.isLaunched != true) {
                token?.let {
                    headlessManagerViewModel.isLaunched = true
                    headlessManagerViewModel.startHeadless(
                        requireContext(), token, viewModel.settings
                    )
                }
            } else {
                headlessManagerViewModel.setHeadlessListeners()
            }
        }
    }

    private fun observePaymentMethodsLoaded() {
        headlessManagerViewModel.paymentMethodsLoaded.observe(viewLifecycleOwner) {
            binding.typeButtonGroup.isVisible = true
            setupPaymentMethod(it)
            hideLoading()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            headlessManagerViewModel.uiState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is UiState.InitializingHeadless -> showLoading("Initializing Headless.")
                    is UiState.InitializedHeadless -> hideLoading()
                    is UiState.TokenizationStarted -> {
                        callbacks.add(PrimerHeadlessCallbacks.ON_TOKENIZATION_STARTED)
                        showLoading("Tokenization started ${state.paymentMethodType}")
                    }

                    is UiState.PreparationStarted -> {
                        callbacks.add(PrimerHeadlessCallbacks.ON_PREPARATION_STARTED)
                        showLoading("Preparation started ${state.paymentMethodType}")
                    }

                    is UiState.PaymentMethodShowed -> {
                        callbacks.add(PrimerHeadlessCallbacks.ON_PAYMENT_METHOD_SHOWED)
                        showLoading("Presented ${state.paymentMethodType}")
                    }

                    is UiState.TokenizationSuccessReceived -> {
                        callbacks.add(PrimerHeadlessCallbacks.ON_TOKENIZE_SUCCESS)
                        if (state.paymentMethodTokenData.isVaulted) {
                            hideLoading()
                            navigateToResultScreen()
                        } else {
                            showLoading("Tokenization success ${state.paymentMethodTokenData}. Creating payment.")
                            headlessManagerViewModel.createPayment(
                                state.paymentMethodTokenData,
                                requireNotNull(viewModel.environment.value),
                                viewModel.descriptor.value.orEmpty(),
                                state.decisionHandler
                            )
                        }
                    }

                    is UiState.ResumePaymentReceived -> {
                        showLoading("Resume success. Resuming payment.")
                        headlessManagerViewModel.resumePayment(
                            state.resumeToken,
                            requireNotNull(viewModel.environment.value),
                            state.decisionHandler
                        )
                    }

                    is UiState.ResumePendingReceived -> {
                        hideLoading()
                        when (state.additionalInfo) {
                            is MultibancoCheckoutAdditionalInfo -> {
                                AlertDialog.Builder(context)
                                    .setMessage("MultibancoData: $state.additionalInfo")
                                    .setPositiveButton("OK") { d, _ -> d.dismiss() }.show()
                                Log.d(
                                    TAG,
                                    "onResumePending MULTIBANCO: $state.additionalInfo"
                                )
                            }
                        }
                    }

                    is UiState.AdditionalInfoReceived -> {
                        hideLoading()
                        when (state.additionalInfo) {
                            is PromptPayCheckoutAdditionalInfo -> {
                                AlertDialog.Builder(context)
                                    .setMessage("PromptPay: $state.additionalInfo")
                                    .setPositiveButton("OK") { d, _ -> d.dismiss() }.show()
                                Log.d(
                                    TAG,
                                    "onAdditionalInfoReceived: $state.additionalInfo"
                                )
                            }

                            is AchAdditionalInfo.ProvideActivityResultRegistry -> {
                                state.additionalInfo.provide(
                                    requireActivity().activityResultRegistry
                                )
                            }

                            is AchAdditionalInfo.DisplayMandate -> {
                                requireContext().showMandateDialog(
                                    text = "Would you like to accept this mandate?",
                                    onOkClick = {
                                        lifecycleScope.launch {
                                            (state.additionalInfo).onAcceptMandate.invoke()
                                        }
                                    },
                                    onCancelClick = {
                                        lifecycleScope.launch {
                                            (state.additionalInfo).onDeclineMandate.invoke()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    is UiState.BeforePaymentCreateReceived -> {
                        callbacks.add(PrimerHeadlessCallbacks.ON_BEFORE_PAYMENT_CREATED)
                    }

                    is UiState.BeforeClientSessionUpdateReceived -> {
                        callbacks.add(PrimerHeadlessCallbacks.ON_BEFORE_CLIENT_SESSION_UPDATED)
                    }

                    is UiState.ClientSessionUpdatedReceived -> {
                        callbacks.add(PrimerHeadlessCallbacks.ON_CLIENT_SESSION_UPDATED)
                    }

                    is UiState.ShowError -> {
                        checkoutDataWithError =
                            CheckoutDataWithError(state.payment, state.error.toMappedError())
                        callbacks.add(PrimerHeadlessCallbacks.ON_FAILED_WITH_CHECKOUT_DATA)
                        hideLoading()
                        navigateToResultScreen()
                    }

                    is UiState.CheckoutCompleted -> {
                        checkoutDataWithError =
                            CheckoutDataWithError(state.checkoutData.payment)
                        callbacks.add(PrimerHeadlessCallbacks.ON_CHECKOUT_COMPLETED)
                        hideLoading()
                        navigateToResultScreen()
                    }
                }
            }
        }
    }

    private fun observeTransactionState() {
        headlessManagerViewModel.transactionState.observe(viewLifecycleOwner) { state ->
            val message = when (state) {
                TransactionState.SUCCESS -> headlessManagerViewModel.transactionResponse.value.toString()
                TransactionState.ERROR -> requireContext().getString(R.string.something_went_wrong)
                else -> return@observe
            }
            AlertDialog.Builder(context).setMessage(message).show()
            viewModel.resetTransactionState()
        }
    }

    private fun configureTypeToggleViews() {
        binding.typeButtonGroup.check(binding.checkout.id)
    }

    private fun setupPaymentMethod(paymentMethodTypes: List<PrimerHeadlessUniversalCheckoutPaymentMethod>) {
        binding.pmView.removeAllViews()
        paymentMethodTypes.forEach {
            addPaymentMethodView(it.paymentMethodType, it.paymentMethodManagerCategories)
        }
    }

    private fun addPaymentMethodView(
        paymentMethodType: String,
        managerCategories: List<PrimerPaymentMethodManagerCategory>
    ) {
        val pmViewGroup = (binding.pmView as ViewGroup)
        pmViewGroup.addView(ImageButton(context).apply {
            minimumHeight = resources.getDimensionPixelSize(R.dimen.pay_button_height)
            val paymentMethodAsset = try {
                PrimerHeadlessUniversalCheckoutAssetsManager.getPaymentMethodAsset(
                    requireContext(),
                    paymentMethodType
                )
            } catch (e: SdkUninitializedException) {
                null
            }
            paymentMethodAsset?.paymentMethodBackgroundColor?.colored?.let {
                setBackgroundColor(it)
            }

            setImageDrawable(
                paymentMethodAsset?.paymentMethodLogo?.colored
            )

            contentDescription = "Pay with ${paymentMethodAsset?.paymentMethodName}"

            setOnClickListener {
                when {
                    paymentMethodType == "NOL_PAY" ->
                        findNavController().navigate(R.id.action_HeadlessComponentsFragment_to_NolPayFragment)

                    managerCategories.contains(PrimerPaymentMethodManagerCategory.RAW_DATA) ->
                        findNavController().navigate(
                            R.id.action_HeadlessComponentsFragment_to_HeadlessRawFragment,
                            Bundle().apply {
                                putString(
                                    HeadlessRawFragment.PAYMENT_METHOD_TYPE_EXTRA,
                                    paymentMethodType
                                )
                            }
                        )

                    paymentMethodType == "ADYEN_IDEAL" || paymentMethodType == "ADYEN_DOTPAY" ->
                        findNavController().navigate(
                            R.id.action_HeadlessComponentsFragment_to_AdyenBankSelectionFragment,
                            bundleOf("paymentMethodType" to paymentMethodType)
                        )

                    paymentMethodType == "KLARNA" ->
                        findNavController().navigate(
                            R.id.action_HeadlessComponentsFragment_to_KlarnaFragment,
                            bundleOf(PRIMER_SESSION_INTENT_ARG to getPrimerSessionIntent())
                        )

                    paymentMethodType == "STRIPE_ACH" ->
                        findNavController().navigate(R.id.action_HeadlessComponentsFragment_to_StripeAchFragment)

                    else -> onPaymentMethodSelected(paymentMethodType)
                }
            }
        })
    }

    private fun showLoading(message: String? = null) {
        binding.progressLayout.progressText.text = message
        binding.progressLayout.progressLayoutRoot.isVisible = true
    }

    private fun hideLoading() {
        binding.progressLayout.progressLayoutRoot.isVisible = false
    }

    private fun onPaymentMethodSelected(paymentMethodType: String) {
        callbacks.clear()
        checkoutDataWithError = null
        try {
            val nativeUiManager =
                PrimerHeadlessUniversalCheckoutNativeUiManager.newInstance(paymentMethodType)
                    .also {
                        headlessManagerViewModel.addCloseable {
                            it.cleanup()
                        }
                    }
            nativeUiManager.showPaymentMethod(requireContext(), getPrimerSessionIntent())
        } catch (e: SdkUninitializedException) {
            AlertDialog.Builder(context).setMessage(e.message).setNegativeButton(
                android.R.string.cancel
            ) { _, _ -> findNavController().navigateUp() }.show()
        } catch (e: UnsupportedPaymentIntentException) {
            AlertDialog.Builder(context).setMessage(e.message).setNegativeButton(
                android.R.string.cancel
            ) { _, _ -> findNavController().navigateUp() }.show()
        }
    }

    private fun getPrimerSessionIntent() = when (binding.typeButtonGroup.checkedButtonId) {
        binding.checkout.id -> PrimerSessionIntent.CHECKOUT
        else -> PrimerSessionIntent.VAULT
    }

    private fun navigateToResultScreen() {
        headlessManagerViewModel.resetUiState()
        findNavController().navigate(
            R.id.action_HeadlessComponentsFragment_to_MerchantResultFragment,
            Bundle().apply {
                putInt(
                    MerchantResultFragment.PAYMENT_STATUS_KEY,
                    if (checkoutDataWithError?.error != null) MerchantResultFragment.Companion.PaymentStatus.FAILURE.ordinal
                    else MerchantResultFragment.Companion.PaymentStatus.SUCCESS.ordinal
                )
                putStringArrayList(
                    MerchantResultFragment.INVOKED_CALLBACKS_KEY,
                    callbacks
                )
                putString(
                    MerchantResultFragment.PAYMENT_RESPONSE_KEY,
                    GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
                        .toJson(checkoutDataWithError)
                )
            })
    }

    companion object {
        private val TAG = this::class.simpleName
    }
}
