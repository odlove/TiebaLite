package com.huanchengfly.tieba.post.ui.page.main.navigation.chrome

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import com.huanchengfly.tieba.core.ui.device.MainNavigationContentPosition

internal enum class LayoutType {
    HEADER, CONTENT
}

internal val ActiveIndicatorHeight = 56.dp
internal val ActiveIndicatorWidth = 240.dp

@Composable
internal fun PositionLayout(
    navigationContentPosition: MainNavigationContentPosition,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
        measurePolicy = { measurables, constraints ->
            lateinit var headerMeasurable: Measurable
            lateinit var contentMeasurable: Measurable
            measurables.forEach {
                when (it.layoutId) {
                    LayoutType.HEADER -> headerMeasurable = it
                    LayoutType.CONTENT -> contentMeasurable = it
                    else -> error("Unknown layoutId encountered!")
                }
            }

            val headerPlaceable = headerMeasurable.measure(constraints)
            val contentPlaceable = contentMeasurable.measure(
                constraints.offset(vertical = -headerPlaceable.height)
            )
            layout(constraints.maxWidth, constraints.maxHeight) {
                headerPlaceable.placeRelative(0, 0)

                val nonContentVerticalSpace = constraints.maxHeight - contentPlaceable.height

                val contentPlaceableY = when (navigationContentPosition) {
                    MainNavigationContentPosition.TOP -> 0
                    MainNavigationContentPosition.CENTER -> nonContentVerticalSpace / 2
                }.coerceAtLeast(headerPlaceable.height)

                contentPlaceable.placeRelative(0, contentPlaceableY)
            }
        }
    )
}

internal fun resolveAppName(context: android.content.Context): String {
    val applicationInfo = context.applicationInfo
    val labelRes = applicationInfo.labelRes
    return when {
        labelRes != 0 -> context.getString(labelRes)
        applicationInfo.nonLocalizedLabel != null -> applicationInfo.nonLocalizedLabel.toString()
        else -> context.packageManager.getApplicationLabel(applicationInfo)?.toString()
            ?: context.packageName
    }
}
