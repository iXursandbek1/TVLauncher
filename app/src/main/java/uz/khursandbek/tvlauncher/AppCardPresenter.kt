package uz.khursandbek.tvlauncher

import android.content.pm.ResolveInfo
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter

class AppCardPresenter : Presenter() {
    companion object {
        private const val CARD_WIDTH = 313
        private const val CARD_HEIGHT = 176
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val context = parent.context
        val cardView = ImageCardView(context).apply {
            setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
            cardType = ImageCardView.CARD_TYPE_INFO_UNDER_WITH_EXTRA
            setBackgroundColor(ContextCompat.getColor(context, R.color.card_info_background))
            isFocusable = true
            isFocusableInTouchMode = true

            setOnFocusChangeListener { _, hasFocus ->
                animate()
                    .scaleX(if (hasFocus) 1.1f else 1f)
                    .scaleY(if (hasFocus) 1.1f else 1f)
                    .setDuration(150)
                    .start()
            }
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val app = item as ResolveInfo
        val context = viewHolder.view.context
        val cardView = viewHolder.view as ImageCardView
        val pm = context.packageManager

        val appName = app.loadLabel(pm).toString()
        val appIcon = try {
            app.loadIcon(pm)
        } catch (e: Exception) {
            ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)
        }

        val packageName = app.activityInfo.packageName
        val isSystemApp = (app.activityInfo.applicationInfo.flags and
                android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0

        cardView.titleText = appName
        cardView.contentText = packageName
        cardView.mainImage = appIcon

        if (isSystemApp) {
            cardView.badgeImage = ContextCompat.getDrawable(
                context,
                android.R.drawable.ic_partial_secure
            )
        } else {
            cardView.badgeImage = null
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.badgeImage = null
        cardView.mainImage = null
    }
}