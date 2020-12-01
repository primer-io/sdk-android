package io.primer.android.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
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

    viewModel.loading.observe(this, { loading ->
      // TODO: hide loading spinner + show UX
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
//
//    viewModel.sheetVisible.observe(this, { visible ->
//      if (!visible) {
//        log("Trying to stop activity")
//        activity?.finish()
//      }
//    })
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