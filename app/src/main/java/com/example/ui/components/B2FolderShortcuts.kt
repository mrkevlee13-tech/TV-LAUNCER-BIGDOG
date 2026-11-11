package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FolderRepository
import com.example.model.FolderShortcut
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
private fun FolderWindow(onClose: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false)) {
        Column(Modifier.fillMaxSize().background(B2TileSurface).padding(28.dp), verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
    }
}

@Composable
internal fun FolderAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, focus: FocusRequester? = null) {
    TvFocusableSurface(itemId = "folder_$text", itemLabel = text, shape = RoundedCornerShape(10.dp),
        onClick = onClick, focusRequester = focus, modifier = modifier.heightIn(min = 40.dp)) {
        Text(text, color = B2MainText, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), maxLines = 2)
    }
}

@Composable
fun B2FolderBrowser(
    initialPath: String?,
    selectFolder: Boolean,
    onChooseFolder: (String) -> Unit = {},
    onPlayVideo: (File) -> Unit = {},
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var directory by remember { mutableStateOf(initialPath?.let(::File)) }
    var history by remember { mutableStateOf(emptyList<File?>()) }
    val roots = remember { FolderRepository.roots(context) }
    val entries by produceState<List<File>?>(null, directory) {
        value = null
        value = withContext(Dispatchers.IO) { directory?.let { FolderRepository.entries(context, it) } ?: roots }
    }
    val back = {
        if (history.isEmpty()) onClose() else { directory = history.last(); history = history.dropLast(1) }
    }
    BackHandler { back() }
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(directory, entries) { if (entries != null) { withFrameNanos { }; firstFocus.requestFocus() } }
    FolderWindow(onClose) {
        Text(if (selectFolder) "Choose a folder" else "Videos", color = B2MainText, fontSize = 26.sp)
        Text(directory?.absolutePath ?: "Shared storage and USB drives", color = B2QuietText, fontSize = 13.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FolderAction("Back", back, focus = firstFocus)
            if (directory != null) {
                FolderAction("Up a folder", {
                    val current = directory!!
                    history = history + listOf(directory)
                    directory = if (roots.any { it.canonicalPath == current.canonicalPath }) null else current.parentFile
                })
                if (selectFolder) FolderAction("Use this folder", { onChooseFolder(directory!!.absolutePath) })
            }
        }
        if (entries == null) Text("Loading folders...", color = B2QuietText)
        else if (entries!!.isEmpty()) Text(
            if (directory?.isDirectory != true || directory?.canRead() != true) "Folder unavailable. Reconnect the drive or choose another folder."
            else "No videos or subfolders here. You can still select this folder.", color = B2QuietText)
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(5.dp)) {
            items(entries.orEmpty(), key = { it.absolutePath }) { file ->
                if (!selectFolder || file.isDirectory) TvFocusableSurface(
                    itemId = "file_${file.absolutePath}", itemLabel = file.name,
                    shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(48.dp),
                    onClick = {
                        if (file.isDirectory) { history = history + listOf(directory); directory = file }
                        else onPlayVideo(file)
                    }
                ) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Icon(if (file.isDirectory) Icons.Outlined.Folder else Icons.Outlined.Movie, null, tint = B2Accent)
                        Text(file.name.ifBlank { "Internal storage" }, color = B2MainText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
fun B2FolderSettings(initial: List<FolderShortcut>, onSave: (List<FolderShortcut>) -> Unit, onClose: () -> Unit) {
    var draft by remember { mutableStateOf(initial) }
    var editing by remember { mutableStateOf<FolderShortcut?>(null) }
    var choosing by remember { mutableStateOf(false) }
    val close = { if (editing != null) { editing = null; choosing = false } else onClose() }
    BackHandler { close() }
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(editing?.id, choosing) { if (!choosing) { withFrameNanos { }; firstFocus.requestFocus() } }
    FolderWindow(close) {
        Text("Home folder shortcuts", color = B2MainText, fontSize = 26.sp)
        Text("Videos open in VLC. App order: hold OK on a Home app, move Left/Right, OK to save, Back to cancel.", color = B2QuietText, fontSize = 13.sp)
        val item = editing
        if (item != null) {
            OutlinedTextField(value = item.name, onValueChange = { editing = item.copy(name = it.take(60)) },
                label = { Text("Shortcut name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Text(if (item.automaticUsb) "Current USB drive / ${item.path.ifBlank { "root" }}" else item.path.ifBlank { "Choose a folder" }, color = B2QuietText)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FolderAction("Choose folder", { choosing = true }, focus = firstFocus)
                FolderAction(if (item.visible) "Shown on Home" else "Hidden from Home", { editing = item.copy(visible = !item.visible) })
                FolderAction("Save shortcut", {
                    if (item.name.isNotBlank() && (item.automaticUsb || item.path.isNotBlank())) {
                        draft = if (draft.any { it.id == item.id }) draft.map { if (it.id == item.id) item else it } else draft + item
                        onSave(draft); editing = null
                    }
                })
                FolderAction("Cancel", { editing = null })
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FolderAction("Done", onClose, focus = firstFocus)
                FolderAction("Add folder", { editing = FolderShortcut("folder_${System.currentTimeMillis()}", "Videos") })
                FolderAction("Add current USB", {
                    draft = draft + FolderShortcut("usb_${System.currentTimeMillis()}", "USB videos", automaticUsb = true); onSave(draft)
                })
            }
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(5.dp)) {
                items(draft, key = { it.id }) { shortcut ->
                    Column(Modifier.fillMaxWidth().background(B2RaisedSurface, RoundedCornerShape(12.dp)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(shortcut.name + if (shortcut.visible) "" else " (hidden)", color = B2MainText, fontSize = 17.sp)
                        Text(if (shortcut.automaticUsb) "Current USB drive / ${shortcut.path.ifBlank { "root" }}" else shortcut.path, color = B2QuietText, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FolderAction("Edit", { editing = shortcut })
                            FolderAction(if (shortcut.visible) "Hide" else "Show", {
                                draft = draft.map { if (it.id == shortcut.id) it.copy(visible = !it.visible) else it }; onSave(draft)
                            })
                            FolderAction("Move earlier", {
                                val index = draft.indexOfFirst { it.id == shortcut.id }
                                if (index > 0) { draft = draft.toMutableList().apply { add(index - 1, removeAt(index)) }; onSave(draft) }
                            })
                            FolderAction("Move later", {
                                val index = draft.indexOfFirst { it.id == shortcut.id }
                                if (index < draft.lastIndex) { draft = draft.toMutableList().apply { add(index + 1, removeAt(index)) }; onSave(draft) }
                            })
                            FolderAction("Remove", { draft = draft.filterNot { it.id == shortcut.id }; onSave(draft) })
                        }
                    }
                }
            }
        }
    }
    if (choosing) B2FolderBrowser(initialPath = editing?.path?.takeIf { it.isNotBlank() }, selectFolder = true,
        onChooseFolder = { path -> editing = editing?.copy(path = path, automaticUsb = false); choosing = false },
        onClose = { choosing = false })
}
