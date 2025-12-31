package com.huanchengfly.tieba.post.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.huanchengfly.tieba.core.ui.image.ImageUrlResolver
import com.huanchengfly.tieba.core.ui.locals.LocalOriginThreadRenderer
import com.huanchengfly.tieba.core.ui.locals.ProvideCoreLocals
import com.huanchengfly.tieba.core.ui.photoview.LocalPhotoViewer
import com.huanchengfly.tieba.core.ui.utils.DevicePosture
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.ui.page.photoview.PhotoViewActivity
import com.huanchengfly.tieba.post.ui.widgets.compose.AppOriginThreadRenderer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TiebaLiteLocalProvider(
    notificationCountFlow: MutableSharedFlow<Int>,
    devicePostureFlow: StateFlow<DevicePosture>,
    imageUrlResolver: ImageUrlResolver,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    ProvideCoreLocals(
        notificationCountFlow = notificationCountFlow,
        devicePostureFlow = devicePostureFlow,
        imageUrlResolver = imageUrlResolver,
    ) {
        CompositionLocalProvider(
            LocalPhotoViewer provides { photoViewData ->
                context.goToActivity<PhotoViewActivity> {
                    putExtra(PhotoViewActivity.EXTRA_PHOTO_VIEW_DATA, photoViewData)
                }
            },
            LocalOriginThreadRenderer provides AppOriginThreadRenderer,
            content = content
        )
    }
}
