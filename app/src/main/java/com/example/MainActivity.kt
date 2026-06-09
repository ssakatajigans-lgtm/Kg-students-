package com.example

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Text to Speech
        try {
            tts = TextToSpeech(this, this)
        } catch (e: Exception) {
            Log.e("KidsCountingApp", "TTS initialization failed: ${e.message}")
        }

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    KidsCountingApp(
                        modifier = Modifier.padding(innerPadding),
                        onSpeak = { text, lang -> speakWord(text, lang) }
                    )
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            // Support multiple languages
            tts?.setSpeechRate(0.85f) // Slower speed so kids can understand
            tts?.setPitch(1.1f) // Slightly higher pitch for kids friendliness
        } else {
            Log.e("KidsCountingApp", "TTS initialization onInit status error")
        }
    }

    private fun speakWord(word: String, lang: String) {
        if (!isTtsInitialized || tts == null) {
            Toast.makeText(this, "Speech engine loading, please wait!", Toast.LENGTH_SHORT).show()
            return
        }

        val locale = when (lang) {
            "HI" -> Locale("hi", "IN")
            "GU" -> Locale("gu", "IN")
            else -> Locale.ENGLISH
        }

        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback for pronunciation if language data is missing, we read in default English
            tts?.language = Locale.ENGLISH
            tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "KidsSpeak")
        } else {
            tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "KidsSpeak")
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

// Simple sealed enum-class for App Screens
enum class AppScreen(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    BOARD("Rainbow Grid", Icons.Default.Home),
    FLASHCARD("Flashcards", Icons.Default.Favorite),
    TRACING("Practice Slate", Icons.Default.Edit),
    QUIZ("Fun Quiz", Icons.Default.Star)
}

// Simple enum for active quiz styles
enum class QuizLanguage {
    ENGLISH, HINDI, GUJARATI
}

// Simple enum for global voice language selection (Voice Tape)
enum class VoiceLanguage(val label: String, val langCode: String) {
    ENGLISH("English", "EN"),
    HINDI("हिन्दी", "HI"),
    GUJARATI("ગુજરાતી", "GU")
}

@Composable
fun VoiceLanguageSelector(
    activeLanguage: VoiceLanguage,
    onLanguageSelected: (VoiceLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, NaturalBorder, RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        VoiceLanguage.values().forEach { lang ->
            val isSelected = activeLanguage == lang
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) NatureGreen else Color.Transparent)
                    .clickable { 
                        onLanguageSelected(lang)
                    }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = lang.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = if (isSelected) Color.White else NatureGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Voice selection",
                        tint = if (isSelected) Color.White.copy(alpha = 0.8f) else NatureGreen.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun KidsCountingApp(
    modifier: Modifier = Modifier,
    onSpeak: (String, String) -> Unit
) {
    var currentScreen by remember { mutableStateOf(AppScreen.BOARD) }
    var selectedNumber by remember { mutableStateOf(1) }
    var scoreStarCount by remember { mutableStateOf(0) }
    var activeVoiceLanguage by remember { mutableStateOf(VoiceLanguage.ENGLISH) }

    // Helper callback to trigger detail and switch to Flashcard model
    val onNumberSelected: (Int) -> Unit = { num ->
        selectedNumber = num
        currentScreen = AppScreen.FLASHCARD
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        // App header
        AppHeader(scoreStarCount = scoreStarCount)

        // Real-time Voice Tape Language selection bar
        VoiceLanguageSelector(
            activeLanguage = activeVoiceLanguage,
            onLanguageSelected = { lang ->
                activeVoiceLanguage = lang
                // Provide elegant pronunciation feedback on toggle
                val greeting = when (lang) {
                    VoiceLanguage.ENGLISH -> "English voice active"
                    VoiceLanguage.HINDI -> "हिंदी"
                    VoiceLanguage.GUJARATI -> "ગુજરાતી"
                }
                onSpeak(greeting, lang.langCode)
            }
        )

        // Contents switching
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentScreen) {
                AppScreen.BOARD -> BoardScreen(
                    onNumberClick = { num ->
                        val detail = NumberData.list[num - 1]
                        val (speakWord, langCode) = when (activeVoiceLanguage) {
                            VoiceLanguage.ENGLISH -> Pair(detail.englishWord, "EN")
                            VoiceLanguage.HINDI -> Pair(detail.hindiWord, "HI")
                            VoiceLanguage.GUJARATI -> Pair(detail.gujaratiWord, "GU")
                        }
                        onSpeak(speakWord, langCode)
                        onNumberSelected(num)
                    }
                )
                AppScreen.FLASHCARD -> FlashcardScreen(
                    selectedNumber = selectedNumber,
                    onNumberChange = { selectedNumber = it },
                    activeVoiceLanguage = activeVoiceLanguage,
                    onSpeak = onSpeak
                )
                AppScreen.TRACING -> TracingScreen(
                    selectedNumber = selectedNumber,
                    onSpeak = onSpeak
                )
                AppScreen.QUIZ -> QuizScreen(
                    onAnswerCorrect = {
                        scoreStarCount += 1
                    },
                    onSpeak = onSpeak
                )
            }
        }

        // Bottom Navigation Bar
        KidsBottomNav(
            currentScreen = currentScreen,
            onScreenSelected = { currentScreen = it }
        )
    }
}

