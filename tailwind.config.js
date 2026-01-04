/** @type {import('tailwindcss').Config} */
const colors = require('tailwindcss/colors');

module.exports = {
  darkMode: 'class', // classベースのダークモード
  content: [
    './src/main/resources/templates/**/*.html',  // Thymeleaf HTML
    './src/main/resources/static/js/**/*.js',    // JS
  ],
  safelist: [
  'font-title',
  'font-body',
  'dark:text-green-300',
  'text-green-700',
  'dark:text-green-300',
  'bg-yellow-400',
  'bg-purple-400',
  'bg-green-400',
  'bg-red-400',
  'bg-blue-400'
  ],
  theme: {
    extend: {
      fontFamily: {
    title: ['Merienda', 'cursive'], // タイトル用
    body: ['ui-sans-serif', 'system-ui'], // 本文用は標準
      },
      colors: {
        green: colors.green,
      },
    },
  },
  plugins: [],
}
