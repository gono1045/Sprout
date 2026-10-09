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
│       │   ├── service/                        # ビジネスロジック層（Interface + Impl）
│       │   ├── dao/                            # MyBatis DAO 層
│       │   ├── model/                          # JPA Entity / MyBatis 結枞クラス
│       │   ├── form/                           # フォームバインド用 DTO
│       │   ├── security/                       # 認証関連
│       │   ├── validation/                     # カスタムバリデーション
│       │   └── enums/                          # Enum 定義
│       └── resources/
│           ├── application-local.yml
│           ├── application-prod.yml
│           ├── mappers/                        # MyBatis XML マッパー
│           └── templates/                      # Thymeleaf テンプレート
├── docs/
│   └── coding-rules.md                     # フロントエンドコーディングルール
├── Dockerfile
├── pom.xml
├── package.json
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

---

## コーディングルール

> ⚠️ **必ず参照すること**: フロントエンドの実装コードを書く際は必ず [`docs/coding-rules.md`](docs/coding-rules.md) を確認すること。
> TailwindCSS ・ JavaScript ・ Thymeleaf 各層の実装規約が記載されている。

### プロジェクト共通ルール

- マイグレーションファイルは直接編集しない（必ずコマンドで生成）
- DBへの直接クエリは本番環境では実行しない

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
- `application-local.yml` に DB パスワードを含むため `.gitignore` で除外すること
- `node_modules/` は `.gitignore` で除外済み

---

## 今後の開発予定（TODO）

- [ ] ドキュメント整備（要件定義書、API 仕様書、DB 設計書）
- [ ] タスクへの工数を植物の経験値として成長させる機能を追加
- [ ] タスク完了後の気づきや学びをメモし、AIで要約して知識の木の実を得られる機能を追加
- [ ] テストコードの充実

---

_最終更新: 2026-10-09_

---

## 下期方針（2026年度下期・提出期限: 2027年2月末）

### 実施内容（優先順）
1. **テスト拡充**（主軸）: Service層中心・カバレッジ目標70〜80%
2. **ナレッジ化機能追加**（主軸）: タイマー気づきメモ→AI要約→「知識の木の実」保存
3. 余裕があれば: React/TypeScript導入・レスポンシブ対応

### AI連携設計（ナレッジ化）
- `AiSummaryService` interface を作成し、Spring Profilesで切り替え
- ローカル: LM Studio（OpenAI互換API / `@Profile("local")`）
- 本番: Gemini API（`@Profile("prod")`）

### ドキュメント管理
- タスク完了のたびに `Sprout-doc/03_進捗・成果/下期成果サマリー.md` に追記する
- フォーマット: `### タスク名（完了日）` → 実施内容・変更ファイル
- Notionの進捗メモ更新と同時に行う

---

## Claude Context

作業を開始する前に、Notionの「Claude Context」ページ(https://app.notion.com/p/36371d05abfe81999527cf52682911b0)を参照してください。Gonoのプロフィール・現在進行中のプロジェクト一覧・行動指針・成長ログがまとまっています。
作業終了時、重要な決定・状態変化があればClaude Contextページのセッション引き継ぎログに追記すること。
