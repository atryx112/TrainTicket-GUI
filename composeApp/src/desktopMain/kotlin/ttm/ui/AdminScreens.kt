package ttm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ttm.data.AdminRepository
import ttm.data.OfferRepository
import ttm.data.StationRepository
import ttm.domain.Station
import ttm.util.Dates

/* -------------------- Admin Login -------------------- */

@Composable
fun AdminLoginScreen(
    adminRepo: AdminRepository,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    AppScaffold(title = "Admin Login", showBack = true, onBack = onCancel) { pads ->
        Column(
            Modifier.fillMaxSize().padding(pads).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionCard {
                OutlinedTextField(user, { user = it }, label = { Text("Username") }, singleLine = true)
                OutlinedTextField(pass, { pass = it }, label = { Text("Password") }, singleLine = true)
                if (err != null) Text(err!!, color = MaterialTheme.colors.error)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onCancel) { Text("Cancel") }
                    PrimaryButton(text = "Login") {
                        if (adminRepo.login(user, pass)) onSuccess() else err = "Invalid credentials"
                    }
                }
            }
        }
    }
}

/* -------------------- Admin Dashboard (fixed + modern) -------------------- */

@Composable
fun AdminDashboardScreen(
    stationRepo: StationRepository,
    onBack: () -> Unit,
    onStationDetail: (Long) -> Unit,
    onAdjustPrices: () -> Unit,
    onAddStation: () -> Unit,
    onOffers: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val allStations = remember { stationRepo.all() }
    val stations = remember(query, allStations) {
        if (query.isBlank()) allStations
        else allStations.filter { it.name.contains(query, ignoreCase = true) }
    }

    AppScaffold(
        title = "Admin Panel",
        showBack = true,
        onBack = onBack,
        topActions = {
            TextButton(onClick = onAdjustPrices) { Text("Adjust Prices") }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onOffers) { Text("Offers") }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onAddStation) { Text("Add Station") }
        }
    ) { pads ->
        Column(
            Modifier.fillMaxSize().padding(pads).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Search stations") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(text = "Adjust Prices") { onAdjustPrices() }
                    OutlinedButton(onClick = onOffers) { Text("Offers") }
                    OutlinedButton(onClick = onAddStation) { Text("Add Station") }
                }
            }

            val cols = 3
            val rows = stations.chunked(cols)
            if (stations.isEmpty()) {
                SectionCard { Text("No stations match “$query”.") }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    rows.forEach { row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            row.forEach { st ->
                                SectionCard(Modifier.weight(1f)) {
                                    Text(st.name, style = MaterialTheme.typography.h5)
                                    Spacer(Modifier.height(6.dp))
                                    Text("Single: ${"%.2f".format(st.singlePrice)}")
                                    Text("Return: ${"%.2f".format(st.returnPrice)}")
                                    Spacer(Modifier.height(6.dp))
                                    Text("Sales: ${st.salesCount}", style = MaterialTheme.typography.subtitle1)
                                    Spacer(Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        PrimaryButton(text = "Open Details") { onStationDetail(st.id) }
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
}

/* -------------------- Station Detail -------------------- */

@Composable
fun AdminStationDetailScreen(
    stationId: Long,
    stationRepo: StationRepository,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val station = remember { stationRepo.byId(stationId) }
    AppScaffold(title = "Station Details", showBack = true, onBack = onBack) { pads ->
        Column(Modifier.fillMaxSize().padding(pads).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionCard {
                if (station != null) {
                    Text("Name: ${station.name}", style = MaterialTheme.typography.h5)
                    Text("Single: ${"%.2f".format(station.singlePrice)}")
                    Text("Return: ${"%.2f".format(station.returnPrice)}")
                    Text("Sales: ${station.salesCount}")
                } else {
                    Text("Station not found")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onBack) { Text("Back") }
                    PrimaryButton(text = "Edit") { onEdit() }
                }
            }
        }
    }
}

/* -------------------- Adjust Prices -------------------- */

@Composable
fun AdminPriceAdjustScreen(
    stationRepo: StationRepository,
    onDone: () -> Unit
) {
    var factor by remember { mutableStateOf("1.0") }
    var info by remember { mutableStateOf<String?>(null) }

    AppScaffold(title = "Adjust Prices", showBack = true, onBack = onDone) { pads ->
        Column(Modifier.fillMaxSize().padding(pads).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionCard {
                OutlinedTextField(factor, { factor = it }, label = { Text("Factor (e.g., 0.9 for -10%)") }, singleLine = true)
                if (info != null) Text(info!!)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDone) { Text("Back") }
                    PrimaryButton(text = "Apply") {
                        val f = factor.toDoubleOrNull()
                        if (f == null || f <= 0) { info = "Invalid factor"; return@PrimaryButton }
                        stationRepo.updatePricesFactor(f)
                        info = "Prices updated by ×$f"
                    }
                }
            }
        }
    }
}

/* -------------------- Add/Edit Station -------------------- */

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

    AppScaffold(title = if (stationId == null) "Add Station" else "Edit Station",
        showBack = true, onBack = onDone) { pads ->
        Column(Modifier.fillMaxSize().padding(pads).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionCard {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(single, { single = it }, label = { Text("Single price") }, singleLine = true)
                OutlinedTextField(ret, { ret = it }, label = { Text("Return price") }, singleLine = true)
                if (msg != null) Text(msg!!, color = MaterialTheme.colors.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDone) { Text("Back") }
                    PrimaryButton(text = "Save") {
                        val s = single.toDoubleOrNull(); val r = ret.toDoubleOrNull()
                        if (name.isBlank() || s == null || r == null || s <= 0 || r <= 0) {
                            msg = "Invalid inputs"; return@PrimaryButton
                        }
                        val upd = Station(
                            id = stationId ?: 0L,
                            name = name,
                            singlePrice = s,
                            returnPrice = r,
                            salesCount = existing?.salesCount ?: 0
                        )
                        stationRepo.upsert(upd)
                        msg = "Saved."
                    }
                }
            }
        }
    }
}

