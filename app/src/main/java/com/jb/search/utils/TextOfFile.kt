package com.jb.search.utils

import UnzipUtils.unzip
import android.content.ContentValues.TAG
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.jb.search.FileType
import com.jb.search.utils.FileUtilFunctions.typeOfFile
import com.orhanobut.logger.Logger
import org.apache.commons.io.FileUtils
import java.io.File
import android.graphics.BitmapFactory

import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions

import android.graphics.Bitmap
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils.copyFile
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream
import kotlin.math.min


fun textOfFile(file: File):String{
           val type = typeOfFile(file)
           return when(type){
                FileType.PDF -> textOfPdfFile(file).toString()

                FileType.TXT -> textOfTxtFile(file)
                FileType.IMAGE -> textOfImageFile(file)
                else-> ""
            }

       }


private fun textOfTxtFile(file: File) :String{
           if (file==null) return " "
           return FileUtils.readFileToString(file,"UTF-8")?:" "
       }

fun textOfPdfFile(file: File):StringBuilder{

          try {
              val stringBuilder: StringBuilder = StringBuilder("");
              val reader: PdfReader = PdfReader(file.path)
              val pdfDocument: PdfDocument = PdfDocument(reader)
              val n: Int = pdfDocument.numberOfPages;

              for (i in 1..n step 30){
                  val minn = min(i+30,n)
                      for (j in i..minn){
                          val page = pdfDocument.getPage(j);
                          val text = PdfTextExtractor.getTextFromPage(page)

                          stringBuilder.append(text);
                      }
              }


              reader.close()
              pdfDocument.close();
              return stringBuilder
          }catch (e:Exception){
              Log.d("TextOfPdfFile", "textOfPdfFile: $e")
              Logger.e("Error in reading pdf file : check fun textOfPdfFile(file: File) in TextOfFile ",e)
          }
           return java.lang.StringBuilder("")
       }

private fun textOfImageFile(file: File):String {
           var resultText :String= " "
           try {
               val options = BitmapFactory.Options()
               options.inSampleSize = 2
               options.inJustDecodeBounds = false
               val bitmap: Bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
               val image = InputImage.fromBitmap(bitmap, 0)
               val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
               var isRecognised = false

               recognizer.process(image)
                   .addOnSuccessListener { visionText ->
                       // Task completed successfully
                       // ...
                       resultText = visionText.text
                       isRecognised = true
                   }.addOnFailureListener {
                       isRecognised = true
                   }

               runBlocking {
                   while (!isRecognised){
                       delay(300)
                   }
               }
               return resultText

           }catch (e:java.lang.Exception){
               Log.e(TAG, "textOfImageFile: $e" )
               return " "
           }


   }


private fun textOfDocxFile(file: File):String{
    val originaldir = file.parentFile.absolutePath

    val zipFile = File("$originaldir/${file.nameWithoutExtension}.zip")
    zipFile.createNewFile()
    copyFile(file,zipFile)

    val unzipDestination = originaldir + "/${file.nameWithoutExtension}"
    File(unzipDestination).mkdir()

    unzip(zipFile.absolutePath,unzipDestination)

    //unzipped
    val documentPath = "$unzipDestination/word/document.xml"
    val txtfile = File("$unzipDestination/word/document.txt")
    txtfile.createNewFile()
    copyFile(File(documentPath),txtfile)

    val filetext =  textOfFile(txtfile)
    val pattern = "<w:t>(.*?)</w:t>".toRegex()
    val texts = pattern.findAll(filetext).mapNotNull{ pattern->pattern.value.slice(IntRange(5,pattern.value.length-7)) }.reduce { acc, s -> "$acc $s" }

    //delete
    //org.apache.commons.io.FileUtils.deleteDirectory(File(unzipDestination))
    FileUtils.delete(zipFile)
    FileUtils.delete(txtfile)
    return texts
}

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