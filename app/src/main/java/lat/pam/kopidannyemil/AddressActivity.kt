package lat.pam.kopidannyemil

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AddressActivity : AppCompatActivity() {

    private lateinit var svAddress: ScrollView
    private lateinit var llAddresses: LinearLayout
    private lateinit var tvNone: TextView
    private lateinit var tvFormTitle: TextView
    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: EditText
    private lateinit var etCity: EditText
    private lateinit var etPostal: EditText
    private lateinit var cbPrimary: CheckBox
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private var editingIndex: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)

        svAddress = findViewById(R.id.svAddress)
        llAddresses = findViewById(R.id.llAddresses)
        tvNone = findViewById(R.id.tvNone)
        tvFormTitle = findViewById(R.id.tvFormTitle)
        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        etAddress = findViewById(R.id.etAddress)
        etCity = findViewById(R.id.etCity)
        etPostal = findViewById(R.id.etPostal)
        cbPrimary = findViewById(R.id.cbPrimary)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        btnCancel.setOnClickListener { clearForm() }
        btnSave.setOnClickListener { saveAddress() }

        clearForm()
        renderList()
    }

    private fun clearForm() {
        editingIndex = null
        etName.setText("")
        etPhone.setText("")
        etAddress.setText("")
        etCity.setText("")
        etPostal.setText("")
        etName.error = null
        etPhone.error = null
        etAddress.error = null
        etCity.error = null
        etPostal.error = null
        cbPrimary.isChecked = AddressBook.all(this).isEmpty()
        tvFormTitle.text = "Tambah Alamat Baru"
        btnSave.text = "Simpan Alamat"
        btnCancel.visibility = View.GONE
    }

    private fun startEdit(index: Int) {
        val a = AddressBook.all(this).getOrNull(index) ?: return
        editingIndex = index
        etName.setText(a.name)
        etPhone.setText(a.phone)
        etAddress.setText(a.street)
        etCity.setText(a.city)
        etPostal.setText(a.postal)
        cbPrimary.isChecked = a.primary
        tvFormTitle.text = "Ubah Alamat"
        btnSave.text = "Perbarui Alamat"
        btnCancel.visibility = View.VISIBLE
        svAddress.post { svAddress.smoothScrollTo(0, tvFormTitle.top) }
    }

    private fun renderList() {
        llAddresses.removeAllViews()
        val list = AddressBook.all(this)
        tvNone.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE

        list.forEachIndexed { index, a ->
            val v = layoutInflater.inflate(R.layout.item_address, llAddresses, false)
            v.findViewById<TextView>(R.id.tvLabel).text = "${a.name} • ${a.phone}"
            v.findViewById<TextView>(R.id.tvFull).text = a.fullText()
            v.findViewById<View>(R.id.tvBadge).visibility = if (a.primary) View.VISIBLE else View.GONE
            v.findViewById<View>(R.id.tvUse).visibility = if (a.primary) View.GONE else View.VISIBLE

            v.findViewById<View>(R.id.tvUse).setOnClickListener {
                AddressBook.setPrimary(this, index)
                toast("Alamat utama diganti")
                renderList()
            }
            v.findViewById<View>(R.id.tvEdit).setOnClickListener { startEdit(index) }
            v.findViewById<View>(R.id.tvDelete).setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Hapus alamat?")
                    .setMessage(a.fullText())
                    .setNegativeButton("Batal", null)
                    .setPositiveButton("Hapus") { _, _ ->
                        AddressBook.delete(this, index)
                        clearForm()
                        renderList()
                        toast("Alamat dihapus")
                    }
                    .show()
            }
            llAddresses.addView(v)
        }
    }

    private fun saveAddress() {
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val street = etAddress.text.toString().trim()
        val city = etCity.text.toString().trim()
        val postal = etPostal.text.toString().trim()

        var ok = true
        if (name.isEmpty()) { etName.error = "Nama penerima wajib diisi"; ok = false }
        if (phone.length !in 9..15) { etPhone.error = "Nomor telepon harus 9–15 digit"; ok = false }
        if (street.length < 5) { etAddress.error = "Alamat lengkap wajib diisi"; ok = false }
        if (city.isEmpty()) { etCity.error = "Kota/Kecamatan wajib diisi"; ok = false }
        if (postal.length != 5) { etPostal.error = "Kode pos harus 5 digit"; ok = false }
        if (!ok) return

        val wasEditing = editingIndex != null
        AddressBook.save(this, Address(name, phone, street, city, postal, cbPrimary.isChecked), editingIndex)
        toast(if (wasEditing) "Alamat diperbarui" else "Alamat baru disimpan")
        clearForm()
        renderList()
    }
}