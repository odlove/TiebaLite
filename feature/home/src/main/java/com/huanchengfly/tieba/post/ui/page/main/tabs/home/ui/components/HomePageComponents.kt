package com.huanchengfly.tieba.post.ui.page.main.tabs.home.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.eygraber.compose.placeholder.material.placeholder
import com.huanchengfly.tieba.core.ui.R as CoreUiR
import com.huanchengfly.tieba.core.ui.compose.MyLazyVerticalGrid
import com.huanchengfly.tieba.core.ui.navigation.LocalHomeNavigation
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.CardSurface
import com.huanchengfly.tieba.core.ui.theme.runtime.compose.ExtendedTheme
import com.huanchengfly.tieba.core.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.core.ui.widgets.compose.Button
import com.huanchengfly.tieba.core.ui.widgets.compose.Chip
import com.huanchengfly.tieba.core.ui.widgets.compose.LongClickMenu
import com.huanchengfly.tieba.core.ui.widgets.compose.MenuState
import com.huanchengfly.tieba.core.ui.widgets.compose.TipScreen
import com.huanchengfly.tieba.core.ui.widgets.compose.TextButton
import com.huanchengfly.tieba.core.ui.widgets.compose.rememberMenuState
import com.huanchengfly.tieba.post.preferences.appPreferences
import com.huanchengfly.tieba.post.ui.page.main.tabs.home.contract.HomeUiState

internal fun getGridCells(
    context: Context,
    listSingle: Boolean = context.appPreferences.listSingle
): GridCells {
    return if (listSingle) {
        GridCells.Fixed(1)
    } else {
        GridCells.Adaptive(180.dp)
    }
}

@Composable
internal fun Header(
    text: String,
    modifier: Modifier = Modifier,
    invert: Boolean = false
) {
    Chip(
        text = text,
        modifier = Modifier
            .padding(start = 16.dp)
            .then(modifier),
        invertColor = invert
    )
}

@Composable
internal fun ForumItemPlaceholder(
    showAvatar: Boolean,
) {
    CardSurface(
        modifier = Modifier.fillMaxWidth(),
        plain = true
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (showAvatar) {
                Image(
                    painter = rememberVectorPainter(image = Icons.Rounded.AccountCircle),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(40.dp)
                        .align(CenterVertically)
                        .placeholder(visible = true, color = ExtendedTheme.colors.chip),
                )
                Spacer(modifier = Modifier.width(14.dp))
            }
            Text(
                color = ExtendedTheme.colors.text,
                text = "",
                modifier = Modifier
                    .weight(1f)
                    .align(CenterVertically)
                    .placeholder(visible = true, color = ExtendedTheme.colors.chip),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(54.dp)
                    .background(
                        color = ExtendedTheme.colors.chip,
                        shape = RoundedCornerShape(3.dp)
                    )
                    .padding(vertical = 4.dp)
                    .align(CenterVertically)
                    .placeholder(visible = true, color = ExtendedTheme.colors.chip)
            ) {
                Text(
                    text = "0",
                    color = ExtendedTheme.colors.onChip,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Center)
                )
            }
        }
    }
}

@Composable
internal fun ForumItemMenuContent(
    menuState: MenuState,
    isTopForum: Boolean,
    onDeleteTopForum: () -> Unit,
    onAddTopForum: () -> Unit,
    onCopyName: () -> Unit,
    onUnfollow: () -> Unit,
) {
    DropdownMenuItem(
        onClick = {
            if (isTopForum) {
                onDeleteTopForum()
            } else {
                onAddTopForum()
            }
            menuState.expanded = false
        }
    ) {
        if (isTopForum) {
            Text(text = stringResource(id = CoreUiR.string.menu_top_del))
        } else {
            Text(text = stringResource(id = CoreUiR.string.menu_top))
        }
    }
    DropdownMenuItem(
        onClick = {
            onCopyName()
            menuState.expanded = false
        }
    ) {
        Text(text = stringResource(id = CoreUiR.string.title_copy_forum_name))
    }
    DropdownMenuItem(
        onClick = {
            onUnfollow()
            menuState.expanded = false
        }
    ) {
        Text(text = stringResource(id = CoreUiR.string.button_unfollow))
    }
}

