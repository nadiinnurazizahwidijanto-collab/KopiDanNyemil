package lat.pam.kopidannyemil

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {

    companion object {
        private const val PHOTO_FILE = "profile.jpg"
        private const val HELP_NUMBER = "08000000000"
        private const val HELP_WA_NUMBER = "628000000000"
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            if (savePhoto(uri)) loadAvatar() else toast("Gagal memuat foto")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<View>(R.id.ivAvatar).setOnClickListener { pickImage.launch("image/*") }
        findViewById<View>(R.id.btnEditPhoto).setOnClickListener { pickImage.launch("image/*") }

        findViewById<View>(R.id.menuPersonal).setOnClickListener { showEditProfile() }
        findViewById<View>(R.id.menuAddress).setOnClickListener {
            startActivity(Intent(this, AddressActivity::class.java))
        }
        findViewById<View>(R.id.menuOrders).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }
        findViewById<View>(R.id.menuHelp).setOnClickListener { showHelp() }
        findViewById<View>(R.id.menuLogout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Keluar")
                .setMessage("Yakin ingin keluar dari akun?")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Keluar") { _, _ ->
                    UserStore.setLoggedIn(this, false)
                    CartManager.clear()
                    openClearTask(WelcomeActivity::class.java)
                }
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        bindHeader()
        loadAvatar()
    }

    private fun bindHeader() {
        findViewById<TextView>(R.id.tvName).text = UserStore.name(this).ifEmpty { "Pengguna Demo" }
        findViewById<TextView>(R.id.tvEmail).text =
            UserStore.email(this).ifEmpty { "demo@kopidannyemil.id" }
    }


    private fun savePhoto(uri: Uri): Boolean = try {
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(File(filesDir, PHOTO_FILE)).use { out -> input.copyTo(out) }
        } != null
    } catch (e: Exception) {
        false
    }

    private fun loadAvatar() {
        val iv = findViewById<ImageView>(R.id.ivAvatar)
        val file = File(filesDir, PHOTO_FILE)
        val bmp = if (file.exists()) decodePhoto(file) else null

        if (bmp != null) {
            iv.imageTintList = null
            iv.setPadding(0, 0, 0, 0)
            iv.scaleType = ImageView.ScaleType.CENTER_CROP
            iv.setImageBitmap(bmp)
        } else {
            iv.setImageResource(R.drawable.ic_person)
            iv.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navy))
            iv.scaleType = ImageView.ScaleType.FIT_CENTER
            iv.setPadding(dp(24), dp(24), dp(24), dp(24))
        }
    }

    private fun decodePhoto(file: File): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        var sample = 1
        while (bounds.outWidth / sample > 800 || bounds.outHeight / sample > 800) sample *= 2

        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bmp = BitmapFactory.decodeFile(file.path, opts) ?: return null

        val orientation = try {
            ExifInterface(file.path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) return bmp
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
    }

    private fun showEditProfile() {
        val view = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etName = view.findViewById<EditText>(R.id.etEditName)
        val etEmail = view.findViewById<EditText>(R.id.etEditEmail)
        val etPhone = view.findViewById<EditText>(R.id.etEditPhone)
        etName.setText(UserStore.name(this))
        etEmail.setText(UserStore.email(this))
        etPhone.setText(UserStore.phone(this))

        val dialog = AlertDialog.Builder(this)
            .setTitle("Ubah Data Pribadi")
            .setView(view)
            .setNegativeButton("Batal", null)
            .setPositiveButton("Simpan", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = etName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val phone = etPhone.text.toString().trim()

                var ok = true
                if (name.isEmpty()) { etName.error = "Nama wajib diisi"; ok = false }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.error = "Format email tidak valid"; ok = false
                }
                if (phone.length !in 9..15) { etPhone.error = "Nomor telepon harus 9–15 digit"; ok = false }
                if (!ok) return@setOnClickListener

                ProfileEditor.update(this, name, email, phone)
                bindHeader()
                toast("Data pribadi diperbarui")
                dialog.dismiss()
            }
        }
        dialog.show()
    }


    private fun showHelp() {
        AlertDialog.Builder(this)
            .setTitle("Bantuan")
            .setMessage("Butuh bantuan? Silakan chat ke nomor berikut:\n\n$HELP_NUMBER")
            .setNeutralButton("Buka WhatsApp") { _, _ ->
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$HELP_WA_NUMBER")))
                } catch (e: ActivityNotFoundException) {
                    toast("Tidak dapat membuka WhatsApp")
                }
            }
            .setPositiveButton("Tutup", null)
            .show()
    }
}

object ProfileEditor {
    fun update(c: Context, name: String, email: String, phone: String) {
        c.getSharedPreferences("kn_user", Context.MODE_PRIVATE).edit()
            .putString("name", name)
            .putString("email", email.lowercase())
            .putString("phone", phone)
            .apply()
    }
}
