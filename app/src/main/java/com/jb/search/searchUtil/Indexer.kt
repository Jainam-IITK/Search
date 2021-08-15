package com.jb.search.searchUtil

import android.util.Log
import com.jb.lucenecompose.SearchUtil.IndexProgressInfo
import com.jb.search.*
import com.jb.search.utils.FileUpdatesInfo.indexedFileUpdate
import com.jb.search.utils.FileUtilFunctions.typeOfFile
import com.jb.search.utils.SingletonHolder
import com.jb.search.utils.TextFilesFilter
import com.jb.search.utils.textOfFile
import org.apache.lucene.analysis.*
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.*
import org.apache.lucene.document.Field.Store
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.store.Directory
import org.apache.lucene.store.LockFactory
import org.apache.lucene.store.NIOFSDirectory
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.lang.Integer.max
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.Exception


class Writer(indexRoot: String){
    val analyzer: Analyzer = StandardAnalyzer()

    val mainIndexDir: Directory = NIOFSDirectory.open(File(indexRoot).toPath())
    val mainIndexWriterConfig = IndexWriterConfig(analyzer).also {mainIndexWriterConfig->
        mainIndexWriterConfig.openMode = IndexWriterConfig.OpenMode.CREATE_OR_APPEND
        mainIndexWriterConfig.ramBufferSizeMB = 128.toDouble()
    }

    var indexWriter:IndexWriter = ThreadedIndexWriter(mainIndexDir,analyzer,true,4,16)

    companion object : SingletonHolder<Writer, String>(::Writer)

}
class Indexer(indexRoot: String) : AutoCloseable {
    private val analyzer: Analyzer = StandardAnalyzer()
    private val myWriter = Writer.getInstance(indexRoot)
    init {
        //writer = ThreadedIndexWriter(mainIndexDir,analyzer,true,5,16)
        Log.d("TAG", "index is open: ")

    }


    companion object {
        //bool Index - > True for index and false for delete
        @Throws(Exception::class)
        fun main(index_dir: String, files: List<File>, toIndex: Boolean, dupDir: String,indexType: IndexType) {

            if (files.isEmpty()) return
            val directory = File(index_dir)
            if (!directory.exists()) directory.mkdirs()

            val start = System.currentTimeMillis()

            val indexer = Indexer(index_dir)
            Log.d("TAG", "main: reached here")

            var numIndexed = 0
             indexer.use { indexer ->
                if (toIndex) {
                    val listOfPaths = files.map { it.absolutePath }
                    indexer.indexFiles(listOfPaths, TextFilesFilter(indexType), dupDir,index_dir=index_dir)
                } else {
                    indexer.deleteFiles(files, TextFilesFilter(indexType))
                }
            }
            val end = System.currentTimeMillis()

            if (toIndex) println("Done Indexing") else println("Done deleting")
            println(
                numIndexed.toString() + " files took "
                        + (end - start) + " milliseconds"
            )
        }

    }


    @Throws(IOException::class)
    override fun close() {
        Log.d("TAG", "close: ")
        myWriter.indexWriter.close()
    }

    /* We input absolute paths of file  and A filter of our choice and
     * Indexer indexes all the paths given and store them in the Indexes
     * */
    @Throws(Exception::class)
    fun indexFiles(files: List<String>, filter: FileFilter?, dupDir: String,index_dir: String): Int? {
            try {

                if (files.isEmpty()) return 0

                //Update the progress for

                //IndexProgressInfo.mMaxFilesToIndex.postValue(files.size)

                val es: ExecutorService = Executors.newFixedThreadPool(max(Runtime.getRuntime().availableProcessors()/2,4))


                for ((idx,filePath) in files.withIndex()) {
//                if (counter == 20*counter2) {
//                    writer.commit()
//                    counter2++
//                    counter = 0
//
//                }
//                counter++
                    if ((idx.rem(50) == 0) and (idx != 0))myWriter.indexWriter.commit()
                    try {
                    es.execute {
                        val f = File(filePath)
                        if (!f.isDirectory && !f.isHidden && f.exists() &&
                            f.canRead() && (filter == null || filter.accept(f))
                        ) {
                                // println("indexing starts")
                                indexFile(f,indexRoot = index_dir)

                                IndexProgressInfo.mCurrentIndexedFiles.postValue(IndexProgressInfo.mCurrentIndexedFiles.value?.plus(1))

                                indexedFileUpdate(f, dupDir)

                        }
                    }
                    }catch (e:Exception){
                        Log.e("TAG", "indexFiles: error while indexing $e", )
                    }
                }


                es.shutdown()
                es.awaitTermination(10, TimeUnit.MINUTES)


                //Add a functionality to delete if file no longer exists
                return myWriter.indexWriter.docStats.numDocs
            }catch (e:Exception){
                Log.d("TAG", "indexFiles: yey i got an exception")
            }finally {
                myWriter.indexWriter.commit()
                myWriter.indexWriter.close()
            }
             return 0
    }

    @Throws(Exception::class)
    fun indexFile(file: File,indexRoot: String) {
        val fulltext = textOfFile(file)
        println("Indexing " + file.canonicalPath)
        val document = Document()
        val filename = StringField(FILENAME, file.name, Store.YES)

        val indexableFieldType =FieldType(TextField.TYPE_STORED)
        indexableFieldType.setStoreTermVectors(true)
        indexableFieldType.setStoreTermVectorOffsets(true)
        indexableFieldType.setStoreTermVectorPositions(true)
        indexableFieldType.setStoreTermVectorPayloads(true)

        val contents = Field(CONTENTS,fulltext,indexableFieldType)
        val filePath = StringField(FILEPATH, file.absolutePath, Store.YES)

        document.add(filename)
        document.add(contents)
        document.add(filePath)
        Writer.getInstance(indexRoot).indexWriter.addDocument(document)
       // myWriter.indexWriter.addDocument(document)
    }



    @Throws(Exception::class)
    fun deleteFiles(filePaths: List<File>, filter: FileFilter?): Int? {
        if (filePaths.isEmpty()) return 0
        for (f in filePaths) {

            if (!f.exists() && (filter == null || filter.accept(f))) {
                deleteFile(f.absolutePath)
            }
        }
        //Add a functionality to delete if file no longer exists
        return myWriter.indexWriter.docStats?.numDocs
    }

    @Throws(Exception::class)
    private fun deleteFile(filePath: String?) {
        //delete indexes for a file
        myWriter.indexWriter.deleteDocuments(Term("filepath", filePath))
    }







}
