package com.muchen.virtualcall.domain.util

/**
 * 电话号码格式化：11 位国内手机号按 3-4-4 分段，其余原样返回。
 * 纯 domain 工具函数，无 Android 依赖。
 */
fun formatPhoneNumber(rawNumber: String): String {
    val digits = rawNumber.filter(Char::isDigit)
    if (digits.length == 11) {
        return "${digits.substring(0, 3)} ${digits.substring(3, 7)} ${digits.substring(7, 11)}"
    }
    return rawNumber
}
