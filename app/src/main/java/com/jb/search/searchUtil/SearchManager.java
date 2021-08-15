package com.jb.search.searchUtil;

import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.LiveIndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;



/** Utility class to get/refresh searchers when you are
 *  using multiple threads. */

public class SearchManager {

    private IndexSearcher currentSearcher;                         //A
    private IndexWriter writer;

    public SearchManager(Directory dir) throws IOException {
        DirectoryReader reader = DirectoryReader.open(dir);
        currentSearcher = new IndexSearcher(reader);  //B
        warm(currentSearcher);
    }

    public  SearchManager(IndexWriter writer) throws IOException { //2
        this.writer = writer;
        DirectoryReader reader =  DirectoryReader.open(writer.getDirectory());
        currentSearcher = new IndexSearcher(reader);      //C
        warm(currentSearcher);
        LiveIndexWriterConfig indexWriterConfig = writer.getConfig();
        indexWriterConfig.setMergedSegmentWarmer(                                     // 3
                leafReader -> {
                    SearchManager.this.warm(new IndexSearcher(reader));         // 3
                });
    }

    public void warm(IndexSearcher searcher)    // D
            throws IOException                        // D
    { }                                          // D

    private boolean reopening;

    private synchronized void startReopen()
            throws InterruptedException {
        while (reopening) {
            wait();
        }
        reopening = true;
    }

    private synchronized void doneReopen() {
        reopening = false;
        notifyAll();
    }

    public void maybeReopen(DirectoryReader directoryReader)                      //E
            throws InterruptedException,                 //E
            IOException {                         //E

        startReopen();

        try {
            final IndexSearcher searcher = get();
            try {
               IndexReader newReader = DirectoryReader.openIfChanged(directoryReader);
                if (newReader != currentSearcher.getIndexReader()) {
                    IndexSearcher newSearcher = new IndexSearcher(newReader);
                    if (writer == null) {
                        warm(newSearcher);
                    }
                    swapSearcher(newSearcher);
                }
            } finally {
                release(searcher);
            }
        } finally {
            doneReopen();
        }
    }

    public synchronized IndexSearcher get() {                      //F
        currentSearcher.getIndexReader().incRef();
        return currentSearcher;
    }

    public synchronized void release(IndexSearcher searcher)       //G
            throws IOException {
        searcher.getIndexReader().decRef();
    }

    private synchronized void swapSearcher(IndexSearcher newSearcher)
            throws IOException {
        release(currentSearcher);
        currentSearcher = newSearcher;
    }

    public void close() throws IOException {
        swapSearcher(null);
    }
}

