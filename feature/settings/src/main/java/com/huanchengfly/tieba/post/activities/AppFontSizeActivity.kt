package com.huanchengfly.tieba.post.activities

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.compose.settings.ThemeScaffold
import com.huanchengfly.tieba.core.theme.compose.scenes.ThemeTopAppBar
import com.stoyanvuchev.systemuibarstweaker.SystemUIBarsTweaker
import com.huanchengfly.tieba.feature.settings.R
import com.huanchengfly.tieba.post.arch.BaseComposeActivity
import com.huanchengfly.tieba.post.ui.common.DefaultBackIcon
import com.huanchengfly.tieba.post.utils.compose.calcStatusBarColor
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class AppFontSizeActivity : BaseComposeActivity() {
    private var oldFontSize: Float = DEFAULT_FONT_SCALE
    private var finished: Boolean = false

    override fun onCreateContent(systemUIBarsTweaker: SystemUIBarsTweaker) {
        super.onCreateContent(systemUIBarsTweaker)
        oldFontSize = appPreferences.fontScale
    }

    @Composable
    override fun Content() {
        var progress by rememberSaveable {
            mutableStateOf(
                ((appPreferences.fontScale - FONT_SCALE_MIN) / FONT_SCALE_STEP)
                    .roundToInt()
                    .coerceIn(0, MAX_PROGRESS)
            )
        }

        val fontScale = remember(progress) { FONT_SCALE_MIN + progress * FONT_SCALE_STEP }

        LaunchedEffect(fontScale) {
            appPreferences.fontScale = fontScale
        }

        BackHandler { finish() }

        ThemeScaffold(
            topBar = {
                val topBarColor = ExtendedTheme.colors.topBar
                ThemeTopAppBar(
                    backgroundColor = topBarColor,
                    statusBarColor = topBarColor.calcStatusBarColor(),
                    centerTitle = true,
                    title = {
                        Text(
                            text = getString(R.string.title_custom_font_size),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = { DefaultBackIcon(onBack = { finish() }) }
                )
            },
            containerColor = ExtendedTheme.colors.background,
            contentColor = ExtendedTheme.colors.text,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                BubblePreview(
                    fontScale = fontScale,
                    startText = getString(R.string.bubble_want_change_font_size),
                    replyText = getString(R.string.bubble_change_font_size),
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ExtendedTheme.colors.card,
                    contentColor = ExtendedTheme.colors.text,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = getSizeLabel(progress),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExtendedTheme.colors.text
                        )

                        Slider(
                            value = progress.toFloat(),
                            onValueChange = { value ->
                                progress = value.roundToInt().coerceIn(0, MAX_PROGRESS)
                            },
                            valueRange = 0f..MAX_PROGRESS.toFloat(),
                            // total discrete positions = steps + 1 + 1 => keep与旧 SeekBar 11 档一致
                            steps = MAX_PROGRESS - 1,
                            colors = SliderDefaults.colors(
                                thumbColor = ExtendedTheme.colors.accent,
                                activeTrackColor = ExtendedTheme.colors.accent,
                                inactiveTrackColor = ExtendedTheme.colors.indicator,
                                activeTickColor = ExtendedTheme.colors.onAccent,
                                inactiveTickColor = ExtendedTheme.colors.divider
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun BubblePreview(
        fontScale: Float,
        startText: String,
        replyText: String,
    ) {
        val bubbles = remember(startText, replyText) {
            listOf(
                Bubble(text = startText, isMe = true),
                Bubble(text = replyText, isMe = false)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            bubbles.forEach { bubble ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (bubble.isMe) Arrangement.End else Arrangement.Start
                ) {
                    val bubbleShape = if (bubble.isMe) {
                        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
                    } else {
                        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
                    }
                    Surface(
                        color = if (bubble.isMe) ExtendedTheme.colors.accent else ExtendedTheme.colors.card,
                        contentColor = if (bubble.isMe) ExtendedTheme.colors.onAccent else ExtendedTheme.colors.text,
                        shape = bubbleShape,
                    ) {
                        Text(
                            text = bubble.text,
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                // 使预览气泡宽度适中，避免顶到另一侧
                                .widthIn(min = 0.dp, max = 260.dp),
                            fontSize = (15.sp * fontScale),
                            textAlign = TextAlign.Start,
                            lineHeight = 20.sp * fontScale
                        )
                    }
                }
            }
        }
    }

    override fun finish() {
        if (!finished && oldFontSize != appPreferences.fontScale) {
            finished = true
            android.widget.Toast.makeText(
                this,
                getString(R.string.toast_after_change_will_restart),
                android.widget.Toast.LENGTH_SHORT
            ).show()
            exitApplication()
            packageManager.getLaunchIntentForPackage(packageName)?.let {
                startActivity(it)
            }
        }
        super.finish()
    }

    private fun getSizeLabel(progress: Int): String {
        val sizeTexts = SIZE_TEXT_MAPPING.filterValues { progress in it }
        return if (sizeTexts.isNotEmpty()) {
            getString(sizeTexts.keys.first())
        } else {
            getString(R.string.text_size_default)
        }
    }

    override fun getLayoutId(): Int = -1

    private data class Bubble(val text: String, val isMe: Boolean)

    companion object {
        const val FONT_SCALE_MIN = 0.8f
        const val FONT_SCALE_MAX = 1.3f
        const val FONT_SCALE_STEP = 0.05f
        const val DEFAULT_FONT_SCALE = 1f
        const val MAX_PROGRESS = 10

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
