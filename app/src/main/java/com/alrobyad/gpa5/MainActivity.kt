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
        setContent {
            GPA5App()
        }
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

    val darkPurpleBg = Brush.verticalGradient(
        listOf(Color(0xFF0F071E), Color(0xFF1F0C3D), Color(0xFF120824))
    )

    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFA855F7), background = Color(0xFF0F071E))) {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF170B2E)) {
                    val items = listOf(
                        Pair(if (isBangla) "হোম" else "Home", Icons.Default.Home),
                        Pair(if (isBangla) "ফোকাস" else "Focus", Icons.Default.Timer),
                        Pair(if (isBangla) "প্ল্যান" else "Plan", Icons.Default.CheckCircle),
                        Pair("SSC", Icons.Default.School),
                        Pair(if (isBangla) "স্ট্যাটস" else "Stats", Icons.Default.BarChart)
                    )
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(item.second, contentDescription = item.first) },
                            label = { Text(item.first, fontSize = 11.sp) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(darkPurpleBg)
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    0 -> HomeDashboard(
                        isBangla = isBangla,
                        onToggleLanguage = {
                            isBangla = !isBangla
                            prefs.edit().putBoolean("is_bangla", isBangla).apply()
                        },
                        rank = currentRank,
                        stars = stars,
                        xp = xpPoints,
                        aura = auraPoints,
                        season = currentSeason,
                        totalMinutes = totalStudyMinutes,
                        onNavigate = { selectedTab = it }
                    )
                    1 -> StudyAndFocusScreen(
                        isBangla = isBangla,
                        totalActiveMinutes = totalStudyMinutes,
                        onSessionComplete = { mins, _ ->
                            totalStudyMinutes += mins
                            xpPoints += mins * 10
                            auraPoints += mins * 2
                            prefs.edit()
                                .putInt("total_study_mins", totalStudyMinutes)
                                .putInt("xp_points", xpPoints)
                                .putInt("aura_points", auraPoints)
                                .apply()
                        }
                    )
                    2 -> PlanScreen(isBangla)
                    3 -> SSCCenterScreen(isBangla)
                    4 -> StatsScreen(isBangla, totalStudyMinutes, stars, xpPoints)
                }
            }
        }
    }
}

@Composable
fun HomeDashboard(
    isBangla: Boolean,
    onToggleLanguage: () -> Unit,
    rank: RankTier,
    stars: Int,
    xp: Int,
    aura: Int,
    season: String,
    totalMinutes: Int,
    onNavigate: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("GPA5", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD8B4FE))
                    Text(
                        if (isBangla) "সিজন: $season (বাংলাদেশ সময়)" else "Season: $season (BD Time)",
                        fontSize = 12.sp,
                        color = Color(0xFFA78BFA)
                    )
                }
                OutlinedButton(onClick = onToggleLanguage) {
                    Text(if (isBangla) "English" else "বাংলা", color = Color.White)
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF261247)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(if (isBangla) "বর্তমান র‍্যাংক" else "CURRENT RANK", fontSize = 12.sp, color = Color(0xFFC084FC))
                            Text(
                                if (isBangla) rank.bnTitle else rank.title,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("⭐ $stars Stars", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFACC15))
                            Text("XP: $xp | Aura: $aura", fontSize = 13.sp, color = Color(0xFFE9D5FF))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (isBangla) "মোট পড়াশোনা: $totalMinutes মিনিট (প্রতি ৩০ মিনিটে ১ স্টার)"
                        else "Total Active Study: $totalMinutes mins (1 Star / 30 mins)",
                        fontSize = 13.sp,
                        color = Color(0xFFD8B4FE)
                    )
                }
            }
        }

        item {
            Text(
                if (isBangla) "ডিজিটাল ওয়েলবিং ও ব্লকার টুলস" else "Digital Wellbeing & Blocker Tools",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        item {
            val features = listOf(
                Pair(if (isBangla) "পড়াশোনা শুরু করুন (Pomodoro)" else "Start Focus Session", 1),
                Pair(if (isBangla) "টাস্ক, নোটস ও হ্যাবিট ট্র্যাকার" else "Tasks, Quick Notes & Habits", 2),
                Pair(if (isBangla) "SSC সেন্টার ও Al Robyad AI" else "SSC Center & Al Robyad AI", 3)
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                features.forEach { (title, tabIndex) ->
                    Card(
                        onClick = { onNavigate(tabIndex) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D0D38)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(title, color = Color.White, fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color(0xFFC084FC), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanScreen(isBangla: Boolean) {
    var taskText by remember { mutableStateOf("") }
    val tasks = remember { mutableStateListOf("Complete Physics Chapter 3", "Solve 20 Math MCQs") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
