package com.akslabs.circletosearch.utils

import java.util.Locale

object CurrencyConverter {
    // Supported symbols and 3-letter codes
    private val currencySymbols = "[$€£¥₹]"
    private val currencyCodes = "USD|EUR|GBP|JPY|INR|AED|SAR|CAD|AUD|KWD|QAR|CNY|MYR|SGD|TRY"
    
    // Regex matches: 
    // 1. Symbol/Code before number: "$ 50" or "USD 50"
    // 2. Number before Symbol/Code: "50 $" or "50 USD"
    private val currencyRegex = Regex(
        """(?i)(?:($currencySymbols|$currencyCodes)\s*(\d+(?:[.,]\d+)?))|(?:(\d+(?:[.,]\d+)?)\s*($currencySymbols|$currencyCodes))"""
    )
    
    private val toPkrRates = mapOf(
        "$" to 278.50, "USD" to 278.50,
        "€" to 302.15, "EUR" to 302.15,
        "£" to 354.40, "GBP" to 354.40,
        "¥" to 1.85,   "JPY" to 1.85, "CNY" to 39.20,
        "₹" to 3.32,   "INR" to 3.32,
        "AED" to 75.82,
        "SAR" to 74.25,
        "CAD" to 202.40,
        "AUD" to 182.10,
        "KWD" to 910.50,
        "QAR" to 76.45,
        "MYR" to 62.15,
        "SGD" to 208.30,
        "TRY" to 8.45
    )

    fun convertToPkr(text: String): String? {
        val match = currencyRegex.find(text) ?: return null
        
        val rawSymbolOrCode: String
        val amountStr: String
        
        if (match.groups[1] != null) {
            rawSymbolOrCode = match.groups[1]?.value ?: ""
            amountStr = match.groups[2]?.value ?: ""
        } else if (match.groups[4] != null) {
            amountStr = match.groups[3]?.value ?: ""
            rawSymbolOrCode = match.groups[4]?.value ?: ""
        } else {
            return null
        }

        val symbolOrCode = rawSymbolOrCode.uppercase()
        val amount = amountStr.replace(",", ".").toDoubleOrNull() ?: return null
        val rate = toPkrRates[symbolOrCode] ?: return null
        val pkrAmount = amount * rate
        
        return "≈ ₨ ${String.format(Locale.US, "%,.0f", pkrAmount)}"
    }
}
