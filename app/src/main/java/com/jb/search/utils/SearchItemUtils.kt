package com.jb.search.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileNotFoundException


object SearchItemUtils {

     fun fileNameFromPath(path:String):String{
        return path.substringAfterLast('/')
     }


     fun openFile(path: String,context: Context) = try{
        val file = File(path)

        if (file.exists() and file.canRead() and !file.isHidden){
           val intent =  FileUtilFunctions.getIntentOf(file,context)
            context.startActivity(intent)

        }else{
            throw FileNotFoundException("file $file is either not readable or doesen't exists or is hidden")
        }


    }catch (e:FileNotFoundException){
        Log.e("OpenFile", "openFile: $e" )
    }
}