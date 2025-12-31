package com.huanchengfly.tieba.core.ui.navigation

import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.utils.currentDestinationFlow
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class MainNavControllerHolder(
    private val scope: CoroutineScope,
) {
    private var direction: Direction? = null
    private var waitingNavCollectorToNavigate = AtomicBoolean(false)

    var navController: NavHostController? = null
        set(value) {
            field = value
            if (value != null && waitingNavCollectorToNavigate.get() && direction != null) {
                scope.launch {
                    value.currentDestinationFlow
                        .take(1)
                        .collect {
                            val dir = direction
                            if (waitingNavCollectorToNavigate.get() && dir != null) {
                                value.navigate(dir)
                                waitingNavCollectorToNavigate.set(false)
                                direction = null
                            }
                        }
                }
            }
        }

    fun navigate(direction: Direction) {
        val controller = navController
        if (controller == null) {
            waitingNavCollectorToNavigate.set(true)
            this.direction = direction
        } else {
            controller.navigate(direction)
        }
    }
}
