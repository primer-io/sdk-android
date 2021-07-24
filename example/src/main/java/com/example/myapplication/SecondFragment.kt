package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlin.concurrent.schedule
import com.example.myapplication.databinding.FragmentSecondBinding
import com.xwray.groupie.GroupieAdapter
import io.primer.android.CheckoutEventListener
import io.primer.android.UniversalCheckout
import io.primer.android.events.CheckoutEvent
import io.primer.android.model.dto.CheckoutExitReason
import io.primer.android.model.dto.PaymentMethodToken
import io.primer.android.payment.card.Card
import io.primer.android.payment.klarna.Klarna
import io.primer.android.payment.paypal.PayPal
import io.primer.android.ui.fragments.ErrorType
import java.lang.IllegalArgumentException
import java.util.Timer

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AppMainViewModel

    private var amount: Int = 1000
    private var currency: String = "SEK"

    private val card = Card()
    private val paypal = PayPal()
    private val klarna = Klarna(webViewTitle = "Add Klarna 💰")

    // private val locale = Locale("se", "SE")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(AppMainViewModel::class.java)
    }

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
        binding.klarnaButton.isVisible = false

        binding.vaultButton.setOnClickListener {
            activity?.let {
                UniversalCheckout.showVault(
                    it, 
                    listener,
                    preferWebView = true,
                    clearAllListeners = true,
                )
            }
        }

        binding.klarnaButton.setOnClickListener {
            activity?.let {
                UniversalCheckout.loadPaymentMethods(listOf(klarna))
                setBusyAs(true)
                UniversalCheckout.showVault(
                    it,
                    listener,
                    preferWebView = true,
                    isStandalonePaymentMethod = true,
                    doNotShowUi = true,
                    clearAllListeners = true,
                )
            }
        }

        binding.checkoutButton.setOnClickListener {
            activity?.let {
                UniversalCheckout
                    .showCheckout(it, listener, amount, currency, clearAllListeners = true)
            }
        }

        viewModel.clientToken.observe(viewLifecycleOwner) { token ->
            binding.vaultButton.isVisible = token != null
            binding.checkoutButton.isVisible = token != null
            binding.klarnaButton.isVisible = token != null

            if (token != null) {
                initializeCheckoutWith(token)
                UniversalCheckout.loadPaymentMethods(listOf(klarna, card, paypal))
                fetchSavedPaymentMethods()
            }
        }

        viewModel.transactionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                TransactionState.SUCCESS -> {
                    UniversalCheckout.dismiss()
                    AlertDialog.Builder(context)
                        .setMessage("Payment successful!")
                        .show()
                    setBusyAs(false)
                    viewModel.resetTransactionState()
                }
                TransactionState.IDLE -> {
                }
                TransactionState.ERROR -> {
                    AlertDialog.Builder(context)
                        .setMessage("Something went wrong, please try a different payment method.")
                        .show()
                    setBusyAs(false)
                    viewModel.resetTransactionState()
                }

                else -> {
                }
            }
        }
    }

    private fun initializeCheckoutWith(token: String) {
        activity?.let {
            UniversalCheckout.initialize(it, token)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setBusyAs(isBusy: Boolean) {
        binding.walletProgressBar.isVisible = isBusy
    }

    private val listener = object : CheckoutEventListener {
        override fun onCheckoutEvent(e: CheckoutEvent) {
            when (e) {
                is CheckoutEvent.TokenizationSuccess -> {
                    e.completionHandler(null)
                }
                is CheckoutEvent.TokenAddedToVault -> {
//                    UniversalCheckout.dismiss()
//                    Handler(Looper.getMainLooper()).post {
//                        UniversalCheckout.showSuccess(
//                            autoDismissDelay = 10000,
//                            SuccessType.VAULT_TOKENIZATION_SUCCESS,
//                        )
//                    }
                }
                is CheckoutEvent.ApiError -> {
//                    UniversalCheckout.dismiss()
                    AlertDialog.Builder(context)
                        .setMessage("Something went wrong!")
                        .show()
//                    UniversalCheckout.showError(
//                        autoDismissDelay = 10000,
//                        ErrorType.VAULT_TOKENIZATION_FAILED,
//                    )
                }
                is CheckoutEvent.Exit -> {

                    if (e.data.reason == CheckoutExitReason.EXIT_SUCCESS) {
                        Log.i("ExampleApp", "Awesome")
                    }

                    fetchSavedPaymentMethods()

                    // Timer("SettingUp", false).schedule(500L) {
                    //     activity?.runOnUiThread {
                    //         val nav = findNavController()
                    //         nav.popBackStack()
                    //     }
                    // }
                }
                is CheckoutEvent.TokenSelected -> {
                    UniversalCheckout.dismiss(true)
//                    viewModel.createTransaction(
//                        e.data.token,
//                        amount,
//                        true,
//                        currency,
//                        e.data.paymentInstrumentType,
//                    )
                }
                else -> {
                }
            }
        }
    }

    private fun onSelect(token: PaymentMethodToken) {
        AlertDialog.Builder(context)
            .setTitle("Payment confirmation")
            .setMessage("Would you like to pay?")
            .setPositiveButton("Yes") { dialog, which ->
                setBusyAs(true)
                viewModel.createTransaction(
                    token.token,
                    amount,
                    true,
                    currency,
                    token.paymentInstrumentType,
                )
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    internal fun fetchSavedPaymentMethods() {

        activity?.runOnUiThread { setBusyAs(true) }

        UniversalCheckout.getSavedPaymentMethods {
            activity?.runOnUiThread {
                setBusyAs(false)
                val adapter = GroupieAdapter()
                it.forEach { t ->
                    adapter.add(PaymentMethodItem(t, ::onSelect))
                }
                binding.paymentMethodList.adapter = adapter
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("second fragment 🔥", "🔥")
    }
}
