//package com.jb.search
//
//import android.content.ContentValues.TAG
//import android.os.Environment
//import android.util.Log
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.platform.app.InstrumentationRegistry
//import com.jb.lucenecompose.SearchUtil.indexOneByOne
//import com.jb.search.searchUtil.Searcher
//import com.jb.search.searchUtil.ThreadedIndexWriter
//import com.jb.search.utils.FileUpdatesInfo
//import com.jb.search.utils.textOfFile
//import com.jb.search.utils.textOfPdfFile
//import kotlinx.coroutines.*
//import org.apache.commons.io.FileUtils
//import org.apache.lucene.analysis.standard.StandardAnalyzer
//import org.apache.lucene.document.Document
//import org.apache.lucene.document.FieldType
//import org.apache.lucene.document.TextField
//import org.apache.lucene.index.DirectoryReader
//import org.apache.lucene.index.IndexWriterConfig
//import org.apache.lucene.search.IndexSearcher
//import org.apache.lucene.store.Directory
//import org.apache.lucene.store.FSDirectory
//
//import org.junit.Test
//import org.junit.runner.RunWith
//import java.io.File
//import java.io.FileInputStream
//import java.lang.Exception
//import java.io.IOException
//import java.io.InputStream
//import java.lang.StringBuilder
//import org.apache.lucene.index.Term
//
//import org.apache.lucene.search.TermQuery
//import org.apache.lucene.search.TopDocs
//import org.apache.lucene.search.highlight.Highlighter
//import org.apache.lucene.search.highlight.QueryScorer
//import org.apache.lucene.search.highlight.SimpleSpanFragmenter
//import org.apache.lucene.search.highlight.TokenSources
//import org.apache.lucene.store.NIOFSDirectory
//
//
///**
// * Instrumented test, which will execute on an Android device.
// *
// * See [testing documentation](http://d.android.com/tools/testing).
// */
//@RunWith(AndroidJUnit4::class)
//class ExampleInstrumentedTest {
//    @Test
//    fun jodTest(){
//        try {
//            val file = File("/sdcard/Download/report.docx")
//            val fis = FileInputStream(file)
//            fis.use { fis ->
//              //  val file = XWPFDocument(OPCPackage.open(fis))
//             //   val ext = XWPFWordExtractor(file)
//              //  System.out.println(ext.getText())
//            }
//        } catch (e: Exception) {
//            println(e)
//        }
//
//
//    }
//
//    @Test
//    fun xmlToText(){
//        val file = File("/sdcard/Download/document.xml")
//        val fis = FileInputStream(file)
//        val stream: InputStream = fis
//        var sting = ""
//        try {
//            val buffer = ByteArray(stream.available())
//            stream.read(buffer)
//            stream.close()
//            sting =  String(buffer)
//        } catch (e: IOException) {
//            // Error handling
//            println(e)
//        }
//
//
//        Log.i("xmlto",  sting)
//
//        val start = "<w:t>"
//        val end = "</w:t>"
//        var startIndex = 0
//        var endIndex = 0;
//        val stringBuilder = StringBuilder("")
//        while (true){
//            startIndex = sting.indexOf(start, startIndex);
//            endIndex = sting.indexOf(end,startIndex+start.length)
//
//            if (startIndex != -1)
//            {
//                stringBuilder.append(sting.substring(startIndex=startIndex + start.length,endIndex=endIndex))
//                stringBuilder.append(' ')
//                startIndex += start.length;
//                endIndex += end.length
//            }
//            else {
//                break;
//            }
//        }
//        Log.i(TAG, "xmlToText: $stringBuilder")
//    }
//
////    @Test
////    fun dupFile(){
////        val context = InstrumentationRegistry.getInstrumentation().targetContext
////        val indexPath = context.filesDir .absolutePath + '/' + INDEX
////        val dupPath = context.filesDir .absolutePath + '/' + DUPLICATE
////
////        FileUtils.deleteDirectory(File(indexPath))
////        val files = FileUpdatesInfo.duplicateFiles(Environment.getExternalStorageDirectory().absolutePath,dupPath)
////       // Indexer.main(indexPath,files,true)
////
////
////    }
//    @Test
//    fun readcpp(){
//        val file = File("/sdcard/Download/main.cpp")
//    }
//
//    @Test
//    fun searcher(){
//        val context = InstrumentationRegistry.getInstrumentation().targetContext
//        val indexPath = context.filesDir .absolutePath + '/' + INDEX
//
//        Searcher.main(indexPath,"so/")
//    }
//
//    @Test
//    fun textOfFile1(){
//
//        val pdf = "/sdcard/Download/JUnit in Action - Unknown_432.pdf"
//        val a = System.currentTimeMillis()
//        val text= textOfPdfFile(File(pdf))
//        println("reading took ${System.currentTimeMillis()-a} time")
//        println(text)
//
//
//    }
//
//    @Test
//    fun duplicateFiles(){
//        val context = InstrumentationRegistry.getInstrumentation().targetContext
//        val dir = Environment.getExternalStorageDirectory().absolutePath
//        val dupPath = context.filesDir .absolutePath + '/' + DUPLICATE
//        FileUpdatesInfo.duplicateFiles(dir,dupPath)
//    }
//
//    @Test
//    fun searchHighlighter(){
//        val context = InstrumentationRegistry.getInstrumentation().targetContext
//        val indexPath = context.filesDir .absolutePath + '/' + INDEX
//        val dir: Directory = FSDirectory.open(File(indexPath).toPath())
//        val FIELD_ID = "contents"
//        val reader: DirectoryReader = DirectoryReader.open(dir)
//        val indexSearcher = IndexSearcher(reader)
//        val query = TermQuery(Term(FIELD_ID, "action"))
//        val hits: TopDocs = indexSearcher.search(query, 10)
//        val scorer = QueryScorer(query, FIELD_ID)
//        val highlighter = Highlighter(scorer)
//        highlighter.textFragmenter = SimpleSpanFragmenter(scorer)
//        val analyzer = StandardAnalyzer()
//        for (sd in hits.scoreDocs){
//            val doc: Document = indexSearcher.doc(sd.doc)
//            val title = doc.get(FIELD_ID)
//            val stream = TokenSources.getAnyTokenStream(indexSearcher.indexReader,sd.doc,FIELD_ID,doc,analyzer)
//            val fragment = highlighter.getBestFragment(stream,title)
//            Log.d(TAG, "searchHighlighter: $fragment")
//        }
//    }
//    @Test
//    fun imageToText(){
//        val path = "/storage/emulated/0/Download/Portal/dan-rogers-lMIvz5b1vRo-unsplash.jpg"
//        val file = File(path)
//
//    }
//
//    @Test
//    fun coroutineTest(){
//        runBlocking {
//            for (i in 0..5){
//               launch{
//                    if (i==5) Log.d(TAG, "coro: $i")
//                    else{
//                        delay(5000L-i*100L)
//                        Log.d(TAG, "coro: $i")
//                    }
//
//                }
//            }
//        }
//    }
//
//    @Test
//    fun parallelIndexing(){
//        val context = InstrumentationRegistry.getInstrumentation().targetContext
//        val mOriginalDir :String = Environment.getExternalStorageDirectory().absolutePath+"/Download"
//        val mDuplicateDir :String = context.filesDir.absolutePath +'/'+ DUPLICATE
//        val mIndexDir :String = context.filesDir.absolutePath + '/' + INDEX
//        val allFiles =   FileUpdatesInfo.duplicateFiles(mOriginalDir,mDuplicateDir)
//
//        if (File(mIndexDir).exists()) FileUtils.deleteDirectory(File(mIndexDir));
//        indexOneByOne(allFiles,mIndexDir,mDuplicateDir)
//        val tenSeconds = 10000L
//        var timer = 0;
//        while (timer < 7*tenSeconds){
//            runBlocking {
//                delay(tenSeconds)
//            }
//        }
//    }
//
//}