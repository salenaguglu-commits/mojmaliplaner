# 🐱 Moj Mali Planer (My Little Planner) — Complete System Architecture & Feature Documentation

> **Target Audience:** AI Developer Agents / System Architects (Gemini Hydrogen / LLM Context)  
> **App Version:** 1.3 (Version Code: 4)  
> **Package Name:** `com.elena.mojmaliplaner`  
> **SDK Compatibility:** Min SDK 26 (Android 8.0), Target SDK 35 (Android 15), Compile SDK 37  

---

## 📌 Executive Overview

**Moj Mali Planer** is a cozy, offline-first Android application designed for children and young students (and their parents). It provides an intuitive, warm Material 3 UI for managing daily tasks, school schedules, free-time ideas, virtual tamagotchi-style pet care, custom MP3/Lofi audio playback, protected diary entries, and data backup.

### Core Philosophy:
1. **100% Offline & Private:** Zero telemetry, zero external tracking, zero analytics servers. All data remains on the device inside Room DB and DataStore.
2. **Cozy Material 3 Aesthetic:** Warm pastel color palette (`#F6F4EE` Light / `#18191E` Dark), soft gold accents (`#CBA84E`), pastel blue (`#6EC5E9`), and purple (`#C49ED4`).
3. **Bilingual & Locale Aware:** Dynamically detects SIM/Network Country ISO (`"rs"`) to automatically serve Serbian (SRP) or English (ENG) without requiring manual language toggling.
4. **Child-Safe & Non-Intrusive:** 100% Freeware with optional "Buy me a coffee" support (NBS IPS QR Code for Serbia, PayPal for International).

---

## 🛠️ Tech Stack & Architecture Pattern

```
+-----------------------------------------------------------------------+
|                              UI LAYER                                 |
|         Jetpack Compose (Material 3) + StateFlow Collection           |
|  HomeScreen, CalendarScreen, DailyPlanScreen, StudyScreen,            |
|  FreeTimeScreen, PetsScreen, MusicScreen, DiaryScreen, Dialogs        |
+-----------------------------------------------------------------------+
                                   |
                                   v
+-----------------------------------------------------------------------+
|                           VIEWMODEL LAYER                             |
|          MainViewModel (StateFlows, DB CRUD, Preferences)             |
|          MusicViewModel (MediaPlayer, Progress Loop, SAF Audio IO)    |
+-----------------------------------------------------------------------+
                                   |
                                   v
+-----------------------------------------------------------------------+
|                            DATA LAYER                                 |
|  AppRepository (Room DB 2.8.5) <---> UserPreferencesRepository (DS)   |
|  BackupManager (JSON Export/Import) <---> CountryDetector (Telephony) |
+-----------------------------------------------------------------------+
```

| Component | Library / Framework | Purpose |
| :--- | :--- | :--- |
| **Language** | Kotlin 2.2.10 | Core programming language |
| **UI Framework** | Jetpack Compose (BOM 2026.02.01) | Declarative UI, Material 3 Components |
| **Architecture** | MVVM + Repository Pattern | Clean separation of concerns with `AndroidViewModel` |
| **Database** | Room 2.8.5 + KSP 2.3.12 | Local SQLite persistence with coroutines |
| **Preferences** | AndroidX DataStore Preferences 1.2.1 | Fast async key-value storage for user profile & settings |
| **Navigation** | Navigation Compose 2.10.2 | Single-activity composable routing (`NavHost`) |
| **Media Playback** | Android `MediaPlayer` | Looped Lofi/Ambience audio & custom MP3 playback |
| **Image Loading** | Coil Compose 2.7.0 | Asynchronous image rendering for sheet music & gallery |
| **QR Code Gen** | ZXing (`com.google.zxing:core:3.5.3`) | Dynamic NBS IPS QR Code generation for Serbian donations |
| **Date & Time** | `java.time.*` (LocalDate, YearMonth) | Exact real-world calendar calculation |

---

## 🗄️ Data Architecture & Database Schemas

The app uses **Room Database v2** (`AppDatabase.kt`) alongside **Preferences DataStore** (`UserPreferencesRepository.kt`).

### 1. Room Entities (`data/db/Entities.kt`)

#### `TaskEntity` (Table: `tasks`)
Represents calendar events, daily plans, school homework, and free time entries.
- `id` (`Long`, Primary Key, AutoGenerate): Unique ID
- `title` (`String`): Title of the task/idea
- `dateMillis` (`Long`): Epoch timestamp for date association
- `category` (`String`): Category tag (`"CALENDAR"`, `"DNEVNI_PLAN"`, `"ŠKOLA"`, `"SLOBODNO_IDEJA"`, `"SLOBODNO_MEDA"`, `"SLOBODNO_ZELJA"`)
- `colorHex` (`String`): Hex color code for UI tag (`"#6EC5E9"`, `"#C49ED4"`, `"#CBA84E"`, etc.)
- `isCompleted` (`Boolean`): Completion status
- `subjectName` (`String?`): Optional field (used for school subject or book 5-star rating)

