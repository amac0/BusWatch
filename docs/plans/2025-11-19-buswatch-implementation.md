# BusWatch Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a Wear OS app showing real-time London bus arrivals for nearby stops using TfL API

**Architecture:** MVVM with Repository pattern, Jetpack Compose for Wear OS, Hilt for DI, Retrofit for networking

**Tech Stack:** Kotlin, Wear OS 3.0+, Jetpack Compose, Hilt, Retrofit, DataStore, Play Services Location

---

## Task 1: Android Wear OS Project Setup

**Files:**
- Create: `build.gradle.kts` (project root)
- Create: `app/build.gradle.kts`
- Create: `settings.gradle.kts`
- Create: `gradle.properties`
- Create: `app/src/main/AndroidManifest.xml`

**Step 1: Create project-level build.gradle.kts**

```kotlin
// ABOUTME: Project-level build configuration for BusWatch Wear OS app
// ABOUTME: Defines plugin versions and repositories for the entire project
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20" apply false
}
```

**Step 2: Create settings.gradle.kts**

```kotlin
// ABOUTME: Gradle settings defining plugin repositories and included modules
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BusWatch"
include(":app")
```

**Step 3: Create gradle.properties**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
```

**Step 4: Create app/build.gradle.kts**

```kotlin
// ABOUTME: App-level build configuration with dependencies and SDK versions
// ABOUTME: Configures Wear OS app with Compose, Hilt, Retrofit, and required libraries
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    kotlin("kapt")
}

