// settings.gradle(.kts) already hosts your repositories; nothing else needed here
plugins {
    // Stable AGP that pairs with Kotlin 1.9.23
    id("com.android.application") version "8.4.0" apply false
    id("com.android.library")    version "8.4.0" apply false

    // Kotlin 1.9.x is required for Compose compiler ≥ 1.5.x
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
}
