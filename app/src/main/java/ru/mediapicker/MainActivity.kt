package ru.mediapicker

import android.Manifest
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ru.mediapicker.ui.theme.MediapickerTheme
import ru.mediapicker.util.BoxImageLoad
import ru.mediapicker.util.PermissionsModule
import ru.mediapicker.util.makeUri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediapickerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {
                    Greeting()
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageUri by remember { mutableStateOf(context.makeUri()) }
    var uriImageLoad by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher =
        PermissionsModule.cameraLauncher(imageUri = imageUri, uploadPhoto = {
            uriImageLoad = it
        })

    val galleryLauncher = PermissionsModule.galleryLauncher(uploadPhoto = {
        uriImageLoad = it
    })


    val imagePickerLauncher = PermissionsModule.imagePickerLauncher(uploadPhoto = {
        uriImageLoad = it
    })


    val multiPermitting = PermissionsModule.launchPermissionMultiple(response = {

    })

    val launchPermission = {
        multiPermitting.launch(arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        ))
    }

    Box(modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = launchPermission) { Text("Get Permission") }
            Button(onClick = cameraLauncher) { Text("Get camera") }
            Button(onClick = galleryLauncher) { Text("Get galary") }
            Button(onClick = imagePickerLauncher) { Text("Get imagePicker") }

            uriImageLoad?.let{
                BoxImageLoad(
                    image = uriImageLoad
                )
            }
        }


    }
}
