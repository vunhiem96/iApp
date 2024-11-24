package com.nhstudio.isettings.quicksettings.iapp.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.nhstudio.iapp.appmanager.databinding.DialogYesBinding
import com.nhstudio.isettings.quicksettings.iapp.extension.darkMode
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClick


class ConfirmationDialog(
    context: Context,
    val titleText: String,
    val onClick: () -> Unit
) : BaseDialog(context) {
    private val binding = DialogYesBinding.inflate(LayoutInflater.from(context))
    override val contentView: View = binding.root


    @SuppressLint("SetTextI18n")
    override fun onViewReady() {
        binding.isLight =!darkMode
        binding.titleDialog.text = titleText
        binding.tvNo.setPreventDoubleClick {
            dismiss()
        }
        binding.tvYes.setPreventDoubleClick {
            onClick()
            dismiss()
        }
    }


}