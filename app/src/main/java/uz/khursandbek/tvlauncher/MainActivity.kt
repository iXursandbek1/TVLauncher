package uz.khursandbek.tvlauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("TVLauncher", "MainActivity onCreate called")
        Log.d("TVLauncher", "Intent action: ${intent.action}")
        Log.d("TVLauncher", "Intent categories: ${intent.categories}")

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_browse_fragment, MainFragment())
                .commitNow()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(
                    this@MainActivity,
                    "You're already on the Home screen",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // Check if we're the default home app
        checkIfDefaultLauncher()
    }

    private fun checkIfDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }

        val packageManager = packageManager
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        val currentHomePackage = resolveInfo?.activityInfo?.packageName

        Log.d("TVLauncher", "Current home launcher: $currentHomePackage")
        Log.d("TVLauncher", "Our package: ${packageName}")

        val isDefaultLauncher = currentHomePackage == packageName

        if (!isDefaultLauncher) {
            // Show how to set as default
            Toast.makeText(
                this,
                "Please set TV Launcher as Home app in Settings",
                Toast.LENGTH_LONG
            ).show()

            // Try to open settings (may not work on all devices)
            try {
                val settingsIntent = Intent(Settings.ACTION_HOME_SETTINGS)
                startActivity(settingsIntent)
            } catch (e: Exception) {
                Log.e("TVLauncher", "Could not open home settings", e)
            }
        }

        return isDefaultLauncher
    }

    override fun onResume() {
        super.onResume()
        Log.d("TVLauncher", "MainActivity onResume called")
    }
}