package com.huanchengfly.tieba.post.ui.page.main.tabs.explore.hot.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eygraber.compose.placeholder.material.placeholder
import androidx.compose.ui.res.stringResource
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.core.theme.compose.ExtendedTheme

@Composable
internal fun ThreadListItemPlaceholder() {
    Row(modifier = Modifier.padding(all = 16.dp)) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "1",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = ExtendedTheme.colors.background,
                modifier = Modifier
                    .padding(top = 3.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .wrapContentSize()
                    .placeholder(visible = true, color = MaterialTheme.colors.surface)
                    .padding(vertical = 1.dp, horizontal = 4.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholder(visible = true, color = MaterialTheme.colors.surface)
                )
                Text(
                    text = stringResource(id = CoreUiR.string.hot_num, "666"),
                    style = MaterialTheme.typography.caption,
                    color = ExtendedTheme.colors.textSecondary,
                    modifier = Modifier.placeholder(visible = true, color = MaterialTheme.colors.surface)
                )
            }
        }
    }
}

@Composable
internal fun ThreadListTab(
    text: String,
    selected: Boolean,
    onSelected: () -> Unit
) {
    val textColor by animateColorAsState(targetValue = if (selected) ExtendedTheme.colors.onAccent else ExtendedTheme.colors.onChip)
    val backgroundColor by animateColorAsState(targetValue = if (selected) ExtendedTheme.colors.primary else ExtendedTheme.colors.chip)
    Text(
        text = text,
        textAlign = TextAlign.Center,
        color = textColor,
        maxLines = 1,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100))
            .background(backgroundColor)
            .clickable(onClick = onSelected)
            .padding(vertical = 4.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
internal fun ChipHeader(
    text: String,
    invert: Boolean = false,
    modifier: Modifier = Modifier
) {
    Text(
        color = if (invert) MaterialTheme.colors.onSecondary else ExtendedTheme.colors.onChip,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(100))
            .then(modifier)
            .background(color = if (invert) MaterialTheme.colors.secondary else ExtendedTheme.colors.chip)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}
