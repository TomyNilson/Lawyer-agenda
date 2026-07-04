package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.example.data.CaseWithDetails
import com.example.data.DocumentEntity
import com.example.data.StepEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseDetailsScreen(
    viewModel: LawyerViewModel,
    caseId: Int,
    onBack: () -> Unit,
    onEditCase: (Int) -> Unit
) {
    val context = LocalContext.current
    val details by viewModel.selectedCaseDetails.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: CASE, 1: STEPS, 2: NOTES/DOCS
    var showAddStepDialog by remember { mutableStateOf(false) }
    var stepToEdit by remember { mutableStateOf<StepEntity?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Dialog for full-screen document image view
    var selectedDocForView by remember { mutableStateOf<DocumentEntity?>(null) }
    var showAddDocDialog by remember { mutableStateOf(false) }
    var newDocTitle by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<String?>("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // Grant read permission for the uri
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            selectedImageUri = uri.toString()
        }
    }

    LaunchedEffect(caseId) {
        viewModel.selectCase(caseId)
    }

    val caseData = details?.case
    val stepsList = details?.steps ?: emptyList()
    val docsList = details?.documents ?: emptyList()

    if (caseData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RoyalBlue)
        }
        return
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(RoyalBlue)) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Case Details (ملف القضية)",
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
                        IconButton(onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "قضية: ${caseData.title} (${caseData.caseNumber}/${caseData.caseYear}) - المحكمة: ${caseData.courtName} - الموكل: ${caseData.partyName}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة بيانات القضية"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = Color.White)
                        }
                        IconButton(onClick = { onEditCase(caseData.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color.White)
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف القضية", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                // Tabs exactly like Screenshot 4 & 5: CASE, STEPS, NOTES
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = RoyalBlue,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color.White,
                            height = 4.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("CASE (البيانات)", fontWeight = FontWeight.Bold, color = Color.White) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("STEPS (${stepsList.size})", fontWeight = FontWeight.Bold, color = Color.White) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("NOTES & DOCS (${docsList.size})", fontWeight = FontWeight.Bold, color = Color.White) }
                    )
                }
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> CaseDataTab(
                    caseData = caseData,
                    onDisposeToggle = {
                        viewModel.setCaseDisposed(caseData.id, !caseData.isDisposed)
                    }
                )
                1 -> StepsTab(
                    steps = stepsList,
                    onAddStep = {
                        stepToEdit = null
                        showAddStepDialog = true
                    },
                    onEditStep = { step ->
                        stepToEdit = step
                        showAddStepDialog = true
                    },
                    onDeleteStep = { step ->
                        viewModel.deleteStep(step)
                    }
                )
                2 -> NotesAndDocsTab(
                    documents = docsList,
                    onUploadClicked = {
                        selectedImageUri = ""
                        newDocTitle = "مستند جلسة / وصل"
                        showAddDocDialog = true
                    },
                    onDocClicked = { doc ->
                        selectedDocForView = doc
                    },
                    onDeleteDoc = { doc ->
                        viewModel.deleteDocument(doc)
                    }
                )
            }
        }
    }

    // Dialog: Add/Edit Step
    if (showAddStepDialog) {
        AddOrEditStepDialog(
            caseId = caseData.id,
            existingStep = stepToEdit,
            onDismiss = { showAddStepDialog = false },
            onSave = { step ->
                if (stepToEdit != null) {
                    // update via repo/vm
                    viewModel.addStep(step.copy(id = stepToEdit!!.id))
                } else {
                    viewModel.addStep(step)
                }
                showAddStepDialog = false
            }
        )
    }

    // Dialog: Confirm Delete Case
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("حذف القضية نهائياً؟") },
            text = { Text("هل أنت متأكد من رغبتك في حذف ملف '${caseData.title}' وجميع جلساته ومستنداته؟ لا يمكن التراجع عن هذا الإجراء.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCase(caseData)
                        showDeleteConfirmDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("حذف نهائي") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Dialog: Upload / Attach Document
    if (showAddDocDialog) {
        AlertDialog(
            onDismissRequest = { showAddDocDialog = false },
            title = { Text("إرفاق مستند أو صورة للقضية") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newDocTitle,
                        onValueChange = { newDocTitle = it },
                        label = { Text("عنوان أو وصف المستند") },
                        placeholder = { Text("مثال: صورة من عريضة الدعوى أو الإشعار") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!selectedImageUri.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = selectedImageUri),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("اختر صورة المستند من الجهاز")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!selectedImageUri.isNullOrEmpty() && newDocTitle.isNotBlank()) {
                            viewModel.addDocument(
                                DocumentEntity(
                                    caseId = caseData.id,
                                    title = newDocTitle.trim(),
                                    imageUriOrBase64 = selectedImageUri!!,
                                    fileType = "image"
                                )
                            )
                            showAddDocDialog = false
                        }
                    },
                    enabled = !selectedImageUri.isNullOrEmpty()
                ) { Text("حفظ المستند") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDocDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Dialog: Full screen image viewer
    if (selectedDocForView != null) {
        Dialog(
            onDismissRequest = { selectedDocForView = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .clickable { selectedDocForView = null },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedDocForView!!.title,
                            style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { selectedDocForView = null }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = selectedDocForView!!.imageUriOrBase64),
                            contentDescription = selectedDocForView!!.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "اضغط في أي مكان للإغلاق",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                    )
                }
            }
        }
    }
}

