package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import io.primer.android.PAYMENT_CARD_IDENTIFIER
import io.primer.android.R
import io.primer.android.payment.PAYMENT_CARD_TYPE
import io.primer.android.payment.TokenAttributes
import io.primer.android.ui.SavedPaymentMethodView
import io.primer.android.ui.VaultedPaymentMethodView
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.ViewStatus
import java.util.*
import kotlin.collections.ArrayList

class VaultedPaymentMethodsFragment: Fragment() {
  private lateinit var viewModel: PrimerViewModel
  private lateinit var list: ViewGroup
  private lateinit var readOnlyHeader: ViewGroup
  private lateinit var editHeader: ViewGroup
  private val views = ArrayList<VaultedPaymentMethodView>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewModel = PrimerViewModel.getInstance(requireActivity())
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_vaulted_payment_methods, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    list = view.findViewById(R.id.vaulted_payment_methods_list)
    readOnlyHeader = view.findViewById(R.id.primer_view_vaulted_payment_methods_header)
    editHeader = view.findViewById(R.id.primer_edit_vaulted_payment_methods_header)

    viewModel.vaultedPaymentMethods.observe(viewLifecycleOwner, { paymentMethods ->
      paymentMethods.forEach { pm ->
        val attributes = TokenAttributes.create(pm)

        attributes?.let { attrs ->
          val pmView = VaultedPaymentMethodView(requireContext(), attrs)

          views.add(pmView)
          list.addView(pmView.getView())
        }
      }
    })

    view.findViewById<View>(R.id.vaulted_payment_methods_go_back).setOnClickListener {
      viewModel.viewStatus.value = ViewStatus.SELECT_PAYMENT_METHOD
    }

    view.findViewById<View>(R.id.vaulted_payment_methods_add_card).setOnClickListener {
      viewModel.paymentMethods.value?.find { it.identifier == PAYMENT_CARD_IDENTIFIER }?.let {
        viewModel.setSelectedPaymentMethod(it)
      }
    }

    view.findViewById<View>(R.id.edit_vaulted_payment_methods).setOnClickListener {
      setEditing(true)
    }

    // TODO: should these do something different?
    view.findViewById<View>(R.id.edit_vaulted_payment_methods_go_back).setOnClickListener {
      setEditing(false)
    }
    view.findViewById<View>(R.id.edit_vaulted_payment_methods_done).setOnClickListener {
      setEditing(false)
    }
  }

  private fun setEditing(isEditing: Boolean) {
    views.forEach {
      it.setEditable(isEditing)
    }
    readOnlyHeader.visibility = if (isEditing) View.GONE else View.VISIBLE
    editHeader.visibility = if (isEditing) View.VISIBLE else View.GONE
  }

  companion object {
    fun newInstance() : VaultedPaymentMethodsFragment {
      return VaultedPaymentMethodsFragment()
    }
  }
}