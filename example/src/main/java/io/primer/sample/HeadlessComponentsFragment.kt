package io.primer.sample

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.GsonBuilder
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.manager.PrimerHeadlessUniversalCheckoutCardComponentsManager
import io.primer.android.components.manager.PrimerHeadlessUniversalCheckoutCardComponentsManagerInterface
import io.primer.android.components.manager.PrimerHeadlessUniversalCheckoutCardComponentsManagerListener
import io.primer.android.components.manager.nativeUi.PrimerHeadlessUniversalCheckoutNativeUiManager
import io.primer.android.components.ui.assets.PrimerHeadlessUniversalCheckoutAssetsManager
import io.primer.android.components.ui.widgets.PrimerEditTextFactory
import io.primer.android.components.ui.widgets.elements.PrimerInputElement
import io.primer.android.components.ui.widgets.elements.PrimerInputElementListener
import io.primer.android.domain.exception.UnsupportedPaymentIntentException
import io.primer.android.domain.payments.additionalInfo.MultibancoCheckoutAdditionalInfo
import io.primer.android.domain.payments.additionalInfo.PromptPayCheckoutAdditionalInfo
import io.primer.android.ui.CardNetwork
import io.primer.sample.constants.PrimerHeadlessCallbacks
import io.primer.sample.databinding.FragmentHeadlessBinding
import io.primer.sample.datamodels.CheckoutDataWithError
import io.primer.sample.datamodels.TransactionState
import io.primer.sample.datamodels.toMappedError
import io.primer.sample.repositories.AppApiKeyRepository
import io.primer.sample.viewmodels.HeadlessManagerViewModel
import io.primer.sample.viewmodels.HeadlessManagerViewModelFactory
import io.primer.sample.viewmodels.MainViewModel
import io.primer.sample.viewmodels.UiState

class HeadlessComponentsFragment : Fragment(), PrimerInputElementListener {

    private val callbacks: ArrayList<String> = arrayListOf()
    private var checkoutDataWithError: CheckoutDataWithError? = null

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var headlessManagerViewModel: HeadlessManagerViewModel

    private lateinit var cardManager: PrimerHeadlessUniversalCheckoutCardComponentsManagerInterface

    private val inputElementListener = object : PrimerInputElementListener {
        override fun inputElementValueChanged(inputElement: PrimerInputElement) {
            /* TODO */
        }

        override fun inputElementValueIsValid(inputElement: PrimerInputElement, isValid: Boolean) {
            /* TODO */
        }

        override fun inputElementDidDetectCardType(network: CardNetwork.Type) {
            /* TODO */
        }
    }

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

