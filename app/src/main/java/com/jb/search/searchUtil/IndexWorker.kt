package com.jb.lucenecompose.SearchUtil

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.jb.search.*
import com.jb.search.R
import com.jb.search.searchUtil.Indexer
import com.jb.search.utils.FileUpdatesInfo
import com.jb.search.utils.FileUtilFunctions
import kotlinx.coroutines.*

import java.io.File
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class IndexWorker(private val ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    private val channelId: String = "Index_Channel_id"
    private var notificationManager:NotificationManager = ctx.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
    private val name :String = "Indexing"
    private val builder = NotificationCompat.Builder(ctx, channelId)
        .setContentTitle("Indexing")
        .setTicker("title")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setOngoing(true)

    var maxProgress: Int = 0
    var currentProgress : Int = 0
    var executorService: ExecutorService = Executors.newFixedThreadPool(2)

    override suspend fun doWork() :Result{

//        makeStatusNotification("Indexing Starts", appContext)
        //WORK STARTS

        try {
            val notification = createNotification()

            val builder: ForegroundInfo = createForegroundInfo(notification)
            setForeground(builder)
//            repeat(10){
//                Log.d(TAG, "doWork: $it")
//            }
          //  checkPermissionRecur(ctx,ctx.)
             doIndexUpdate()
            executorService?.shutdownNow()
            executorService?.awaitTermination(1,TimeUnit.MINUTES)
            return Result.success()
        }catch (throwable:Throwable){
            Log.e(TAG, "doWork: error In Indexworker $throwable", )
            executorService?.shutdownNow()
            executorService?.awaitTermination(1,TimeUnit.MINUTES)
            return Result.failure()
        }

    }

   private suspend fun doIndexUpdate(){
        val mOriginalDir :String = Environment.getExternalStorageDirectory().absolutePath
        val mDuplicateDir :String = ctx.filesDir.absolutePath +'/'+ DUPLICATE
        val mIndexDir :String = ctx.filesDir.absolutePath + '/' + INDEX

        if (File(mDuplicateDir).exists()){

            val mFilesToDelete = checkForfilesToDeleteOneByOne(mOriginalDir,mDuplicateDir)
            val mNotIndexedFilesToAdd = checkUnIndexedFilesOneByOne(mOriginalDir,mDuplicateDir)
            val mNewFilesToAdd = checkNewFilesOneByOne(mOriginalDir,mDuplicateDir)
            val allFilesToIndex = mNotIndexedFilesToAdd + mNewFilesToAdd
           // maxProgress = allFilesToIndex.size
            Log.d(TAG, "doIndexUpdate: files to index $maxProgress")
           // notificationManager.notify(1,builder.setProgress(maxProgress,0,false).setContentText("$maxProgress").build())

            //deleting
          //  Indexer.main(mIndexDir,mFilesToDelete,false,mDuplicateDir,indexType = )
            //indexing
//            withContext(Dispatchers.Main){
//                IndexProgressInfo.mCurrentIndexedFiles.observeForever {
//                    GlobalScope.launch {
//                        increaseNotificationProgressByOne(it)
//                    }
//                }
//            }

            indexOneByOne(allFilesToIndex,mIndexDir,mDuplicateDir,executorService)

        }else{
            val allFiles = duplicateOneByOne(mOriginalDir,mDuplicateDir)
            indexOneByOne(allFiles,mIndexDir,mDuplicateDir,executorService)
        }
    }

    private fun increaseNotificationProgressByOne(progress: Int) {
        notificationManager.notify(1,builder.setContentText("${progress}/$maxProgress").setProgress(maxProgress,progress,false).build())
    }
    private fun createForegroundInfo(notification: Notification): ForegroundInfo {
        // Use a different id for each Notification.
        val notificationId = 1
        return ForegroundInfo(notificationId, notification)
    }

    /**
     * Create the notification and required channel (O+) for running work
     * in a foreground service.
     */
    private fun createNotification(): Notification {
        // This PendingIntent can be used to cancel the Worker.

        createNotificationChannel(channelId, name).also {
            builder.setChannelId(it.id)
        }

        return builder.build()
    }

    /**
     * Create the required notification channel for O+ devices.
     */
    private fun createNotificationChannel(channelId: String, name: String): NotificationChannel {
        return NotificationChannel(
            channelId, name, NotificationManager.IMPORTANCE_LOW
        ).also { channel ->
            notificationManager.createNotificationChannel(channel)
        }
    }


}
fun indexOneByOne(filesToIndex:List<File>,mIndexDir :String,mDuplicateDir:String,executorService: ExecutorService){
    try {
        val a= System.currentTimeMillis()


        for (indexType in IndexType.values()){

            if (indexType == IndexType.NONE)continue
            //  executorService?.execute {
            val indexTypeName : String = indexType.name
            Log.d(TAG, "indexOneByOne: indexing $indexType")
            val startTime = System.currentTimeMillis()

            val allFilesToIndex = filesToIndex.filter { FileUtilFunctions.indexTypeFromFile(file = it) == indexType }
            Indexer.main(index_dir = "$mIndexDir/$indexTypeName",allFilesToIndex,true, "$mDuplicateDir/$indexTypeName",indexType=indexType)
            Log.d(TAG, "indexOneByOne: indexing of $indexType files took ${System.currentTimeMillis()-startTime}")

            //  }
        }
        executorService.shutdown()
        executorService.awaitTermination(10,TimeUnit.MINUTES)

        Log.d(TAG, "indexOneByOne: indexing took ${System.currentTimeMillis()-a}")
    }catch (e:Exception){}


}


fun checkForfilesToDeleteOneByOne(mDir:String, mDuplicateDir:String):List<File>{
    val filesToDelete = arrayListOf<File>()
    IndexType.values()
        .filter { type->(type != IndexType.NONE ) }
        .forEach {indexType->
            val indexDupDir = "$mDuplicateDir/${indexType.name}"
            filesToDelete.addAll(FileUpdatesInfo.deleteFiles(mDir,indexDupDir,indexType))
        }
    return filesToDelete
}
fun checkUnIndexedFilesOneByOne(mDir:String, mDuplicateDir:String): List<File> {
    val mUnIndexedFiles = arrayListOf<File>()

    IndexType.values()
        .filter { type->(type!=IndexType.NONE) }
        .forEach {indexType->
            val indexDupDir = "$mDuplicateDir/${indexType.name}"
            mUnIndexedFiles.addAll(FileUpdatesInfo.checkUnindexedFiles(mDir,indexDupDir,indexType))
         }

    return mUnIndexedFiles
}

fun checkNewFilesOneByOne(mDir:String, mDuplicateDir:String): List<File>{
    val newFiles = arrayListOf<File>()
    IndexType.values()
        .filter { type->type!=IndexType.NONE }
        .forEach {indexType->
            val indexDupDir = "$mDuplicateDir/${indexType.name}"
            newFiles.addAll(FileUpdatesInfo.checkForNewFiles(mDir,indexDupDir,indexType))
        }
    return newFiles
}
fun duplicateOneByOne(mDir:String, mDuplicateDir:String): List<File>{
    val newFiles = arrayListOf<File>()
    IndexType.values()
        .filter { type->(type != IndexType.NONE ) or (type!= IndexType.IMAGE) }
        .forEach {indexType->
            val indexDupDir = "$mDuplicateDir/${indexType.name}"
            newFiles.addAll(FileUpdatesInfo.duplicateFiles(mDir,indexDupDir,indexType))
        }
    return newFiles
}


object IndexProgressInfo{
    var mCurrentIndexedFiles : MutableLiveData<Int> = MutableLiveData(0)
}