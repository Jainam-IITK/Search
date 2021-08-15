package com.jb.search.searchUtil

import android.content.ContentValues.TAG
import android.util.Log
import com.jb.search.CONTENTS
import com.jb.search.FILENAME
import com.jb.search.FILEPATH
import com.jb.search.INDEX
import com.jb.search.Model.SearchViewModel.SearchResults
import com.jb.search.searchUtil.search.SearcherInitialise
import com.jb.search.utils.SingletonHolder
import com.orhanobut.logger.Logger
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.*
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser
import org.apache.lucene.queryparser.classic.ParseException
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TopDocs
import org.apache.lucene.search.highlight.Highlighter
import org.apache.lucene.search.highlight.QueryScorer
import org.apache.lucene.search.highlight.TokenSources
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter
import org.apache.lucene.store.Directory
import org.apache.lucene.store.NIOFSDirectory
import java.io.File
import java.io.IOException
import java.lang.IllegalArgumentException


class FilesAndDirs(indexDir: String) {
    val analyzer: Analyzer = StandardAnalyzer()
    private val dir: Directory = NIOFSDirectory.open(File(indexDir).toPath())
    private val reader: DirectoryReader = DirectoryReader.open(dir)
    val indexSearcher: IndexSearcher = IndexSearcher(reader)
    companion object : SingletonHolder<FilesAndDirs, String>(::FilesAndDirs)
}

object Searcher {


    @Throws(IllegalArgumentException::class, IOException::class, ParseException::class)
    fun main(indexDir: String, query: String): SearchResults {
        return search(indexDir, query)
    }


    private var isSearchOngoing = true


    @Throws(IOException::class, ParseException::class)
    fun search(mIndexDir: String, q: String): SearchResults {

        isSearchOngoing = true



        //val search
        val searcherInitialise = SearcherInitialise.getInstance(mIndexDir)

        val indexSearcher = searcherInitialise.indexSearcher

        if (q.trim().isEmpty())return Pair(arrayListOf(), arrayListOf())
        val fields = arrayOf(CONTENTS, FILENAME, FILEPATH)
        val query: Query = MultiFieldQueryParser(fields, StandardAnalyzer()).parse(QueryParser.escape(q.trim()))


        val fHighlighter = searcherInitialise.fHighlighter
        val fieldQuery = fHighlighter.getFieldQuery(query)

        val hits = indexSearcher.search(query, 5)


        val filePaths: List<String> = hits.scoreDocs.map { scoreDoc ->  indexSearcher.doc(scoreDoc.doc)["filepath"]}
        val snippets: List<String> = hits.scoreDocs.map { scoreDoc -> fHighlighter.getBestFragment(fieldQuery,indexSearcher.indexReader,scoreDoc.doc,"contents",100) }

        filePaths.forEach { println(it) }
        snippets.forEach { println(it) }


        Logger.d("$filePaths")
        isSearchOngoing = false
        return Pair(filePaths, snippets)
    }

    private fun isIndexedDir(indexDir: String): Boolean {
        val path = "$indexDir/write.lock"
        Logger.d(path)
        val file = File(path)
        return file.exists()
    }

    fun isSearching(): Boolean = isSearchOngoing


    fun stopSearching() {
        isSearchOngoing = false
    }


}

