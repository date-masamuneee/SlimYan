# 実装計画 — 週間メニューテンプレ中心の新機能群（v2）

SPEC.md（更新済み）に基づく。核は **週間メニューテンプレ → 当日展開** のループ。
※ MVP（設定/体重/食品/食事/筋トレ/ダッシュボード/リカバリT8）は実装済み。本計画はその上に積む新機能群。

## 前提・横断方針

- **黄金ルール厳守**：個人データはコードに焼き込まない。シード/ロジックは汎用のみ。兄弟の常用食品・テンプレ・目標はUI経由でDBに入れる（→ SPEC §1）。
- **DBマイグレーション切替（重要）**：現状 `fallbackToDestructiveMigration()` ＝スキーマ変更でデータ全消去。これから実データが溜まるので、**T1以降は加算的 `Migration`（新規 CREATE TABLE のみ）で既存データを保護**する。`fallback` は最終手段として残す。
- **テスト方針**：
  - 純粋ロジック（栄養計算・展開マッピング・買い物集計・進捗判定・曜日マスク・監査ペイロード生成）は **JVM unit test**（`./gradlew test`）で RED→GREEN。
  - Room DAO / WorkManager / Compose UI は **ビルド成功＋実機スモーク**で検証（エミュレータ無しのため instrumented test は任意・別枠）。
- **ビルド検証**：`./gradlew test` ＋ `./gradlew assembleDebug`（JAVA_HOME=Android Studio jbr）。
- **コミット**：1タスク=1コミット。触ったファイル＋タスク状態のみステージ。

## 依存グラフ

```
T1 テンプレ データ基盤(+加算migration) ──> T2 テンプレ編集UI ──> T3 当日展開 ──[CP-A]
                                       ├──> T4 買い物リスト ──[CP-B]
                                       └──> T5 AI監査 ──[CP-C: 課金API確認]
T6 リマインド基盤(WorkManager+権限) ──> T7 リマインド設定UI ──[CP-D: 通知/権限確認]
T8 筋トレ固定プログラム＋進捗 ──[CP-E]
T9 テスト拡充・仕上げ
```

T1→T2→T3 が最優先（核）。T4/T5 は T1 のデータ基盤に乗る。T6/T7（リマインド）と T8（筋トレ）は独立着手可。

---

## Phase 1 — 週間メニューテンプレ（★核）

### T1: テンプレ データ基盤 ＋ 加算マイグレーション切替
- **内容**
  - `MealTemplateItem` entity（id, dayOfWeek:Int 1-7, mealSlot:String, foodId:Long, grams:Float, sortOrder:Int）
  - `MealTemplateDao`（getAll:Flow / getForDay(dayOfWeek):Flow / insert / update / delete / deleteForSlot）
  - `MealTemplateRepository` ＋ Hilt provider（DatabaseModule）
  - `AppDatabase` に entity 追加、version 2→3、`Migration(2,3)` で `CREATE TABLE meal_template_item ...`、DatabaseModule で `.addMigrations(MIGRATION_2_3)`
  - 純粋ヘルパ `NutritionMath`：food + grams → (kcal,P,F,C)。式 `per_serving * grams / servingGrams`
- **受け入れ基準**：永続化＆dayOfWeekクエリ可／既存データが消えない（加算migration）／NutritionMath が一食分スケールを正しく計算
- **検証**：JVM `NutritionMathTest` → `./gradlew test`／`assembleDebug`／手動でプロフィール残存確認

### T2: テンプレ編集UI（食事タブのサブ画面化）
- **内容**
  - 食事タブ内サブ画面 toggle（「記録」／「週間テンプレ」）。ボトムナビ5タブ維持
  - 曜日（月〜日）× slot（朝/昼/夜/間食）で品目を追加（マイ食品ピッカー＋grams）/削除/並べ替え
  - 曜日別合計・週合計の kcal/PFC＋目標差分（`NutritionMath`）
  - `MealTemplateViewModel`／集計は純粋関数 `TemplateSummary.calc(items, foods)`
- **受け入れ基準**：品目追加→保存→再表示で残る／曜日別・週合計が正しい
- **検証**：JVM `TemplateSummaryTest`／ビルド＋手動（月曜3品で合計確認）

### T3: 当日展開（テンプレ → 記録）
- **内容**
  - 「今日の分を記録に展開」ボタン
  - 今日の曜日の template items を当日 `MealEntry` へ一括 insert（isPlanned=true, isChecked=false, foodId保持, foodName/calories スナップショット）
  - 重複展開ガード（当日展開済みなら確認ダイアログ）
  - 展開マッピングを純粋関数 `TemplateExpander.expand(items, foods, dateEpochDay)`
- **受け入れ基準**：展開で当日記録に並ぶ／チェックでカロリー集計反映／二重展開防止
- **検証**：JVM `TemplateExpanderTest`／ビルド＋手動

> **CP-A**：テンプレ作成 → 当日展開 → 記録 → チェック → ダッシュボード反映 の核ループを手動確認。

---

## Phase 2 — 買い物リスト

### T4: 買い物リスト（テンプレ集計）
- **内容**
  - 全曜日 items を食材ごと集計：個数 = Σ(grams)/servingGrams、総グラム
  - 食事タブのサブ画面 or ボトムシート（例「サラダチキン ×13」）
  - N週倍率（MVPは×1）
  - 純粋関数 `ShoppingListCalculator.calc(items, foods, weeks=1)`
