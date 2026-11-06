package lat.pam.kopidannyemil

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object AddressBook {
    private const val PREF = "kn_addressbook"
    private const val KEY = "list"

    private fun p(c: Context) = c.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun all(c: Context): List<Address> {
        val raw = p(c).getString(KEY, null)
        if (raw == null) {
            val old = AddressStore.get(c)
            if (old != null) {
                val migrated = listOf(old.copy(primary = true))
                write(c, migrated)
                return migrated
            }
            return emptyList()
        }
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Address(
                    o.getString("name"), o.getString("phone"), o.getString("street"),
                    o.getString("city"), o.getString("postal"), o.optBoolean("primary", false)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun write(c: Context, list: List<Address>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject()
                    .put("name", it.name).put("phone", it.phone).put("street", it.street)
                    .put("city", it.city).put("postal", it.postal).put("primary", it.primary)
            )
        }
        p(c).edit().putString(KEY, arr.toString()).apply()
    }

    fun primary(c: Context): Address? {
        val list = all(c)
        return list.find { it.primary } ?: list.firstOrNull()
    }

    fun save(c: Context, address: Address, index: Int?) {
        val list = all(c).toMutableList()
        val pos = if (index != null && index in list.indices) index else list.size
        if (pos < list.size) list[pos] = address else list.add(address)

        val fixed = when {
            address.primary -> list.mapIndexed { i, a -> a.copy(primary = i == pos) }
            list.none { it.primary } -> list.mapIndexed { i, a -> a.copy(primary = i == 0) }
            else -> list
        }
        write(c, fixed)
    }

    fun setPrimary(c: Context, index: Int) {
        write(c, all(c).mapIndexed { i, a -> a.copy(primary = i == index) })
    }

    fun delete(c: Context, index: Int) {
        val list = all(c).toMutableList()
        if (index !in list.indices) return
        list.removeAt(index)
        val fixed = if (list.isNotEmpty() && list.none { it.primary }) {
            list.mapIndexed { i, a -> a.copy(primary = i == 0) }
        } else list
        write(c, fixed)
    }
}