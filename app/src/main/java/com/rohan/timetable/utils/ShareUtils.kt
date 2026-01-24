package com.rohan.timetable.utils

import android.R
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.rohan.timetable.ClassEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object ShareUtils {
    fun saveToFile(context: Context,grouped:Map<String, List<ClassEntity>>):String{
            val root= JSONObject();
            grouped.forEach {(day,classes)->
                val classArray= JSONArray();
                classes.forEach {c->
                    val t= JSONObject()
                    t.put("time",c.time)
                    t.put("subjectName",c.subjectName)
                    t.put("classroom",c.classroom)
                    t.put("classType",c.classType)
                    classArray.put(t);
                }
                root.put(day,classArray);
            }
        val jsonString=root.toString(2);
        Log.d("JSON",jsonString);
        val filename:String="timetable_export.json";
        val file= File(context.filesDir,filename);
        file.writeText(jsonString);
        return  file.path;
    }
    fun ShareFile(context: Context, filepath: String){
        val file=File(filepath);
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file,
            )
        val intent= Intent(Intent.ACTION_SEND).apply {
            type="application/json"
            putExtra(Intent.EXTRA_STREAM,uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(
                intent,"Share Timetable"
            )
        )
    }
}