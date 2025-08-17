/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: 'class', // classベースのダークモード
  content: [
    './src/main/resources/templates/**/*.html',  // Thymeleaf HTML
    './src/main/resources/static/js/**/*.js',    // JS
  ],
  safelist: [
	'font-title',
	'font-body',
  ],
  theme: {
    extend: {
      fontFamily: {
		title: ['Merienda', 'cursive'], // タイトル用
		body: ['ui-sans-serif', 'system-ui'], // 本文用は標準
      },
    },
  },
  plugins: [],
}
