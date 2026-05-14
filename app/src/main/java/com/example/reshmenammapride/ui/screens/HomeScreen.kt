package com.example.reshmenammapride.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reshmenammapride.database.entity.BatchEntity
import com.example.reshmenammapride.viewmodel.BatchViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val GreenPrimary = Color(0xFF43A047)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BatchViewModel,
    onCreateBatch: () -> Unit,
    onOpenDashboard: (batchId: Int, batchName: String, breedType: String, epochDay: Long) -> Unit
) {
    val batches by viewModel.allBatches.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Reshme-Namma Pride", fontWeight = FontWeight.Bold)
                        Text("Silkworm Batch Manager", style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = GreenPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick           = onCreateBatch,
                containerColor    = GreenPrimary,
                contentColor      = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create New Batch")
            }
        },
        containerColor = Color(0xFFF2F7F2)
    ) { padding ->
        if (batches.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier        = Modifier.fillMaxSize().padding(padding),
                contentPadding  = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text  = "${batches.size} batch${if (batches.size != 1) "es" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(batches, key = { it.id }) { batch ->
                    BatchCard(
                        batch    = batch,
                        onClick  = {
                            onOpenDashboard(
                                batch.id,
                                batch.batchName,
                                batch.breedType,
                                batch.createdEpochDay
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BatchCard(batch: BatchEntity, onClick: () -> Unit) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val createdDate   = LocalDate.ofEpochDay(batch.createdEpochDay)
    val daysElapsed   = createdDate.until(LocalDate.now()).days.coerceAtLeast(0)

    Surface(
        modifier      = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape         = RoundedCornerShape(16.dp),
        color         = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🐛", fontSize = 32.sp, modifier = Modifier.padding(end = 14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = batch.batchName,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF1B5E20)
                )
                Text(
                    text  = "Breed: ${batch.breedType}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
                Text(
                    text  = "Started: ${createdDate.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = GreenPrimary.copy(alpha = 0.12f)
            ) {
                Text(
                    text     = "Day $daysElapsed",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style    = MaterialTheme.typography.labelMedium,
                    color    = GreenPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier             = modifier,
        verticalArrangement  = Arrangement.Center,
        horizontalAlignment  = Alignment.CenterHorizontally
    ) {
        Text("🐛", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text       = "No batches yet",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF1B5E20)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text  = "Tap + to create your first batch",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF757575)
        )
    }
}
