package util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object Money {
    private val nf = NumberFormat.getCurrencyInstance(Locale.UK)
    fun f(v: Double): String = nf.format(kotlin.math.round(v * 100.0)/100.0)
}

object Dates {
    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE
    fun parse(s: String): LocalDate = LocalDate.parse(s, fmt)
    fun format(d: LocalDate): String = d.format(fmt)
}
