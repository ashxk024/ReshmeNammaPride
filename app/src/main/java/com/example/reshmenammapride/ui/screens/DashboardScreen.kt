package com.example.reshmenammapride.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reshmenammapride.model.AiAdviceState
import com.example.reshmenammapride.model.ClimateEntry
import com.example.reshmenammapride.model.ClimateStatus
import com.example.reshmenammapride.model.DashboardUiState
import com.example.reshmenammapride.model.InstarStage
import com.example.reshmenammapride.viewmodel.DashboardViewModel
import java.time.format.DateTimeFormatter
import androidx.compose.animation.ExperimentalAnimationApi

// ── Colours ───────────────────────────────────────────────────────────────────
private val GreenGood   = Color(0xFF43A047)
private val YellowMod   = Color(0xFFFFA000)
private val RedBad      = Color(0xFFE53935)
private val SectionGrey = Color(0xFF757575)

private fun ClimateStatus.color() = when (this) {
    ClimateStatus.GOOD     -> GreenGood
    ClimateStatus.MODERATE -> YellowMod
    ClimateStatus.BAD      -> RedBad
}
private fun ClimateStatus.label() = when (this) {
    ClimateStatus.GOOD     -> "GOOD"
    ClimateStatus.MODERATE -> "MODERATE"
    ClimateStatus.BAD      -> "BAD"
}
private fun InstarStage.label() = when (this) {
    InstarStage.EARLY  -> "Early Stage"
    InstarStage.MID    -> "Mid Growth"
    InstarStage.MATURE -> "Mature Stage"
}

