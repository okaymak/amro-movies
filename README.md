# AMRO Movies

An Android application for the Advanced Movie Recommendation Organisation (AMRO) that displays trending movies and detailed movie information using the TMDB API. Built with a focus on scalability, testability, and modern Android development practices.

## Key Features

- **Adaptive Layout**: Optimized for all screen sizes, featuring a multi-pane list-detail layout on tablets and foldable devices using Navigation 3.
- **Trending Movies**: Displays the top 100 trending movies with dynamic grid density.
- **Concurrent Fetching**: Fetches data across multiple API pages concurrently for improved performance.
- **Advanced Filtering**: Filters movies by genre within the trending list.
- **Flexible Sorting**: Sorts movies by popularity, title, or release date.
- **Detailed Metadata**: Displays ratings, runtime, budget, revenue, and taglines.
- **Shared Element Transitions**: Smooth poster animations when navigating between the list and the detail view.
- **Edge-to-Edge**: Immersive, full-screen experience with seamless system bar integration.
- **Predictive Back**: Native support for modern back gestures and animations.
- **IMDB Integration**: Direct links to movie pages via Chrome Custom Tabs.
- **Robust Error Handling**: Retry mechanisms for both list and detail views.
- **Pull-to-Refresh**: Support for refreshing the trending list.

## Architecture and Tech Stack

The project follows a layered architecture inspired by Clean Architecture principles, with a feature-based package structure. It is optimized for the latest Android releases, targeting **Android 16 (Baklava)**.

- **UI**: Jetpack Compose with Material 3.
- **Navigation**: Adaptive multi-pane navigation that scales seamlessly from phones to tablets.
- **Dependency Injection**: Metro for lightweight, compile-time safe dependency injection.
- **Networking**: Ktor Client with OkHttp and Kotlinx Serialization.
- **Image Loading**: Coil 3 with Ktor integration.
- **Concurrency**: Kotlin Coroutines and Flow.
- **State Management**: Unidirectional Data Flow (UDF) using ViewModels and StateFlow.
- **Testing**:
  - Turbine for testing Kotlin Flows.
  - Unit tests for UseCases, Repositories, Mappers, and ViewModels.
  - Compose UI tests for core screens and edge cases.
  - GitHub Actions pipeline running tests on Android 12 (API 31).

## Setup and Configuration

### Prerequisites
- Android Studio Ladybug or newer.
- JDK 17.
- TMDB API Bearer Token.

### API Key Configuration
The app requires a TMDB Bearer Token (v4). You can provide it in two ways:
1. **Environment Variable**: Set `TMDB_BEARER_TOKEN`.
2. **local.properties**: Add `TMDB_BEARER_TOKEN=your_token_here`.

The build script will automatically pick up the token and inject it into the app's `BuildConfig`. If the token is missing or invalid, the app will display an error state with retry support.

## Testing

To run the unit tests:
```bash
./gradlew test
```

To run the instrumented UI tests:
```bash
./gradlew connectedDebugAndroidTest
```

Test reports (HTML) are generated locally and uploaded as artifacts during CI runs.

## Design Decisions

- **Navigation 3**: Selected for its modern, composable-first approach. It simplifies the creation of multi-pane layouts and provides type-safe routing.
- **Turbine**: Used to ensure ViewModel state transitions are tested deterministically, avoiding race conditions in reactive stream tests.
- **Metro DI**: Chosen for its simplicity and compile-time safety, providing a balance between performance and developer productivity.
- **Ktor**: Preferred for its multiplatform capabilities and native coroutine support.
- **R8 Optimization**: Release builds are optimized with R8 for minification and resource shrinking to ensure a small APK footprint.
- **Stateless UI**: Implemented as stateless composables to improve testability and preview support.

## Trade-offs & Assumptions
- The “Top 100” requirement is implemented by fetching the first five API pages concurrently instead of using Paging 3, as the dataset is bounded.
- The project is intentionally kept as a single module to reduce complexity within the scope of the MVP.
- Genre metadata is fetched once per session and cached in memory.
- Offline persistence was not implemented to keep the focus on core requirements and clarity of architecture.

## Roadmap and Future Improvements

### Modularization
- **Goal**: Transition from a monolithic `:app` module to feature-based modules (e.g., `:feature:movie-list`, `:core:network`).
- **Benefit**: Improves build times and establishes clearer code boundaries.

### Multi-Source API Support
- **Goal**: Extend the repository to aggregate or fallback between multiple data providers (e.g., OMDb).

### Advanced Testing
- **Screenshot Testing**: Introduce screenshot testing to detect visual regressions.
- **Integration Tests**: Add higher-level integration tests.

### New Features
- **Local Storage**: Implement Room or DataStore for offline caching and user preferences.
- **Search**: Add local or remote search functionality.
- **Pagination**: Migrate to Paging 3 for infinite scrolling support.

