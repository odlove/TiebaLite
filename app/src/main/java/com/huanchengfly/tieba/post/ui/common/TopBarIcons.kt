package com.huanchengfly.tieba.post.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.material.LocalContentColor
import com.huanchengfly.tieba.core.ui.compose.ActionItem
import com.huanchengfly.tieba.core.ui.compose.BackNavigationIcon
import com.huanchengfly.tieba.post.R

@Composable
fun DefaultBackIcon(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    imageVector: ImageVector = Icons.AutoMirrored.Rounded.ArrowBack,
    contentDescription: String = stringResource(id = R.string.button_back),
    tint: Color = LocalContentColor.current,
) {
    BackNavigationIcon(
        onBackPressed = onBack,
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier,
    )
}

@Composable
fun DefaultActionIcon(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    ActionItem(
        icon = imageVector,
        contentDescription = contentDescription,
        onClick = onClick,
        tint = tint,
        modifier = modifier
    )
}
