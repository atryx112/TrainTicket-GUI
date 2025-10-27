package ttm.app

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ttm.data.*
import ttm.ui.AppRouter

fun main() = application {
    val conn = Database.connection()
    val stationRepo = StationRepository(conn)
    val cardRepo = CardRepository(conn)
    val adminRepo = AdminRepository(conn)
    val offerRepo = OfferRepository(conn)
    val ticketRepo = TicketRepository(conn)

    val nav = rememberNav()

    Window(onCloseRequest = ::exitApplication, title = "Train Ticket Machine") {
        MaterialTheme {
            AppRouter(
                nav = nav,
                stationRepo = stationRepo,
                cardRepo = cardRepo,
                adminRepo = adminRepo,
                offerRepo = offerRepo,
                ticketRepo = ticketRepo
            )
        }
    }
}