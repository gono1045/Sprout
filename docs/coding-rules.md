# Sprout コーディングルール

> ⚠️ コードリファイン（Issue #7）時に再整理予定。現状ルールを抽出・確認したもの。

---

## CSS / TailwindCSS

| ルール | 詳細 |
|---|---|
| TailwindCSS v4 使用 | `input.css` → `output.css` ビルド（`npm run watch:css`） |
| クラスの記述場所 | **HTMLテンプレート（`.html`）に直接記述**。CSS ファイルには書かない |
| カスタムスタイル | Tailwind で表現できない場合のみ `input.css` に記述 |
| JS動的クラス | JS 文字内で使う Tailwind クラスは `tailwind.config.js` の `safelist` に追加必須 |
| ダークモード | `dark:` プレフィックス。`darkMode: 'class'` 設定済み |
| カスタムフォント | `font-title`（Merienda）、`font-body`（system-ui）→ `tailwind.config.js` の `theme.extend` で定義済み |

---

## JavaScript

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

---

## HTML / Thymeleaf

| ルール | 詳細 |
|---|---|
| フォームバインド | `th:object="${form}"` + `th:field="*{field}"` |
| 画面ルート要素 | `id="画面名Root"` を付与（prefix 付与のコンテナとして使用） |
| モーダルHTML | Controller からフラグメントとして返却し、`sprout.util.openModal()` で描画 |
| 閉じるボタン | `class="js-modal-close"` を付与（`sprout.util` が自動でイベント登録） |

---

## ファイル構成規則

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
