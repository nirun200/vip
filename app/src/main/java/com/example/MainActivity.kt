package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.BaccaratViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: BaccaratViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Edge-to-edge friendly system navigation bar padding
                    }
                ) { innerPadding ->
                    BaccaratApp(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun BaccaratApp(
    viewModel: BaccaratViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val results by viewModel.currentResults.collectAsStateWithLifecycle()
    val shoeName by viewModel.shoeName.collectAsStateWithLifecycle()
    val savedShoes by viewModel.savedShoes.collectAsStateWithLifecycle()
    val activeHand by viewModel.activeHand.collectAsStateWithLifecycle()
    val isDealing by viewModel.isDealing.collectAsStateWithLifecycle()
    val aiAnalysis by viewModel.aiAnalysis.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Realistic Simulator, 2: Money Formula & Saved Shoes

    // Toast message trigger for simple interactive alerts
    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearUiMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCharcoal)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Elegant Luxury Header Banner
            AppHeaderBanner(shoeName = shoeName, currentResultsCount = results.size)

            // Primary Screen Content based on active tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Crossfade(targetState = currentTab, label = "tabTransition") { tab ->
                    when (tab) {
                        0 -> DashboardScreen(
                            results = results,
                            aiAnalysis = aiAnalysis,
                            viewModel = viewModel
                        )
                        1 -> SimulatorScreen(
                            activeHand = activeHand,
                            isDealing = isDealing,
                            viewModel = viewModel,
                            results = results
                        )
                        2 -> MoneyAndHistoricalScreen(
                            savedShoes = savedShoes,
                            viewModel = viewModel,
                            shoeName = shoeName,
                            results = results
                        )
                    }
                }
            }

            // High-End Premium Material Tab Navigation Row
            BaccaratBottomNavBar(
                activeTab = currentTab,
                onTabSelect = { currentTab = it }
            )
        }
    }
}

// ================= HEADER BANNER =================
@Composable
fun AppHeaderBanner(shoeName: String, currentResultsCount: Int) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F3E2B),
                            Color(0xFF11141A)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(BrightGold, shape = CircleShape)
                                .shadow(4.dp, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "สูตรบาคาร่า AI อัจฉริยะ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChampagneGold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ขอนไพ่ปัจจุบัน: ",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Text(
                            text = shoeName,
                            fontSize = 12.sp,
                            color = TextLight,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Round counter badge
                Box(
                    modifier = Modifier
                        .background(
                            color = LightCasinoGreen.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, ChampagneGold.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "รอบที่ $currentResultsCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrightGold
                    )
                }
            }
        }
        Divider(color = ChampagneGold.copy(alpha = 0.3f), thickness = 1.dp)
    }
}

// ================= CUSTOM TAB BAR =================
@Composable
fun BaccaratBottomNavBar(activeTab: Int, onTabSelect: (Int) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardSlate,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NavBarItem(
                isActive = activeTab == 0,
                icon = Icons.Default.Home,
                label = "สูตรและสถิติ",
                onClick = { onTabSelect(0) }
            )
            NavBarItem(
                isActive = activeTab == 1,
                icon = Icons.Default.PlayArrow,
                label = "จำลองแทงจริง",
                onClick = { onTabSelect(1) }
            )
            NavBarItem(
                isActive = activeTab == 2,
                icon = Icons.Default.List,
                label = "ประวัติ & ทบเงิน",
                onClick = { onTabSelect(2) }
            )
        }
    }
}

@Composable
fun RowScope.NavBarItem(
    isActive: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val activeColor = ChampagneGold
    val inactiveColor = TextMuted
    val scale by animateFloatAsState(if (isActive) 1.05f else 1.0f, label = "tabScale")

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .rotate(if (isActive) 0f else 0f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) activeColor else inactiveColor,
            modifier = Modifier
                .rotate(if (isActive) -2f else 0f)
                .size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isActive) activeColor else inactiveColor,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ================= TAB 1: DASHBOARD =================
