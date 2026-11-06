package lat.pam.kopidannyemil

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val target = if (UserStore.isLoggedIn(this)) HomeActivity::class.java else WelcomeActivity::class.java
        openClearTask(target)
        finish()
    }
}
