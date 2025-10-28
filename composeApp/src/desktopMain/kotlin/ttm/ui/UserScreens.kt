package ttm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ttm.data.*
import ttm.domain.TicketType
import ttm.util.Money

@Composable
fun UserSearchScreen(
    stationRepo: StationRepository,
    offerRepo: OfferRepository,
    onBuy: (stationId: Long, typeIsReturn: Boolean) -> Unit,
    onAdmin: () -> Unit,
    onOpenStations: () -> Unit, // NEW
) {
    val stations by remember { mutableStateOf(stationRepo.all()) }
    var selectedId by remember { mutableStateOf<Long?>(stations.firstOrNull()?.id) }
    var typeReturn by remember { mutableStateOf(false) }

    val selectedStation = stations.find { it.id == selectedId }
    val quote = selectedStation?.let { offerRepo.quote(it) }

    AppScaffold(
        title = "Train Ticket Machine",
        topActions = {
            TextButton(onClick = onOpenStations) { Text("Stations") }
            Spacer(Modifier.width(8.dp))
            AdminAction(onClick = onAdmin)
        }
    ) { pads ->
        Column(
            Modifier.fillMaxSize().padding(pads).padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                SectionCard(Modifier.weight(1f)) {
                    Text("Choose Destination", style = MaterialTheme.typography.h5)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Destination:")
                        DropdownMenuBox(
                            items = stations.map { it.id to it.name },
                            selected = selectedId,
                            onSelect = { selectedId = it }
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Text("Ticket Type:")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = !typeReturn, onClick = { typeReturn = false }); Spacer(Modifier.width(6.dp)); Text("Single")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = typeReturn, onClick = { typeReturn = true }); Spacer(Modifier.width(6.dp)); Text("Return")
                        }
                    }
                }

                SectionCard(Modifier.weight(1f)) {
                    Text("Price", style = MaterialTheme.typography.h5)
                    if (quote == null) {
                        Text("Select a destination to see prices.")
                    } else {
                        Text("Single:  ${Money.f(quote.baseSingle)}  →  ${Money.f(quote.singleAfterOffer)}")
                        Text("Return:  ${Money.f(quote.baseReturn)}  →  ${Money.f(quote.returnAfterOffer)}")
                        if (quote.activeOffer != null) {
                            Text(
                                "Active offer: -${quote.activeOffer!!.discountPercent}%  (${quote.activeOffer!!.startDate} to ${quote.activeOffer!!.endDate})",
                                color = MaterialTheme.colors.secondary,
                                style = MaterialTheme.typography.subtitle1
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    PrimaryButton(
                        text = "Buy with Card",
                        enabled = selectedId != null
                    ) { if (selectedId != null) onBuy(selectedId!!, typeReturn) }
                }
            }
        }
    }
}

@Composable
fun PaymentScreen(
    stationId: Long,
    typeIsReturn: Boolean,
    stationRepo: StationRepository,
    offerRepo: OfferRepository,
    cardRepo: CardRepository,
    ticketRepo: TicketRepository,
    onPaid: (ticketText: String) -> Unit,
    onBack: () -> Unit
) {
    val station = remember { stationRepo.byId(stationId)!! }
    val quote = remember { offerRepo.quote(station) }
    val price = if (typeIsReturn) quote.returnAfterOffer else quote.singleAfterOffer
    var cardNumber by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AppScaffold(title = "Payment", showBack = true, onBack = onBack) { pads ->
        Column(
            Modifier.fillMaxSize().padding(pads).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard {
                Text("Confirm Purchase", style = MaterialTheme.typography.h5)
                Text("Destination: ${station.name}")
                Text("Type: ${if (typeIsReturn) "Return" else "Single"}")
                Text("Amount: ${Money.f(price)}")
            }

            SectionCard {
                Text("Insert Card (Test Number)", style = MaterialTheme.typography.h5)
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it.filter(Char::isDigit) },
                    placeholder = { Text("4242424242424242") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) Text(error!!, color = MaterialTheme.colors.error)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrimaryButton(text = "Pay") {
                        val card = cardRepo.find(cardNumber)
                        if (card == null) { error = "Card not found (use a valid test number)."; return@PrimaryButton }
                        if (!cardRepo.deduct(cardNumber, price)) { error = "Insufficient funds."; return@PrimaryButton }

                        stationRepo.incSales(stationId)
                        ticketRepo.log(
                            origin = "ORIGIN STATION",
                            destination = station.name,
                            type = if (typeIsReturn) TicketType.RETURN else TicketType.SINGLE,
                            price = price,
                            cardNumber = cardNumber
                        )

                        val ticketText = buildString {
                            appendLine("ORIGIN STATION")
                            appendLine("to")
                            appendLine(station.name.uppercase())
                            appendLine("Price: ${"%.2f".format(price)} [${if (typeIsReturn) "Return" else "Single"}]")
                        }
                        onPaid(ticketText)
                    }
                }
            }
        }
    }
}

@Composable
fun TicketScreen(ticketText: String, onDone: () -> Unit) {
    AppScaffold(title = "Ticket Issued") { pads ->
        Column(
            Modifier.fillMaxSize().padding(pads).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard {
                Text("Your Ticket", style = MaterialTheme.typography.h5)
                Surface(elevation = 1.dp) {
                    Box(Modifier.fillMaxWidth().padding(16.dp)) { Text(ticketText) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrimaryButton(text = "Print (save ticket.txt)") {
                        java.nio.file.Files.writeString(
                            java.nio.file.Paths.get("ticket.txt"),
                            "***\n" + ticketText.lines().joinToString("\n") + "\n***\n"
                        )
                    }
                    OutlinedButton(onClick = onDone) { Text("Done") }
                }
            }
        }
    }
}

/* ---------- NEW modern stations browser ----------- */

@Composable
fun StationsBrowserScreen(
    stationRepo: StationRepository,
    offerRepo: OfferRepository,
    onBack: () -> Unit,
    onBuy: (stationId: Long, typeIsReturn: Boolean) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val allStations = remember { stationRepo.all() }
    val stations = remember(query, allStations) {
        if (query.isBlank()) allStations
        else allStations.filter { it.name.contains(query, ignoreCase = true) }
    }

    AppScaffold(title = "Stations", showBack = true, onBack = onBack) { pads ->
        Column(
            Modifier.fillMaxSize().padding(pads).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search station") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val cols = 3
            val rows = stations.chunked(cols)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                rows.forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { st ->
                            val quote = remember(st.id) { offerRepo.quote(st) }
                            SectionCard(Modifier.weight(1f)) {
                                Text(st.name, style = MaterialTheme.typography.h5)
                                Spacer(Modifier.height(6.dp))
                                if (quote.activeOffer != null) {
                                    Text(
                                        "-${quote.activeOffer!!.discountPercent}% offer active",
                                        color = MaterialTheme.colors.secondary,
                                        style = MaterialTheme.typography.subtitle1
                                    )
                                }
                                Text("Single: ${Money.f(quote.singleAfterOffer)}")
                                Text("Return: ${Money.f(quote.returnAfterOffer)}")
                                Spacer(Modifier.height(6.dp))
                                Text("Sales: ${st.salesCount}", style = MaterialTheme.typography.subtitle1)

                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PrimaryButton(text = "Buy Single") { onBuy(st.id, false) }
                                    OutlinedButton(onClick = { onBuy(st.id, true) }) { Text("Buy Return") }
                                }
                            }
                        }
                        if (row.size < cols) repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}