@Composable
fun DashboardScreen(
    results: List<BaccaratResult>,
    aiAnalysis: AIAnalysis,
    viewModel: BaccaratViewModel
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showPlayerPair by remember { mutableStateOf(false) }
    var showBankerPair by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("ล้างข้อมูลขอนไพ่", color = ChampagneGold) },
            text = { Text("คุณมั่นใจหรือไม่ที่จะรีเซ็ตขอนไพ่นี้? ข้อมูลผลลัพธ์รอบทั้งหมดจะสูญหาย", color = TextLight) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearCurrentShoe()
                    showResetDialog = false
                }) {
                    Text("ยืนยันรีเซ็ต", color = BankerCrimson)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("ยกเลิก", color = TextMuted)
                }
            },
            containerColor = CardSlate
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Predictor Hero Card
        item {
            AILiveIndicatorCard(analysis = aiAnalysis, nextRoundIndex = results.size + 1)
        }

        // Statistical Grids Title
        item {
            SectionHeader(title = "ตารางรายงานผลลัพธ์ขอนไพ่")
        }

        // Bead Plate Composable (ตารางใหญ่แบบไข่ปลา)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ตารางไข่ปลา (Bead Plate)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChampagneGold
                        )
                        Text(
                            text = "ปัดเพื่อดูขอนเก่า ➔",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    BeadPlateGrid(results = results)
                }
            }
        }

        // Big Road Composable (ตารางหลัก)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "ตารางถนนใหญ่ (Big Road)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChampagneGold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    BigRoadGrid(results = results)
                }
            }
        }

        // Interactive Result Input Controls Panel
        item {
            SectionHeader(title = "ระบบจดแต้มแมนนวล (คลิกเพื่อบันทึกผล)")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Optional pair tick options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = showPlayerPair,
                                onCheckedChange = { showPlayerPair = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = PlayerBlue,
                                    uncheckedColor = TextMuted
                                )
                            )
                            Text("ผู้เล่นคู่ (PP)", color = PlayerBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = showBankerPair,
                                onCheckedChange = { showBankerPair = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = BankerCrimson,
                                    uncheckedColor = TextMuted
                                )
                            )
                            Text("เจ้ามือคู่ (BP)", color = BankerCrimson, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Big Manual input buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // PLAYER BUTTON
                        Button(
                            onClick = {
                                viewModel.addManualResult(
                                    winner = HandWinner.PLAYER,
                                    playerPair = showPlayerPair,
                                    bankerPair = showBankerPair
                                )
                                // Auto reset checks
                                showPlayerPair = false
                                showBankerPair = false
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PlayerBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("PLAYER", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                Text("ผู้เล่น (น้ำเงิน)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }

                        // TIE BUTTON
                        Button(
                            onClick = {
                                viewModel.addManualResult(
                                    winner = HandWinner.TIE,
                                    playerPair = showPlayerPair,
                                    bankerPair = showBankerPair
                                )
                                showPlayerPair = false
                                showBankerPair = false
                            },
                            modifier = Modifier
                                .weight(0.8f)
                                .height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TieGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TIE", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                Text("เสมอ (เขียว)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }

                        // BANKER BUTTON
                        Button(
                            onClick = {
                                viewModel.addManualResult(
                                    winner = HandWinner.BANKER,
                                    playerPair = showPlayerPair,
                                    bankerPair = showBankerPair
                                )
                                showPlayerPair = false
                                showBankerPair = false
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BankerCrimson),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BANKER", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                Text("เจ้ามือ (แดง)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Undo/Reset Utility actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.undoLastResult() },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .padding(end = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ChampagneGold),
                            border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Undo", size = 16.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ย้อนกลับจุดจด", fontSize = 13.sp)
                        }

                        Button(
                            onClick = { showResetDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .padding(start = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C161D)),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BankerCrimson.copy(alpha = 0.4f))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = BankerCrimson, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("รีเซ็ตขอนไพ่", color = BankerCrimson, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Summary Statistics Pie-Bar Representation
        item {
            ShoeStatsBreakdown(results = results)
        }
    }
}

// Helper icons size shorthand
@Composable
private fun Icon(imageVector: ImageVector, contentDescription: String, size: androidx.compose.ui.unit.Dp) {
    Icon(imageVector, contentDescription, modifier = Modifier.size(size), tint = LocalContentColor.current)
}


// ================= AI PREDICTION HERO CARD =================
@Composable
fun AILiveIndicatorCard(analysis: AIAnalysis, nextRoundIndex: Int) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            CasinoTableGreen,
            CardSlate
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, ChampagneGold)
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(BrightGold, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI วิเคราะห์แนวโน้มหลักรอบที่ #$nextRoundIndex",
                            color = ChampagneGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Win rate stat
                    Text(
                        text = "ความแม่นสูตรโดยประมวล: ${analysis.matchRate}%",
                        color = BrightGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Big Prediction Color Box indicator
                    val recColor = when (analysis.recommendation) {
                        HandWinner.PLAYER -> PlayerBlue
                        HandWinner.BANKER -> BankerCrimson
                        HandWinner.TIE -> TieGreen
                    }
                    val recTextTh = when (analysis.recommendation) {
                        HandWinner.PLAYER -> "ผู้เล่น (P)"
                        HandWinner.BANKER -> "เจ้ามือ (B)"
                        HandWinner.TIE -> "เสมอ (T)"
                    }

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(recColor, shape = RoundedCornerShape(12.dp))
                            .shadow(6.dp, shape = RoundedCornerShape(12.dp))
                            .border(1.dp, ChampagneGold.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = analysis.recommendation.name.first().toString(),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "แนะนำลงทุนถัดไป:",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Text(
                            text = recTextTh,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = recColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Match Confidence level bar
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ระดับความแม่นยำ: ",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "${analysis.confidence}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrightGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress confidence indicator bar
                LinearProgressIndicator(
                    progress = { analysis.confidence / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = BrightGold,
                    trackColor = Color(0xFF142C1E)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Confidence stars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star",
                                modifier = Modifier.size(16.dp),
                                tint = if (index < analysis.strengthStars) BrightGold else Color.Gray.copy(alpha = 0.4f)
                            )
                        }
                    }

                    Text(
                        text = "ระดับความมั่นใจ: ${analysis.strengthStars}/5 ดาว",
                        fontSize = 11.sp,
                        color = TextLight,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Explainable Formula text box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.Black.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Analysis",
                            tint = ChampagneGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = analysis.predictionFormula,
                            fontSize = 11.sp,
                            color = TextLight
                        )
                    }
                }
            }
        }
    }
}


