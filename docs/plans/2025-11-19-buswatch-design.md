# BusWatch Design Specification

**Date:** 2025-11-19
**Version:** 1.0
**Target Platform:** Wear OS 3.0+

## Overview

BusWatch is a Wear OS app that provides real-time London bus arrival times for nearby bus stops. On launch, it displays bus stops within 500 meters (or the closest 5 stops), allows users to select a stop, and shows the next two arrivals for each route, ordered by arrival time.

## Architecture

### Technology Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose for Wear OS
- **Minimum SDK**: Wear OS 3.0 (API level 30)
- **Architecture**: MVVM with Repository pattern
- **Dependency Injection**: Hilt
- **Networking**: Retrofit + OkHttp
- **Location**: Google Play Services Location API
- **State Management**: ViewModel with StateFlow
- **Persistence**: DataStore (Preferences)

### Module Structure

```
app/
├── data/
│   ├── repository/      # TfLRepository, LocationRepository
│   ├── remote/          # TfL API client, DTOs
│   └── local/           # DataStore wrapper
├── domain/
│   └── model/           # Domain models (BusStop, BusArrival)
├── ui/
│   ├── screens/         # StopListScreen, ArrivalScreen
│   ├── components/      # Reusable UI components
│   └── theme/           # Material theme configuration
└── util/                # Extensions, constants
```

### Key Dependencies

- `androidx.wear.compose:compose-material`
- `com.google.android.gms:play-services-location`
- `com.squareup.retrofit2:retrofit`
- `com.google.dagger:hilt-android`
- `androidx.datastore:datastore-preferences`

The repository pattern isolates API and location logic from UI, making it easy to handle retries, errors, and future changes. ViewModels expose StateFlow for reactive UI updates, and Hilt manages dependency injection throughout.

## Screen Components

### Navigation

Simple two-screen app with state-based screen switching (no navigation component needed).

### 1. Stop List Screen

**States:**
- `Loading`: Shows spinner with "Getting your location..."
- `Error`: Displays error message (location denied, no internet, API failure)
- `StopsLoaded`: Shows scrollable list of bus stops

**UI Layout:**
- **Loading**: Centered spinner with text
- **Error**: Centered error message with icon
- **Stops List**:
  - Scrollable column using `ScalingLazyColumn` (Wear OS optimized)
  - Each stop item shows:
    - Stop code (bold, e.g., "BP")
    - Stop name (e.g., "Oxford Street")
    - Route numbers (e.g., "Routes: 25, 73, 98")
  - Ordered by proximity (closest first)
  - Shows closest 5 stops (or all within 500m if fewer than 5)

### 2. Arrival Screen

**States:**
- `Loading`: Shows spinner while fetching arrivals
- `Error`: Error message with retry option
- `ArrivalsLoaded`: Shows arrival times
- `NoArrivals`: Shows "No buses currently scheduled"

**UI Layout:**
- Stop code and name at top
- Scrollable list of arrivals grouped by route
- Each arrival shows:
  - Route number
  - Destination (first 3 letters)
  - Time ("Due", "2 min", etc.)
  - Color coding: green (live tracked), white (scheduled)
- Up to 2 arrivals per route (hide second row if unavailable)
- "Change Stop" button at bottom
- Auto-refreshes every 60 seconds (stops after 5 min inactivity)

## Data Flow

### App Launch Flow

1. **Check for saved stop** (from DataStore)
   - If exists: Get current location → Check if saved stop still within 500m
   - If within range: Load arrivals for saved stop
   - If out of range or no saved stop: Show stop list

2. **Stop List Flow:**
   - `StopListViewModel` requests location from `LocationRepository`
   - Location acquired → `TfLRepository.getNearbyStops(lat, lon)`
   - TfL API returns stops → Filter/sort by distance
   - Emit `StopsLoaded` state → UI renders list
   - User taps stop → Save to DataStore → Switch to arrival screen

3. **Arrival Screen Flow:**
   - `ArrivalViewModel` starts with selected stop
   - Calls `TfLRepository.getArrivals(stopId)`
   - TfL API returns arrivals → Process and group by route
   - Emit `ArrivalsLoaded` state → UI renders
   - Start 60-second refresh timer
   - After 5 minutes inactivity: Stop auto-refresh
   - User taps "Change Stop" → Return to stop list

