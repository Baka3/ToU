# ToU — Task Planner (Android)

A native Android application for managing tasks, notes, and reminders. Built entirely with Jetpack Compose and local data persistence. The project showcases a modern Android development stack: declarative UI, a relational database with versioned migrations, scheduled system notifications, and user preferences management.

## Features

- **Tasks and subtasks** — create tasks with nested subtasks, descriptions, and due dates/times.
- **Topics (categories)** — group tasks by topic, including custom topics and drag-and-drop reordering.
- **Reminders** — single and date-range reminders powered by `AlarmManager`, delivered via system notifications.
- **Calendar** — view tasks by date.
- **Deadlines and completed tasks** — dedicated screens for tracking due dates and completion history.
- **Attachments and images** — attach files and images to tasks.
- **Settings** — choose the app theme (light / dark / system), language, wallpaper, and notification options.

## Tech Stack

| Category | Stack |
|----------|-------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3, Material Icons Extended |
| Navigation | Navigation Compose |
| Database | Room (SQLite) with manual migrations |
| Preferences | DataStore Preferences |
| Notifications | AlarmManager + BroadcastReceiver |
| Images | Coil |
| Calendar | Kizitonwose Calendar |
| Drag & Drop | Reorderable |
| Build | Gradle (Kotlin DSL), version catalog |

## Architecture

- **Data.** Room with three entities (`NoteEntity`, `SubtaskEntity`, `CustomTopicEntity`) and their DAOs. The schema has evolved through 13 versions with a complete set of migrations that preserve user data across updates.
- **UI.** Screen-oriented structure built on Compose; each screen is a separate composable, wired together through a single `NavHost` in `MainActivity`.
- **Reminders.** Exact alarms are scheduled via `AlarmManager` and handled by `ReminderReceiver`, which posts notifications.
- **Settings.** Theme and language are stored in DataStore and applied reactively through `Flow`.

## Project Structure

```
app/src/main/java/com/example/tou/
├── MainActivity.kt          # Entry point, navigation graph
├── App.kt                   # Application class, DB init, migrations
├── AppDatabase.kt           # Room configuration
├── *Entity.kt / *Dao.kt     # Data model and access layer
├── *Screen.kt               # Screens (Home, Notes, Calendar, Settings, etc.)
├── Reminder*.kt             # Reminder scheduling and handling
├── AppSettings.kt           # Preferences via DataStore
└── ui/theme/                # Colors, typography, themes
```

## Build and Run

**Requirements:** Android Studio, JDK 11, Android SDK 35 (minimum: Android 8.0, API 26).

```bash
git clone <repository-url>
cd ToU
./gradlew assembleDebug
```

Alternatively, open the project in Android Studio and run it on an emulator or a physical device.

## Status

A learning / portfolio project under active development. It demonstrates hands-on experience with Jetpack Compose, Room, and Android system APIs.