#### `PetLogEntity` (Table: `pet_logs`)
Stores Tamagotchi-style pet interaction logs.
- `id` (`Long`, Primary Key, AutoGenerate): Unique ID
- `petType` (`String`): `"MIA_MACKA"` (Cat) or `"BLEKI_PAS"` (Dog)
- `actionType` (`String`): ActionPerformed (`"HRANA"`, `"VODA"`, `"CETKANJE"`, `"MAZENJE"`, `"SETNJA"`)
- `timestampMillis` (`Long`): Epoch timestamp of care action
- `isDoneToday` (`Boolean`): Daily completion flag

#### `DiaryEntryEntity` (Table: `diary_entries`)
Stores protected personal secret diary entries.
- `id` (`Long`, Primary Key, AutoGenerate): Unique ID
- `dateMillis` (`Long`): Epoch timestamp
- `title` (`String`): Diary title
- `content` (`String`): Full text body
- `moodEmoji` (`String`): Mood icon (`"🐱"`, `"🌸"`, `"🦉"`, `"🌙"`, `"💖"`)
- `imageUri` (`String?`): Optional attached photo URI

#### `MusicSheetEntity` (Table: `music_sheets`)
Stores sheet music images and notes for music school.
- `id` (`Long`, Primary Key, AutoGenerate): Unique ID
- `title` (`String`): Sheet music title
- `instrumentOrCategory` (`String`): Instrument or category name
- `fileUri` (`String`): PhotoPicker URI string

#### `CustomTrackEntity` (Table: `custom_tracks`)
Stores imported MP3 audio file metadata.
- `id` (`Long`, Primary Key, AutoGenerate): Unique ID
- `title` (`String`): Track display title
- `filePath` (`String`): Internal app storage file path (`files/custom_audio/custom_*.mp3`)
- `category` (`String`): Track category tag (`"MOJA_MUZIKA"`)

---

### 2. DataStore Preferences Keys (`data/UserPreferencesRepository.kt`)

- `user_name` (`String`, default `"Elena"`): Child/User name
- `user_pin` (`String`, default `"1111"`): 4-digit PIN for Diary & System Settings
- `cat_name` (`String`, default `"Mia"`): Custom cat name
- `dog_name` (`String`, default `"Bleki"`): Custom dog name
- `is_onboarding_completed` (`Boolean`, default `false`): Onboarding flag
- `is_dark_theme` (`Boolean`, default `false`): Dark/Light theme toggle

> **Critical Fix Note:** `userPin` in `MainViewModel` uses `SharingStarted.Eagerly` so that PIN verification works instantaneously in the background without requiring active UI composable collection.

---

## 📱 Detailed Feature & Screen Breakdown

### 1. Onboarding Flow (`OnboardingScreen.kt`)
- Triggered on first launch if `is_onboarding_completed == false`.
- Collects user name and initial 4-digit PIN.
- Saves preferences and redirects to Home Screen (`Routes.HOME`).

### 2. Home Screen (`HomeScreen.kt`)
- **Grid Layout:** Displays 6 main category cards + 1 wide Protected Diary card at the bottom.
- **Top Bar:**
  - **Cat Mascot Icon (`🐱`)**: Interactive icon. Long-press triggers `PinDialog.kt` to open Hidden System Settings (`SettingsDialog.kt`).
  - **App Title**: Localized title (`R.string.app_name`).
  - **Theme Toggle (`☀️`/`🌙`)**: Switches Material 3 Light/Dark color palette.
  - **"Pošalji plan" (Send Plan) Button**:
    - Summarizes all today's tasks into a cute, formatted message.
    - Automatically copies the plan to System Clipboard.
    - Launches native Android Share Sheet (`Intent.ACTION_SEND`, `text/plain`) allowing direct sharing via Viber, WhatsApp, SMS, or Notes.

### 3. Schedule Calendar (`CalendarScreen.kt`)
- **View Switcher:** `SingleChoiceSegmentedButtonRow` for **Monthly Grid 🗓️** and **Weekly Agenda 📆**.
- **Real-Time Calendar Engine:** Built on `java.time.LocalDate` and `java.time.YearMonth`. Calculates exact starting day of the week, total days in month, and week ranges.
- **Task Association:** Tasks are strictly associated with dates via `TaskEntity.dateMillis` converted to `LocalDate`.
- **Per-Day Task Filtering & Input:** Adding a task on a specific day records the exact `dateMillis` of that selected day, preventing task duplication across unrelated days.
- **Safe Hex Color Parsing:** Uses `Color(android.graphics.Color.parseColor(task.colorHex))` with fallback protection against invalid hex strings.

