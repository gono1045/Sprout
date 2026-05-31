# Sprout — CLAUDE.md

このファイルはClaude Codeおよびclaude.aiのProjectsがプロジェクトのコンテキストを把握するための設定ファイルです。

---

## プロジェクト概要

| 項目         | 内容                                           |
| ------------ | ---------------------------------------------- |
| アプリ名     | Sprout                                         |
| 概要         | タスク管理 Web アプリケーション                |
| 開発目的     | 個人学習としての開発、ポートフォリオとして公開 |
| 開発フェーズ | MVP 開発中                                     |

---

## 技術スタック

### バックエンド

- **Java 17**
- **Spring Boot 3.5.8**
  - spring-boot-starter-web（MVC）
  - spring-boot-starter-thymeleaf（テンプレートエンジン）
  - spring-boot-starter-data-jpa（ORM）
  - spring-boot-starter-security（認証・認可）
  - spring-boot-starter-validation（バリデーション）
- **MyBatis 3.0.3**（JPA と併用、SQL マッパー）
- **PostgreSQL**（DB: `sprout` / port: `5432`）
- **Maven**（ビルドツール）

### フロントエンド

- **Thymeleaf**（サーバーサイドレンダリング）
- **Tailwind CSS 4.x**（npm でビルド）

---

## ディレクトリ構成

```
sprout/
├── src/
│   └── main/
│       ├── java/com/example/sprout/
│       │   ├── SproutAppApplication.java       # エントリーポイント
│       │   ├── config/
│       │   │   └── SecurityConfig.java         # Spring Security 設定
│       │   ├── controller/                     # MVC Controller 層
│       │   │   ├── LoginController.java
│       │   │   ├── SproutItemListController.java
│       │   │   ├── SproutTagListController.java
│       │   │   ├── SproutUserController.java
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── service/                        # ビジネスロジック層（Interface + Impl）
│       │   ├── dao/                            # MyBatis DAO 層
│       │   ├── model/                          # JPA Entity / MyBatis 結枞クラス
│       │   ├── form/                           # フォームバインド用 DTO
│       │   ├── security/                       # 認証関連（UserDetails 等）
│       │   ├── validation/                     # カスタムバリデーション
│       │   └── enums/                          # Enum 定義（SproutTagColor 等）
│       └── resources/
│           ├── application-local.yml           # ローカル環境設定
│           ├── application-prod.yml            # 本番環境設定
│           ├── mappers/                        # MyBatis XML マッパー
│           └── templates/                      # Thymeleaf テンプレート
│               ├── login.html
│               ├── register.html
│               ├── sproutTop.html
│               └── itemUpdateModal.html
├── Dockerfile
├── pom.xml
├── package.json                                # Tailwind CSS ビルド用
└── tailwind.config.js
```

---

## ローカル開発環境

### 前提条件

- Java 17
- Maven
- Node.js / npm（Tailwind ビルド用）
- PostgreSQL（DB名: `sprout`, ユーザー: `yuigono`, port: `5432`）

### 起動手順

```bash
# 1. Tailwind CSS のビルド（変更監視）
npm run watch:css

# 2. Spring Boot 起動（local プロファイル）
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### DB マイグレーション

`spring.jpa.hibernate.ddl-auto: update` を使用（スキーマは JPA Entity から自動更新）。

# このプロジェクト固有のルール

- マイグレーションファイルは直接編集しない（必ずコマンドで生成）
- DBへの直接クエリは本番環境では実行しない

---

## フロントエンド実装ルール（2026-05-31 時点）

> ⚠️ コードリファイン（Issue #7）時に再整理予定。現状ルールを抽出したもの。

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

## アーキテクチャ設計方針

- **レイヤードアーキテクチャ**（Controller → Service → DAO）
- **Service 層は Interface + Impl 分離**（`SproutItemListService` / `SproutItemListServiceImpl`）
- **JPA と MyBatis の併用**
  - JPA: Entity 管理、基本 CRUD
  - MyBatis: 複雑な JOIN クエリや動的 SQL
- **認証**: Spring Security + `UserDetailsService` カスタム実装
- **アクセス制御**: `AccessControlService` で認可ロジックを集約

---

## 主要ドメインモデル

| クラス                 | 役割                             |
| ---------------------- | -------------------------------- |
| `SproutUser`           | ユーザー情報                     |
| `SproutItemListDetail` | タスク（アイテム）詳細           |
| `SproutTagList`        | タグ                             |
| `SproutItemTag`        | タスク ⇔ タグ の中間テーブル対応 |
| `SproutTagColor`       | タグカラー Enum                  |

---

## コーディング規約・注意事項

- パッケージ名: `com.example.sprout`
- クラス名のプレフィックスは `Sprout` に統一
- フォームクラスは `SproutAbstractForm` を継承する
- セキュリティ上の注意: `application-local.yml` に DB パスワードを含むため `.gitignore` で除外すること
- `node_modules/` は `.gitignore` で除外済み

---

## 今後の開発予定（TODO）

- [ ] ドキュメント整備（要件定義書、API 仕様書、DB 設計書）
- [ ] タスクへの工数を植物の経験値として成長させる機能を追加
- [ ] タスク完了後の気づきや学びをメモし、AIで要約して知識の木の実を得られる機能を追加
- [ ] テストコードの充実

---

_最終更新: 2026-05-31_
