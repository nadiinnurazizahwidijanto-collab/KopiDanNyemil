package lat.pam.kopidannyemil

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class HomeActivity : AppCompatActivity() {

    private var category = "Semua"
    private lateinit var grid: GridLayout
    private lateinit var etSearch: EditText
    private lateinit var tvEmpty: TextView
    private lateinit var tvBadge: TextView
    private lateinit var chips: List<Pair<TextView, String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        grid = findViewById(R.id.gridProducts)
        etSearch = findViewById(R.id.etSearch)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvBadge = findViewById(R.id.tvBadge)

        chips = listOf(
            findViewById<TextView>(R.id.chipAll) to "Semua",
            findViewById<TextView>(R.id.chipCoffee) to "Kopi",
            findViewById<TextView>(R.id.chipNonCoffee) to "Non-Kopi",
            findViewById<TextView>(R.id.chipSnack) to "Snack"
        )
        chips.forEach { (view, name) ->
            view.setOnClickListener {
                category = name
                updateChips()
                render()
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) = render()
        })

        findViewById<View>(R.id.btnCart).setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
        findViewById<View>(R.id.navOrders).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }
        findViewById<View>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        updateChips()
        render()
    }

    override fun onResume() {
        super.onResume()
        updateBadge()
    }

    private fun updateBadge() {
        val count = CartManager.totalQuantity()
        tvBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
        tvBadge.text = if (count > 99) "99+" else count.toString()
    }

    private fun updateChips() {
        chips.forEach { (view, name) ->
            val active = name == category
            view.setBackgroundResource(if (active) R.drawable.bg_category_active else R.drawable.bg_category)
            view.setTextColor(ContextCompat.getColor(this, if (active) R.color.white else R.color.navy))
        }
    }


    private fun gridParams(): GridLayout.LayoutParams {
        val lp = GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED),
            GridLayout.spec(GridLayout.UNDEFINED, 1f)
        )
        lp.width = 0
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
        lp.setMargins(dp(4), dp(4), dp(4), dp(4))
        return lp
    }

    private fun render() {
        val query = etSearch.text.toString().trim().lowercase()
        val list = DummyData.products.filter {
            (category == "Semua" || it.category == category) && it.name.lowercase().contains(query)
        }

        grid.removeAllViews()
        tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE

        for (product in list) {
            val card = layoutInflater.inflate(R.layout.item_product, grid, false)
            card.findViewById<ImageView>(R.id.ivProduct).setImageResource(product.imageRes)
            card.findViewById<TextView>(R.id.tvName).text = product.name
            card.findViewById<TextView>(R.id.tvPrice).text = formatRupiah(product.price)
            card.findViewById<View>(R.id.btnAdd).setOnClickListener {
                CartManager.add(product)
                updateBadge()
                toast("${product.name} ditambahkan ke keranjang")
            }
            grid.addView(card, gridParams())
        }


        val sisa = list.size % 3
        if (list.isNotEmpty() && sisa != 0) {
            repeat(3 - sisa) {
                val filler = layoutInflater.inflate(R.layout.item_product, grid, false)
                filler.visibility = View.INVISIBLE
                grid.addView(filler, gridParams())
            }
        }
    }
}