### Repository Layer

**LocationRepository:**
- Wraps `FusedLocationProviderClient`
- Returns `Flow<Location>` or error
- Handles permission checks

**TfLRepository:**
- Manages Retrofit API client
- Implements retry logic (3 attempts with exponential backoff)
- In-memory cache for last API response (5-minute TTL)
- Transforms API DTOs to domain models
- Determines arrival color (green/white) based on TfL API data

### Preference Storage

- Last selected stop ID
- Last selected stop location (lat/lon)
- Uses Kotlin serialization for DataStore

## Error Handling

### Location Errors

- **Permission Denied**: Show error screen with message "Location permission required. Please enable in settings." No retry option.
- **Location Unavailable**: After 10-second timeout, show "Unable to get location. Please ensure GPS is enabled."
- **GPS Accuracy Low**: Accept any location accuracy - don't block on precision.

### Network Errors

- **No Internet**: Catch `UnknownHostException` / network failures → Show "No internet connection"
- **TfL API Errors**:
  - HTTP 500/502/503: Retry 3 times with exponential backoff (1s, 2s, 4s)
  - HTTP 429 (Rate Limited): Show "Service temporarily unavailable. Please try again in a moment."
  - HTTP 404: Show "Bus stop not found"
  - Timeout (30s): Retry, then show "Request timed out"
  - After 3 failed retries: Show "Unable to load bus times. Please try again later."

### Data Errors

- **Empty Results**:
  - No stops found: Show closest 5 regardless of distance
  - No arrivals for stop: Show "No buses currently scheduled"
- **Malformed API Response**: Log error, show generic "Unable to load data" message

### State Management

All errors are sealed classes:
```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val canRetry: Boolean) : UiState<Nothing>()
}
```

ViewModels expose `StateFlow<UiState<T>>` for each screen. UI observes and renders appropriate state.

### Logging

- Use Timber for debug logging
- Log all API errors with request details
- No crash reporting initially (can add Firebase Crashlytics later)

## Testing Strategy

### Unit Tests

**Repository Tests** (`TfLRepositoryTest`, `LocationRepositoryTest`):
- Mock Retrofit API client and Location services
- Test successful data fetching and transformation
- Test retry logic (verify 3 attempts with delays)
- Test error handling for each HTTP error code
- Test caching behavior (5-minute TTL)
- Verify domain model mapping from DTOs

**ViewModel Tests** (`StopListViewModelTest`, `ArrivalViewModelTest`):
- Mock repositories
- Test state transitions (Loading → Success, Loading → Error)
- Test 60-second refresh timer and 5-minute timeout
- Test distance calculations and sorting
- Test arrival grouping by route
- Test color determination logic (green/white)
- Verify DataStore interactions for saving last stop

### Integration Tests

**API Integration** (`TfLApiIntegrationTest`):
- Real network calls to TfL API (staging/test endpoint if available)
- Verify actual response structure matches DTOs
- Test with real stop IDs and coordinates
- Validate API key authentication

**Location Integration** (`LocationIntegrationTest`):
- Test with Android Location test provider
- Verify permission handling flows
- Test location timeout behavior

### End-to-End Tests

**UI Tests** (Using Compose Testing):
- `StopListE2ETest`: Full flow from location → stop list → tap stop
- `ArrivalE2ETest`: Load arrivals → verify display → auto-refresh → change stop
- `ErrorFlowTest`: Simulate no internet, API errors, permission denied
- `ThemeTest`: Verify light/dark theme rendering
- `HapticTest`: Verify haptic feedback triggers

### Test Infrastructure

- JUnit 5 for unit tests
- MockK for mocking
- Turbine for Flow testing
- Compose Testing library for UI tests
- Robolectric for Android framework dependencies in unit tests

## TfL API Integration

### API Endpoints

Base URL: `https://api.tfl.gov.uk`

**1. Get Nearby Stops:**
```
GET /StopPoint?lat={lat}&lon={lon}&stopTypes=NaptanPublicBusCoachTram&radius=500
```
- Returns all bus stops within radius (meters)
- Request 500m radius, then sort by distance and take closest 5
- Response includes: stop ID, name, indicator (letter code), lat/lon, lines serving it

