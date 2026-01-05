package com.huanchengfly.tieba.post.ui.page.settings.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.core.common.theme.ThemeChannel
import com.huanchengfly.tieba.core.common.theme.ThemeChannelConfig
import com.huanchengfly.tieba.core.theme.model.ThemeCatalog
import com.huanchengfly.tieba.core.theme.model.ThemePalette
import com.huanchengfly.tieba.core.theme.model.ThemeSpec
import com.huanchengfly.tieba.core.common.theme.ThemeTokens
import com.huanchengfly.tieba.core.theme.model.ThemeType
import com.huanchengfly.tieba.core.ui.compose.widgets.CardSurface
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.compose.base.PreviewTheme
import com.huanchengfly.tieba.core.theme.compose.rememberThemePalette
import com.huanchengfly.tieba.feature.settings.R

@Composable
internal fun ChannelFramedContainer(
    selected: ThemeChannel,
    onSelect: (ThemeChannel) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val tabBounds = remember { mutableStateMapOf<ThemeChannel, Rect>() }
    var tabRowOffset by remember { mutableStateOf(Offset.Zero) }
    var contentBounds by remember { mutableStateOf<Rect?>(null) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ChannelTabRow(
                selected = selected,
                onSelect = onSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        tabRowOffset = coords.positionInParent()
                    },
                onTabBoundsChange = { channel, rect ->
                    tabBounds[channel] = Rect(
                        left = rect.left + tabRowOffset.x,
                        top = rect.top + tabRowOffset.y,
                        right = rect.right + tabRowOffset.x,
                        bottom = rect.bottom + tabRowOffset.y
                    )
                }
            )
            CardSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords -> contentBounds = coords.boundsInParent() },
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                border = BorderStroke(1.dp, ExtendedTheme.colors.divider)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    content()
                }
            }
        }

        val selectedTabRect = tabBounds[selected]
        val bodyRect = contentBounds
        if (selectedTabRect != null && bodyRect != null) {
            val strokeColor = ExtendedTheme.colors.primary
            Canvas(modifier = Modifier.fillMaxSize()) {
                val paddingPx = 8.dp.toPx()
                val bodyLeft = bodyRect.left - paddingPx
                val bodyRight = bodyRect.right + paddingPx
                val bodyBottom = bodyRect.bottom + paddingPx
                val bodyTop = bodyRect.top - paddingPx

                val tabLeft = selectedTabRect.left - paddingPx
                val tabRight = selectedTabRect.right + paddingPx
                val tabTop = selectedTabRect.top - paddingPx
                val tabBottom = selectedTabRect.bottom + paddingPx
                val tabCapTop = tabTop - paddingPx * 1.5f

                val path = Path().apply {
                    moveTo(bodyLeft, bodyBottom)
                    lineTo(bodyRight, bodyBottom)
                    lineTo(bodyRight, bodyTop)
                    lineTo(tabRight, bodyTop)
                    lineTo(tabRight, tabCapTop)
                    lineTo(tabLeft, tabCapTop)
                    lineTo(tabLeft, bodyTop)
                    lineTo(bodyLeft, bodyTop)
                    close()
                }

                drawPath(
                    path = path,
                    color = strokeColor,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.cornerPathEffect(24.dp.toPx())
                    )
                )
            }
        }
    }
}

@Composable
private fun ChannelTabRow(
    selected: ThemeChannel,
    onSelect: (ThemeChannel) -> Unit,
    modifier: Modifier = Modifier,
    onTabBoundsChange: ((ThemeChannel, Rect) -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ChannelTab(
            channel = ThemeChannel.DAY,
            text = stringResource(id = R.string.theme_label_channel_day),
            selected = selected == ThemeChannel.DAY,
            onClick = { onSelect(ThemeChannel.DAY) },
            onBoundsChange = onTabBoundsChange
        )
        ChannelTab(
            channel = ThemeChannel.NIGHT,
            text = stringResource(id = R.string.theme_label_channel_night),
            selected = selected == ThemeChannel.NIGHT,
            onClick = { onSelect(ThemeChannel.NIGHT) },
            onBoundsChange = onTabBoundsChange
        )
    }
}

