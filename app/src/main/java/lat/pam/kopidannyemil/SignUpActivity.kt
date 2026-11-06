package lat.pam.kopidannyemil

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        etPassword.enablePasswordToggle()

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<TextView>(R.id.tvLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<Button>(R.id.btnSignUp).setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            var ok = true
            if (name.isEmpty()) { etName.error = "Nama lengkap wajib diisi"; ok = false }
            if (email.isEmpty()) {
                etEmail.error = "Email wajib diisi"; ok = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Format email tidak valid"; ok = false
            }
            if (password.isEmpty()) {
                etPassword.error = "Password wajib diisi"; ok = false
            } else if (password.length < 6) {
                etPassword.error = "Password minimal 6 karakter"; ok = false
            }
            if (!ok) return@setOnClickListener

            startActivity(
                Intent(this, RegisterActivity::class.java)
                    .putExtra(RegisterActivity.EXTRA_NAME, name)
                    .putExtra(RegisterActivity.EXTRA_EMAIL, email)
                    .putExtra(RegisterActivity.EXTRA_PASSWORD, password)
            )
        }
    }
}