// ================= GRID GRAPHICS =================

// Horizontal scrollable bead plate grid
@Composable
fun BeadPlateGrid(results: List<BaccaratResult>) {
    val itemsPerColumn = 6
    val columnsCount = if (results.isEmpty()) 10 else ((results.size + 5) / itemsPerColumn).coerceAtLeast(10)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(Color(0xFF090B0F), shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF2C3E50).copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (col in 0 until columnsCount) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (row in 0 until 6) {
                        val index = col * itemsPerColumn + row
                        if (index < results.size) {
                            BeasSphere(result = results[index])
                        } else {
                            EmptyBead()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BeasSphere(result: BaccaratResult) {
    val winnerBg = when (result.winner) {
        HandWinner.PLAYER -> PlayerBlue
        HandWinner.BANKER -> BankerCrimson
        HandWinner.TIE -> TieGreen
    }
    
    val winLetter = when (result.winner) {
        HandWinner.PLAYER -> "P"
        HandWinner.BANKER -> "B"
        HandWinner.TIE -> "T"
    }

    Box(
        modifier = Modifier
            .size(24.dp)
            .background(winnerBg, shape = CircleShape)
            .border(
                width = if (result.playerPair || result.bankerPair) 1.5.dp else 0.dp,
                color = if (result.playerPair) Color.Cyan else if (result.bankerPair) Color.Yellow else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = winLetter,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        
        // Small pair dots indicator
        if (result.playerPair) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(Color.Cyan, shape = CircleShape)
                    .align(Alignment.TopStart)
            )
        }
        if (result.bankerPair) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(Color.Yellow, shape = CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun EmptyBead() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(Color(0xFF141722), shape = CircleShape)
            .border(0.5.dp, Color(0xFF252A34), shape = CircleShape)
    )
}

// Scrollable Big Road Grid
@Composable
fun BigRoadGrid(results: List<BaccaratResult>) {
    // Group results by winner to compute big road columns
    val nonTies = results.filter { it.winner != HandWinner.TIE }
    val columns = remember(nonTies) { computeBigRoadColumns(results) }
    val displayColsCount = columns.size.coerceAtLeast(15)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(Color(0xFF090B0F), shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF2C3E50).copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (colIndex in 0 until displayColsCount) {
                val colList = if (colIndex < columns.size) columns[colIndex] else emptyList()
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Maximum 6 rows down in big road grid display
                    for (rowIndex in 0 until 6) {
                        if (rowIndex < colList.size) {
                            RoadRing(result = colList[rowIndex])
                        } else {
                            EmptyBead()
                        }
                    }
                }
            }
        }
    }
}

