package ttm.domain

import java.time.LocalDate

enum class TicketType { SINGLE, RETURN }

data class Station(
    val id: Long,
    val name: String,
    val singlePrice: Double,
    val returnPrice: Double,
    val salesCount: Int
)

data class Card(
    val cardNumber: String,
    val credit: Double
)

data class Admin(
    val username: String,
    val password: String
)

data class Offer(
    val id: Long,
    val stationId: Long,
    val discountPercent: Double,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class PriceQuote(
    val baseSingle: Double,
    val baseReturn: Double,
    val singleAfterOffer: Double,
    val returnAfterOffer: Double,
    val activeOffer: Offer?
)