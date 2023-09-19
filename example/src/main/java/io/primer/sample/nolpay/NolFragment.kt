package io.primer.sample.nolpay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import io.primer.android.components.manager.nolPay.PrimerHeadlessUniversalCheckoutNolPayManager
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
            findNavController().navigate(R.id.action_NolFragment_to_NolUnlinkFragment)
        }

        binding.addNewNolCardButton.setOnClickListener {
            findNavController().navigate(R.id.action_NolFragment_to_NolLinkFragment)
        }

        binding.payWithNolCard.setOnClickListener {
            findNavController().navigate(R.id.action_NolFragment_to_NolPayPaymentFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            manager.provideNolPayLinkedCardsComponent().getLinkedCards("62250843", "387")
                .onSuccess { cards ->
                    cards.forEach { card ->
                        binding.linkedCards.apply {
                            addView(RadioButton(requireContext()).apply {
                                text = card.cardNumber
                                id = card.cardNumber.toInt()
                            })
                            setOnCheckedChangeListener { group, checkedId ->
                                binding.unlinkNolCard.isVisible = true
                            }
                        }
                    }
                }
        }
    }
}