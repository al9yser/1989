package com.al9yser.mufakkik

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

private data class ApkReport(
    val name: String,
    val entries: List<String>,
    val totalBytes: Long,
    val dexCount: Int,
    val hasManifest: Boolean,
    val nativeLibraries: Int,
    val resourceFiles: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MufakkikApp() }
    }
}

@Composable
private fun MufakkikApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var report by remember { mutableStateOf<ApkReport?>(null) }
    var dex by remember { mutableStateOf<DexAnalysis?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("جاهز لتحليل APK") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            busy = true
            error = null
            report = null
            dex = null
            status = "جارٍ نسخ الملف وفحص الحاوية وملفات DEX…"
            scope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        val name = displayName(context, uri)
                        val apk = copyToCache(context, uri, name)
                        val archiveReport = inspectArchive(apk, name)
                        val work = File(context.cacheDir, "mufakkik-work").apply {
                            deleteRecursively()
                            mkdirs()
                        }
                        val dexReport = DexAnalyzer.analyze(apk, work)
                        archiveReport to dexReport
                    }
                    report = result.first
                    dex = result.second
                    status = "اكتمل التحليل والتفكيك إلى Smali"
                } catch (e: Exception) {
                    error = e.message ?: e.localizedMessage ?: "حدث خطأ غير معروف"
                    status = "تعذر إكمال التحليل"
                } finally {
                    busy = false
                }
            }
        }
    }

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        val current = report
        val disassembly = dex
        if (uri != null && current != null && disassembly != null) {
            busy = true
            scope.launch {
                try {
                    withContext(Dispatchers.IO) { exportProject(context, uri, current, disassembly) }
                    status = "تم إنشاء حزمة Smali والتقرير بنجاح"
                } catch (e: Exception) {
                    error = e.message ?: "فشل تصدير المشروع"
                    status = "فشل التصدير"
                } finally {
                    busy = false
                }
            }
        }
    }

    val bg = Color(0xFF071018)
    val panel = Color(0xFF111F2A)
    val mint = Color(0xFF5DE2B0)
    MaterialTheme(colorScheme = darkColorScheme(primary = mint, secondary = Color(0xFF65B7D8), background = bg, surface = panel, surfaceVariant = Color(0xFF1A2B37))) {
        Surface(Modifier.fillMaxSize(), color = bg) {
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFF143A35)) {
                        Icon(Icons.Default.Android, null, tint = mint, modifier = Modifier.padding(11.dp).size(30.dp))
                    }
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text("المفكّك", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("AL-MUFAKKIK  /  APK REVERSE ENGINEERING", color = Color(0xFF8EA5B2), fontSize = 9.sp, letterSpacing = 0.8.sp)
                    }
                    Text("v0.2", color = mint, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FolderOpen, null, tint = mint)
                            Spacer(Modifier.width(8.dp))
                            Text("مساحة تحليل APK", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(7.dp))
                        Text("فحص آمن للأرشيف، قراءة DEX، استخراج أسماء الكلاسات والدوال والحقول، ثم تفكيك bytecode إلى ملفات Smali قابلة للتصفح والتصدير.", color = Color(0xFF9EB1BC), fontSize = 12.sp, lineHeight = 19.sp)
                        Spacer(Modifier.height(13.dp))
                        Button(onClick = { picker.launch(arrayOf("application/vnd.android.package-archive", "application/octet-stream", "application/zip")) }, enabled = !busy, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(13.dp)) {
                            Icon(Icons.Default.Archive, null)
                            Spacer(Modifier.width(8.dp))
                            Text("اختيار APK وبدء التفكيك")
                        }
                        if (report != null && dex != null) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(onClick = { exporter.launch("AL-MUFAKKIK-${report!!.name.substringBeforeLast('.', "project")}-Smali.zip") }, enabled = !busy, modifier = Modifier.fillMaxWidth().height(46.dp), shape = RoundedCornerShape(13.dp)) {
                                Icon(Icons.Default.Download, null)
                                Spacer(Modifier.width(8.dp))
                                Text("تصدير مشروع Smali + التقارير")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, tint = mint, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(status, color = mint, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    if (busy) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                }
                if (busy) {
                    Spacer(Modifier.height(7.dp))
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Color(0xFFFF8A80), fontSize = 11.sp)
                }
                val a = report
                val d = dex
                if (a != null && d != null) {
                    Spacer(Modifier.height(13.dp))
                    Text(a.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                    Text("الحجم المفكوك: ${formatBytes(a.totalBytes)}  •  Manifest: ${if (a.hasManifest) "موجود" else "غير موجود"}  •  مكتبات Native: ${a.nativeLibraries}", color = Color(0xFF8EA5B2), fontSize = 10.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Metric("DEX", d.dexFiles.toString(), Modifier.weight(1f), mint)
                        Metric("Classes", d.classCount.toString(), Modifier.weight(1f), mint)
                        Metric("Methods", d.methodCount.toString(), Modifier.weight(1f), mint)
                        Metric("Smali", d.smaliFileCount.toString(), Modifier.weight(1f), mint)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Metric("Fields", d.fieldCount.toString(), Modifier.weight(1f), Color(0xFF79C7E8))
                        Metric("Strings", d.stringCount.toString(), Modifier.weight(1f), Color(0xFF79C7E8))
                        Metric("Resources", a.resourceFiles.toString(), Modifier.weight(1f), Color(0xFF79C7E8))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("الكلاسات المستخرجة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(d.classes.take(600)) { item ->
                            Row(Modifier.fillMaxWidth().background(panel, RoundedCornerShape(8.dp)).padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Code, null, tint = mint, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(7.dp))
                                Text(item, color = Color(0xFFD6E1E7), fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }
                    Text("تم تفكيك ${d.smaliFileCount} ملف Smali. عينة الدوال: ${d.methodPreview.size} — صدّر الحزمة لعرض جميع الملفات.", color = Color(0xFF718A98), fontSize = 9.sp)
                } else {
                    Spacer(Modifier.height(12.dp))
                    Card(Modifier.weight(1f).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Terminal, null, tint = Color(0xFF36515F), modifier = Modifier.size(54.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("لم يتم تحميل APK بعد", color = Color(0xFFDCE6EA), fontWeight = FontWeight.SemiBold)
                            Text("اختر ملف التطبيق للبدء بتحليل DEX وإنشاء ملفات Smali حقيقية.", color = Color(0xFF8299A5), fontSize = 11.sp, lineHeight = 18.sp)
                        }
                    }
                }
                Spacer(Modifier.height(7.dp))
                Text("الملف المستورد يعامل كبيانات فقط ولا يتم تشغيله. استخدم هذه الأداة لتحليل التطبيقات التي تملكها أو تملك إذناً بفحصها.", color = Color(0xFF607985), fontSize = 9.sp, lineHeight = 14.sp)
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier, tint: Color) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFF142A31)), shape = RoundedCornerShape(11.dp)) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 9.dp)) {
            Text(value, color = tint, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, color = Color(0xFF9CB0BB), fontSize = 9.sp)
        }
    }
}

