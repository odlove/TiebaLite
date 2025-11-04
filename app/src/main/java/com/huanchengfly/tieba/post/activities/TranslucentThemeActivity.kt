package com.huanchengfly.tieba.post.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.ColorInt
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.sketch.request.LoadResult
import com.github.panpf.sketch.request.execute
import com.github.panpf.sketch.resize.Scale
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.gyf.immersionbar.ImmersionBar
import com.huanchengfly.tieba.core.ui.theme.ThemeTokens
import com.huanchengfly.tieba.post.*
import com.huanchengfly.tieba.post.App.Companion.translucentBackground
import com.huanchengfly.tieba.post.adapters.TranslucentThemeColorAdapter
import com.huanchengfly.tieba.post.adapters.WallpaperAdapter
import com.huanchengfly.tieba.post.api.LiteApi
import com.huanchengfly.tieba.core.network.retrofit.doIfSuccess
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.dividers.HorizontalSpacesDecoration
import com.huanchengfly.tieba.post.components.transformations.SketchBlurTransformation
import com.huanchengfly.tieba.post.databinding.ActivityTranslucentThemeBinding
import com.huanchengfly.tieba.post.interfaces.OnItemClickListener
import com.huanchengfly.tieba.core.ui.theme.runtime.ThemeColorResolver
import com.huanchengfly.tieba.post.ui.common.theme.ThemeUiDelegate
import com.huanchengfly.tieba.core.ui.widgets.theme.TintMaterialButton
import com.huanchengfly.tieba.post.utils.*
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File

