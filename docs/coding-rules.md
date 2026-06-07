# Sprout コーディングルール

> ⚠️ コードリファイン（Issue #7）時に再整理予定。現状ルールを抽出・確認したもの。

---

## バックエンド（Java / Spring Boot）

### 全層共通

| ルール | 詳細 |
|---|---|
| クラス名プレフィックス | 全クラスに `Sprout` を付ける（例: `SproutItemListController`） |
| DI | `@Autowired` フィールドインジェクションを使用（コンストラクタインジェクションは未使用） |
| Lombok | 未使用。getter/setter は手書き |
| Javadoc | クラス・メソッド・フィールドに `/** 説明 **/` 形式で記載 |
| パッケージ | `com.example.sprout.層名`（controller / service / dao / model / form / enums / security / validation） |

### Controller 層

| ルール | 詳細 |
|---|---|
| アノテーション | `@Controller` |
| 画面返却 | `String` でビュー名を return |
| JSON 返却 | `@ResponseBody` + Form / Model を return |
| ログインユーザー取得 | `@AuthenticationPrincipal SproutUserDetails` を引数に宣言 |
| パス変数 | `@PathVariable` |
| リクエストパラメータ | `@RequestParam` |
| JSONボディ | `@RequestBody` + DTO/Form |
| エンドポイント命名 | リソース名複数形 + 動詞（例: `/task/new`, `/task/update`, `/tags/all`） |
| ビジネスロジック | **Controller に書かない**。必ず Service 層に委譲 |

### Service 層

| ルール | 詳細 |
|---|---|
| 構成 | Interface + Impl 分離。`@Service` は Impl 側に付与 |
| 認証情報 | `accessControlService.getLoginUserId()` / `getLoginId()` で取得 |
| 共通フィールドセット | `userId`, `updateUser`, `updateAt` は Service 層でセット（Controller/DAO では行わない） |
| 権限チェック | `accessControlService.checkItemOwner(id)` 等で所有権を検証 |

### DAO 層（MyBatis）

| ルール | 詳細 |
|---|---|
| アノテーション | `@Mapper`（interface のみ定義，実装は XML mapper） |
| 複数パラメータ | `@Param("名前")` を必ず付ける（例: `@Param("userId") Long userId`） |
| メソッド命名 | `selectAll` / `selectByXxx` / `insert` / `update` / `delete` のパターン |
| XML 配置場所 | `src/main/resources/mappers/com/example/sprout/XXXDao.xml` |

### Model クラス

| ルール | 詳細 |
|---|---|
| 用途 | DBマッピング・層間データ渡し |
| フィールド | `/** 説明 **/` コメント付きで宣言 |
| getter/setter | 全フィールドに実装（Lombok 未使用） |
| 日付型 | `LocalDate`（日付）/ `LocalDateTime`（日時刻） |

### Form クラス

| ルール | 詳細 |
|---|---|
| 継承 | **`SproutAbstractForm<M>` を必ず継承**する |
| `newModel()` | 対応する Model のインスタンスを返す（抽象メソッドの実装） |
| `setDetailListFrom(src)` | Model → Form に `BeanUtils.copyProperties` でコピー |
| `createModel()` | Form → Model 変換。Controller から呼び出す |
| 用途 | Controller ↔ View 間のデータバインド。Service へは Model に変換して渡す |

### 例外ハンドリング

| 例外 | 処理 |
|---|---|
| `AccessDeniedException` | `GlobalExceptionHandler` で捕捉、`error/403` ビューへ |
| `IllegalStateException` | ログイン時の認証情報取得失敗。`/login` へリダイレクト |
| `Exception`（汎用） | `GlobalExceptionHandler` で捕捉、`error/500` ビューへ |
| 層内スロー | 権限エラーは `AccessDeniedException`、認証失敗は `IllegalStateException` をスロー |
| エラービュー | `src/main/resources/templates/error/403.html` / `500.html` |

### アクセス制御

| ルール | 詳細 |
|---|---|
| 認可集約 | `AccessControlService` に権限チェックロジックを集約 |
| 所有権検証 | `checkItemOwner(id)` / `checkTagOwner(id)` で他ユーザーデータへのアクセスを防止 |
| ユーザー情報取得 | `SecurityContextHolder` から `SproutUserDetails` を経由して取得 |

### Enum

| ルール | 詳細 |
|---|---|
| パッケージ | `com.example.sprout.enums` |
| ユーティリティメソッド | enum 内に定義可（例: `SproutTagColor.random()`） |
| Tailwind連携 | カラー系 enum は `getTailwindClass()` でクラス文字列を返す |

---

## フロントエンド（TailwindCSS / jQuery / Thymeleaf）

### CSS / TailwindCSS

| ルール | 詳細 |
|---|---|
| TailwindCSS v4 使用 | `input.css` → `output.css` ビルド（`npm run watch:css`） |
| クラスの記述場所 | **HTMLテンプレート（`.html`）に直接記述**。CSS ファイルには書かない |
| カスタムスタイル | Tailwind で表現できない場合のみ `input.css` に記述 |
| JS動的クラス | JS 文字内で使う Tailwind クラスは `tailwind.config.js` の `safelist` に追加必須 |
| ダークモード | `dark:` プレフィックス。`darkMode: 'class'` 設定済み |
| カスタムフォント | `font-title`（Merienda）、`font-body`（system-ui）→ `tailwind.config.js` の `theme.extend` で定義済み |

### JavaScript

| ルール | 詳細 |
|---|---|
| ライブラリ | **jQuery v3.7.1**。アロー鈦数より `function` を優先（既存コードに合わせる） |
| 名前空間 | `var sprout = sprout \|\| {};` パターン。各モジュールは `sprout.xxx = (function(){})()` |
| DOM ready | `$(function() { ... })` |
| 要素ID | `sprout.util.applyScreenIdPrefix($container, SCREEN_ID)` で prefix 付与。取得は `sprout.util.getId(SCREEN_ID, 'elementId')` |
| イベント | 名前空間付き: `.off('click.xxx').on('click.xxx', ...)` |
| AJAX | `$.getJSON()` / `$.ajax()` / `$.post()` / `sprout.util.sendForm()` |
| モーダル | `sprout.util.openModal({ modalId, url, data, callBack })` でAJAX取得・動的生成 |
| 外部ライブラリ | flatpickr（日付入力）、DataTables（テーブル）、Day.js（日付操作） |

### HTML / Thymeleaf

| ルール | 詳細 |
|---|---|
| フォームバインド | `th:object="${form}"` + `th:field="*{field}"` |
| 画面ルート要素 | `id="画面名Root"` を付与（prefix 付与のコンテナとして使用） |
| モーダルHTML | Controller からフラグメントとして返却し、`sprout.util.openModal()` で描画 |
| 閉じるボタン | `class="js-modal-close"` を付与（`sprout.util` が自動でイベント登録） |

### ファイル構成規則

```
static/
  css/
    input.css    ← @import "tailwindcss" + カスタムCSS
    output.css   ← ビルド成果物（コミット対象）
  js/
    sprout-util.js      ← 共通ユーティリティ（sprout.util）
    sprout-tags.js      ← タグコンポーネント（sprout.tags）
    sprout-message.js   ← メッセージ表示
    sproutTop.js        ← ホーム画面
    itemUpdateModal.js  ← タスク登録・更新モーダル
templates/
  sproutTop.html        ← ホーム画面テンプレート
  itemUpdateModal.html  ← モーダルフラグメント
```

---

_最終更新: 2026-05-31_
