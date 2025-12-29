package com.huanchengfly.tieba.post.ui.page.photoview

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.content.FileProvider
import com.github.panpf.sketch.request.DownloadRequest
import com.github.panpf.sketch.request.DownloadResult
import com.github.panpf.sketch.request.execute
import com.huanchengfly.tieba.feature.photoview.R
import com.huanchengfly.tieba.post.utils.PermissionUtils
import com.huanchengfly.tieba.post.utils.PermissionUtils.PermissionData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

object PhotoDownloadHelper {
    private const val FILE_FOLDER = "TiebaLite"

    fun download(context: Context, url: String?) {
        download(context, url, false, null)
    }

    fun downloadForShare(
        context: Context,
        url: String?,
        onGetUri: (Uri) -> Unit
    ) {
        download(context, url, true, onGetUri)
    }

    @SuppressLint("StaticFieldLeak")
    private fun download(
        context: Context,
        url: String?,
        forShare: Boolean,
        onGetUri: ((Uri) -> Unit)?
    ) {
        if (url.isNullOrEmpty()) return
        if (forShare) {
            onGetUri ?: return
            downloadForShareInternal(context, url, onGetUri)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadAboveQ(context, url)
            return
        }
        PermissionUtils.askPermission(
            context,
            PermissionData(
                listOf(
                    PermissionUtils.READ_EXTERNAL_STORAGE,
                    PermissionUtils.WRITE_EXTERNAL_STORAGE
                ),
                context.getString(R.string.tip_permission_storage)
            ),
            R.string.toast_no_permission_save_photo
        ) {
            downloadBelowQ(context, url)
        }
    }

    private fun downloadForShareInternal(
        context: Context,
        url: String,
        onGetUri: (Uri) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val downloadResult = DownloadRequest(context, url).execute()
            if (downloadResult is DownloadResult.Success) {
                val inputStream = downloadResult.data.data.newInputStream()
                val pictureFolder = File(context.cacheDir, ".shareTemp")
                if (pictureFolder.exists() || pictureFolder.mkdirs()) {
                    val fileName = "share_" + System.currentTimeMillis()
                    val destFile = File(pictureFolder, fileName)
                    if (!destFile.exists()) {
                        withContext(Dispatchers.IO) {
                            destFile.createNewFile()
                        }
                    }
                    inputStream.use { input ->
                        if (destFile.canWrite()) {
                            destFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    val shareUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileProvider.getUriForFile(
                            context,
                            context.packageName + ".share.FileProvider",
                            destFile
                        )
                    } else {
                        Uri.fromFile(destFile)
                    }
                    withContext(Dispatchers.Main) {
                        onGetUri(shareUri)
                    }
                }
            }
        }
    }

    private fun downloadAboveQ(context: Context, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val downloadResult = DownloadRequest(context, url).execute()
            if (downloadResult is DownloadResult.Success) {
                var mimeType = "image/jpeg"
                var fileName = URLUtil.guessFileName(url, null, mimeType)
                downloadResult.data.data.newInputStream().use { inputStream ->
                    if (isGifFile(inputStream)) {
                        mimeType = "image/gif"
                        fileName = changeFileExtension(fileName, ".gif")
                    }
                }
                downloadResult.data.data.newInputStream().use { inputStream ->
                    val relativePath =
                        Environment.DIRECTORY_PICTURES + File.separator + FILE_FOLDER
                    val values = ContentValues().apply {
                        put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, relativePath)
                        put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, fileName)
                        put(android.provider.MediaStore.Images.Media.MIME_TYPE, mimeType)
                        put(android.provider.MediaStore.Images.Media.DESCRIPTION, fileName)
                    }
                    val cr = context.contentResolver
                    val uri: Uri = runCatching {
                        cr.insert(
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            values
                        )
                    }.getOrNull() ?: return@launch
                    try {
                        cr.openFileDescriptor(uri, "w")?.use {
                            inputStream.copyTo(it.fileDescriptor)
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.toast_photo_saved, relativePath),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        cr.delete(uri, null, null)
                    }
                }
            }
        }
    }

    private fun downloadBelowQ(context: Context, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val downloadResult = DownloadRequest(context, url).execute()
            if (downloadResult is DownloadResult.Success) {
                var fileName = URLUtil.guessFileName(url, null, "image/jpeg")
                downloadResult.data.data.newInputStream().use { inputStream ->
                    if (isGifFile(inputStream)) {
                        fileName = changeFileExtension(fileName, ".gif")
                    }
                }
                downloadResult.data.data.newInputStream().use { inputStream ->
                    val pictureFolder =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val appDir = File(pictureFolder, FILE_FOLDER)
                    val dirExists =
                        withContext(Dispatchers.IO) { appDir.exists() || appDir.mkdirs() }
                    if (dirExists) {
                        val destFile = File(appDir, fileName)
                        if (!destFile.exists()) {
                            withContext(Dispatchers.IO) {
                                destFile.createNewFile()
                            }
                        }
                        destFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        context.sendBroadcast(
                            Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.fromFile(File(destFile.path))
                            )
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.toast_photo_saved, destFile.path),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun isGifFile(inputStream: java.io.InputStream?): Boolean {
        if (inputStream == null) return false
        val bytes = ByteArray(4)
        return try {
            inputStream.read(bytes)
            val str = String(bytes)
            str.equals("GIF8", ignoreCase = true)
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun changeFileExtension(fileName: String, extension: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex != -1) {
            fileName.substring(0, dotIndex) + extension
        } else {
            fileName + extension
        }
    }
}

private fun java.io.InputStream.copyTo(fileDescriptor: java.io.FileDescriptor) {
    java.io.FileOutputStream(fileDescriptor).use { outputStream ->
        copyTo(outputStream)
    }
}
