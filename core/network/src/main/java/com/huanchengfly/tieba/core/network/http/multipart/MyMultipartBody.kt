package com.huanchengfly.tieba.core.network.http.multipart

import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import java.io.IOException
import java.util.UUID

fun buildMultipartBody(
    boundary: String = UUID.randomUUID().toString(),
    builder: MyMultipartBody.Builder.() -> Unit
): MyMultipartBody {
    return MyMultipartBody.Builder(boundary)
        .apply(builder)
        .build()
}

@Suppress("NAME_SHADOWING")
class MyMultipartBody internal constructor(
    private val boundaryByteString: ByteString,
    @get:JvmName("type") val type: MediaType,
    @get:JvmName("parts") val parts: List<Part>
) : RequestBody() {
    private val contentType: MediaType = "$type; boundary=$boundary".toMediaType()
    private var contentLength = -1L

    @get:JvmName("boundary")
    val boundary: String
        get() = boundaryByteString.utf8()

    @get:JvmName("size")
    val size: Int
        get() = parts.size

    fun part(index: Int): Part = parts[index]

    override fun contentType(): MediaType = contentType

    @Throws(IOException::class)
    override fun contentLength(): Long {
        var result = contentLength
        if (result == -1L) {
            result = writeOrCountBytes(null, true)
            contentLength = result
        }
        return result
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        writeOrCountBytes(sink, false)
    }

    @Throws(IOException::class)
    private fun writeOrCountBytes(
        sink: BufferedSink?,
        countBytes: Boolean
    ): Long {
        var sink = sink
        var byteCount = 0L

        var byteCountBuffer: Buffer? = null
        if (countBytes) {
            byteCountBuffer = Buffer()
            sink = byteCountBuffer
        }

        for (part in parts) {
            val headers = part.headers
            val body = part.body

            checkNotNull(sink).write(DASHDASH)
            sink.write(boundaryByteString)
            sink.write(CRLF)

            if (headers != null) {
                for (h in 0 until headers.size) {
                    sink.writeUtf8(headers.name(h))
                        .write(COLONSPACE)
                        .writeUtf8(headers.value(h))
                        .write(CRLF)
                }
            }

            val contentType = body.contentType()
            if (contentType != null) {
                sink.writeUtf8("Content-Type: ")
                    .writeUtf8(contentType.toString())
                    .write(CRLF)
            }

            val contentLength = body.contentLength()
            if (countBytes) {
                if (contentLength == -1L) {
                    checkNotNull(byteCountBuffer).clear()
                    return -1L
                }
            }

            sink.write(CRLF)

            if (countBytes) {
                byteCount += contentLength
            } else {
                body.writeTo(sink)
            }

            sink.write(CRLF)
        }

        checkNotNull(sink).write(DASHDASH)
        sink.write(boundaryByteString)
        sink.write(DASHDASH)
        sink.write(CRLF)

        if (countBytes) {
            byteCount += checkNotNull(byteCountBuffer).size
            byteCountBuffer.clear()
        }

        return byteCount
    }

    class Part private constructor(
        @get:JvmName("headers") val headers: Headers?,
        @get:JvmName("body") val body: RequestBody
    ) {
        companion object {
            @JvmStatic
            fun create(body: RequestBody): Part = create(null, body)

            @JvmStatic
            fun create(headers: Headers?, body: RequestBody): Part {
                require(headers?.get("Content-Type") == null) { "Unexpected header: Content-Type" }
                require(headers?.get("Content-Length") == null) { "Unexpected header: Content-Length" }
                return Part(headers, body)
            }

            @JvmStatic
            fun createFormData(name: String, value: String): Part =
                createFormData(name, null, value.toRequestBody())

            @JvmStatic
            fun createFormData(name: String, filename: String?, body: RequestBody): Part {
                val disposition = buildString {
                    append("form-data; name=")
                    appendQuotedString(name)

                    if (filename != null) {
                        append("; filename=")
                        appendQuotedString(filename)
                    }
                }

                val headers = Headers.Builder()
                    .addUnsafeNonAscii("Content-Disposition", disposition)
                    .build()

                return create(headers, body)
            }
        }
    }

    class Builder @JvmOverloads constructor(boundary: String = UUID.randomUUID().toString()) {
        private val boundary: ByteString = boundary.encodeUtf8()
        private var type = MIXED
        private val parts = mutableListOf<Part>()

        fun setType(type: MediaType) = apply {
            require(type.type == "multipart") { "multipart != $type" }
            this.type = type
        }

        fun addPart(body: RequestBody) = apply {
            addPart(Part.create(body))
        }

        fun addPart(headers: Headers?, body: RequestBody) = apply {
            addPart(Part.create(headers, body))
        }

        fun addFormDataPart(name: String, value: String) = apply {
            addPart(Part.createFormData(name, value))
        }

        fun addFormDataPart(name: String, filename: String?, body: RequestBody) = apply {
            addPart(Part.createFormData(name, filename, body))
        }

        fun addPart(part: Part) = apply {
            parts += part
        }

        fun build(): MyMultipartBody {
            check(parts.isNotEmpty()) { "Multipart body must have at least one part." }
            return MyMultipartBody(boundary, type, parts.toList())
        }
    }

    companion object {
        @JvmField
        val MIXED = "multipart/mixed".toMediaType()

        @JvmField
        val ALTERNATIVE = "multipart/alternative".toMediaType()

        @JvmField
        val DIGEST = "multipart/digest".toMediaType()

        @JvmField
        val PARALLEL = "multipart/parallel".toMediaType()

        @JvmField
        val FORM = "multipart/form-data".toMediaType()

        private val COLONSPACE = byteArrayOf(':'.code.toByte(), ' '.code.toByte())
        private val CRLF = byteArrayOf('\r'.code.toByte(), '\n'.code.toByte())
        private val DASHDASH = byteArrayOf('-'.code.toByte(), '-'.code.toByte())

        internal fun StringBuilder.appendQuotedString(key: String) {
            append('"')
            for (element in key) {
                when (element) {
                    '\n' -> append("%0A")
                    '\r' -> append("%0D")
                    '"' -> append("%22")
                    else -> append(element)
                }
            }
            append('"')
        }
    }
}
