package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.models.PicPageBean
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 图片数据仓库实现
 */
@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val api: ITiebaApi
) : PhotoRepository {
    override fun picPage(
        forumId: String,
        forumName: String,
        threadId: String,
        seeLz: Boolean,
        picId: String,
        picIndex: String,
        objType: String,
        prev: Boolean
    ): Flow<PicPageBean> =
        api.picPageFlow(
            forumId,
            forumName,
            threadId,
            seeLz,
            picId,
            picIndex,
            objType,
            prev
        )
}
