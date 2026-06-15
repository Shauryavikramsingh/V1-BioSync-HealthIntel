# Design Philosophy & Architecture: BioSync HealthIntel

## 1. Visual Language & UX Strategy
BioSync HealthIntel adopts a "Clinical Neural" aesthetic—a design language intended to instill confidence, clarity, and calm. By leveraging Material Design 3 (M3) components, the interface minimizes cognitive load, allowing users to focus on critical health data.

*   **Color Palette:** A foundation of deep, muted "system dark" tones, punctuated by high-contrast functional highlights (e.g., success greens for normal metrics, cautionary ambers for intermediate, and alert reds for serious diagnostic states).
*   **Typography:** Clean, geometric sans-serif fonts to ensure readability of medical parameters even on smaller display densities.
*   **Interaction:** Fluid navigation paths using Jetpack Compose animations, ensuring that moving between journaling and AI triage feels integrated rather than disruptive.

## 2. Architectural Blueprint (MVVM)
The application adheres to a strict Model-View-ViewModel (MVVM) separation of concerns:
*   **Model:** Represents the data structure, utilizing Room SQLite entities for persistence and Moshi for JSON handling.
*   **View:** Declarative UI built with Jetpack Compose. It responds to state changes rather than direct manipulation.
*   **ViewModel:** Acts as the mediator, exposing StateFlow to the UI, handling lifecycle-aware coroutines, and triggering business logic for the AI Triage engine.

## 3. The Clinical Neural Fabric (Logic Interaction)
The core of the application is a data-driven interaction model:
1.  **Ingestion:** Daily logs are parsed and indexed locally within the Room database.
2.  **Synthesis:** The RAG engine retrieves the two most recent longitudinal logs.
3.  **Evaluation:** These logs are synthesized by the Gemini model through an OkHttp pipe, returning a structured JSON response.
4.  **Action:** The Triage Evaluation Engine maps this response to a severity tier, driving immediate, context-aware feedback.

## 4. Design for Accessibility & Portability
*   **Edge-to-Edge Design:** Utilizing system insets to maximize screen real estate for symptom charting.
*   **Compressed Export Logic:** A design-first approach to the Share-to-ZIP engine. Instead of exposing raw file system paths, the user interacts with a high-contrast modal to select data volume, keeping the underlying complexity of FileProvider abstraction hidden.
*   **Offline-First Resilience:** The UI is designed to remain fully functional without network connectivity, with graceful UI transitions that indicate the "sync status" of the AI Triage engine.

---
*Architectural design documentation for BioSync HealthIntel, a joint development by Shaurya Vikram Singh and Veer Vikram Singh.*
