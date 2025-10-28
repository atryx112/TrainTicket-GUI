package ttm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ttm.data.AdminRepository
import ttm.data.OfferRepository
import ttm.data.StationRepository
import ttm.domain.Offer
import ttm.domain.Station
import ttm.util.Dates
import java.time.LocalDate

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

@Composable
private fun DashboardHero(
    stationCount: Int,
    totalSales: Int,
    activeOffers: Int,
    avgSingle: Double
) {
    val brush = remember {
        Brush.linearGradient(
            listOf(
                MaterialTheme.colors.primary,
                MaterialTheme.colors.primaryVariant
            )
        )
    }
    val onPrimary = MaterialTheme.colors.onPrimary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(brush)
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Operations overview", color = onPrimary.copy(alpha = 0.85f), style = MaterialTheme.typography.subtitle1)
                Text("Train network dashboard", color = onPrimary, style = MaterialTheme.typography.h4)
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                HeroMetric(label = "Stations", value = stationCount.toString(), color = onPrimary)
                HeroMetric(label = "Tickets sold", value = totalSales.toString(), color = onPrimary)
                HeroMetric(label = "Active offers", value = activeOffers.toString(), color = onPrimary)
                HeroMetric(
                    label = "Avg. single fare",
                    value = if (avgSingle > 0) "${"%.2f".format(avgSingle)}" else "—",
                    color = onPrimary
                )
            }
        }
    }
}

@Composable
private fun RowScope.HeroMetric(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, color = color, style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)
        Text(label, color = color.copy(alpha = 0.75f), style = MaterialTheme.typography.subtitle1)
    }
}

@Composable
private fun StationDashboardCard(
    modifier: Modifier = Modifier,
    station: Station,
    activeOffer: Offer?,
    totalOffers: Int,
    onEdit: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colors.surface,
        elevation = 8.dp
    ) {
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(station.name, style = MaterialTheme.typography.h5)
                Spacer(Modifier.weight(1f))
                StatPill(text = "${station.salesCount} sold", color = MaterialTheme.colors.primary)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FarePill(label = "Single", value = station.singlePrice)
                FarePill(label = "Return", value = station.returnPrice)
            }

            if (activeOffer != null) {
                OfferBadge("-${"%.0f".format(activeOffer.discountPercent)}% until ${Dates.format(activeOffer.endDate)}")
            } else {
                val info = if (totalOffers > 0) {
                    "$totalOffers offer${if (totalOffers == 1) "" else "s"} saved"
                } else {
                    "No offers yet"
                }
                Text(info, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
            }

            Button(
                onClick = onEdit,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Edit Station")
            }
        }
    }
}

@Composable
private fun StatPill(text: String, color: Color) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text,
            color = color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.subtitle1
        )
    }
}

@Composable
private fun FarePill(label: String, value: Double) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colors.primary.copy(alpha = 0.08f)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(label.uppercase(), color = MaterialTheme.colors.primaryVariant, style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.SemiBold)
            Text("${"%.2f".format(value)}", style = MaterialTheme.typography.h5)
        }
    }
}

@Composable
private fun OfferBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colors.secondary.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = MaterialTheme.colors.secondary)
            Text(text, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.subtitle1)
        }
    }
}

/* -------------------- Admin Dashboard (modernized) -------------------- */

