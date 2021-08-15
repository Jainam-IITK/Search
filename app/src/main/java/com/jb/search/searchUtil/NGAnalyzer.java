package com.jb.search.searchUtil;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.io.IOException;
import java.io.Reader;

public final class NGAnalyzer extends StopwordAnalyzerBase {
    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;
    private int maxTokenLength;

    public NGAnalyzer(CharArraySet stopWords) {
        super(stopWords);
        this.maxTokenLength = 255;
    }

    public NGAnalyzer() {
        this(CharArraySet.EMPTY_SET);
    }


    public void setMaxTokenLength(int length) {
        this.maxTokenLength = length;
    }

    public int getMaxTokenLength() {
        return this.maxTokenLength;
    }

    protected TokenStreamComponents createComponents(String fieldName) {
        StandardTokenizer src = new StandardTokenizer();
        src.setMaxTokenLength(this.maxTokenLength);
        TokenStream tok = new LowerCaseFilter(src);

        return new TokenStreamComponents((r) -> {
            src.setMaxTokenLength(this.maxTokenLength);
            src.setReader(r);
        }, tok);
    }

    protected TokenStream normalize(String fieldName, TokenStream in) {
        return new LowerCaseFilter(in);
    }
}
