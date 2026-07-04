package com.example.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageListsScreen(
    viewModel: LawyerViewModel,
    isCourtsMode: Boolean, // true for Courts, false for CaseTypes
    onBack: () -> Unit
) {
    val allCourts by viewModel.allCourts.collectAsState()
    val allCaseTypes by viewModel.allCaseTypes.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }

    val listItems = if (isCourtsMode) allCourts.map { it.id to it.name } else allCaseTypes.map { it.id to it.name }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isCourtsMode) "إدارة المحاكم والمجالس القضائية" else "إدارة أنواع وتصنيفات القضايا",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RoyalBlue)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = RoyalBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة جديد")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = if (isCourtsMode) {
                    "قائمة المحاكم المعتمدة في المفكرة (${listItems.size}):"
                } else {
                    "قائمة أنواع القضايا المعتمدة (${listItems.size}):"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (listItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("القائمة فارغة، اضغط + لإضافة عنصر جديد", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listItems, key = { it.first }) { (id, name) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isCourtsMode) Icons.Default.AccountBalance else Icons.Default.Work,
                                        contentDescription = null,
                                        tint = RoyalBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (isCourtsMode) {
                                            allCourts.find { it.id == id }?.let { viewModel.deleteCourt(it) }
                                        } else {
                                            allCaseTypes.find { it.id == id }?.let { viewModel.deleteCaseType(it) }
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(if (isCourtsMode) "إضافة محكمة أو جهة قضائية" else "إضافة نوع قضية جديد")
            },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("الاسم") },
                    placeholder = { Text(if (isCourtsMode) "مثال: محكمة القضاء الإداري" else "مثال: جنايات عسكرية") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            if (isCourtsMode) viewModel.addCourt(inputName) else viewModel.addCaseType(inputName)
                            inputName = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) { Text("إضافة وحفظ") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("إلغاء") }
            }
        )
    }
}
