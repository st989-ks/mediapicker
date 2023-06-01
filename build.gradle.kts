plugins {
    val kotlin = "1.8.21"
    val application = "8.0.1"
    val gsm = "4.3.14"

    id ("com.android.application") version application apply false
    id ("org.jetbrains.kotlin.android") version kotlin apply false
    id ("org.jetbrains.kotlin.plugin.serialization") version kotlin apply false
}