@Composable
internal fun ForumItemContent(
    item: HomeUiState.Forum,
    showAvatar: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        AnimatedVisibility(visible = showAvatar) {
            Row {
                Avatar(data = item.avatar, size = 40.dp, contentDescription = null)
                Spacer(modifier = Modifier.width(14.dp))
            }
        }
        Text(
            color = ExtendedTheme.colors.text,
            text = item.forumName,
            modifier = Modifier
                .weight(1f)
                .align(CenterVertically),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .width(54.dp)
                .background(
                    color = ExtendedTheme.colors.chip,
                    shape = RoundedCornerShape(3.dp)
                )
                .padding(vertical = 4.dp)
                .align(CenterVertically)
        ) {
            Row(
                modifier = Modifier.align(Center),
            ) {
                Text(
                    text = "Lv.${item.levelId}",
                    color = ExtendedTheme.colors.onChip,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(CenterVertically)
                )
                if (item.isSign) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = stringResource(id = CoreUiR.string.tip_signed),
                        modifier = Modifier
                            .size(12.dp)
                            .align(CenterVertically),
                        tint = ExtendedTheme.colors.onChip
                    )
                }
            }
        }
    }
}

@Composable
internal fun ForumItem(
    item: HomeUiState.Forum,
    showAvatar: Boolean,
    onClick: (HomeUiState.Forum) -> Unit,
    onUnfollow: (HomeUiState.Forum) -> Unit,
    onAddTopForum: (HomeUiState.Forum) -> Unit,
    onDeleteTopForum: (HomeUiState.Forum) -> Unit,
    isTopForum: Boolean = false,
) {
    val menuState = rememberMenuState()
    LongClickMenu(
        menuContent = {
            ForumItemMenuContent(
                menuState = menuState,
                isTopForum = isTopForum,
                onDeleteTopForum = { onDeleteTopForum(item) },
                onAddTopForum = { onAddTopForum(item) },
                onCopyName = {
                    /* TODO host copy forum name */
                },
                onUnfollow = { onUnfollow(item) }
            )
        },
        menuState = menuState,
        onClick = {
            onClick(item)
        },
        shape = RectangleShape
    ) {
        CardSurface(
            modifier = Modifier.fillMaxWidth(),
            plain = true
        ) {
            ForumItemContent(item = item, showAvatar = showAvatar)
        }
    }
}

@Composable
internal fun HomePageSkeletonScreen(
    listSingle: Boolean,
    gridCells: GridCells
) {
    MyLazyVerticalGrid(
        columns = gridCells,
        contentPadding = PaddingValues(bottom = 12.dp),
        modifier = Modifier
            .fillMaxSize(),
    ) {
        item(key = "TopForumHeaderPlaceholder", span = { GridItemSpan(maxLineSpan) }) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Header(
                    text = stringResource(id = CoreUiR.string.title_top_forum),
                    modifier = Modifier.placeholder(
                        visible = true,
                        color = ExtendedTheme.colors.chip
                    ),
                    invert = true
                )
            }
        }
        items(6, key = { "TopPlaceholder$it" }) {
            ForumItemPlaceholder(listSingle)
        }
        item(
            key = "Spacer",
            span = { GridItemSpan(maxLineSpan) }) {
            Spacer(
                modifier = Modifier.height(
                    16.dp
                )
            )
        }
        item(key = "ForumHeaderPlaceholder", span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Header(
                    text = stringResource(id = CoreUiR.string.forum_list_title),
                    modifier = Modifier.placeholder(
                        visible = true,
                        color = ExtendedTheme.colors.chip
                    ),
                    invert = true
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        items(12, key = { "Placeholder$it" }) {
            ForumItemPlaceholder(listSingle)
        }
    }
}

@Composable
internal fun EmptyScreen(
    loggedIn: Boolean,
    canOpenExplore: Boolean,
    onOpenExplore: () -> Unit
) {
    val navigator = LocalHomeNavigation.current
    TipScreen(
        title = {
            if (!loggedIn) {
                Text(text = stringResource(id = CoreUiR.string.title_empty_login))
            } else {
                Text(text = stringResource(id = CoreUiR.string.title_empty))
            }
        },
        image = {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(CoreUiR.raw.lottie_astronaut))
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
            )
        },
        message = {
            if (!loggedIn) {
                Text(
                    text = stringResource(id = CoreUiR.string.home_empty_login),
                    style = MaterialTheme.typography.body1,
                    color = ExtendedTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        },
        actions = {
            if (!loggedIn) {
                Button(
                    onClick = {
                        navigator.openLogin()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(text = stringResource(id = CoreUiR.string.button_login))
                }
            }
            if (canOpenExplore) {
                TextButton(
                    onClick = onOpenExplore,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(text = stringResource(id = CoreUiR.string.button_go_to_explore))
                }
            }
        },
    )
}
