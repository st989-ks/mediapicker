package ru.mediapicker.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

val LocaleRu = Locale("ru", "RU")

class PermissionsModule(val context: Context) {
    private fun checkPermission(namePermission: String): Int {
        val inPermission = ContextCompat.checkSelfPermission(context, namePermission)
        logIRRealise("permission", namePermission, inPermission == GRANTED)
        return inPermission
    }

    /**
     * Get Permissions
     * */
    private val permissionCamera by lazy { checkPermission(CAMERA) }
    private val permissionAccessCoarseLocation by lazy { checkPermission(ACCESS_COARSE_LOCATION) }
    private val permissionAccessFineLocation by lazy { checkPermission(ACCESS_FINE_LOCATION) }
    private val permissionReadExternalStorage by lazy { checkPermission(READ_EXTERNAL_STORAGE) }
    private val permissionWriteExternalStorage by lazy { checkPermission(WRITE_EXTERNAL_STORAGE) }
    private val permissionReadMediaImages by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            checkPermission(READ_MEDIA_IMAGES) else GRANTED
    }
    private val permissionReadMediaVideo by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            checkPermission(READ_MEDIA_VIDEO) else GRANTED
    }
    private val permissionReadMediaAudio by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            checkPermission(READ_MEDIA_AUDIO) else GRANTED
    }

    /**
     * Check Permissions
     * */
    fun grantedCamera(): Boolean = permissionCamera == GRANTED
    fun grantedLocationCoarse(): Boolean = permissionAccessCoarseLocation == GRANTED
    fun grantedLocationFine(): Boolean = permissionAccessFineLocation == GRANTED
    fun grantedReadStorage(): Boolean = permissionReadExternalStorage == GRANTED
    fun grantedWriteStorage(): Boolean = permissionWriteExternalStorage == GRANTED

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun grantedReadMediaImages(): Boolean = permissionReadMediaImages == GRANTED

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun grantedReadMediaVideo(): Boolean = permissionReadMediaVideo == GRANTED

    fun listPermissionsNeededCameraImagesReadWriteStorage(): Array<String> {
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        listPermissionsNeeded.add(CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listPermissionsNeeded.add(READ_MEDIA_IMAGES)
            listPermissionsNeeded.add(READ_MEDIA_VIDEO)
        } else {
            listPermissionsNeeded.add(READ_EXTERNAL_STORAGE)
            listPermissionsNeeded.add(WRITE_EXTERNAL_STORAGE)
        }
        return listPermissionsNeeded.toTypedArray()
    }

    fun grantedImagesReadWriteStorage(): Boolean {
        val listPermissionsNeeded: MutableList<Int> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listPermissionsNeeded.add(permissionReadMediaImages)
            listPermissionsNeeded.add(permissionReadMediaVideo)
        } else {
            listPermissionsNeeded.add(permissionReadExternalStorage)
            listPermissionsNeeded.add(permissionWriteExternalStorage)
        }
        logD(listPermissionsNeeded)
        return !listPermissionsNeeded.contains(GRANTED)
    }

    fun checkCameraImagesReadWriteStorage(): Boolean {
        val listPermissionsNeeded: MutableList<Int> = ArrayList()
        listPermissionsNeeded.add(permissionCamera)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listPermissionsNeeded.add(permissionReadMediaImages)
            listPermissionsNeeded.add(permissionReadMediaVideo)
        } else {
            listPermissionsNeeded.add(permissionReadExternalStorage)
            listPermissionsNeeded.add(permissionWriteExternalStorage)
        }
        return !listPermissionsNeeded.contains(GRANTED)
    }

    fun listPermissionsNeededLocation(): Array<String> = arrayListOf(
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION
    ).toTypedArray()


    companion object {
        private const val GRANTED = PackageManager.PERMISSION_GRANTED
        private const val CAMERA = Manifest.permission.CAMERA
        private const val ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
        private const val ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        private const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
        private const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val READ_MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val READ_MEDIA_VIDEO = Manifest.permission.READ_MEDIA_VIDEO

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val READ_MEDIA_AUDIO = Manifest.permission.READ_MEDIA_AUDIO

        @Composable
        fun SetLocation(address: (List<Address>) -> Unit) {
            val context = LocalContext.current
            PermissionCoarseLocation { permitted ->
                if (permitted) {
                    getLocationServices(context) {
                        address.invoke(it)
                    }
                }
            }
        }

        @Composable
        fun permissionLauncher(returnResult: (Boolean) -> Unit) =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                returnResult(isGranted)
            }

        @Composable
        fun launchPermissionMultiple(response: (Boolean) -> Unit)
                : ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>> {
            return rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissions.entries.forEach {
                    logD("permissionMultiple", "${it.key} = ${it.value}")
                }
                response.invoke(permissions.all { (_, permissionValue) -> permissionValue == true })
            }
        }

        @Composable
        fun launchPermissionCameraAngGallery(response: (Boolean) -> Unit): () -> Unit {
            val context = LocalContext.current
            val permissionsModule = remember { PermissionsModule(context) }
            val launch = launchPermissionMultiple(response)
            return { launch.launch(permissionsModule.listPermissionsNeededCameraImagesReadWriteStorage()) }
        }

        @Composable
        fun PermissionCoarseLocation(returnResult: (Boolean) -> Unit) {
            val context = LocalContext.current
            val permissionsModule = remember { PermissionsModule(context) }
            val permission = launchPermissionMultiple { isGranted -> returnResult(isGranted) }
            LaunchedEffect(key1 = Unit, block = {
                val isPermission =
                    permissionsModule.grantedLocationCoarse() && permissionsModule.grantedLocationFine()
                if (!isPermission) {
                    permission.launch(permissionsModule.listPermissionsNeededLocation())
                } else {
                    returnResult(true)
                }
            })
        }

        @Composable
        fun galleryLauncher(
            uploadPhoto: (Uri) -> Unit
        ): () -> Unit {
            val context = LocalContext.current
            logE("grantedCamera", PermissionsModule(context).grantedCamera().toString())
            logE("grantedLocationCoarse",
                PermissionsModule(context).grantedLocationCoarse().toString())
            logE("grantedLocationFine", PermissionsModule(context).grantedLocationFine().toString())
            logE("grantedReadStorage", PermissionsModule(context).grantedReadStorage().toString())
            logE("grantedWriteStorage", PermissionsModule(context).grantedWriteStorage().toString())
            logE("grantedWriteStorage",
                PermissionsModule(context).grantedImagesReadWriteStorage().toString())
            logE("grantedWriteStorage")


//            if (PermissionsModule(context).grantedImagesReadWriteStorage()) return {
//                logE("grantedImagesReadWriteStorage false - > Cannot save the image!")
//            }
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val launchRun =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val uri = result.data?.data ?: run {
                            logIRRealise("Cannot save the image!")
                            return@rememberLauncherForActivityResult
                        }

                        val file = context.outputFile(uri) ?: run {
                            logIRRealise("Cannot save the image!")
                            return@rememberLauncherForActivityResult
                        }
                        uploadPhoto(file.toUri())
                    }
                }
            return {
                try {
                    launchRun.launch(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


        @Composable
        fun imagePickerLauncher(
            uploadPhoto: (Uri) -> Unit
        ): () -> Unit {
            val launchRun =
                rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { result ->
                    result?.let { uploadPhoto.invoke(it) }
                }
            return {
                try {
                    launchRun.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        @Composable
        fun cameraLauncher(
            imageUri: Uri,
            uploadPhoto: (Uri) -> Unit
        ): () -> Unit {
            val context = LocalContext.current
            val launchRun = rememberLauncherForActivityResult(
                ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    context.outputFile(imageUri)?.let { new ->
                        uploadPhoto(new.toUri())
                    } ?: run {
                        logE("Cannot save the image!")
                    }
                } else {
                    logE("Cannot save the image!")
                }
            }
            val launchGranted = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) launchRun.launch(imageUri) else {
                    logE("grantedCamera false - > Cannot save the image!")
                }
            }
            return {
                try {
                    if (!PermissionsModule(context).grantedCamera()) {
                        launchGranted.launch(CAMERA)
                    } else {
                        launchRun.launch(imageUri)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}


fun getLocationServices(context: Context, localGeo: (List<Address>) -> Unit): Boolean {
    val locationServices = LocationServices.getFusedLocationProviderClient(context)
    @SuppressLint("MissingPermission")
    if (PermissionsModule(context).grantedLocationFine() &&
        PermissionsModule(context).grantedLocationCoarse()) {
        try {
            locationServices.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    getGeoLocationAddress(latitude, longitude, context) {
                        localGeo.invoke(it)
                    }
                    logD("location ${latitude}: $longitude")
                }
                logD("location $location")
            }.addOnFailureListener {
                logE(it.stackTraceToString())
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    return false
}

private fun getGeoLocationAddress(
    latitude: Double,
    longitude: Double,
    context: Context,
    localGeo: (List<Address>) -> Unit
) = CoroutineScope(Dispatchers.Main).launch {
    val geocoder = Geocoder(context, LocaleRu)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
            localGeo.invoke(addresses)
        }
    } else {
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1) ?: listOf()
            localGeo.invoke(addresses)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
