package com.nhstudio.isettings.quicksettings.iapp.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.View

abstract class BaseDialog(context: Context) : Dialog(context) {

    abstract val contentView: View

    abstract fun onViewReady()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(0)
    }

    final override fun setContentView(layoutResID: Int) {
        super.setContentView(contentView)

        setCancelable(false)
        setCanceledOnTouchOutside(false)
        onViewReady()
    }


    override fun show() {
        super.show()

        val width = getScreenWidth() * 0.85
        window?.setLayout(width.toInt(), -2)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}

fun getScreenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
}