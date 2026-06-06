As an expert Android Architect, initialize a Planning and Scaffold routine for a new native Android app named "VannaMaayamTamil".

Our target audience is children aged 4-7 learning spoken Tamil. The UI must be built using Jetpack Compose, targeting a minimum SDK of 26 (Android 8.0).

---

CORE ARCHITECTURE & DATA STRUCTURE REQUIREMENTS:

1. ARCHITECTURE PATTERN: Use clean MVVM (Model-View-ViewModel) architecture.
2. DEPENDENCIES TO ADD:
   - Add necessary lifecycle, viewmodel, and compose navigation dependencies to build.gradle.
   - Include placeholders for Lottie/Rive animation runtime dependencies.
3. RELEVENT ASSET DIRECTORIES: Generate the standard directory structures, including an explicit 'res/raw/' folder to store the developer's custom voice .mp3 recordings.

---

CORE CODE MODULES TO GENERATE:

Module A: Speech & Audio Engine

- Create a 'TamilSpeechManager.kt' class using the native Android SpeechRecognizer API.
- Hardcode the intent language configuration explicitly to "ta-IN" (Tamil - India).
- Configure the speech recognizer to be resilient to toddler speech patterns (handle partial results, continuous listening, and audio level callbacks).
- Add runtime check and permission requests for Manifest.permission.RECORD_AUDIO.

Module B: AI Agent Logic and State Models

- Create the core data models representing our AI Agent interaction.
- Map out the exact JSON schemas for inputs and outputs.
- Input model: GameState (current_animal: String, target_color_tamil: String, child_audio_transcript: String).
- Output model: ThuliAgentResponse (status: Enum[SUCCESS, HINT], animation_state: String, visual_cue_asset: String, voice_clip_id: String).
- Create a 'VoicePlayerManager.kt' helper that takes a 'voice_clip_id' string and dynamically plays the corresponding audio resource from 'res/raw/'.

Module C: Jetpack Compose UI

- Create an interactive dashboard screen representing the "Vanna Maayam" mode.
- The UI should feature a prominent character canvas area (for Thuli and the Target Animal) and a microphone action trigger.
- Implement an absolute zero-text UX paradigm (no text fields, labels, or keyboards). The layout must rely entirely on visual cues, state-driven color overlays, and voice commands.

---

EXECUTION STEPS FOR ANTIGRAVITY:

1. Provide a step-by-step implementation plan.
2. Generate the necessary boilerplate files, directory structure, and updated build.gradle file configuration.
3. Automatically execute code assembly for the foundational classes defined above.
