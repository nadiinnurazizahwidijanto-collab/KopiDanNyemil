package lat.pam.kopidannyemil

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class RegisterActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_PASSWORD = "extra_password"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirm = findViewById<EditText>(R.id.etConfirm)
        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)
        val tvTerms = findViewById<TextView>(R.id.tvTerms)

        etPassword.enablePasswordToggle()
        etConfirm.enablePasswordToggle()

        etName.setText(intent.getStringExtra(EXTRA_NAME) ?: "")
        etEmail.setText(intent.getStringExtra(EXTRA_EMAIL) ?: "")
        etPassword.setText(intent.getStringExtra(EXTRA_PASSWORD) ?: "")

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        val full = "Saya setuju dengan Syarat & Ketentuan"
        val span = SpannableString(full)
        val start = full.indexOf("Syarat")
        val navy = ContextCompat.getColor(this, R.color.navy)
        span.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                showTerms()
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = navy
                ds.isUnderlineText = true
            }
        }, start, full.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvTerms.text = span
        tvTerms.movementMethod = LinkMovementMethod.getInstance()

        findViewById<Button>(R.id.btnCreate).setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString()
            val confirm = etConfirm.text.toString()

            var ok = true
            if (name.isEmpty()) {
                etName.error = "Nama lengkap wajib diisi"; ok = false
            }
            if (email.isEmpty()) {
                etEmail.error = "Email wajib diisi"; ok = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Format email tidak valid"; ok = false
            }
            if (phone.isEmpty()) {
                etPhone.error = "Nomor telepon wajib diisi"; ok = false
            } else if (phone.length !in 9..15) {
                etPhone.error = "Nomor telepon harus 9–15 digit"; ok = false
            }
            if (password.isEmpty()) {
                etPassword.error = "Password wajib diisi"; ok = false
            } else if (password.length < 6) {
                etPassword.error = "Password minimal 6 karakter"; ok = false
            }
            if (confirm != password) {
                etConfirm.error = "Konfirmasi password tidak cocok"; ok = false
            }
            if (!ok) return@setOnClickListener

            if (!cbTerms.isChecked) {
                toast("Centang persetujuan Syarat & Ketentuan terlebih dahulu")
                return@setOnClickListener
            }

            UserStore.register(this, name, email, phone, password)
            toast("Akun berhasil dibuat (simulasi)")
            openClearTask(HomeActivity::class.java)
        }
    }

    private fun showTerms() {
        val isi = """
Halo, Sahabat Ngopi! ☕
Sebelum membuat akun, mohon baca ketentuan singkat berikut.

HAK ANDA SEBAGAI PELANGGAN
• Mendapatkan pesanan sesuai menu, jumlah, dan harga yang tertera di aplikasi.
• Melihat rincian subtotal, ongkos kirim, dan total sebelum membayar.
• Memilih metode pembayaran: Transfer Bank, E-Wallet, atau QRIS.
• Melihat riwayat dan status pesanan di menu Riwayat Pesanan.
• Mendapat penggantian atau bantuan jika pesanan salah atau rusak saat diterima.
• Meminta perubahan atau penghapusan data pribadi Anda.

KEWAJIBAN ANDA SEBAGAI PELANGGAN
• Mengisi data akun dan alamat dengan benar dan lengkap.
• Menjaga kerahasiaan password. Aktivitas di akun Anda menjadi tanggung jawab Anda.
• Memastikan nomor telepon aktif agar kurir dapat menghubungi Anda.
• Membayar sesuai total pesanan melalui metode yang dipilih.
• Menerima pesanan di alamat yang dituju.

PEMESANAN DAN PEMBAYARAN
• Pesanan diproses setelah pembayaran kami terima dan terverifikasi.
• Kami berhak membatalkan pesanan jika stok habis atau terjadi kesalahan harga, dan akan memberi tahu Anda.

PEMBATALAN DAN KELUHAN
• Minuman dan makanan dibuat saat dipesan, sehingga pesanan yang sudah diproses tidak dapat dibatalkan.
• Jika pesanan salah, tumpah, atau rusak, hubungi kami maksimal 1 jam setelah pesanan diterima dan sertakan foto buktinya.

DATA PRIBADI
Nama, email, nomor telepon, dan alamat Anda hanya dipakai untuk memproses pesanan dan layanan akun. Data tidak dijual kepada pihak lain.

PERUBAHAN KETENTUAN
Ketentuan ini dapat diperbarui sewaktu-waktu. Dengan mencentang kotak persetujuan, Anda menyatakan telah membaca dan menyetujui ketentuan ini.

Butuh bantuan? Hubungi kami di kopindannyemil@gmail.com
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Syarat & Ketentuan Pelanggan")
            .setMessage(isi)
            .setPositiveButton("Mengerti", null)
            .show()
    }
}
