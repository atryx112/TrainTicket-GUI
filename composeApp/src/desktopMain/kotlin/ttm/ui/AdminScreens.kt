package ttm.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import ttm.data.AdminRepository
import ttm.data.OfferRepository
import ttm.data.StationRepository
import ttm.domain.Offer
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
    offerRepo: OfferRepository,
    onBack: () -> Unit,
    onStationDetail: (Long) -> Unit,
    onAdjustPrices: () -> Unit,
    onAddStation: () -> Unit,
    onOffers: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val allStations = stationRepo.all()
    val offers = offerRepo.list()
    val today = remember { java.time.LocalDate.now() }
    val filteredStations = if (query.isBlank()) {
        allStations
    } else {
        allStations.filter { it.name.contains(query, ignoreCase = true) }
    }
    val totalSales = allStations.sumOf { it.salesCount }
    val activeOffersCount = offers.count { offer ->
        offer.startDate <= today && offer.endDate >= today
    }

    AppScaffold(
        title = "Admin Panel",
        showBack = true,
        onBack = onBack,
        topActions = {
codex/remove-adjust-offers-button-and-rename-y8fgaw
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onAdjustPrices) { Text("Adjust Prices") }
                TextButton(onClick = onOffers) { Text("Offers") }
                TextButton(onClick = onAddStation) { Text("Add Station") }
            }
        }
    ) { pads ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pads)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AdminDashboardHero(
                totalStations = allStations.size,
                totalSales = totalSales,
                activeOffers = activeOffersCount
            )

            FilterAndActionsCard(
                query = query,
                onQueryChange = { query = it },
                onOffers = onOffers,
                onAddStation = onAddStation,
                onAdjustPrices = onAdjustPrices
            )

            Text(
                text = if (query.isBlank()) "All stations" else "Results for “$query”",
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )

            if (filteredStations.isEmpty()) {
                EmptyStateCard(message = "No stations match the current search.")
            } else {
                Box(Modifier.weight(1f, fill = true)) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 280.dp),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(filteredStations, key = { it.id }) { station ->
                            StationManagementCard(
                                station = station,
                                today = today,
                                offers = offers,
                                onEdit = { onStationDetail(station.id) }
                            )

            TextButton(onClick = onAddStation) { Text("Add Station") }
        }
    ) { pads ->
        Column(
            Modifier.fillMaxSize().padding(pads).padding(24.dp)
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
                    OutlinedButton(onClick = onAddStation) { Text("Add Station") }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (stations.isEmpty()) {
                SectionCard { Text("No stations match “$query”.") }
            } else {
                Box(Modifier.weight(1f, fill = true)) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 260.dp),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(stations, key = { it.id }) { st ->
                            SectionCard {
                                Text(st.name, style = MaterialTheme.typography.h5)
                                Spacer(Modifier.height(6.dp))
                                Text("Single: ${"%.2f".format(st.singlePrice)}")
                                Text("Return: ${"%.2f".format(st.returnPrice)}")
                                Spacer(Modifier.height(6.dp))
                                Text("Sales: ${st.salesCount}", style = MaterialTheme.typography.subtitle1)
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PrimaryButton(text = "Edit") { onStationDetail(st.id) }
                                }
                            }
 master
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardHero(totalStations: Int, totalSales: Int, activeOffers: Int) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colors.primary,
            MaterialTheme.colors.secondary
        )
    )
    Surface(
        elevation = 8.dp,
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "Modern station management",
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.onPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Stay on top of pricing and offers across your network.",
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.onPrimary.copy(alpha = 0.85f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardMetric(label = "Stations", value = totalStations.toString())
                    DashboardMetric(label = "Total sales", value = totalSales.toString())
                    DashboardMetric(label = "Active offers", value = activeOffers.toString())
                }
            }
        }
    }
}

@Composable
private fun DashboardMetric(label: String, value: String) {
    Surface(
        color = MaterialTheme.colors.onPrimary.copy(alpha = 0.12f),
        contentColor = MaterialTheme.colors.onPrimary,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = value, style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold)
            Text(text = label, style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
private fun FilterAndActionsCard(
    query: String,
    onQueryChange: (String) -> Unit,
    onOffers: () -> Unit,
    onAddStation: () -> Unit,
    onAdjustPrices: () -> Unit
) {
    Surface(
        elevation = 2.dp,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.08f)),
        color = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Search stations") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AssistActionButton(text = "Manage offers", onClick = onOffers)
                AssistActionButton(text = "Add station", onClick = onAddStation)
                AssistActionButton(text = "Adjust prices", onClick = onAdjustPrices)
            }
        }
    }
}

@Composable
private fun AssistActionButton(text: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, shape = RoundedCornerShape(50)) {
        Text(text)
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        elevation = 0.dp,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.05f)),
        color = MaterialTheme.colors.surface
    ) {
        Box(Modifier.padding(horizontal = 24.dp, vertical = 32.dp)) {
            Text(
                text = message,
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun StationManagementCard(
    station: Station,
    today: java.time.LocalDate,
    offers: List<Offer>,
    onEdit: () -> Unit
) {
    val activeOffers = offers.count { offer ->
        offer.stationId == station.id && offer.startDate <= today && offer.endDate >= today
    }
    val offerLabel = if (activeOffers == 0) "No active offers" else "$activeOffers active offer${if (activeOffers > 1) "s" else ""}"
    val offerColor = if (activeOffers == 0) {
        MaterialTheme.colors.onSurface.copy(alpha = 0.06f)
    } else {
        MaterialTheme.colors.secondary.copy(alpha = 0.18f)
    }
    val offerTextColor = if (activeOffers == 0) {
        MaterialTheme.colors.onSurface.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colors.secondary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        elevation = 6.dp,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.05f)),
        color = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(station.name, style = MaterialTheme.typography.h6, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PricePill(label = "Single", value = station.singlePrice)
                PricePill(label = "Return", value = station.returnPrice)
            }
            Text(
                text = "Sales to date: ${station.salesCount}",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.75f)
            )
            Surface(
                color = offerColor,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    text = offerLabel,
                    color = offerTextColor,
                    style = MaterialTheme.typography.caption
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onEdit,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Edit station")
            }
        }
    }
}

@Composable
private fun PricePill(label: String, value: Double) {
    Surface(
        color = MaterialTheme.colors.primary.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.caption, color = MaterialTheme.colors.primary)
            Text(text = "${"%.2f".format(value)}", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Medium)
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
    offerRepo: OfferRepository,
    onDone: () -> Unit
) {
    val existing = remember(stationId) { stationId?.let { stationRepo.byId(it) } }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var single by remember { mutableStateOf(existing?.singlePrice?.toString() ?: "") }
    var ret by remember { mutableStateOf(existing?.returnPrice?.toString() ?: "") }
    var msg by remember { mutableStateOf<String?>(null) }
    val offersForStation = remember(stationId) {
        stationId?.let { id -> offerRepo.list().filter { it.stationId == id } } ?: emptyList()
    }

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

            if (stationId != null) {
                SectionCard {
                    Text("Offers for this station", style = MaterialTheme.typography.h6)
                    Spacer(Modifier.height(8.dp))
                    if (offersForStation.isEmpty()) {
                        Text("There are no active offers for this station.")
                    } else {
                        offersForStation.forEach { offer ->
                            Text("• -${offer.discountPercent}%  ${offer.startDate} → ${offer.endDate}")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Manage offers from the Offers screen.", style = MaterialTheme.typography.body2)
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
