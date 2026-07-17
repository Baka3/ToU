package com.example.tou

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.graphics.Bitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import android.graphics.Canvas as AndroidCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import android.graphics.Picture
import androidx.compose.material.icons.filled.OpenWith

data class DoodlePath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val alpha: Float,
    val brushType: BrushType,
    val isEraser: Boolean = false
)

enum class BrushType { NORMAL, PENCIL, MARKER }

@Composable
fun DoodleScreen(
    onSave: (String) -> Unit, // повертає шлях до файлу
    onDismiss: () -> Unit
) {
    val picture = remember { Picture() }
    val paths = remember { mutableStateListOf<DoodlePath>() }
    val currentPoints = remember { mutableStateListOf<Offset>() }
    val context = LocalContext.current

    var isEraser by remember { mutableStateOf(false) }
    var brushType by remember { mutableStateOf(BrushType.NORMAL) }
    var strokeWidth by remember { mutableStateOf(8f) }
    var alpha by remember { mutableStateOf(1f) }
    var color by remember { mutableStateOf(Color.Black) }

    var showBrushMenu by remember { mutableStateOf(false) }
    var showSizeMenu by remember { mutableStateOf(false) }
    var showColorMenu by remember { mutableStateOf(false) }
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isTransformMode by remember { mutableStateOf(false) }


    val colors = listOf(
        Color.Black, Color.White, Color.Red, Color.Green,
        Color.Blue, Color.Yellow, Color.Cyan, Color.Magenta,
        Color(0xFFFF6B00), Color(0xFF9C27B0), Color(0xFF795548)
    )

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Верхня панель
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onDismiss() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.btn_close))
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    // створюємо bitmap з намальованого
                    val width = canvasSize.width.toInt().takeIf { it > 0 } ?: 1080
                    val height = canvasSize.height.toInt().takeIf { it > 0 } ?: 1920

                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val androidCanvas = android.graphics.Canvas(bitmap)
                    androidCanvas.drawColor(android.graphics.Color.WHITE)
                    paths.forEach { path -> drawDoodlePathOnCanvas(androidCanvas, path) }

                    // зберігаємо у файл
                    val file = File(context.cacheDir, "doodle_${System.currentTimeMillis()}.png")
                    java.io.FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    onSave(file.absolutePath)
                }) {
                    Text(stringResource(R.string.btn_save))
                }
            }

            // Холст
            val canvasModifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFDDDDDD)) // сірий фон щоб видно краї холсту
                .onSizeChanged { size ->
                    canvasSize = androidx.compose.ui.geometry.Size(
                        size.width.toFloat(),
                        size.height.toFloat()
                    )
                }
                .pointerInput(isTransformMode) {
                    if (isTransformMode) {
                        detectTransformGestures { _, pan, zoom, rotationDelta ->
                            scale = (scale * zoom).coerceIn(0.1f, 5f)
                            rotation += rotationDelta
                            offset += pan
                        }
                    }
                }
                .pointerInput(isTransformMode, isEraser, brushType, color, strokeWidth, alpha) {
                    if (!isTransformMode) {
                        detectDragGestures(
                            onDragStart = { offset -> currentPoints.clear(); currentPoints.add(offset) },
                            onDrag = { change, _ -> currentPoints.add(change.position) },
                            onDragEnd = {
                                if (currentPoints.isNotEmpty()) {
                                    paths.add(DoodlePath(
                                        points = currentPoints.toList(),
                                        color = if (isEraser) Color.White else color,
                                        strokeWidth = if (isEraser) strokeWidth * 3 else strokeWidth,
                                        alpha = if (isEraser) 1f else alpha,
                                        brushType = brushType,
                                        isEraser = isEraser
                                    ))
                                    currentPoints.clear()
                                }
                            }
                        )
                    }
                }

