package io.primer.sample

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.icu.text.NumberFormat
import android.icu.util.Currency
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.GsonBuilder
import io.primer.android.components.SdkUninitializedException
import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.bancontact.PrimerBancontactCardData
import io.primer.android.components.domain.core.models.bancontact.PrimerBancontactCardMetadata
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.card.PrimerCardMetadataState
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadata
import io.primer.android.components.domain.core.models.metadata.PrimerPaymentMethodMetadataState
import io.primer.android.components.domain.core.models.otp.PrimerOtpCodeData
import io.primer.android.components.domain.core.models.phoneNumber.PrimerPhoneNumberData
import io.primer.android.components.domain.core.models.retailOutlet.PrimerRetailerData
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManager
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerInterface
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.components.ui.assets.PrimerHeadlessUniversalCheckoutAssetsManager
import io.primer.android.data.payments.configure.PrimerInitializationData
import io.primer.android.data.payments.configure.retailOutlets.RetailOutletsList
import io.primer.android.domain.exception.UnsupportedPaymentIntentException
import io.primer.android.domain.payments.additionalInfo.MultibancoCheckoutAdditionalInfo
import io.primer.android.domain.payments.additionalInfo.PromptPayCheckoutAdditionalInfo
import io.primer.android.domain.payments.additionalInfo.XenditCheckoutVoucherAdditionalInfo
import io.primer.android.ui.CardNetwork
import io.primer.sample.constants.PrimerHeadlessCallbacks
import io.primer.sample.databinding.CardNumberInputViewBinding
import io.primer.sample.databinding.FragmentHeadlessBinding
import io.primer.sample.databinding.InputViewBinding
import io.primer.sample.datamodels.CheckoutDataWithError
import io.primer.sample.datamodels.TransactionState
import io.primer.sample.datamodels.toMappedError
import io.primer.sample.viewmodels.HeadlessManagerViewModel
import io.primer.sample.viewmodels.MainViewModel
import io.primer.sample.viewmodels.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.math.pow

class HeadlessRawFragment : Fragment(), PrimerHeadlessUniversalCheckoutRawDataManagerListener {

    private val viewModel: MainViewModel by activityViewModels()
    private val headlessManagerViewModel: HeadlessManagerViewModel by activityViewModels()

    private lateinit var rawDataManager: PrimerHeadlessUniversalCheckoutRawDataManagerInterface

    private var _binding: FragmentHeadlessBinding? = null
    private val binding get() = requireNotNull(_binding)

    private var _cardNumberInputBinding: CardNumberInputViewBinding? = null
    private val cardNumberInputBinding get() = requireNotNull(_cardNumberInputBinding)

    private val callbacks: ArrayList<String> = arrayListOf()
    private var checkoutDataWithError: CheckoutDataWithError? = null

    private val rawDataFlow = MutableStateFlow<PrimerRawData?>(null)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentHeadlessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headlessManagerViewModel.transactionState.observe(viewLifecycleOwner) { state ->
            val message = when (state) {
                TransactionState.SUCCESS -> viewModel.transactionResponse.value.toString()
                TransactionState.ERROR -> requireContext().getString(R.string.something_went_wrong)
                else -> return@observe
            }
            hideLoading()
            AlertDialog.Builder(context).setMessage(message).show()
            viewModel.resetTransactionState()
        }

        observeUiState()

        setupManager(requireNotNull(requireArguments().getString(PAYMENT_METHOD_TYPE_EXTRA)))

