package com.example.data

import android.content.Context
import android.os.Environment
import android.os.storage.StorageManager
import com.example.model.FolderShortcut
import java.io.File

object FolderRepository {
    fun roots(context: Context): List<File> {
        val manager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        return (listOf(Environment.getExternalStorageDirectory()) + manager.storageVolumes.mapNotNull {
            it.uuid?.let { uuid -> File("/storage", uuid) }
        }).distinctBy { it.absolutePath }.filter { it.isDirectory }
    }

    fun resolve(context: Context, shortcut: FolderShortcut): File? =
        if (shortcut.automaticUsb) roots(context).firstOrNull { it.absolutePath != Environment.getExternalStorageDirectory().absolutePath }
            ?.let { root -> if (shortcut.path.isBlank()) root else File(root, shortcut.path) }
        else shortcut.path.takeIf { it.isNotBlank() }?.let(::File)

    fun permitted(context: Context, file: File): Boolean = runCatching {
        val target = file.canonicalPath
        roots(context).any { root -> target == root.canonicalPath || target.startsWith(root.canonicalPath + "/") }
    }.getOrDefault(false)

    fun entries(context: Context, directory: File): List<File> {
        if (!permitted(context, directory) || !directory.isDirectory || !directory.canRead()) return emptyList()
        return directory.listFiles()?.filter { !it.name.startsWith(".") && (it.isDirectory || isVideo(it)) }
            ?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() }) ?: emptyList()
    }

    fun isVideo(file: File): Boolean = file.extension.lowercase() in
        setOf("mp4", "mkv", "avi", "mov", "m4v", "webm", "ts", "m2ts", "mpeg", "mpg", "wmv", "flv", "3gp", "vob")
}