- **受け入れ基準**：食材別の必要個数/グラムが正しい（複数曜日の同一食材が合算）
- **検証**：JVM `ShoppingListCalculatorTest`／ビルド＋手動

> **CP-B**：テンプレ → 買い物リスト算出の整合を手動確認。

---

## Phase 3 — AI監査

### T5: AI食事プラン監査（Claude API）
- **内容**
  - `AiRepository.auditMealPlan(template, profile): Result<MealPlanAudit>`
  - 送信ペイロード生成を純粋関数 `AuditRequestBuilder.build(items, foods, profile)`（週×slot×品目×kcal/PFC＋目標＋TDEE）
  - レスポンス：診断サマリ（優先度順）＋修正提案（曜日×slot差し替え案）。`stripCodeFence`＋JSONパース流用
  - プレビュー（前後比較）＋チャット微調整→再診断（生成セッション内マルチターン）／確認で `MealTemplateItem` に差分反映
- **受け入れ基準**：診断＋修正提案が返る／確認で反映・キャンセルで不変
- **検証**：JVM `AuditRequestBuilderTest`＋パーステスト／ビルド＋手動（実APIキー）

> **CP-C（要確認・課金）**：実 Claude API＝コスト発生。実行前に兄弟確認。出力は必ずプレビュー→確認保存（自動反映しない）。

---

## Phase 4 — リマインド（食事＋筋トレ）

### T6: リマインド基盤（WorkManager ＋ 権限）
- **内容**
  - `Reminder` entity（id, type:"meal"|"workout", mealSlot:String?, label:String, hour:Int, minute:Int, daysOfWeekMask:Int, enabled:Boolean）
  - `ReminderDao` / `ReminderRepository` / Hilt provider
  - DB version 3→4、`Migration(3,4)` で CREATE TABLE
  - WorkManager 依存追加（libs.versions.toml＋build.gradle）／通知チャンネル（Application）
  - `ReminderScheduler`：enabled な Reminder を曜日マスクで次回時刻にスケジュール／キャンセル
  - `POST_NOTIFICATIONS` 実行時要求（MainActivity, Android 13+）。拒否でも他機能は動作
  - 純粋ロジック `ReminderTiming.shouldFireOn(mask, dayOfWeek)` / `nextTrigger(reminder, now)`
- **受け入れ基準**：保存→指定時刻に通知／権限拒否でもクラッシュせず他機能動作
- **検証**：JVM `ReminderTimingTest`／ビルド＋手動（直近時刻で発火確認）

### T7: リマインド設定UI
- **内容**
  - 設定タブに「リマインド」：食事/筋トレ一覧＋追加/編集（type, mealSlot, 時刻ピッカー, 曜日トグル, ON/OFF）
  - 保存で `ReminderScheduler` 再スケジュール
  - 曜日トグル↔ビットマスク変換を純粋関数 `DayMask`
- **受け入れ基準**：食事/筋トレのリマインドを追加・編集・ON/OFF／曜日トグルがマスクと往復一致
- **検証**：JVM `DayMaskTest`／ビルド＋手動

> **CP-D（外部作用・権限）**：通知は外部作用。権限フロー・発火を実機確認。

---

## Phase 5 — 筋トレ固定プログラム＋進捗

### T8: 固定プログラム＋ダブルプログレッション
- **内容**
  - `WorkoutProgramItem` entity（id, exerciseId:Long, targetSets:Int, repCeiling:Int, sortOrder:Int）＋ DAO/Repo（汎用：特定6種目は焼き込まない）
  - DB version 4→5、`Migration(4,5)` で CREATE TABLE
  - 筋トレタブに「今日のプログラム」。前回履歴で全セットが repCeiling 到達なら「重量UP」バッジ
  - 進捗判定を純粋関数 `ProgressionAdvisor.shouldIncreaseWeight(prevSets, targetSets, repCeiling)`
- **受け入れ基準**：プログラム種目登録可／進捗条件で重量UP提案
- **検証**：JVM `ProgressionAdvisorTest`（境界値）／ビルド＋手動

> **CP-E**：筋トレプログラム＋進捗提案を手動確認。

---

## Phase 6 — 仕上げ

### T9: テスト拡充・整理
- **内容**：`Example*Test` スタブ整理／`TdeeCalculator` JVMテスト追加（SPEC §9）／主要純粋ロジックのテスト棚卸し／（任意）Room in-memory DAOテスト
- **受け入れ基準**：`./gradlew test` 全グリーン
- **検証**：`./gradlew test`＋`assembleDebug`

---

## リスク・要確認ポイント

| 項目 | リスク | 対応 |
|------|--------|------|
| DBマイグレーション | 破壊的のままだと実データ消失 | T1で加算migrationに切替（最優先） |
| AI監査（T5/CP-C） | 課金API・出力の誤り | 実行前確認・プレビュー必須・自動保存しない |
| リマインド（T6/CP-D） | 通知権限・端末依存 | 権限拒否でも動作・実機確認 |
| 黄金ルール | 個人データ焼き込み | シード/ロジックは汎用のみで一貫 |

詳細タスクは [todo.md](todo.md)。
