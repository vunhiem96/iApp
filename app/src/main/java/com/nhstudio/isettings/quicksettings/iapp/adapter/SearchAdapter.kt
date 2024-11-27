package com.nhstudio.isettings.quicksettings.iapp.adapter

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nhstudio.iapp.appmanager.R
import com.nhstudio.iapp.appmanager.databinding.ItemSearchBinding
import com.nhstudio.isettings.quicksettings.iapp.data.SettingModel
import com.nhstudio.isettings.quicksettings.iapp.extension.darkMode
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClick


class SearchAdapter(
    var context: Context,
    var listImage: List<ApplicationInfo>
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
            tvPin.text =  item.loadLabel(tvPin.context.packageManager)
            Glide.with(tvPin.context).load(item.loadIcon(tvPin.context.packageManager))
                .into(appIconImageView)
            root.setPreventDoubleClick {
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", item.packageName, null)
                    intent.data = uri
                    context.startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(tvPin.context,
                        tvPin.context.getString(R.string.app_not_found3), Toast.LENGTH_LONG).show()
                }
            }
        }

    }

}

class ImageViewHolder(val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root)