// Холст з трансформацією
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFDDDDDD))
                    .onSizeChanged { size ->
                        canvasSize = androidx.compose.ui.geometry.Size(
                            size.width.toFloat(),
                            size.height.toFloat()
                        )
                    }
                    .pointerInput(isTransformMode) {
                        if (isTransformMode) {
                            detectTransformGestures { _, pan, zoom, rotationDelta ->
                                scale = (scale * zoom).coerceIn(0.1f, 5f)
                                rotation += rotationDelta
                                offset += pan
                            }
                        }
                    }
                    .pointerInput(isTransformMode, isEraser, brushType, color, strokeWidth, alpha) {
                        if (!isTransformMode) {
                            detectDragGestures(
                                onDragStart = { o -> currentPoints.clear(); currentPoints.add(o) },
                                onDrag = { change, _ -> currentPoints.add(change.position) },
                                onDragEnd = {
                                    if (currentPoints.isNotEmpty()) {
                                        paths.add(DoodlePath(
                                            points = currentPoints.toList(),
                                            color = if (isEraser) Color.White else color,
                                            strokeWidth = if (isEraser) strokeWidth * 3 else strokeWidth,
                                            alpha = if (isEraser) 1f else alpha,
                                            brushType = brushType,
                                            isEraser = isEraser
                                        ))
                                        currentPoints.clear()
                                    }
                                }
                            )
                        }
                    }
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            rotationZ = rotation,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .background(Color.White)
                ) {
                    paths.forEach { path -> drawDoodlePath(path) }
                    if (currentPoints.size > 1) {
                        drawDoodlePath(DoodlePath(
                            points = currentPoints.toList(),
                            color = if (isEraser) Color.White else color,
                            strokeWidth = if (isEraser) strokeWidth * 3 else strokeWidth,
                            alpha = if (isEraser) 1f else alpha,
                            brushType = brushType
                        ))
                    }
                }
            }
            /*Canvas(modifier = canvasModifier) {
                paths.forEach { path -> drawDoodlePath(path) }
                if (currentPoints.size > 1) {
                    drawDoodlePath(
                        DoodlePath(
                            points = currentPoints.toList(),
                            color = if (isEraser) Color.White else color,
                            strokeWidth = if (isEraser) strokeWidth * 3 else strokeWidth,
                            alpha = if (isEraser) 1f else alpha,
                            brushType = brushType,
                            isEraser = isEraser
                        )
                    )
                }
            }*/

            // Нижня панель
            IconButton(
                onClick = { isTransformMode = !isTransformMode },
                modifier = Modifier.background(
                    if (isTransformMode) Color.LightGray else Color.Transparent,
                    CircleShape
                )
            ) {
                Icon(
                    imageVector = Icons.Default.OpenWith,
                    contentDescription = stringResource(R.string.desc_move)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 1. Пензлик/Ластік
                IconButton(
                    onClick = { isEraser = !isEraser },
                    modifier = Modifier
                        .background(
                            if (isEraser) Color.LightGray else Color.Transparent,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isEraser) Icons.Default.AutoFixNormal
                        else Icons.Default.Edit,
                        contentDescription = if (isEraser) stringResource(R.string.desc_eraser) else stringResource(R.string.desc_brush)
                    )
                }

                // 2. Тип пензлика
                Box {
                    IconButton(onClick = { showBrushMenu = true }) {
                        Icon(imageVector = Icons.Default.Brush, contentDescription = stringResource(R.string.desc_brush_type))
                    }
                    DropdownMenu(
                        expanded = showBrushMenu,
                        onDismissRequest = { showBrushMenu = false }
                    ) {
                        listOf(
                            BrushType.NORMAL to stringResource(R.string.brush_normal),
                            BrushType.PENCIL to stringResource(R.string.brush_pencil),
                            BrushType.MARKER to stringResource(R.string.brush_marker)

                        ).forEach { (type, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                leadingIcon = {
                                    if (brushType == type) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                                    }
                                },
                                onClick = {
                                    brushType = type
                                    showBrushMenu = false
                                }
                            )
                        }
                    }
                }

                // 3. Товщина і прозорість
                Box {
                    IconButton(onClick = { showSizeMenu = !showSizeMenu }) {
                        Icon(imageVector = Icons.Default.LinearScale, contentDescription = stringResource(R.string.desc_size))
                    }
                    DropdownMenu(
                        expanded = showSizeMenu,
                        onDismissRequest = { showSizeMenu = false }
                    ) {
                        Column(modifier = Modifier.padding(16.dp).width(200.dp)) {
                            Text(stringResource(R.string.brush_thickness, strokeWidth.toInt()))
                            Slider(
                                value = strokeWidth,
                                onValueChange = { strokeWidth = it },
                                valueRange = 2f..50f
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.brush_opacity, (alpha * 100).toInt()))
                            Slider(
                                value = alpha,
                                onValueChange = { alpha = it },
                                valueRange = 0.1f..1f
                            )
                        }
                    }
                }

                // 4. Колір
                Box {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(2.dp, Color.Gray, CircleShape)
                            .clickable { showColorMenu = !showColorMenu }
                    )
                    DropdownMenu(
                        expanded = showColorMenu,
                        onDismissRequest = { showColorMenu = false }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(stringResource(R.string.brush_select_color), modifier = Modifier.padding(bottom = 8.dp))
                            colors.chunked(4).forEach { row ->
                                Row {
                                    row.forEach { c ->
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .padding(4.dp)
                                                .clip(CircleShape)
                                                .background(c)
                                                .border(
                                                    width = if (c == color) 3.dp else 1.dp,
                                                    color = if (c == color) Color.Blue else Color.Gray,
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    color = c
                                                    isEraser = false
                                                    showColorMenu = false
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Скасувати останній штрих
                IconButton(
                    onClick = { if (paths.isNotEmpty()) paths.removeLastOrNull() }
                ) {
                    Icon(imageVector = Icons.Default.Undo, contentDescription = stringResource(R.string.btn_cancel))
                }
            }
        }
    }
}

fun DrawScope.drawDoodlePath(path: DoodlePath) {
    if (path.points.size < 2) return
    val paintColor = when (path.brushType) {
        BrushType.PENCIL -> path.color.copy(alpha = path.alpha * 0.7f)
        BrushType.MARKER -> path.color.copy(alpha = path.alpha * 0.5f)
        else -> path.color.copy(alpha = path.alpha)
    }
    val paintWidth = when (path.brushType) {
        BrushType.PENCIL -> path.strokeWidth * 0.7f
        BrushType.MARKER -> path.strokeWidth * 2f
        else -> path.strokeWidth
    }
    val cap = if (path.brushType == BrushType.MARKER) StrokeCap.Square else StrokeCap.Round

    val androidPath = androidx.compose.ui.graphics.Path()
    androidPath.moveTo(path.points.first().x, path.points.first().y)
    path.points.drop(1).forEach { androidPath.lineTo(it.x, it.y) }

    drawPath(
        path = androidPath,
        color = paintColor,
        style = Stroke(width = paintWidth, cap = cap, join = StrokeJoin.Round)
    )
}

fun drawDoodlePathOnCanvas(canvas: android.graphics.Canvas, path: DoodlePath) {
    if (path.points.size < 2) return
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeJoin = android.graphics.Paint.Join.ROUND
        color = when (path.brushType) {
            BrushType.PENCIL -> android.graphics.Color.argb(
                (path.alpha * 0.7f * 255).toInt(),
                (path.color.red * 255).toInt(),
                (path.color.green * 255).toInt(),
                (path.color.blue * 255).toInt()
            )
            BrushType.MARKER -> android.graphics.Color.argb(
                (path.alpha * 0.5f * 255).toInt(),
                (path.color.red * 255).toInt(),
                (path.color.green * 255).toInt(),
                (path.color.blue * 255).toInt()
            )
            else -> android.graphics.Color.argb(
                (path.alpha * 255).toInt(),
                (path.color.red * 255).toInt(),
                (path.color.green * 255).toInt(),
                (path.color.blue * 255).toInt()
            )
        }
        strokeWidth = when (path.brushType) {
            BrushType.PENCIL -> path.strokeWidth * 0.7f
            BrushType.MARKER -> path.strokeWidth * 2f
            else -> path.strokeWidth
        }
        strokeCap = if (path.brushType == BrushType.MARKER)
            android.graphics.Paint.Cap.SQUARE
        else android.graphics.Paint.Cap.ROUND
    }
    val nativePath = android.graphics.Path()
    nativePath.moveTo(path.points.first().x, path.points.first().y)
    path.points.drop(1).forEach { nativePath.lineTo(it.x, it.y) }
    canvas.drawPath(nativePath, paint)
}