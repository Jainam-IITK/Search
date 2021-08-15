package com.jb.search.utils

import com.jb.search.FileType
import com.jb.search.IndexType
import com.jb.search.utils.FileUtilFunctions.indexTypeFromFile
import com.jb.search.utils.FileUtilFunctions.typeOfFile
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.IOFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import java.io.File

class TextFilesFilter(private val indexType: IndexType) : IOFileFilter {
    override fun accept(file: File?): Boolean {
        if (file == null) return false
        return  indexTypeFromFile(file) ==indexType
    }
    override fun accept(file: File?, name: String?): Boolean {
        if (file == null) return false
        return  indexTypeFromFile(file) ==indexType
    }
}
object FileUpdatesInfo {


    private fun Iterable<File>.createDuplicateFilesInDuplicateDir(duplicateDir: String) {
        forEach { file ->
            val dupFile = File("$duplicateDir/${file.absolutePath}")
            val parentDirs = dupFile.parentFile
            parentDirs?.mkdirs()
            dupFile.createNewFile()
            FileUtils.writeStringToFile(dupFile, "no1", "UTF-8")
        }
    }

    //
    @JvmStatic
    @Throws(Exception::class)
    fun deleteFiles(dir: String, duplicateDir: String,indexType: IndexType): List<File> {
        if (!File(duplicateDir).exists()) duplicateFiles(dir, duplicateDir,indexType)

        val duplicateDirRoot = "$duplicateDir/$dir"
        val duplicateDirLen = duplicateDir.length

        val fileList: MutableCollection<File> =
            FileUtils.listFiles(File(duplicateDirRoot), TextFilesFilter(indexType), TrueFileFilter.INSTANCE)

        return fileList.filter { file ->
            val filePathInOriginalDir = file.absolutePath.substring(duplicateDirLen)
            !File(filePathInOriginalDir).exists()
        }.map {
            File(it.absolutePath.substring(duplicateDirLen))
        }
    }

    @JvmStatic
    @Throws(Exception::class)

    fun duplicateFiles(dir: String, duplicateDir: String,indexType: IndexType): List<File> {


        File("$duplicateDir/$dir").mkdirs()

        val fileList: MutableCollection<File> =
            FileUtils.listFiles(File(dir), TextFilesFilter(indexType), TrueFileFilter.INSTANCE)

        fileList.createDuplicateFilesInDuplicateDir(duplicateDir)


        return fileList as MutableList<File>
    }

    @JvmStatic
    @Throws(Exception::class)
    fun checkForNewFiles(dir: String, duplicateDir: String,indexType: IndexType): List<File> {
        if (!File(duplicateDir).exists()) duplicateFiles(dir, duplicateDir,indexType)

        val fileList = FileUtils.listFiles(File(dir), TextFilesFilter(indexType), TrueFileFilter.INSTANCE)

        val newFileList = fileList.filter { file ->
            !File("$duplicateDir/${file.absolutePath}").exists()
        }
        newFileList.createDuplicateFilesInDuplicateDir(duplicateDir)

        return newFileList
    }

    @JvmStatic
    @Throws(Exception::class)
    fun checkUnindexedFiles(dir: String, duplicateDir: String, indexType: IndexType): List<File> {
        if (!File(duplicateDir).exists()) duplicateFiles(dir, duplicateDir,indexType)

        deleteFiles(dir, duplicateDir,indexType)

        val duplicateDirRoot = "$duplicateDir/$dir"
        val duplicateDirLen = duplicateDir.length
        val fileList: MutableCollection<File> = FileUtils.listFiles(File(duplicateDirRoot), TextFilesFilter(indexType), TrueFileFilter.INSTANCE)
        println("unindexed files reporting: fileList size is ${fileList.size}")
        return fileList.filter { file ->
            val filePathInOriginalDir = file.absolutePath.substring(duplicateDirLen)
            File(filePathInOriginalDir).exists() and FileUtils.readFileToString(file, "UTF-8").equals("no1")
        }.map { file ->
            val filePathInOriginalDir = file.absolutePath.substring(duplicateDirLen)
            File(filePathInOriginalDir)
        }

    }

    @JvmStatic
    fun indexedFileUpdate(file: File, duplicateDir: String) {
        val dupFilePath = "$duplicateDir/${file.absolutePath}"
        FileUtils.writeStringToFile(File(dupFilePath), "yes", "UTF-8")
    }


}







