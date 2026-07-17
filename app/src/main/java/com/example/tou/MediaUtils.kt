package com.example.tou

import android.content.Context
import android.net.Uri

fun getMimeType(context: Context, path: String): String {
    return try {
        if (path.startsWith("content://")) {
            context.contentResolver.getType(Uri.parse(path)) ?: ""
        } else {
            android.webkit.MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(path.substringAfterLast(".").lowercase()) ?: ""
        }
    } catch (e: Exception) {
        ""
    }
}

fun isImagePath(context: Context, path: String): Boolean {
    if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") ||
        path.endsWith(".gif") || path.endsWith(".webp")) return true
    val mime = getMimeType(context, path)
    return mime.startsWith("image/")
}

fun isVideoPath(context: Context, path: String): Boolean {
    if (listOf(".mp4", ".mov", ".avi", ".mkv", ".webm", ".3gp")
            .any { path.lowercase().endsWith(it) }) return true
    val mime = getMimeType(context, path)
    return mime.startsWith("video/")
}

fun isAudioPath(context: Context, path: String): Boolean {
    if (listOf(".m4a", ".mp3", ".wav", ".ogg", ".aac")
            .any { path.lowercase().endsWith(it) }) return true
    if (path.contains("voice_")) return true
    val mime = getMimeType(context, path)
    return mime.startsWith("audio/")
}