package com.nhstudio.isettings.quicksettings.iapp.extension

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.doOnEnd
import com.nhstudio.iapp.appmanager.R


class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var progress: Float = 0f
    private var duration: Long = 12000
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var colorProgress: Int = Color.RED
    private var colorBackground: Int = Color.GRAY
    private var anim: ValueAnimator? = null

    init {
        initStyledAttributes(attrs)
    }

    // khoảng từ 0->1
    fun setProgress(value: Float) {
        progress = value
        postInvalidate()
    }

    private fun initStyledAttributes(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LoadingView)
        colorProgress = a.getInt(R.styleable.LoadingView_lv_progress_color, Color.GRAY)
        colorBackground =
            a.getColor(R.styleable.LoadingView_lv_progress_background_color, Color.WHITE)
        a.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        canvas?.apply {
            val viewHeight = height.toFloat()
            val viewWidth = width.toFloat()
            paint.color = colorBackground
            drawRoundRect(0f, 0f, viewWidth, viewHeight, viewHeight, viewHeight, paint)
            paint.color = colorProgress
            drawRoundRect(0f, 0f, viewWidth * progress, viewHeight, viewHeight, viewHeight, paint)
        }
    }

    fun endAnim() {
        anim?.end()
    }

    fun startAnim(duration: Long, onSuccess: () -> Unit = {}) {
        anim?.cancel()
        anim = ValueAnimator.ofFloat(1f)
        anim?.addUpdateListener {
            progress = it.animatedValue as Float
            postInvalidate()
        }
        anim?.doOnEnd {
            onSuccess.invoke()
        }
        anim?.duration = duration
        anim?.start()
    }
}