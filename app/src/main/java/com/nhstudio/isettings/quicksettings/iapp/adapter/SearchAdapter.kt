package com.nhstudio.isettings.quicksettings.iapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.nhstudio.iapp.appmanager.R
import com.nhstudio.iapp.appmanager.databinding.ItemSearchBinding
import com.nhstudio.isettings.quicksettings.iapp.data.SettingModel
import com.nhstudio.isettings.quicksettings.iapp.extension.darkMode
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClick


class SearchAdapter(
    var context: Context,
    var listImage: List<SettingModel>
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
            tvPin.text = item.nameSetting
            root.setPreventDoubleClick {
                try {
                    context.startActivity(Intent(item.urlSetting))
                } catch (e: Exception) {
                    Toast.makeText(context,
                        context.getString(R.string.your_device_does_not_support_this_feature), Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    }

}

class ImageViewHolder(val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root)









