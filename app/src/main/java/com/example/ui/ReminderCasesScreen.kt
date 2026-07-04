package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCasesScreen(
    viewModel: LawyerViewModel,
    onBack: () -> Unit,
    onCaseClicked: (Int) -> Unit
) {
    val activeCases by viewModel.activeCases.collectAsState()
    val allSteps by viewModel.allSteps.collectAsState()

    val caseMap = remember(activeCases) { activeCases.associateBy { it.id } }
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val today = sdf.format(Date())

    val reminderItems = remember(allSteps, activeCases) {
        allSteps.filter {
            it.isReminder && it.caseId in caseMap && it.adjournDate.isNotBlank() && it.adjournDate >= today
        }.sortedBy { it.adjournDate }
    }

    var notificationMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تنبيهات الجلسات والمواعيد القادمة",
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
                        viewModel.triggerManualReminderCheck { count ->
                            notificationMessage = if (count > 0) {
                                "تم إرسال تنبيه في شريط الإشعارات بعدد $count جلسة قادمة!"
                            } else {
                                "لا توجد جلسات مجدولة في الأيام القادمة."
                            }
                        }
                    }) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "فحص التنبيهات", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RoyalBlue)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (notificationMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(notificationMessage!!, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = RoyalBlueDark))
                        }
                        IconButton(onClick = { notificationMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = RoyalBlueDark)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الجلسات والمذكرات المجدولة (${reminderItems.size}):",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )

                Button(
                    onClick = {
                        viewModel.triggerManualReminderCheck { count ->
                            notificationMessage = if (count > 0) {
                                "تم إرسال تنبيه في شريط الإشعارات بعدد $count جلسة قادمة!"
                            } else {
                                "لا توجد جلسات مجدولة في الأيام القادمة."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إرسال تنبيه الآن", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (reminderItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("لا توجد جلسات أو مواعيد مجدولة في الأيام القادمة", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("سيتم التنبيه تلقائياً عند اقتراب موعد الجلسات النشطة", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(reminderItems) { step ->
                        val case = caseMap[step.caseId]
                        if (case != null) {
                            ReminderItemCard(
                                step = step,
                                case = case,
                                today = today,
                                onClick = { onCaseClicked(case.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderItemCard(
    step: com.example.data.StepEntity,
    case: com.example.data.CaseEntity,
    today: String,
    onClick: () -> Unit
) {
    val isToday = step.adjournDate == today
    val badgeColor = if (isToday) Color(0xFFDC2626) else GoldAccent
    val badgeText = if (isToday) "جلسة اليوم!" else "جلسة قادمة"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تاريخ الجلسة: ${step.adjournDate}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RoyalBlueDark)
                    )
                }

                Icon(Icons.Default.ChevronLeft, contentDescription = "التفاصيل", tint = TextSecondary)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "قضية: ${case.title} (${case.caseNumber}/${case.caseYear})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "المحكمة: ${case.courtName}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
                Text(
                    text = "الموكل: ${case.partyName}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "الغرض / المطلوب: ${step.purpose}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = RoyalBlue)
                )
            }
        }
    }
}