@Composable
fun AppHeader(scoreStarCount: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "StarRotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StarTilt"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "LEARNING PATH",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    color = NatureGreen.copy(alpha = 0.7f),
                    fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Count Together",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = NaturalText,
                    fontFamily = FontFamily.SansSerif
                )
            }

            // Right side: score + seed icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Interactive shiny star collection count
                Row(
                    modifier = Modifier
                        .background(SageGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .border(1.dp, NaturalBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Stars collected",
                        tint = Sand,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(angle)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$scoreStarCount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NaturalText
                    )
                }

                // Plant badge 🌱
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SageGreen)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌱", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun KidsBottomNav(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = NaturalBorder, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppScreen.values().forEach { screen ->
                val isSelected = currentScreen == screen
                val activeColor = when (screen) {
                    AppScreen.BOARD -> NatureGreen
                    AppScreen.FLASHCARD -> Oatmeal
                    AppScreen.TRACING -> Slate
                    AppScreen.QUIZ -> Terracotta
                }

                val transition = updateTransition(isSelected, label = "ButtonScale")
                val padding by transition.animateDp(label = "ButtonPadding") { if (it) 10.dp else 4.dp }

                Box(
                    modifier = Modifier
                        .clickable { onScreenSelected(screen) }
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent)
                        .padding(horizontal = padding + 6.dp, vertical = 8.dp)
                        .testTag("nav_${screen.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            tint = if (isSelected) activeColor else Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = screen.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = if (isSelected) activeColor else Color.Gray.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

// --- SCREEN 1: BOARD / GRID VIEW ---
@Composable
fun BoardScreen(
    onNumberClick: (Int) -> Unit
) {
    var rangeFilterIndex by remember { mutableStateOf(0) }
    val filters = listOf(
        "1 to 20",
        "21 to 40",
        "41 to 60",
        "61 to 80",
        "81 to 100",
        "All 100"
    )

    val numbersList = NumberData.list
    val filteredNumbers = remember(rangeFilterIndex) {
        when (rangeFilterIndex) {
            0 -> numbersList.filter { it.value in 1..20 }
            1 -> numbersList.filter { it.value in 21..40 }
            2 -> numbersList.filter { it.value in 41..60 }
            3 -> numbersList.filter { it.value in 61..80 }
            4 -> numbersList.filter { it.value in 81..100 }
            else -> numbersList
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Range Filter Row
        Text(
            text = "Select Counting Range:",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = CocoaPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEachIndexed { index, text ->
                val isSelected = rangeFilterIndex == index
                val activeBgColor = NatureGreen
                val inactiveBgColor = Color.White
                val activeTextColor = Color.White
                val inactiveTextColor = NatureGreen

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) activeBgColor else inactiveBgColor)
                        .border(
                            1.5.dp,
                            if (isSelected) activeBgColor else NaturalBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { rangeFilterIndex = index }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("filter_range_$index")
                ) {
                    Text(
                        text = text,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = if (isSelected) activeTextColor else inactiveTextColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Numbers Rainbow Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(filteredNumbers) { detail ->
                NumberGridItem(detail = detail, onClick = { onNumberClick(detail.value) })
            }
        }
    }
}

@Composable
fun NumberGridItem(
    detail: NumberDetail,
    onClick: () -> Unit
) {
    // Select color group dynamically based on number tens
    val color = when ((detail.value - 1) / 10) {
        0 -> CandyPink
        1 -> CandyOrange
        2 -> SunYellow
        3 -> MintGreen
        4 -> SkyBlue
        5 -> BerryPurple
        6 -> CandyPink
        7 -> CandyOrange
        8 -> SunYellow
        else -> MintGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() }
            .testTag("grid_num_${detail.value}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(2.dp, color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main English label
            Text(
                text = "${detail.value}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = color.darker()
            )

            // Hindi and Gujarati secondary translations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = detail.hindiSymbol,
                    fontSize = 11.sp,
                    color = CocoaSecondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = detail.gujaratiSymbol,
                    fontSize = 11.sp,
                    color = CocoaSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// --- SCREEN 2: FLASHCARDS screen ---
@Composable
fun FlashcardScreen(
    selectedNumber: Int,
    onNumberChange: (Int) -> Unit,
    activeVoiceLanguage: VoiceLanguage,
    onSpeak: (String, String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val detail = remember(selectedNumber) { NumberData.list[selectedNumber - 1] }

    // Active Toy / Object selection index, can toggle between Candy, Balloon, Star, etc.
    var toyIndex by remember { mutableStateOf(0) }
    val toys = listOf(
        Pair("🎈", "Balloons"),
        Pair("🍬", "Candies"),
        Pair("⭐", "Stars"),
        Pair("🍎", "Apples"),
        Pair("🧸", "Teddies")
    )

    // Keeps track of which items are tapped/popped
    var poppedSet by remember(selectedNumber, toyIndex) { mutableStateOf(setOf<Int>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Prev / Next top selector box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, NaturalBorder, RoundedCornerShape(16.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (selectedNumber > 1) onNumberChange(selectedNumber - 1) },
                enabled = selectedNumber > 1
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Number",
                    tint = if (selectedNumber > 1) NatureGreen else Color.LightGray
                )
            }

            Text(
                text = "Flashcard #$selectedNumber",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = NaturalText
            )

            IconButton(
                onClick = { if (selectedNumber < 100) onNumberChange(selectedNumber + 1) },
                enabled = selectedNumber < 100
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Number",
                    tint = if (selectedNumber < 100) NatureGreen else Color.LightGray
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Giant Playful interactive Card styled matching Natural Tones mockup
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Overlapping rotating background shadow element
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.1f)
                    .clip(RoundedCornerShape(40.dp))
                    .background(SageGreen.copy(alpha = 0.5f))
                    .rotate(3f)
            )

            // Main foreground card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.1f),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, NaturalBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val padNum = if (selectedNumber < 10) "0$selectedNumber" else "$selectedNumber"
                    Text(
                        text = padNum,
                        fontWeight = FontWeight.Black,
                        fontSize = 78.sp,
                        color = NatureGreen,
                        letterSpacing = (-2).sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // English pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SageGreenLight)
                            .clickable { onSpeak(detail.englishWord, "EN") }
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = detail.englishWord,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalText
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Speak English",
                                tint = NatureGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hindi pronunciation row
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSpeak(detail.hindiWord, "HI") }
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${detail.hindiSymbol}  •  ${detail.hindiWord}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = NatureGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Speak Hindi",
                            tint = NatureGreen.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Gujarati pronunciation row
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSpeak(detail.gujaratiWord, "GU") }
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${detail.gujaratiSymbol}  •  ${detail.gujaratiWord}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Oatmeal
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Speak Gujarati",
                            tint = Oatmeal.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive sensory toy gardener row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Touch Toys to Count!",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NaturalText,
                modifier = Modifier.padding(start = 4.dp)
            )

            // Toggle Button for toy
            Row(
                modifier = Modifier
                    .background(SageGreenLight, RoundedCornerShape(12.dp))
                    .border(1.dp, NaturalBorder, RoundedCornerShape(12.dp))
                    .clickable {
                        toyIndex = (toyIndex + 1) % toys.size
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = toys[toyIndex].first, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = toys[toyIndex].second,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NatureGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Toys Flow Layout Board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp, max = 300.dp)
                .background(Color.White, RoundedCornerShape(18.dp))
                .border(1.dp, NaturalBorder, RoundedCornerShape(18.dp))
                .padding(10.dp)
        ) {
            if (selectedNumber > 50) {
                // High numbers we layout compact
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Count of ${toys[toyIndex].second}:",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalGap = 8.dp,
                            verticalGap = 8.dp
                        ) {
                            for (i in 1..selectedNumber) {
                                ToyItemBubble(
                                    index = i,
                                    emoji = toys[toyIndex].first,
                                    isPopped = poppedSet.contains(i),
                                    onTap = {
                                        poppedSet = poppedSet + i
                                        val bubbleDetail = NumberData.list[i - 1]
                                        val (speakWord, langCode) = when (activeVoiceLanguage) {
                                            VoiceLanguage.ENGLISH -> Pair(bubbleDetail.englishWord, "EN")
                                            VoiceLanguage.HINDI -> Pair(bubbleDetail.hindiWord, "HI")
                                            VoiceLanguage.GUJARATI -> Pair(bubbleDetail.gujaratiWord, "GU")
                                        }
                                        onSpeak(speakWord, langCode)
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Standard numbers we layout perfectly
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalGap = 10.dp,
                        verticalGap = 10.dp
                    ) {
                        for (i in 1..selectedNumber) {
                            ToyItemBubble(
                                index = i,
                                emoji = toys[toyIndex].first,
                                isPopped = poppedSet.contains(i),
                                onTap = {
                                    poppedSet = poppedSet + i
                                    val bubbleDetail = NumberData.list[i - 1]
                                    val (speakWord, langCode) = when (activeVoiceLanguage) {
                                        VoiceLanguage.ENGLISH -> Pair(bubbleDetail.englishWord, "EN")
                                        VoiceLanguage.HINDI -> Pair(bubbleDetail.hindiWord, "HI")
                                        VoiceLanguage.GUJARATI -> Pair(bubbleDetail.gujaratiWord, "GU")
                                    }
                                    onSpeak(speakWord, langCode)
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Pop Reset Helper
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tapped: ${poppedSet.size} of $selectedNumber",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = NatureGreen
            )

            if (poppedSet.isNotEmpty()) {
                Button(
                    onClick = { poppedSet = emptySet() },
                    colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalGap: androidx.compose.ui.unit.Dp = 8.dp,
    verticalGap: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val layoutWidth = constraints.maxWidth
        var currentY = 0
        var currentX = 0
        var rowHeight = 0
        val positionMap = mutableListOf<Pair<androidx.compose.ui.layout.Placeable, Offset>>()

        for (placeable in placeables) {
            if (currentX + placeable.width > layoutWidth) {
                currentX = 0
                currentY += rowHeight + verticalGap.roundToPx()
                rowHeight = 0
            }
            positionMap.add(Pair(placeable, Offset(currentX.toFloat(), currentY.toFloat())))
            currentX += placeable.width + horizontalGap.roundToPx()
            rowHeight = maxOf(rowHeight, placeable.height)
        }

        layout(
            width = layoutWidth,
            height = maxOf(0, currentY + rowHeight)
        ) {
            for ((placeable, offset) in positionMap) {
                placeable.placeRelative(offset.x.toInt(), offset.y.toInt())
            }
        }
    }
}

@Composable
fun ToyItemBubble(
    index: Int,
    emoji: String,
    isPopped: Boolean,
    onTap: () -> Unit
) {
    var isShaking by remember { mutableStateOf(false) }
    val scaleFactor by animateFloatAsState(
        targetValue = if (isPopped) 1.25f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "PopAnim"
    )

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (isPopped) CandyPink.copy(alpha = 0.2f) else SoftYellow().copy(alpha = 0.5f))
            .border(
                1.5.dp,
                if (isPopped) CandyPink else CocoaPrimary.copy(alpha = 0.2f),
                CircleShape
            )
            .clickable {
                isShaking = true
                onTap()
            }
            .scale(scaleFactor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = emoji,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
        // Small bubble indicator for index count
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (0).dp, y = (0).dp)
                .size(13.dp)
                .background(CocoaPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$index",
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun LanguageLabelColumn(
    langName: String,
    symbol: String,
    wordName: String,
    translit: String,
    cardColor: Color,
    onSpeakClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(96.dp)
            .border(1.5.dp, cardColor, RoundedCornerShape(16.dp))
            .clickable { onSpeakClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = langName,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CocoaSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Giant numeral character
            Text(
                text = symbol,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = cardColor.darker(),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Word translation
            Text(
                text = wordName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = CocoaPrimary,
                textAlign = TextAlign.Center
            )

            // Roman Translits representation
            if (translit.isNotEmpty()) {
                Text(
                    text = "\"$translit\"",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Micro Audio Icon
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Speak pronunciation",
                tint = cardColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}


// --- SCREEN 3: TRACING GAME screen ---
@Composable
fun TracingScreen(
    selectedNumber: Int,
    onSpeak: (String, String) -> Unit
) {
    val detail = remember(selectedNumber) { NumberData.list[selectedNumber - 1] }
    var activeTraceLanguage by remember { mutableStateOf(0) } // 0: English, 1: Hindi, 2: Gujarati

    val listCrayonColors = listOf(
        Pair(CandyPink, "Strawberry"),
        Pair(CandyOrange, "Orange Cup"),
        Pair(SunYellow, "Sunshine"),
        Pair(MintGreen, "Melon"),
        Pair(SkyBlue, "Blueberry"),
        Pair(BerryPurple, "Grape")
    )
    var selectedColorIndex by remember { mutableStateOf(0) }

    // Canvas drawing paths
    val paths = remember { mutableStateListOf<Path>() }
    var activePath by remember { mutableStateOf<Path?>(null) }
    val colorsList = remember { mutableStateListOf<Color>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Selection info and controller
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Let's Draw!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = CocoaPrimary
                )
                Text(
                    text = "Practice tracing selected number:",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // Simple language toggle for trace background
            Row(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(3.dp)
            ) {
                listOf("ENG", "HIN", "GUJ").forEachIndexed { index, tag ->
                    val isToggled = activeTraceLanguage == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isToggled) SkyBlue else Color.Transparent)
                            .clickable { 
                                activeTraceLanguage = index 
                                val (speakWord, langCode) = when (index) {
                                    1 -> Pair(detail.hindiWord, "HI")
                                    2 -> Pair(detail.gujaratiWord, "GU")
                                    else -> Pair(detail.englishWord, "EN")
                                }
                                onSpeak(speakWord, langCode)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isToggled) Color.White else CocoaPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val currentDigitToTrace = when (activeTraceLanguage) {
            1 -> detail.hindiSymbol
            2 -> detail.gujaratiSymbol
            else -> detail.englishSymbol
        }

        // Beautiful Writing Slate Card
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E3B33)), // Chalkboard Teal Dark color
            border = BorderStroke(4.dp, SunYellow),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val path = Path().apply { moveTo(offset.x, offset.y) }
                                activePath = path
                                paths.add(path)
                                colorsList.add(listCrayonColors[selectedColorIndex].first)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                activePath?.lineTo(change.position.x, change.position.y)
                            },
                            onDragEnd = {
                                activePath = null
                            }
                        )
                    }
            ) {
                // Giant Faint guide number inside center of canvas
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentDigitToTrace,
                        fontSize = 140.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.08f), // Faint Guide ghost layer
                        textAlign = TextAlign.Center
                    )
                }

                // Small instructional note
                Text(
                    text = "Trace over the faint ghost lines!",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                )

                // Canvas drawing layer
                Canvas(modifier = Modifier.fillMaxSize()) {
                    paths.forEachIndexed { idx, path ->
                        drawPath(
                            path = path,
                            color = colorsList.getOrNull(idx) ?: Color.White,
                            style = Stroke(
                                width = 12.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }

                // Clear slate button
                IconButton(
                    onClick = {
                        paths.clear()
                        colorsList.clear()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(CandyPink, CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear slate drawing",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Rainbow chalk / Crayons selection bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listCrayonColors.forEachIndexed { idx, item ->
                    val isSelected = selectedColorIndex == idx
                    val elevation by animateDpAsState(if (isSelected) 8.dp else (-2).dp, label = "crayon")

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { selectedColorIndex = idx }
                            .padding(horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(if (isSelected) 38.dp else 30.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(item.first)
                                .border(
                                    1.dp,
                                    if (isSelected) CocoaPrimary else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.second,
                            fontSize = 8.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) CocoaPrimary else Color.LightGray
                        )
                    }
                }
            }
        }
    }
}


// --- SCREEN 4: FUN QUIZ screen ---
@Composable
fun QuizScreen(
    onAnswerCorrect: () -> Unit,
    onSpeak: (String, String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Interactive States
    var quizLanguage by remember { mutableStateOf(QuizLanguage.ENGLISH) }
    var currentScore by remember { mutableStateOf(0) }
    var currentQuestionNumber by remember { mutableStateOf(1) }

    // Quiz Question Generator
    var questionType by remember { mutableStateOf(0) } // 0: Match Symbol, 1: Count Emoji flow, 2: Match Spelling
    var numberTargetTarget by remember { mutableStateOf(5) }
    var emojiSymbolToCount by remember { mutableStateOf("🎈") }
    var listOptions = remember { mutableStateListOf<Int>() }

    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var isRightSelected by remember { mutableStateOf<Boolean?>(null) }

    // Confetti celebrates correct answers
    var runCelebrationParticles by remember { mutableStateOf(false) }

    // Generates a fully randomized kids question
    fun generateKidsQuestion() {
        selectedAnswerIndex = null
        isRightSelected = null
        numberTargetTarget = Random.nextInt(1, 40) // Target simple ranges for KG students
        questionType = Random.nextInt(0, 3)

        // Select cute emojis
        val emojis = listOf("🎈", "🍬", "⭐", "🍎", "🧸", "🦋", "🍪", "🍕")
        emojiSymbolToCount = emojis.random()

        // Generate options (1 correct, 3 distractor)
        val options = mutableSetOf<Int>()
        options.add(numberTargetTarget)
        while (options.size < 4) {
            val offset = Random.nextInt(-5, 6)
            val dist = numberTargetTarget + offset
            if (dist in 1..100) {
                options.add(dist)
            }
        }
        listOptions.clear()
        listOptions.addAll(options.shuffled())
    }

    // Trigger on start only
    LaunchedEffect(Unit) {
        generateKidsQuestion()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Header Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, NaturalBorder, RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            QuizLanguage.values().forEach { lang ->
                val isSelected = quizLanguage == lang
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) NatureGreen else Color.Transparent)
                        .clickable {
                            quizLanguage = lang
                            generateKidsQuestion()
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = when (lang) {
                            QuizLanguage.ENGLISH -> "English"
                            QuizLanguage.HINDI -> "हिंदी"
                            QuizLanguage.GUJARATI -> "ગુજરાતી"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else NaturalText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quiz Question Canvas Box
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, NaturalBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Score feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question: $currentQuestionNumber/10",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Row(
                        modifier = Modifier
                            .background(BerryPurple.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, "stars", tint = SunYellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Score: $currentScore",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = BerryPurple
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Render dynamic child question
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    when (questionType) {
                        0 -> { // Match symbol representation
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Which digit is this?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = CocoaPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                val displaySymbol = when (quizLanguage) {
                                    QuizLanguage.HINDI -> NumberData.list[numberTargetTarget - 1].hindiSymbol
                                    QuizLanguage.GUJARATI -> NumberData.list[numberTargetTarget - 1].gujaratiSymbol
                                    else -> NumberData.list[numberTargetTarget - 1].englishSymbol
                                }
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .background(SoftPurple().copy(alpha = 0.5f), CircleShape)
                                        .border(3.dp, BerryPurple, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = displaySymbol,
                                        fontSize = 54.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BerryPurple.darker()
                                    )
                                }
                                // Speak prompt icon
                                IconButton(
                                    onClick = {
                                        val langCode = when (quizLanguage) {
                                            QuizLanguage.HINDI -> "HI"
                                            QuizLanguage.GUJARATI -> "GU"
                                            else -> "EN"
                                        }
                                        val valWord = when (quizLanguage) {
                                            QuizLanguage.HINDI -> NumberData.list[numberTargetTarget - 1].hindiWord
                                            QuizLanguage.GUJARATI -> NumberData.list[numberTargetTarget - 1].gujaratiWord
                                            else -> NumberData.list[numberTargetTarget - 1].englishWord
                                        }
                                        onSpeak(valWord, langCode)
                                    }
                                ) {
                                    Icon(Icons.Default.PlayArrow, "speak", tint = BerryPurple)
                                }
                            }
                        }
                        1 -> { // Count emoji flow
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Can you count the toys?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = CocoaPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                        .background(CreamBackground, RoundedCornerShape(12.dp))
                                        .border(
                                            1.dp,
                                            Color.LightGray.copy(alpha = 0.3f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(8.dp),
                                    horizontalGap = 8.dp,
                                    verticalGap = 8.dp
                                ) {
                                    for (i in 1..numberTargetTarget) {
                                        Text(text = emojiSymbolToCount, fontSize = 26.sp)
                                    }
                                }
                            }
                        }
                        else -> { // Match word name spelling
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val currentWordRepresentation = when (quizLanguage) {
                                    QuizLanguage.HINDI -> NumberData.list[numberTargetTarget - 1].hindiWord
                                    QuizLanguage.GUJARATI -> NumberData.list[numberTargetTarget - 1].gujaratiWord
                                    else -> NumberData.list[numberTargetTarget - 1].englishWord
                                }
                                Text(
                                    text = "Can you find this name on the board?",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CocoaSecondary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color.White, RoundedCornerShape(16.dp))
                                        .border(2.dp, BerryPurple, RoundedCornerShape(16.dp))
                                        .padding(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = currentWordRepresentation,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CocoaPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Options list (4 choices)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOptions.forEachIndexed { idx, optionValue ->
                        val isSelected = selectedAnswerIndex == idx
                        val targetDetail = NumberData.list[optionValue - 1]

                        // Option display string based on screen preference
                        val optionText = when (quizLanguage) {
                            QuizLanguage.HINDI -> "${targetDetail.englishSymbol} - (${targetDetail.hindiSymbol})"
                            QuizLanguage.GUJARATI -> "${targetDetail.englishSymbol} - (${targetDetail.gujaratiSymbol})"
                            else -> targetDetail.englishSymbol
                        }

                        val btnBg = when {
                            isSelected && isRightSelected == true -> SageGreen
                            isSelected && isRightSelected == false -> CandyPink
                            else -> SageGreenLight
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(btnBg)
                                .border(
                                    1.5.dp,
                                    if (isSelected) {
                                        if (isRightSelected == true) NatureGreen else Terracotta
                                    } else NaturalBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable(enabled = selectedAnswerIndex == null) {
                                    selectedAnswerIndex = idx
                                    if (optionValue == numberTargetTarget) {
                                        isRightSelected = true
                                        currentScore += 10
                                        onAnswerCorrect()
                                        runCelebrationParticles = true
                                        onSpeak("YAY! Correct!", "EN")
                                        coroutineScope.launch {
                                            delay(2500)
                                            runCelebrationParticles = false
                                        }
                                    } else {
                                        isRightSelected = false
                                        onSpeak("Try again, little star!", "EN")
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("quiz_choice_$idx")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = optionText,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = if (isSelected) {
                                        if (isRightSelected == true) NatureGreen else Color.White
                                    } else NaturalText
                                )

                                if (isSelected) {
                                    Icon(
                                        imageVector = if (isRightSelected == true) Icons.Default.CheckCircle else Icons.Default.Close,
                                        contentDescription = "Answer result check",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Next Button if answer is completed
                if (selectedAnswerIndex != null) {
                    Button(
                        onClick = {
                            if (currentQuestionNumber >= 10) {
                                currentQuestionNumber = 1
                                currentScore = 0
                            } else {
                                currentQuestionNumber += 1
                            }
                            generateKidsQuestion()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BerryPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("quiz_next_button")
                    ) {
                        Text(
                            text = if (currentQuestionNumber >= 10) "Restart Game 🎮" else "Next Question ➡️",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Fun overlay celebrating correct answer with sparkles
        if (runCelebrationParticles) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🎉 ⭐ 🎈 ⭐ 🎉", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "AMAZING JOB!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = PrimaryKid
                    )
                }
            }
        }
    }
}


// --- EXTENSION HELPERS FOR COLORS ---
fun Color.darker(factor: Float = 0.7f): Color {
    return Color(
        red = this.red * factor,
        green = this.green * factor,
        blue = this.blue * factor,
        alpha = this.alpha
    )
}

fun Color.Companion.FaintYellow() = Color(0xFFFCF9EE)
fun Color.Companion.FaintPurple() = Color(0xFFFAF7FF)
fun SoftYellow() = Color(0xFFFEFDF0)
fun SoftPurple() = Color(0xFFFDF9FF)
