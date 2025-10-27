package uz.khursandbek.tvlauncher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.OnItemViewSelectedListener
import uz.khursandbek.tvlauncher.utils.AppUtils

class MainFragment : BrowseSupportFragment() {

    private lateinit var rowsAdapter: ArrayObjectAdapter
    private var allApps: List<ResolveInfo> = emptyList()

    @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupUI()
        loadApps()
        setupListeners()
    }

    private fun setupUI() {
        title = getString(R.string.launcher_title)

        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        brandColor = ContextCompat.getColor(requireContext(), R.color.brand_primary)

        searchAffordanceColor = ContextCompat.getColor(requireContext(), R.color.brand_accent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadApps() {
        allApps = getInstalledApps()
        buildRows()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildRows() {
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        val filteredApps = allApps.filter {
            it.activityInfo.packageName != requireContext().packageName
        }

        if (filteredApps.isEmpty()) {
            showEmptyState()
            return
        }

        val sortedApps = filteredApps.sortedBy {
            it.loadLabel(requireContext().packageManager).toString().lowercase()
        }

        addAppsSection(
            title = getString(R.string.section_all_apps),
            apps = sortedApps,
            headerId = 0
        )

        val recentApps = AppUtils.getRecentlyInstalledApps(
            requireContext(),
            filteredApps,
            daysAgo = 30
        )
        if (recentApps.isNotEmpty()) {
            addAppsSection(
                title = getString(R.string.section_recent),
                apps = recentApps,
                headerId = 1
            )
        }

        val games = filteredApps.filter { AppUtils.isGame(it) }
            .sortedBy { it.loadLabel(requireContext().packageManager).toString().lowercase() }
        if (games.isNotEmpty()) {
            addAppsSection(
                title = getString(R.string.section_games),
                apps = games,
                headerId = 2
            )
        }

        val videoApps = filteredApps.filter { AppUtils.isVideoApp(it) }
            .sortedBy { it.loadLabel(requireContext().packageManager).toString().lowercase() }
        if (videoApps.isNotEmpty()) {
            addAppsSection(
                title = getString(R.string.section_video),
                apps = videoApps,
                headerId = 3
            )
        }

        val musicApps = filteredApps.filter { AppUtils.isMusicApp(it) }
            .sortedBy { it.loadLabel(requireContext().packageManager).toString().lowercase() }
        if (musicApps.isNotEmpty()) {
            addAppsSection(
                title = getString(R.string.section_music),
                apps = musicApps,
                headerId = 4
            )
        }

        val systemApps = filteredApps.filter { AppUtils.isSystemApp(it) }
            .sortedBy { it.loadLabel(requireContext().packageManager).toString().lowercase() }
        if (systemApps.isNotEmpty()) {
            addAppsSection(
                title = getString(R.string.section_system),
                apps = systemApps,
                headerId = 5
            )
        }

        adapter = rowsAdapter
        restoreLastPosition()
    }

    private fun addAppsSection(
        title: String,
        apps: List<ResolveInfo>,
        headerId: Long
    ) {
        val cardPresenter = AppCardPresenter()
        val listRowAdapter = ArrayObjectAdapter(cardPresenter)

        apps.forEach { app ->
            listRowAdapter.add(app)
        }

        val header = HeaderItem(headerId, title)
        rowsAdapter.add(ListRow(header, listRowAdapter))
    }

    private fun showEmptyState() {
        Toast.makeText(
            requireContext(),
            getString(R.string.no_apps_found),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun setupListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is ResolveInfo) {
                launchApp(item)
            }
        }

        onItemViewSelectedListener = OnItemViewSelectedListener { _, item, _, row ->
            if (item is ResolveInfo) {
                val rowIndex = (row as? ListRow)?.id?.toInt() ?: 0
                saveLastPosition(rowIndex)
            }
        }
    }

    private fun launchApp(app: ResolveInfo) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setClassName(app.activityInfo.packageName, app.activityInfo.name)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            startActivity(intent)
            saveLastLaunchedApp(app.activityInfo.packageName)

        } catch (e: Exception) {
            try {
                val fallbackIntent = requireContext().packageManager
                    .getLaunchIntentForPackage(app.activityInfo.packageName)
                if (fallbackIntent != null) {
                    startActivity(fallbackIntent)
                } else {
                    showLaunchError()
                }
            } catch (e2: Exception) {
                showLaunchError()
            }
        }
    }

    private fun showLaunchError() {
        Toast.makeText(
            requireContext(),
            getString(R.string.app_launch_failed),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun getInstalledApps(): List<ResolveInfo> {
        val pm = requireContext().packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return pm.queryIntentActivities(mainIntent, 0)
    }

    private fun saveLastPosition(position: Int) {
        val prefs = requireContext().getSharedPreferences("launcher_prefs", 0)
        prefs.edit { putInt("last_position", position) }
    }

    private fun restoreLastPosition() {
        val prefs = requireContext().getSharedPreferences("launcher_prefs", 0)
        val lastPosition = prefs.getInt("last_position", 0)

        if (lastPosition > 0 && lastPosition < rowsAdapter.size()) {
            setSelectedPosition(lastPosition, true)
        }
    }

    private fun saveLastLaunchedApp(packageName: String) {
        val prefs = requireContext().getSharedPreferences("launcher_prefs", 0)
        prefs.edit { putString("last_launched_app", packageName) }
        prefs.edit { putLong("last_launch_time", System.currentTimeMillis()) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        loadApps()
    }
}