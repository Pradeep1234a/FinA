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

    // File picker integration
    fun createFilePickerIntent(mimeTypes: Array<String> = arrayOf("text/csv", "application/json"), allowMultiple: Boolean = false): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
        }
    }

    fun createDirectoryPickerIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
    }

    // Get file from URI (for handling file picker results)
    fun getFileFromUri(context: Context, uri: Uri): Result<File> {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                return Result.failure(IllegalArgumentException("Could not open input stream for URI"))
            }

            // Create a temporary file in cache
            val fileName = getFileNameFromUri(context, uri) ?: "imported_file"
            val tempFile = File(context.cacheDir, fileName)
            copyInputStreamToFile(inputStream, tempFile).getOrThrow()
            Result.success(tempFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get directory from URI (for handling directory picker results)
    fun getDirectoryFromUri(context: Context, uri: Uri): Result<File> {
        return try {
            // For directory URIs, we can't directly get a File object
            // Return a wrapper that provides access via DocumentFile or similar
            // For simplicity, we return a file-like object that can be used with DocumentFile APIs
            Result.failure(UnsupportedOperationException("Directory URIs require DocumentFile API - use context.contentResolver directly"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use { c ->
            if (c.moveToFirst()) {
                val nameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) c.getString(nameIndex) else null
            } else null
        }
    }

    // Read file content from URI
    fun readFileFromUri(context: Context, uri: Uri): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                return Result.failure(IllegalArgumentException("Could not open input stream for URI"))
            }
            val content = inputStream.readText()
            inputStream.close()
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Write file content to URI (for saving files via Storage Access Framework)
    fun writeFileToUri(context: Context, uri: Uri, content: String): Result<Unit> {
        return try {
            val outputStream = context.contentResolver.openOutputStream(uri)
            if (outputStream == null) {
                return Result.failure(IllegalArgumentException("Could not open output stream for URI"))
            }
            outputStream.write(content.toByteArray())
            outputStream.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}