private fun computeBigRoadColumns(results: List<BaccaratResult>): List<List<BaccaratResult>> {
    val columns = mutableListOf<MutableList<BaccaratResult>>()
    if (results.isEmpty()) return columns

    var currentColumn = mutableListOf<BaccaratResult>()
    columns.add(currentColumn)

    results.forEach { res ->
        if (res.winner == HandWinner.TIE) {
            // Tie attaches to the last item, or starts first column if empty
            if (currentColumn.isNotEmpty()) {
                currentColumn.add(res)
            } else {
                currentColumn.add(res)
            }
        } else {
            if (currentColumn.isEmpty()) {
                currentColumn.add(res)
            } else {
                val lastWinner = currentColumn.lastOrNull { it.winner != HandWinner.TIE }?.winner
                if (lastWinner == null || lastWinner == res.winner) {
                    if (currentColumn.size < 6) {
                        currentColumn.add(res)
                    } else {
                        // Bend to the right! In simple list representation, just spawn a new column
                        currentColumn = mutableListOf(res)
                        columns.add(currentColumn)
                    }
                } else {
                    // Winner switched! Start a new column
                    currentColumn = mutableListOf(res)
                    columns.add(currentColumn)
                }
            }
        }
    }
    return columns
}

@Composable
fun RoadRing(result: BaccaratResult) {
    if (result.winner == HandWinner.TIE) {
        // Tie ring representation: Green diagonal dash inside empty circle
        val borderBrush = Brush.sweepGradient(listOf(TieGreen, TieGreen))
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color.Transparent, shape = CircleShape)
                .border(2.dp, TieGreen, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "/",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TieGreen
            )
        }
        return
    }

    val themeColor = when (result.winner) {
        HandWinner.PLAYER -> PlayerBlue
        HandWinner.BANKER -> BankerCrimson
        else -> TieGreen
    }

    Box(
        modifier = Modifier
            .size(24.dp)
            .background(Color.Transparent, shape = CircleShape)
            .border(2.2.dp, themeColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Circle core is hollow in classic road
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    if (result.playerPair || result.bankerPair) themeColor else Color.Transparent,
                    shape = CircleShape
                )
        )
    }
}


