package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CaseEntity
import com.example.data.StepEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseListScreen(
    viewModel: LawyerViewModel,
    initialDisposedTab: Boolean,
    onBack: () -> Unit,
    onCaseClicked: (Int) -> Unit,
    onAddCaseClicked: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(if (initialDisposedTab) 1 else 0) } // 0: Active, 1: Disposed, 2: All
    var searchText by remember { mutableStateOf("") }

    val allCases by viewModel.allCases.collectAsState()
    val activeCases by viewModel.activeCases.collectAsState()
    val disposedCases by viewModel.disposedCases.collectAsState()
    val allSteps by viewModel.allSteps.collectAsState()

    val filteredCases = remember(selectedTab, searchText, allCases, activeCases, disposedCases) {
        val list = when (selectedTab) {
            0 -> activeCases
            1 -> disposedCases
            else -> allCases
        }
        if (searchText.isBlank()) {
            list
        } else {
            list.filter {
                it.title.contains(searchText, ignoreCase = true) ||
                it.caseNumber.contains(searchText, ignoreCase = true) ||
                it.courtName.contains(searchText, ignoreCase = true) ||
                it.partyName.contains(searchText, ignoreCase = true) ||
                it.adversePartyName.contains(searchText, ignoreCase = true) ||
                it.caseType.contains(searchText, ignoreCase = true)
            }
        }
    }

    val stepsByCaseId = remember(allSteps) {
        allSteps.groupBy { it.caseId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "قائمة القضايا النشطة"
                            1 -> "أرشيف القضايا المفصولة"
                            else -> "جميع القضايا المسجلة"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward, // RTL back arrow
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddCaseClicked) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة قضية",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RoyalBlue
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCaseClicked,
                containerColor = RoyalBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة قضية")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top Search Bar exactly like Screenshot 2
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RoyalBlue)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("بحث عن قضية، رقم، محكمة، موكل...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = RoyalBlue)
                    },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح")
                            }
                        } else {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = RoyalBlue)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Tabs: Active, Disposed, All
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = RoyalBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("نشطة (${activeCases.size})", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("مفصولة (${disposedCases.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("الكل (${allCases.size})", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            if (filteredCases.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لا توجد قضايا مطابقة للبحث",
                            style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onAddCaseClicked,
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إضافة قضية جديدة")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredCases, key = { it.id }) { case ->
                        val steps = stepsByCaseId[case.id] ?: emptyList()
                        val lastStep = steps.maxByOrNull { it.id }
                        CaseItemCard(
                            case = case,
                            stepsCount = steps.size,
                            latestStep = lastStep,
                            onClick = { onCaseClicked(case.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CaseItemCard(
    case: CaseEntity,
    stepsCount: Int,
    latestStep: StepEntity?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and arrow exactly like Screenshot 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = case.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ChevronLeft, // RTL arrow pointing left
                    contentDescription = "التفاصيل",
                    tint = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Court, Case Type, Case#
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Court (المحكمة)", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Text(case.courtName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Case Type (النوع)", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Text(case.caseType, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Case# (رقم الملف)", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Text("${case.caseNumber}/${case.caseYear}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = RoyalBlue))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Party Name + Contact
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${case.partyName} ${if (case.partyContact.isNotBlank()) "(${case.partyContact})" else ""}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // On Behalf Of
            Text(
                text = "On Behalf Of (بصفة): ${case.onBehalfOf}",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            if (case.adversePartyName.isNotBlank()) {
                Text(
                    text = "Adverse Party (الخصم): ${case.adversePartyName}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dates and Steps badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateBackground, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Previous Date: ${latestStep?.previousDate ?: "-"}", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Text("Adjourn Date (الجلسة القادمة): ${latestStep?.adjournDate ?: "N/A"}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = if (latestStep?.adjournDate != null) GoldAccent else TextSecondary))
                }

                Surface(
                    color = RoyalBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Steps: $stepsCount",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = RoyalBlue,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
