package com.example.tou

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import java.io.File


@Composable
fun AttachmentsSection(
    attachments: List<String>,
    onAttachmentsChange: (List<String>) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var cameraImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            onAttachmentsChange((attachments + cameraImageUri.toString()).take(10))
        }
    }

    /*val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val newPaths = uris.map { it.toString() }
        onAttachmentsChange((attachments + newPaths).take(10))
    }*/

    var showViewer by remember { mutableStateOf(false) }
    var pendingImages by remember { mutableStateOf(listOf<String>()) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            pendingImages = uris.map { it.toString() }
            showViewer = true
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val newPaths = uris.map { it.toString() }
        onAttachmentsChange((attachments + newPaths).take(10))
    }

    if (showViewer && pendingImages.isNotEmpty()) {
        ImageViewerScreen(
            images = pendingImages,
            onConfirm = { selected ->
                onAttachmentsChange((attachments + selected).take(10))
                showViewer = false
                pendingImages = emptyList()
            },
            onDismiss = {
                showViewer = false
                pendingImages = emptyList()
            }
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        attachments.forEachIndexed { index, path ->
            val isImage = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp")
                .any { path.lowercase().endsWith(it) } || path.contains("image")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isImage) {
                    AsyncImage(
                        model = path,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).padding(8.dp)
                    )
                }
                Text(
                    text = path.substringAfterLast("/"),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    maxLines = 1
                )
                IconButton(onClick = {
                    onAttachmentsChange(attachments.toMutableList().also { it.removeAt(index) })
                }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.cd_delete))
                }
            }
        }

        // Кнопка скріпка
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = stringResource(R.string.cd_attach)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {DropdownMenuItem(
                text = { Text(stringResource(R.string.take_photo)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                },
                onClick = {
                    showMenu = false
                    // створюємо тимчасовий файл для фото
                    val photoFile = File(
                        context.cacheDir,
                        "photo_${System.currentTimeMillis()}.jpg"
                    )
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        photoFile
                    )
                    cameraImageUri = uri
                    cameraLauncher.launch(uri)
                }
            )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.btn_add_image)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null)
                    },
                    onClick = {
                        showMenu = false
                        imagePicker.launch("image/*")
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.btn_add_file)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.AttachFile, contentDescription = null)
                    },
                    onClick = {
                        showMenu = false
                        filePicker.launch("*/*")
                    }
                )
            }
        }
    }
}