// ================= STATISTICS BREAKDOWN =================
@Composable
fun ShoeStatsBreakdown(results: List<BaccaratResult>) {
    val totalCount = results.size
    val pCount = results.count { it.winner == HandWinner.PLAYER }
    val bCount = results.count { it.winner == HandWinner.BANKER }
    val tCount = results.count { it.winner == HandWinner.TIE }
    val pPairCount = results.count { it.playerPair }
    val bPairCount = results.count { it.bankerPair }
    val naturalCount = results.count { it.natural }

    val pPct = if (totalCount == 0) 0 else (pCount * 100 / totalCount)
    val bPct = if (totalCount == 0) 0 else (bCount * 100 / totalCount)
    val tPct = if (totalCount == 0) 0 else (tCount * 100 / totalCount)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "อัตราส่วนและรายงานสถิติขอนนี้",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ChampagneGold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Percentage Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                if (totalCount == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ยังไม่มีข้อมูลสถิติสัดส่วน", fontSize = 10.sp, color = TextLight)
                    }
                } else {
                    if (pCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight(pCount.toFloat())
                                .fillMaxHeight()
                                .background(PlayerBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$pPct%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    if (tCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight(tCount.toFloat())
                                .fillMaxHeight()
                                .background(TieGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$tPct%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    if (bCount > 0) {
                        Box(
                            modifier = Modifier
                                .weight(bCount.toFloat())
                                .fillMaxHeight()
                                .background(BankerCrimson),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$bPct%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatUnitBox(label = "ผู้เล่น (Player)", count = pCount, pct = pPct, color = PlayerBlue)
                StatUnitBox(label = "เสมอ (Tie)", count = tCount, pct = tPct, color = TieGreen)
                StatUnitBox(label = "เจ้ามือ (Banker)", count = bCount, pct = bPct, color = BankerCrimson)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.Gray.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ผู้เล่นคู่ (PP)", fontSize = 11.sp, color = TextMuted)
                    Text("$pPairCount ครั้ง", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PlayerBlue)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("เจ้ามือคู่ (BP)", fontSize = 11.sp, color = TextMuted)
                    Text("$bPairCount ครั้ง", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BankerCrimson)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ป๊อกแปด/เก้า", fontSize = 11.sp, color = TextMuted)
                    Text("$naturalCount ครั้ง", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrightGold)
                }
            }
        }
    }
}

@Composable
fun StatUnitBox(label: String, count: Int, pct: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, size = 11.dp, color = TextMuted)
        Text(
            text = "$count ครั้ง ($pct%)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun Text(text: String, size: androidx.compose.ui.unit.Dp, color: Color) {
    Text(text = text, fontSize = size.value.sp, color = color)
}


// ================= TAB 2: INTERACTIVE DEALING SIMULATOR =================
@Composable
fun SimulatorScreen(
    activeHand: ActiveHand,
    isDealing: Boolean,
    viewModel: BaccaratViewModel,
    results: List<BaccaratResult>
) {
    val resultsSize = results.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Golden Casino Felt table billboard
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                CasinoTableGreen,
                                Color(0xFF071F14)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(1.5.dp, ChampagneGold, shape = RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                // Table branding markings
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header title of felt
                    Text(
                        text = "โต๊ะทดลองสูตรและแจกไพ่จำลอง (ROYAL FELT)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChampagneGold.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )

                    // Players & Banker Cards layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // PLAYER FIELD
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "PLAYER",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PlayerBlue
                            )
                            
                            // Score Card
                            Box(
                                modifier = Modifier
                                    .background(PlayerBlue.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${activeHand.playerScore} แต้ม",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PlayerBlue
                                )
                            }

                            // Horizontal list of cards
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.height(100.dp)
                            ) {
                                if (activeHand.playerHand.isEmpty()) {
                                    EmptyCardSlot()
                                } else {
                                    activeHand.playerHand.forEach { card ->
                                        PlayingCardUi(card = card)
                                    }
                                }
                            }
                        }

                        // VS Indicator
                        Text(
                            text = "VS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = ChampagneGold
                        )

                        // BANKER FIELD
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "BANKER",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BankerCrimson
                            )

                            // Score Card
                            Box(
                                modifier = Modifier
                                    .background(BankerCrimson.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${activeHand.bankerScore} แต้ม",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BankerCrimson
                                )
                            }

                            // Horizontal list of cards
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.height(100.dp)
                            ) {
                                if (activeHand.bankerHand.isEmpty()) {
                                    EmptyCardSlot()
                                } else {
                                    activeHand.bankerHand.forEach { card ->
                                        PlayingCardUi(card = card)
                                    }
                                }
                            }
                        }
                    }

                    // Display Hand result banner if finished
                    if (activeHand.handState == HandState.COMPLETED && activeHand.lastResult != null) {
                        val finalResult = activeHand.lastResult
                        val bannerColor = when (finalResult.winner) {
                            HandWinner.PLAYER -> PlayerBlue
                            HandWinner.BANKER -> BankerCrimson
                            HandWinner.TIE -> TieGreen
                        }
                        val winnerTh = when (finalResult.winner) {
                            HandWinner.PLAYER -> "PLAYER ชนะ!"
                            HandWinner.BANKER -> "BANKER ชนะ!"
                            HandWinner.TIE -> "เสมอ (TIE)!"
                        }

                        Box(
                            modifier = Modifier
                                .background(bannerColor, shape = RoundedCornerShape(8.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$winnerTh [ผลบันทึกแล้ว]",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else if (isDealing) {
                        CircularProgressIndicator(
                            color = ChampagneGold,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "กดปุ่มด้านล่างเพื่อทำการเริ่มแจกไพ่",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Action Trigger Button
        item {
            Button(
                onClick = { viewModel.startDealHand() },
                enabled = !isDealing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(8.dp, shape = RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrightGold,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Deal",
                        tint = DarkCharcoal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isDealing) "กำลังดำเนินการแจกไพ่..." else "แจกไพ่และคำนวณผลออโต้",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoal
                    )
                }
            }
        }

        // Guide text detailing the Baccarat drawing algorithm
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "Draw Rules", tint = ChampagneGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "กติกาการจั่วป๊อกแปดป๊อกเก้าจำลอง",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChampagneGold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• หากฝ่ายใดฝ่ายหนึ่งได้ผลป๊อก 8 หรือ 9 ใน 2 ใบแรก จะจบเกมทันที\n" +
                                "• ฝั่งผู้เล่น (Player) จะจั่วใบที่ 3 เมื่อแต้มรวมมีค่า 0-5 แต้ม\n" +
                                "• ฝั่งเจ้ามือ (Banker) จั่วใบที่ 3 โดยเป็นไปตามตารางกฎสากลคณิตศาสตร์บาคาร่า ขึ้นกับแต้มและใบที่สามของผู้เล่นเพื่อจำลองความยุติธรรมสูงสุด",
                        fontSize = 11.sp,
                        color = TextLight,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCardSlot() {
    Box(
        modifier = Modifier
            .width(62.dp)
            .fillMaxHeight()
            .background(Color.Black.copy(alpha = 0.25f), shape = RoundedCornerShape(6.dp))
            .border(1.dp, ChampagneGold.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("ไพ่", fontSize = 11.sp, color = TextMuted)
    }
}

@Composable
fun PlayingCardUi(card: Card) {
    val suitColor = if (card.suit.isRed) BankerCrimson else Color.Black

    Card(
        modifier = Modifier
            .width(66.dp)
            .fillMaxHeight()
            .shadow(4.dp, shape = RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Value and Suit small
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = card.rank.representation,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = suitColor
                )
                Spacer(modifier = Modifier.width(1.dp))
                Text(
                    text = card.suit.symbol,
                    fontSize = 12.sp,
                    color = suitColor
                )
            }

            // Big Center Suit
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.suit.symbol,
                    fontSize = 24.sp,
                    color = suitColor
                )
            }

            // Bottom inverse (empty space/rotated placeholder alignment shorthand)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = card.rank.representation,
                    fontSize = 10.sp,
                    color = suitColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}


// ================= TAB 3: MONEY SYSTEM & SAVED SHOES =================
@Composable
fun MoneyAndHistoricalScreen(
    savedShoes: List<BaccaratShoe>,
    viewModel: BaccaratViewModel,
    shoeName: String,
    results: List<BaccaratResult>
) {
    var brandNewShoeName by remember { mutableStateOf("") }
    var baseBetInput by remember { mutableStateOf("100") }
    var selectedFormulaTab by remember { mutableStateOf(0) } // 0: Martingale, 1: Super Martingale, 2: 1-3-2-4 Strategy

    // Bet calculator math
    val baseBet = baseBetInput.toDoubleOrNull() ?: 100.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Shoename Persistence Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ระบบจัดเก็บเซฟขอนไพ่นี้",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChampagneGold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = brandNewShoeName,
                            onValueChange = { brandNewShoeName = it },
                            placeholder = { Text("ระบุชื่อโต๊ะไพ่ เช่น ห้อง 7 บาคาร่าปอยเปต", color = TextMuted, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ChampagneGold,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (brandNewShoeName.isNotBlank()) {
                                    viewModel.changeShoeName(brandNewShoeName)
                                    viewModel.saveCurrentShoeToDb()
                                    brandNewShoeName = ""
                                } else {
                                    viewModel.saveCurrentShoeToDb()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("เซฟขอน", color = DarkCharcoal, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Saved Shoes list
        item {
            Text(
                text = "แฟ้มบันทึกขอนไพ่แมนนวลในอดีต (${savedShoes.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ChampagneGold
            )
        }

        if (savedShoes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.05f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ยังไม่มีขอนไพ่ที่บันทึกไว้ในความจำเครื่อง",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(savedShoes) { shoe ->
                SavedShoeItem(shoe = shoe, onSelect = { viewModel.loadSavedShoe(shoe) }, onDelete = { viewModel.deleteSavedShoe(shoe.id) })
            }
        }

        // Dynamic Money management tables
        item {
            SectionHeader(title = "สูตรเดินเงินบาคาร่า (Money Management Calculator)")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ระบุยอดหน่วยลงทุนเริ่มต้น (Base Unit)",
                        fontSize = 12.sp,
                        color = ChampagneGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = baseBetInput,
                        onValueChange = { baseBetInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Text("฿", color = ChampagneGold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChampagneGold,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tab selector formulas
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                    ) {
                        FormulaTabItem(name = "1. ทบสองเท่า (Martingale)", isActive = selectedFormulaTab == 0) { selectedFormulaTab = 0 }
                        FormulaTabItem(name = "2. ซุปเปอร์มาติงเกล", isActive = selectedFormulaTab == 1) { selectedFormulaTab = 1 }
                        FormulaTabItem(name = "3. แผน 1-3-2-4", isActive = selectedFormulaTab == 2) { selectedFormulaTab = 2 }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Formula calculated steps details
                    when (selectedFormulaTab) {
                        0 -> {
                            Text(
                                "ระบบทบเดินเงินแบบมาติงเกลสากล: เสียตาใดให้ทบยอดเดิม 2 เท่าเพื่อดึงต้นทุนและกำไรกลับทันทีเมื่อชนะหนึ่งครา",
                                fontSize = 11.sp, color = TextLight, lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            MartingaleCalculatedSteps(base = baseBet)
                        }
                        1 -> {
                            Text(
                                "สูตรเดินเงินอัพเกรด (Super Martingale): ยอดทบสะสมเพิ่ม 1 หน่วยพิเศษ เพื่อให้รอบชนะดึงสติและส่วนเกินทวีคูณยิ่งขึ้น",
                                fontSize = 11.sp, color = TextLight, lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            SuperMartingaleCalculatedSteps(base = baseBet)
                        }
                        2 -> {
                            Text(
                                "ระบบความสมดุลต่ำ (1-3-2-4): หากชนะเล่นตามสเต็ป 1, 3, 2, และ 4 หน่วย หากสเต็ปใดแพ้ตัดขาดทุนกลับจุดสตาร์ทรอบแรกสุดยอดเยี่ยมเพื่อความปลอดภัย",
                                fontSize = 11.sp, color = TextLight, lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Balanced1324StepCalculations(base = baseBet)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.FormulaTabItem(name: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .background(if (isActive) ChampagneGold else Color.Transparent, shape = RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) DarkCharcoal else TextMuted,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 12.sp
        )
    }
}

@Composable
fun MartingaleCalculatedSteps(base: Double) {
    val fmt = { amt: Double -> String.format("฿%,.0f", amt) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BetStepRow(step = 1, amount = fmt(base), isFocus = true)
        BetStepRow(step = 2, amount = fmt(base * 2), isFocus = false)
        BetStepRow(step = 3, amount = fmt(base * 4), isFocus = false)
        BetStepRow(step = 4, amount = fmt(base * 8), isFocus = false)
        BetStepRow(step = 5, amount = fmt(base * 16), isFocus = false)
        BetStepRow(step = 6, amount = fmt(base * 32), isFocus = false)
    }
}

@Composable
fun SuperMartingaleCalculatedSteps(base: Double) {
    val fmt = { amt: Double -> String.format("฿%,.0f", amt) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BetStepRow(step = 1, amount = fmt(base), isFocus = true)
        BetStepRow(step = 2, amount = fmt(base * 2 + base), isFocus = false)
        BetStepRow(step = 3, amount = fmt(base * 4 + base), isFocus = false)
        BetStepRow(step = 4, amount = fmt(base * 8 + base), isFocus = false)
        BetStepRow(step = 5, amount = fmt(base * 16 + base), isFocus = false)
        BetStepRow(step = 6, amount = fmt(base * 32 + base), isFocus = false)
    }
}

@Composable
fun Balanced1324StepCalculations(base: Double) {
    val fmt = { amt: Double -> String.format("฿%,.0f", amt) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BetStepRow(step = 1, amount = fmt(base), isFocus = true)
        BetStepRow(step = 2, amount = fmt(base * 3), isFocus = false)
        BetStepRow(step = 3, amount = fmt(base * 2), isFocus = false)
        BetStepRow(step = 4, amount = fmt(base * 4), isFocus = false)
    }
}

@Composable
fun BetStepRow(step: Int, amount: String, isFocus: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isFocus) ChampagneGold.copy(alpha = 0.08f) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("ไม้ที่ $step (Step $step)", fontSize = 12.sp, color = TextLight, fontWeight = if (isFocus) FontWeight.Bold else FontWeight.Normal)
        Text(amount, fontSize = 13.sp, color = if (isFocus) BrightGold else TextLight, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SavedShoeItem(
    shoe: BaccaratShoe,
    onSelect: (BaccaratShoe) -> Unit,
    onDelete: () -> Unit
) {
    val resultsSize = shoe.getResultsList().size
    val dateString = remember(shoe.createdAt) {
        val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
        sdf.format(Date(shoe.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(shoe) },
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shoe.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChampagneGold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "ข้อมูลจดแล้ว $resultsSize ไม้ | เซฟเมื่อ $dateString",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = BankerCrimson
                )
            }
        }
    }
}


// ================= GENERIC REUSABLE COMPONENTS =================
@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(14.dp)
                .background(ChampagneGold)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextLight,
            letterSpacing = 0.5.sp
        )
    }
}