// ── Screen root ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    batchId: Int,
    batchName: String,
    breedType: String,
    createdDateEpochDay: Long,
    onBack: () -> Unit,
    viewModelFactory: DashboardViewModel.Factory
) {
    val viewModel: DashboardViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(batchId) {
        viewModel.initBatch(batchId, batchName, breedType, createdDateEpochDay)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.batchName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = GreenGood,
                    titleContentColor          = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF2F7F2)
    ) { padding ->

        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenGood)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding      = PaddingValues(vertical = 16.dp)
        ) {
            item { BatchHeaderCard(state) }
            item { ClimateSection(state, viewModel) }
            item { InstarAgeCard(state) }
            item { HarvestTimelineCard(state) }

            if (state.climateEntries.isNotEmpty()) {
                item {
                    Text(
                        text     = "Climate History",
                        style    = MaterialTheme.typography.titleSmall,
                        color    = SectionGrey,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
                items(state.climateEntries, key = { it.id }) { entry ->
                    ClimateEntryCard(entry)
                }
            }

            item { AiAdviceCard(state.aiAdviceState) }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (state.showClimateDialog) {
        ClimateInputDialog(
            state            = state,
            onTempChange     = viewModel::onDialogTempChange,
            onHumidityChange = viewModel::onDialogHumidityChange,
            onSave           = viewModel::saveClimateEntry,
            onDismiss        = viewModel::closeClimateDialog
        )
    }
}

// ── Batch header ──────────────────────────────────────────────────────────────
@Composable
private fun BatchHeaderCard(state: DashboardUiState) {
    val fmt = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    DashCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(state.batchName, style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                Text("Breed: ${state.breedType}", style = MaterialTheme.typography.bodyMedium, color = SectionGrey)
                Text("Started: ${state.createdDate.format(fmt)}", style = MaterialTheme.typography.bodySmall, color = SectionGrey)
            }
            Surface(shape = RoundedCornerShape(12.dp), color = GreenGood.copy(alpha = 0.12f)) {
                Text("Day ${state.daysElapsed}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = GreenGood, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Climate section ───────────────────────────────────────────────────────────
@Composable
private fun ClimateSection(state: DashboardUiState, viewModel: DashboardViewModel) {
    DashCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Climate Status", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold, color = Color(0xFF1B5E20))
            Spacer(Modifier.height(16.dp))
            ClimateDial(status = state.climateStatus)
            Spacer(Modifier.height(8.dp))

            state.climateEntries.firstOrNull()?.let { latest ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ReadingChip("Temp",     "${latest.temperature}°C")
                    ReadingChip("Humidity", "${latest.humidity}%")
                }
                Spacer(Modifier.height(12.dp))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = viewModel::openClimateDialog, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Input Temp & Humidity", fontSize = 12.sp)
                }

                Button(
                    onClick = viewModel::analyzeWithAI,
                    enabled = state.aiAdviceState !is AiAdviceState.Loading,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenGood
                    )
                ) {
                    Text(
                        text = if (state.aiAdviceState is AiAdviceState.Loading)
                            "Analyzing..."
                        else
                            "Analyze with AI"
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadingChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = SectionGrey)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

// ── Climate Dial ──────────────────────────────────────────────────────────────
@Composable
private fun ClimateDial(status: ClimateStatus) {
    val dialColor = status.color()
    val animSweep by animateFloatAsState(
        targetValue = when (status) {
            ClimateStatus.GOOD -> 240f; ClimateStatus.MODERATE -> 160f; ClimateStatus.BAD -> 80f
        },
        animationSpec = tween(800), label = "dial"
    )
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
        Canvas(Modifier.size(160.dp)) {
            val sw  = 18.dp.toPx(); val ins = sw / 2f
            val arc = Size(size.width - sw, size.height - sw); val tl = Offset(ins, ins)
            drawArc(Color(0xFFE0E0E0), 150f, 240f, false, tl, arc, style = Stroke(sw, cap = StrokeCap.Round))
            drawArc(dialColor, 150f, animSweep, false, tl, arc, style = Stroke(sw, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(status.label(), style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold, color = dialColor)
            Text("Climate", style = MaterialTheme.typography.labelSmall, color = SectionGrey)
        }
    }
}

// ── Climate input dialog ──────────────────────────────────────────────────────
@Composable
private fun ClimateInputDialog(
    state: DashboardUiState,
    onTempChange: (String) -> Unit,
    onHumidityChange: (String) -> Unit,
    onSave: () -> String?,
    onDismiss: () -> Unit
) {
    var errorMsg by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Climate Reading") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(state.dialogTemp,
                    onValueChange = { onTempChange(it); errorMsg = null },
                    label = { Text("Temperature (°C)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.dialogHumidity,
                    onValueChange = { onHumidityChange(it); errorMsg = null },
                    label = { Text("Humidity (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                errorMsg?.let { Text(it, color = RedBad, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(onClick = { errorMsg = onSave() },
                colors = ButtonDefaults.buttonColors(containerColor = GreenGood)) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ── Instar Age ────────────────────────────────────────────────────────────────
@Composable
private fun InstarAgeCard(state: DashboardUiState) {
    DashCard {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Instar Growth Stage", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, color = Color(0xFF1B5E20))
                Text(state.instarStage.label(), style = MaterialTheme.typography.labelMedium,
                    color = GreenGood, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            val animProg by animateFloatAsState(state.instarProgress, tween(800), label = "instar")
            LinearProgressIndicator(progress = { animProg },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(8.dp)),
                color = GreenGood, trackColor = Color(0xFFDCEDDC))
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Early\n(0–10d)", "Mid\n(11–20d)", "Mature\n(21d+)").forEach {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = SectionGrey,
                        textAlign = TextAlign.Center, lineHeight = 14.sp)
                }
            }
        }
    }
}

// ── Harvest Timeline ──────────────────────────────────────────────────────────
@Composable
private fun HarvestTimelineCard(state: DashboardUiState) {
    DashCard {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Harvest Timeline", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, color = Color(0xFF1B5E20))
                Text("~${state.estimatedHarvestMonth}", style = MaterialTheme.typography.labelMedium,
                    color = YellowMod, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            val animProg by animateFloatAsState(state.harvestProgress, tween(800), label = "harvest")
            LinearProgressIndicator(progress = { animProg },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(8.dp)),
                color = YellowMod, trackColor = Color(0xFFFFF9E6))
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Started", style = MaterialTheme.typography.labelSmall, color = SectionGrey)
                Text("${(state.harvestProgress * 100).toInt()}% ready",
                    style = MaterialTheme.typography.labelSmall, color = SectionGrey)
                Text("Harvest", style = MaterialTheme.typography.labelSmall, color = SectionGrey)
            }
        }
    }
}

// ── Climate Entry row ─────────────────────────────────────────────────────────
private val dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val timeFmt = DateTimeFormatter.ofPattern("hh:mm a")

@Composable
private fun ClimateEntryCard(entry: ClimateEntry) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        color = Color.White, shadowElevation = 1.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically) {
            EntryChip("🌡️ Temp",  "${entry.temperature}°C")
            EntryChip("💧 Humid", "${entry.humidity}%")
            Column(horizontalAlignment = Alignment.End) {
                Text(entry.recordedAt.format(dateFmt),
                    style = MaterialTheme.typography.labelSmall, color = SectionGrey)
                Text(entry.recordedAt.format(timeFmt),
                    style = MaterialTheme.typography.labelSmall, color = SectionGrey)
            }
        }
    }
}

@Composable
private fun EntryChip(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = SectionGrey)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

// ── AI Advice card — handles all AiAdviceState variants ───────────────────────
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AiAdviceCard(adviceState: AiAdviceState) {
    Surface(
        Modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(16.dp),
        color  = Color(0xFFE8F5E9),
        border = androidx.compose.foundation.BorderStroke(1.dp, GreenGood.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🤖", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text("AI Farm Advisor", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
            }
            Spacer(Modifier.height(12.dp))

            AnimatedContent(
                targetState = adviceState,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "ai_advice"
            ) { state ->
                when (state) {
                    is AiAdviceState.Idle -> {
                        Text(
                            "Tap \"Analyze with AI\" after logging at least 3 climate readings " +
                                    "to receive personalized farming advice.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2E7D32), lineHeight = 22.sp
                        )
                    }

                    is AiAdviceState.Loading -> {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp),
                                color = GreenGood, strokeWidth = 2.dp)
                            Text("Analyzing your climate data…",
                                style = MaterialTheme.typography.bodyMedium, color = SectionGrey)
                        }
                    }

                    is AiAdviceState.Success -> {
                        Text(state.text, style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1B5E20), lineHeight = 22.sp)
                    }

                    is AiAdviceState.Error -> {
                        Column {
                            Text("⚠ Analysis failed", fontWeight = FontWeight.Bold,
                                color = RedBad, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(state.message, style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF757575), lineHeight = 20.sp)
                        }
                    }

                    is AiAdviceState.ApiKeyMissing -> {
                        Column {
                            Text("🔑 API Key Not Set", fontWeight = FontWeight.Bold,
                                color = YellowMod, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Add your Gemini API key to local.properties:\n" +
                                        "GEMINI_API_KEY=AIza...\n\n" +
                                        "Get a free key at: aistudio.google.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF757575), lineHeight = 20.sp
                            )
                        }
                    }

                    is AiAdviceState.InsufficientData -> {
                        Text(
                            "📊 Not enough data yet. Please log at least 3 climate readings " +
                                    "before running an AI analysis.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF757575), lineHeight = 22.sp
                        )
                    }

                    is AiAdviceState.RateLimited -> {
                        Column {
                            Text("⏱ Too many requests", fontWeight = FontWeight.Bold,
                                color = YellowMod, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "The API rate limit was reached. Please wait about " +
                                        "${state.waitSeconds} seconds before trying again.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF757575), lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color = Color.White, shadowElevation = 2.dp) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

