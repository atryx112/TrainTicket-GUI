package ttm.app

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp
import ttm.data.*
import ttm.ui.AppRouter
import ttm.ui.AppTheme

fun main() = application {
    val conn = Database.connection()
    val stationRepo = StationRepository(conn)
    val cardRepo = CardRepository(conn)
    val adminRepo = AdminRepository(conn)
    val offerRepo = OfferRepository(conn)
    val ticketRepo = TicketRepository(conn)

    val nav = rememberNav()

    val state = rememberWindowState(width = 1000.dp, height = 700.dp)

    Window(onCloseRequest = ::exitApplication, title = "Train Ticket Machine", state = state) {
        AppTheme {
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
