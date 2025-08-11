package com.nhstudio.isettings.quicksettings.iapp.adapter

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nhstudio.iapp.appmanager.R
import com.nhstudio.iapp.appmanager.databinding.ItemAppBinding
import com.nhstudio.iapp.appmanager.databinding.ItemLetterBinding
import com.nhstudio.isettings.quicksettings.iapp.extension.applyColorFilter
import com.nhstudio.isettings.quicksettings.iapp.extension.beGone
import com.nhstudio.isettings.quicksettings.iapp.extension.canShowOpenAds
import com.nhstudio.isettings.quicksettings.iapp.extension.config
import com.nhstudio.isettings.quicksettings.iapp.extension.darkMode
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClick
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClickAlphaItemView
import com.nhstudio.isettings.quicksettings.iapp.extension.setTextColor
import androidx.core.graphics.toColorInt

class AppListAdapter(private val packageManager: PackageManager) :
    ListAdapter<AppListAdapter.AppListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_LETTER = 0
        private const val VIEW_TYPE_APP = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LETTER -> LetterViewHolder.from(parent)
            VIEW_TYPE_APP -> AppViewHolder.from(parent, packageManager)
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
        private val packageManager: PackageManager
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            if(darkMode){
                binding.apply {
                    appNameTextView.setTextColor(Color.WHITE)
                    ivNext.applyColorFilter("#545456".toColorInt())
                    viewBot.setBackgroundColor("#3D3D41".toColorInt())
                }
            }
        }

        fun bind(appInfo: ApplicationInfo, appItem: AppListItem.AppItem) {
            Glide.with(itemView.context).load(appInfo.loadIcon(packageManager))
                .into(binding.appIconImageView)
            binding.appNameTextView.text = appItem.appInfo.loadLabel(packageManager)
            binding.isLight = !darkMode
            binding.apply {
                if(darkMode){
                    itemView.setBackgroundResource(R.drawable.bg_select_center_dark)
                }
                if (appItem.isFirst) {
                    if(darkMode){
                        itemView.setBackgroundResource(R.drawable.bg_select_top_dark)
                    } else {
                        itemView.setBackgroundResource(R.drawable.bg_select_top_white)
                    }
                }
                if (appItem.isLast) {
                    viewBot.beGone()
                    if(darkMode){
                        itemView.setBackgroundResource(R.drawable.bg_select_bot_dark)
                    } else {
                        itemView.setBackgroundResource(R.drawable.bg_select_bot_white)
                    }

                    // ... (Tùy chỉnh giao diện cho phần tử cuối cùng)
                }
                if (appItem.isFirst && appItem.isLast) {
                    if(darkMode){
                        itemView.setBackgroundResource(R.drawable.bg_select_bot_white_one_dark)
                    } else {
                        itemView.setBackgroundResource(R.drawable.bg_select_bot_white_one)
                    }

                }
                root.setPreventDoubleClick {
                    canShowOpenAds = true
                    if(itemView.context.config.showToast){
                        itemView.context.config.showToast = false
                        Toast.makeText( itemView.context,
                            itemView.context.getString(R.string.app_not_found2), Toast.LENGTH_LONG).show()
                    }
                    openAppDetails(itemView.context,appInfo.packageName)
                }
                root.setOnLongClickListener {
                    try {
                        val intent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
                        if (intent != null) {
                            itemView.context.startActivity(intent)
                        } else {
                            // App not found, handle the error (e.g., show a toast message)
                            Toast.makeText( itemView.context,
                                itemView.context.getString(R.string.app_not_found), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText( itemView.context,
                            itemView.context.getString(R.string.app_not_found), Toast.LENGTH_SHORT).show()
                    }
                    true
                }
            }

            // Hiển thị trạng thái select
//            binding.root.isSelected = appItem.isSelected
        }
        fun openAppDetails(context: Context, packageName: String) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                context.startActivity(intent)
            } catch (_: Exception) {
              Toast.makeText(context,"An error occurred",Toast.LENGTH_SHORT).show()
            }
        }

        companion object {
            fun from(parent: ViewGroup, packageManager: PackageManager): AppViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAppBinding.inflate(layoutInflater, parent, false)
                return AppViewHolder(binding, packageManager)
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
            return oldItem == newItem
        }
    }

    sealed class AppListItem {
        data class LetterItem(val letter: Char) : AppListItem()
        data class AppItem(
            val appInfo: ApplicationInfo,
            var isSelected: Boolean = false,
            var isFirst: Boolean = false,
            var isLast: Boolean = false
        ) : AppListItem()
    }
    // ... (Các ViewHolder và DiffCallback)
}