### 4. Today's Plan (`DailyPlanScreen.kt`)
- Focused daily checklist for immediate tasks (`category == "DNEVNI_PLAN"`).
- **Quick Templates ⚡:** Instant task creation chips ("Sredi sobu 🧹", "Uradi domaći 📚", "Spakuj ranac 🎒", "Popij vodu 💧").
- **Gamified Reward System:** Displays gold star counter incremented as daily tasks are checked off.

### 5. Study & Homework (`StudyScreen.kt`)
- **Tab 1 — Timetable 🗓️:** School schedule organized by days of the week (Monday through Sunday).
- **Tab 2 — Homework & Exams 📝:** Backlog of assignments and test dates with completion checkmarks.

### 6. Free Time & Hobbies (`FreeTimeScreen.kt`)
- **Tab 1 — Games & Drawing 🎨** (`SLOBODNO_IDEJA`)
- **Tab 2 — Books & Movies 🎬** (`SLOBODNO_MEDA`) — Includes interactive 1-to-5 star rating selector saved into `TaskEntity.subjectName`.
- **Tab 3 — Weekend Wishes ⭐** (`SLOBODNO_ZELJA`)
- **Room Database Persistence:** All entries are stored in Room (`TaskEntity`). Items persist permanently across app restarts and screen transitions.
- **Auto-Seeding:** Uses `LaunchedEffect` to seed default cozy ideas on initial launch if categories are empty.

### 7. Pets Tamagotchi Mode (`PetsScreen.kt`)
- Interactive Tamagotchi care cards for Cat (`catName`) and Dog (`dogName`).
- Care buttons: Feed 🍖, Give Water 💧, Brush 🪮, Cuddle 💖, Walk 🦮, Play 🎾.
- Logs every care interaction into `PetLogEntity` with timestamp.
- **Unlockable Pets Expansion:** Dynamically loads up to 5 legendary pets (Axolotl, Capybara, Red Panda, Fennec Fox, Snowy Owl) once their corresponding sticker is unlocked in `unlocked_stickers` table. These pets share full interaction logic with the core cat/dog.

### 8. Music & Lofi Corner (`MusicScreen.kt` & `MusicViewModel.kt`)
- **Tab 1 — Cozy Lofi Player 🎧:**
  - Built-in factory tracks: Lofi melodies (*Tokyo Vibe*, *Sonder Beat*, *Classic Hip-Hop*) & Sound Ambience (*Calming Rain*, *Cute Cat*, *Cat Meow*, *Dog Bark*).
  - **Custom MP3 Import:** SAF picker (`ActivityResultContracts.GetContent("audio/*")`). Selected files are safely copied to app internal storage (`files/custom_audio/custom_*.mp3`) for offline playback, recorded in `CustomTrackEntity`, and displayed in "Moje Pesme (MP3)" list with delete support.
  - **Subtle Circular Progress Bar:** Dual `CircularProgressIndicator` layered around the cat mascot avatar showing real-time song progress arc (`currentPositionMs` / `trackDurationMs`) updated via 200ms coroutine loop.
  - **Audio Track Sharing (`AudioShareHelper.kt`):** Users can share both custom imported MP3s and built-in Lofi tracks (via localized cache cloning) using Android Share Sheet.
  - **Asynchronous MediaPlayer:** Uses `prepareAsync()` and `setOnPreparedListener` to avoid UI main-thread freezing (ANR protection).
  - **Sleep Timer & Volume:** Volume slider (0.0 to 1.0) and 15, 30, 45 minute auto-stop sleep countdown.
- **Tab 2 — Sheet Music Gallery 🎼:**
  - PhotoPicker integration (`PickVisualMedia`) for capturing and saving sheet music photos.

### 9. Protected Secret Diary (`DiaryScreen.kt`)
- Protected by 4-digit PIN authentication modal (`PinDialog.kt`).
- Create, read, and delete diary entries with date, title, text body, and mood emojis (🐱, 🌸, 🦉, 🌙, 💖).

### 10. Hidden System Settings (`SettingsDialog.kt`)
- Triggered by long-pressing top bar cat icon (`🐱`) on Home Screen + entering 4-digit PIN.
- Options to edit User Name, Cat Name, Dog Name, and change 4-digit PIN.
- **Backup & Restore System (`BackupManager.kt`)**:
  - **`Napravi rezervnu kopiju (Backup) 💾`**: Exports all user preferences, tasks, pet logs, diary entries, and sheet music into a formatted JSON string, saves to `cacheDir`, and opens Android Share Sheet.
  - **`Vrati podatke iz kopije (Restore) 📂`**: Uses SAF document picker (`*/*`) to import JSON backup file, parse entries, and re-hydrate Room Database & DataStore.
