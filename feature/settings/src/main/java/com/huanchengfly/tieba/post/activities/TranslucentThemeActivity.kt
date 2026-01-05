package com.huanchengfly.tieba.post.activities

import android.annotation.SuppressLint
import android.animation.LayoutTransition
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.sketch.request.LoadResult
import com.github.panpf.sketch.request.execute
import com.github.panpf.sketch.resize.Scale
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.gyf.immersionbar.ImmersionBar
import com.huanchengfly.tieba.core.common.theme.PersistedTranslucentThemeConfig
import com.huanchengfly.tieba.core.common.theme.ThemeChannel
import com.huanchengfly.tieba.core.common.wallpaper.WallpaperRepository
import com.huanchengfly.tieba.core.runtime.device.ScreenMetricsRegistry
import com.huanchengfly.tieba.core.common.theme.ThemeTokens
import com.huanchengfly.tieba.core.theme.runtime.bridge.ThemeColorResolver
import com.huanchengfly.tieba.core.ui.device.dpToPx
import com.huanchengfly.tieba.feature.settings.R
import com.huanchengfly.tieba.feature.settings.databinding.ActivityTranslucentThemeBinding
import com.huanchengfly.tieba.post.adapters.TranslucentThemeColorAdapter
import com.huanchengfly.tieba.post.adapters.WallpaperAdapter
import com.huanchengfly.tieba.post.di.entrypoints.WallpaperRepositoryEntryPoint
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.dividers.HorizontalSpacesDecoration
import com.huanchengfly.tieba.post.components.transformations.SketchBlurTransformation
import com.huanchengfly.tieba.post.interfaces.OnItemClickListener
import com.huanchengfly.tieba.post.utils.CacheUtil
import com.huanchengfly.tieba.post.utils.ImageCacheUtil
import com.huanchengfly.tieba.post.utils.PermissionUtils
import com.huanchengfly.tieba.post.utils.PickMediasRequest
import com.huanchengfly.tieba.post.utils.registerPickMediasLauncher
import com.huanchengfly.tieba.post.utils.requestPermission
import com.huanchengfly.tieba.post.utils.shouldUsePhotoPicker
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class TranslucentThemeActivity : BaseActivity(), View.OnClickListener, OnSeekBarChangeListener,
    ColorPickerDialogListener {
    private lateinit var binding: ActivityTranslucentThemeBinding

    private var mUri: Uri? = null
    private var alpha = 0
    private var blur = 0
    private var themeVariant = ThemeTokens.TRANSLUCENT_THEME_LIGHT
    private var currentBackgroundPath: String? = null
    private var mPalette: Palette? = null
    private var pendingImageChanged = false

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
    private val wallpaperRepository: WallpaperRepository by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            WallpaperRepositoryEntryPoint::class.java
        ).wallpaperRepository()
    }

    private val mTranslucentThemeColorAdapter: TranslucentThemeColorAdapter by lazy {
        TranslucentThemeColorAdapter(
            this
        )
    }

    private val requestedChannel: ThemeChannel? by lazy {
        intent?.getStringExtra(EXTRA_THEME_CHANNEL)?.let { channelName ->
            runCatching { ThemeChannel.valueOf(channelName) }.getOrNull()
        }
    }

    private fun currentChannel(): ThemeChannel =
        requestedChannel ?: if (themeController.themeState.value.isNightMode) ThemeChannel.NIGHT else ThemeChannel.DAY

    private fun currentChannelConfig() = if (currentChannel() == ThemeChannel.DAY) {
        themeRepository.currentSettings().light
    } else {
        themeRepository.currentSettings().dark
    }

    private fun ensureTranslucentConfig(
        existing: PersistedTranslucentThemeConfig?
    ): PersistedTranslucentThemeConfig = existing ?: PersistedTranslucentThemeConfig(
        backgroundPath = null,
        primaryColor = null,
        themeVariant = ThemeTokens.TRANSLUCENT_THEME_LIGHT,
        blur = 0,
        alpha = DEFAULT_TRANSLUCENT_ALPHA
    )

    private fun updateTranslucentConfig(
        transform: (PersistedTranslucentThemeConfig) -> PersistedTranslucentThemeConfig
    ) {
        val channel = currentChannel()
        launch {
            themeRepository.updateChannel(channel) { config ->
                config.copy(translucent = transform(ensureTranslucentConfig(config.translucent)))
            }
        }
    }

    private fun loadInitialConfig() {
        val config = currentChannelConfig().translucent
        alpha = config?.alpha ?: DEFAULT_TRANSLUCENT_ALPHA
        blur = config?.blur ?: 0
        themeVariant = config?.themeVariant ?: ThemeTokens.TRANSLUCENT_THEME_LIGHT
        currentBackgroundPath = config?.backgroundPath
        pendingImageChanged = false
        mUri = currentBackgroundPath?.let { path ->
            val file = File(path)
            if (file.exists()) Uri.fromFile(file) else null
        }
    }

    private fun launchUCrop(sourceUri: Uri) {
        binding.progress.visibility = View.VISIBLE
        launch {
            val result = LoadRequest(this@TranslucentThemeActivity, sourceUri.toString()).execute()
            if (result is LoadResult.Success) {
                binding.progress.visibility = View.GONE
                val file =
                    bitmapToFile(result.bitmap, File(cacheDir, "origin_background.jpg"))
                val sourceFileUri = Uri.fromFile(file)
                val destUri = Uri.fromFile(File(filesDir, "cropped_background.jpg"))
                val metrics = ScreenMetricsRegistry.current
                val height = metrics.exactScreenHeightPx.toFloat()
                val width = metrics.exactScreenWidthPx.toFloat()
                UCrop.of(sourceFileUri, destUri)
                    .withAspectRatio(width / height, 1f)
                    .withOptions(UCrop.Options().apply {
                        setShowCropFrame(true)
                        setShowCropGrid(true)
                        setToolbarColor(
                            ThemeColorResolver.primaryColor(this@TranslucentThemeActivity)
                        )
                        setToolbarWidgetColor(
                            ThemeColorResolver.onPrimaryColor(this@TranslucentThemeActivity)
                        )
                        setActiveControlsWidgetColor(
                            ThemeColorResolver.accentColor(this@TranslucentThemeActivity)
                        )
                        setLogoColor(
                            ThemeColorResolver.primaryColor(this@TranslucentThemeActivity)
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
            pendingImageChanged = true
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
                mPalette = Palette.from(drawableToBitmap(result.drawable)).generate()
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
        loadInitialConfig()
        invalidateFinishBtn()

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
        binding.colorTheme.layoutTransition = LayoutTransition().apply {
            enableTransitionType(LayoutTransition.CHANGING)
        }
        wallpaperAdapter.setOnItemClickListener { _, item, _ ->
            launchUCrop(Uri.parse(item))
        }
        binding.wallpapersRv.addItemDecoration(
            HorizontalSpacesDecoration(
                0,
                0,
                dpToPxInt(16),
                dpToPxInt(16),
                false
            )
        )
        binding.wallpapersRv.adapter = wallpaperAdapter
        binding.wallpapersRv.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mTranslucentThemeColorAdapter.onItemClickListener =
            OnItemClickListener { _: View?, themeColor: Int, _: Int, _: Int ->
                updateTranslucentConfig { it.copy(primaryColor = themeColor) }
                binding.mask.post { themeUiDelegate.invalidateDecorView(this) }
            }
        binding.selectColorRecyclerView.apply {
            addItemDecoration(HorizontalSpacesDecoration(0, 0, dpToPxInt(12), dpToPxInt(12), false))
            layoutManager = MyLinearLayoutManager(
                this@TranslucentThemeActivity,
                MyLinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = mTranslucentThemeColorAdapter
        }
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
            pendingImageChanged = true
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
            wallpaperRepository
                .fetchWallpapers()
                .onSuccess {
                    CacheUtil.putCache(this@TranslucentThemeActivity, "recommend_wallpapers", it)
                    wallpapers = it
                }
        }
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        updateTranslucentConfig { it.copy(primaryColor = color) }
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
            currentBackgroundPath?.let { oldFilePath ->
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
                val bitmap = drawableToBitmap(result.drawable)
                val file = compressImage(
                    bitmap,
                    File(filesDir, "background_${System.currentTimeMillis()}.jpg"),
                    maxSizeKb = 512,
                    initialQuality = 97
                )
                mPalette = Palette.from(bitmap).generate()
                themeUiDelegate.invalidateDecorView(this@TranslucentThemeActivity)
                callback.onSuccess(file)
            }
        }
    }

    private fun invalidateFinishBtn() {
        binding.buttonFinish.visibility = if (mUri != null && pendingImageChanged) {
            View.VISIBLE
        } else {
            View.GONE
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
        when (themeVariant) {
            ThemeTokens.TRANSLUCENT_THEME_DARK -> {
                binding.darkColor.setBackgroundTintList(
                    ColorStateList.valueOf(ThemeColorResolver.accentColor(this))
                )
                binding.darkColor.setTextColor(ThemeColorResolver.onAccentColor(this))
                binding.darkColor.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    ContextCompat.getDrawable(this, R.drawable.ic_round_check_circle),
                    null,
                    null,
                    null
                )
                binding.lightColor.setBackgroundTintList(
                    ColorStateList.valueOf(ThemeColorResolver.dividerColor(this))
                )
                binding.lightColor.setTextColor(ThemeColorResolver.textSecondaryColor(this))
                binding.lightColor.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    null,
                    null
                )
            }
            ThemeTokens.TRANSLUCENT_THEME_LIGHT -> {
                binding.darkColor.setBackgroundTintList(
                    ColorStateList.valueOf(ThemeColorResolver.dividerColor(this))
                )
                binding.darkColor.setTextColor(ThemeColorResolver.textSecondaryColor(this))
                binding.darkColor.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
                binding.lightColor.setBackgroundTintList(
                    ColorStateList.valueOf(ThemeColorResolver.accentColor(this))
                )
                binding.lightColor.setTextColor(ThemeColorResolver.onAccentColor(this))
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
                savePic(object : SavePicCallback<File> {
                    override fun onSuccess(t: File) {
                        val savedPath = t.absolutePath
                        currentBackgroundPath = savedPath
                        pendingImageChanged = false
                        updateTranslucentConfig {
                            it.copy(
                                backgroundPath = savedPath,
                                blur = blur,
                                alpha = alpha,
                                themeVariant = themeVariant
                            )
                        }
                        invalidateFinishBtn()
                        toastShort(R.string.toast_save_pic_success)
                        translucentBackgroundStore.drawable = null
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
                    .setColor(ThemeColorResolver.colorById(this, R.color.sem_state_active))
                    .create()
                primaryColorPicker.setColorPickerDialogListener(this)
                primaryColorPicker.show(
                    supportFragmentManager,
                    "ColorPicker_TranslucentThemePrimaryColor"
                )
            }
            R.id.dark_color -> {
                themeVariant = ThemeTokens.TRANSLUCENT_THEME_DARK
                updateTranslucentConfig { it.copy(themeVariant = themeVariant) }
                refreshTheme()
            }
            R.id.light_color -> {
                themeVariant = ThemeTokens.TRANSLUCENT_THEME_LIGHT
                updateTranslucentConfig { it.copy(themeVariant = themeVariant) }
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

    private fun dpToPxInt(value: Int): Int = dpToPx(value.toFloat()).toInt()

    private fun toastShort(@StringRes resId: Int) {
        android.widget.Toast.makeText(this, getString(resId), android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        )
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    private fun bitmapToFile(
        bitmap: Bitmap,
        output: File,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): File {
        val baos = java.io.ByteArrayOutputStream()
        bitmap.compress(format, 100, baos)
        try {
            val fos = FileOutputStream(output)
            try {
                fos.write(baos.toByteArray())
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return output
    }

    private fun compressImage(
        bitmap: Bitmap,
        output: File,
        maxSizeKb: Int = 100,
        initialQuality: Int = 100
    ): File {
        val baos = java.io.ByteArrayOutputStream()
        var quality = initialQuality
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        while (baos.toByteArray().size / 1024 > maxSizeKb && quality > 0) {
            baos.reset()
            quality -= 5
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        }
        try {
            val fos = FileOutputStream(output)
            try {
                fos.write(baos.toByteArray())
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return output
    }

    interface SavePicCallback<T> {
        fun onSuccess(t: T)
    }

    companion object {
        val TAG = TranslucentThemeActivity::class.java.simpleName
        const val REQUEST_CODE_CHOOSE = 2
        private const val DEFAULT_TRANSLUCENT_ALPHA = 255
        const val EXTRA_THEME_CHANNEL = "extra_theme_channel"
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
