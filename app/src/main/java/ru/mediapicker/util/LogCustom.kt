package ru.mediapicker.util

import android.util.Log
import ru.mediapicker.BuildConfig


private const val mainStr = "AXAS ${BuildConfig.APPLICATION_ID}"
private val SHOW_LOG = BuildConfig.DEBUG

fun logI(vararg any: Any?) {
    if (!SHOW_LOG) return
    Log.i(mainStr, any.toList().toString())
}

fun logE(vararg any: Any?) {
    if (!SHOW_LOG) return
    Log.e(mainStr, any.toList().toString())
}

fun logD(vararg any: Any?) {
    if (!SHOW_LOG) return
    Log.d(mainStr, any.toList().toString())
}

fun logW(vararg any: Any?) {
    if (!SHOW_LOG) return
    Log.w(mainStr, any.toList().toString())
}

fun logV(vararg any: Any?) {
    if (!SHOW_LOG) return
    Log.v(mainStr, any.toList().toString())
}

fun logIRRealise(vararg any: Any?) {
    Log.i(mainStr, any.toList().toString())
}