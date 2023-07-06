package fr.kokyett.rsspire.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.webkit.URLUtil
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream

class DownloaderUtils {
    companion object {
        private const val TIMEOUT = 30 * DateTimeUtils.SECOND
        private const val MAXIMUM_REDIRECTS = 7

        fun getBitmap(url: String): Bitmap? {
            return if (URLUtil.isValidUrl(url)) {
                getBitmap(URL(url))
            } else {
                //TODO: Log
                null
            }
        }

        fun getBitmap(url: URL): Bitmap? {
            return try {
                val bytes = getBytes(url)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                //TODO: Log Exception
                null
            }
        }

        fun getString(url: String): String? {
            return if (URLUtil.isValidUrl(url)) {
                val connection = getHttpURLConnection(URL(url), 0)
                val bytes = getBytes(connection)
                val charset = getCharset(connection)
                connection.disconnect()

                if (charset == null)
                    String(bytes)
                else
                    String(bytes, Charset.forName(charset))
            } else {
                //TODO: Log
                null
            }
        }

        private fun getCharset(connection: HttpURLConnection): String? {
            val contentType = connection.contentType
            if (contentType != null) {
                val start = contentType.indexOf("charset=")
                val end = contentType.indexOf(";", start + 8)
                if (start == -1) {
                    return null
                }
                return if (end == -1) {
                    contentType.substring(start + 8)
                } else {
                    contentType.substring(start + 8, end)
                }
            }
            return null
        }

        private fun getBytes(url: URL): ByteArray {
            val connection = getHttpURLConnection(url, 0)
            val bytes = getBytes(connection)
            connection.disconnect()
            return bytes
        }

        private fun getHttpURLConnection(url: URL, cycle: Int): HttpURLConnection {
            val connection = url.openConnection() as HttpURLConnection
            HttpURLConnection.setFollowRedirects(true)
            connection.doInput = true
            connection.doOutput = false
            connection.setRequestProperty("Accept", "*/*")
            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/115.0"
            )
            connection.connectTimeout = TIMEOUT
            connection.readTimeout = TIMEOUT
            connection.useCaches = false
            connection.connect()
            val location = connection.getHeaderField("Location")
            if (location != null) {
                connection.disconnect()
                return if (cycle < MAXIMUM_REDIRECTS) {
                    getHttpURLConnection(URL(location), cycle + 1)
                } else {
                    throw IOException("Too many redirects for $url")
                }
            }
            return connection
        }

        private fun getBytes(connection: HttpURLConnection): ByteArray {
            var inputStream = connection.inputStream
            if (connection.contentEncoding == "gzip" && inputStream !is GZIPInputStream) {
                inputStream = GZIPInputStream(inputStream)
            }
            var n: Int
            val buffer = ByteArray(4096)
            val baos = ByteArrayOutputStream()
            while (inputStream.read(buffer).also { n = it } > 0) {
                baos.write(buffer, 0, n)
            }
            val bytes = baos.toByteArray()
            baos.close()
            inputStream.close()
            return bytes
        }
    }
}