@Composable
fun AdminDashboardScreen(
    stationRepo: StationRepository,
    offerRepo: OfferRepository,
    onBack: () -> Unit,
    onEditStation: (Long) -> Unit,
    onAddStation: () -> Unit,
    onOffers: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val allStations = remember { stationRepo.all() }
    val allOffers = remember { offerRepo.list() }
    val stations = remember(query, allStations) {
        if (query.isBlank()) allStations
        else allStations.filter { it.name.contains(query, ignoreCase = true) }
    }
    val today = remember { LocalDate.now() }
    val offersByStation = remember(allOffers) { allOffers.groupBy { it.stationId } }
    val activeOfferCount = remember(allOffers, today) {
        allOffers.count { it.startDate <= today && it.endDate >= today }
    }
    val totalSales = remember(allStations) { allStations.sumOf { it.salesCount } }
    val avgSingle = remember(allStations) {
        if (allStations.isNotEmpty()) allStations.map { it.singlePrice }.average() else 0.0
    }

    AppScaffold(
        title = "Admin Control Center",
        showBack = true,
        onBack = onBack,
        topActions = {
            TextButton(onClick = onOffers) {
                Icon(Icons.Default.LocalOffer, contentDescription = "Offers")
                Spacer(Modifier.width(6.dp))
                Text("Offers")
            }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onAddStation) {
                Icon(Icons.Default.Add, contentDescription = "Add Station")
                Spacer(Modifier.width(6.dp))
                Text("Add Station")
            }
        }
    ) { pads ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pads)
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            DashboardHero(
                stationCount = allStations.size,
                totalSales = totalSales,
                activeOffers = activeOfferCount,
                avgSingle = avgSingle
            )

            SectionCard {
                Text(
                    "Quick actions",
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.SemiBold)
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search stations") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onAddStation,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("New Station")
                    }
                    OutlinedButton(
                        onClick = onOffers,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.LocalOffer, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("View Offers")
                    }
                }
            }

            val columns = 2
            val rows = stations.chunked(columns)
            if (stations.isEmpty()) {
                SectionCard {
                    Text(
                        "No stations match “$query”.",
                        style = MaterialTheme.typography.subtitle1
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    rows.forEach { row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            row.forEach { station ->
                                val stationOffers = offersByStation[station.id].orEmpty()
                                val activeOffer = stationOffers.firstOrNull { it.startDate <= today && it.endDate >= today }
                                StationDashboardCard(
                                    modifier = Modifier.weight(1f),
                                    station = station,
                                    activeOffer = activeOffer,
                                    totalOffers = stationOffers.size,
                                    onEdit = { onEditStation(station.id) }
                                )
                            }
                            if (row.size < columns) {
                                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
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
    var currentId by remember { mutableStateOf(stationId) }
    val existing = remember(currentId) { currentId?.let { stationRepo.byId(it) } }

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var single by remember { mutableStateOf(existing?.singlePrice?.let { "%.2f".format(it) } ?: "") }
    var ret by remember { mutableStateOf(existing?.returnPrice?.let { "%.2f".format(it) } ?: "") }
    var msg by remember { mutableStateOf<String?>(null) }

    var offerPercent by remember { mutableStateOf("10") }
    var offerStart by remember { mutableStateOf(Dates.format(LocalDate.now())) }
    var offerEnd by remember { mutableStateOf(Dates.format(LocalDate.now().plusDays(7))) }
    var offerMsg by remember { mutableStateOf<String?>(null) }
    var offersRefresh by remember { mutableStateOf(0) }
    val offers = remember(currentId, offersRefresh) {
        currentId?.let { offerRepo.forStation(it) } ?: emptyList()
    }

    AppScaffold(
        title = if (currentId == null) "Add Station" else "Edit Station",
        showBack = true,
        onBack = onDone
    ) { pads ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pads)
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SectionCard {
                Text(
                    if (currentId == null) "New station details" else "Station details",
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.SemiBold)
                )
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        single,
                        { single = it },
                        label = { Text("Single price") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        ret,
                        { ret = it },
                        label = { Text("Return price") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (msg != null) {
                    Text(msg!!, color = MaterialTheme.colors.secondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDone, shape = RoundedCornerShape(12.dp)) { Text("Cancel") }
                    Button(
                        onClick = {
                            val s = single.toDoubleOrNull()
                            val r = ret.toDoubleOrNull()
                            if (name.isBlank() || s == null || r == null || s <= 0 || r <= 0) {
                                msg = "Please provide valid prices"
                                return@Button
                            }
                            val updated = Station(
                                id = currentId ?: 0L,
                                name = name.trim(),
                                singlePrice = s,
                                returnPrice = r,
                                salesCount = existing?.salesCount ?: 0
                            )
                            val savedId = stationRepo.upsert(updated)
                            currentId = if (updated.id == 0L) savedId else updated.id
                            msg = "Station saved"
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Save changes")
                    }
                }
            }

            if (currentId == null) {
                SectionCard {
                    Text(
                        "Save the station first to manage special offers.",
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            } else {
                SectionCard {
                    Text(
                        "Create a new offer",
                        style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.SemiBold)
                    )
                    OutlinedTextField(
                        offerPercent,
                        { offerPercent = it },
                        label = { Text("Discount %") },
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            offerStart,
                            { offerStart = it },
                            label = { Text("Start date (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            offerEnd,
                            { offerEnd = it },
                            label = { Text("End date (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (offerMsg != null) {
                        Text(offerMsg!!, color = MaterialTheme.colors.secondary)
                    }
                    Button(
                        onClick = {
                            val sid = currentId ?: return@Button
                            val pct = offerPercent.toDoubleOrNull()
                            if (pct == null || pct <= 0) {
                                offerMsg = "Enter a positive discount"
                                return@Button
                            }
                            val startDate = runCatching { Dates.parse(offerStart) }.getOrNull()
                            val endDate = runCatching { Dates.parse(offerEnd) }.getOrNull()
                            if (startDate == null || endDate == null) {
                                offerMsg = "Invalid dates"
                                return@Button
                            }
                            if (endDate.isBefore(startDate)) {
                                offerMsg = "End date must be after start date"
                                return@Button
                            }
                            offerRepo.add(sid, pct, startDate, endDate)
                            offerMsg = "Offer created"
                            offersRefresh++
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.LocalOffer, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Add offer")
                    }
                }

                SectionCard {
                    Text(
                        "Existing offers",
                        style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.SemiBold)
                    )
                    if (offers.isEmpty()) {
                        Text(
                            "No offers for this station yet.",
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    } else {
                        offers.forEach { offer ->
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        "-${"%.0f".format(offer.discountPercent)}%",
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "${Dates.format(offer.startDate)} → ${Dates.format(offer.endDate)}",
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        offerRepo.delete(offer.id)
                                        offerMsg = "Offer removed"
                                        offersRefresh++
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
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
