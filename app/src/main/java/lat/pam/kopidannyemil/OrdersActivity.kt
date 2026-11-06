package lat.pam.kopidannyemil

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OrdersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        val container = findViewById<LinearLayout>(R.id.llOrders)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        container.removeAllViews()

        val orders = OrderStore.all(this)
        tvEmpty.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE

        for (o in orders) {
            val v = layoutInflater.inflate(R.layout.item_order, container, false)
            v.findViewById<TextView>(R.id.tvOrderId).text = o.id
            v.findViewById<TextView>(R.id.tvDate).text = formatDate(o.createdAt)
            v.findViewById<TextView>(R.id.tvItems).text =
                o.lines.joinToString("\n") { "${it.qty}× ${it.name} — ${formatRupiah(it.price * it.qty)}" }
            v.findViewById<TextView>(R.id.tvAddress).text =
                "Dikirim ke: ${o.receiver}, ${o.addressText}"
            v.findViewById<TextView>(R.id.tvMethod).text = "Pembayaran: ${o.method}"
            v.findViewById<TextView>(R.id.tvStatus).text =
                "${o.orderStatus}\n${o.paymentStatus}"
            v.findViewById<TextView>(R.id.tvTotal).text = formatRupiah(o.total)
            container.addView(v)
        }
    }
}