@Composable
fun CaseDataTab(caseData: com.example.data.CaseEntity, onDisposeToggle: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Card exactly like Screenshot 5
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    // Light blue banner for Case title exactly like Screenshot 5
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFDCE6F1))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = caseData.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlueDark
                            )
                        )
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        InfoRow("Court (المحكمة):", caseData.courtName)
                        InfoRow("Case No./Year:", "${caseData.caseNumber}/${caseData.caseYear}")
                        InfoRow("Type (النوع):", caseData.caseType)
                        InfoRow("On Behalf Of:", caseData.onBehalfOf)
                        if (caseData.filedUnderSection.isNotBlank()) {
                            InfoRow("Filed U/sec:", caseData.filedUnderSection)
                        }
                        if (caseData.respondentName.isNotBlank()) {
                            InfoRow("Judge/Room:", caseData.respondentName)
                        }
                    }

                    // Light blue banner: Party
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFDCE6F1))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Party (الموكل)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlueDark
                            )
                        )
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InfoRow("Name:", caseData.partyName.ifEmpty { "-" })
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InfoRow("Contact:", caseData.partyContact.ifEmpty { "-" })
                            if (caseData.partyContact.isNotBlank()) {
                                Spacer(modifier = Modifier.width(12.dp))
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${caseData.partyContact}"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.size(32.dp).background(AccentBlue.copy(alpha = 0.15f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "اتصال", tint = AccentBlue, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    // Light blue banner: Adverse Party
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFDCE6F1))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Adverse Party (الخصم)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlueDark
                            )
                        )
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InfoRow("Name:", caseData.adversePartyName.ifEmpty { "-" })
                        InfoRow("Contact:", caseData.adversePartyContact.ifEmpty { "-" })
                        if (caseData.adverseAdvocateName.isNotBlank()) {
                            InfoRow("Advocate:", "${caseData.adverseAdvocateName} ${caseData.adverseAdvocateContact}")
                        }
                    }
                }
            }
        }

        // Dispose Button exactly like Screenshot 5
        Button(
            onClick = onDisposeToggle,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (caseData.isDisposed) GoldAccent else RoyalBlue
            )
        ) {
            Icon(if (caseData.isDisposed) Icons.Default.Replay else Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (caseData.isDisposed) "إعادة تنشيط القضية (Reopen Case)" else "فصل أو غلق القضية (Dispose Case)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            ),
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StepsTab(
    steps: List<StepEntity>,
    onAddStep: () -> Unit,
    onEditStep: (StepEntity) -> Unit,
    onDeleteStep: (StepEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Saved Case Steps (جلسات وإجراءات الدعوى):",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (steps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد جلسات أو خطوات مسجلة بعد", color = TextSecondary)
                }
            } else {
                // Table header exactly like Screenshot 4
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoyalBlue, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Previous Dt. (تاريخ سابق)", style = MaterialTheme.typography.labelLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    Text("Adjourn Dt. (الجلسة القادمة)", style = MaterialTheme.typography.labelLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                ) {
                    items(steps) { step ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = step.previousDate.ifEmpty { "-" },
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (step.isReminder) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = step.adjournDate.ifEmpty { "-" },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Step (الغرض): ${step.purpose}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = RoyalBlue, fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.weight(1f)
                                )

                                Row {
                                    IconButton(onClick = { onEditStep(step) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = AccentBlue, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { onDeleteStep(step) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            if (step.notes.isNotBlank()) {
                                Text(
                                    text = "ملاحظة: ${step.notes}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add Step Button exactly like Screenshot 4
        Button(
            onClick = onAddStep,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Step (إضافة جلسة أو إجراء جديد)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun NotesAndDocsTab(
    documents: List<DocumentEntity>,
    onUploadClicked: () -> Unit,
    onDocClicked: (DocumentEntity) -> Unit,
    onDeleteDoc: (DocumentEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "المستندات ومراجع الصور (${documents.size}):",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Button(
                onClick = onUploadClicked,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("إرفاق مستند")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (documents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ImageNotSupported,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("لم يتم إرفاق أي صور أو مستندات لهذه القضية بعد", color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("يمكنك إرفاق صور وثائق الجلسات، الوصولات، والشهادات كمرجع سريع", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(documents) { doc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable { onDocClicked(doc) },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = rememberAsyncImagePainter(model = doc.imageUriOrBase64),
                                contentDescription = doc.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Title overlay at bottom
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = doc.title,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { onDeleteDoc(doc) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFFF6B6B), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddOrEditStepDialog(
    caseId: Int,
    existingStep: StepEntity?,
    onDismiss: () -> Unit,
    onSave: (StepEntity) -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    var previousDate by remember { mutableStateOf(existingStep?.previousDate ?: sdf.format(Date())) }
    var adjournDate by remember { mutableStateOf(existingStep?.adjournDate ?: "") }
    var purpose by remember { mutableStateOf(existingStep?.purpose ?: "") }
    var notes by remember { mutableStateOf(existingStep?.notes ?: "") }
    var isReminder by remember { mutableStateOf(existingStep?.isReminder ?: true) }

    val presetPurposes = listOf(
        "للحضور والمرافعة",
        "تقديم مذكرة جوابية",
        "سماع أقوال الشهود",
        "تعيين خبير وعرض التقرير",
        "النطق بالحكم القضائي",
        "تسديد الكفالة أو الرسوم",
        "تبليغ الأطراف"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existingStep != null) "تعديل بيانات الجلسة" else "إضافة جلسة أو إجراء قضائي")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = previousDate,
                    onValueChange = { previousDate = it },
                    label = { Text("Previous Dt. (تاريخ الجلسة السابقة)") },
                    placeholder = { Text("YYYY-MM-DD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = adjournDate,
                    onValueChange = { adjournDate = it },
                    label = { Text("Adjourn Dt. * (تاريخ الجلسة القادمة / التأجيل)") },
                    placeholder = { Text("YYYY-MM-DD (مثال: 2026-07-20)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("اختر أو اكتب الغرض من الجلسة:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                // Chip selections for fast input
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    presetPurposes.take(2).forEach { p ->
                        FilterChip(
                            selected = purpose == p,
                            onClick = { purpose = p },
                            label = { Text(p, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Step Purpose * (الغرض / الإجراء المطلوب)") },
                    placeholder = { Text("مثال: للحضور وتقديم مستندات الدفاع") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات تفصيلية (اختياري)") },
                    placeholder = { Text("أي تفاصيل إضافية أو أسماء شهود...") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateBackground, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تفعيل التنبيه بموعد هذه الجلسة", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = isReminder,
                        onCheckedChange = { isReminder = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = GoldLight)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (adjournDate.isNotBlank() || purpose.isNotBlank()) {
                        val step = StepEntity(
                            id = existingStep?.id ?: 0,
                            caseId = caseId,
                            previousDate = previousDate.trim(),
                            adjournDate = adjournDate.trim(),
                            purpose = purpose.trim().ifEmpty { "للحضور" },
                            isReminder = isReminder,
                            notes = notes.trim()
                        )
                        onSave(step)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) { Text("حفظ الجلسة") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
