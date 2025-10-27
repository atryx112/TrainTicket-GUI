package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.AdminRepository
import data.OfferRepository
import data.StationRepository
import domain.Station
import util.Dates
import java.time.LocalDate

@Composable
fun AdminLoginScreen(
    adminRepo: AdminRepository,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Admin Login", style = MaterialTheme.typography.h5)
        OutlinedTextField(user, { user = it }, label = { Text("Username") })
        OutlinedTextField(pass, { pass = it }, label = { Text("Password") })
        if (err != null) Text(err!!, color = MaterialTheme.colors.error)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
            Button(onClick = {
                if (adminRepo.login(user, pass)) onSuccess() else err = "Invalid credentials"
            }) { Text("Login") }
        }
    }
}

@Composable
fun AdminDashboardScreen(
    onBack: () -> Unit,
    onStationDetail: (Long) -> Unit,
    onAdjustPrices: () -> Unit,
    onAddStation: () -> Unit,
    onOffers: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Admin Dashboard", style = MaterialTheme.typography.h5)
        Button(onClick = onAdjustPrices) { Text("Change ALL prices by factor") }
        Button(onClick = onAddStation) { Text("Add Station") }
        Button(onClick = onOffers) { Text("Manage Offers") }
        OutlinedButton(onClick = onBack) { Text("Back to User") }
        Divider()
        Text("Open a station to view details:")
        Row {
            Text("→ Use the User page to see stations; or go via Manage Offers to see IDs. In a fuller UI we’d list stations here.")
        }
    }
}

@Composable
fun AdminStationDetailScreen(
    stationId: Long,
    stationRepo: StationRepository,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val station = remember { stationRepo.byId(stationId) }
    Column(Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Station details", style = MaterialTheme.typography.h5)
        if (station != null) {
            Text("Name: ${station.name}")
            Text("Single: ${station.singlePrice}")
            Text("Return: ${station.returnPrice}")
            Text("Sales: ${station.salesCount}")
        } else {
            Text("Station not found")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Button(onClick = onEdit) { Text("Edit") }
        }
    }
}

@Composable
fun AdminPriceAdjustScreen(
    stationRepo: StationRepository,
    onDone: () -> Unit
) {
    var factor by remember { mutableStateOf("1.0") }
    var info by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Change ALL ticket prices by factor", style = MaterialTheme.typography.h5)
        OutlinedTextField(factor, { factor = it }, label = { Text("Factor (e.g., 0.9 for -10%)") })
        if (info != null) Text(info!!)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDone) { Text("Back") }
            Button(onClick = {
                val f = factor.toDoubleOrNull()
                if (f == null || f <= 0) { info = "Invalid factor"; return@Button }
                stationRepo.updatePricesFactor(f)
                info = "Prices updated by ×$f"
            }) { Text("Apply") }
        }
    }
}

@Composable
fun AdminStationEditScreen(
    stationId: Long?,
    stationRepo: StationRepository,
    onDone: () -> Unit
) {
    val existing = remember(stationId) { stationId?.let { stationRepo.byId(it) } }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var single by remember { mutableStateOf(existing?.singlePrice?.toString() ?: "") }
    var ret by remember { mutableStateOf(existing?.returnPrice?.toString() ?: "") }
    var msg by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(if (stationId == null) "Add Station" else "Edit Station", style = MaterialTheme.typography.h5)
        OutlinedTextField(name, { name = it }, label = { Text("Name") })
        OutlinedTextField(single, { single = it }, label = { Text("Single price") })
        OutlinedTextField(ret, { ret = it }, label = { Text("Return price") })
        if (msg != null) Text(msg!!, color = MaterialTheme.colors.primary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDone) { Text("Back") }
            Button(onClick = {
                val s = single.toDoubleOrNull(); val r = ret.toDoubleOrNull()
                if (name.isBlank() || s == null || r == null || s <= 0 || r <= 0) { msg = "Invalid inputs"; return@Button }
                val id = stationId ?: 0L
                val upd = Station(id = id, name = name, singlePrice = s, returnPrice = r, salesCount = existing?.salesCount ?: 0)
                stationRepo.upsert(upd)
                msg = "Saved."
            }) { Text("Save") }
        }
    }
}

@Composable
fun AdminOffersScreen(
    offerRepo: OfferRepository,
    stationRepo: StationRepository,
    onBack: () -> Unit
) {
    val stations by remember { mutableStateOf(stationRepo.all()) }
    var stationId by remember { mutableStateOf(stations.firstOrNull()?.id) }
    var percent by remember { mutableStateOf("10") }
    var start by remember { mutableStateOf(Dates.format(LocalDate.now())) }
    var end by remember { mutableStateOf(Dates.format(LocalDate.now().plusDays(7))) }
    var info by remember { mutableStateOf<String?>(null) }

    var refresh by remember { mutableStateOf(0) }
    val offers = remember(refresh) { offerRepo.list() }

    Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Special Offers", style = MaterialTheme.typography.h5)
        Row {
            Text("Station: "); Spacer(Modifier.width(8.dp))
            DropdownMenuBox(
                items = stations.map { it.id to it.name },
                selected = stationId,
                onSelect = { stationId = it }
            )
        }
        OutlinedTextField(percent, { percent = it }, label = { Text("Discount %") })
        OutlinedTextField(start, { start = it }, label = { Text("Start (YYYY-MM-DD)") })
        OutlinedTextField(end, { end = it }, label = { Text("End (YYYY-MM-DD)") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Button(onClick = {
                val sid = stationId ?: return@Button
                val p = percent.toDoubleOrNull() ?: run { info = "Invalid %"; return@Button }
                val sd = runCatching { Dates.parse(start) }.getOrNull() ?: run { info = "Invalid start date"; return@Button }
                val ed = runCatching { Dates.parse(end) }.getOrNull() ?: run { info = "Invalid end date"; return@Button }
                offerRepo.add(sid, p, sd, ed); info = "Offer added"; refresh++
            }) { Text("Add Offer") }
        }
        if (info != null) Text(info!!, color = MaterialTheme.colors.primary)
        Divider()
        Text("Existing offers:")
        offers.forEach { o ->
            val stName = stations.find { it.id == o.stationId }?.name ?: "Unknown"
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text("#${o.id}  $stName  -${o.discountPercent}%  ${o.startDate}..${o.endDate}")
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = { offerRepo.delete(o.id); info = "Deleted offer #${o.id}"; refresh++ }) { Text("Delete") }
            }
        }
    }
}
