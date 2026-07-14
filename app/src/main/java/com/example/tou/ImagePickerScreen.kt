package com.example.tou

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.ui.res.stringResource

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ImageViewerScreen(
    images: List<String>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedImages by remember { mutableStateOf(images.toSet()) }
    val pagerState = rememberPagerState(pageCount = { images.size })

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // Пейджер зображень
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = images[page],
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Кружечок вибору вгорі справа
        val currentImage = images[pagerState.currentPage]
        val isSelected = currentImage in selectedImages

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f))
                .border(2.dp, Color.White, CircleShape)
                .clickable {
                    selectedImages = if (isSelected) {
                        selectedImages - currentImage
                    } else {
                        selectedImages + currentImage
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Нижня панель
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Пензлик (майбутня функція)
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Brush,
                    contentDescription = stringResource(R.string.action_draw),
                    tint = Color.White
                )
            }

            // Обрізати/повернути
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Crop,
                    contentDescription = stringResource(R.string.action_crop),
                    tint = Color.White
                )
            }

            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.RotateRight,
                    contentDescription = stringResource(R.string.action_rotate),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Кнопка підтвердити
            Button(
                onClick = { onConfirm(selectedImages.toList()) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = stringResource(R.string.cd_attach, selectedImages.size),
                    color = Color.White
                )
                //Text(stringResource(R.string.cd_attach) (${selectedImages.size})", color = Color.White)
            }
        }

        // Кнопка закрити
        IconButton(
            onClick = { onDismiss() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.btn_close),
                tint = Color.White
            )
        }
    }
}