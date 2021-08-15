package com.jb.search.view.searchFragment.searchListAdapter

import android.widget.ImageView
import coil.load
import com.jb.search.FileType
import com.jb.search.R
import com.jb.search.utils.FileUtilFunctions
import java.io.File

fun ImageView.setImageImage(filePath: String) {
    when (FileUtilFunctions.typeOfFile(filePath)){
        FileType.PDF ->this.load(R.drawable.ic_pdf)
        FileType.TXT ->this.load(R.drawable.ic_txt)
        FileType.IMAGE ->this.load(File(filePath))
        else -> this.load(R.drawable.ic_launcher_foreground)
    }
}
