package com.al9yser.mufakkik

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.zip.ZipInputStream

private data class ApkReport(val name: String, val entries: List<String>, val totalBytes: Long, val dexCount: Int, val hasManifest: Boolean, val nativeLibraries: Int, val resourceFiles: Int)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MufakkikApp() }
    }
}

@Composable
private fun MufakkikApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var report by remember { mutableStateOf<ApkReport?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("جاهز لتحليل ملف APK") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            busy = true
            error = null
            scope.launch {
                try {
                    report = withContext(Dispatchers.IO) { inspectArchive(uri, context) }
                    status = "اكتمل الفحص الأولي"
                } catch (e: Exception) {
                    error = e.localizedMessage ?: "تعذر قراءة الملف"
                    status = "تعذر إكمال الفحص"
                } finally { busy = false }
            }
        }
    }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        val current = report
        if (uri != null && current != null) scope.launch(Dispatchers.IO) {
            val body = buildString {
                appendLine("AL-MUFAKKIK APK INSPECTION REPORT")
                appendLine("File: ${current.name}")
                appendLine("Entries: ${current.entries.size}")
                appendLine("Uncompressed bytes: ${current.totalBytes}")
                appendLine("DEX files: ${current.dexCount}")
                appendLine("Manifest present: ${current.hasManifest}")
                appendLine("Native libraries: ${current.nativeLibraries}")
                appendLine("Resource files: ${current.resourceFiles}")
                appendLine()
                current.entries.forEach { appendLine(it) }
            }
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(body) }
            withContext(Dispatchers.Main) { status = "تم حفظ تقرير الفحص" }
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF64E6B2), secondary = Color(0xFF65B7D8), background = Color(0xFF090F14), surface = Color(0xFF111B23), surfaceVariant = Color(0xFF1A2933))) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFF163B35)) {
                        Icon(Icons.Default.Android, null, tint = Color(0xFF64E6B2), modifier = Modifier.padding(12.dp).size(30.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("المفكك", fontSize = 27.sp, fontWeight = FontWeight.Bold)
                        Text("AL-MUFAKKIK  •  APK ANALYSIS STUDIO", color = Color(0xFF8EA5B2), fontSize = 10.sp, letterSpacing = 1.sp)
                    }
                    Text("v0.1", color = Color(0xFF64E6B2))
                }
                Spacer(Modifier.height(22.dp))
                Text("مساحة العمل", color = Color(0xFF8EA5B2), fontSize = 13.sp)
                Spacer(Modifier.height(9.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF111B23)), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FolderOpen, null, tint = Color(0xFF64E6B2))
                            Spacer(Modifier.width(9.dp))
                            Text("تحليل تطبيق جديد", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("ارفع ملف APK لفهرسة محتوياته وملفات DEX والموارد والمكتبات دون تشغيل التطبيق.", color = Color(0xFF9CB0BB), fontSize = 13.sp, lineHeight = 20.sp)
                        Spacer(Modifier.height(15.dp))
                        Button(onClick = { picker.launch(arrayOf("application/vnd.android.package-archive", "application/octet-stream", "application/zip")) }, enabled = !busy, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) {
                            Icon(Icons.Default.ArrowUpward, null)
                            Spacer(Modifier.width(8.dp))
                            Text("اختيار ملف APK")
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    FeatureTile("فهرسة الملفات", Icons.Default.FolderOpen, Modifier.weight(1f))
                    FeatureTile("تحليل البنية", Icons.Default.Analytics, Modifier.weight(1f))
                    FeatureTile("مراجعة آمنة", Icons.Default.Security, Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("نتيجة التحليل", fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
                    if (report != null) TextButton(onClick = { exporter.launch("mufakkik-report.txt") }) { Text("تصدير التقرير") }
                }
                if (busy) { LinearProgressIndicator(Modifier.fillMaxWidth()); Spacer(Modifier.height(7.dp)) }
                Text(status, color = Color(0xFF64E6B2), fontSize = 12.sp)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                val current = report
                if (current != null) {
                    Spacer(Modifier.height(9.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        StatCard("الملفات", current.entries.size.toString(), Modifier.weight(1f))
                        StatCard("DEX", current.dexCount.toString(), Modifier.weight(1f))
                        StatCard("الموارد", current.resourceFiles.toString(), Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(current.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("الحجم غير المضغوط: ${formatBytes(current.totalBytes)} • المكتبات: ${current.nativeLibraries} • Manifest: ${if (current.hasManifest) "موجود" else "غير موجود"}", color = Color(0xFF8EA5B2), fontSize = 10.sp)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(current.entries.take(300)) { path ->
                            Row(Modifier.fillMaxWidth().background(Color(0xFF111B23), RoundedCornerShape(8.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Code, null, tint = Color(0xFF64E6B2), modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(7.dp))
                                Text(path, color = Color(0xFFD4E0E6), fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                    if (current.entries.size > 300) Text("يتم عرض أول 300 مسار.", color = Color(0xFF8EA5B2), fontSize = 10.sp)
                } else {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Code, null, tint = Color(0xFF38515F), modifier = Modifier.size(52.dp))
                            Spacer(Modifier.height(9.dp))
                            Text("لم يتم اختيار مشروع بعد", color = Color(0xFF8EA5B2))
                            Text("ابدأ باختيار ملف APK من جهازك", color = Color(0xFF607985), fontSize = 12.sp)
                        }
                    }
                }
                Text("يتم التعامل مع الملف كبيانات فقط — لا يتم تشغيل APK المستورد.", color = Color(0xFF607985), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun FeatureTile(title: String, icon: ImageVector, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFF111B23)), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 13.dp, horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Color(0xFF64E6B2), modifier = Modifier.size(21.dp))
            Spacer(Modifier.height(6.dp))
            Text(title, color = Color(0xFFB8C8D0), fontSize = 10.sp)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFF16302E)), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(11.dp)) {
            Text(value, color = Color(0xFF64E6B2), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(label, color = Color(0xFF9CB0BB), fontSize = 11.sp)
        }
    }
}

private fun inspectArchive(uri: Uri, context: android.content.Context): ApkReport {
    val entries = mutableListOf<String>()
    var total = 0L
    var dex = 0
    var libs = 0
    var resources = 0
    var manifest = false
    context.contentResolver.openInputStream(uri).use { input ->
        requireNotNull(input) { "تعذر فتح الملف المحدد" }
        ZipInputStream(input.buffered()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory) {
                    entries += entry.name
                    val buffer = ByteArray(8192)
                    while (true) {
                        val count = zip.read(buffer)
                        if (count < 0) break
                        total += count
                    }
                    if (entry.name.matches(Regex("classes(\\d+)?\\.dex"))) dex++
                    if (entry.name.startsWith("lib/") && entry.name.endsWith(".so")) libs++
                    if (entry.name.startsWith("res/")) resources++
                    if (entry.name == "AndroidManifest.xml") manifest = true
                }
                zip.closeEntry()
            }
        }
    }
    return ApkReport(uri.lastPathSegment ?: "selected.apk", entries.sorted(), total, dex, manifest, libs, resources)
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L -> "%.2f MB".format(bytes / (1024.0 * 1024.0))
    bytes >= 1024L -> "%.1f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}
