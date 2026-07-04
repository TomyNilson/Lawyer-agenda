package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupAndSecurityScreen(
    viewModel: LawyerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val hasPassword by viewModel.hasPassword.collectAsState()
    val lastBackupDate by viewModel.lastBackupDate.collectAsState()

    var showSetPasswordDialog by remember { mutableStateOf(false) }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var showExportResultDialog by remember { mutableStateOf(false) }
    var exportedJsonString by remember { mutableStateOf("") }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var restoreMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "النسخ الاحتياطي وحماية المفكرة",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RoyalBlue)
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
            // Security Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "حماية التطبيق بكلمة مرور / رمز مرور",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                                Text(
                                    text = if (hasPassword) "نظام قفل الحماية مفعل حالياً لحفظ السرية" else "التطبيق مفتوح حالياً وبدون كلمة سر",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasPassword) {
                                Button(
                                    onClick = {
                                        newPasswordInput = ""
                                        confirmPasswordInput = ""
                                        passwordError = null
                                        showSetPasswordDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                                ) { Text("تغيير كلمة المرور") }

                                OutlinedButton(
                                    onClick = { viewModel.removePassword() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) { Text("إلغاء القفل") }
                            } else {
                                Button(
                                    onClick = {
                                        newPasswordInput = ""
                                        confirmPasswordInput = ""
                                        passwordError = null
                                        showSetPasswordDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تفعيل قفل الحماية الآن")
                                }
                            }
                        }
                    }
                }
            }

            // Backup & Restore Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "النسخ الاحتياطي (Backup & Restore)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                                Text(
                                    text = "آخر نسخ احتياطي مسجل: $lastBackupDate",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "يمكنك تصدير قاعدة البيانات كاملة (القضايا، الجلسات، والمستندات) كملف نصي مشفر وحفظه أو مشاركته عبر الواتساب أو البريد الإلكتروني، واستعادته في أي وقت.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.exportBackup { json ->
                                    exportedJsonString = json
                                    showExportResultDialog = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تصدير النسخة الاحتياطية (Export JSON)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                restoreJsonInput = ""
                                restoreMessage = null
                                showRestoreDialog = true
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalBlue)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("استعادة النسخة الاحتياطية (Restore JSON)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }

    // Dialog: Set/Change Password
    if (showSetPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showSetPasswordDialog = false },
            title = { Text(if (hasPassword) "تغيير رمز المرور" else "تعيين رمز مرور جديد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("أدخل كلمة المرور أو الرمز السري الذي ترغب في استخدامه لفتح التطبيق:", style = MaterialTheme.typography.bodySmall)

                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = {
                            newPasswordInput = it
                            passwordError = null
                        },
                        label = { Text("كلمة المرور الجديدة") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = {
                            confirmPasswordInput = it
                            passwordError = null
                        },
                        label = { Text("تأكيد كلمة المرور") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (passwordError != null) {
                        Text(passwordError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPasswordInput.isBlank()) {
                            passwordError = "الرجاء إدخال كلمة مرور"
                        } else if (newPasswordInput != confirmPasswordInput) {
                            passwordError = "كلمتا المرور غير متطابقتين"
                        } else {
                            viewModel.setPassword(newPasswordInput)
                            showSetPasswordDialog = false
                            Toast.makeText(context, "تم حفظ رمز المرور بنجاح!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) { Text("حفظ الرمز") }
            },
            dismissButton = {
                TextButton(onClick = { showSetPasswordDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Dialog: Export Result
    if (showExportResultDialog) {
        AlertDialog(
            onDismissRequest = { showExportResultDialog = false },
            title = { Text("تم إنشاء النسخة الاحتياطية بنجاح!") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("يمكنك نسخ كود النسخة الاحتياطية أو مشاركته لحفظه في مكان آمن:", style = MaterialTheme.typography.bodyMedium)

                    OutlinedTextField(
                        value = exportedJsonString,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, exportedJsonString)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة ملف النسخة الاحتياطية"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة (Share)")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("LawyerBackup", exportedJsonString)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم النسخ إلى الحافظة!", Toast.LENGTH_SHORT).show()
                        }
                    ) { Text("نسخ (Copy)") }

                    TextButton(onClick = { showExportResultDialog = false }) { Text("إغلاق") }
                }
            }
        )
    }

    // Dialog: Restore Backup
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("استعادة النسخة الاحتياطية") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("الرجاء لصق كود النسخة الاحتياطية (JSON) هنا لاستعادة جميع بيانات القضايا والجلسات:", style = MaterialTheme.typography.bodySmall)

                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = {
                            restoreJsonInput = it
                            restoreMessage = null
                        },
                        placeholder = { Text("ألصق الكود هنا...") },
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (restoreMessage != null) {
                        Text(restoreMessage!!, color = if (restoreMessage!!.contains("بنجاح")) Color(0xFF16A34A) else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreJsonInput.isNotBlank()) {
                            viewModel.restoreBackup(restoreJsonInput.trim()) { success ->
                                if (success) {
                                    restoreMessage = "تمت استعادة جميع البيانات والجلسات والمستندات بنجاح!"
                                    Toast.makeText(context, "تم استرجاع البيانات!", Toast.LENGTH_SHORT).show()
                                } else {
                                    restoreMessage = "كود غير صالح أو تالف، تأكد من لصق كود النسخة الاحتياطية كاملاً."
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) { Text("بدء الاستعادة") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) { Text("إغلاق") }
            }
        )
    }
}
