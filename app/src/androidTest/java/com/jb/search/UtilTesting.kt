package com.jb.search

import android.content.ContentValues.TAG
import android.os.Environment
import android.os.FileUtils
import android.util.Log
import androidx.test.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jb.search.utils.textOfFile
import org.junit.Test
import org.junit.runner.RunWith
import java.io.*
import java.lang.Exception
import java.nio.channels.FileChannel
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@RunWith(AndroidJUnit4::class)
class UtilTesting {

    private fun unzip(zipFilePath: String, destDir: String) {
        val dir = File(destDir)
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs()
        val fis: FileInputStream
        //buffer for read and write data to file
        val buffer = ByteArray(1024)
        try {
            fis = FileInputStream(zipFilePath)
            val zis = ZipInputStream(fis)
            var ze = zis.nextEntry
            while (ze != null) {
                val fileName = ze.name
                val newFile = File(destDir + File.separator.toString() + fileName)
                System.out.println("Unzipping to " + newFile.absolutePath)
                //create directories for sub directories in zip
                File(newFile.parent).mkdirs()
                val fos = FileOutputStream(newFile)
                var len: Int
                while (zis.read(buffer).also { len = it } > 0) {
                    fos.write(buffer, 0, len)
                }
                fos.close()
                //close this ZipEntry
                zis.closeEntry()
                ze = zis.nextEntry
            }
            //close last ZipEntry
            zis.closeEntry()
            zis.close()
            fis.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    @Throws(IOException::class)
    fun renameFile(toBeRenamed: File, new_name: String?) {
        //need to be in the same path
        val fileWithNewName = File(toBeRenamed.parent, new_name)
        if (fileWithNewName.exists()) {
            throw IOException("file exists")
        }
        // Rename file (or directory)
        val success = toBeRenamed.renameTo(fileWithNewName)
        if (!success) {
            // File was not successfully renamed
        }
    }

    /**
     * copy file from source to destination
     *
     * @param src source
     * @param dst destination
     * @throws java.io.IOException in case of any problems
     */
    @Throws(IOException::class)
    fun copyFile(src: File?, dst: File?) {
        val inChannel: FileChannel? = FileInputStream(src).channel
        val outChannel: FileChannel? = FileOutputStream(dst).channel
        try {
            inChannel?.transferTo(0, inChannel.size(), outChannel)
        } finally {
            inChannel?.close()
            outChannel?.close()
        }
    }
    @Test
    fun unzipDocxTest(){


        val file = File("/sdcard/Download/report.docx")
        val originaldir = file.parentFile.absolutePath

        val zipFile = File("$originaldir/${file.nameWithoutExtension}.zip")
        zipFile.createNewFile()
        copyFile(file,zipFile)

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val unzipDestination = context.filesDir.absolutePath


        unzip(zipFile.absolutePath,unzipDestination)

        //unzipped
        val documentPath = "$unzipDestination/word/document.xml"
        val txtfile = File("$unzipDestination/word/document.txt")
        txtfile.createNewFile()
        copyFile(File(documentPath),txtfile)

        val filetext =  textOfFile(txtfile)
        val pattern = "<w:t>(.*?)</w:t>".toRegex()
        val texts = pattern.findAll(filetext).mapNotNull{ pattern->pattern.value.slice(IntRange(5,pattern.value.length-7)) }.reduce { acc, s -> "$acc $s" }
        println(texts)

        //delete
        //org.apache.commons.io.FileUtils.deleteDirectory(File(unzipDestination))
        org.apache.commons.io.FileUtils.delete(zipFile)
        org.apache.commons.io.FileUtils.delete(txtfile)


    }
}