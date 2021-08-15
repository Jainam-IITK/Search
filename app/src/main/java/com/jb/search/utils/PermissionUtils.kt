package com.jb.search.utils

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay


fun isStoragePermissionGranted(context: Context,activity: Activity) :Boolean{

    if (ContextCompat.checkSelfPermission(context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    ) {
        Log.v(ContentValues.TAG, "Permission is granted")
        return true
    } else {
        Log.v(ContentValues.TAG, "Permission is revoked")
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1
        )
    }
    return false
}
suspend fun checkPermissionRecur(context :Context,activity:Activity):Boolean{
    while (!isStoragePermissionGranted(context, activity)){
        delay(1000L)
    }
    return true
}