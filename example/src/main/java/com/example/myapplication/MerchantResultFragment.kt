package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentCheckoutResultBinding
import org.json.JSONArray

class MerchantResultFragment : Fragment() {

    private var _binding: FragmentCheckoutResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCheckoutResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.status.text =
            PaymentStatus.values()[requireArguments().getInt(PAYMENT_STATUS_KEY)].name
        val callbacks = requireArguments().getStringArrayList(INVOKED_CALLBACKS_KEY)
        binding.logs.text = JSONArray(callbacks).toString()
        binding.paymentResponse.text = requireArguments().getString(PAYMENT_RESPONSE_KEY)
    }

    companion object {

        enum class PaymentStatus {
            SUCCESS, FAILURE
        }

        const val PAYMENT_STATUS_KEY = "PAYMENT_STATUS"
        const val INVOKED_CALLBACKS_KEY = "INVOKED_CALLBACKS"
        const val PAYMENT_RESPONSE_KEY = "PAYMENT_RESPONSE"
    }
}
