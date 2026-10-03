package com.muchen.virtualcall.domain.util

fun formatPhoneNumber(rawNumber: String): String {
    val trimmed = rawNumber.trim()
    val digits = trimmed.filter(Char::isDigit)
    val isChineseMobile = digits.length == 11 && digits.startsWith("1")
    if (isChineseMobile) {
        return "${digits.substring(0, 3)} ${digits.substring(3, 7)} ${digits.substring(7, 11)}"
    }
    return trimmed
}
