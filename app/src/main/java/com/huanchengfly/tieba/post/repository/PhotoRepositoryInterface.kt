package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.PicPageBean
import kotlinx.coroutines.flow.Flow

/**
 * 图片数据仓库接口
 *
 * 负责处理图片浏览相关的数据获取
 */
interface PhotoRepository {
    /**
     * 获取图片页面数据
     *
     * @param forumId 论坛ID
     * @param forumName 论坛名称
     * @param threadId 帖子ID
     * @param seeLz 是否只看楼主
     * @param picId 图片ID
     * @param picIndex 图片索引
     * @param objType 对象类型
     * @param prev 是否加载前面的图片
     * @return 图片页面数据流
     */
    fun picPage(
        forumId: String,
        forumName: String,
        threadId: String,
        seeLz: Boolean,
        picId: String,
        picIndex: String,
        objType: String,
        prev: Boolean
    ): Flow<PicPageBean>
}
