package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.primer.android.payment.PAYMENT_CARD_IDENTIFIER
import io.primer.android.R
import io.primer.android.payment.TokenAttributes
import io.primer.android.ui.VaultedPaymentMethodView
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationViewModel
import io.primer.android.viewmodel.ViewStatus
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class VaultedPaymentMethodsFragment : Fragment() {

    private lateinit var viewModel: PrimerViewModel
    private lateinit var tokenizationViewModel: TokenizationViewModel

    // FIXME replace with view binding
    private lateinit var paymentMethodsLinearLayout: ViewGroup
    private lateinit var readOnlyHeaderLinearLayout: ViewGroup
    private lateinit var editHeaderLinearLayout: ViewGroup

    private val views = ArrayList<VaultedPaymentMethodView>()

    private var isEditing = false
        private set(value) {
            field = value

            views.forEach {
                it.setEditable(isEditing)
            }

            readOnlyHeaderLinearLayout.visibility = if (isEditing) View.GONE else View.VISIBLE
            editHeaderLinearLayout.visibility = if (isEditing) View.VISIBLE else View.GONE
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
    ): View? =
            inflater.inflate(R.layout.fragment_vaulted_payment_methods, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        paymentMethodsLinearLayout = view.findViewById(R.id.vaulted_payment_methods_list)
        readOnlyHeaderLinearLayout = view.findViewById(
            R.id.primer_view_vaulted_payment_methods_header
        )
        editHeaderLinearLayout = view.findViewById(R.id.primer_edit_vaulted_payment_methods_header)

        viewModel.vaultedPaymentMethods.observe(viewLifecycleOwner) { paymentMethods ->
            views.forEach { paymentMethodsLinearLayout.removeView(it.getView()) }
            views.clear()

            paymentMethods.forEach { paymentMethodToken ->
                val attributes = TokenAttributes.create(paymentMethodToken)

                attributes?.let { attrs ->
                    val paymentMethodView = VaultedPaymentMethodView(requireContext(), attrs)

                    paymentMethodView.setOnDeleteListener {
                        tokenizationViewModel.deleteToken(paymentMethodToken)
                    }

                    views.add(paymentMethodView)
                    paymentMethodView.setEditable(isEditing)
                    paymentMethodsLinearLayout.addView(paymentMethodView.getView())
                }
            }

            if (views.isEmpty()) {
                gotoSelectPaymentMethod()
            }
        }

        view.findViewById<View>(R.id.vaulted_payment_methods_go_back).setOnClickListener {
            gotoSelectPaymentMethod()
        }

        view.findViewById<View>(R.id.vaulted_payment_methods_add_card).setOnClickListener {
            // FIXME this is looking for a PAYMENT_CARD_IDENTIFIER method, if there is none (for ex.
            //  merchant has not set it up) it will do nothing
            viewModel.paymentMethods.value?.find { it.identifier == PAYMENT_CARD_IDENTIFIER }?.let {
                viewModel.selectPaymentMethod(it)
            }
        }

        view.findViewById<View>(R.id.edit_vaulted_payment_methods).setOnClickListener {
            isEditing = true
        }

        view.findViewById<View>(R.id.edit_vaulted_payment_methods_go_back).setOnClickListener {
            isEditing = false
        }
        view.findViewById<View>(R.id.edit_vaulted_payment_methods_done).setOnClickListener {
            isEditing = false
        }
    }

    private fun gotoSelectPaymentMethod() {
        viewModel.viewStatus.value = ViewStatus.SELECT_PAYMENT_METHOD
    }

    companion object {

        fun newInstance(): VaultedPaymentMethodsFragment {
            return VaultedPaymentMethodsFragment()
        }
    }
}
