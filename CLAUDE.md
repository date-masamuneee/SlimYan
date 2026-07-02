# CLAUDE.md

## Commands

```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew installDebug
./gradlew test
./gradlew connectedAndroidTest
./gradlew lint
```

## Architecture

ダイエット総合サポートアプリ。Jetpack Compose + Material3 のネイティブ Android アプリ。
データは端末内 Room（SQLite）のみ。ログイン・クラウド同期なし。Claude API でリカバリ提案あり。

**コアバリュー：** 食事プランを事前に組む → 記録 → 崩れたら AI が当日・翌日の修正案を提案。

**Tech stack:**
- UI: Jetpack Compose + Material3, edge-to-edge
- アーキテクチャ: MVVM（ViewModel + StateFlow）
- DI: Hilt
- Database: Room
- Network: Ktor Client
- AI: Claude API（Haiku、リカバリ提案のみ）
- Navigation: Navigation Compose
- Min SDK 26 / Target SDK 34

**Namespace:** `com.example.slimyan`

**Claude API キー**は `local.properties` の `claude.api.key`（gitignore 済み）。`BuildConfig.CLAUDE_API_KEY` 経由で参照。

**依存バージョン**は `gradle/libs.versions.toml` で集中管理。先にここへ追加し、`app/build.gradle.kts` から `libs.*` で参照する。

## 構成

- `data/ai/` — Claude API クライアント（AiRepository）。プロンプト組立・JSON parse
- `data/` — Room レイヤ全般（entity / dao / repository）
- `di/` — Hilt モジュール（DatabaseModule / NetworkModule）
- `domain/` — ユースケース（TdeeCalculator / MealCalculator 等）
- `ui/` — 画面ごとの Composable + ViewModel
- `tasks/` — plan.md / todo.md（進捗管理）

## 方針

- 必要以上の機能追加・抽象化はしない
- 単位は kg・kcal 固定（MVP）
- DB スキーマ変更時は Room version を上げて migration を追加する
- 新ライブラリ追加・スキーマ変更・スコープ外機能は着手前に確認する（SPEC.md 参照）
