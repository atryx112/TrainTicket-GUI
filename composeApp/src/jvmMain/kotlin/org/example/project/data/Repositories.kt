package data

import domain.*
import util.Dates
import java.sql.Connection
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

class StationRepository(private val conn: Connection) {
    fun all(): List<Station> =
        conn.prepareStatement("SELECT id,name,single_price,return_price,sales_count FROM stations ORDER BY name;")
            .use { ps -> ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) add(
                        Station(
                            id = rs.getLong("id"),
                            name = rs.getString("name"),
                            singlePrice = rs.getDouble("single_price"),
                            returnPrice = rs.getDouble("return_price"),
                            salesCount = rs.getInt("sales_count")
                        )
                    )
                }
            } }

    fun byId(id: Long): Station? =
        conn.prepareStatement("SELECT id,name,single_price,return_price,sales_count FROM stations WHERE id=?;")
            .use { ps ->
                ps.setLong(1,id)
                ps.executeQuery().use { rs ->
                    if (rs.next()) Station(
                        rs.getLong(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4), rs.getInt(5)
                    ) else null
                }
            }

    fun incSales(id: Long) {
        conn.prepareStatement("UPDATE stations SET sales_count = sales_count + 1 WHERE id=?;").use { ps ->
            ps.setLong(1,id); ps.executeUpdate()
        }
    }

    fun updatePricesFactor(factor: Double) {
        conn.prepareStatement("UPDATE stations SET single_price = single_price * ?, return_price = return_price * ?;")
            .use { ps -> ps.setDouble(1,factor); ps.setDouble(2,factor); ps.executeUpdate() }
    }

    fun upsert(station: Station?): Long {
        return if (station == null) {
            error("station is null")
        } else if (station.id == 0L) {
            conn.prepareStatement("INSERT INTO stations(name,single_price,return_price) VALUES(?,?,?);").use { ps ->
                ps.setString(1, station.name); ps.setDouble(2, station.singlePrice); ps.setDouble(3, station.returnPrice)
                ps.executeUpdate()
            }
            conn.createStatement().use { st -> st.executeQuery("SELECT last_insert_rowid();").use { r -> if (r.next()) r.getLong(1) else 0L } }
        } else {
            conn.prepareStatement("UPDATE stations SET name=?, single_price=?, return_price=? WHERE id=?;").use { ps ->
                ps.setString(1, station.name); ps.setDouble(2, station.singlePrice); ps.setDouble(3, station.returnPrice); ps.setLong(4, station.id)
                ps.executeUpdate(); station.id
            }
        }
    }
}

class CardRepository(private val conn: Connection) {
    fun find(cardNumber: String): Card? =
        conn.prepareStatement("SELECT card_number, credit FROM cards WHERE card_number=?;").use { ps ->
            ps.setString(1, cardNumber)
            ps.executeQuery().use { rs -> if (rs.next()) Card(rs.getString(1), rs.getDouble(2)) else null }
        }

    fun deduct(cardNumber: String, amount: Double): Boolean {
        conn.autoCommit = false
        try {
            val cur = find(cardNumber) ?: return false
            if (cur.credit + 1e-9 < amount) { conn.rollback(); return false }
            conn.prepareStatement("UPDATE cards SET credit = credit - ? WHERE card_number=?;").use { ps ->
                ps.setDouble(1, amount); ps.setString(2, cardNumber); ps.executeUpdate()
            }
            conn.commit(); return true
        } catch (e: Exception) {
            conn.rollback(); throw e
        } finally {
            conn.autoCommit = true
        }
    }
}

class AdminRepository(private val conn: Connection) {
    fun login(username: String, password: String): Boolean =
        conn.prepareStatement("SELECT 1 FROM admins WHERE username=? AND password=?;").use { ps ->
            ps.setString(1, username); ps.setString(2, password)
            ps.executeQuery().use { it.next() }
        }
}

class OfferRepository(private val conn: Connection) {
    fun activeFor(stationId: Long, on: LocalDate = LocalDate.now()): Offer? =
        conn.prepareStatement("""
            SELECT id, station_id, discount_percent, start_date, end_date
            FROM offers WHERE station_id=? AND start_date<=? AND end_date>=? ORDER BY id DESC LIMIT 1;
        """.trimIndent()).use { ps ->
            val s = on.toString()
            ps.setLong(1, stationId); ps.setString(2, s); ps.setString(3, s)
            ps.executeQuery().use { rs ->
                if (rs.next()) Offer(
                    rs.getLong(1), rs.getLong(2), rs.getDouble(3),
                    Dates.parse(rs.getString(4)), Dates.parse(rs.getString(5))
                ) else null
            }
        }

    fun list(): List<Offer> =
        conn.prepareStatement("SELECT id, station_id, discount_percent, start_date, end_date FROM offers ORDER BY id DESC;")
            .use { ps -> ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) add(
                        Offer(rs.getLong(1), rs.getLong(2), rs.getDouble(3),
                            Dates.parse(rs.getString(4)), Dates.parse(rs.getString(5)))
                    )
                }
            } }

    fun add(stationId: Long, percent: Double, start: LocalDate, end: LocalDate) {
        conn.prepareStatement("INSERT INTO offers(station_id,discount_percent,start_date,end_date) VALUES(?,?,?,?);")
            .use { ps ->
                ps.setLong(1, stationId); ps.setDouble(2, percent)
                ps.setString(3, start.toString()); ps.setString(4, end.toString())
                ps.executeUpdate()
            }
    }

    fun delete(id: Long) {
        conn.prepareStatement("DELETE FROM offers WHERE id=?;").use { ps -> ps.setLong(1,id); ps.executeUpdate() }
    }

    fun quote(station: Station): PriceQuote {
        val offer = activeFor(station.id)
        val f = if (offer != null) (1.0 - offer.discountPercent/100.0) else 1.0
        return PriceQuote(
            baseSingle = station.singlePrice,
            baseReturn = station.returnPrice,
            singleAfterOffer = station.singlePrice * f,
            returnAfterOffer = station.returnPrice * f,
            activeOffer = offer
        )
    }
}

class TicketRepository(private val conn: Connection) {
    fun log(origin: String, destination: String, type: TicketType, price: Double, cardNumber: String) {
        conn.prepareStatement("""
            INSERT INTO tickets(time_utc,origin,destination,type,price,card_number)
            VALUES(?,?,?,?,?,?);
        """.trimIndent()).use { ps ->
            ps.setString(1, OffsetDateTime.now(ZoneOffset.UTC).toString())
            ps.setString(2, origin)
            ps.setString(3, destination)
            ps.setString(4, type.name)
            ps.setDouble(5, price)
            ps.setString(6, cardNumber)
            ps.executeUpdate()
        }
    }
}