android {
    namespace = "com.buswatch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.buswatch"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val apiKey = project.findProperty("tfl.api.key") as String? ?: ""
        buildConfigField("String", "TFL_API_KEY", "\"$apiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Wear OS
    implementation("androidx.wear:wear:1.3.0")
    implementation("androidx.wear.compose:compose-material:1.2.1")
    implementation("androidx.wear.compose:compose-foundation:1.2.1")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.8.1")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kapt {
    correctErrorTypes = true
}
```

**Step 5: Create AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-feature android:name="android.hardware.type.watch" />

    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <application
        android:name=".BusWatchApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.DeviceDefault">

        <uses-library
            android:name="com.google.android.wearable"
            android:required="true" />

        <meta-data
            android:name="com.google.android.wearable.standalone"
            android:value="true" />

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@android:style/Theme.DeviceDefault">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

**Step 6: Create local.properties (for TfL API key)**

In root directory, create `local.properties`:
```properties
tfl.api.key=YOUR_TFL_API_KEY_HERE
```

**Step 7: Commit**

```bash
git add build.gradle.kts settings.gradle.kts gradle.properties app/build.gradle.kts app/src/main/AndroidManifest.xml
git commit -m "feat: initial Android Wear OS project setup"
```

---

## Task 2: Application Class and Hilt Setup

**Files:**
- Create: `app/src/main/java/com/buswatch/BusWatchApplication.kt`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/mipmap-hdpi/ic_launcher.png` (placeholder)

**Step 1: Write BusWatchApplication class**

```kotlin
// ABOUTME: Application class initializing Timber logging and Hilt dependency injection
// ABOUTME: Entry point for the BusWatch Wear OS application
package com.buswatch

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class BusWatchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.d("BusWatch application started")
    }
}
```

**Step 2: Create strings.xml**

```xml
<resources>
    <string name="app_name">BusWatch</string>
</resources>
```

**Step 3: Commit**

```bash
git add app/src/main/java/com/buswatch/BusWatchApplication.kt app/src/main/res/values/strings.xml
git commit -m "feat: add Application class with Hilt and Timber"
```

---

## Task 3: Domain Models

**Files:**
- Create: `app/src/main/java/com/buswatch/domain/model/BusStop.kt`
- Create: `app/src/main/java/com/buswatch/domain/model/BusArrival.kt`
- Create: `app/src/main/java/com/buswatch/domain/model/ArrivalType.kt`
- Create: `app/src/test/java/com/buswatch/domain/model/BusStopTest.kt`

**Step 1: Write failing test for BusStop**

```kotlin
// ABOUTME: Unit tests for BusStop domain model
package com.buswatch.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BusStopTest {
    @Test
    fun `BusStop creation with all properties`() {
        val stop = BusStop(
            id = "490000001B",
            code = "BP",
            name = "Oxford Street",
            latitude = 51.5074,
            longitude = -0.1278,
            routes = listOf("25", "73", "98"),
            distanceMeters = 150
        )

        assertEquals("490000001B", stop.id)
        assertEquals("BP", stop.code)
        assertEquals("Oxford Street", stop.name)
        assertEquals(150, stop.distanceMeters)
        assertEquals(3, stop.routes.size)
    }
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew test --tests BusStopTest`
Expected: FAIL with "Unresolved reference: BusStop"

**Step 3: Write BusStop model**

```kotlin
// ABOUTME: Domain model representing a London bus stop with location and routes
package com.buswatch.domain.model

data class BusStop(
    val id: String,
    val code: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val routes: List<String>,
    val distanceMeters: Int
)
```

**Step 4: Write ArrivalType enum**

```kotlin
// ABOUTME: Enum representing bus arrival data source type
package com.buswatch.domain.model

enum class ArrivalType {
    LIVE,       // Real-time tracked data
    SCHEDULED   // Timetable-based data
}
```

**Step 5: Write BusArrival model**

```kotlin
// ABOUTME: Domain model representing a bus arrival with route, destination, and timing
package com.buswatch.domain.model

data class BusArrival(
    val route: String,
    val destinationShort: String,
    val minutesUntil: Int,
    val arrivalType: ArrivalType
)
```

**Step 6: Run test to verify it passes**

Run: `./gradlew test --tests BusStopTest`
Expected: PASS

**Step 7: Commit**

```bash
git add app/src/main/java/com/buswatch/domain/model/ app/src/test/java/com/buswatch/domain/model/
git commit -m "feat: add domain models for BusStop and BusArrival"
```

---

## Task 4: TfL API DTOs and Client

**Files:**
- Create: `app/src/main/java/com/buswatch/data/remote/dto/StopPointDto.kt`
- Create: `app/src/main/java/com/buswatch/data/remote/dto/ArrivalDto.kt`
- Create: `app/src/main/java/com/buswatch/data/remote/TfLApiService.kt`
- Create: `app/src/main/java/com/buswatch/data/remote/di/NetworkModule.kt`

**Step 1: Create StopPointDto**

```kotlin
// ABOUTME: Data transfer object for TfL API stop point response
package com.buswatch.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StopPointDto(
    @SerializedName("id") val id: String,
    @SerializedName("commonName") val commonName: String,
    @SerializedName("indicator") val indicator: String?,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("lines") val lines: List<LineDto>
)

data class LineDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)
```

**Step 2: Create ArrivalDto**

```kotlin
// ABOUTME: Data transfer object for TfL API arrival prediction response
package com.buswatch.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ArrivalDto(
    @SerializedName("lineId") val lineId: String,
    @SerializedName("lineName") val lineName: String,
    @SerializedName("destinationName") val destinationName: String,
    @SerializedName("timeToStation") val timeToStation: Int,
    @SerializedName("timing") val timing: TimingDto?
)

data class TimingDto(
    @SerializedName("source") val source: String?
)
```

**Step 3: Create TfLApiService interface**

```kotlin
// ABOUTME: Retrofit service interface for TfL API endpoints
package com.buswatch.data.remote

import com.buswatch.data.remote.dto.ArrivalDto
import com.buswatch.data.remote.dto.StopPointDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TfLApiService {
    @GET("StopPoint")
    suspend fun getNearbyStops(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("stopTypes") stopTypes: String = "NaptanPublicBusCoachTram",
        @Query("radius") radius: Int = 500
    ): List<StopPointDto>

    @GET("StopPoint/{stopId}/Arrivals")
    suspend fun getArrivals(
        @Path("stopId") stopId: String
    ): List<ArrivalDto>
}
```

**Step 4: Create NetworkModule for Hilt**

```kotlin
// ABOUTME: Hilt module providing network dependencies (Retrofit, OkHttp, API service)
package com.buswatch.data.remote.di

import com.buswatch.BuildConfig
import com.buswatch.data.remote.TfLApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val apiKeyInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val urlWithApiKey = originalRequest.url.newBuilder()
                .addQueryParameter("app_key", BuildConfig.TFL_API_KEY)
                .build()
            val requestWithApiKey = originalRequest.newBuilder()
                .url(urlWithApiKey)
                .build()
            chain.proceed(requestWithApiKey)
        }

        return OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.tfl.gov.uk/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTfLApiService(retrofit: Retrofit): TfLApiService {
        return retrofit.create(TfLApiService::class.java)
    }
}
```

**Step 5: Commit**

```bash
git add app/src/main/java/com/buswatch/data/remote/
git commit -m "feat: add TfL API DTOs and Retrofit service"
```

---

## Task 5: Repositories - TfLRepository

**Files:**
- Create: `app/src/main/java/com/buswatch/data/repository/TfLRepository.kt`
- Create: `app/src/main/java/com/buswatch/util/Result.kt`
- Create: `app/src/test/java/com/buswatch/data/repository/TfLRepositoryTest.kt`

**Step 1: Create Result sealed class**

```kotlin
// ABOUTME: Sealed class representing operation results with success or error states
package com.buswatch.util

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}
```

**Step 2: Write failing test for TfLRepository**

```kotlin
// ABOUTME: Unit tests for TfLRepository with mocked API service
package com.buswatch.data.repository

import com.buswatch.data.remote.TfLApiService
import com.buswatch.data.remote.dto.ArrivalDto
import com.buswatch.data.remote.dto.LineDto
import com.buswatch.data.remote.dto.StopPointDto
import com.buswatch.data.remote.dto.TimingDto
import com.buswatch.domain.model.ArrivalType
import com.buswatch.util.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TfLRepositoryTest {

    private lateinit var apiService: TfLApiService
    private lateinit var repository: TfLRepository

    @Before
    fun setup() {
        apiService = mockk()
        repository = TfLRepository(apiService)
    }

    @Test
    fun `getNearbyStops returns success with stops`() = runTest {
        val mockStops = listOf(
            StopPointDto(
                id = "490000001B",
                commonName = "Oxford Street",
                indicator = "BP",
                lat = 51.5074,
                lon = -0.1278,
                lines = listOf(LineDto("25", "25"))
            )
        )

        coEvery { apiService.getNearbyStops(any(), any(), any(), any()) } returns mockStops

        val result = repository.getNearbyStops(51.5074, -0.1278)

        assertTrue(result is Result.Success)
        val stops = (result as Result.Success).data
        assertEquals(1, stops.size)
        assertEquals("BP", stops[0].code)
    }

    @Test
    fun `getArrivals returns success with live arrivals`() = runTest {
        val mockArrivals = listOf(
            ArrivalDto(
                lineId = "25",
                lineName = "25",
                destinationName = "Ilford",
                timeToStation = 180,
                timing = TimingDto("Estimated")
            )
        )

        coEvery { apiService.getArrivals(any()) } returns mockArrivals

        val result = repository.getArrivals("490000001B")

        assertTrue(result is Result.Success)
        val arrivals = (result as Result.Success).data
        assertEquals(1, arrivals.size)
        assertEquals("25", arrivals[0].route)
        assertEquals("Ilf", arrivals[0].destinationShort)
        assertEquals(3, arrivals[0].minutesUntil)
        assertEquals(ArrivalType.LIVE, arrivals[0].arrivalType)
    }
}
```

**Step 3: Run test to verify it fails**

Run: `./gradlew test --tests TfLRepositoryTest`
Expected: FAIL with "Unresolved reference: TfLRepository"

**Step 4: Write TfLRepository implementation**

```kotlin
// ABOUTME: Repository handling TfL API data fetching, transformation, and error handling
package com.buswatch.data.repository

import android.location.Location
import com.buswatch.data.remote.TfLApiService
import com.buswatch.domain.model.ArrivalType
import com.buswatch.domain.model.BusArrival
import com.buswatch.domain.model.BusStop
import com.buswatch.util.Result
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TfLRepository @Inject constructor(
    private val apiService: TfLApiService
) {
    suspend fun getNearbyStops(latitude: Double, longitude: Double): Result<List<BusStop>> {
        return executeWithRetry {
            val stops = apiService.getNearbyStops(latitude, longitude)

            val userLocation = Location("").apply {
                this.latitude = latitude
                this.longitude = longitude
            }

            stops.map { dto ->
                val stopLocation = Location("").apply {
                    this.latitude = dto.lat
                    this.longitude = dto.lon
                }
                val distance = userLocation.distanceTo(stopLocation).toInt()

                BusStop(
                    id = dto.id,
                    code = dto.indicator ?: "N/A",
                    name = dto.commonName,
                    latitude = dto.lat,
                    longitude = dto.lon,
                    routes = dto.lines.map { it.name },
                    distanceMeters = distance
                )
            }.sortedBy { it.distanceMeters }
        }
    }

    suspend fun getArrivals(stopId: String): Result<List<BusArrival>> {
        return executeWithRetry {
            val arrivals = apiService.getArrivals(stopId)

            arrivals.map { dto ->
                val minutesUntil = (dto.timeToStation / 60).coerceAtLeast(0)
                val arrivalType = when (dto.timing?.source) {
                    "Estimated" -> ArrivalType.LIVE
                    else -> ArrivalType.SCHEDULED
                }
                val destinationShort = dto.destinationName.take(3)

                BusArrival(
                    route = dto.lineName,
                    destinationShort = destinationShort,
                    minutesUntil = minutesUntil,
                    arrivalType = arrivalType
                )
            }.sortedBy { it.minutesUntil }
        }
    }

    private suspend fun <T> executeWithRetry(
        maxRetries: Int = 3,
        initialDelay: Long = 1000,
        block: suspend () -> T
    ): Result<T> {
        var currentDelay = initialDelay
        repeat(maxRetries) { attempt ->
            try {
                val result = block()
                return Result.Success(result)
            } catch (e: Exception) {
                Timber.e(e, "API call failed (attempt ${attempt + 1}/$maxRetries)")
                if (attempt == maxRetries - 1) {
                    return Result.Error(getErrorMessage(e))
                }
                delay(currentDelay)
                currentDelay *= 2
            }
        }
        return Result.Error("Unknown error")
    }

    private fun getErrorMessage(exception: Exception): String {
        return when {
            exception is java.net.UnknownHostException -> "No internet connection"
            exception.message?.contains("429") == true -> "Service temporarily unavailable. Please try again in a moment."
            exception.message?.contains("404") == true -> "Bus stop not found"
            exception.message?.contains("timeout") == true -> "Request timed out"
            else -> "Unable to load bus times. Please try again later."
        }
    }
}
```

**Step 5: Run test to verify it passes**

Run: `./gradlew test --tests TfLRepositoryTest`
Expected: PASS

**Step 6: Commit**

```bash
git add app/src/main/java/com/buswatch/data/repository/TfLRepository.kt app/src/main/java/com/buswatch/util/Result.kt app/src/test/java/com/buswatch/data/repository/
git commit -m "feat: add TfLRepository with retry logic"
```

---

## Task 6: LocationRepository

**Files:**
- Create: `app/src/main/java/com/buswatch/data/repository/LocationRepository.kt`
- Create: `app/src/test/java/com/buswatch/data/repository/LocationRepositoryTest.kt`

**Step 1: Write failing test for LocationRepository**

```kotlin
// ABOUTME: Unit tests for LocationRepository location acquisition logic
package com.buswatch.data.repository

import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.buswatch.util.Result
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocationRepositoryTest {

    private lateinit var context: Context
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var repository: LocationRepository

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        locationClient = mockk()
        repository = LocationRepository(context, locationClient)
    }

    @Test
    fun `getCurrentLocation returns success with location`() = runTest {
        val mockLocation = mockk<Location> {
            every { latitude } returns 51.5074
            every { longitude } returns -0.1278
        }

        val taskMock = mockk<Task<Location>>()
        val successSlot = slot<OnSuccessListener<Location>>()

        every { locationClient.lastLocation } returns taskMock
        every { taskMock.addOnSuccessListener(capture(successSlot)) } answers {
            successSlot.captured.onSuccess(mockLocation)
            taskMock
        }
        every { taskMock.addOnFailureListener(any()) } returns taskMock

        val result = repository.getCurrentLocation()

        assertTrue(result is Result.Success)
        val location = (result as Result.Success).data
        assertEquals(51.5074, location.latitude, 0.0001)
    }
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew test --tests LocationRepositoryTest`
Expected: FAIL

**Step 3: Write LocationRepository implementation**

```kotlin
// ABOUTME: Repository managing device location acquisition using Play Services
package com.buswatch.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.buswatch.util.Result
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationRepository @Inject constructor(
    private val context: Context,
    private val locationClient: FusedLocationProviderClient
) {
    suspend fun getCurrentLocation(): Result<Location> {
        if (!hasLocationPermission()) {
            return Result.Error("Location permission required. Please enable in settings.")
        }

        return suspendCancellableCoroutine { continuation ->
            try {
                locationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            Timber.d("Location acquired: ${location.latitude}, ${location.longitude}")
                            continuation.resume(Result.Success(location))
                        } else {
                            Timber.w("Location is null")
                            continuation.resume(Result.Error("Unable to get location. Please ensure GPS is enabled."))
                        }
                    }
                    .addOnFailureListener { exception ->
                        Timber.e(exception, "Failed to get location")
                        continuation.resume(Result.Error("Unable to get location. Please ensure GPS is enabled."))
                    }
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException getting location")
                continuation.resume(Result.Error("Location permission required. Please enable in settings."))
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
```

**Step 4: Add LocationModule for Hilt**

Create: `app/src/main/java/com/buswatch/data/repository/di/LocationModule.kt`

```kotlin
// ABOUTME: Hilt module providing location-related dependencies
package com.buswatch.data.repository.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
}
```

**Step 5: Run test to verify it passes**

Run: `./gradlew test --tests LocationRepositoryTest`
Expected: PASS

**Step 6: Commit**

```bash
git add app/src/main/java/com/buswatch/data/repository/LocationRepository.kt app/src/main/java/com/buswatch/data/repository/di/ app/src/test/java/com/buswatch/data/repository/LocationRepositoryTest.kt
git commit -m "feat: add LocationRepository with permission handling"
```

---

## Task 7: DataStore for Preferences

**Files:**
- Create: `app/src/main/java/com/buswatch/data/local/PreferencesDataStore.kt`
- Create: `app/src/main/java/com/buswatch/data/local/di/DataStoreModule.kt`

**Step 1: Write PreferencesDataStore**

```kotlin
// ABOUTME: DataStore wrapper for storing last selected bus stop preferences
package com.buswatch.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bus_watch_prefs")

@Singleton
class PreferencesDataStore @Inject constructor(
    private val context: Context
) {
    private object Keys {
        val LAST_STOP_ID = stringPreferencesKey("last_stop_id")
        val LAST_STOP_LAT = doublePreferencesKey("last_stop_lat")
        val LAST_STOP_LON = doublePreferencesKey("last_stop_lon")
    }

    suspend fun saveLastStop(stopId: String, latitude: Double, longitude: Double) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_STOP_ID] = stopId
            prefs[Keys.LAST_STOP_LAT] = latitude
            prefs[Keys.LAST_STOP_LON] = longitude
        }
    }

    fun getLastStop(): Flow<LastStop?> {
        return context.dataStore.data.map { prefs ->
            val stopId = prefs[Keys.LAST_STOP_ID]
            val lat = prefs[Keys.LAST_STOP_LAT]
            val lon = prefs[Keys.LAST_STOP_LON]

            if (stopId != null && lat != null && lon != null) {
                LastStop(stopId, lat, lon)
            } else {
                null
            }
        }
    }

    suspend fun clearLastStop() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.LAST_STOP_ID)
            prefs.remove(Keys.LAST_STOP_LAT)
            prefs.remove(Keys.LAST_STOP_LON)
        }
    }
}

data class LastStop(
    val stopId: String,
    val latitude: Double,
    val longitude: Double
)
```

**Step 2: Create DataStoreModule**

```kotlin
// ABOUTME: Hilt module providing DataStore dependency
package com.buswatch.data.local.di

import android.content.Context
import com.buswatch.data.local.PreferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): PreferencesDataStore {
        return PreferencesDataStore(context)
    }
}
```

**Step 3: Commit**

```bash
git add app/src/main/java/com/buswatch/data/local/
git commit -m "feat: add DataStore for last stop preferences"
```

---

## Task 8: UI State Classes

**Files:**
- Create: `app/src/main/java/com/buswatch/ui/state/UiState.kt`
- Create: `app/src/main/java/com/buswatch/ui/state/StopListState.kt`
- Create: `app/src/main/java/com/buswatch/ui/state/ArrivalState.kt`

**Step 1: Create UiState sealed class**

```kotlin
// ABOUTME: Generic sealed class for representing UI loading, success, and error states
package com.buswatch.ui.state

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val canRetry: Boolean = true) : UiState<Nothing>()
}
```

**Step 2: Create StopListState**

```kotlin
// ABOUTME: UI state for bus stop list screen
package com.buswatch.ui.state

import com.buswatch.domain.model.BusStop

data class StopListData(
    val stops: List<BusStop>
)
```

**Step 3: Create ArrivalState**

```kotlin
// ABOUTME: UI state for arrival times screen
package com.buswatch.ui.state

import com.buswatch.domain.model.BusArrival

data class ArrivalData(
    val stopCode: String,
    val stopName: String,
    val arrivalsByRoute: Map<String, List<BusArrival>>
)
```

**Step 4: Commit**

```bash
git add app/src/main/java/com/buswatch/ui/state/
git commit -m "feat: add UI state classes"
```

---

## Task 9: StopListViewModel

**Files:**
- Create: `app/src/main/java/com/buswatch/ui/viewmodel/StopListViewModel.kt`
- Create: `app/src/test/java/com/buswatch/ui/viewmodel/StopListViewModelTest.kt`

**Step 1: Write failing test for StopListViewModel**

```kotlin
// ABOUTME: Unit tests for StopListViewModel state management
package com.buswatch.ui.viewmodel

import android.location.Location
import com.buswatch.data.local.LastStop
import com.buswatch.data.local.PreferencesDataStore
import com.buswatch.data.repository.LocationRepository
import com.buswatch.data.repository.TfLRepository
import com.buswatch.domain.model.BusStop
import com.buswatch.ui.state.UiState
import com.buswatch.util.Result
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StopListViewModelTest {

    private lateinit var locationRepository: LocationRepository
    private lateinit var tflRepository: TfLRepository
    private lateinit var preferencesDataStore: PreferencesDataStore
    private lateinit var viewModel: StopListViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        locationRepository = mockk()
        tflRepository = mockk()
        preferencesDataStore = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNearbyStops success shows stops`() = runTest {
        val mockLocation = mockk<Location> {
            every { latitude } returns 51.5074
            every { longitude } returns -0.1278
        }
        val mockStops = listOf(
            BusStop("1", "BP", "Oxford St", 51.5074, -0.1278, listOf("25"), 100)
        )

        coEvery { locationRepository.getCurrentLocation() } returns Result.Success(mockLocation)
        coEvery { tflRepository.getNearbyStops(any(), any()) } returns Result.Success(mockStops)
        every { preferencesDataStore.getLastStop() } returns flowOf(null)

        viewModel = StopListViewModel(locationRepository, tflRepository, preferencesDataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
    }
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew test --tests StopListViewModelTest`
Expected: FAIL

**Step 3: Write StopListViewModel implementation**

```kotlin
// ABOUTME: ViewModel managing stop list screen state and location-based stop fetching
package com.buswatch.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buswatch.data.local.PreferencesDataStore
import com.buswatch.data.repository.LocationRepository
import com.buswatch.data.repository.TfLRepository
import com.buswatch.domain.model.BusStop
import com.buswatch.ui.state.StopListData
import com.buswatch.ui.state.UiState
import com.buswatch.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class StopListViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val tflRepository: TfLRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<StopListData>>(UiState.Loading)
    val uiState: StateFlow<UiState<StopListData>> = _uiState.asStateFlow()

    private var currentLocation: Location? = null

    init {
        loadNearbyStops()
    }

    fun loadNearbyStops() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            when (val locationResult = locationRepository.getCurrentLocation()) {
                is Result.Success -> {
                    currentLocation = locationResult.data
                    fetchNearbyStops(locationResult.data.latitude, locationResult.data.longitude)
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(locationResult.message, canRetry = false)
                }
            }
        }
    }

    private suspend fun fetchNearbyStops(latitude: Double, longitude: Double) {
        when (val stopsResult = tflRepository.getNearbyStops(latitude, longitude)) {
            is Result.Success -> {
                val stops = stopsResult.data.take(5)
                _uiState.value = UiState.Success(StopListData(stops))
                Timber.d("Loaded ${stops.size} nearby stops")
            }
            is Result.Error -> {
                _uiState.value = UiState.Error(stopsResult.message, canRetry = true)
            }
        }
    }

    suspend fun saveSelectedStop(stop: BusStop) {
        preferencesDataStore.saveLastStop(stop.id, stop.latitude, stop.longitude)
        Timber.d("Saved last stop: ${stop.code}")
    }

    suspend fun checkLastStop(): BusStop? {
        val lastStop = preferencesDataStore.getLastStop().first() ?: return null
        val currentLoc = currentLocation ?: return null

        val stopLocation = Location("").apply {
            latitude = lastStop.latitude
            longitude = lastStop.longitude
        }

        val distance = currentLoc.distanceTo(stopLocation)

        return if (distance <= 500) {
            _uiState.value.let { state ->
                if (state is UiState.Success) {
                    state.data.stops.find { it.id == lastStop.stopId }
                } else null
            }
        } else {
            null
        }
    }
}
```

**Step 4: Run test to verify it passes**

Run: `./gradlew test --tests StopListViewModelTest`
Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/buswatch/ui/viewmodel/StopListViewModel.kt app/src/test/java/com/buswatch/ui/viewmodel/StopListViewModelTest.kt
git commit -m "feat: add StopListViewModel with location handling"
```

