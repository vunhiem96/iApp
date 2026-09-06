package com.nhstudio.isettings.quicksettings.iapp.adapter

import android.content.ActivityNotFoundException
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nhstudio.iapp.appmanager.R
import com.nhstudio.iapp.appmanager.databinding.ItemAppBinding
import com.nhstudio.iapp.appmanager.databinding.ItemLetterBinding
import com.nhstudio.isettings.quicksettings.iapp.extension.LoadAppUtils
import com.nhstudio.isettings.quicksettings.iapp.extension.applyColorFilter
import com.nhstudio.isettings.quicksettings.iapp.extension.beGone
import com.nhstudio.isettings.quicksettings.iapp.extension.beVisible
import com.nhstudio.isettings.quicksettings.iapp.extension.canShowOpenAds
import com.nhstudio.isettings.quicksettings.iapp.extension.config
import com.nhstudio.isettings.quicksettings.iapp.extension.darkMode
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClick
import androidx.core.graphics.toColorInt

class AppListAdapter(
    private val packageManager: PackageManager,
    private val onItemClick: (String) -> Unit
) : ListAdapter<AppListAdapter.AppListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_LETTER = 0
        private const val VIEW_TYPE_APP = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LETTER -> LetterViewHolder.from(parent)
            VIEW_TYPE_APP -> AppViewHolder.from(parent, packageManager, onItemClick)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LetterViewHolder -> {
                val item = getItem(position) as AppListItem.LetterItem
                holder.bind(item.letter)
            }

            is AppViewHolder -> {
                val item = getItem(position) as AppListItem.AppItem
                holder.bind(item.appInfo, item)
            }
        }
    }

    class LetterViewHolder private constructor(private val binding: ItemLetterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(letter: Char) {
            binding.letterTextView.text = letter.toString()
        }

        companion object {
            fun from(parent: ViewGroup): LetterViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemLetterBinding.inflate(layoutInflater, parent, false)
                return LetterViewHolder(binding)
            }
        }
    }


    class AppViewHolder private constructor(
        private val binding: ItemAppBinding,
        private val packageManager: PackageManager,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(appInfo: ApplicationInfo, appItem: AppListItem.AppItem) {
            val packageName = appInfo.packageName
            binding.appIconImageView.tag = packageName
            val cachedIcon = LoadAppUtils.getCachedIcon(packageName)
            if (cachedIcon != null) {
                binding.appIconImageView.setImageDrawable(cachedIcon)
            } else {
                binding.appIconImageView.setImageResource(R.drawable.ic_app)
                LoadAppUtils.getIconApp(appInfo) { icon ->
                    if (binding.appIconImageView.tag == packageName) {
                        binding.appIconImageView.setImageDrawable(icon)
                    }
                }
            }
            binding.appNameTextView.text = appItem.label
            binding.isLight = !darkMode
            binding.apply {
                applyThemeColors()
                val backgroundRes = when {
                    appItem.isFirst && appItem.isLast ->
                        if (darkMode) R.drawable.bg_select_bot_white_one_dark else R.drawable.bg_select_bot_white_one
                    appItem.isFirst ->
                        if (darkMode) R.drawable.bg_select_top_dark else R.drawable.bg_select_top_white
                    appItem.isLast ->
                        if (darkMode) R.drawable.bg_select_bot_dark else R.drawable.bg_select_bot_white
                    else ->
                        if (darkMode) R.drawable.bg_select_center_dark else R.drawable.bg_select_center_white
                }
                itemView.setBackgroundResource(backgroundRes)
                if (appItem.isLast) {
                    viewBot.beGone()
                } else {
                    viewBot.beVisible()
                }
                root.setPreventDoubleClick {
                    canShowOpenAds = true
                    onItemClick(appInfo.packageName)
                }
                root.setOnLongClickListener {
                    try {
                        val intent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
                        if (intent != null) {
                            itemView.context.startActivity(intent)
                        } else {
                            Toast.makeText( itemView.context,
                                itemView.context.getString(R.string.app_not_found), Toast.LENGTH_SHORT).show()
                            LoadAppUtils.removePackage(appInfo.packageName)
                        }
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText( itemView.context,
                            itemView.context.getString(R.string.app_not_found), Toast.LENGTH_SHORT).show()
                        LoadAppUtils.removePackage(appInfo.packageName)
                    }
                    true
                }
            }

        }

        private fun ItemAppBinding.applyThemeColors() {
            if (darkMode) {
                appNameTextView.setTextColor(Color.WHITE)
                ivNext.applyColorFilter("#545456".toColorInt())
                viewBot.setBackgroundColor("#3D3D41".toColorInt())
            } else {
                appNameTextView.setTextColor(Color.BLACK)
                ivNext.colorFilter = null
                viewBot.setBackgroundColor("#E2E2E3".toColorInt())
            }
        }

        companion object {
            fun from(
                parent: ViewGroup,
                packageManager: PackageManager,
                onItemClick: (String) -> Unit
            ): AppViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAppBinding.inflate(layoutInflater, parent, false)
                return AppViewHolder(binding, packageManager, onItemClick)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AppListItem.LetterItem -> VIEW_TYPE_LETTER
            is AppListItem.AppItem -> VIEW_TYPE_APP
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AppListItem>() {
        override fun areItemsTheSame(oldItem: AppListItem, newItem: AppListItem): Boolean {
            return when {
                oldItem is AppListItem.LetterItem && newItem is AppListItem.LetterItem -> oldItem.letter == newItem.letter
                oldItem is AppListItem.AppItem && newItem is AppListItem.AppItem -> oldItem.appInfo.packageName == newItem.appInfo.packageName
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: AppListItem, newItem: AppListItem): Boolean {
            if (oldItem is AppListItem.AppItem && newItem is AppListItem.AppItem) {
                return oldItem.appInfo.packageName == newItem.appInfo.packageName &&
                        oldItem.label == newItem.label &&
                        oldItem.isFirst == newItem.isFirst &&
                        oldItem.isLast == newItem.isLast
            }
            return oldItem == newItem
        }
    }

    sealed class AppListItem {
        data class LetterItem(val letter: Char) : AppListItem()
        data class AppItem(
            val appInfo: ApplicationInfo,
            val label: String,
            var isSelected: Boolean = false,
            var isFirst: Boolean = false,
            var isLast: Boolean = false
        ) : AppListItem()
    }
    // ... (Các ViewHolder và DiffCallback)
}