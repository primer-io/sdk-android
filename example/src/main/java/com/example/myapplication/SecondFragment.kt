package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.myapplication.components.PaymentMethodItem
import com.example.myapplication.datamodels.TransactionState
import com.example.myapplication.viewmodels.MainViewModel
import io.primer.android.Primer
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentSecondBinding
import com.xwray.groupie.GroupieAdapter
import io.primer.android.CheckoutEventListener
import io.primer.android.PaymentMethodIntent
import io.primer.android.completion.CheckoutErrorHandler
import io.primer.android.completion.PaymentCreationDecisionHandler
import io.primer.android.completion.ResumeDecisionHandler
import io.primer.android.domain.CheckoutData
import io.primer.android.domain.action.models.ClientSession
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.tokenization.models.PaymentMethodData
import io.primer.android.model.dto.*
import io.primer.android.model.dto.PaymentMethodToken
import io.primer.android.threeds.data.models.ResponseCode

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vaultButton.isVisible = false
        binding.checkoutButton.isVisible = false
        binding.showPaymentMethodButton.isVisible = false

        // VAULT MANAGER
        binding.vaultButton.setOnClickListener {
            viewModel.mode.postValue(MainViewModel.Mode.VAULT)
            viewModel.clientToken.value?.let { token ->
                activity?.let { context -> Primer.instance.showVaultManager(context, token) }
            }
        }

        // UNIVERSAL CHECKOUT
        binding.checkoutButton.setOnClickListener {
            viewModel.mode.postValue(MainViewModel.Mode.CHECKOUT)
            viewModel.clientToken.value?.let { token ->
                activity?.let { context -> Primer.instance.showUniversalCheckout(context, token) }
            }
        }

        // SHOW PAYMENT METHOD
        binding.showPaymentMethodButton.setOnClickListener {
            viewModel.clientToken.value?.let { token ->
                activity?.let { context ->
                    Primer.instance.showPaymentMethod(
                        context,
                        token,
                        viewModel.useStandalonePaymentMethod.value!!,
                        PaymentMethodIntent.CHECKOUT
                    )
                }
            }
        }

        viewModel.postalCode.observe(viewLifecycleOwner) { value ->
            binding.postalCodeLabel.text = requireContext().getString(R.string.postal_code, value)
        }

        viewModel.clientToken.observe(viewLifecycleOwner) { token ->
            if (viewModel.vaultDisabled) {
                binding.vaultButton.isVisible = false
            } else {
                binding.vaultButton.isVisible = token != null
            }

            binding.checkoutButton.isVisible = token != null
            binding.showPaymentMethodButton.isVisible =
                token != null && viewModel.isStandalonePaymentMethod

            if (token != null) {
                initializeCheckout()
                fetchSavedPaymentMethods()
            }
        }

        viewModel.transactionState.observe(viewLifecycleOwner) { state ->
            val message = when (state) {
                TransactionState.SUCCESS -> viewModel.transactionResponse.value.toString()
                TransactionState.ERROR -> requireContext().getString(R.string.something_went_wrong)
                else -> return@observe
            }
            AlertDialog.Builder(context).setMessage(message).show()
            viewModel.resetTransactionState()
        }

        viewModel.paymentInstruments.observe(viewLifecycleOwner) { data ->
            setBusy(false)
            val adapter = GroupieAdapter()
            data.iterator().forEach { t -> adapter.add(PaymentMethodItem(t, ::onSelect)) }
            binding.paymentMethodList.adapter = adapter
        }

        viewModel.threeDsResult.observe(viewLifecycleOwner) {
            val message = it?.let {
                when (it.responseCode) {
                    ResponseCode.AUTH_SUCCESS -> getString(R.string.three_ds_success_message)
                    else -> getString(
                        R.string.three_ds_error_message,
                        it.responseCode,
                        it.reasonCode,
                        it.reasonText
                    )
                }
            }
            message?.let {
                AlertDialog.Builder(context).setMessage(it).show()
                viewModel.clearThreeDsResult()
            }
        }

        viewModel.fetchClientSession()
    }

    private fun initializeCheckout() = viewModel.configure(listener)

    private val listener = object : CheckoutEventListener {

        override fun onBeforePaymentCreated(
            paymentMethodData: PaymentMethodData,
            createPaymentHandler: PaymentCreationDecisionHandler
        ) {
            createPaymentHandler.continuePaymentCreation()
            Log.d(TAG, "onBeforePaymentCreated with $paymentMethodData")
        }

        override fun onFailed(
            error: PrimerError,
            checkoutData: CheckoutData?,
            errorHandler: CheckoutErrorHandler?
        ) {
            AlertDialog.Builder(context).setMessage("onFailed $error with data $checkoutData")
                .show()
            errorHandler?.showErrorMessage("Something went wrong!")
        }

        override fun onCheckoutCompleted(checkoutData: CheckoutData) {
            AlertDialog.Builder(context).setMessage("onCheckoutCompleted $checkoutData").show()
        }

        override fun onBeforeClientSessionUpdated() {
            Log.d(TAG, "onBeforeClientSessionUpdated")
        }

        override fun onClientSessionUpdated(clientSession: ClientSession) {
            super.onClientSessionUpdated(clientSession)
            Log.d(TAG, "onClientSessionUpdated with result $clientSession")
        }

        override fun onFailed(error: PrimerError, errorHandler: CheckoutErrorHandler?) {
            AlertDialog.Builder(context).setMessage("onFailed $error")
                .show()
            errorHandler?.showErrorMessage("Something went wrong!")
        }

        override fun onTokenizeSuccess(
            paymentMethodToken: PaymentMethodToken,
            resumeHandler: ResumeDecisionHandler
        ) {
            Log.d(TAG, "onTokenizeSuccess")
            viewModel.createPayment(paymentMethodToken, resumeHandler)
        }

        override fun onTokenAddedToVault(paymentMethodToken: PaymentMethodToken) {
            super.onTokenAddedToVault(paymentMethodToken)
            Log.d(TAG, "onTokenAddedToVault")
        }

        override fun onResume(resumeToken: String, resumeHandler: ResumeDecisionHandler) {
            Log.d(TAG, "onResumeSuccess")
            viewModel.resumePayment(resumeToken, resumeHandler)
        }

        override fun onCheckoutDismissed(reason: CheckoutExitReason) {
            Log.d(TAG, "onCheckoutDismissed")
            viewModel.clientToken.value?.let { _ -> fetchSavedPaymentMethods() }
        }
    }

    private fun onConfirmDialogAction(token: PaymentMethodToken) {
        viewModel.createPayment(token)
    }

    private fun onSelect(token: PaymentMethodToken) {
        AlertDialog.Builder(context)
            .setTitle("Payment confirmation")
            .setMessage("Would you like to pay?")
            .setPositiveButton("Yes") { _, _ -> onConfirmDialogAction(token) }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setBusy(isBusy: Boolean) {
        binding.walletProgressBar.isVisible = isBusy
    }

    private fun fetchSavedPaymentMethods() {
        activity?.runOnUiThread { setBusy(true) }
        viewModel.fetchPaymentInstruments()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Primer.instance.cleanup()
        Log.i("second fragment 🔥", "🔥")
    }

    private companion object {
        const val TAG = "CheckoutEventListener"
    }
}