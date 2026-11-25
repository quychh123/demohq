package com.shopcore.ui

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShopConstants {
  val WARRANTY_OPTIONS =
    listOf(
      "Bao test 7 ngày",
      "Bảo hành 1 tháng",
      "Bảo hành 2 tháng",
      "Bảo hành 3 tháng",
      "Không bảo hành",
    )

  val REPAIR_STATUSES = listOf("Đang nhận", "Đang sửa", "Hoàn tất")

  val GOOD_SUBCATEGORIES = listOf("Phụ kiện", "Cường lực", "Pin", "Cáp sạc")

  fun groupForSubCategory(subCategory: String): String =
    when (subCategory) {
      "Pin", "Cáp sạc" -> "Linh kiện"
      "Cường lực" -> "Cường lực"
      else -> "Phụ kiện"
    }
}

private val vnLocale = Locale.forLanguageTag("vi-VN")

private val vnSymbols =
  DecimalFormatSymbols(vnLocale).apply {
    groupingSeparator = '.'
    decimalSeparator = ','
  }

private val numberFormatter = DecimalFormat("#,###", vnSymbols)
private val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy · HH:mm", vnLocale)

fun formatVnd(amount: Long): String = "${numberFormatter.format(amount)} ₫"

fun formatCompactVnd(amount: Long): String {
  val abs = kotlin.math.abs(amount)
  val sign = if (amount < 0) "-" else ""
  return when {
    abs >= 1_000_000_000L -> "$sign${String.format(Locale.US, "%.2f Tỷ", abs / 1_000_000_000.0)}"
    abs >= 1_000_000L -> "$sign${String.format(Locale.US, "%.1f Tr", abs / 1_000_000.0)}"
    else -> formatVnd(amount)
  }
}

fun formatShortDateTime(millis: Long): String = dateTimeFormatter.format(Date(millis))
