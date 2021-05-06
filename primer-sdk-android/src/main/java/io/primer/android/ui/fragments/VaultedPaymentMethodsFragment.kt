package io.primer.android.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.primer.android.R
import io.primer.android.model.dto.PaymentMethodTokenInternal
import io.primer.android.ui.ApmData
import io.primer.android.ui.CardData
import io.primer.android.ui.PaymentMethodItemData
import io.primer.android.ui.VaultedPaymentMethodRecyclerAdapter
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationViewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class VaultedPaymentMethodsFragment : Fragment() {

    private lateinit var viewModel: PrimerViewModel
    private lateinit var tokenizationViewModel: TokenizationViewModel

    // FIXME replace with view binding
    private lateinit var readOnlyHeaderLinearLayout: ViewGroup
    private lateinit var editHeaderLinearLayout: ViewGroup

    private var paymentMethods: List<PaymentMethodTokenInternal> = listOf()

    private var isEditing = false
        private set(value) {
            field = value

            adapter.setEditingStatusAs(isEditing)

            view?.findViewById<TextView>(R.id.vault_title_label)?.apply {
                text = if (isEditing) getString(R.string.edit_saved_payment_methods)
                else getString(R.string.other_ways_to_pay)
            }

            view?.findViewById<TextView>(R.id.edit_vaulted_payment_methods)?.text =
                if (isEditing) getString(R.string.cancel) else getString(R.string.edit)
        }

    private var adapter: VaultedPaymentMethodRecyclerAdapter = VaultedPaymentMethodRecyclerAdapter(
        ::onSetSelectedWith,
        ::onDeleteSelectedWith,
    )

    private fun configureRecyclerView(view: View, paymentMethods: List<PaymentMethodItemData>) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.vault_recycler_view)
        val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!)
        recyclerView.addItemDecoration(itemDecorator)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter.updateDataWith(paymentMethods)
        adapter.setSelectedIdWith(viewModel.getSelectedPaymentMethodId())
        recyclerView.adapter = adapter
    }

    private fun generateItemDataFromPaymentMethods(
        paymentMethods: List<PaymentMethodTokenInternal>
    ): List<PaymentMethodItemData> {
        return paymentMethods.map {
            when (it.paymentInstrumentType) {
                "KLARNA_CUSTOMER_TOKEN" -> {
                    val email = it.paymentInstrumentData?.sessionData?.billingAddress?.email
                    ApmData(email ?: "Klarna Payment Method", it.token)
                }
                "GOCARDLESS_MANDATE" -> {
                    ApmData("Direct Debit Mandate", it.token)
                }
                "PAYMENT_CARD" -> {
                    CardData(
                        it.paymentInstrumentData?.cardholderName ?: "unknown",
                        it.paymentInstrumentData?.last4Digits ?: 1234,
                        it.paymentInstrumentData?.expirationMonth ?: 1,
                        it.paymentInstrumentData?.expirationYear ?: 2021,
                        it.paymentInstrumentData?.network ?: "unknown",
                        it.token,
                    )
                }
                else -> {
                    ApmData("title", it.token)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = PrimerViewModel.getInstance(requireActivity())
        tokenizationViewModel = TokenizationViewModel.getInstance(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_vaulted_payment_methods, container, false)

    private fun onSetSelectedWith(id: String) = viewModel.setSelectedPaymentMethodId(id)

    private fun onDeleteSelectedWith(id: String) {
        val dialog = AlertDialog.Builder(view?.context, R.style.Primer_AlertDialog)
            .setTitle("Are you sure you want to delete this card?")
            // positive button as delete since it defaults to right
            .setPositiveButton("Delete") { dialog, _ ->
                // delete payment method from vault then dismiss
                val methodToBeDeleted = paymentMethods.find {
                    it.token == id
                }

                if (methodToBeDeleted == null) dialog.dismiss()
                else tokenizationViewModel.deleteToken(methodToBeDeleted)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
        dialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        readOnlyHeaderLinearLayout = view.findViewById(
            R.id.primer_view_vaulted_payment_methods_header
        )

        editHeaderLinearLayout = view.findViewById(R.id.primer_edit_vaulted_payment_methods_header)

        viewModel.vaultedPaymentMethods.observe(viewLifecycleOwner) { data ->

            // return to checkout view if no saved payment methods
            if (data.isEmpty()) {
                viewModel.goToSelectPaymentMethodsView()
            }

            paymentMethods = data

            configureRecyclerView(view, generateItemDataFromPaymentMethods(paymentMethods))
        }

        view.findViewById<View>(R.id.vaulted_payment_methods_go_back).setOnClickListener {
            viewModel.goToSelectPaymentMethodsView()
        }

        view.findViewById<Button>(R.id.edit_vaulted_payment_methods).setOnClickListener {
            isEditing = !isEditing
        }
    }

    companion object {

        fun newInstance(): VaultedPaymentMethodsFragment {
            return VaultedPaymentMethodsFragment()
        }
    }
}
