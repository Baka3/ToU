package com.example.tou

/*fun isImagePath(path: String): Boolean {
    return path.contains("image") ||
            listOf(".jpg", ".jpeg", ".png", ".gif", ".webp")
                .any { path.lowercase().endsWith(it) } ||
            (path.startsWith("content://") && !path.contains("audio") &&
                    !path.contains("video") && !path.contains("document"))
}*/

fun isVideoPath(path: String): Boolean {
    return path.contains("video") ||
            listOf(".mp4", ".mov", ".avi", ".mkv", ".webm")
                .any { path.lowercase().endsWith(it) }
}

fun isAudioPath(path: String): Boolean {
    return path.contains("audio") ||
            listOf(".m4a", ".mp3", ".wav", ".ogg", ".aac")
                .any { path.lowercase().endsWith(it) }
}