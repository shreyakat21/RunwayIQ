# RunwayIQ

AI-powered startup CFO dashboard built with Kotlin + Compose Desktop.

## What it does

- Tracks revenue and expenses by month and category
- Computes burn rate, runway, MRR, and MRR growth live
- Renders a revenue vs. expenses chart using Compose Canvas
- Named scenarios (e.g. "Base case", "Lean mode") with instant switching
- CFO chat panel powered by Claude — asks questions against your actual numbers, streamed token by token
- All data stored locally in `~/.runwayiq/runway.db` — nothing leaves your machine

## Requirements

- JDK 17+
- Gradle (wrapper included)
- A Claude API key (get one at console.anthropic.com)

## Setup

```bash
git clone <repo>
cd RunwayIQ
./gradlew run
```

On first launch:
1. Go to **Settings** and paste your Claude API key
2. Go to **Scenarios** — a default "Base case" scenario is created automatically. Edit the starting cash balance.
3. Add revenue entries under **Revenue**
4. Add expense entries under **Expenses**
5. Return to **Dashboard** to see your metrics and chat with your CFO

## Build a native app

```bash
# macOS .dmg
./gradlew packageDmg

# Windows .msi
./gradlew packageMsi

# Linux .deb
./gradlew packageDeb
```

## Project structure

```
src/main/kotlin/com/runwayiq/
├── Main.kt                          # Entry point, window setup
├── ai/
│   └── ClaudeClient.kt              # Ktor-based Claude API with SSE streaming
├── data/
│   ├── db/
│   │   └── DatabaseFactory.kt       # SQLite driver via SQLDelight
│   ├── model/
│   │   └── Models.kt                # Domain data classes
│   └── repository/
│       └── FinancialRepository.kt   # All DB reads/writes
├── domain/
│   └── usecase/
│       └── FinancialSummaryUseCase.kt  # Metrics computation + Claude context builder
└── ui/
    ├── AppViewModel.kt              # StateFlow-based state management
    ├── NavScreen.kt
    ├── components/
    │   └── Components.kt            # MetricCard, RunwayChart, SideNav, etc.
    ├── screens/
    │   ├── DashboardScreen.kt       # Metrics + CFO chat panel
    │   └── Screens.kt               # Revenue, Expenses, Scenarios, Settings
    └── theme/
        └── Theme.kt                 # Material3 color scheme + typography

src/main/sqldelight/
└── RunwayDatabase.sq                # All SQL queries and schema
```

## Extending it

Ideas for going further:
- **CSV import** — Apache POI to parse bank exports and auto-categorize via Claude
- **Monte Carlo simulation** — randomize burn rate variance over N runs, show runway distribution
- **Board PDF export** — iText7 + headless Compose Canvas chart rendering
- **QuickBooks OAuth** — pull real transactions via their API
- **Anomaly detection** — Z-score per expense category, alert when a category spikes
