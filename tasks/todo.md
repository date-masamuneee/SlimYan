# タスクリスト — Slim-yan

記法: `[ ]` 未着手 / `[~]` 進行中 / `[x]` 完了

---

## P0 — プロジェクト雛形（CP-A ゲート）

### [ ] T0. Gradle・DI・NavHost 雛形
- **deps**: なし
- **acceptance**:
  - Gradle sync 成功（Kotlin 1.9.25 / KSP / Hilt / Room / Ktor / Compose BOM 2024.06）
  - `KintoreLogApplication`（@HiltAndroidApp）
  - `MainActivity` に NavHost、ボトムナビ5タブ（ホーム/食事/筋トレ/体重/設定）
  - 各タブにプレースホルダー画面（Text だけでOK）
- **verify**: `assembleDebug` 成功 → 実機起動 → 5タブを行き来できる
- ✅ **CP-A: ここで停止・ビルド結果を確認してから P1 へ**

---

## P1 — コア5パスを実機検証（CP-B ゲート）

### [ ] T1. データ層（Entity / DAO / DB / Repository）
- **deps**: T0
- **acceptance**:
  - Entity 6種: `UserProfile`, `BodyWeight`, `Food`, `MealEntry`, `Exercise`, `SetEntry`
  - DAO: `UserProfileDao`, `BodyWeightDao`, `FoodDao`, `MealEntryDao`, `ExerciseDao`, `SetEntryDao`
  - `AppDatabase`（Room, version=1）
  - `DatabaseModule`（Hilt）
  - Repository 4種: `ProfileRepository`, `BodyWeightRepository`, `FoodRepository`, `MealRepository`, `WorkoutRepository`
  - 主要種目プリセット（Exercise シードデータ）
  - 主要食品プリセット（Food シードデータ、ご飯・パン・卵など10件程度）
- **verify**: `assembleDebug` 成功（コンパイルエラーなし）

### [ ] T2. 設定画面（UserProfile + TDEE計算）
- **deps**: T1
- **acceptance**:
  - 身長・体重・年齢・性別・活動量を入力して保存できる
  - TDEE が Harris-Benedict 式で自動計算されて表示される
  - 目標体重・達成期日を入力 → 1日の摂取カロリー目標が自動算出される
  - 目標PFC（g）を手動入力できる
  - 再起動後も保存されている
- **verify**: 各値を入力→保存→アプリ再起動→値が残っている

### [ ] T3. 体重記録画面
- **deps**: T1
- **acceptance**:
  - 今日の体重を入力・保存できる
  - 折れ線グラフで過去の推移が表示される（週・月切替）
  - 目標体重と現在体重の差が表示される
- **verify**: 数日分の体重を入力 → グラフに折れ線が描画される

### [ ] T4. 食品マスタ管理
- **deps**: T1
- **acceptance**:
  - 食品の追加（名前・カロリー/100g・PFC・デフォルトグラム）
  - 一覧表示（お気に入り優先）
  - 編集・削除
  - プリセット食品が最初から入っている
- **verify**: 食品追加 → 一覧に表示 → 削除 → 消える

### [ ] T5. 食事プラン・記録画面
- **deps**: T1, T4
- **acceptance**:
  - 朝・昼・夜・間食の枠に食品を追加できる（マイ食品から選択）
  - グラム数を変更できる
  - 「食べた」チェックを押すと実績に変わる
  - カロリー残量（目標 − 摂取）がリアルタイム表示される
  - PFC の摂取量も表示される
- **verify**: 朝食に食品追加→チェック→カロリーが減る→昼食追加→合算される

### [ ] T6. 筋トレ記録画面
- **deps**: T1
- **acceptance**:
  - 種目を選択して重量・レップを入力 → セット追加
  - 当日のセット一覧が種目別に表示される
  - 種目選択時に前回値が初期表示される
  - 種目の追加・編集・削除ができる
- **verify**: 種目選択→セット追加→一覧反映→再起動後も残る

- ✅ **CP-B: T2〜T6 が実機で全部動いたら停止・フィードバックを取る**

---

## P2 — ダッシュボード統合（CP-C ゲート）

### [ ] T7. ホームダッシュボード
- **deps**: T2, T3, T5, T6
- **acceptance**:
  - 今日のカロリー摂取状況（残量バー・数値）
  - 体重の直近トレンド（直近7日の折れ線 or 前日比）
  - 今日の食事プランサマリ（朝昼夜間食のチェック状況）
  - 今日の筋トレ実施有無
- **verify**: 各画面で記録 → ホームに戻ると全て反映されている

- ✅ **CP-C: ダッシュボードが正しく統合されていることを確認してから P3 へ**

---

## P3 — Claude API 連携（CP-D ゲート）

### [ ] T8. AiRepository + リカバリ提案画面
- **deps**: T7
- **acceptance**:
  - `local.properties` の `claude.api.key` を読み込む
  - ホーム画面の「リカバリ提案を出す」ボタン押下でAPI呼び出し
  - 今日の目標カロリー・実績・残り枠・PFCをプロンプトに含める
  - 当日の残り食事案カード・翌日プラン修正カードが表示される
  - API エラー時はエラーカードを表示（クラッシュしない）
- **verify**: プランを崩した状態でボタン押下 → 提案カードが表示される

- ✅ **CP-D: リカバリ提案が実機で動作することを確認してから P4 へ**

---

## P4 — テストと品質（CP-E ゲート）

### [ ] T9. JVM ユニットテスト
- **deps**: T1, T2
- **acceptance**: `./gradlew test` がパス
  - TDEE 計算（Harris-Benedict 式の各パターン）
  - カロリー集計ロジック
  - リカバリ提案のリクエスト生成ロジック

### [ ] T10. instrumented DAO テスト
- **deps**: T1（実機 or エミュ必要）
- **acceptance**: `./gradlew connectedAndroidTest` がパス
  - MealEntry の日別集計
  - BodyWeight の期間絞り込み
  - Exercise CASCADE 削除

### [ ] T11. コードレビュー対応
- **deps**: CP-D 到達後
- **acceptance**: `/code-review` の妥当な指摘を反映

- ✅ **CP-E: test 緑 ＆ レビュー対応済み → MVP 完成**