- **"O aplikaciji i Privatnost ℹ️" Button**: Opens `AboutAppDialog.kt`.
- **"Resetuj sve podatke ⚠️" Button**: Wipes Room tables, resets DataStore, and deletes internal `custom_audio` files.

### 11. About App & Privacy (`AboutAppDialog.kt`)
- Displays app version dynamically (`Version: 1.3`).
- **Privacy Manifesto:** Clarifies 100% Freeware, 100% offline, zero data collection. Removed all external links or payment prompts to comply with Google Play Families policy.

---

## 🌍 Global Locale & Country Detection (`locales_config.xml`)

The app dynamically sets locale via Android's native system properties rather than forcing a SIM-based override.
- **Android 13+ Per-App Language:** Included `res/xml/locales_config.xml` to specify `en` and `sr` as supported locales.
- **Language Selection:** Language logic checks active system locale via `LocalConfiguration.current.locales[0].language`.

---

## 📂 Project Structure & Key Files

```
com.elena.mojmaliplaner
├── MainActivity.kt                      # Main entry activity
├── data
│   ├── AppRepository.kt                 # Central Room DB repository
│   ├── BackupManager.kt                 # JSON Backup & Restore exporter/importer
│   ├── StickerCatalog.kt                # Static data catalog for 20 collectible animal stickers
│   ├── UserPreferencesRepository.kt     # Preferences DataStore manager
│   ├── quiz
│   │   ├── QuizModels.kt                # QuizQuestion data model and schemas
│   │   ├── QuizPackDownloader.kt        # Remote JSON pack fetcher & downloader via HttpURLConnection
│   │   └── QuizRepository.kt            # Handles reading starter_quiz.json and imported packs
│   └── db
│       ├── AppDatabase.kt               # Room database configuration (v3, with migrations)
│       ├── Daos.kt                      # Room DAOs (TaskDao, PetLogDao, DiaryDao, StickerDao)
│       └── Entities.kt                  # Room Entities (Task, PetLog, Diary, MusicSheet, CustomTrack, UnlockedSticker)
└── ui
    ├── navigation
    │   └── AppNavigation.kt             # Compose NavHost & route declarations
    ├── screens
    │   ├── AboutAppDialog.kt            # Privacy manifesto, Version info
    │   ├── CalendarScreen.kt            # Monthly Grid & Weekly Agenda with real LocalDate
    │   ├── DailyPlanScreen.kt           # Daily checklist, templates & star reward counter
    │   ├── DiaryScreen.kt               # Secret diary entries with mood emojis
    │   ├── FreeTimeScreen.kt            # Hobbies, books/movies ratings & weekend wishes (Room backed)
    │   ├── HomeScreen.kt                # Main grid, cat long-press trigger & Send Plan action
    │   ├── LanguageGameScreen.kt        # Upcoming game screen using quiz packs
    │   ├── LanguagePacksDialog.kt       # Remote quiz pack downloader and importer
    │   ├── MusicScreen.kt               # Lofi player, circular progress ring, SAF MP3 & Sheet music
    │   ├── OnboardingScreen.kt          # Profile & PIN setup on first launch
    │   ├── PetsScreen.kt                # Tamagotchi cat & dog care logging + unlocked legendaries
    │   ├── PinDialog.kt                 # 4-digit PIN authentication keypad dialog
    │   ├── SettingsDialog.kt            # System settings, PIN update, Backup/Restore & Reset
    │   ├── StickerAlbumScreen.kt        # Album UI for unlocked stickers, 3⭐ scratch pack game
    │   └── StudyScreen.kt               # School timetable & homework backlog
    ├── theme
    │   ├── Color.kt                     # Pastel Blue, Purple, Soft Gold & Dark theme colors
    │   ├── Theme.kt                     # Material 3 light/dark color scheme definitions
    │   └── Type.kt                      # Typography configurations
    └── viewmodel
        ├── MainViewModel.kt             # Central ViewModel managing DB & DataStore StateFlows
        └── MusicViewModel.kt            # MediaPlayer, circular progress loop, sleep timer & SAF IO
```

---

## 💡 Potential Expansion Ideas for Future Development (Brainstorming Guide for AI Agent)

When analyzing this application for next-stage enhancements, consider these natural feature evolutions:

1. **Pomodoro / Focus Study Timer:** Add an interactive countdown timer in `StudyScreen` with lofi background music integration.
2. **Interactive Pet Animations & Levels:** Enhance `PetsScreen` with animated cat/dog avatars (Lottie animations) that gain happiness levels based on care logs.
3. **Android Home Screen Widgets:** Develop App Widgets for today's tasks, daily star counter, or quick pet care buttons.
4. **Voice Note Recording in Secret Diary:** Allow audio recording attachments inside protected diary entries.
5. **PDF Export for Timetable & Weekly Plan:** Export weekly agenda or school timetable as a printable PDF.
