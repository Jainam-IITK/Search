package com.jb.search.utils

import android.content.ContentValues.TAG
import android.content.Context
import com.jb.search.FileType
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.jb.search.IndexType
import org.apache.commons.io.FilenameUtils
import java.lang.IllegalArgumentException


object FileUtilFunctions{
        @JvmStatic
        fun typeOfFile(file: File): FileType {
            //val extension = path.
            return when(file.extension){
                "txt" -> FileType.TXT
                "pdf" -> FileType.PDF
                "docx" ->FileType.DOCX
                "jpg","jpeg","png","HEIC","TIFF","raw" -> FileType.IMAGE
                else  -> FileType.NONE;
            }

        }
    @JvmStatic
    fun typeOfFile(fileName: String): FileType {
        //val extension = path.
        return when(FilenameUtils.getExtension(fileName)){
            "txt" -> FileType.TXT
            "pdf" -> FileType.PDF
            "docx" ->FileType.DOCX
            "jpg","jpeg","png","HEIC","TIFF","raw" -> FileType.IMAGE
            else  -> FileType.NONE;
        }

    }

    @JvmStatic
    fun indexTypeFromFile(file: File): IndexType {
        //val extension = path.
        return when(file.extension){
            "docx","txt","pdf" -> IndexType.DOCUMENT
            "jpg","jpeg","png","HEIC","TIFF","raw" -> IndexType.IMAGE
            else  -> IndexType.NONE;
        }

    }


        @JvmStatic
        fun getIntentOf(file: File,context:Context):Intent{
            val type = typeOfFile(file)
            val uri =  FileProvider.getUriForFile(context,"com.jb.search.fileprovider",file)

            val intent = Intent(Intent.ACTION_VIEW)

            when(type){
                FileType.PDF -> intent.setDataAndType(uri,"application/pdf")
                FileType.TXT ->intent.setDataAndType(uri,"text/plain")
                FileType.IMAGE ->intent.setDataAndType(uri,"image/*")
                FileType.NONE -> throw IllegalArgumentException("Intent not supported")
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Log.d(TAG, "openFile: $intent")
            return intent

        }




        @JvmStatic
        @Throws(Exception::class)
        fun listFilesInDirAndSubDir(dir:String):List<String>{
            val files_st = mutableListOf<String>()

            fun listf(directoryName: String?, files: MutableList<String>) {
                val directory = File(directoryName)

                // Get all files from a directory.
                val fList = directory.listFiles()
                if (fList != null) for (file in fList) {
                    files.add(file.absolutePath)
                     if (file.isDirectory) {
                        listf(file.absolutePath, files)
                     }
                }
            }
            listf(dir,files_st)
            return files_st
        }

    }



