package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CaseEntity
import com.example.data.StepEntity
import com.example.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCaseScreen(
    viewModel: LawyerViewModel,
    caseIdToEdit: Int? = null,
    onBack: () -> Unit,
    onSaved: (Int) -> Unit
) {
    val allCourts by viewModel.allCourts.collectAsState()
    val allCaseTypes by viewModel.allCaseTypes.collectAsState()
    val allCases by viewModel.allCases.collectAsState()

    val existingCase = remember(caseIdToEdit, allCases) {
        if (caseIdToEdit != null) allCases.find { it.id == caseIdToEdit } else null
    }

    // Standard preset case titles
    val presetTitles = listOf(
        "نصب واحتيال",
        "سرقة موصوفة",
        "عدم تسديد نفقة",
        "نزاع ملكية عقارية",
        "شيك بدون رصيد",
        "طلاق بالتراضي",
        "طلاق للضرر",
        "خيانة أمانة",
        "مشاجرة وضرب",
        "فصل تعسفي من العمل",
        "إخلاء مسكن"
    )

    val behalfOfOptions = listOf(
        "شاكي / مدعي",
        "مدعى عليه",
        "متهم",
        "طرف مدني",
        "مستأنف",
        "مستأنف ضده",
        "شاهد"
    )

    var caseTitle by remember { mutableStateOf(existingCase?.title ?: "") }
    var courtName by remember { mutableStateOf(existingCase?.courtName ?: "") }
    var caseType by remember { mutableStateOf(existingCase?.caseType ?: "") }
    var caseNumber by remember { mutableStateOf(existingCase?.caseNumber ?: "") }
    var caseYear by remember { mutableStateOf(existingCase?.caseYear ?: "${Calendar.getInstance().get(Calendar.YEAR)}") }
    var onBehalfOf by remember { mutableStateOf(existingCase?.onBehalfOf ?: "شاكي / مدعي") }

    var partyName by remember { mutableStateOf(existingCase?.partyName ?: "") }
    var partyContact by remember { mutableStateOf(existingCase?.partyContact ?: "") }
    var adverseAdvocateName by remember { mutableStateOf(existingCase?.adverseAdvocateName ?: "") }
    var adverseAdvocateContact by remember { mutableStateOf(existingCase?.adverseAdvocateContact ?: "") }

    var adversePartyName by remember { mutableStateOf(existingCase?.adversePartyName ?: "") }
    var adversePartyContact by remember { mutableStateOf(existingCase?.adversePartyContact ?: "") }
    var respondentName by remember { mutableStateOf(existingCase?.respondentName ?: "") }
    var filedUnderSection by remember { mutableStateOf(existingCase?.filedUnderSection ?: "") }

    // Initial step fields for new cases
    var initialAdjournDate by remember { mutableStateOf("") }
    var initialPurpose by remember { mutableStateOf("") }

    // Dropdown expanded states
    var titleExpanded by remember { mutableStateOf(false) }
    var courtExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var behalfExpanded by remember { mutableStateOf(false) }

    // Dialog for adding custom court or case type
    var showAddCourtDialog by remember { mutableStateOf(false) }
    var showAddTypeDialog by remember { mutableStateOf(false) }
    var newCourtInput by remember { mutableStateOf("") }
    var newTypeInput by remember { mutableStateOf("") }

    // Set default selections if empty and lists are available
    LaunchedEffect(allCourts, allCaseTypes) {
        if (courtName.isEmpty() && allCourts.isNotEmpty()) {
            courtName = allCourts.first().name
        }
        if (caseType.isEmpty() && allCaseTypes.isNotEmpty()) {
            caseType = allCaseTypes.first().name
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingCase != null) "تعديل ملف القضية" else "إضافة قضية جديدة (Add Case)",
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
                    IconButton(
                        onClick = {
                            if (caseTitle.isNotBlank() && caseNumber.isNotBlank()) {
                                val entity = CaseEntity(
                                    id = existingCase?.id ?: 0,
                                    title = caseTitle.trim(),
                                    courtName = courtName.trim().ifEmpty { "محكمة غير محددة" },
                                    caseType = caseType.trim().ifEmpty { "عام" },
                                    caseNumber = caseNumber.trim(),
                                    caseYear = caseYear.trim(),
                                    onBehalfOf = onBehalfOf,
                                    partyName = partyName.trim(),
                                    partyContact = partyContact.trim(),
                                    adversePartyName = adversePartyName.trim(),
                                    adversePartyContact = adversePartyContact.trim(),
                                    adverseAdvocateName = adverseAdvocateName.trim(),
                                    adverseAdvocateContact = adverseAdvocateContact.trim(),
                                    respondentName = respondentName.trim(),
                                    filedUnderSection = filedUnderSection.trim(),
                                    isDisposed = existingCase?.isDisposed ?: false,
                                    createdAt = existingCase?.createdAt ?: System.currentTimeMillis()
                                )
                                if (existingCase != null) {
                                    viewModel.updateCase(entity)
                                    onSaved(entity.id)
                                } else {
                                    val step = if (initialAdjournDate.isNotBlank() || initialPurpose.isNotBlank()) {
                                        StepEntity(
                                            caseId = 0,
                                            previousDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH).format(java.util.Date()),
                                            adjournDate = initialAdjournDate.trim(),
                                            purpose = initialPurpose.trim().ifEmpty { "للحضور ومتابعة الملف" },
                                            isReminder = true
                                        )
                                    } else null
                                    viewModel.addCase(entity, step) { newId ->
                                        onSaved(newId)
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "حفظ", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RoyalBlue
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header exactly like Screenshot 1: CASE DETAILS
            item {
                SectionHeader("CASE DETAILS (بيانات القضية)")
            }

            // Case Title Dropdown + Add
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = titleExpanded,
                        onExpandedChange = { titleExpanded = !titleExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = caseTitle,
                            onValueChange = { caseTitle = it },
                            label = { Text("Case Title * (موضوع الدعوى)") },
                            placeholder = { Text("اختر أو اكتب عنوان القضية") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = titleExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalBlue)
                        )
                        ExposedDropdownMenu(
                            expanded = titleExpanded,
                            onDismissRequest = { titleExpanded = false }
                        ) {
                            presetTitles.forEach { title ->
                                DropdownMenuItem(
                                    text = { Text(title) },
                                    onClick = {
                                        caseTitle = title
                                        titleExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Court Name Dropdown + Add button
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = courtExpanded,
                        onExpandedChange = { courtExpanded = !courtExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = courtName,
                            onValueChange = { courtName = it },
                            label = { Text("Court Name * (المحكمة أو المجلس)") },
                            readOnly = false,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courtExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalBlue)
                        )
                        ExposedDropdownMenu(
                            expanded = courtExpanded,
                            onDismissRequest = { courtExpanded = false }
                        ) {
                            allCourts.forEach { court ->
                                DropdownMenuItem(
                                    text = { Text(court.name) },
                                    onClick = {
                                        courtName = court.name
                                        courtExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = { showAddCourtDialog = true },
                        modifier = Modifier.background(RoyalBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة محكمة", tint = RoyalBlue)
                    }
                }
            }

            // Case Type Dropdown + Add button
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = caseType,
                            onValueChange = { caseType = it },
                            label = { Text("Case Type * (نوع أو غرف الدعوى)") },
                            readOnly = false,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalBlue)
                        )
                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false }
                        ) {
                            allCaseTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = {
                                        caseType = type.name
                                        typeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = { showAddTypeDialog = true },
                        modifier = Modifier.background(RoyalBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة نوع", tint = RoyalBlue)
                    }
                }
            }

            // Case No and Year
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = caseNumber,
                        onValueChange = { caseNumber = it },
                        label = { Text("Case No. * (رقم الملف)") },
                        placeholder = { Text("مثال: 5157") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.3f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalBlue)
                    )
                    OutlinedTextField(
                        value = caseYear,
                        onValueChange = { caseYear = it },
                        label = { Text("Year * (السنة)") },
                        placeholder = { Text("2026") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalBlue)
                    )
                }
            }

            // On Behalf Of
            item {
                ExposedDropdownMenuBox(
                    expanded = behalfExpanded,
                    onExpandedChange = { behalfExpanded = !behalfExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = onBehalfOf,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("On Behalf Of * (بصفة الموكل)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = behalfExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalBlue)
                    )
                    ExposedDropdownMenu(
                        expanded = behalfExpanded,
                        onDismissRequest = { behalfExpanded = false }
                    ) {
                        behalfOfOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onBehalfOf = option
                                    behalfExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Header: PARTY DETAILS
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("PARTY DETAILS (بيانات الموكل والخصم)")
            }

            item {
                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text("Party Name (اسم الموكل)") },
                    placeholder = { Text("الاسم الكامل للموكل") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalBlue)
                )
            }

            item {
                OutlinedTextField(
                    value = partyContact,
                    onValueChange = { partyContact = it },
                    label = { Text("Contact No (رقم هاتف الموكل)") },
                    placeholder = { Text("0550 / 0660 / 0770") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalBlue)
                )
            }

            item {
                OutlinedTextField(
                    value = adversePartyName,
                    onValueChange = { adversePartyName = it },
                    label = { Text("Adverse Party Name (اسم الخصم)") },
                    placeholder = { Text("الاسم الكامل للطرف الآخر أو النيابة") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalBlue)
                )
            }

            item {
                OutlinedTextField(
                    value = adversePartyContact,
                    onValueChange = { adversePartyContact = it },
                    label = { Text("Adverse Party Contact (هاتف الخصم)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalBlue)
                )
            }

            item {
                OutlinedTextField(
                    value = adverseAdvocateName,
                    onValueChange = { adverseAdvocateName = it },
                    label = { Text("Adverse Advocate Name (محامي الخصم)") },
                    placeholder = { Text("اسم المحامي الموكل عن الطرف الآخر") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalBlue)
                )
            }

            // Header: OTHER DETAILS
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("OTHER DETAILS (تفاصيل ومواد قانونية)")
            }

            item {
                OutlinedTextField(
                    value = filedUnderSection,
                    onValueChange = { filedUnderSection = it },
                    label = { Text("Filed U/sec (المادة أو السند القانوني)") },
                    placeholder = { Text("مثال: المادة 372 من قانون العقوبات أو المادة 57 أسرة") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalBlue)
                )
            }

            item {
                OutlinedTextField(
                    value = respondentName,
                    onValueChange = { respondentName = it },
                    label = { Text("Respondent / Judge Name (القاضي أو الغرفة)") },
                    placeholder = { Text("اسم الغرفة القضائية أو المستشار") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalBlue)
                )
            }

            if (existingCase == null) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader("INITIAL HEARING / STEP (أول جلسة - اختياري)")
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = initialAdjournDate,
                            onValueChange = { initialAdjournDate = it },
                            label = { Text("Adjourn Dt. (تاريخ الجلسة القادمة)") },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalBlue)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = initialPurpose,
                        onValueChange = { initialPurpose = it },
                        label = { Text("Step Purpose (الغرض من الجلسة)") },
                        placeholder = { Text("مثال: للحضور، تقديم مذكرة، تبليغ، النطق بالحكم") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoyalBlue)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (caseTitle.isNotBlank() && caseNumber.isNotBlank()) {
                            val entity = CaseEntity(
                                id = existingCase?.id ?: 0,
                                title = caseTitle.trim(),
                                courtName = courtName.trim().ifEmpty { "محكمة غير محددة" },
                                caseType = caseType.trim().ifEmpty { "عام" },
                                caseNumber = caseNumber.trim(),
                                caseYear = caseYear.trim(),
                                onBehalfOf = onBehalfOf,
                                partyName = partyName.trim(),
                                partyContact = partyContact.trim(),
                                adversePartyName = adversePartyName.trim(),
                                adversePartyContact = adversePartyContact.trim(),
                                adverseAdvocateName = adverseAdvocateName.trim(),
                                adverseAdvocateContact = adverseAdvocateContact.trim(),
                                respondentName = respondentName.trim(),
                                filedUnderSection = filedUnderSection.trim(),
                                isDisposed = existingCase?.isDisposed ?: false,
                                createdAt = existingCase?.createdAt ?: System.currentTimeMillis()
                            )
                            if (existingCase != null) {
                                viewModel.updateCase(entity)
                                onSaved(entity.id)
                            } else {
                                val step = if (initialAdjournDate.isNotBlank() || initialPurpose.isNotBlank()) {
                                    StepEntity(
                                        caseId = 0,
                                        previousDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH).format(java.util.Date()),
                                        adjournDate = initialAdjournDate.trim(),
                                        purpose = initialPurpose.trim().ifEmpty { "للحضور ومتابعة الملف" },
                                        isReminder = true
                                    )
                                } else null
                                viewModel.addCase(entity, step) { newId ->
                                    onSaved(newId)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (existingCase != null) "حفظ التعديلات" else "حفظ ملف القضية الجديد",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // Dialog: Add Court
    if (showAddCourtDialog) {
        AlertDialog(
            onDismissRequest = { showAddCourtDialog = false },
            title = { Text("إضافة محكمة أو مجلس جديد") },
            text = {
                OutlinedTextField(
                    value = newCourtInput,
                    onValueChange = { newCourtInput = it },
                    label = { Text("اسم المحكمة / المجلس") },
                    placeholder = { Text("مثال: محكمة القضاء الإداري") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCourtInput.isNotBlank()) {
                            viewModel.addCourt(newCourtInput)
                            courtName = newCourtInput.trim()
                            newCourtInput = ""
                            showAddCourtDialog = false
                        }
                    }
                ) { Text("إضافة") }
            },
            dismissButton = {
                TextButton(onClick = { showAddCourtDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Dialog: Add Case Type
    if (showAddTypeDialog) {
        AlertDialog(
            onDismissRequest = { showAddTypeDialog = false },
            title = { Text("إضافة نوع قضية جديد") },
            text = {
                OutlinedTextField(
                    value = newTypeInput,
                    onValueChange = { newTypeInput = it },
                    label = { Text("نوع القضية") },
                    placeholder = { Text("مثال: جنايات عسكرية أو تحكيم تجاري") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTypeInput.isNotBlank()) {
                            viewModel.addCaseType(newTypeInput)
                            caseType = newTypeInput.trim()
                            newTypeInput = ""
                            showAddTypeDialog = false
                        }
                    }
                ) { Text("إضافة") }
            },
            dismissButton = {
                TextButton(onClick = { showAddTypeDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFDCE6F1), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = RoyalBlueDark
            )
        )
    }
}