        binding.nextButton.setOnClickListener {
            if (::cardManager.isInitialized && cardManager.isCardFormValid()) cardManager.submit()
        }

    }

    private fun initViewModel() {
        headlessManagerViewModel = ViewModelProvider(
            this,
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
                    checkoutDataWithError = CheckoutDataWithError(state.checkoutData.payment)
                    callbacks.add(PrimerHeadlessCallbacks.ON_CHECKOUT_COMPLETED)
                    hideLoading()
                    navigateToResultScreen()
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

    override fun inputElementValueChanged(inputElement: PrimerInputElement) {
        Log.d(TAG, "inputElementValueChanged ${inputElement.getType()}")
        binding.nextButton.isEnabled = cardManager.isCardFormValid()
    }

    override fun inputElementValueIsValid(inputElement: PrimerInputElement, isValid: Boolean) {
        Log.d(TAG, "inputElementValueIsValid ${inputElement.getType()} $isValid")
    }

    override fun inputElementDidDetectCardType(network: CardNetwork.Type) {
        Log.d(TAG, "inputElementDidDetectCardType $network")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setClientToken(null)
    }

    private fun setupPaymentMethod(paymentMethodTypes: List<PrimerHeadlessUniversalCheckoutPaymentMethod>) {
        binding.pmView.removeAllViews()
        paymentMethodTypes.forEach {
            if (it.paymentMethodManagerCategories.contains(PrimerPaymentMethodManagerCategory.CARD_COMPONENTS)) {
                cardManager =
                    PrimerHeadlessUniversalCheckoutCardComponentsManager.newInstance(it.paymentMethodType)
                cardManager.setCardManagerListener(object :
                    PrimerHeadlessUniversalCheckoutCardComponentsManagerListener {
                    override fun onCardValidationChanged(isCardFormValid: Boolean) {
                        Log.d(
                            TAG,
                            "onCardValidChanged $isCardFormValid"
                        )
                        binding.nextButton.isEnabled = isCardFormValid
                    }
                })
                cardManager.setInputElements(
                    createForm(
                        it.paymentMethodType, cardManager.getRequiredInputElementTypes()
                    )
                )
            } else if (it.paymentMethodManagerCategories.contains(PrimerPaymentMethodManagerCategory.NATIVE_UI)) {
                addPaymentMethodView(it.paymentMethodType)
            }
        }
    }

    private fun createForm(
        title: String,
        requiredInputElementTypes: List<PrimerInputElementType>
    ): List<PrimerInputElement> {
        val inputElements = requiredInputElementTypes.map { type ->
            PrimerEditTextFactory.createFromType(requireContext(), type).apply {
                (this as TextView).setHint(getHint(type))
                setPrimerInputElementListener(inputElementListener)
            }
        }

        val viewGroup = (binding.parentView as ViewGroup)
        viewGroup.removeAllViews()
        LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(resources.getDimensionPixelSize(R.dimen.large_padding))
            setBackgroundResource(R.drawable.background_section_outline)
            addView(TextView(requireContext()).apply {
                text = title
            })

            inputElements.forEach {
                addView((it as TextView))
            }
            viewGroup.addView(this)
        }

        return inputElements
    }

    private fun addPaymentMethodView(paymentMethodType: String) {
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
                onPaymentMethodSelected(paymentMethodType)
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

    private fun getHint(inputElementType: PrimerInputElementType): Int {
        return when (inputElementType) {
            PrimerInputElementType.CARDHOLDER_NAME -> R.string.card_holder_name
            PrimerInputElementType.CVV -> R.string.card_cvv
            PrimerInputElementType.EXPIRY_DATE -> R.string.card_expiry
            PrimerInputElementType.CARD_NUMBER -> R.string.card_number
            PrimerInputElementType.POSTAL_CODE -> R.string.postalCodeLabel
            PrimerInputElementType.COUNTRY_CODE -> R.string.countryLabel
            PrimerInputElementType.CITY -> R.string.cityLabel
            PrimerInputElementType.STATE -> R.string.stateLabel
            PrimerInputElementType.ADDRESS_LINE_1 -> R.string.addressLine1
            PrimerInputElementType.ADDRESS_LINE_2 -> R.string.addressLine2
            PrimerInputElementType.PHONE_NUMBER -> R.string.input_hint_form_phone_number
            PrimerInputElementType.FIRST_NAME -> R.string.firstNameLabel
            PrimerInputElementType.LAST_NAME -> R.string.lastNameLabel
            else -> R.string.enter_address
        }
    }

    private fun onPaymentMethodSelected(paymentMethodType: String) {
        callbacks.clear()
        checkoutDataWithError = null
        try {
            val nativeUiManager =
                PrimerHeadlessUniversalCheckoutNativeUiManager.newInstance(paymentMethodType).also {
                    headlessManagerViewModel.addCloseable {
                        it.cleanup()
                    }
                }
            nativeUiManager.showPaymentMethod(
                requireContext(), when (binding.typeButtonGroup.checkedButtonId) {
                    binding.checkout.id -> PrimerSessionIntent.CHECKOUT
                    else -> PrimerSessionIntent.VAULT
                }
            )
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

    private fun navigateToResultScreen() {
        headlessManagerViewModel.resetUiState()
        findNavController().navigate(
            R.id.action_MerchantComponentsFragment_to_MerchantResultFragment,
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
