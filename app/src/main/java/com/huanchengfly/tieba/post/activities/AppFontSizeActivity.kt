package com.huanchengfly.tieba.post.activities

import android.os.Bundle
import android.util.TypedValue
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.huanchengfly.tieba.post.*
import com.huanchengfly.tieba.post.adapters.ChatBubbleStyleAdapter
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.databinding.ActivityAppFontSizeBinding
import com.huanchengfly.tieba.post.utils.ThemeUtil


class AppFontSizeActivity : BaseActivity() {
    private lateinit var binding: ActivityAppFontSizeBinding

    var oldFontSize: Float = 0f
    var finished: Boolean = false

    private val bubblesAdapter: ChatBubbleStyleAdapter by lazy {
        ChatBubbleStyleAdapter(
            this,
            listOf(
                ChatBubbleStyleAdapter.Bubble(
                    getString(R.string.bubble_want_change_font_size),
                    ChatBubbleStyleAdapter.Bubble.POSITION_RIGHT
                ),
                ChatBubbleStyleAdapter.Bubble(getString(R.string.bubble_change_font_size))
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppFontSizeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ThemeUtil.setTranslucentThemeBackground(this, binding.background)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = this@AppFontSizeActivity.title
        }
        findViewById<com.google.android.material.appbar.CollapsingToolbarLayout>(R.id.collapsing_toolbar).title = title
        oldFontSize = appPreferences.fontScale
        binding.appFontSizeBubbles.apply {
            layoutManager =
                MyLinearLayoutManager(this@AppFontSizeActivity, LinearLayoutManager.VERTICAL, false)
            adapter = bubblesAdapter
        }
        val progress =
            ((appPreferences.fontScale * 1000L - FONT_SCALE_MIN * 1000L).toInt()) / ((FONT_SCALE_STEP * 1000L).toInt())
        binding.appFontSizeSeekbar.progress = progress
        updateSizeText(progress)
        binding.appFontSizeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val fontScale = FONT_SCALE_MIN + progress * FONT_SCALE_STEP
                appPreferences.fontScale = fontScale
                updatePreview(fontScale)
                updateSizeText(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun finish() {
        if (!finished && oldFontSize != appPreferences.fontScale) {
            finished = true
            toastShort(R.string.toast_after_change_will_restart)
            App.INSTANCE.removeAllActivity()
            packageManager.getLaunchIntentForPackage(packageName)?.let {
                startActivity(it)
            }
        }
        super.finish()
    }

    fun updateSizeText(progress: Int) {
        val sizeTexts = SIZE_TEXT_MAPPING.filterValues {
            progress in it
        }
        if (sizeTexts.isNotEmpty()) {
            binding.appFontSizeText.setText(sizeTexts.map { it.key }[0])
        }
    }

    fun updatePreview(fontScale: Float = appPreferences.fontScale) {
        bubblesAdapter.bubblesFontSize = 15f.dpToPxFloat() * fontScale
        binding.appFontSizeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16f.dpToPxFloat() * fontScale)
    }

    override fun getLayoutId(): Int {
        return -1  // Using View Binding instead
    }

    companion object {
        const val FONT_SCALE_MIN = 0.8f
        const val FONT_SCALE_MAX = 1.3f
        const val FONT_SCALE_STEP = 0.05f
        const val DEFAULT_FONT_SCALE = 1f

        val SIZE_TEXT_MAPPING = mapOf(
            R.string.text_size_small to 0..1,
            R.string.text_size_little_small to 2..3,
            R.string.text_size_default to 4..4,
            R.string.text_size_little_large to 5..6,
            R.string.text_size_large to 7..8,
            R.string.text_size_very_large to 9..10
        )
    }
}