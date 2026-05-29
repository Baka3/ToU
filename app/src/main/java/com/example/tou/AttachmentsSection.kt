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

@Composable
fun AttachmentsSection(
    attachments: List<String>,
    onAttachmentsChange: (List<String>) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val newPaths = uris.map { it.toString() }
        onAttachmentsChange((attachments + newPaths).take(10))
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val newPaths = uris.map { it.toString() }
        onAttachmentsChange((attachments + newPaths).take(10))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Показуємо прикріплені файли
        attachments.forEachIndexed { index, path ->
            val isImage = path.contains("image") ||
                    listOf(".jpg", ".jpeg", ".png", ".gif", ".webp")
                        .any { path.lowercase().endsWith(it) }

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
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Видалити")
                }
            }
        }

        // Кнопка скріпка
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Прикріпити"
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Додати зображення") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null)
                    },
                    onClick = {
                        showMenu = false
                        imagePicker.launch("image/*")
                    }
                )
                DropdownMenuItem(
                    text = { Text("Додати файл") },
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