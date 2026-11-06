package lat.pam.kopidannyemil

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class CartActivity : AppCompatActivity() {

    companion object {
        const val METHOD_BANK = "Transfer Bank"
        const val METHOD_EWALLET = "E-Wallet"
        const val METHOD_QRIS = "QRIS"
    }

    private var selectedMethod: String? = null

    private lateinit var llItems: LinearLayout
    private lateinit var tvEmpty: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvShipping: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvAddress: TextView
    private lateinit var payBank: View
    private lateinit var payEwallet: View
    private lateinit var payQris: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        llItems = findViewById(R.id.llItems)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvShipping = findViewById(R.id.tvShipping)
        tvTotal = findViewById(R.id.tvTotal)
        tvAddress = findViewById(R.id.tvAddress)
        payBank = findViewById(R.id.payBank)
        payEwallet = findViewById(R.id.payEwallet)
        payQris = findViewById(R.id.payQris)

        savedInstanceState?.getString("method")?.let { selectedMethod = it }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnClear).setOnClickListener {
            if (CartManager.isEmpty()) {
                toast("Keranjang sudah kosong")
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Kosongkan keranjang?")
                    .setMessage("Semua produk di keranjang akan dihapus.")
                    .setNegativeButton("Batal", null)
                    .setPositiveButton("Kosongkan") { _, _ ->
                        CartManager.clear()
                        refresh()
                    }
                    .show()
            }
        }

        tvAddress.setOnClickListener { startActivity(Intent(this, AddressActivity::class.java)) }

        payBank.setOnClickListener { selectMethod(METHOD_BANK) }
        payEwallet.setOnClickListener { selectMethod(METHOD_EWALLET) }
        payQris.setOnClickListener { selectMethod(METHOD_QRIS) }

        findViewById<Button>(R.id.btnCheckout).setOnClickListener { checkout() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("method", selectedMethod)
    }

    override fun onResume() {
        super.onResume()
        refresh() // juga memuat ulang alamat setelah kembali dari AddressActivity
    }

    private fun selectMethod(method: String) {
        selectedMethod = method
        updatePaymentUi()
    }

    private fun updatePaymentUi() {
        payBank.setBackgroundResource(if (selectedMethod == METHOD_BANK) R.drawable.bg_payment_selected else R.drawable.bg_payment)
        payEwallet.setBackgroundResource(if (selectedMethod == METHOD_EWALLET) R.drawable.bg_payment_selected else R.drawable.bg_payment)
        payQris.setBackgroundResource(if (selectedMethod == METHOD_QRIS) R.drawable.bg_payment_selected else R.drawable.bg_payment)
    }

    private fun refresh() {
        llItems.removeAllViews()
        val items = CartManager.getItems()
        tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE

        for (item in items) {
            val v = layoutInflater.inflate(R.layout.item_cart, llItems, false)
            v.findViewById<ImageView>(R.id.ivProduct).setImageResource(item.product.imageRes)
            v.findViewById<TextView>(R.id.tvName).text = item.product.name
            v.findViewById<TextView>(R.id.tvUnitPrice).text = formatRupiah(item.product.price)
            v.findViewById<TextView>(R.id.tvQty).text = item.quantity.toString()
            v.findViewById<TextView>(R.id.tvLineTotal).text =
                formatRupiah(item.product.price * item.quantity)

            v.findViewById<View>(R.id.btnPlus).setOnClickListener {
                CartManager.increase(item.product.id); refresh()
            }
            v.findViewById<View>(R.id.btnMinus).setOnClickListener {
                CartManager.decrease(item.product.id); refresh()
            }
            v.findViewById<View>(R.id.btnDelete).setOnClickListener {
                CartManager.remove(item.product.id); refresh()
            }
            llItems.addView(v)
        }

        tvSubtotal.text = formatRupiah(CartManager.subtotal())
        tvShipping.text = formatRupiah(CartManager.shipping())
        tvTotal.text = formatRupiah(CartManager.total())

        val address = AddressStore.get(this)
        tvAddress.text = if (address == null) {
            "Belum ada alamat pengiriman.\nKetuk di sini untuk menambahkan."
        } else {
            "${address.name} • ${address.phone}\n${address.fullText()}\n(Ketuk untuk mengubah)"
        }

        updatePaymentUi()
    }

    private fun checkout() {
        if (CartManager.isEmpty()) {
            toast("Keranjang masih kosong")
            return
        }
        val address = AddressStore.get(this)
        if (address == null) {
            toast("Isi alamat pengiriman terlebih dahulu")
            startActivity(Intent(this, AddressActivity::class.java))
            return
        }
        val method = selectedMethod
        if (method == null) {
            toast("Pilih metode pembayaran terlebih dahulu")
            return
        }

        val order = OrderStore.create(this, address, method)
        CartManager.clear()
        startActivity(
            Intent(this, ConfirmationActivity::class.java)
                .putExtra(ConfirmationActivity.EXTRA_ORDER_ID, order.id)
        )
        finish()
    }
}