**2. Get Arrivals:**
```
GET /StopPoint/{stopId}/Arrivals
```
- Returns all upcoming arrivals for the stop
- Response includes: line ID, destination name, expected arrival time, time to station (seconds), modeName, timing.source ("Estimated" for live, "Scheduled" for timetable)

### API Authentication

- Add header: `app_key: {YOUR_API_KEY}`
- Rate limit: 500 requests/min for registered keys
- Bundled key stored in `BuildConfig` (not in version control)

### Data Models

```kotlin
// Domain models
data class BusStop(
    val id: String,
    val code: String,        // e.g., "BP"
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val routes: List<String>, // e.g., ["25", "73", "98"]
    val distanceMeters: Int
)

data class BusArrival(
    val route: String,
    val destinationShort: String,  // First 3 letters
    val minutesUntil: Int,
    val arrivalType: ArrivalType   // LIVE or SCHEDULED
)

enum class ArrivalType {
    LIVE,      // Green - timing.source == "Estimated"
    SCHEDULED  // White - timing.source == "Scheduled"
}
```

## Theme, Styling & Build Configuration

### Material Theme

**Colors:**
- Follow system theme (light/dark mode)
- **Dark theme**:
  - Background: `Color(0xFF000000)` (true black for OLED)
  - Primary: `Color(0xFF8AB4F8)` (light blue)
  - Live arrivals: `Color(0xFF34A853)` (green)
  - Scheduled arrivals: `Color(0xFFFFFFFF)` (white)
- **Light theme**:
  - Background: `Color(0xFFFFFFFF)`
  - Primary: `Color(0xFF1A73E8)` (blue)
  - Live arrivals: `Color(0xFF137333)` (darker green)
  - Scheduled arrivals: `Color(0xFF000000)` (black)

### Typography

- Stop codes: 18sp, bold
- Stop names: 14sp, regular
- Route numbers: 16sp, bold
- Destinations: 14sp, regular
- Times: 14sp, medium
- Error messages: 14sp, regular

### Spacing

- Consistent 8dp grid system
- List item padding: 12dp vertical, 16dp horizontal
- Button padding: 16dp

### Haptics

```kotlin
LocalView.current.performHapticFeedback(
    HapticFeedbackConstants.CLOCK_TICK
)
```
Triggered on: stop selection, "Change Stop" button, refresh actions

### Build Configuration

`build.gradle.kts`:
```kotlin
android {
    namespace = "com.buswatch"
    compileSdk = 34

    defaultConfig {
        minSdk = 30  // Wear OS 3.0
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        // API key from local.properties
        buildConfigField("String", "TFL_API_KEY",
            "\"${project.findProperty("tfl.api.key")}\"")
    }
}
```

`local.properties` (not in git):
```
tfl.api.key=YOUR_KEY_HERE
```

### App Icon

- Simple bus icon with "W" (for Watch)
- Adaptive icon with circular mask for Wear OS
- Primary color background

## User Experience Details

### Features

- **Location-based**: Shows stops near user's current location
- **No manual entry**: GPS-only for simplicity
- **Smart memory**: Remembers last viewed stop if still within 500m
- **Auto-refresh**: Updates every 60 seconds, stops after 5 min inactivity
- **No favorites**: Just remembers last stop
- **No complications/tiles**: Focused app experience only
- **No onboarding**: Intuitive UI without tutorial
- **No settings**: Single-purpose, streamlined

### Display Format

- **Stop list**: Code, name, route numbers (sorted by proximity, no distance shown)
- **Arrivals**: Route → Destination (3 letters), Time
- **Imminent arrivals**: Show "Due" for buses arriving in < 1 minute
- **Missing data**: Hide second arrival row if only one bus coming
- **No arrivals**: Display "No buses currently scheduled"

### Navigation

- Swipe right to go back (standard Wear OS gesture)
- "Change Stop" button at bottom of arrivals for discoverability
- No formal navigation component needed

## Future Enhancements (Not in v1)

- Delay detection (orange color for delayed buses)
- Favorite stops
- Manual stop code entry
- Wear OS complications
- Wear OS tiles
- Notifications for specific buses
- Offline caching with Room database
- Route filtering
- Settings screen
