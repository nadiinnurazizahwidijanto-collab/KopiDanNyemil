package lat.pam.kopidannyemil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        findViewById<Button>(R.id.btnStart).setOnClickListener {

            val target = if (UserStore.hasAccount(this)) LoginActivity::class.java else SignUpActivity::class.java
            startActivity(Intent(this, target))
        }
    }
}