class TranslucentThemeActivity : BaseActivity(), View.OnClickListener, OnSeekBarChangeListener,
    ColorPickerDialogListener {
    private lateinit var binding: ActivityTranslucentThemeBinding

    private var mUri: Uri? = null
    private var alpha = 0
    private var blur = 0
    private var mPalette: Palette? = null

    private val selectImageLauncher = registerPickMediasLauncher { (_, uris) ->
        if (uris.isNotEmpty()) {
            val sourceUri = uris[0]
            launchUCrop(sourceUri)
        }
    }

    var wallpapers: List<String>? = null
        set(value) {
            field = value
            refreshWallpapers()
        }
    private val wallpaperAdapter: WallpaperAdapter by lazy { WallpaperAdapter(this) }

    private val mTranslucentThemeColorAdapter: TranslucentThemeColorAdapter by lazy {
        TranslucentThemeColorAdapter(
            this
        )
    }

    private fun launchUCrop(sourceUri: Uri) {
        binding.progress.visibility = View.VISIBLE
        launch {
            val result = LoadRequest(this@TranslucentThemeActivity, sourceUri.toString()).execute()
            if (result is LoadResult.Success) {
                binding.progress.visibility = View.GONE
                val file =
                    ImageUtil.bitmapToFile(result.bitmap, File(cacheDir, "origin_background.jpg"))
                val sourceFileUri = Uri.fromFile(file)
                val destUri = Uri.fromFile(File(filesDir, "cropped_background.jpg"))
                val height = App.ScreenInfo.EXACT_SCREEN_HEIGHT.toFloat()
                val width = App.ScreenInfo.EXACT_SCREEN_WIDTH.toFloat()
                UCrop.of(sourceFileUri, destUri)
                    .withAspectRatio(width / height, 1f)
                    .withOptions(UCrop.Options().apply {
                        setShowCropFrame(true)
                        setShowCropGrid(true)
                        setToolbarColor(
                            ThemeColorResolver.colorByAttr(
                                this@TranslucentThemeActivity,
                                R.attr.colorPrimary
                            )
                        )
                        setToolbarWidgetColor(
                            ThemeColorResolver.colorByAttr(
                                this@TranslucentThemeActivity,
                                R.attr.colorTextOnPrimary
                            )
                        )
                        setActiveControlsWidgetColor(
                            ThemeColorResolver.colorByAttr(
                                this@TranslucentThemeActivity,
                                R.attr.colorAccent
                            )
                        )
                        setLogoColor(
                            ThemeColorResolver.colorByAttr(
                                this@TranslucentThemeActivity,
                                R.attr.colorPrimary
                            )
                        )
                        setCompressionFormat(Bitmap.CompressFormat.JPEG)
                    })
                    .start(this@TranslucentThemeActivity)
            } else if (result is LoadResult.Error) {
                binding.progress.visibility = View.GONE
                toastShort(R.string.text_load_failed)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            mUri = data?.let { UCrop.getOutput(it) }
            invalidateFinishBtn()
            refreshBackground()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            data?.let { intentData ->
                UCrop.getError(intentData)?.printStackTrace()
            }
        }
    }

    private fun refreshWallpapers() {
        if (wallpapers.isNullOrEmpty()) {
            binding.recommendWallpapers.visibility = View.GONE
        } else {
            binding.recommendWallpapers.visibility = View.VISIBLE
            wallpaperAdapter.setData(wallpapers)
        }
    }

    private fun refreshBackground() {
        binding.progress.visibility = View.VISIBLE
        if (mUri == null) {
            binding.background.setBackgroundColor(Color.BLACK)
            binding.progress.visibility = View.GONE
            return
        }
        launch {
            val result = DisplayRequest(this@TranslucentThemeActivity, mUri.toString()) {
                resizeScale(Scale.CENTER_CROP)
                if (blur > 0) {
                    transformations(SketchBlurTransformation(blur))
                }
            }.execute()
            if (result is DisplayResult.Success) {
                result.drawable.alpha = alpha
                binding.background.background = result.drawable
                mPalette = Palette.from(ImageUtil.drawableToBitmap(result.drawable)).generate()
                mTranslucentThemeColorAdapter.setPalette(mPalette)
                binding.selectColor.visibility = View.VISIBLE
                binding.progress.visibility = View.GONE
            }
        }
    }

    override fun refreshStatusBarColor() {
        ImmersionBar.with(this)
            .transparentBar()
            .init()
    }

    override fun getLayoutId(): Int {
        return -1  // Using View Binding instead
    }

    @SuppressLint("ApplySharedPref", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTranslucentThemeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.experimentalTip.setOnClickListener {
            showDialog {
                setTitle(R.string.title_translucent_theme_experimental_feature)
                setMessage(
                    HtmlCompat.fromHtml(
                        getString(R.string.tip_translucent_theme),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                )
                setNegativeButton(R.string.btn_close, null)
            }
        }
        listOf(
            binding.customColor,
            binding.selectPic,
            binding.darkColor,
            binding.lightColor,
            binding.buttonBack,
            binding.buttonFinish
        ).forEach {
            it.setOnClickListener(this@TranslucentThemeActivity)
        }
        wallpapers =
            CacheUtil.getCache(this, "recommend_wallpapers", List::class.java) as List<String>?
        binding.colorTheme.enableChangingLayoutTransition()
        wallpaperAdapter.setOnItemClickListener { _, item, _ ->
            launchUCrop(Uri.parse(item))
        }
        binding.wallpapersRv.addItemDecoration(
            HorizontalSpacesDecoration(
                0,
                0,
                16.dpToPx(),
                16.dpToPx(),
                false
            )
        )
        binding.wallpapersRv.adapter = wallpaperAdapter
        binding.wallpapersRv.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mTranslucentThemeColorAdapter.onItemClickListener =
            OnItemClickListener { _: View?, themeColor: Int, _: Int, _: Int ->
                appPreferences.translucentPrimaryColor = toString(themeColor)
                binding.mask.post { themeUiDelegate.invalidateDecorView(this) }
            }
        binding.selectColorRecyclerView.apply {
            addItemDecoration(HorizontalSpacesDecoration(0, 0, 12.dpToPx(), 12.dpToPx(), false))
            layoutManager = MyLinearLayoutManager(
                this@TranslucentThemeActivity,
                MyLinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = mTranslucentThemeColorAdapter
        }
        alpha = appPreferences.translucentBackgroundAlpha
        blur = appPreferences.translucentBackgroundBlur
        binding.alpha.apply {
            progress = this@TranslucentThemeActivity.alpha
            setOnSeekBarChangeListener(this@TranslucentThemeActivity)
        }
        binding.blur.apply {
            progress = this@TranslucentThemeActivity.blur
            setOnSeekBarChangeListener(this@TranslucentThemeActivity)
        }
        binding.progress.setOnTouchListener { _: View?, _: MotionEvent? -> true }
        binding.progress.visibility = View.GONE
        val file = File(filesDir, "cropped_background.jpg")
        if (file.exists()) {
            mUri = Uri.fromFile(file)
            invalidateFinishBtn()
        }
        val bottomSheetBehavior =
            (binding.bottomSheet.layoutParams as CoordinatorLayout.LayoutParams).behavior as BottomSheetBehavior
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.mask.alpha = slideOffset
                binding.mask.visibility = if (slideOffset < 0.01f) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

        })
        binding.mask.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        refreshBackground()
        refreshTheme()
        fetchWallpapers()
    }

    private fun fetchWallpapers() {
        launch(IO + job) {
            LiteApi.instance
                .wallpapersAsync()
                .doIfSuccess {
                    CacheUtil.putCache(this@TranslucentThemeActivity, "recommend_wallpapers", it)
                    wallpapers = it
                }
        }
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        appPreferences.translucentPrimaryColor = toString(color)
        themeUiDelegate.invalidateDecorView(this)
    }

    override fun onDialogDismissed(dialogId: Int) {}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.select_color -> return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun savePic(callback: SavePicCallback<File>) {
        runCatching {
            val oldFilePath = appPreferences.translucentThemeBackgroundPath
            if (oldFilePath != null) {
                val oldFile = File(oldFilePath)
                oldFile.delete()
            }
        }
        binding.progress.visibility = View.VISIBLE
        launch {
            val result = DisplayRequest(this@TranslucentThemeActivity, mUri.toString()) {
                resizeScale(Scale.CENTER_CROP)
                if (blur > 0) {
                    transformations(SketchBlurTransformation(blur))
                }
            }.execute()
            if (result is DisplayResult.Success) {
                result.drawable.alpha = alpha
                val bitmap = ImageUtil.drawableToBitmap(result.drawable)
                val file = ImageUtil.compressImage(
                    bitmap,
                    File(filesDir, "background_${System.currentTimeMillis()}.jpg"),
                    maxSizeKb = 512,
                    initialQuality = 97
                )
                mPalette = Palette.from(bitmap).generate()
                appPreferences.translucentThemeBackgroundPath = file.absolutePath
                themeUiDelegate.invalidateDecorView(this@TranslucentThemeActivity)
                callback.onSuccess(file)
            }
        }
    }

    private fun invalidateFinishBtn() {
        if (mUri != null) {
            binding.buttonFinish.visibility = View.VISIBLE
        } else {
            binding.buttonFinish.visibility = View.GONE
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {
        when (seekBar.id) {
            R.id.alpha -> alpha = seekBar.progress
            R.id.blur -> blur = seekBar.progress
        }
        refreshBackground()
    }

    private fun refreshTheme() {
        when (appPreferences.translucentBackgroundTheme) {
            ThemeTokens.TRANSLUCENT_THEME_DARK -> {
                binding.darkColor.setBackgroundTintResId(R.color.default_color_accent)
                binding.darkColor.setTextColorResId(R.color.white)
                binding.darkColor.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    ContextCompat.getDrawable(this, R.drawable.ic_round_check_circle),
                    null,
                    null,
                    null
                )
                binding.lightColor.setBackgroundTintResId(R.color.color_divider)
                binding.lightColor.setTextColorResId(R.color.color_text_secondary)
                binding.lightColor.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    null,
                    null
                )
            }
            ThemeTokens.TRANSLUCENT_THEME_LIGHT -> {
                binding.darkColor.setBackgroundTintResId(R.color.color_divider)
                binding.darkColor.setTextColorResId(R.color.color_text_secondary)
                binding.darkColor.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
                binding.lightColor.setBackgroundTintResId(R.color.default_color_accent)
                binding.lightColor.setTextColorResId(R.color.white)
                binding.lightColor.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    ContextCompat.getDrawable(this, R.drawable.ic_round_check_circle),
                    null,
                    null,
                    null
                )
            }
        }
    }

    override fun finish() {
        ImageCacheUtil.clearImageMemoryCache(this)
        super.finish()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_finish -> {
                appPreferences.apply {
                    translucentBackgroundAlpha = alpha
                    translucentBackgroundBlur = blur
                }
                savePic(object : SavePicCallback<File> {
                    override fun onSuccess(t: File) {
                        themeController.switchTheme(ThemeTokens.THEME_TRANSLUCENT, false)
                        toastShort(R.string.toast_save_pic_success)
                        translucentBackground = null
                        binding.progress.visibility = View.GONE
                        finish()
                    }
                })
            }
            R.id.button_back -> {
                finish()
            }
            R.id.select_pic -> askPermission {
                selectImageLauncher.launch(PickMediasRequest(mediaType = PickMediasRequest.ImageOnly))
            }
            R.id.custom_color -> {
                val primaryColorPicker = ColorPickerDialog.newBuilder()
                    .setDialogTitle(R.string.title_color_picker_primary)
                    .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                    .setShowAlphaSlider(true)
                    .setDialogId(0)
                    .setAllowPresets(false)
                    .setColor(ThemeColorResolver.colorById(this, R.color.default_color_primary))
                    .create()
                primaryColorPicker.setColorPickerDialogListener(this)
                primaryColorPicker.show(
                    supportFragmentManager,
                    "ColorPicker_TranslucentThemePrimaryColor"
                )
            }
            R.id.dark_color -> {
                appPreferences.translucentBackgroundTheme = ThemeTokens.TRANSLUCENT_THEME_DARK
                refreshTheme()
            }
            R.id.light_color -> {
                appPreferences.translucentBackgroundTheme = ThemeTokens.TRANSLUCENT_THEME_LIGHT
                refreshTheme()
            }
        }
    }

    private fun askPermission(granted: () -> Unit) {
        if (shouldUsePhotoPicker(appPreferences)) {
            granted()
            return
        }
        requestPermission {
            unchecked = true
            permissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                listOf(
                    PermissionUtils.READ_EXTERNAL_STORAGE,
                    PermissionUtils.WRITE_EXTERNAL_STORAGE
                )
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                listOf(
                    PermissionUtils.READ_EXTERNAL_STORAGE
                )
            } else {
                listOf(PermissionUtils.READ_MEDIA_IMAGES)
            }
            description = getString(R.string.tip_permission_storage)
            onGranted = granted
            onDenied = { toastShort(R.string.toast_no_permission_insert_photo) }
        }
    }

    interface SavePicCallback<T> {
        fun onSuccess(t: T)
    }

    companion object {
        val TAG = TranslucentThemeActivity::class.java.simpleName
        const val REQUEST_CODE_CHOOSE = 2
        fun toString(alpha: Int, red: Int, green: Int, blue: Int): String {
            val hr = Integer.toHexString(red)
            val hg = Integer.toHexString(green)
            val hb = Integer.toHexString(blue)
            val ha = Integer.toHexString(alpha)
            return "#" + fixHexString(ha) + fixHexString(hr) + fixHexString(hg) + fixHexString(hb)
        }

        private fun fixHexString(string: String): String {
            var hexStr = string
            if (hexStr.isEmpty()) {
                hexStr = "00"
            }
            if (hexStr.length == 1) {
                hexStr = "0$hexStr"
            }
            if (hexStr.length > 2) {
                hexStr = hexStr.substring(0, 2)
            }
            return hexStr
        }

        fun toString(@ColorInt color: Int): String {
            return toString(
                Color.alpha(color),
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
        }
    }
}