        binding.nextButton.setOnClickListener {
            rawDataManager.submit()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                rawDataFlow.filterNotNull().collectLatest { rawData ->
                    rawDataManager.setRawData(rawData)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        headlessManagerViewModel.resetUiState()
        if (::rawDataManager.isInitialized) rawDataManager.cleanup()
        _binding = null
        _cardNumberInputBinding = null
    }

    override fun onValidationChanged(
        isValid: Boolean,
        errors: List<PrimerInputValidationError>
    ) {
        binding.pmView.children.iterator().forEach { parent ->
            (parent as ViewGroup).children.iterator().forEach {
                PrimerInputElementType.values().forEach { type ->
                    it.findViewWithTag<TextInputLayout>(type)?.error = null
                }
            }
        }
        errors.forEach { validationError ->
            binding.pmView.children.iterator().forEach { parent ->
                (parent as ViewGroup).children.iterator().forEach {
                    it.findViewWithTag<TextInputLayout>(validationError.inputElementType)?.error =
                        validationError.description
                }
            }
        }
        binding.nextButton.isEnabled = isValid
    }

    override fun onMetadataChanged(metadata: PrimerPaymentMethodMetadata) {
        when (metadata) {
            is PrimerBancontactCardMetadata -> binding.pmView.findViewWithTag<TextInputLayout>(
                PrimerInputElementType.CARD_NUMBER
            ).prefixText = metadata.cardNetwork.name
        }
    }

    override fun onMetadataStateChanged(metadataState: PrimerPaymentMethodMetadataState) {
        when (metadataState) {
            is PrimerCardMetadataState -> {
                when (metadataState) {
                    is PrimerCardMetadataState.Fetching ->
                        _cardNumberInputBinding?.progressBar?.isVisible = true

                    is PrimerCardMetadataState.Fetched -> _cardNumberInputBinding?.apply {
                        progressBar.isVisible = false
                        cardNetworksSelectionView.isVisible = false
                        cardNetworksSelectionView.removeAllViews()
                        cardNetworkPreview.isVisible = false
                        metadataState.cardNumberEntryMetadata.selectableCardNetworks?.let { selectableCardNetworks ->
                            cardNetworksSelectionView.isVisible = true
                            cardNetworksSelectionView.apply {
                                val lastSelectedCardNetwork =
                                    children.filterIsInstance<AppCompatRadioButton>()
                                        .firstOrNull {
                                            it.id == checkedRadioButtonId
                                        }?.tag
                                selectableCardNetworks.items.forEach { cardNetwork ->
                                    addView(
                                        getCardNetworkView(
                                            lastSelectedCardNetwork as? PrimerCardNetwork,
                                            cardNetwork
                                        )
                                    )

                                }
                            }
                        } ?: run {
                            metadataState.cardNumberEntryMetadata.detectedCardNetworks.let { detectedCardNetworks ->
                                val cardNetworkMetadata = (detectedCardNetworks.preferred
                                    ?: detectedCardNetworks.items.firstOrNull())
                                cardNetworkMetadata?.let {
                                    cardNetworkPreview.isVisible = true
                                    cardNetworkPreview.setImageDrawable(
                                        PrimerHeadlessUniversalCheckoutAssetsManager
                                            .getCardNetworkAsset(
                                                requireContext(),
                                                cardNetworkMetadata.network
                                            ).cardImage
                                    )
                                    cardNetworkPreview.alpha =
                                        if (cardNetworkMetadata.allowed) 1.0f else 0.4f
                                    cardNetworkPreview.contentDescription =
                                        cardNetworkMetadata.displayName
                                } ?: run {
                                    cardNetworkPreview.setImageDrawable(null)
                                    cardNetworkPreview.isVisible = false
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
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

                            is XenditCheckoutVoucherAdditionalInfo -> {
                                AlertDialog.Builder(context)
                                    .setMessage("Obtained additional info: ${state.additionalInfo}")
                                    .show()
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
                        val format = NumberFormat.getCurrencyInstance()
                        val clientSession = state.clientSession
                        val currency =
                            Currency.getInstance(clientSession.currencyCode.orEmpty())
                        format.maximumFractionDigits = currency.defaultFractionDigits
                        format.minimumFractionDigits = currency.defaultFractionDigits
                        format.currency =
                            Currency.getInstance(clientSession.currencyCode.orEmpty())

                        binding.nextButton.text =
                            format.format(
                                clientSession.totalAmount?.toDouble()!! / 10.0.pow(
                                    currency.defaultFractionDigits
                                )
                            )
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

    private fun createForm(
        paymentMethodType: String,
        requiredInputElementTypes: List<PrimerInputElementType>
    ) {
        val inputElements = requiredInputElementTypes.map { type ->
            FrameLayout(requireContext()).apply {
                if (type == PrimerInputElementType.CARD_NUMBER) {
                    _cardNumberInputBinding =
                        CardNumberInputViewBinding.inflate(layoutInflater).apply {
                            cardInput.apply {
                                tag = type
                                hint = getString(getHint(type))
                                addView(TextInputEditText(context).apply {
                                    id = View.generateViewId()
                                    isSingleLine = true
                                    doAfterTextChanged {
                                        // the only reason for this is because Appium sends blank input
                                        // after focusing input
                                        if (rawDataFlow.value != null || it.isNullOrBlank().not()) {
                                            setRawData(getRawData(paymentMethodType))
                                        }
                                    }
                                    layoutParams = LinearLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.MATCH_PARENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                                    )
                                })
                                cardNetworksSelectionView.setOnCheckedChangeListener { _, checkedId ->
                                    cardNetworksSelectionView.children.filterIsInstance<AppCompatRadioButton>()
                                        .iterator()
                                        .forEachRemaining {
                                            it.isChecked = it.id == checkedId
                                            it.compoundDrawables[0]?.alpha = if (it.isChecked) {
                                                RADIO_BUTTON_SELECTED_ALPHA
                                            } else RADIO_BUTTON_UNSELECTED_ALPHA
                                        }
                                    setRawData(getRawData(paymentMethodType))
                                }
                            }
                        }.also {
                            addView(it.root)
                        }
                } else {
                    InputViewBinding.inflate(layoutInflater).apply {
                        input.apply {
                            tag = type
                            hint = getString(getHint(type))
                            addView(TextInputEditText(context).apply {
                                id = View.generateViewId()
                                isSingleLine = true
                                doAfterTextChanged {
                                    setRawData(getRawData(paymentMethodType))
                                }
                                layoutParams = LinearLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.MATCH_PARENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                                )
                            })
                        }
                    }.also {
                        addView(it.root)
                    }
                }
            }
        }

        val viewGroup = (binding.pmView as ViewGroup)
        viewGroup.removeAllViews()
        inputElements.forEach {
            viewGroup.addView(it)
        }
    }

    private fun showLoading(message: String? = null) {
        binding.progressLayout.progressText.text = message
        binding.progressLayout.root.isVisible = true
    }

    private fun hideLoading() {
        binding.progressLayout.root.isVisible = false
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
            PrimerInputElementType.OTP_CODE -> R.string.input_hint_form_blik_otp
            else -> R.string.enter_address
        }
    }

    private fun getRawData(paymentMethodType: String): PrimerRawData {
        return when (paymentMethodType) {
            "PAYMENT_CARD" -> PrimerCardData(
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.CARD_NUMBER
                ).editText?.text.toString().trim(),
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.EXPIRY_DATE
                ).editText?.text.toString().trim(),
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.CVV
                ).editText?.text.toString().trim(),
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.CARDHOLDER_NAME
                ).editText?.text.toString().trim(),
                (cardNumberInputBinding.cardNetworksSelectionView.let { cardNetworkBinding ->
                    cardNetworkBinding.children.firstOrNull { child ->
                        child.id == cardNetworkBinding.checkedRadioButtonId
                    }
                }?.tag as? PrimerCardNetwork)?.network
            )

            "ADYEN_BANCONTACT_CARD" -> PrimerBancontactCardData(
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.CARD_NUMBER
                ).editText?.text.toString().trim(),
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.EXPIRY_DATE
                ).editText?.text.toString().trim(),
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.CARDHOLDER_NAME
                ).editText?.text.toString().trim(),
            )

            "XENDIT_OVO" -> PrimerPhoneNumberData(
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.PHONE_NUMBER
                ).editText?.text.toString()
            )

            "ADYEN_MBWAY" -> PrimerPhoneNumberData(
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.PHONE_NUMBER
                ).editText?.text.toString()
            )

