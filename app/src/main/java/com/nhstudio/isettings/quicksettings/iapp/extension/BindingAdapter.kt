package com.nhstudio.isettings.quicksettings.iapp.extension

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.nhstudio.iapp.appmanager.R
import com.suke.widget.SwitchButton


@BindingAdapter("visible")
fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

@BindingAdapter("alpha")
fun View.alpha(alphas: Boolean) {
    alpha = if (alphas) 1f else 0.7f
}

@BindingAdapter("text")
fun TextView.text(string: String) {
    text = string
}

@BindingAdapter("visibleAndInVisible")
fun View.visibleAndInVisible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
}


@BindingAdapter("invisible")
fun View.invisible(isInvisible: Boolean) {
    visibility = if (isInvisible) View.INVISIBLE else View.GONE
}


@BindingAdapter("gone")
fun View.gone(isGone: Boolean) {
    visibility = if (isGone) View.GONE else View.VISIBLE
}


/**
 * Load ảnh với coil
 * @param => Truyền vào 1 trong các key Source bên dưới
 */


@BindingAdapter("drawableSynchronized")
fun ImageView.loadDrawableSynchronized(
    drawable: Drawable? = null,
) {
    setImageDrawable(drawable)
}

@BindingAdapter("disableSwitchButton")
fun SwitchButton.disableSwitchButton(disableSwitchButton: Boolean) {
    isEnabled = disableSwitchButton
}

@BindingAdapter("switchButton")
fun SwitchButton.switchButton(disableSwitchButton: Boolean) {
    isEnabled = true
    isChecked = disableSwitchButton
    isEnabled = false
}


@BindingAdapter("setBackgroundTintInt")
fun View.setBackgroundTintInt(color: Int) {
    backgroundTintList = ColorStateList.valueOf(color)
}

@BindingAdapter("setTextColorInt")
fun TextView.setTextColorInt(color: Int) {
    setTextColor(color)
}

@BindingAdapter("setTextColor")
fun TextView.setTextColor(light: Boolean) {
    if(!light) {
        setTextColor(Color.WHITE)
    }
}

@BindingAdapter("colorfilter")
fun ImageView.setColorFilter(light: Boolean) {
    if(!light) {
        applyColorFilter(Color.parseColor("#545456"))
    }
}


@BindingAdapter("setViewBackground")
fun View.setBackground(light: Boolean) = if(light){

} else{
    setBackgroundColor(Color.parseColor("#3D3D41"))
}


@BindingAdapter("setViewBackgroundRoot")
fun View.setBackgroundRoot(light: Boolean) = if(light){

} else{
    setBackgroundColor(Color.BLACK)
}

@BindingAdapter("setViewBackgroundZone")
fun View.setBackgroundZone(light: Boolean) = if(light){

} else{
    setBackgroundResource(R.drawable.background_dark_radius)
}


@BindingAdapter("setViewBackgroundZone2")
fun View.setBackgroundZone2(light: Boolean) = if(light){

} else{
    setBackgroundResource(R.drawable.button_selector_dark)
}

@BindingAdapter("setTintInt")
fun ImageView.setBackgroundTintInt(color: Int) {
    setColorFilter(color)
}



@BindingAdapter("setTextRes")
fun TextView.setTextRes(idRes: Int?) {
    if (idRes != null) {
        text = context.getString(idRes)
    }
}



