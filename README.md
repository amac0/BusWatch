# BusWatch

A Wear OS app for tracking London bus arrivals in real-time, designed specifically for Pixel Watch and other Wear OS devices.

**NOTE: This whole app was coded by Claude Code. That includes most of this page. Please use only at your own risk.**
More about how it was developed at [my blog](https://www.bricoleur.org/2025/12/ai-coding-another-update.html)

## Features

- 📍 **Location-aware**: Automatically shows the 5 closest bus stops based on your current location
- 🔄 **Auto-refresh**: Updates stops when you move more than 200 meters
- ⏱️ **Live arrivals**: Real-time bus arrival predictions with color-coded timing
- 🎯 **Smart remembering**: Remembers your last selected stop if you're within 500m
- 🚌 **Stop details**: Shows stop codes, names, and all routes serving each stop

## Screenshots

*Coming soon*

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 33 or higher
- Wear OS device or emulator (API level 30+)
- TfL API key (free from [TfL API Portal](https://api.tfl.gov.uk))

## Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/amac0/BusWatch.git
   cd BusWatch
   ```

2. **Configure TfL API key**

   Create a `local.properties` file in the project root:
   ```properties
   sdk.dir=/path/to/android-sdk
   tfl.api.key=YOUR_TFL_API_KEY_HERE
   ```

   Get your free TfL API key from: https://api.tfl.gov.uk

3. **Build and run**
   ```bash
   ./gradlew installDebug
   ```

## Architecture

BusWatch follows Clean Architecture principles with clear separation of concerns:

```
app/
├── data/           # Data layer
│   ├── local/      # Local data storage (DataStore)
│   ├── remote/     # TfL API integration (Retrofit)
│   └── repository/ # Repository implementations
├── domain/         # Domain models
│   └── model/      # Business entities
├── ui/             # Presentation layer
│   ├── components/ # Reusable UI components
│   ├── screens/    # Screen composables
│   ├── theme/      # Material Design theme
│   ├── viewmodel/  # ViewModels with state management
│   └── state/      # UI state classes
└── util/           # Utility classes
```

## Technologies

- **Language**: Kotlin
- **UI**: Jetpack Compose for Wear OS
- **DI**: Hilt/Dagger
- **Networking**: Retrofit + OkHttp
- **Location**: Google Play Services Location API
- **Async**: Coroutines + Flow
- **Testing**: JUnit, MockK
- **Logging**: Timber

## How It Works

1. **Location Monitoring**: Uses FusedLocationProviderClient to monitor your location every 30 seconds
2. **Proximity Detection**: When you move more than 200m, the app automatically refreshes nearby stops
3. **API Integration**: Fetches stop and arrival data from Transport for London's unified API
4. **Smart Caching**: Remembers your last stop and shows it immediately if you're still nearby
5. **Live Updates**: Arrival times auto-refresh every 15 seconds when viewing a stop

## Testing

Run the test suite:
```bash
./gradlew test
```

Run tests for a specific class:
```bash
./gradlew test --tests "com.buswatch.ui.viewmodel.StopListViewModelTest"
```

## Building Release APK

```bash
./gradlew assembleRelease
```

The APK will be at: `app/build/outputs/apk/release/app-release.apk`

## Installing on Wear OS Device

### Via ADB over Wi-Fi

1. Enable Developer Options on your watch
2. Enable ADB debugging and Debug over Wi-Fi
3. Note the IP address shown on your watch
4. Connect from your computer:
   ```bash
   adb connect <WATCH_IP_ADDRESS>:5555
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Via USB (if supported)

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Known Issues

- Location updates require GPS to be active and location permissions granted
- TfL API has rate limits - excessive requests may be throttled
- First location fix can take 10-30 seconds on cold start

## Contributing

Contributions welcome! Please ensure:
- Tests pass: `./gradlew test`
- Code follows existing style
- Commits are descriptive

## License

This project is open source and available under the MIT License.

## Acknowledgments

- Transport for London for providing the unified API
- Android team for Wear OS and Compose libraries
- Contributors and testers

## Support

For issues and feature requests, please open an issue on GitHub.

---

Built with ❤️ for London commuters
