package com.nhstudio.isettings.quicksettings.iapp.shortcut

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.nhstudio.iapp.appmanager.R
import com.nhstudio.iapp.appmanager.databinding.ActivityWidgetConfigBinding
import com.nhstudio.isettings.quicksettings.iapp.adapter.AppBigAdapter
import com.nhstudio.isettings.quicksettings.iapp.adapter.AppListAdapter
import com.nhstudio.isettings.quicksettings.iapp.extension.LoadAppUtils
import com.nhstudio.isettings.quicksettings.iapp.extension.beGone
import com.nhstudio.isettings.quicksettings.iapp.extension.darkMode
import com.nhstudio.isettings.quicksettings.iapp.extension.defaultSortList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator


class WidgetConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWidgetConfigBinding

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var packageName: String? = null
    private var appName: String? = null
    private var selectedColor: Int = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWidgetConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy widgetId nếu từ launcher
        widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        setupListeners()
        getAllApp()
    }

    private fun getAllApp() {
//        binding.isLight = !darkMode
//        if (defaultSortList.isEmpty()) {
        LoadAppUtils.getAppsAll {
            binding.loadingView.beGone()
            defaultSortList.clear()
            defaultSortList.addAll(it.sortedBy { item ->
                if (checkSelect(item)) {
                    -1
                } else 1
            })
            initRvApp()

        }

    }

    private fun initRvApp() {
        Log.i("dasdasdasdasdassa", "vao")
        CoroutineScope(Dispatchers.IO).launch {
            val groupedApps = groupAppsAlphabetically(
                defaultSortList,
                packageManager = this@WidgetConfigActivity.packageManager
            )
            val appListItems = mutableListOf<AppBigAdapter.AppListItem>()
            for ((letter, apps) in groupedApps) {
                appListItems.add(AppBigAdapter.AppListItem.LetterItem(letter))
                apps.forEachIndexed { index, appInfo ->
                    val isFirst = index == 0
                    val isLast = index == apps.size - 1
                    appListItems.add(
                        AppBigAdapter.AppListItem.AppItem(
                            appInfo,
                            isFirst = isFirst,
                            isLast = isLast
                        )
                    )
                }
            }
            withContext(Dispatchers.Main) {
                binding.recyclerView.adapter = AppBigAdapter(packageManager) {
                    // Nếu từ app → chưa có widgetId
                    packageName = it.packageName
                    appName = getAppName(this@WidgetConfigActivity, packageName!!)

                    // Sử dụng binding. để truy cập view thay vì gọi trực tiếp ID
                    binding.editName.setText(appName)
                    binding.previewName.text = appName

                    loadIcon()

                }
                binding.recyclerView.layoutManager =
                    LinearLayoutManager(this@WidgetConfigActivity)
                (binding.recyclerView.adapter as AppBigAdapter).submitList(appListItems)

            }


        }
    }


    fun groupAppsAlphabetically(
        appList: List<ApplicationInfo>,
        packageManager: PackageManager
    ): Map<Char, List<ApplicationInfo>> {
        return appList.groupBy {
            it.loadLabel(packageManager).toString().firstOrNull()?.uppercaseChar() ?: '#'
        }
    }

    private val listLockSelect: MutableList<String> = mutableListOf()
    private fun checkSelect(packageInfo: ApplicationInfo): Boolean {
        return listLockSelect.find { it == packageInfo.packageName } != null
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

    private fun pickImage() {
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

//        binding.btnPickImage.setOnClickListener {
//            pickImage()
//        }

        // ✏️ Nhập tên → preview realtime
        binding.editName.addTextChangedListener {
            binding.previewName.text = it.toString()
            updatePreviewVisibility()
        }

        // 👻 Bật/tắt tên
        binding.switchShowName.setOnCheckedChangeListener { _, _ ->
            if (packageName != null) {
                updatePreviewVisibility()
            } else {
                Toast.makeText(this, getString(R.string.please_select_an_app), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // 🎨 Chọn màu
        val colorClick = View.OnClickListener { v ->
            selectedColor = when (v.id) {
                binding.colorRed.id -> Color.RED
                binding.colorBlue.id -> Color.BLUE
                binding.colorGreen.id -> Color.GREEN
                binding.colorBlack.id -> Color.BLACK
                else -> Color.WHITE
            }
            if (packageName != null) {
                binding.previewName.setTextColor(selectedColor)
            } else {
                Toast.makeText(this, getString(R.string.please_select_an_app), Toast.LENGTH_SHORT)
                    .show()
            }

        }

        binding.colorWhite.setOnClickListener(colorClick)
        binding.colorRed.setOnClickListener(colorClick)
        binding.colorBlue.setOnClickListener(colorClick)
        binding.colorGreen.setOnClickListener(colorClick)
        binding.colorBlack.setOnClickListener(colorClick)

        // 💾 Save
        binding.btnSave.setOnClickListener {
            if (packageName != null) {
                saveWidget()
            } else {
                Toast.makeText(this, getString(R.string.please_select_an_app), Toast.LENGTH_SHORT)
                    .show()
            }

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