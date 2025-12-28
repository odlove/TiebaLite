package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.core.common.photoview.PicPageResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.huanchengfly.tieba.post.models.mappers.toPicPageResult

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
    ): Flow<PicPageResult> =
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
            .map { it.toPicPageResult() }
}
