package com.jb.search.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.jb.search.FileType
import java.io.File
import java.io.IOException

object FileOpener {
    @Throws(IOException::class)
    fun openFile(context: Context, url: File) {
        // Create URI
        val uri = Uri.fromFile(url)
        val intent = Intent(Intent.ACTION_VIEW)
        // Check what kind of file you are trying to open, by comparing the url with extensions.
        // When the if condition is matched, plugin sets the correct intent (mime) type,
        // so Android knew what application to use to open the file
        val type = FileUtilFunctions.typeOfFile(url)

        when(type){
            FileType.TXT -> intent.setDataAndType(uri, "application/txt")
            FileType.PDF ->intent.setDataAndType(uri, "application/pdf")
            FileType.IMAGE -> intent.setDataAndType(uri,"image/*")
            else -> intent.setDataAndType(uri, "*/*")
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}