---

## Task 10: ArrivalViewModel

**Files:**
- Create: `app/src/main/java/com/buswatch/ui/viewmodel/ArrivalViewModel.kt`
- Create: `app/src/test/java/com/buswatch/ui/viewmodel/ArrivalViewModelTest.kt`

**Step 1: Write failing test for ArrivalViewModel**

```kotlin
// ABOUTME: Unit tests for ArrivalViewModel refresh and state management
package com.buswatch.ui.viewmodel

import com.buswatch.data.repository.TfLRepository
import com.buswatch.domain.model.ArrivalType
import com.buswatch.domain.model.BusArrival
import com.buswatch.ui.state.UiState
import com.buswatch.util.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArrivalViewModelTest {

    private lateinit var tflRepository: TfLRepository
    private lateinit var viewModel: ArrivalViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        tflRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadArrivals success shows arrivals grouped by route`() = runTest {
        val mockArrivals = listOf(
            BusArrival("25", "Ilf", 3, ArrivalType.LIVE),
            BusArrival("25", "Ilf", 8, ArrivalType.LIVE)
        )

        coEvery { tflRepository.getArrivals(any()) } returns Result.Success(mockArrivals)

        viewModel = ArrivalViewModel(tflRepository)
        viewModel.loadArrivals("490000001B", "BP", "Oxford St")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
    }
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew test --tests ArrivalViewModelTest`
Expected: FAIL

**Step 3: Write ArrivalViewModel implementation**

```kotlin
// ABOUTME: ViewModel managing arrival screen state with auto-refresh logic
package com.buswatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buswatch.data.repository.TfLRepository
import com.buswatch.domain.model.BusArrival
import com.buswatch.ui.state.ArrivalData
import com.buswatch.ui.state.UiState
import com.buswatch.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ArrivalViewModel @Inject constructor(
    private val tflRepository: TfLRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ArrivalData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ArrivalData>> = _uiState.asStateFlow()

    private var refreshJob: Job? = null
    private var stopId: String = ""
    private var stopCode: String = ""
    private var stopName: String = ""
    private var lastActivityTime: Long = System.currentTimeMillis()

    fun loadArrivals(stopId: String, stopCode: String, stopName: String) {
        this.stopId = stopId
        this.stopCode = stopCode
        this.stopName = stopName
        fetchArrivals()
        startAutoRefresh()
    }

    fun onUserActivity() {
        lastActivityTime = System.currentTimeMillis()
    }

    private fun fetchArrivals() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            when (val result = tflRepository.getArrivals(stopId)) {
                is Result.Success -> {
                    if (result.data.isEmpty()) {
                        _uiState.value = UiState.Error("No buses currently scheduled", canRetry = false)
                    } else {
                        val groupedArrivals = result.data
                            .groupBy { it.route }
                            .mapValues { (_, arrivals) -> arrivals.take(2) }

                        _uiState.value = UiState.Success(
                            ArrivalData(stopCode, stopName, groupedArrivals)
                        )
                        Timber.d("Loaded arrivals for ${groupedArrivals.size} routes")
                    }
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.message, canRetry = true)
                }
            }
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(60_000) // 60 seconds

                val inactiveDuration = System.currentTimeMillis() - lastActivityTime
                if (inactiveDuration >= 300_000) { // 5 minutes
                    Timber.d("Stopping auto-refresh due to inactivity")
                    break
                }

                fetchArrivals()
            }
        }
    }

    fun retry() {
        fetchArrivals()
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}
```

**Step 4: Run test to verify it passes**

Run: `./gradlew test --tests ArrivalViewModelTest`
Expected: PASS

**Step 5: Commit**

```bash
git add app/src/main/java/com/buswatch/ui/viewmodel/ArrivalViewModel.kt app/src/test/java/com/buswatch/ui/viewmodel/ArrivalViewModelTest.kt
git commit -m "feat: add ArrivalViewModel with auto-refresh"
```

---

## Task 11: Theme Setup

**Files:**
- Create: `app/src/main/java/com/buswatch/ui/theme/Color.kt`
- Create: `app/src/main/java/com/buswatch/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/buswatch/ui/theme/Type.kt`

**Step 1: Create Color.kt**

```kotlin
// ABOUTME: Color definitions for light and dark themes
package com.buswatch.ui.theme

