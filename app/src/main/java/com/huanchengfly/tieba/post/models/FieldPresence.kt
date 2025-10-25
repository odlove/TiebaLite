package com.huanchengfly.tieba.post.models

/**
 * 字段出现标记 - 记录 ThreadInfo 在网络数据中实际出现的字段
 *
 * 用于区分"API 没返回这个字段"和"API 返回了这个字段的默认值"
 * 例如：replyNum = 0 可能是"真的没有回复"，也可能是"API 没返回这个字段"
 * 通过追踪网络协议中的 tag 编号，我们能精确判断
 *
 * **设计理念**：
 * - 直接保存所有出现过的 proto tag 编号的集合
 * - 自动支持 proto 的任何字段（包括新增字段）
 * - 无需手工维护字段列表
 *
 * @param presentTags - 在网络数据中实际出现过的 proto tag 编号集合
 *                      例如：tag 22 表示 media 字段出现，tag 21 表示 abstract 字段出现
 *                      使用 ProtoFieldTags 中的常量来访问
 */
data class FieldPresence(
    val presentTags: Set<Int> = emptySet()
)

/**
 * ThreadInfo 包装类 - 包含完整的 proto 和字段出现信息
 *
 * @param proto 完整的 ThreadInfo protobuf 对象
 * @param presence 字段出现标记
 */
data class ThreadInfoWithPresence(
    val proto: com.huanchengfly.tieba.post.api.models.protos.ThreadInfo,
    val presence: FieldPresence,
)
