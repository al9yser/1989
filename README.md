# AL-MUFAKKIK — المفكّك

Android APK inspection and DEX-to-Smali disassembly workspace.

## v0.2.0 — real bytecode analysis

This release moves beyond ZIP inventory and adds an on-device DEX analysis pipeline:

- Select an APK through Android's document picker; the APK is never executed.
- Inspect the APK container, resources, native libraries, and multidex layout.
- Parse DEX files with dexlib2.
- Enumerate class descriptors, method signatures, fields, and string-reference counts.
- Disassemble every classes*.dex file into real Smali source using baksmali.
- Export a ZIP containing the Smali tree, APK inventory, class list, method sample, and DEX summary.
- Enforce file-size, archive-size, and path validation limits before analysis.

## Important accuracy note

Smali is a readable disassembly of Dalvik bytecode. It is **not** the original Kotlin/Java source. Variable names, comments, formatting, and some high-level language constructs cannot generally be recovered from a compiled APK. This build does not yet decode Android binary XML/resources into their original editable project form, nor does it decompile native .so libraries.

## Build and install

GitHub Actions builds an installable debug-signed APK on every push to main:

- GitHub Actions artifact: AL-MUFAKKIK-v0.2.0-APK
- GitHub Release: https://github.com/al9yser/1989/releases/tag/v0.2.0
- Direct APK: https://github.com/al9yser/1989/releases/download/v0.2.0/AL-MUFAKKIK-v0.2.0-debug.apk

Open the repository in Android Studio or build with Gradle 8.11.1 and JDK 17:

    gradle --no-daemon assembleDebug

Output: app/build/outputs/apk/debug/app-debug.apk.

## Third-party components

- dexlib2 and baksmali 2.5.2 — BSD license.
- Guava — Apache License 2.0.
- AndroidX / Jetpack Compose — Apache License 2.0.

Use this tool only on APKs you own or are authorized to inspect. Imported APKs are treated as data and are never executed.