            "ADYEN_BLIK" -> PrimerOtpCodeData(
                binding.pmView.findViewWithTag<TextInputLayout>(
                    PrimerInputElementType.OTP_CODE
                ).editText?.text.toString()
            )

            else -> throw IllegalArgumentException("Unsupported payment method type $paymentMethodType")
        }
    }

    private fun setupManager(paymentMethodType: String) {
        try {
            rawDataManager =
                PrimerHeadlessUniversalCheckoutRawDataManager.newInstance(paymentMethodType)
            rawDataManager.setListener(this)
            if (paymentMethodType == "XENDIT_RETAIL_OUTLETS") {
                rawDataManager.configure { primerInitializationData, error ->
                    if (error == null) {
                        showChooser(primerInitializationData)
                    } else {
                        AlertDialog.Builder(context)
                            .setMessage(error.description)
                            .show()
                    }
                }
            } else {
                createForm(paymentMethodType, rawDataManager.getRequiredInputElementTypes())
            }
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

    private fun showChooser(primerInitializationData: PrimerInitializationData?) {
        when (primerInitializationData) {
            is RetailOutletsList -> {
                val retailerNames = primerInitializationData.result.map { it.name }.toTypedArray()
                val itemClick = DialogInterface.OnClickListener { dialog, which ->
                    if (which > -1) {
                        setRawData(
                            PrimerRetailerData(
                                primerInitializationData.result[which].id
                            )
                        )

                        rawDataManager.submit()
                    }
                    dialog.dismiss()
                }
                AlertDialog.Builder(context)
                    .setItems(
                        retailerNames,
                        itemClick
                    )
                    .show()
            }
        }
    }

    private fun getCardNetworkView(
        lastSelectedCardNetwork: PrimerCardNetwork?,
        cardNetwork: PrimerCardNetwork
    ) = AppCompatRadioButton(requireContext()).apply {
        isChecked = lastSelectedCardNetwork
            ?.network == cardNetwork.network
        buttonDrawable = null
        setCompoundDrawablesWithIntrinsicBounds(
            PrimerHeadlessUniversalCheckoutAssetsManager
                .getCardNetworkAsset(
                    requireContext(),
                    cardNetwork.network
                ).cardImage,
            null,
            null,
            null
        )
        id = cardNetwork.network.hashCode()
        tag = cardNetwork
        contentDescription = cardNetwork.displayName
        compoundDrawables[0]?.alpha = if (isChecked) {
            RADIO_BUTTON_SELECTED_ALPHA
        } else RADIO_BUTTON_UNSELECTED_ALPHA

        layoutParams = RelativeLayout.LayoutParams(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                28f,
                resources.displayMetrics
            ).toInt(),
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                30f,
                resources.displayMetrics
            ).toInt()
        )
    }

    private fun setRawData(rawData: PrimerRawData) = lifecycleScope.launch {
        rawDataFlow.emit(rawData)
    }

    private fun navigateToResultScreen() {
        headlessManagerViewModel.resetUiState()
        findNavController().navigate(
            R.id.action_MerchantRawFragment_to_MerchantResultFragment,
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
        private const val RADIO_BUTTON_SELECTED_ALPHA = 255
        private const val RADIO_BUTTON_UNSELECTED_ALPHA = 100

        const val PAYMENT_METHOD_TYPE_EXTRA = "PAYMENT_METHOD_TYPE"
    }
}