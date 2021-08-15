package com.jb.search

import android.content.ContentValues.TAG
import android.os.Environment
import android.util.Log
import androidx.test.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jb.lucenecompose.SearchUtil.*
import com.jb.search.searchUtil.search.SearcherInitialise
import com.jb.search.utils.FileUpdatesInfo
import com.jb.search.utils.TextFilesFilter
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.Query

import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)


class InstrumentTest2 {
    @Test
    fun textOfPdfFile1() {

        val pdf = "/sdcard/Download/JUnit in Action - Unknown_432.pdf"
        val a = System.currentTimeMillis()
        val text = com.jb.search.utils.textOfPdfFile(File(pdf))
        println("reading took ${System.currentTimeMillis() - a} time")
    }

    @Test
    fun parallelIndexing() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val mOriginalDir: String = Environment.getExternalStorageDirectory().absolutePath
        val mDuplicateDir: String = context.filesDir.absolutePath + '/' + DUPLICATE
        val mIndexDir: String = context.filesDir.absolutePath + '/' + INDEX

        val allFiles = FileUpdatesInfo.duplicateFiles(mOriginalDir, mDuplicateDir,indexType = IndexType.DOCUMENT)

        if (File(mIndexDir).exists()) FileUtils.deleteDirectory(File(mIndexDir));
        val executorService = Executors.newFixedThreadPool(2)
        indexOneByOne(allFiles, mIndexDir, mDuplicateDir,executorService)

    }

    @Test
    fun numOfMaxThreads() {
        val p = Runtime.getRuntime().availableProcessors()
        println("threads are $p")
    }

    @Test
    fun listAllFiles() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val mOriginalDir: String = Environment.getExternalStorageDirectory().absolutePath

        val filesByApache: MutableCollection<File> = FileUtils.listFiles(
            File(mOriginalDir),
            TrueFileFilter.INSTANCE,
            TrueFileFilter.INSTANCE
        )
        val fileList = arrayListOf<String>()


    }

    @Test
    fun indexSearching() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val mIndexDir: String = context.filesDir.absolutePath + '/' + INDEX


        //val search
        val searcherInitialise = SearcherInitialise.getInstance(mIndexDir)

        val indexSearcher = searcherInitialise.indexSearcher

        val query: Query = searcherInitialise.parser.parse(QueryParser.escape("hello"))

        val startH = System.currentTimeMillis()

        val fHighlighter = searcherInitialise.fHighlighter
        val fieldQuery = fHighlighter.getFieldQuery(query)

        val start = System.currentTimeMillis()
        val hits = indexSearcher.search(query, 3)
        val end = System.currentTimeMillis()-start
        Log.d(TAG, "indexSearching: searching took $end")


        val hStart = System.currentTimeMillis()
        val filePaths = hits.scoreDocs.map { scoreDoc ->  indexSearcher.doc(scoreDoc.doc)["filepath"]}
        val snippets = hits.scoreDocs.map { scoreDoc -> fHighlighter.getBestFragment(fieldQuery,indexSearcher.indexReader,scoreDoc.doc,"contents",100) }

        filePaths.forEach { println(it) }
        snippets.forEach { println(it) }



        println("highlighting took ${System.currentTimeMillis()-hStart} milliseconds")

    }

    @Test
    fun duplicateFilesTest(){

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val originalPath = Environment.getExternalStorageDirectory().absolutePath
         val dupPath = context.filesDir .absolutePath + '/' + DUPLICATE
        if (File(dupPath).exists())FileUtils.deleteDirectory(File(dupPath))
        val start = System.currentTimeMillis()
        FileUpdatesInfo.duplicateFiles(originalPath,dupPath,indexType = IndexType.DOCUMENT)
        println("duplicating files took ${System.currentTimeMillis()-start} ms")
    }
    @Test
    fun checkFiles(){
       // FileUpdatesInfo.checkForNewFiles()
        val filaname = "/sdcard/Download/report.docx"
        val file  = File(filaname)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dupPath = context.filesDir .absolutePath + '/' + DUPLICATE
        File(dupPath + file.parentFile.path).mkdirs()
        File(dupPath+file.absolutePath).createNewFile()
    }
    @Test
    fun pdfFiles(){
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val originalPath = Environment.getExternalStorageDirectory().absolutePath
        val dupPath = context.filesDir .absolutePath + '/' + DUPLICATE

       // val pdfs = FileUtils.listFiles(File(originalPath),TrueFileFilter.INSTANCE,TrueFileFilter.INSTANCE).filter { it.extension.equals("pdf") or it.extension.equals("txt") }.size
        val pdfs = checkNewFilesOneByOne(originalPath,mDuplicateDir = dupPath).size

        duplicateOneByOne(originalPath,dupPath)

        val newPdfs = checkUnIndexedFilesOneByOne(originalPath,dupPath).size
        val textFilesFilter: TextFilesFilter = TextFilesFilter(IndexType.DOCUMENT)
        val pdfsExpected = FileUtils.listFiles(File(dupPath+"/${IndexType.DOCUMENT}"),textFilesFilter,TrueFileFilter.INSTANCE).size
        val pdfsInOriginalDir  = FileUtils.listFiles(File(originalPath),textFilesFilter,TrueFileFilter.INSTANCE).size
        println("documents are $pdfsExpected and original pdfs are $pdfsInOriginalDir and check for unindexed pdfs $newPdfs and new $pdfs")
    }


}