@Composable
private fun RowScope.ChannelTab(
    channel: ThemeChannel,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    onBoundsChange: ((ThemeChannel, Rect) -> Unit)?
) {
    val shape = RoundedCornerShape(999.dp)
    val background = if (selected) ExtendedTheme.colors.primary else ExtendedTheme.colors.chip
    val content = if (selected) ExtendedTheme.colors.onPrimary else ExtendedTheme.colors.onChip
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(shape)
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
            .onGloballyPositioned { coords ->
                onBoundsChange?.invoke(channel, coords.boundsInParent())
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = content, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ThemePresetSection(
    themeValues: Array<String>,
    editingChannel: ThemeChannel,
    editingConfig: ThemeChannelConfig,
    onThemeSelected: (String) -> Unit
) {
    val themeSpecs = themeValues.map { ThemeCatalog.get(it.removeSuffix("_dynamic")) }
    val (lightThemes, darkThemes) = themeSpecs.partition { !it.isNight }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ThemePresetGroup(
                title = stringResource(id = R.string.theme_section_light),
                themes = lightThemes,
                editingChannel = editingChannel,
                editingConfig = editingConfig,
                onThemeSelected = onThemeSelected,
                maxItemsPerRow = 6,
                swatchSize = 48.dp,
                maxWidth = 360.dp
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ThemePresetGroup(
                title = stringResource(id = R.string.theme_section_dark),
                themes = darkThemes,
                editingChannel = editingChannel,
                editingConfig = editingConfig,
                onThemeSelected = onThemeSelected,
                maxItemsPerRow = 6,
                swatchSize = 48.dp,
                maxWidth = 360.dp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemePresetGroup(
    title: String,
    themes: List<ThemeSpec>,
    editingChannel: ThemeChannel,
    editingConfig: ThemeChannelConfig,
    onThemeSelected: (String) -> Unit,
    maxItemsPerRow: Int = 4,
    swatchSize: Dp = 64.dp,
    maxWidth: Dp? = null
) {
    if (themes.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold
        )
        val flowModifier = if (maxWidth != null) Modifier.width(maxWidth) else Modifier.fillMaxWidth()
        FlowRow(
            modifier = flowModifier,
            maxItemsInEachRow = maxItemsPerRow,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            themes.forEach { spec ->
                val palette = rememberThemePalette(spec.key)
                val selected = editingConfig.rawTheme.removeSuffix("_dynamic") == spec.key
                ThemePresetSwatch(
                    themeSpec = spec,
                    palette = palette,
                    selected = selected,
                    contentDescription = spec.displayName,
                    swatchSize = swatchSize,
                    onClick = {
                        onThemeSelected(spec.key)
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemePresetSwatch(
    themeSpec: ThemeSpec,
    palette: ThemePalette,
    selected: Boolean,
    contentDescription: String,
    swatchSize: Dp = 64.dp,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val borderColor = if (selected) ExtendedTheme.colors.primary else ExtendedTheme.colors.divider
    val iconImage = if (themeSpec.isNight) Icons.Rounded.NightsStay else Icons.Rounded.ColorLens
    val iconTint = if (themeSpec.isNight) Color.White else Color(palette.primary)
    val strokeWidth = 2.dp

    Box(
        modifier = Modifier
            .size(swatchSize)
            .semantics(mergeDescendants = true) {
                this.contentDescription = contentDescription
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .drawBehind {
                    val strokePx = strokeWidth.toPx()
                    val radiusPx = 12.dp.toPx()
                    val outerWidth = size.width
                    val outerHeight = size.height
                    val outerPath = Path().apply {
                        addRoundRect(
                            RoundRect(0f, 0f, outerWidth, outerHeight, CornerRadius(radiusPx))
                        )
                    }
                    drawPath(path = outerPath, color = borderColor)

                    val insetWidth = (outerWidth - strokePx * 2).coerceAtLeast(0f)
                    val insetHeight = (outerHeight - strokePx * 2).coerceAtLeast(0f)
                    if (insetWidth > 0f && insetHeight > 0f) {
                        val innerPath = Path().apply {
                            addRoundRect(
                                RoundRect(
                                    left = strokePx,
                                    top = strokePx,
                                    right = strokePx + insetWidth,
                                    bottom = strokePx + insetHeight,
                                    cornerRadius = CornerRadius((radiusPx - strokePx).coerceAtLeast(0f))
                                )
                            )
                        }
                        drawPath(path = innerPath, color = Color(palette.windowBackground))
                    }
                }
                .clickable(onClick = onClick)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconImage,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(swatchSize * 0.5f)
            )
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(ExtendedTheme.colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = ExtendedTheme.colors.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ChannelFramedContainerPreview() {
    val sampleThemes = ThemeCatalog.themes.values.filter { it.type != ThemeType.TRANSLUCENT && !it.isNight }.take(6)
    PreviewTheme(themeKey = ThemeTokens.THEME_DEFAULT) {
        Column(modifier = Modifier.padding(16.dp)) {
            ChannelFramedContainer(selected = ThemeChannel.DAY, onSelect = {}) {
                FlowRow(
                    maxItemsInEachRow = 4,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    sampleThemes.forEach { spec ->
                        val palette = rememberThemePalette(spec.key)
                        ThemePresetSwatch(
                            themeSpec = spec,
                            palette = palette,
                            selected = false,
                            contentDescription = spec.displayName,
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}
