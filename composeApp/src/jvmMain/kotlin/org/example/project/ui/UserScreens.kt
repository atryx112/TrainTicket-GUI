package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.OfferRepository
import data.StationRepository
import data.CardRepository
import data.TicketRepository
import domain.TicketType
import util.Money
import java.nio.file.Files
import java.nio.file.Paths

@Composable
fun UserSearchScreen(
    stationRepo: StationRepository,
    offerRepo: OfferRepository,
    onBuy: (stationId: Long, typeIsReturn: Boolean) -> Unit,
    onAdmin: () -> Unit
) {
    val stations by remember { mutableStateOf(stationRepo.all()) }
    var selectedId by remember { mutableStateOf<Long?>(stations.firstOrNull()?.id) }
    var typeReturn by remember { mutableStateOf(false) }

    val selectedStation = stations.find { it.id == selectedId }
    val quote = selectedStation?.let { offerRepo.quote(it) }

    Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Train Ticket Machine", style = MaterialTheme.typography.h5)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Destination: ")
            Spacer(Modifier.width(8.dp))

        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Type:")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = !typeReturn, onClick = { typeReturn = false }); Text("Single")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = typeReturn, onClick = { typeReturn = true }); Text("Return")
            }
        }

        Divider()
        if (quote != null) {
            Text("Prices:")
            Text("Single: ${Money.f(quote.baseSingle)}  →  ${Money.f(quote.singleAfterOffer)}")
            Text("Return: ${Money.f(quote.baseReturn)}  →  ${Money.f(quote.returnAfterOffer)}")
            if (quote.activeOffer != null) {
                Text("Offer active: -${quote.activeOffer.discountPercent}% (${quote.activeOffer.startDate} to ${quote.activeOffer.endDate})",
                    color = MaterialTheme.colors.primary)
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(enabled = selectedId != null, onClick = {
                onBuy(selectedId!!, typeReturn)
            }) { Text("Buy with Card") }

            OutlinedButton(onClick = onAdmin) { Text("Admin Login") }
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

    Column(Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Enter Test Card Number (e.g., 4242 4242 4242 4242)")
        OutlinedTextField(value = cardNumber, onValueChange = { cardNumber = it.filter { ch -> ch.isDigit() } },
            placeholder = { Text("4242424242424242") }, singleLine = true)

        Text("Amount: ${Money.f(price)}")
        if (error != null) Text(error!!, color = MaterialTheme.colors.error)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Button(onClick = {
                val card = cardRepo.find(cardNumber)
                if (card == null) { error = "Card not found (use a valid test number)."; return@Button }
                if (!cardRepo.deduct(cardNumber, price)) { error = "Insufficient funds."; return@Button }

                // Success: log sale and increment station popularity
                stationRepo.incSales(stationId)
                ticketRepo.log(origin = "ORIGIN STATION", destination = station.name,
                    type = if (typeIsReturn) TicketType.RETURN else TicketType.SINGLE, price = price, cardNumber = cardNumber)

                val ticketText = buildString {
                    appendLine("ORIGIN STATION")
                    appendLine("to")
                    appendLine(station.name.uppercase())
                    appendLine("Price: ${"%.2f".format(price)} [${if (typeIsReturn) "Return" else "Single"}]")
                }
                onPaid(ticketText)
            }) { Text("Pay") }
        }
    }
}

@Composable
fun TicketScreen(ticketText: String, onDone: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ticket issued:")
        Surface(elevation = 2.dp) { Box(Modifier.padding(16.dp)) { Text(ticketText) } }
        Button(onClick = {
            Files.writeString(
                Paths.get("ticket.txt"),
                "***\n" + ticketText.lines().joinToString("\n") + "\n***\n")
        }) { Text("Print (save ticket.txt)") }
        OutlinedButton(onClick = onDone) { Text("Done") }
    }
}
