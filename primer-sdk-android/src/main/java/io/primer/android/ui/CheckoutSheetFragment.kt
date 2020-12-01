package io.primer.android.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.ViewStatus

class CheckoutSheetFragment : BottomSheetDialogFragment() {
  private val log = Logger("checkout-fragment")
  private lateinit var viewModel: PrimerViewModel

  @SuppressLint("RestrictedApi")
  override fun setupDialog(dialog: Dialog, style: Int) {
    super.setupDialog(dialog, style)

    dialog.setCanceledOnTouchOutside(false)
    (dialog as BottomSheetDialog).behavior.isHideable = false
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    viewModel = ViewModelProvider(this.requireActivity()).get(PrimerViewModel::class.java)

    viewModel.viewStatus.observe(this, { status ->
      when (status) {
        ViewStatus.SELECT_PAYMENT_METHOD -> transition(R.id.screen_initializing, R.id.screen_select_payment_method)
        ViewStatus.PAYMENT_METHOD_SELECTED -> transition(R.id.screen_select_payment_method, R.id.screen_payment_method_form)
        null -> {}
      }
    })

    viewModel.paymentMethods.observe(this, { items ->
      val container: ViewGroup = findViewById(R.id.primer_sheet_payment_methods_list)

      items.forEach { pm ->
        val button = pm.createButton(container)

        button.setOnClickListener {
          viewModel.setSelectedPaymentMethod(pm)
        }
      }
    })

    viewModel.amount.observe(this, {
      findViewById<SelectPaymentMethodTitle>(R.id.primer_sheet_title_layout).setAmount(it)
    })

    viewModel.uxMode.observe(this, {
      findViewById<SelectPaymentMethodTitle>(R.id.primer_sheet_title_layout).setUXMode(it)
    })
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.activity_checkout_sheet, container, false)
  }

  override fun onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    viewModel.setSheetDismissed(true)
  }

  private fun transition(from: Int, to: Int) {
    val fromView = findViewById<View>(from)
    val toView = findViewById<View>(to)

    fromView.visibility = View.GONE
    toView.visibility = View.VISIBLE
  }

  private fun <T: View> findViewById(id: Int): T {
    return requireView().findViewById(id)
  }

  companion object {
    @JvmStatic
    fun newInstance(bundle: Bundle): CheckoutSheetFragment {
      val fragment = CheckoutSheetFragment()
      fragment.arguments = bundle
      return fragment
    }
  }
}