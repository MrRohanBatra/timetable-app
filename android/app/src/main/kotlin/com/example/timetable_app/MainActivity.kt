package com.example.timetable_app

import android.content.Intent
import androidx.core.content.FileProvider
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.File

class MainActivity : FlutterActivity() {
    private val CHANNEL = "migration"
    // The flag acts as a "state" waiting for Flutter to be ready
    private var isMigrationMode = false

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // 1. Check if we were launched for Migration
        // We do this immediately upon engine config
        if (intent.action == "com.example.timetable_app.ACTION_MIGRATE") {
            isMigrationMode = true
        }

        // 2. Setup the Channel
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                // NEW: Flutter asks if migration is needed
                "checkForMigration" -> {
                    result.success(isMigrationMode)
                }

                // Existing: Flutter sends the file path back to native
                "shareFileToNewApp" -> {
                    val path = call.argument<String>("path")
                    if (path != null) {
                        shareFile(path)
                        result.success(null)
                        // Close the app if it was just opened for migration
                        if (isMigrationMode) finish()
                    } else {
                        result.error("INVALID_PATH", "Path was null", null)
                    }
                }

                else -> result.notImplemented()
            }
        }

        // DELETED: The Handler().postDelayed block is removed.
        // We no longer forcefully invoke the method from Native.
    }

    private fun shareFile(path: String) {
        val file = File(path)
        val uri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage("com.rohan.timetable")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(intent)
    }
}