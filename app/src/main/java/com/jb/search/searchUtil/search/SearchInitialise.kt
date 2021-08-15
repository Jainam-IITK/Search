package com.jb.search.searchUtil.search

import com.jb.search.CONTENTS
import com.jb.search.IndexType
import com.jb.search.utils.SingletonHolder
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.MultiReader
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter
import org.apache.lucene.store.NIOFSDirectory
import java.io.File
import java.lang.Exception

class SearcherInitialise(mIndexDir:String){
    private val dirs = IndexType.values()
        .filter { indexType -> indexType!= IndexType.NONE }
        .map{ indexType ->
            val typeName = indexType.name
            val indexDir = "$mIndexDir/$typeName"
            NIOFSDirectory.open(File(indexDir).toPath())
        }
    private var readers: List<DirectoryReader> = dirs.mapNotNull { dir ->
        try {
            DirectoryReader.open(dir)
        } catch (e: Exception) {
            null
        }
    }

    private val multiReader = MultiReader(readers.toTypedArray(),false)
    val indexSearcher = IndexSearcher(multiReader)
    val fHighlighter = FastVectorHighlighter()
    val parser = QueryParser(CONTENTS, StandardAnalyzer())

    companion object : SingletonHolder<SearcherInitialise, String>(::SearcherInitialise)
}