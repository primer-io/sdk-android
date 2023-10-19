package io.primer.sample.nolpay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import io.primer.android.components.manager.nolPay.PrimerHeadlessUniversalCheckoutNolPayManager
import io.primer.nolpay.api.models.PrimerNolPaymentCard
import io.primer.sample.R
import io.primer.sample.databinding.FragmentNolPayBinding
import kotlinx.coroutines.launch

class NolFragment : Fragment() {

    private lateinit var binding: FragmentNolPayBinding
    private lateinit var manager: PrimerHeadlessUniversalCheckoutNolPayManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentNolPayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manager = PrimerHeadlessUniversalCheckoutNolPayManager()

        binding.unlinkNolCard.setOnClickListener {
            findNavController().navigate(R.id.action_NolFragment_to_NolUnlinkFragment,
                Bundle().apply {
                    putSerializable(
                        NOL_CARD_KEY,
                        binding.linkedCards.children.find { view ->
                            view.id ==
                                binding.linkedCards.checkedRadioButtonId
                        }?.tag as PrimerNolPaymentCard
                    )
                })
        }

        binding.addNewNolCardButton.setOnClickListener {
            findNavController().navigate(R.id.action_NolFragment_to_NolLinkFragment)
        }

        binding.payWithNolCard.setOnClickListener {
            findNavController().navigate(R.id.action_NolFragment_to_NolPayPaymentFragment,
                Bundle().apply {
                    putSerializable(
                        NOL_CARD_KEY,
                        binding.linkedCards.children.find { view ->
                            view.id ==
                                binding.linkedCards.checkedRadioButtonId
                        }?.tag as PrimerNolPaymentCard
                    )
                })
        }

        binding.getLinkedCards.setOnClickListener {
            getLinkedCards(
                binding.mobileNumber.text.toString(),
                binding.mobileCountryCode.text.toString()
            )
        }
    }

    private fun getLinkedCards(mobileNumber: String, diallingCode: String) {
        lifecycleScope.launch {
            binding.linkedCards.removeAllViews()
            manager.provideNolPayLinkedCardsComponent().getLinkedCards(mobileNumber, diallingCode)
                .onSuccess { cards ->
                    cards.forEach { card ->
                        binding.linkedCards.apply {
                            addView(RadioButton(requireContext()).apply {
                                text = card.cardNumber
                                id = card.cardNumber.toInt()
                                tag = card
                            })
                            setOnCheckedChangeListener { _, _ ->
                                binding.payWithNolCard.isVisible = true
                                binding.unlinkNolCard.isVisible = true
                            }
                        }
                    }
                }.onFailure {
                    Snackbar.make(requireView(), it.message.orEmpty(), Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    companion object {

        const val NOL_CARD_KEY = "NOL_CARD"
    }
}
