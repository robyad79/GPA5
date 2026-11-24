package com.alrobyad.gpa5

import android.app.Activity
import android.media.RingtoneManager
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun StudyAndFocusScreen(
    isBangla: Boolean,
    totalActiveMinutes: Int,
    onSessionComplete: (Int, String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val presets = listOf(25, 50, 90)
    var selectedMinutes by remember { mutableIntStateOf(25) }
    var customMinutesInput by remember { mutableStateOf("") }
    var showCustomDialog by remember { mutableStateOf(false) }

    val subjects = remember { mutableStateListOf("Physics", "Chemistry", "Higher Math", "Biology", "English", "ICT") }
    var selectedSubject by remember { mutableStateOf("Physics") }
    var newSubjectInput by remember { mutableStateOf("") }
    var showAddSubject by remember { mutableStateOf(false) }

    var isRunning by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(25 * 60) }
    var activeTrackedSeconds by remember { mutableIntStateOf(0) }
    var keepScreenOn by remember { mutableStateOf(true) }
    var minimalMode by remember { mutableStateOf(false) }
    var wallpaperIndex by remember { mutableIntStateOf(0) }

    val wallpapers = listOf(
        listOf(Color(0xFF1A0B2E), Color(0xFF431259)),
        listOf(Color(0xFF0F172A), Color(0xFF3B0764)),
        listOf(Color(0xFF1E1B4B), Color(0xFF311042)),
        listOf(Color(0xFF090D16), Color(0xFF2E1065)),
        listOf(Color(0xFF18181B), Color(0xFF4C1D95))
    )

    DisposableEffect(isRunning, keepScreenOn) {
        if (isRunning && keepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(isRunning, isPaused) {
        while (isRunning && !isPaused && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
            activeTrackedSeconds += 1
        }
        if (isRunning && remainingSeconds == 0) {
            isRunning = false
            val studiedMins = (activeTrackedSeconds / 60).coerceAtLeast(1)
            onSessionComplete(studiedMins, selectedSubject)
            activeTrackedSeconds = 0
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                RingtoneManager.getRingtone(context, uri)?.play()
            } catch (_: Exception) {}
        }
    }

    val currentWallpaper = wallpapers[wallpaperIndex % wallpapers.size]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(currentWallpaper))
            .padding(16.dp)
    ) {
        if (minimalMode && isRunning) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%02d:%02d", remainingSeconds / 60, remainingSeconds % 60),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(onClick = { minimalMode = false }) {
                    Text(if (isBangla) "মিনিমাল মোড বন্ধ করুন" else "Exit Minimal Mode", color = Color.White)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isBangla) "ফোকাস ও স্টাডি মোড" else "Focus & Study Mode",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(subjects) { sub ->
                        FilterChip(
                            selected = selectedSubject == sub,
                            onClick = { if (!isRunning) selectedSubject = sub },
                            label = { Text(sub) }
                        )
                    }
                    item {
                        AssistChip(
                            onClick = { if (!isRunning) showAddSubject = true },
                            label = { Text("+ Custom") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { mins ->
                        Button(
                            onClick = {
                                if (!isRunning) {
                                    selectedMinutes = mins
                                    remainingSeconds = mins * 60
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedMinutes == mins) Color(0xFF9333EA) else Color(0xFF2E1065)
                            )
                        ) {
                            Text("${mins}m")
                        }
                    }
                    Button(
                        onClick = { if (!isRunning) showCustomDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B0764))
                    ) {
                        Text(if (isBangla) "কাস্টম" else "Custom")
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(230.dp)
                        .clip(CircleShape)
                        .background(Color(0x339333EA))
                        .border(4.dp, Color(0xFFC084FC), CircleShape)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%02d:%02d", remainingSeconds / 60, remainingSeconds % 60),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = selectedSubject,
                            fontSize = 14.sp,
                            color = Color(0xFFE9D5FF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!isRunning) {
                        Button(
                            onClick = {
                                isRunning = true
                                isPaused = false
                                activeTrackedSeconds = 0
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E22CE)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(if (isBangla) "শুরু করুন" else "Start Study")
                        }
                    } else {
                        Button(
                            onClick = { isPaused = !isPaused },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                        ) {
                            Text(if (isPaused) (if (isBangla) "চালু করুন" else "Resume") else (if (isBangla) "বিরতি" else "Pause"))
                        }
                        Button(
                            onClick = {
                                val studiedMins = activeTrackedSeconds / 60
                                if (studiedMins > 0) {
                                    onSessionComplete(studiedMins, selectedSubject)
                                }
                                isRunning = false
                                isPaused = false
                                remainingSeconds = selectedMinutes * 60
                                activeTrackedSeconds = 0
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
