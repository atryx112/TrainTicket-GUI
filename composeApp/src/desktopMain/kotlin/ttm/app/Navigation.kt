package ttm.app

import androidx.compose.runtime.*

sealed interface Screen {
    data object UserSearch : Screen
    data class Payment(val destinationId: Long, val typeIsReturn: Boolean) : Screen
    data class Ticket(val ticketText: String) : Screen
    data object AdminLogin : Screen
    data object AdminDashboard : Screen
    data class AdminStationDetail(val stationId: Long) : Screen
    data object AdminPriceAdjust : Screen
    data class AdminStationEdit(val stationId: Long?) : Screen
    data object AdminOffers : Screen
}

class Nav {
    var screen by mutableStateOf<Screen>(Screen.UserSearch)
    fun go(s: Screen) { screen = s }
}

@Composable
fun rememberNav(): Nav = remember { Nav() }