import androidx.compose.ui.graphics.Color

// Dark theme colors
val DarkBackground = Color(0xFF000000)
val DarkPrimary = Color(0xFF8AB4F8)
val LiveGreen = Color(0xFF34A853)
val ScheduledWhite = Color(0xFFFFFFFF)

// Light theme colors
val LightBackground = Color(0xFFFFFFFF)
val LightPrimary = Color(0xFF1A73E8)
val LiveGreenDark = Color(0xFF137333)
val ScheduledBlack = Color(0xFF000000)
```

**Step 2: Create Type.kt**

```kotlin
// ABOUTME: Typography definitions for consistent text styling
package com.buswatch.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Typography

val BusWatchTypography = Typography(
    display1 = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    ),
    title1 = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    ),
    body1 = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    ),
    button = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )
)
```

**Step 3: Create Theme.kt**

```kotlin
// ABOUTME: Main theme composable managing light/dark theme colors
package com.buswatch.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

@Composable
fun BusWatchTheme(
    content: @Composable () -> Unit
) {
    val colors = Colors(
        primary = DarkPrimary,
        primaryVariant = DarkPrimary,
        secondary = LiveGreen,
        secondaryVariant = LiveGreen,
        background = DarkBackground,
        surface = DarkBackground,
        error = Color(0xFFCF6679),
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
        onError = Color.Black
    )

    MaterialTheme(
        colors = colors,
        typography = BusWatchTypography,
        content = content
    )
}
```

**Step 4: Commit**

```bash
git add app/src/main/java/com/buswatch/ui/theme/
git commit -m "feat: add theme with color and typography"
```

---

## Task 12: StopListScreen UI

**Files:**
- Create: `app/src/main/java/com/buswatch/ui/screens/StopListScreen.kt`
- Create: `app/src/main/java/com/buswatch/ui/components/LoadingScreen.kt`
- Create: `app/src/main/java/com/buswatch/ui/components/ErrorScreen.kt`

**Step 1: Create LoadingScreen component**

```kotlin
// ABOUTME: Reusable loading indicator component with message
package com.buswatch.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text

