package com.al9yser.mufakkik

import org.jf.baksmali.Baksmali
import org.jf.baksmali.BaksmaliOptions
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.dexbacked.DexBackedDexFile
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipFile

data class DexAnalysis(
    val dexFiles: Int,
    val classCount: Int,
    val methodCount: Int,
    val fieldCount: Int,
    val stringCount: Int,
    val classes: List<String>,
    val methodPreview: List<String>,
    val smaliDirectory: File,
    val smaliFileCount: Int
)

object DexAnalyzer {
    private val dexName = Regex("classes(\\d+)?\\.dex")

    fun analyze(apk: File, output: File): DexAnalysis {
        require(apk.isFile && apk.length() > 0L) { "ملف APK غير صالح أو فارغ" }
        require(apk.length() <= 250L * 1024L * 1024L) { "حجم APK يتجاوز الحد الآمن البالغ 250 MB" }

        val dexEntries = ZipFile(apk).use { zip ->
            zip.entries().asSequence()
                .filter { !it.isDirectory && dexName.matches(it.name) }
                .sortedBy { if (it.name == "classes.dex") 1 else it.name.removePrefix("classes").removeSuffix(".dex").toIntOrNull() ?: 999 }
                .toList()
                .map { entry -> entry.name to zip.getInputStream(entry).use { it.readBytes() } }
        }
        require(dexEntries.isNotEmpty()) { "لم يتم العثور على ملفات DEX داخل APK" }

        val smaliRoot = File(output, "smali").apply { mkdirs() }
        var classCount = 0
        var methodCount = 0
        var fieldCount = 0
        var stringCount = 0
        val classes = mutableListOf<String>()
        val methods = mutableListOf<String>()
        val options = BaksmaliOptions().apply {
            apiLevel = 35
            parameterRegisters = true
            debugInfo = true
            accessorComments = true
        }

        dexEntries.forEachIndexed { index, pair ->
            val dex = DexBackedDexFile.fromInputStream(Opcodes.getDefault(), ByteArrayInputStream(pair.second))
            classCount += dex.classes.size
            stringCount += dex.stringReferences.size
            dex.classes.forEach { clazz ->
                classes += clazz.type
                clazz.methods.forEach { method ->
                    methodCount++
                    if (methods.size < 500) {
                        val params = method.parameterTypes.joinToString(",") { it.toString() }
                        methods += "${method.definingClass}->${method.name}($params):${method.returnType}"
                    }
                }
                fieldCount += clazz.fields.count()
            }
            val target = if (index == 0) smaliRoot else File(smaliRoot, "dex${index + 1}").apply { mkdirs() }
            check(Baksmali.disassembleDexFile(dex, target, 2, options)) {
                "تعذر تفكيك ${pair.first} إلى Smali"
            }
        }

        val smaliCount = smaliRoot.walkTopDown().count { it.isFile && it.extension == "smali" }
        return DexAnalysis(
            dexFiles = dexEntries.size,
            classCount = classCount,
            methodCount = methodCount,
            fieldCount = fieldCount,
            stringCount = stringCount,
            classes = classes.sorted(),
            methodPreview = methods,
            smaliDirectory = smaliRoot,
            smaliFileCount = smaliCount
        )
    }
}
