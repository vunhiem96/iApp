package com.nhstudio.isettings.quicksettings.iapp.shortcut

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.nhstudio.iapp.appmanager.databinding.ActivityWidgetConfigBinding


class WidgetConfigActivity : AppCompatActivity() {

    // Khởi tạo biến binding (lazy hoặc lateinit)
    private lateinit var binding: ActivityWidgetConfigBinding

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var packageName: String? = null
    private var appName: String? = null
    private var selectedColor: Int = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cấu trúc chuẩn để khởi tạo View Binding
        binding = ActivityWidgetConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy widgetId nếu từ launcher
        widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )


        // Nếu từ app → chưa có widgetId
        packageName = "com.android.chrome"
        appName =  getAppName(this,packageName!!)

        // Sử dụng binding. để truy cập view thay vì gọi trực tiếp ID
        binding.editName.setText(appName)
        binding.previewName.text = appName

        loadIcon()
        setupListeners()
    }

    fun getAppName(context: Context, packageName: String): String? {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            null
        }
    }

    private fun loadIcon() {
        try {
            val icon = packageManager.getApplicationIcon(packageName!!)
            binding.previewIcon.setImageDrawable(icon)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private  fun pickImage(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1001)
    }

    private var selectedImageUri: String? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val uri = data?.data
            selectedImageUri = uri.toString()

            binding.previewIcon.setImageURI(uri) // preview

//            uri?.let {
//                contentResolver.takePersistableUriPermission(
//                    it,
//                    Intent.FLAG_GRANT_READ_URI_PERMISSION
//                )
//            }
        }
    }

    private fun setupListeners() {

        binding.btnPickImage.setOnClickListener {
            pickImage()
        }

        // ✏️ Nhập tên → preview realtime
        binding.editName.addTextChangedListener {
            binding.previewName.text = it.toString()
            updatePreviewVisibility()
        }

        // 👻 Bật/tắt tên
        binding.switchShowName.setOnCheckedChangeListener { _, _ ->
            updatePreviewVisibility()
        }

        // 🎨 Chọn màu
        val colorClick = View.OnClickListener { v ->
            selectedColor = when (v.id) {
                binding.colorRed.id -> Color.RED
                binding.colorBlue.id -> Color.BLUE
                binding.colorGreen.id -> Color.GREEN
                else -> Color.WHITE
            }
            binding.previewName.setTextColor(selectedColor)
        }

        binding.colorWhite.setOnClickListener(colorClick)
        binding.colorRed.setOnClickListener(colorClick)
        binding.colorBlue.setOnClickListener(colorClick)
        binding.colorGreen.setOnClickListener(colorClick)

        // 💾 Save
        binding.btnSave.setOnClickListener {
            saveWidget()
        }
    }

    private fun updatePreviewVisibility() {
        val text = binding.editName.text.toString()
        val show = binding.switchShowName.isChecked

        binding.previewName.visibility = if (!show || text.isEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun saveWidget() {
        val manager = AppWidgetManager.getInstance(this)

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val name = binding.editName.text.toString()
        val show = binding.switchShowName.isChecked

        WidgetHelper.saveConfig(
            this,
            widgetId,
            packageName!!,
            name,
            show,
            selectedColor, selectedImageUri
        )

        // Cập nhật widget ngay lập tức
        MyWidgetProvider().onUpdate(this, manager, intArrayOf(widgetId))

        val result = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }

        setResult(RESULT_OK, result)
        finish()
    }
}