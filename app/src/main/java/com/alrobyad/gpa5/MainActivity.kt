package com.alrobyad.gpa5

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GPA5App() }
    }
}

@Composable
fun GPA5App() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("gpa5_prefs", Context.MODE_PRIVATE) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var isBangla by remember { mutableStateOf(prefs.getBoolean("is_bangla", false)) }
    var totalStudyMinutes by remember { mutableIntStateOf(prefs.getInt("total_study_mins", 0)) }
    var xpPoints by remember { mutableIntStateOf(prefs.getInt("xp_points", 0)) }
    var auraPoints by remember { mutableIntStateOf(prefs.getInt("aura_points", 100)) }

    val stars = RankAndSeasonEngine.calculateStars(totalStudyMinutes)
    val currentRank = RankAndSeasonEngine.getRankFromStars(stars)
    val currentSeason = RankAndSeasonEngine.getCurrentSeasonId()
    val bg = Brush.verticalGradient(listOf(Color(0xFF0F071E), Color(0xFF1F0C3D)))

    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFA855F7))) {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF170B2E)) {
                    val tabs = listOf(
                        Pair(if (isBangla) "হোম" else "Home", Icons.Default.Home),
                        Pair(if (isBangla) "ফোকাস" else "Focus", Icons.Default.Star),
                        Pair(if (isBangla) "প্ল্যান" else "Plan", Icons.Default.CheckCircle),
                        Pair("SSC", Icons.Default.Info),
                        Pair(if (isBangla) "স্ট্যাটস" else "Stats", Icons.Default.List)
                    )
                    tabs.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(item.second, contentDescription = null) },
                            label = { Text(item.first, fontSize = 11.sp) }
                        )
                    }
                }
            }
        ) { pad ->
            Box(modifier = Modifier.fillMaxSize().background(bg).padding(pad)) {
                when (selectedTab) {
                    0 -> HomeDashboard(isBangla, {
                        isBangla = !isBangla
                        prefs.edit().putBoolean("is_bangla", isBangla).apply()
                    }, currentRank, stars, xpPoints, auraPoints, currentSeason, totalStudyMinutes) { selectedTab = it }
                    1 -> StudyAndFocusScreen(isBangla, totalStudyMinutes) { mins, _ ->
                        totalStudyMinutes += mins
                        xpPoints += mins * 10
                        auraPoints += mins * 2
                        prefs.edit().putInt("total_study_mins", totalStudyMinutes).putInt("xp_points", xpPoints).putInt("aura_points", auraPoints).apply()
                    }
                    2 -> PlanScreen(isBangla)
                    3 -> SSCCenterScreen(isBangla)
                    4 -> StatsScreen(isBangla, totalStudyMinutes, stars, xpPoints)
                }
            }
        }
    }
}

@Composable
fun HomeDashboard(isBangla: Boolean, onLang: () -> Unit, rank: RankTier, stars: Int, xp: Int, aura: Int, season: String, mins: Int, onNav: (Int) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("GPA5", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD8B4FE))
                    Text("Season: $season (BD)", fontSize = 12.sp, color = Color(0xFFA78BFA))
                }
                OutlinedButton(onClick = onLang) { Text(if (isBangla) "English" else "বাংলা", color = Color.White) }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF261247)), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(if (isBangla) "র‍্যাংক: ${rank.bnTitle}" else "RANK: ${rank.title}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("⭐ $stars Stars | XP: $xp | Aura: $aura", color = Color(0xFFFACC15), fontSize = 14.sp)
                    Text(if (isBangla) "মোট পড়াশোনা: $mins মিনিট" else "Total Study: $mins mins", color = Color(0xFFD8B4FE), fontSize = 13.sp)
                }
            }
        }
        item {
            Button(onClick = { onNav(1) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E22CE))) {
                Text(if (isBangla) "পড়াশোনা শুরু করুন (Pomodoro)" else "Start Focus Session")
            }
        }
    }
}

@Composable
fun PlanScreen(isBangla: Boolean) {
    var text by remember { mutableStateOf("") }
    val tasks = remember { mutableStateListOf("Physics Chapter 3", "20 Math MCQs") }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(if (isBangla) "ডেইলি টাস্ক" else "Daily Tasks", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Button(onClick = { if (text.isNotBlank()) { tasks.add(text); text = "" } }) { Text("+") }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tasks) { t ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF261247)), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(t, color = Color.White, modifier = Modifier.weight(1f))
                        IconButton(onClick = { tasks.remove(t) }) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                    }
                }
            }
        }
    }
}

@Composable
fun SSCCenterScreen(isBangla: Boolean) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(if (isBangla) "SSC সেন্টার" else "SSC Center", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF3B0764)), modifier = Modifier.fillMaxWidth()) {
            Text(if (isBangla) "লক্ষ্য: জিপিএ ৫.০০ — প্রতিদিন ফোকাস স্টাডি করুন।" else "Target: GPA 5.00 — Stay focused daily.", modifier = Modifier.padding(16.dp), color = Color.White)
        }
    }
}

@Composable
fun StatsScreen(isBangla: Boolean, mins: Int, stars: Int, xp: Int) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(if (isBangla) "পরিসংখ্যান" else "Statistics", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF261247)), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Study: $mins mins | Stars: $stars ⭐ | XP: $xp", color = Color.White)
            }
        }
    }
}
