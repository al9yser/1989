# AL-MUFAKKIK — المفكك

Android APK inspection and reconstruction workspace.

## Current milestone

This repository starts the native Android application foundation. The first milestone provides a real APK picker, safe ZIP/container inspection, file inventory, project summary, and export of the extracted archive contents. Advanced DEX/Smali decompilation, AI-assisted code analysis, and APK rebuilding will be added as separately testable modules.

## Build

Open this project in Android Studio and run the app configuration, or use the GitHub Actions workflow to build a debug APK.

## Safety

Only analyze APKs you own or are authorized to inspect. Imported APKs are treated as data and are never executed by the app.