/* -------------------- Offers -------------------- */

@Composable
fun AdminOffersScreen(
    offerRepo: OfferRepository,
    stationRepo: StationRepository,
    onBack: () -> Unit
) {
    val stations by remember { mutableStateOf(stationRepo.all()) }
    var stationId by remember { mutableStateOf(stations.firstOrNull()?.id) }
    var percent by remember { mutableStateOf("10") }
    var start by remember { mutableStateOf(Dates.format(java.time.LocalDate.now())) }
    var end by remember { mutableStateOf(Dates.format(java.time.LocalDate.now().plusDays(7))) }
    var info by remember { mutableStateOf<String?>(null) }

    var refresh by remember { mutableStateOf(0) }
    val offers = remember(refresh) { offerRepo.list() }

    AppScaffold(title = "Special Offers", showBack = true, onBack = onBack) { pads ->
        Column(
            Modifier.fillMaxSize().padding(pads).padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Station:"); Spacer(Modifier.width(8.dp))
                    DropdownMenuBox(
                        items = stations.map { it.id to it.name },
                        selected = stationId,
                        onSelect = { stationId = it }
                    )
                }
                OutlinedTextField(percent, { percent = it }, label = { Text("Discount %") }, singleLine = true)
                OutlinedTextField(start, { start = it }, label = { Text("Start (YYYY-MM-DD)") }, singleLine = true)
                OutlinedTextField(end, { end = it }, label = { Text("End (YYYY-MM-DD)") }, singleLine = true)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onBack) { Text("Back") }
                    PrimaryButton(text = "Add Offer") {
                        val sid = stationId ?: return@PrimaryButton
                        val p = percent.toDoubleOrNull() ?: run { info = "Invalid %"; return@PrimaryButton }
                        val sd = runCatching { Dates.parse(start) }.getOrNull() ?: run { info = "Invalid start date"; return@PrimaryButton }
                        val ed = runCatching { Dates.parse(end) }.getOrNull() ?: run { info = "Invalid end date"; return@PrimaryButton }
                        offerRepo.add(sid, p, sd, ed)
                        info = "Offer added"; refresh++
                    }
                }
                if (info != null) Text(info!!, color = MaterialTheme.colors.primary)
            }

            SectionCard {
                Text("Existing offers", style = MaterialTheme.typography.h5)
                offers.forEach { o ->
                    val stName = stations.find { it.id == o.stationId }?.name ?: "Unknown"
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("#${o.id}  $stName  -${o.discountPercent}%  ${o.startDate}..${o.endDate}")
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(onClick = {
                            offerRepo.delete(o.id)
                            info = "Deleted offer #${o.id}"
                            refresh++
                        }) { Text("Delete") }
                    }
                }
            }
        }
    }
}
