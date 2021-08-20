package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentSecondBinding
import com.example.myapplication.models.TransactionState
import com.xwray.groupie.GroupieAdapter
import io.primer.android.UniversalCheckout
import io.primer.android.model.PrimerDebugOptions
import io.primer.android.model.dto.PaymentMethodToken
import io.primer.android.threeds.data.models.ResponseCode

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppMainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchClientToken()

        binding.vaultButton.isVisible = false
        binding.checkoutButton.isVisible = false

        binding.vaultButton.setOnClickListener {
            activity?.let { activityContext ->
                UniversalCheckout.showVault(
                    activityContext,
                    viewModel.listener,
                    viewModel.amount.value,
                    viewModel.countryCode.value?.currencyCode.toString(),
                    is3DSAtTokenizationEnabled = viewModel.threeDsEnabled.value ?: false,
                    debugOptions = PrimerDebugOptions(is3DSSanityCheckEnabled = false),
                    isStandalonePaymentMethod = viewModel.isStandalonePaymentMethod,
                    preferWebView = true,
                    webBrowserRedirectScheme = "primer",
                    clearAllListeners = true,
                    orderId = viewModel.orderId.value,
                    userDetails = viewModel.userDetails.value
                )
            }
        }

        binding.checkoutButton.setOnClickListener {
            activity?.let { activityContext ->
                UniversalCheckout.showCheckout(
                    activityContext,
                    viewModel.listener,
                    viewModel.amount.value,
                    viewModel.countryCode.value?.currencyCode.toString(),
                    is3DSAtTokenizationEnabled = viewModel.threeDsEnabled.value ?: false,
                    debugOptions = PrimerDebugOptions(is3DSSanityCheckEnabled = false),
                    isStandalonePaymentMethod = viewModel.isStandalonePaymentMethod,
                    preferWebView = true,
                    clearAllListeners = true,
                    webBrowserRedirectScheme = "primer",
                    orderId = viewModel.orderId.value,
                    userDetails = viewModel.userDetails.value
                )
            }
        }

        viewModel.clientToken.observe(viewLifecycleOwner) { token ->
            if (viewModel.vaultDisabled) {
                binding.vaultButton.isVisible = false
            } else {
                binding.vaultButton.isVisible = token != null
            }

            binding.checkoutButton.isVisible = token != null

            if (token != null) {
                initializeCheckoutWith(token)
                UniversalCheckout.loadPaymentMethods(viewModel.generatePaymentMethodList())
                viewModel.fetchSavedPaymentMethods()
            }
        }

        viewModel.isBusy.observe(viewLifecycleOwner) { isBusy ->
            binding.walletProgressBar.isVisible = isBusy
        }

        viewModel.transactionState.observe(viewLifecycleOwner) { state ->
            val message = when (state) {
                TransactionState.SUCCESS -> requireContext().getString(R.string.success_text)
                TransactionState.ERROR -> requireContext().getString(R.string.something_went_wrong)
                else -> return@observe
            }
            UniversalCheckout.dismiss()
            AlertDialog.Builder(context).setMessage(message).show()
            viewModel.resetTransactionState()
        }

        viewModel.vaultedPaymentTokens.observe(viewLifecycleOwner) {
            val adapter = GroupieAdapter()
            it.forEach { t -> adapter.add(PaymentMethodItem(t, ::onSelect)) }
            binding.paymentMethodList.adapter = adapter
        }

        viewModel.threeDsResult.observe(viewLifecycleOwner) {
            val message = it?.let {
                when (it.responseCode) {
                    ResponseCode.AUTH_SUCCESS -> getString(R.string.three_ds_success_message)
                    else -> getString(R.string.three_ds_error_message,
                                      it.responseCode,
                                      it.reasonCode,
                                      it.reasonText)
                }
            }
            message?.let {
                AlertDialog.Builder(context).setMessage(it).show()
                viewModel.clearThreeDsResult()
            }
        }
    }

    private fun initializeCheckoutWith(token: String) {
        activity?.let {
            UniversalCheckout.initialize(it, token)
        }
    }

    private fun onConfirmDialogAction(token: PaymentMethodToken) {
        viewModel.createTransaction(token.token, token.paymentInstrumentType)
    }

    private fun onSelect(token: PaymentMethodToken) {
        AlertDialog.Builder(context)
            .setTitle("Payment confirmation")
            .setMessage("Would you like to pay?")
            .setPositiveButton("Yes") { _, _ -> onConfirmDialogAction(token) }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("second fragment 🔥", "🔥")
    }
}