private fun displayName(context: Context, uri: Uri): String {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) return cursor.getString(0) ?: "selected.apk"
    }
    return uri.lastPathSegment?.substringAfterLast('/') ?: "selected.apk"
}

private fun copyToCache(context: Context, uri: Uri, name: String): File {
    val target = File(context.cacheDir, "input-${System.currentTimeMillis()}.apk")
    context.contentResolver.openInputStream(uri).use { input ->
        requireNotNull(input) { "تعذر فتح ملف APK" }
        target.outputStream().buffered().use { output -> input.copyTo(output) }
    }
    require(target.length() in 1..(250L * 1024L * 1024L)) { "الملف فارغ أو يتجاوز 250 MB" }
    return target
}

private fun inspectArchive(apk: File, name: String): ApkReport {
    val paths = mutableListOf<String>()
    var total = 0L
    var dex = 0
    var native = 0
    var resources = 0
    var manifest = false
    ZipFile(apk).use { zip ->
        val entries = zip.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            if (entry.isDirectory) continue
            require(entry.name.length < 1024 && !entry.name.startsWith("/") && !entry.name.split('/').contains("..")) { "مسار ملف غير آمن داخل APK" }
            val size = entry.size.coerceAtLeast(0L)
            require(size <= 200L * 1024L * 1024L) { "أحد الملفات داخل APK يتجاوز الحد الآمن" }
            total += size
            require(total <= 1024L * 1024L * 1024L) { "الحجم المفكوك للأرشيف يتجاوز 1 GB" }
            paths += entry.name
            if (entry.name.matches(Regex("classes(\\d+)?\\.dex"))) dex++
            if (entry.name.startsWith("lib/") && entry.name.endsWith(".so")) native++
            if (entry.name.startsWith("res/")) resources++
            if (entry.name == "AndroidManifest.xml") manifest = true
        }
    }
    return ApkReport(name, paths.sorted(), total, dex, manifest, native, resources)
}

private fun exportProject(context: Context, uri: Uri, report: ApkReport, dex: DexAnalysis) {
    val output = context.contentResolver.openOutputStream(uri) ?: error("تعذر إنشاء ملف التصدير")
    ZipOutputStream(output.buffered()).use { zip ->
        val summary = buildString {
            appendLine("AL-MUFAKKIK — DEX DISASSEMBLY REPORT")
            appendLine("APK: ${report.name}")
            appendLine("DEX files: ${dex.dexFiles}")
            appendLine("Classes: ${dex.classCount}")
            appendLine("Methods: ${dex.methodCount}")
            appendLine("Fields: ${dex.fieldCount}")
            appendLine("Strings: ${dex.stringCount}")
            appendLine("Smali files: ${dex.smaliFileCount}")
            appendLine("Native libraries: ${report.nativeLibraries}")
            appendLine("Resources: ${report.resourceFiles}")
            appendLine()
            appendLine("Note: Smali is a disassembly of DEX bytecode, not recovered original Kotlin source.")
        }
        zip.putNextEntry(ZipEntry("analysis/DEX-SUMMARY.txt"))
        zip.write(summary.toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        zip.putNextEntry(ZipEntry("analysis/APK-INVENTORY.txt"))
        zip.write(report.entries.joinToString("\n", postfix = "\n").toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        zip.putNextEntry(ZipEntry("analysis/CLASSES.txt"))
        zip.write(dex.classes.joinToString("\n", postfix = "\n").toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        zip.putNextEntry(ZipEntry("analysis/METHODS-SAMPLE.txt"))
        zip.write(dex.methodPreview.joinToString("\n", postfix = "\n").toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        val root = dex.smaliDirectory
        root.walkTopDown().filter { it.isFile }.forEach { file ->
            val relative = file.relativeTo(root).invariantSeparatorsPath
            zip.putNextEntry(ZipEntry("smali/$relative"))
            file.inputStream().buffered().use { it.copyTo(zip) }
            zip.closeEntry()
        }
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L -> "%.2f MB".format(bytes / (1024.0 * 1024.0))
    bytes >= 1024L -> "%.1f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}
