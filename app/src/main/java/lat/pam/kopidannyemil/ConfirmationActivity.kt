package lat.pam.kopidannyemil

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class ConfirmationActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ORDER_ID = "extra_order_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        val order = intent.getStringExtra(EXTRA_ORDER_ID)?.let { OrderStore.find(this, it) }
        if (order == null) {
            toast("Data pesanan tidak ditemukan")
            openClearTask(HomeActivity::class.java)
            return
        }

        findViewById<TextView>(R.id.tvOrderId).text = order.id
        findViewById<TextView>(R.id.tvMethod).text = order.method
        findViewById<TextView>(R.id.tvOrderStatus).text = order.orderStatus
        findViewById<TextView>(R.id.tvPaymentStatus).text = order.paymentStatus
        findViewById<TextView>(R.id.tvTotal).text = formatRupiah(order.total)
        findViewById<TextView>(R.id.tvAddress).text =
            "${order.receiver} • ${order.phone}\n${order.addressText}"

        val summary = StringBuilder()
        order.lines.forEach {
            summary.append("${it.qty}× ${it.name} — ${formatRupiah(it.price * it.qty)}\n")
        }
        summary.append("Subtotal: ${formatRupiah(order.subtotal)}\n")
        summary.append("Ongkos kirim: ${formatRupiah(order.shipping)}")
        findViewById<TextView>(R.id.tvItems).text = summary.toString()

        val tvPayTitle = findViewById<TextView>(R.id.tvPayTitle)
        val tvInstruction = findViewById<TextView>(R.id.tvPayInstruction)
        val ivQr = findViewById<ImageView>(R.id.ivQr)
        val nominal = formatRupiah(order.total)

        when (order.method) {
            CartActivity.METHOD_BANK -> {
                tvPayTitle.text = "Instruksi Transfer Bank"
                tvInstruction.text =
                    "Transfer sebesar $nominal ke rekening berikut:\n\n" +
                            "Bank DEMO — 0000 0000 00\na.n. Kopi dan Nyemil\n\n" +
                            "(DATA DEMO — bukan rekening sungguhan, jangan transfer uang.)"
            }
            CartActivity.METHOD_EWALLET -> {
                tvPayTitle.text = "Instruksi E-Wallet"
                tvInstruction.text =
                    "Bayar sebesar $nominal melalui aplikasi E-Wallet Anda ke nomor demo 0812 0000 0000.\n\n" +
                            "(SIMULASI — belum terhubung ke penyedia E-Wallet, jadi tidak ada pembayaran yang diproses.)"
            }
            else -> {
                tvPayTitle.text = "Pembayaran QRIS"
                ivQr.visibility = View.VISIBLE
                tvInstruction.text =
                    "Nominal yang harus dibayar: $nominal.\n" +
                            "Gambar QRIS di atas hanya ilustrasi. QRIS."
            }
        }

        findViewById<View>(R.id.btnViewOrders).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }
        findViewById<View>(R.id.btnHome).setOnClickListener { goHome() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goHome()
        })
    }

    private fun goHome() = openClearTask(HomeActivity::class.java)
}