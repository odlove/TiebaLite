package com.huanchengfly.tieba.core.ui.account

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.huanchengfly.tieba.core.network.error.defaultErrorMessage
import com.huanchengfly.tieba.core.ui.R
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.requestNotificationPermissionIfNeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn

@Composable
fun HandleAccountRefreshAndNotificationPermission(
    @StringRes descriptionResId: Int = R.string.desc_permission_post_notifications,
    @StringRes deniedResId: Int = R.string.tip_no_permission,
) {
    val context = LocalContext.current
    val currentAccount = AccountUtil.LocalAccount.current
    LaunchedEffect(currentAccount) {
        if (currentAccount != null) {
            AccountUtil.fetchAccountFlow()
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    context.toastShort(e.defaultErrorMessage())
                    e.printStackTrace()
                }
                .collect()

            context.requestNotificationPermissionIfNeeded(
                descriptionResId = descriptionResId,
                deniedResId = deniedResId,
            )
        }
    }
}
