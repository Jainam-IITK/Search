package com.jb.search

import android.content.ContentValues.TAG
import android.os.Build
import android.os.FileObserver
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.orhanobut.logger.Logger
import java.io.File

class MyFileObserver(filepath:String) :FileObserver(filepath) {
    override fun onEvent(p0: Int, p1: String?) {
        //Log.d(TAG, "onEvent:$p0 $p1 ")
        Logger.d("$p0 $p1")
       // Toast.makeText(getBaseContext(), file + " was saved!", Toast.LENGTH_LONG).show();

    }
}