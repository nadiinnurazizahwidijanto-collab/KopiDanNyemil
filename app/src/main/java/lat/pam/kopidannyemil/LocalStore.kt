package lat.pam.kopidannyemil

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.InputType
import android.view.MotionEvent
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

fun formatRupiah(value: Int): String {
    val digits = value.toString().reversed().chunked(3).joinToString(".").reversed()
    return "Rp$digits"
}

fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date(timestamp))

fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.openClearTask(target: Class<*>) {
    startActivity(
        Intent(this, target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    )
}


@SuppressLint("ClickableViewAccessibility")
fun EditText.enablePasswordToggle() {
    var visible = false
    fun applyState() {
        inputType = InputType.TYPE_CLASS_TEXT or
                (if (visible) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                else InputType.TYPE_TEXT_VARIATION_PASSWORD)
        val icon = ContextCompat.getDrawable(
            context, if (visible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
        )
        val start = compoundDrawablesRelative[0]
        setCompoundDrawablesRelativeWithIntrinsicBounds(start, null, icon, null)
        setSelection(text.length)
    }
    applyState()
    setOnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            val end = compoundDrawablesRelative[2]
            if (end != null) {
                val hitStart = width - paddingEnd - end.bounds.width() - context.dp(8)
                if (event.x >= hitStart) {
                    visible = !visible
                    applyState()
                    v.performClick()
                    return@setOnTouchListener true
                }
            }
        }
        false
    }
}

// ---------------------------------------------------------------
// Akun pengguna (SIMULASI, disimpan lokal; password di-hash, bukan teks biasa)
// ---------------------------------------------------------------
object UserStore {
    private const val PREF = "kn_user"

    private fun p(c: Context) = c.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    private fun hash(password: String, salt: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest((salt + password).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun register(c: Context, name: String, email: String, phone: String, password: String) {
        val salt = UUID.randomUUID().toString()
        p(c).edit()
            .putString("name", name)
            .putString("email", email.lowercase())
            .putString("phone", phone)
            .putString("salt", salt)
            .putString("hash", hash(password, salt))
            .putBoolean("loggedIn", true)
            .apply()
    }

    fun hasAccount(c: Context) = p(c).contains("hash")

    fun checkLogin(c: Context, email: String, password: String): Boolean {
        val pref = p(c)
        val salt = pref.getString("salt", null) ?: return false
        return pref.getString("email", "") == email.lowercase() &&
                pref.getString("hash", "") == hash(password, salt)
    }

    fun setLoggedIn(c: Context, value: Boolean) = p(c).edit().putBoolean("loggedIn", value).apply()
    fun isLoggedIn(c: Context) = p(c).getBoolean("loggedIn", false)
    fun name(c: Context) = p(c).getString("name", "") ?: ""
    fun email(c: Context) = p(c).getString("email", "") ?: ""
    fun phone(c: Context) = p(c).getString("phone", "") ?: ""
}

// ---------------------------------------------------------------
// Alamat pengiriman
// ---------------------------------------------------------------
data class Address(
    val name: String,
    val phone: String,
    val street: String,
    val city: String,
    val postal: String,
    val primary: Boolean
) {
    fun fullText() = "$street, $city $postal"
}

object AddressStore {
    private const val PREF = "kn_address"

    private fun p(c: Context) = c.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun save(c: Context, a: Address) {
        val o = JSONObject()
            .put("name", a.name).put("phone", a.phone).put("street", a.street)
            .put("city", a.city).put("postal", a.postal).put("primary", a.primary)
        p(c).edit().putString("address", o.toString()).apply()
    }

    fun get(c: Context): Address? {
        val raw = p(c).getString("address", null) ?: return null
        return try {
            val o = JSONObject(raw)
            Address(
                o.getString("name"), o.getString("phone"), o.getString("street"),
                o.getString("city"), o.getString("postal"), o.optBoolean("primary", true)
            )
        } catch (e: Exception) {
            null
        }
    }
}

// ---------------------------------------------------------------
// Pesanan
// ---------------------------------------------------------------
data class OrderLine(val name: String, val qty: Int, val price: Int)

data class Order(
    val id: String,
    val createdAt: Long,
    val receiver: String,
    val phone: String,
    val addressText: String,
    val lines: List<OrderLine>,
    val method: String,
    val subtotal: Int,
    val shipping: Int,
    val total: Int,
    val orderStatus: String,
    val paymentStatus: String
)

object OrderStore {
    private const val PREF = "kn_orders"

    private fun p(c: Context) = c.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    private fun readArray(c: Context): JSONArray =
        try { JSONArray(p(c).getString("orders", "[]")) } catch (e: Exception) { JSONArray() }

    /** Semua pesanan, yang terbaru di urutan pertama. */
    fun all(c: Context): List<Order> {
        val arr = readArray(c)
        val list = mutableListOf<Order>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val linesJson = o.getJSONArray("lines")
            val lines = (0 until linesJson.length()).map {
                val l = linesJson.getJSONObject(it)
                OrderLine(l.getString("name"), l.getInt("qty"), l.getInt("price"))
            }
            list.add(
                Order(
                    o.getString("id"), o.getLong("createdAt"), o.getString("receiver"),
                    o.getString("phone"), o.getString("addressText"), lines,
                    o.getString("method"), o.getInt("subtotal"), o.getInt("shipping"),
                    o.getInt("total"), o.getString("orderStatus"), o.getString("paymentStatus")
                )
            )
        }
        return list.reversed()
    }

    fun find(c: Context, id: String): Order? = all(c).find { it.id == id }

    /** Membuat pesanan dari isi keranjang saat ini lalu menyimpannya. */
    fun create(c: Context, address: Address, method: String): Order {
        val pref = p(c)
        val counter = pref.getInt("counter", 0) + 1
        val datePart = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val id = "#KN" + datePart + "-" + "%03d".format(counter)

        val lines = CartManager.getItems().map { OrderLine(it.product.name, it.quantity, it.product.price) }
        val order = Order(
            id = id,
            createdAt = System.currentTimeMillis(),
            receiver = address.name,
            phone = address.phone,
            addressText = address.fullText(),
            lines = lines,
            method = method,
            subtotal = CartManager.subtotal(),
            shipping = CartManager.shipping(),
            total = CartManager.total(),
            orderStatus = "Pesanan dibuat — Dalam Proses",
            // Belum ada integrasi penyedia pembayaran, jadi TIDAK PERNAH otomatis "berhasil".
            paymentStatus = "Menunggu Pembayaran (SIMULASI)"
        )

        val linesJson = JSONArray()
        lines.forEach {
            linesJson.put(JSONObject().put("name", it.name).put("qty", it.qty).put("price", it.price))
        }
        val obj = JSONObject()
            .put("id", order.id).put("createdAt", order.createdAt)
            .put("receiver", order.receiver).put("phone", order.phone)
            .put("addressText", order.addressText).put("lines", linesJson)
            .put("method", order.method).put("subtotal", order.subtotal)
            .put("shipping", order.shipping).put("total", order.total)
            .put("orderStatus", order.orderStatus).put("paymentStatus", order.paymentStatus)

        val arr = readArray(c)
        arr.put(obj)
        pref.edit().putString("orders", arr.toString()).putInt("counter", counter).apply()
        return order
    }
}
