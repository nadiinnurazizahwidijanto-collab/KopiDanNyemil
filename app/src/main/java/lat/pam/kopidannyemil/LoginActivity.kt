package lat.pam.kopidannyemil

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        etPassword.enablePasswordToggle()

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<TextView>(R.id.tvRegister).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        findViewById<TextView>(R.id.tvForgot).setOnClickListener {
            toast("Fitur reset kata sandi belum tersedia (aplikasi demo)")
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            var ok = true
            if (email.isEmpty()) {
                etEmail.error = "Email wajib diisi"; ok = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Format email tidak valid"; ok = false
            }
            if (password.isEmpty()) { etPassword.error = "Password wajib diisi"; ok = false }
            if (!ok) return@setOnClickListener

            if (!UserStore.hasAccount(this)) {
                toast("Belum ada akun di perangkat ini. Silakan daftar dulu.")
                return@setOnClickListener
            }
            if (UserStore.checkLogin(this, email, password)) {
                UserStore.setLoggedIn(this, true)
                openClearTask(HomeActivity::class.java)
            } else {
                etPassword.error = "Email atau password salah"
            }
        }
    }
}
