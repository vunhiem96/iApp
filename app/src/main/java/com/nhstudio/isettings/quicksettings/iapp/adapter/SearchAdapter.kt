package com.nhstudio.isettings.quicksettings.iapp.adapter

import android.content.pm.ApplicationInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nhstudio.iapp.appmanager.R
import com.nhstudio.iapp.appmanager.databinding.ItemSearchBinding
import com.nhstudio.isettings.quicksettings.iapp.extension.LoadAppUtils
import com.nhstudio.isettings.quicksettings.iapp.extension.canShowOpenAds
import com.nhstudio.isettings.quicksettings.iapp.extension.darkMode
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClick


class SearchAdapter(
    var listImage: List<ApplicationInfo>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ImageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listImage.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item = listImage[position]
        val viewBind = holder.binding
        holder.binding.isLight = !darkMode
        viewBind.apply {
            val packageName = item.packageName
            tvPin.text = LoadAppUtils.getAppName(item)
            appIconImageView.tag = packageName
            val cachedIcon = LoadAppUtils.getCachedIcon(packageName)
            if (cachedIcon != null) {
                appIconImageView.setImageDrawable(cachedIcon)
            } else {
                appIconImageView.setImageResource(R.drawable.ic_app)
                LoadAppUtils.getIconApp(item) { icon ->
                    if (appIconImageView.tag == packageName) {
                        appIconImageView.setImageDrawable(icon)
                    }
                }
            }
            root.setPreventDoubleClick {
                canShowOpenAds = true
                onItemClick(packageName)
            }
        }

    }

}

class ImageViewHolder(val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root)
