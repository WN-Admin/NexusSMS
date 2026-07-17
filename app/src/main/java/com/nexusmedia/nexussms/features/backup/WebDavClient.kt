package com.nexusmedia.nexussms.features.backup

import android.content.Context
import android.util.Base64
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

data class WebDavFile(
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val contentType: String?
)

class WebDavClient(
    @ApplicationContext private val context: Context
) {
    private var baseUrl: String = ""
    private var username: String = ""
    private var password: String = ""
    private var isAuthenticated = false

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun authenticate(url: String, user: String, pass: String): Boolean = withContext(Dispatchers.IO) {
        try {
            baseUrl = url.trimEnd('/')
            username = user
            password = pass

            val request = Request.Builder()
                .url("$baseUrl/")
                .method("PROPFIND", "".toRequestBody("application/xml".toMediaType()))
                .header("Depth", "0")
                .header("Authorization", getBasicAuthHeader())
                .header("Content-Type", "application/xml")
                .build()

            val response = client.newCall(request).execute()
            isAuthenticated = response.isSuccessful || response.code == 207
            Timber.d("WebDAV auth result: $isAuthenticated (code=${response.code})")
            isAuthenticated
        } catch (e: Exception) {
            Timber.e(e, "WebDAV auth failed")
            isAuthenticated = false
            false
        }
    }

    suspend fun uploadFile(path: String, content: String, contentType: String = "application/json"): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/$path")
                .put(content.toRequestBody(contentType.toMediaType()))
                .header("Authorization", getBasicAuthHeader())
                .header("Content-Type", contentType)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code == 201 || response.code == 204) {
                Timber.d("WebDAV upload success: $path")
                path
            } else {
                Timber.e("WebDAV upload failed: code=${response.code}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "WebDAV upload failed: $path")
            null
        }
    }

    suspend fun downloadFile(path: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/$path")
                .get()
                .header("Authorization", getBasicAuthHeader())
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                Timber.e("WebDAV download failed: code=${response.code}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "WebDAV download failed: $path")
            null
        }
    }

    suspend fun listFiles(path: String = ""): List<WebDavFile> = withContext(Dispatchers.IO) {
        try {
            val propfindBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <D:propfind xmlns:D="DAV:">
                    <D:allprop/>
                </D:propfind>
            """.trimIndent()

            val request = Request.Builder()
                .url("$baseUrl/$path")
                .method("PROPFIND", propfindBody.toRequestBody("application/xml".toMediaType()))
                .header("Depth", "1")
                .header("Authorization", getBasicAuthHeader())
                .header("Content-Type", "application/xml")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code == 207) {
                val body = response.body?.string() ?: return@withContext emptyList()
                parseMultiStatusResponse(body, path)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "WebDAV list files failed: $path")
            emptyList()
        }
    }

    suspend fun deleteFile(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/$path")
                .delete()
                .header("Authorization", getBasicAuthHeader())
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful || response.code == 204
        } catch (e: Exception) {
            Timber.e(e, "WebDAV delete failed: $path")
            false
        }
    }

    suspend fun createDirectory(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/$path")
                .method("MKCOL", null)
                .header("Authorization", getBasicAuthHeader())
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful || response.code == 201 || response.code == 405
        } catch (e: Exception) {
            Timber.e(e, "WebDAV create directory failed: $path")
            false
        }
    }

    suspend fun fileExists(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/$path")
                .method("HEAD", null)
                .header("Authorization", getBasicAuthHeader())
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    fun isAuthenticated(): Boolean = isAuthenticated
    fun getBaseUrl(): String = baseUrl
    fun getUsername(): String = username

    private fun getBasicAuthHeader(): String {
        val credentials = "$username:$password"
        val bytes = credentials.toByteArray()
        val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return "Basic $encoded"
    }

    private fun parseMultiStatusResponse(xml: String, basePath: String): List<WebDavFile> {
        val files = mutableListOf<WebDavFile>()
        val basePathClean = basePath.trimStart('/').trimEnd('/')

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(xml.reader())

            var inResponse = false
            var href: String? = null
            var lastModified: Long = 0L
            var contentLength: Long = 0L
            var contentType: String? = null
            var currentTag: String? = null

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val tag = parser.name
                        when {
                            tag == "response" || tag.endsWith(":response") -> {
                                inResponse = true
                                href = null
                                lastModified = 0L
                                contentLength = 0L
                                contentType = null
                            }
                            inResponse -> {
                                currentTag = tag
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inResponse && currentTag != null) {
                            val text = parser.text?.trim() ?: ""
                            when {
                                currentTag!!.endsWith("href") && text.isNotEmpty() -> href = text
                                currentTag!!.endsWith("getlastmodified") && text.isNotEmpty() -> lastModified = parseIsoDate(text)
                                currentTag!!.endsWith("getcontentlength") && text.isNotEmpty() -> contentLength = text.toLongOrNull() ?: 0L
                                currentTag!!.endsWith("getcontenttype") && text.isNotEmpty() -> contentType = text
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        val tag = parser.name
                        if (tag == "response" || tag.endsWith(":response")) {
                            inResponse = false
                            if (href != null) {
                                val cleanPath = href!!.trimStart('/').trimEnd('/')
                                if (cleanPath != basePathClean && cleanPath.isNotEmpty()) {
                                    val name = href!!.split("/").filter { it.isNotEmpty() }.lastOrNull()
                                    if (name != null) {
                                        files.add(
                                            WebDavFile(
                                                path = cleanPath,
                                                name = name,
                                                size = contentLength,
                                                lastModified = lastModified,
                                                contentType = contentType
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        currentTag = null
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Timber.w(e, "XmlPullParser failed, falling back to regex")
            return parseMultiStatusResponseRegex(xml, basePath)
        }

        return files
    }

    private fun parseMultiStatusResponseRegex(xml: String, basePath: String): List<WebDavFile> {
        val files = mutableListOf<WebDavFile>()
        val responseBlocks = xml.split("<D:response>").drop(1)
        val basePathClean = basePath.trimStart('/').trimEnd('/')

        for (block in responseBlocks) {
            try {
                val hrefMatch = Regex("<D:href>([^<]+)</D:href>").find(block)
                val href = hrefMatch?.groupValues?.get(1) ?: continue
                val cleanPath = href.trimStart('/').trimEnd('/')
                if (cleanPath == basePathClean || cleanPath.isEmpty()) continue

                val lastModifiedMatch = Regex("<D:getlastmodified>([^<]+)</D:getlastmodified>").find(block)
                val lastModified = lastModifiedMatch?.groupValues?.get(1)?.let { parseIsoDate(it) } ?: 0L
                val contentLengthMatch = Regex("<D:getcontentlength>(\\d+)</D:getcontentlength>").find(block)
                val size = contentLengthMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0L
                val contentTypeMatch = Regex("<D:getcontenttype>([^<]+)</D:getcontenttype>").find(block)
                val contentType = contentTypeMatch?.groupValues?.get(1)
                val name = href.split("/").filter { it.isNotEmpty() }.lastOrNull() ?: continue

                files.add(WebDavFile(path = cleanPath, name = name, size = size, lastModified = lastModified, contentType = contentType))
            } catch (e: Exception) {
                Timber.w(e, "Failed to parse WebDAV response block")
            }
        }
        return files
    }

    private fun parseIsoDate(dateStr: String): Long {
        return try {
            val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
            format.parse(dateStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
