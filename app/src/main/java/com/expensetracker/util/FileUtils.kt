package com.expensetracker.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {

    fun saveFileToCache(context: Context, fileName: String, content: ByteArray): Result<File> {
        return try {
            val file = File(context.cacheDir, fileName)
            file.outputStream().use { out ->
                out.write(content)
            }
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun saveFileToExternal(context: Context, fileName: String, content: ByteArray): Result<File> {
        return try {
            val dir = context.getExternalFilesDir(null) ?: context.cacheDir
            val file = File(dir, fileName)
            file.outputStream().use { out ->
                out.write(content)
            }
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun createShareIntent(context: Context, file: File, mimeType: String = "text/csv"): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".csv") -> "text/csv"
            fileName.endsWith(".json") -> "application/json"
            fileName.endsWith(".pdf") -> "application/pdf"
            else -> "application/octet-stream"
        }
    }

    fun copyInputStreamToFile(inputStream: InputStream, file: File): Result<Unit> {
        return try {
            file.outputStream().use { out ->
                inputStream.copyTo(out)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteFile(file: File): Boolean {
        return file.delete()
    }

    fun getFileSize(file: File): Long {
        return file.length()
    }

    fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }
}