package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: LawyerViewModel,
    onNavigateToCases: (Boolean) -> Unit, // false for active, true for disposed
    onNavigateToAddCase: () -> Unit,
    onNavigateToCourts: () -> Unit,
    onNavigateToCaseTypes: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onLockClicked: () -> Unit
) {
    val activeCases by viewModel.activeCases.collectAsState()
    val disposedCases by viewModel.disposedCases.collectAsState()
    val allSteps by viewModel.allSteps.collectAsState()
    val hasPassword by viewModel.hasPassword.collectAsState()

    // Count upcoming reminders
    val upcomingRemindersCount = remember(allSteps, activeCases) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH).format(java.util.Date())
        val activeIds = activeCases.map { it.id }.toSet()
        allSteps.count { it.isReminder && it.caseId in activeIds && it.adjournDate >= today }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.lawyer_diary_icon_1783160657724),
                            contentDescription = "شعار التطبيق",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مفكرة المحامي القانونية",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                },
                actions = {
                    if (hasPassword) {
                        IconButton(onClick = onLockClicked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "قفل التطبيق",
                                tint = Color.White
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToReminders) {
                        BadgedBox(
                            badge = {
                                if (upcomingRemindersCount > 0) {
                                    Badge(
                                        containerColor = GoldAccent,
                                        contentColor = Color.White
                                    ) {
                                        Text("$upcomingRemindersCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "التنبيهات",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RoyalBlue
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCase,
                containerColor = RoyalBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "إضافة قضية جديدة",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Banner Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.lawyer_banner_img_1783160668502),
                            contentDescription = "أجندة المحامي",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.55f))
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.lawyer_diary_icon_1783160657724),
                                contentDescription = "شعار التطبيق",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "أهلاً بك في مكتبك الرقمي",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "إدارة شاملة ومؤمنة للقضايا، الجلسات القادمة، ومذكرات الدفاع مع نسخ احتياطي وتنبيهات فورية.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Menu Items exactly like Screenshot 3
            item {
                DashboardMenuItem(
                    title = "القضايا النشطة (Cases)",
                    subtitle = "عرض قائمة جميع القضايا الجارية في المحاكم",
                    icon = Icons.Default.Search,
                    iconColor = RoyalBlue,
                    badgeCount = activeCases.size,
                    onClick = { onNavigateToCases(false) }
                )
            }

            item {
                DashboardMenuItem(
                    title = "القضايا المحكومة / المفصولة (Disposed)",
                    subtitle = "أرشيف القضايا المنتهية والصادرة بها أحكام",
                    icon = Icons.Default.Bookmark,
                    iconColor = RoyalBlue,
                    badgeCount = disposedCases.size,
                    onClick = { onNavigateToCases(true) }
                )
            }

            item {
                DashboardMenuItem(
                    title = "إضافة قضية جديدة (Add Cases)",
                    subtitle = "تسجيل ملف قضية جديد والأطراف والمواعيد",
                    icon = Icons.Default.AddBox,
                    iconColor = RoyalBlue,
                    badgeCount = null,
                    onClick = onNavigateToAddCase
                )
            }

            item {
                DashboardMenuItem(
                    title = "إدارة المحاكم والمجالس (Manage Court)",
                    subtitle = "إضافة وتعديل أسماء المحاكم والجهات القضائية",
                    icon = Icons.Default.AccountBalance,
                    iconColor = RoyalBlue,
                    badgeCount = null,
                    onClick = onNavigateToCourts
                )
            }

            item {
                DashboardMenuItem(
                    title = "إدارة أنواع القضايا (Manage CaseType)",
                    subtitle = "تصنيف القضايا (جنح، مدني، عقاري، تجاري، إلخ)",
                    icon = Icons.Default.Work,
                    iconColor = RoyalBlue,
                    badgeCount = null,
                    onClick = onNavigateToCaseTypes
                )
            }

            item {
                DashboardMenuItem(
                    title = "تنبيهات الجلسات (Reminder Cases)",
                    subtitle = "مواعيد الجلسات القادمة وإيداع المذكرات والمستندات",
                    icon = Icons.Default.Alarm,
                    iconColor = if (upcomingRemindersCount > 0) GoldAccent else RoyalBlue,
                    badgeCount = upcomingRemindersCount,
                    badgeColor = if (upcomingRemindersCount > 0) GoldAccent else RoyalBlue,
                    onClick = onNavigateToReminders
                )
            }

            item {
                DashboardMenuItem(
                    title = "النسخ الاحتياطي والحماية (Backup & Security)",
                    subtitle = "حفظ واسترجاع قاعدة بيانات القضايا وإعداد كلمة السر",
                    icon = Icons.Default.History,
                    iconColor = RoyalBlue,
                    badgeCount = null,
                    onClick = onNavigateToBackup
                )
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun DashboardMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color = RoyalBlue,
    badgeCount: Int? = null,
    badgeColor: Color = RoyalBlue,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary
                    )
                )
            }

            if (badgeCount != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$badgeCount",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronLeft, // RTL arrow pointing left
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }
        }
    }
}
