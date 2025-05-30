package io.primer.composable.internal.domain.models

import io.primer.android.configuration.data.model.CardNetwork

internal data class Card(
    val number: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cvv: String,
    val holderName: String,
    val network: CardNetwork.Type? = null
) {
    companion object {
        fun empty() = Card(
            number = "",
            expiryMonth = 0,
            expiryYear = 0,
            cvv = "",
            holderName = "",
            network = null
        )
    }
    
    fun isValid(): Boolean {
        return isValidCardNumber() && 
               isValidExpiry() && 
               isValidCvv() && 
               holderName.isNotBlank()
    }
    
    private fun isValidCardNumber(): Boolean {
        return number.replace(" ", "").length >= 13 && 
               number.replace(" ", "").all { it.isDigit() }
    }
    
    private fun isValidExpiry(): Boolean {
        return expiryMonth in 1..12 && 
               expiryYear >= getCurrentYear() &&
               !(expiryYear == getCurrentYear() && expiryMonth < getCurrentMonth())
    }
    
    private fun isValidCvv(): Boolean {
        return cvv.length in 3..4 && cvv.all { it.isDigit() }
    }
    
    private fun getCurrentYear(): Int {
        return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    }
    
    private fun getCurrentMonth(): Int {
        return java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
    }
}
