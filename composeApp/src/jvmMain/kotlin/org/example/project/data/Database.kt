package data

import java.sql.Connection
import java.sql.DriverManager
import java.time.LocalDate

/**
 * Main application database: stations, admins, offers, sales.
 * File: train.db
 */
object Database {
    private var conn: Connection? = null

    fun connection(): Connection {
        val existing = conn
        if (existing != null && !existing.isClosed) return existing
        val c = DriverManager.getConnection("jdbc:sqlite:train.db")
        c.createStatement().use { it.execute("PRAGMA foreign_keys = ON;") }
        ensureSchema(c)
        conn = c
        return c
    }

    private fun ensureSchema(c: Connection) {
        c.createStatement().use { st ->
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS stations(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  name TEXT UNIQUE NOT NULL,
                  single_price REAL NOT NULL,
                  return_price REAL NOT NULL,
                  sales_count INTEGER NOT NULL DEFAULT 0
                );
            """.trimIndent())
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS admins(
                  username TEXT PRIMARY KEY,
                  password TEXT NOT NULL
                );
            """.trimIndent())
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS offers(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  station_id INTEGER NOT NULL,
                  discount_percent REAL NOT NULL,
                  start_date TEXT NOT NULL,
                  end_date TEXT NOT NULL,
                  FOREIGN KEY(station_id) REFERENCES stations(id) ON DELETE CASCADE
                );
            """.trimIndent())
        }

        // Seed once
        c.createStatement().use { st ->
            val rs = st.executeQuery("SELECT COUNT(*) AS n FROM stations;")
            val count = if (rs.next()) rs.getInt("n") else 0
            if (count == 0) {
                c.autoCommit = false
                try {
                    // Stations
                    c.prepareStatement("INSERT INTO stations(name,single_price,return_price) VALUES(?,?,?);").use { ps ->
                        listOf(
                            Triple("Central", 3.20, 5.70),
                            Triple("North",   3.20, 5.70),
                            Triple("East",    3.80, 6.80),
                            Triple("West",    3.80, 6.80),
                            Triple("Airport", 8.50, 15.30),
                        ).forEach { (n,s,r) ->
                            ps.setString(1, n); ps.setDouble(2, s); ps.setDouble(3, r); ps.addBatch()
                        }
                        ps.executeBatch()
                    }
                    // Default admin
                    c.prepareStatement("INSERT OR IGNORE INTO admins(username,password) VALUES(?,?);").use {
                        it.setString(1, "admin"); it.setString(2, "admin"); it.executeUpdate()
                    }
                    // Example introductory offer for Airport
                    val airportId = c.createStatement().use { s ->
                        s.executeQuery("SELECT id FROM stations WHERE name='Airport'").use { r -> if (r.next()) r.getLong(1) else 1L }
                    }
                    c.prepareStatement("INSERT INTO offers(station_id,discount_percent,start_date,end_date) VALUES(?,?,?,?);").use {
                        it.setLong(1, airportId)
                        it.setDouble(2, 10.0)
                        it.setString(3, LocalDate.now().minusDays(1).toString())
                        it.setString(4, LocalDate.now().plusDays(30).toString())
                        it.executeUpdate()
                    }
                    c.commit()
                } catch (e: Exception) {
                    c.rollback(); throw e
                } finally {
                    c.autoCommit = true
                }
            }
        }
    }
}