@Composable
fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message)
        }
    }
}
```

**Step 2: Create ErrorScreen component**

```kotlin
// ABOUTME: Reusable error display component with optional retry
package com.buswatch.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text

@Composable
fun ErrorScreen(
    message: String,
    canRetry: Boolean = true,
    onRetry: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = message,
                textAlign = TextAlign.Center
            )
            if (canRetry && onRetry != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}
```

**Step 3: Create StopListScreen**

```kotlin
// ABOUTME: Stop list screen displaying nearby bus stops in scrollable list
package com.buswatch.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import com.buswatch.domain.model.BusStop
import com.buswatch.ui.components.ErrorScreen
import com.buswatch.ui.components.LoadingScreen
import com.buswatch.ui.state.UiState
import com.buswatch.ui.viewmodel.StopListViewModel
import android.view.HapticFeedbackConstants

@Composable
fun StopListScreen(
    onStopSelected: (BusStop) -> Unit,
    viewModel: StopListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val view = LocalView.current

    when (val state = uiState) {
        is UiState.Loading -> {
            LoadingScreen(message = "Getting your location...")
        }
        is UiState.Error -> {
            ErrorScreen(
                message = state.message,
                canRetry = state.canRetry,
                onRetry = if (state.canRetry) {
                    { viewModel.loadNearbyStops() }
                } else null
            )
        }
        is UiState.Success -> {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.data.stops) { stop ->
                    StopListItem(
                        stop = stop,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            onStopSelected(stop)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StopListItem(
    stop: BusStop,
    onClick: () -> Unit
) {
    Chip(
        onClick = onClick,
        label = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Stop ${stop.code}",
                    fontWeight = FontWeight.Bold
                )
                Text(text = stop.name)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Routes: ${stop.routes.joinToString(", ")}")
            }
        },
        colors = ChipDefaults.primaryChipColors(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    )
}
```

**Step 4: Commit**

```bash
git add app/src/main/java/com/buswatch/ui/screens/StopListScreen.kt app/src/main/java/com/buswatch/ui/components/
git commit -m "feat: add StopListScreen UI with loading and error states"
```

---

## Task 13: ArrivalScreen UI

**Files:**
- Create: `app/src/main/java/com/buswatch/ui/screens/ArrivalScreen.kt`

**Step 1: Create ArrivalScreen**

```kotlin
// ABOUTME: Arrival screen displaying bus arrival times with auto-refresh
package com.buswatch.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import com.buswatch.domain.model.ArrivalType
import com.buswatch.domain.model.BusArrival
import com.buswatch.ui.components.ErrorScreen
import com.buswatch.ui.components.LoadingScreen
import com.buswatch.ui.state.UiState
import com.buswatch.ui.theme.LiveGreen
import com.buswatch.ui.theme.ScheduledWhite
import com.buswatch.ui.viewmodel.ArrivalViewModel
import android.view.HapticFeedbackConstants

@Composable
fun ArrivalScreen(
    stopId: String,
    stopCode: String,
    stopName: String,
    onChangeStop: () -> Unit,
    viewModel: ArrivalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val view = LocalView.current

    DisposableEffect(stopId) {
        viewModel.loadArrivals(stopId, stopCode, stopName)
        onDispose { }
    }

    when (val state = uiState) {
        is UiState.Loading -> {
            LoadingScreen(message = "Loading arrivals...")
        }
        is UiState.Error -> {
            ErrorScreen(
                message = state.message,
                canRetry = state.canRetry,
                onRetry = if (state.canRetry) {
                    { viewModel.retry() }
                } else null
            )
        }
        is UiState.Success -> {
            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Stop ${state.data.stopCode}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = state.data.stopName)
                    }
                }

                state.data.arrivalsByRoute.forEach { (route, arrivals) ->
                    items(arrivals.size) { index ->
                        ArrivalItem(
                            arrival = arrivals[index],
                            onClick = { viewModel.onUserActivity() }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            onChangeStop()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Stop")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ArrivalItem(
    arrival: BusArrival,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = arrival.route,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )
        Text(text = "→")
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = arrival.destinationShort)
        Spacer(modifier = Modifier.weight(1f))

        val timeText = if (arrival.minutesUntil < 1) "Due" else "${arrival.minutesUntil} min"
        val timeColor = when (arrival.arrivalType) {
            ArrivalType.LIVE -> LiveGreen
            ArrivalType.SCHEDULED -> ScheduledWhite
        }

        Text(
            text = timeText,
            color = timeColor,
            fontWeight = FontWeight.Medium
        )
    }
}
```

**Step 2: Commit**

```bash
git add app/src/main/java/com/buswatch/ui/screens/ArrivalScreen.kt
git commit -m "feat: add ArrivalScreen UI with color-coded times"
```

---

## Task 14: MainActivity and Navigation

**Files:**
- Create: `app/src/main/java/com/buswatch/MainActivity.kt`

**Step 1: Write MainActivity**

```kotlin
// ABOUTME: Main activity managing navigation between stop list and arrival screens
package com.buswatch

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buswatch.domain.model.BusStop
import com.buswatch.ui.screens.ArrivalScreen
import com.buswatch.ui.screens.StopListScreen
import com.buswatch.ui.theme.BusWatchTheme
import com.buswatch.ui.viewmodel.StopListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Timber.d("Location permission granted")
        } else {
            Timber.w("Location permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkLocationPermission()

        setContent {
            BusWatchTheme {
                var selectedStop by remember { mutableStateOf<BusStop?>(null) }
                val scope = rememberCoroutineScope()
                val stopListViewModel: StopListViewModel = viewModel()

                if (selectedStop == null) {
                    StopListScreen(
                        onStopSelected = { stop ->
                            scope.launch {
                                stopListViewModel.saveSelectedStop(stop)
                                selectedStop = stop
                            }
                        }
                    )
                } else {
                    ArrivalScreen(
                        stopId = selectedStop!!.id,
                        stopCode = selectedStop!!.code,
                        stopName = selectedStop!!.name,
                        onChangeStop = {
                            selectedStop = null
                        }
                    )
                }
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                Timber.d("Location permission already granted")
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
}
```

**Step 2: Commit**

```bash
git add app/src/main/java/com/buswatch/MainActivity.kt
git commit -m "feat: add MainActivity with navigation and permissions"
```

---

## Task 15: Build and Run

**Step 1: Sync Gradle**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 2: Run unit tests**

Run: `./gradlew test`
Expected: All tests PASS

**Step 3: Create a simple integration test**

Create: `app/src/androidTest/java/com/buswatch/MainActivityTest.kt`

```kotlin
// ABOUTME: Integration test verifying MainActivity launches correctly
package com.buswatch

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivity_launches() {
        composeTestRule.onNodeWithText("Getting your location...").assertExists()
    }
}
```

**Step 4: Commit**

```bash
git add app/src/androidTest/java/com/buswatch/MainActivityTest.kt
git commit -m "test: add MainActivity integration test"
```

**Step 5: Final verification**

Run: `./gradlew build test`
Expected: BUILD SUCCESSFUL, all tests PASS

---

## Summary

This implementation plan covers:
✅ Android Wear OS project setup with Gradle
✅ Domain models (BusStop, BusArrival)
✅ TfL API client with Retrofit
✅ Repositories (TfLRepository, LocationRepository)
✅ DataStore for preferences
✅ ViewModels with state management
✅ UI screens (StopList, Arrivals)
✅ Theme and styling
✅ MainActivity with navigation
✅ Unit and integration tests

**Next Steps:**
1. Register for TfL API key at https://api-portal.tfl.gov.uk/
2. Add key to `local.properties`
3. Deploy to Wear OS device/emulator
4. Test with real data

**Future Enhancements:**
- Add delay detection (orange color)
- Improve error messages
- Add app icon
- Add more comprehensive E2E tests
