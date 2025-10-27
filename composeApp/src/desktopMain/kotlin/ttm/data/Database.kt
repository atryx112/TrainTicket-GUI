package ttm.data

import java.sql.Connection
import java.sql.DriverManager
import java.time.LocalDate

object Database {
    private var conn: Connection? = null

    fun connection(): Connection {
        val c = conn?.takeIf { !it.isClosed } ?: DriverManager.getConnection("jdbc:sqlite:train.db")
        conn = c
        c.createStatement().use { it.execute("PRAGMA foreign_keys = ON;") }
        ensureSchema(c)
        seedIfEmpty(c)
        return c
    }

    private fun ensureSchema(c: Connection) {
        c.createStatement().use { st ->
            st.execute(
                """
                CREATE TABLE IF NOT EXISTS stations(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  name TEXT UNIQUE NOT NULL,
                  single_price REAL NOT NULL,
                  return_price REAL NOT NULL,
                  sales_count INTEGER NOT NULL DEFAULT 0
                );
                """.trimIndent()
            )
            st.execute(
                """
                CREATE TABLE IF NOT EXISTS cards(
                  card_number TEXT PRIMARY KEY,
                  credit REAL NOT NULL
                );
                """.trimIndent()
            )
            st.execute(
                """
                CREATE TABLE IF NOT EXISTS admins(
                  username TEXT PRIMARY KEY,
                  password TEXT NOT NULL
                );
                """.trimIndent()
            )
            st.execute(
                """
                CREATE TABLE IF NOT EXISTS offers(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  station_id INTEGER NOT NULL,
                  discount_percent REAL NOT NULL,
                  start_date TEXT NOT NULL,
                  end_date TEXT NOT NULL,
                  FOREIGN KEY(station_id) REFERENCES stations(id) ON DELETE CASCADE
                );
                """.trimIndent()
            )
            st.execute(
                """
                CREATE TABLE IF NOT EXISTS tickets(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  time_utc TEXT NOT NULL,
                  origin TEXT NOT NULL,
                  destination TEXT NOT NULL,
                  type TEXT NOT NULL,
                  price REAL NOT NULL,
                  card_number TEXT NOT NULL
                );
                """.trimIndent()
            )
        }
    }

    private fun seedIfEmpty(c: Connection) {
        c.createStatement().use { st ->
            val rs = st.executeQuery("SELECT COUNT(*) AS n FROM stations")
            val empty = rs.next() && rs.getInt("n") == 0
            if (empty) {
                c.autoCommit = false
                try {
                    st.executeUpdate(
                        "INSERT INTO stations(name,single_price,return_price) VALUES" +
                        "('Airport',8.50,15.00)," +
                        "('North',3.20,5.80)," +
                        "('East',3.80,6.70)," +
                        "('West',3.80,6.70)," +
                        "('Harbor',5.20,9.50)"
                    )
                    st.executeUpdate(
                        "INSERT INTO cards(card_number,credit) VALUES" +
                        "('4242424242424242', 30.00)," +
                        "('4000000000009995', 5.00)," +
                        "('5555555555554444', 50.00)"
                    )
                    st.executeUpdate(
                        "INSERT INTO admins(username,password) VALUES ('admin','admin123')"
                    )
                    val today = LocalDate.now()
                    val start = today.withDayOfMonth(1).toString()
                    val end = today.withDayOfMonth(today.lengthOfMonth()).toString()
                    st.executeUpdate(
                        "INSERT INTO offers(station_id,discount_percent,start_date,end_date) " +
                        "SELECT id, 10.0, '" + start + "', '" + end + "' FROM stations WHERE name='Airport'"
                    )
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