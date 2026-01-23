package com.example.timetable_app

import android.content.Intent
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.File

class MainActivity : FlutterActivity() {
    private val CHANNEL = "migration"
    private var isMigrationMode = false

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // 1. Check if we were launched for Migration
        if (intent.action == "com.example.timetable_app.ACTION_MIGRATE") {
            isMigrationMode = true
        }

        // 2. Setup the Channel
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                // Flutter asks: "Should I migrate?"
                "checkForMigration" -> {
                    result.success(isMigrationMode)
                }

                // Flutter says: "Here is the path to the file I created"
                "shareFileToNewApp" -> {
                    val path = call.argument<String>("path")
                    if (path != null) {
                        // NEW LOGIC: Read file -> Send Text
                        readAndSendFileContent(path)
                        result.success(null)

                        // Close old app if done
                        if (isMigrationMode) finish()
                    } else {
                        result.error("INVALID_PATH", "Path was null", null)
                    }
                }

                else -> result.notImplemented()
            }
        }
    }

    private fun readAndSendFileContent(path: String) {
        try {
            val file = File(path)

            // 1. NATIVE READ: Read the file content into a String
            // This works perfectly for internal files without FileProvider permissions
            val jsonContent = file.readText()

            // 2. SEND DIRECTLY: Send the content string
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain" // or "application/json"

                // Put the DATA directly (no file URI needed)
                putExtra(Intent.EXTRA_TEXT, jsonContent)

                // 3. TARGET SPECIFIC APP: Bypasses the "Share with..." UI
                // Ensure this package and class name matches your NEW APP exactly
                setClassName("com.rohan.timetable", "com.rohan.timetable.MainActivity")

                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            startActivity(intent)
            println("✅ Data sent successfully via Native Read!")

        } catch (e: Exception) {
            println("❌ Failed to read/send file: ${e.message}")
            e.printStackTrace()
        }
    }
}