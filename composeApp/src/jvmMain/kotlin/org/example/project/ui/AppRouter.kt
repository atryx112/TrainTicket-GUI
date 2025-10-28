package ui

import androidx.compose.runtime.Composable
import app.Nav
import app.Screen
import data.*

@Composable
fun AppRouter(
    nav: Nav,
    stationRepo: StationRepository,
    cardRepo: CardRepository,
    adminRepo: AdminRepository,
    offerRepo: OfferRepository,
    ticketRepo: TicketRepository
) {
    when (val s = nav.screen) {
        is Screen.UserSearch -> UserSearchScreen(
            stationRepo = stationRepo,
            offerRepo = offerRepo,
            onBuy = { stationId, typeIsReturn -> nav.go(Screen.Payment(stationId, typeIsReturn)) },
            onAdmin = { nav.go(Screen.AdminLogin) }
        )
        is Screen.Payment -> PaymentScreen(
            stationId = s.destinationId,
            typeIsReturn = s.typeIsReturn,
            stationRepo = stationRepo,
            offerRepo = offerRepo,
            cardRepo = cardRepo,
            ticketRepo = ticketRepo,
            onPaid = { ticketText -> nav.go(Screen.Ticket(ticketText)) },
            onBack = { nav.go(Screen.UserSearch) }
        )
        is Screen.Ticket -> TicketScreen(ticketText = s.ticketText) { nav.go(Screen.UserSearch) }
        is Screen.AdminLogin -> AdminLoginScreen(
            adminRepo = adminRepo,
            onSuccess = { nav.go(Screen.AdminDashboard) },
            onCancel = { nav.go(Screen.UserSearch) }
        )
        is Screen.AdminDashboard -> AdminDashboardScreen(
            onBack = { nav.go(Screen.UserSearch) },
            onStationDetail = { id -> nav.go(Screen.AdminStationEdit(id)) },
            onAdjustPrices = { nav.go(Screen.AdminPriceAdjust) },
            onAddStation = { nav.go(Screen.AdminStationEdit(null)) },
            onOffers = { nav.go(Screen.AdminOffers) }
        )
        is Screen.AdminStationDetail -> AdminStationDetailScreen(
            stationId = s.stationId, stationRepo = stationRepo, onBack = { nav.go(Screen.AdminDashboard) },
            onEdit = { nav.go(Screen.AdminStationEdit(s.stationId)) }
        )
        is Screen.AdminPriceAdjust -> AdminPriceAdjustScreen(
            stationRepo = stationRepo, onDone = { nav.go(Screen.AdminDashboard) }
        )
        is Screen.AdminStationEdit -> AdminStationEditScreen(
            stationId = s.stationId, stationRepo = stationRepo, offerRepo = offerRepo, onDone = { nav.go(Screen.AdminDashboard) }
        )
        is Screen.AdminOffers -> AdminOffersScreen(
            offerRepo = offerRepo, stationRepo = stationRepo, onBack = { nav.go(Screen.AdminDashboard) }
        )
    }
}
