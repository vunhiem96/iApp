package com.nhstudio.isettings.quicksettings.iapp.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import com.nhstudio.iapp.appmanager.databinding.DialogSortAppBinding
import com.nhstudio.isettings.quicksettings.iapp.extension.AppSort
import com.nhstudio.isettings.quicksettings.iapp.extension.config
import com.nhstudio.isettings.quicksettings.iapp.extension.darkMode
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClick

class SortAppsDialog(
    context: Context,
    private val onSelected: () -> Unit
) : BaseDialog(context) {
    private val binding = DialogSortAppBinding.inflate(LayoutInflater.from(context))
    override val contentView: View = binding.root

    override fun onViewReady() {
        binding.isLight = !darkMode
        val options = listOf(
            binding.rowSortAz to AppSort.NAME_AZ,
            binding.rowSortZa to AppSort.NAME_ZA,
            binding.rowSortInstall to AppSort.INSTALL_TIME,
            binding.rowSortInstallOld to AppSort.INSTALL_TIME_OLD,
            binding.rowSortSizeAsc to AppSort.SIZE_ASC,
            binding.rowSortSizeDesc to AppSort.SIZE_DESC
        )
        val radios = listOf(
            binding.radioSortAz,
            binding.radioSortZa,
            binding.radioSortInstall,
            binding.radioSortInstallOld,
            binding.radioSortSizeAsc,
            binding.radioSortSizeDesc
        )
        showSelection(radios, context.config.appSortMode)
        options.forEach { (row, mode) ->
            row.setPreventDoubleClick {
                context.config.appSortMode = mode
                showSelection(radios, mode)
                onSelected()
                dismiss()
            }
        }
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }

    private fun showSelection(radios: List<RadioButton>, mode: Int) {
        val selected = when (mode) {
            AppSort.NAME_ZA -> 1
            AppSort.INSTALL_TIME -> 2
            AppSort.INSTALL_TIME_OLD -> 3
            AppSort.SIZE_ASC -> 4
            AppSort.SIZE_DESC -> 5
            else -> 0
        }
        radios.forEachIndexed { index, radio ->
            radio.isChecked = index == selected
        }
    }
}
