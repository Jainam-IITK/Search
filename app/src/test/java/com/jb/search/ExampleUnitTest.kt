package com.jb.search

import android.util.Log
import com.orhanobut.logger.Logger
import org.apache.commons.io.FilenameUtils
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.io.Reader
import kotlin.math.min


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        //val tika = Tika();
        val path= "/sdcard/Download/abstraction(1).pdf"
        val file = FilenameUtils.getExtension(path)
        println( "addition_isCorrect: $file")
        //val fulltext = tika.parseToString(file);


    }

    @Test
    fun for_loop(){
        val n = 238
        for (i in 1..n step 10){
            val minn = min(i+10,n)
            for (j in i..minn){
                println(j)
            }
        }
    }
    @Test
    fun filter(){
        val collection = mutableListOf("hey","man") as List<String>
        val me =  collection.filter { it != "man" }
        println(me)
        assertTrue(me.equals(mutableListOf("hey")))
    }
}