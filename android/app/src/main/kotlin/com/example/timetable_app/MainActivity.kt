package com.example.timetable_app
import android.content.Intent
import androidx.core.content.FileProvider
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
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->
            if (call.method == "shareFileToNewApp") {
                val path = call.argument<String>("path")
                if (path != null) {
                    shareFile(path)
                    result.success(null)
                    // If this was a background migration launch, close the app now
                    if (isMigrationMode) finish()
                }
            }
        }

        // 3. If Migration Mode, immediately tell Flutter to generate the file
        if (isMigrationMode) {
            // Give Flutter a moment to initialize, then trigger
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
                    .invokeMethod("generateMigrationData", null)
            }, 500) // Small delay to ensure Flutter engine is ready
        }
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