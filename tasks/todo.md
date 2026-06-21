# タスクリスト — 週間テンプレ中心の新機能群（v2）

記法: `[ ]` 未着手 / `[~]` 進行中 / `[x]` 完了
※ MVP（旧 T0〜T8 リカバリ）は実装済み。本リストは新機能群。

---

## Phase 1 — 週間メニューテンプレ（★核 / CP-A ゲート）

### [x] T1. テンプレ データ基盤 ＋ 加算マイグレーション切替
- **deps**: なし（MVP基盤の上）
- **acceptance**:
  - `MealTemplateItem` entity（dayOfWeek 1-7, mealSlot, foodId, grams, sortOrder）
  - `MealTemplateDao` / `MealTemplateRepository` / Hilt provider
  - DB version 2→3、`Migration(2,3)` で CREATE TABLE、`addMigrations` 適用、既存データ保護
  - 純粋ヘルパ `NutritionMath`（food+grams → kcal/P/F/C）
- **test**: JVM `NutritionMathTest`
- **verify**: `./gradlew test` ＋ `assembleDebug` ＋ 既存プロフィール残存

### [ ] T2. テンプレ編集UI（食事タブのサブ画面化）
- **deps**: T1
- **acceptance**:
  - 食事タブ内サブ画面 toggle（記録／週間テンプレ）、ボトムナビ5タブ維持
  - 曜日×slot で品目 追加（マイ食品＋grams）/削除/並べ替え
  - 曜日別・週合計 kcal/PFC＋目標差分
  - `MealTemplateViewModel`、集計は `TemplateSummary.calc`
- **test**: JVM `TemplateSummaryTest`
- **verify**: ビルド＋手動（月曜3品で合計確認）

### [ ] T3. 当日展開（テンプレ → 記録）
- **deps**: T1, T2
- **acceptance**:
  - 「今日の分を展開」で今日の曜日 items を当日 MealEntry へ一括 insert（スナップショット）
  - 二重展開ガード
  - 展開マッピングは `TemplateExpander.expand`
- **test**: JVM `TemplateExpanderTest`
- **verify**: ビルド＋手動（展開→記録→チェック→ダッシュボード反映）

> ✅ **CP-A**: 核ループ（テンプレ→展開→記録→チェック→ダッシュボード）を手動確認して停止。

---

## Phase 2 — 買い物リスト（CP-B ゲート）

### [ ] T4. 買い物リスト（テンプレ集計）
- **deps**: T1
- **acceptance**:
  - 全曜日集計で食材別の必要個数（Σgrams/servingGrams）・総グラム
  - 食事タブのサブ画面 or ボトムシート表示、N週倍率（MVP×1）
  - `ShoppingListCalculator.calc`
- **test**: JVM `ShoppingListCalculatorTest`
- **verify**: ビルド＋手動

> ✅ **CP-B**: テンプレ→買い物リスト算出の整合を確認。

---

## Phase 3 — AI監査（CP-C ゲート / 課金）

### [ ] T5. AI食事プラン監査（Claude API）
- **deps**: T1（テンプレ存在）
- **acceptance**:
  - `AiRepository.auditMealPlan(template, profile)`
  - ペイロード生成 `AuditRequestBuilder.build`
  - 診断サマリ＋修正提案を返す、プレビュー（前後比較）＋チャット微調整→再診断
  - 確認で `MealTemplateItem` に差分反映、キャンセルで不変
- **test**: JVM `AuditRequestBuilderTest` ＋ レスポンスパース
- **verify**: ビルド＋手動（実APIキー）

> ✅ **CP-C（要確認・課金）**: 実API＝コスト発生。実行前に兄弟確認。自動反映しない。

---

## Phase 4 — リマインド（食事＋筋トレ / CP-D ゲート）

### [ ] T6. リマインド基盤（WorkManager ＋ 権限）
- **deps**: なし
- **acceptance**:
  - `Reminder` entity（type meal/workout, mealSlot?, label, hour, minute, daysOfWeekMask, enabled）
  - DAO / Repository / Hilt、DB 3→4 ＋ `Migration(3,4)`
  - WorkManager 依存追加、通知チャンネル、`ReminderScheduler`
  - `POST_NOTIFICATIONS` 実行時要求（拒否でも他機能動作）
  - 純粋 `ReminderTiming`（曜日マスク・次回時刻）
- **test**: JVM `ReminderTimingTest`
- **verify**: ビルド＋手動（直近時刻で発火）

### [ ] T7. リマインド設定UI
- **deps**: T6
- **acceptance**:
  - 設定タブにリマインド一覧＋追加/編集（type, mealSlot, 時刻, 曜日トグル, ON/OFF）
  - 保存で再スケジュール、曜日トグル↔マスク `DayMask`
- **test**: JVM `DayMaskTest`
- **verify**: ビルド＋手動

> ✅ **CP-D（外部作用・権限）**: 権限フロー・発火を実機確認。

---

## Phase 5 — 筋トレ固定プログラム＋進捗（CP-E ゲート）

### [ ] T8. 固定プログラム＋ダブルプログレッション
- **deps**: なし（既存 Exercise/SetEntry の上）
- **acceptance**:
  - `WorkoutProgramItem` entity（exerciseId, targetSets, repCeiling, sortOrder）＋ DAO/Repo
  - DB 4→5 ＋ `Migration(4,5)`
  - 「今日のプログラム」表示、前回全セット repCeiling 到達で「重量UP」バッジ
  - 進捗判定 `ProgressionAdvisor.shouldIncreaseWeight`
- **test**: JVM `ProgressionAdvisorTest`（境界値）
- **verify**: ビルド＋手動

> ✅ **CP-E**: プログラム＋進捗提案を手動確認。

---

## Phase 6 — 仕上げ

### [ ] T9. テスト拡充・整理
- **deps**: T1〜T8
- **acceptance**: `Example*Test` 整理／`TdeeCalculator` JVMテスト追加／純粋ロジックのテスト棚卸し／`./gradlew test` 全グリーン
- **verify**: `./gradlew test` ＋ `assembleDebug`
