package ru.mediapicker.util

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.size.Size

@Composable
fun BoxImageLoad(
    modifier: Modifier = Modifier,
    image: Any?,
    alignment: Alignment = Alignment.Center,
    sizeToIntrinsics: Boolean = true,
    contentScale: ContentScale = ContentScale.Inside,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    @DrawableRes drawableError: Int? = null,
    @DrawableRes drawablePlaceholder: Int? = null,
    colorLoader: Color = MaterialTheme.colorScheme.primary,
    strokeWidthLoader: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    content: @Composable BoxScope.(error: Boolean) -> Unit = {},
) {

    val des = LocalDensity.current
    var sizeLoader by remember {
        mutableStateOf(0.dp)
    }

    var isLoading by remember(image) { mutableStateOf(false) }
    var isError by remember(image) { mutableStateOf(false) }
    val painter = rememberAsyncImagePainter(
        onLoading = {
            isLoading = true
            isError = false
        }, onError = {
            isLoading = false
            isError = true
        }, onSuccess = {
            isError = false
            isLoading = false
        }, model = ImageRequest.Builder(LocalContext.current).apply {
            size(Size.ORIGINAL)
            drawableError?.let { error(it) }
            drawablePlaceholder?.let { placeholder(it) }
            decoderFactory(SvgDecoder.Factory())
            data(image)
            crossfade(true)
        }.build())
    Box(
        modifier = modifier
            .onGloballyPositioned {
                sizeLoader = with(des) { (it.size.height / 2).toDp() }
            }
            .paint(
                painter = painter,
                alignment = alignment,
                sizeToIntrinsics = sizeToIntrinsics,
                contentScale = contentScale,
                colorFilter = colorFilter,
                alpha = alpha
            )
    ) {
        content.invoke(this, isError)
        if (isLoading) CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.Center)
                .size(sizeLoader),
            color = colorLoader,
            strokeWidth = strokeWidthLoader,
        )
    }
}