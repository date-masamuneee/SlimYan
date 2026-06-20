# SPEC.md — Slim-yan（ダイエット総合サポートアプリ）

## 1. Objective（目的・対象ユーザー）

「痩せたい、でも記録がめんどくさい」を解決する Android ネイティブアプリ。

### コアバリュー
- **入力コストを最小化**：よく食べるものは登録しておいてタップ一発で記録
- **食事プランを事前に組む**：1日の食事を朝のうちに決めて、その通りに動く
- **崩れたら即リカバリ**：飲み会・食べ損ね・過食が起きた瞬間に当日〜翌日の修正プランを Claude API が提案

### 解決する課題
- カロリーが分からない → マイ食品登録で解決
- 予定が崩れたときどうすれば良いか分からない → AI が当日の残り食事・翌日プランを提案
- 進捗が体感でしか分からない → 体重・カロリーグラフで可視化

### 対象ユーザー
自分専用（個人利用）。ログイン・アカウント機能なし。

---

## 2. 機能スコープ（MVP）

### ① ユーザープロフィール・目標設定
- 身長・体重・年齢・性別・活動量を入力
- TDEE（総消費カロリー）を自動計算
- 目標体重・達成期日を設定 → 1日の摂取カロリー目標を自動算出
- 目標PFC（タンパク質・脂質・炭水化物）も設定可

### ② 体重記録
- 毎日の体重を記録
- グラフで推移表示（週・月単位）
- 目標体重への残りを表示

### ③ マイ食品登録
- 食品名・カロリー・PFCを登録
- よく食べるものをすぐ呼び出せる
- 食品を組み合わせた「セット」も登録可能（例：コンビニ定番の組み合わせ）

### ④ 食事プラン・記録
- 1日の食事を朝・昼・夜・間食の枠で事前プラン
- マイ食品から選んで枠に追加
- 食べたらチェック → カロリー・PFC を自動集計
- 1日のカロリー残量をリアルタイム表示

### ⑤ リカバリ提案（Claude API）
- プランから外れたとき（過食・食べ損ね・急な外食）にトリガー
- 当日の残り食事でのリカバリ案を提案
- 翌日以降のプラン修正案も提案
- 提案はチャットUIではなくカード形式で表示（シンプルに）

### ⑥ 筋トレ記録
- 種目 × 重量 × レップ × セットを記録
- 前回値を初期表示
- 部位別・種目別の履歴

### ⑦ ホームダッシュボード
- 今日のカロリー摂取状況（残量バー）
- 体重の直近トレンド
- 今日の食事プランのサマリ
- 筋トレ実施有無

### スコープ外（MVP では作らない）
- バーコードスキャンでカロリー取得
- 外部DB（カロリーSlism等）との連携
- 有酸素運動の消費カロリー計算
- 週次レビュー（Claude API活用）← P2 以降
- ソーシャル機能・クラウド同期

---

## 3. Commands

```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew installDebug
./gradlew test
./gradlew connectedAndroidTest
./gradlew lint
```

---

## 4. Tech Stack

| 項目 | 採用 |
|------|------|
| 言語 | Kotlin 1.9.25 |
| UI | Jetpack Compose + Material3, edge-to-edge |
| アーキテクチャ | MVVM（ViewModel + StateFlow） |
| DI | Hilt 2.51.1 |
| DB | Room 2.6.1（端末内のみ） |
| Navigation | Navigation Compose 2.7.7（ボトムナビ5タブ） |
| HTTP | Ktor Client（Claude API呼び出し） |
| AI | Claude API（claude-haiku-4-5、軽量・低コスト） |
| KSP | 1.9.25-1.0.20（kapt 非推奨のため） |
| AGP / Compose BOM | 8.4.2 / 2024.06.00 |
| minSdk / targetSdk | 26 / 34 |

**Namespace**: `com.example.slimyan`

---

## 5. データモデル

```kotlin
// ユーザープロフィール（1件のみ）
UserProfile(
    id: Int,
    heightCm: Float,
    weightKg: Float,        // 初期体重
    age: Int,
    sex: String,            // "male" | "female"
    activityLevel: String,  // "sedentary" | "light" | "moderate" | "active" | "very_active"
    goalWeightKg: Float,
    goalDate: Long,         // epochDay
    dailyCalorieTarget: Int,  // 自動算出 or 手動上書き
    proteinTargetG: Float,
    fatTargetG: Float,
    carbTargetG: Float,
)

// 体重記録
BodyWeight(
    id: Long,
    dateEpochDay: Long,
    weightKg: Float,
    recordedAt: Long,
)

// 食品マスタ
Food(
    id: Long,
    name: String,
    caloriesPer100g: Float,
    proteinPer100g: Float,
    fatPer100g: Float,
    carbPer100g: Float,
    defaultGrams: Float,    // デフォルト量（例：ご飯なら150g）
    isFavorite: Boolean,
)

// 食事記録（1食の1品）
MealEntry(
    id: Long,
    dateEpochDay: Long,
    mealSlot: String,       // "breakfast" | "lunch" | "dinner" | "snack"
    foodId: Long,
    grams: Float,
    calories: Float,        // 記録時点で計算してキャッシュ
    isPlanned: Boolean,     // プランか実績か
    isChecked: Boolean,     // 食べたかどうか
    createdAt: Long,
)

// 筋トレ種目マスタ
Exercise(
    id: Long,
    name: String,
    muscleGroup: String,
)

// 筋トレセット記録
SetEntry(
    id: Long,
    exerciseId: Long,
    dateEpochDay: Long,
    weight: Float,
    reps: Int,
    setOrder: Int,
    createdAt: Long,
)
```

---

## 6. 画面構成（ボトムナビ5タブ）

| タブ | 画面 | 主要操作 |
|------|------|---------|
| ホーム | ダッシュボード | 今日のサマリ・リカバリ提案トリガー |
| 食事 | プラン＆記録 | 食品追加・チェック・カロリー確認 |
| 筋トレ | 記録 | セット追加・種目管理 |
| 体重 | 記録＆グラフ | 体重入力・推移確認 |
| 設定 | プロフィール・目標 | TDEE設定・目標変更 |

---

## 7. Claude API 連携仕様

- **呼び出しタイミング**：ユーザーが「リカバリ提案を出す」ボタンを押したとき（自動実行しない）
- **送るコンテキスト**：今日の目標カロリー・現在の摂取実績・残り食事枠・目標PFC・ユーザーの傾向
- **受け取るもの**：当日の残り食事案 + 翌日プランの修正提案（JSON形式で受け取りカード表示）
- **APIキー管理**：`local.properties` に保存、`.gitignore` で除外
- **Repository層に閉じ込める**：`AiRepository` で完結させUI層はAI実装を知らない

---

## 8. Code Style

- ユーザーレベル CLAUDE.md に従う
- 必要以上の機能追加・抽象化はしない
- DB アクセスは Repository 経由のみ
- Composable から DAO を直接触らない
- 単位は kg / kcal 固定（MVP）

---

## 9. Testing Strategy

- **Unit**: TDEE計算・カロリー集計・リカバリ提案のリクエスト生成ロジック
- **DAO test**: Room in-memory で CRUD・集計クエリを検証
- UI テストは MVP では最小限

---

## 10. Boundaries

### Always do
- 端末内 Room に保存、オフラインで基本動作
- APIキーを git に含めない

### Ask first
- 新しい外部ライブラリの追加
- データモデルの破壊的変更
- MVP スコープ外機能

### Never do
- アカウント・クラウド同期
- ネットワーク通信（Claude API以外）
- 個人データの外部送信（Claude APIへのリクエストは除く）
