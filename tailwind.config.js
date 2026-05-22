/** @type {import('tailwindcss').Config} */
const colors = require('tailwindcss/colors');

module.exports = {
  darkMode: 'class', // class ベースのダークモード
  content: [
    './src/main/resources/templates/**/*.html', // Thymeleaf
    './src/main/resources/static/js/**/*.js',   // JS（動的HTML含む）
  ],

  /**
   * JS文字列内で使うクラスを purge から守る
   */
  safelist: [
    // font
    'font-title',
    'font-body',

    // ===== タグ用ベースカラー（チップ表示） =====
    'bg-red-400',
    'bg-orange-400',
    'bg-amber-400',
    'bg-yellow-400',
    'bg-lime-400',
    'bg-green-400',
    'bg-emerald-400',
    'bg-teal-400',
    'bg-cyan-400',
    'bg-sky-400',
    'bg-blue-400',
    'bg-purple-400',

    // ===== 操作ポップアップ（色変更） =====
    'bg-yellow-50',
    'bg-yellow-100',
    'text-yellow-800',
    'dark:bg-yellow-900/30',
    'dark:text-yellow-300',
    'dark:hover:bg-yellow-900/50',

    // ===== 操作ポップアップ（削除） =====
    'bg-red-50',
    'bg-red-100',
    'text-red-700',
    'dark:bg-red-900/30',
    'dark:text-red-300',
    'dark:hover:bg-red-900/50',

    // ===== hover / text =====
    'hover:bg-gray-100',
    'dark:hover:bg-gray-700',
    'text-gray-400',
    'hover:text-black',

    // ===== layout utility =====
    'min-w-[160px]',
    'max-w-[60%]',
    'z-[9999]',
    'z-[10000]',

    // ===== ログインページ背景 =====
    'bg-gradient-to-br',
    'from-green-100',
    'via-emerald-200',
    'to-teal-200',

    // ===== Sproutグリーンパレット（safelist） =====
    'bg-sprout-leaf',
    'bg-sprout-moss',
    'bg-sprout-fern',
    'text-sprout-leaf',
    'text-sprout-mint',
    'border-sprout-fern',
    'hover:bg-sprout-moss',
    'dark:bg-dark-surface',
    'dark:bg-dark-raised',
  ],

  theme: {
    extend: {
      fontFamily: {
        title: ['Merienda', 'cursive'],
        // Noto Sans JP を追加（body の第1候補に昇格）
        body: ['Noto Sans JP', 'ui-sans-serif', 'system-ui'],
      },

      colors: {
        // ===== Sproutブランドカラー（パターンC準拠） =====
        sprout: {
          forest: '#0D2818',   // 最深グリーン
          grove:  '#1A4A30',   // 深グリーン
          fern:   '#2D6A4F',   // 中グリーン
          moss:   '#40916C',   // メインアクセント（ボタンなど）
          leaf:   '#52B788',   // プライマリカラー
          mint:   '#74C69D',   // ライトアクセント
          amber:  '#C89A3A',   // 警告・強調
          rose:   '#C85A5A',   // エラー・削除
        },

        // ===== ダークモード背景カラー =====
        dark: {
          base:    '#111820',  // 最深部
          surface: '#1A2430',  // カード面
          raised:  '#1F2C3A',  // 浮き上がり要素
          input:   '#162030',  // 入力フィールド
          border:  '#243440',  // ボーダー
        },

        // ===== Tailwind標準カラー（既存互換） =====
        gray:   colors.gray,
        yellow: colors.yellow,
        red:    colors.red,
        green:  colors.green,
        blue:   colors.blue,
        purple: colors.purple,
      },
    },
  },

  plugins: [],
};
