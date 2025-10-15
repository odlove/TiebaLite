package com.huanchengfly.tieba.post.arch

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * 协程调度器提供者接口
 *
 * 用于提供不同的协程调度器，便于测试时注入测试调度器
 */
interface DispatcherProvider {
    /**
     * 主线程调度器，用于 UI 更新
     */
    val main: CoroutineDispatcher

    /**
     * IO 调度器，用于网络请求、文件操作等 IO 密集型任务
     */
    val io: CoroutineDispatcher
}

/**
 * 默认的调度器提供者实现
 *
 * 生产环境使用，返回标准的 Kotlin 协程调度器
 */
object DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
}
