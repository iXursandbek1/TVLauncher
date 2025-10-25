package uz.khursandbek.tvlauncher

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_browse_fragment, MainFragment())
                .commitNow()
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {
        // Prevent exiting launcher
        // Don't call super — launcher should stay open
        // Optional: show toast to indicate HOME button should be used
    }
}