package ru.mediapicker.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

fun Context.outputFile(uri: Uri): File? {
    val input = this.contentResolver.openInputStream(uri) ?: return null
    val dataName = System.currentTimeMillis().toTimeStringForeFile()
    val outputFile = this.filesDir.resolve("${dataName}_new_picture.jpg")
    input.copyTo(outputFile.outputStream())
    input.close()
    return outputFile
}

fun File.getUriForFile(context: Context): Uri {
    return FileProvider.getUriForFile(context,
        "${context.applicationInfo.packageName}.fileprovider",
        this)
}

@Throws(IOException::class)
fun Context.createImageFile(): File {
    // Create an image file name
    val timeStamp = Date().toTimeStringForeFile()
    val storageDir = cacheDir
    return File.createTempFile(
        "JPEG_${timeStamp}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
    ).apply {
        deleteOnExit()
    }
}


fun Context.makeUri() = this.createImageFile().getUriForFile(this)

fun Date.toTimeStringForeFile(): String =
    SimpleDateFormat("yyyyMMdd_HHmmss", LocaleRu).format(this)

fun Long.toTimeStringForeFile(): String =
    SimpleDateFormat("yyyyMMdd_HHmmss", LocaleRu).format(this)
