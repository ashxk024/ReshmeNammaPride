package com.example.reshmenammapride.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.reshmenammapride.viewmodel.BatchViewModel
import com.example.reshmenammapride.viewmodel.SavedBatch

private val GreenPrimary = Color(0xFF43A047)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBatchScreen(
    viewModel: BatchViewModel,
    onBack: () -> Unit,
    onBatchSaved: (saved: SavedBatch) -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val savedBatch by viewModel.savedBatch.collectAsState()
    val context    = LocalContext.current

    // React to successful save: navigate to Dashboard
    LaunchedEffect(savedBatch) {
        savedBatch?.let { saved ->
            onBatchSaved(saved)
            viewModel.onSaveConsumed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Batch") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Color(0xFFC8E6C9),
                    titleContentColor = GreenPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value         = formState.batchName,
                onValueChange = viewModel::onBatchNameChange,
                label         = { Text("Batch Name") },
                placeholder   = { Text("e.g. Spring Batch 2025") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value         = formState.breedType,
                onValueChange = viewModel::onBreedTypeChange,
                label         = { Text("Breed Type") },
                placeholder   = { Text("e.g. PM×CSR2, Nistari") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val ok = viewModel.saveBatch()
                    if (!ok) {
                        Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                    }
                    // On success, LaunchedEffect above handles navigation
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Save Batch")
            }
        }
    }
}
