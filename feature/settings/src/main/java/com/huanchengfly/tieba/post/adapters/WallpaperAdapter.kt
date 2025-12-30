package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.widget.ImageView
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.enqueue
import com.huanchengfly.tieba.feature.settings.R
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeAdapter
import com.huanchengfly.tieba.post.components.MyViewHolder

class WallpaperAdapter(context: Context) : BaseSingleTypeAdapter<String>(context) {
    override fun getItemLayoutId(): Int = R.layout.item_wallpaper

    override fun convert(viewHolder: MyViewHolder, item: String, position: Int) {
        val imageView = viewHolder.getView<ImageView>(R.id.image_view)
        DisplayRequest(imageView, item).enqueue()
    }
}
