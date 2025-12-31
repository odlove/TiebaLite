package com.huanchengfly.tieba.post.services.notification

import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableSharedFlow

fun ComponentActivity.registerNotificationRuntime(
    notificationCountFlow: MutableSharedFlow<Int>,
) {
    val controller = NotificationRuntimeController(this, notificationCountFlow, lifecycleScope)
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            controller.onStart()
        }

        override fun onStop(owner: LifecycleOwner) {
            controller.onStop()
        }
    })
}
