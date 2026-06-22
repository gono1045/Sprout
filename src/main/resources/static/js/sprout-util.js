/**
 * sprout-util.js
 * 共通ユーティリティ関数集
 */
var sprout = sprout || {};
let modalSeq = 0;

sprout.util = (function() {

  return {

    /**
     * コンテナ内の id / for 属性に ScreenId を prefix として付与する
     * @param {jQuery} $container - 対象DOM（画面 or モーダル）
     * @param {string} screenId - 画面固有ID
     */
    applyScreenIdPrefix: function ($container, screenId) {
      if (!$container || !$container.length) {
        console.error('applyScreenIdPrefix: container が不正です');
        return;
      }
      if (!screenId) {
        console.error('applyScreenIdPrefix: screenId が指定されていません');
        return;
      }

      $container.removeData('prefixed');

      $container.find('[id]').not('[data-no-prefix]').each(function () {
        const $el = $(this);
        const oldId = $el.attr('id');

        // すでに何か prefix が付いていたらスキップ
        if (oldId.startsWith(screenId + '_')) return;

        const newId = screenId + '_' + oldId;
        $el.attr('id', newId);

        $container.find('label[for="' + oldId + '"]')
          .attr('for', newId);
      });
    },

    /**
         * 画面固有IDと要素IDを組み合わせてセレクタを生成
     * @param {string} screenId - 画面固有ID
     * @param {string} elementId - HTMLの要素ID
         * @returns {string} セレクタ文字列 (#screenId_elementId)
     */
    getId: function(screenId, elementId) {
      if (!screenId || !elementId) {
        console.error('getId: screenId または elementId が指定されていません');
      }
          return '#' + screenId + '_' + elementId;
    },

    /**
     * フォーム要素に値をセット
     * @param {string|jQuery} selector - CSSセレクタ文字列またはjQueryオブジェクト
     * @param {*} value - セットする値
     */
    setFormValue: function(selector, value) {
      if (!selector) {
        console.error('setFormValue: selector が指定されていません');
        return;
      }
      $(selector).val(value);
    },

    /**
     * フォーム要素の値を取得
     * @param {string|jQuery} selector - CSSセレクタ文字列またはjQueryオブジェクト
     * @returns {*} 要素の値
     */
    getFormValue: function(selector) {
      if (!selector) {
        console.error('getFormValue: selector が指定されていません');
        return null;
      }
      return $(selector).val();
    },

    /**
     * モーダル用HTMLを動的生成し、表示する
         *
         * @param {object} params
         * @param {string} params.modalId - モーダル識別子（例: 'itemUpdateModal'）
         * @param {string} params.url - Controller のエンドポイント
         * @param {object} [params.data] - Controller に送信するパラメータ
         * @param {string} [params.method='GET'] - HTTPメソッド
         * @param {function} [params.callBack] - HTML描画後コールバック
         */
        /**
         * モーダル用HTMLを動的生成し、表示する（安定版）
     */
    openModal: function (params) {
      if (!params || !params.modalId) { console.error('modalId 未指定'); return; }
      if (!params.url) { console.error('url 未指定'); return; }

      const modalId = params.modalId;
      const seq = ++modalSeq;

      // 開いたままのインライン編集（ドロップダウン・タグ編集）があれば
      // モーダルより手前に残ってしまうため、先に確定して閉じる
      if (window.sproutTopInline && typeof window.sproutTopInline.closeActive === 'function') {
        window.sproutTopInline.closeActive();
      }
      if (window.sprout && window.sprout.tags && typeof window.sprout.tags.closeActiveEdit === 'function') {
        window.sprout.tags.closeActiveEdit();
      }
      $('#sprout-inline-dropdown, .sprout-tag-dropdown-portal').remove();

      // 既存モーダルは必ず破棄
      $('#' + modalId).remove();
      $(document).off('keydown.modal');

      // 新規生成
      let $modalEl = $('<div>', {
        id: modalId,
        'data-no-prefix': true,
        class: 'fixed inset-0 z-50 hidden bg-black/80 flex items-center justify-center'
      }).appendTo('body');

      $modalEl.removeClass('hidden');

      $.ajax({
        url: params.url,
        type: params.method || 'GET',
        data: params.data || {},
        cache: false,
        success: function (html) {

          if (seq !== modalSeq) {
            return;
          }

          $modalEl.removeData('prefixed');
          $modalEl.html(html);

          // prefix付与
          sprout.util.applyScreenIdPrefix($modalEl, modalId);

          // フォーム存在チェック（skipFormCheck: true の場合はスキップ）
          if (!params.skipFormCheck) {
            const $form = $modalEl.find('#' + modalId + '_sproutItemListForm');
            if ($form.length === 0) {
              console.error('フォームが見つかりません');
              return;
            }
          }

          // コールバック実行
          if (typeof params.callBack === 'function') {
            params.callBack($modalEl);
          }

          // モーダル閉じる処理
          function closeModal() {
            $(document).off('keydown.modal');
            $modalEl.remove();
          }

          $modalEl.off('click.modal-close').on('click.modal-close', '.js-modal-close', closeModal);
          $modalEl.off('click.modal-bg').on('click.modal-bg', function(e){ if(e.target===this) closeModal(); });
          $(document).off('keydown.modal').on('keydown.modal', function(e){ if(e.key==='Escape') closeModal(); });
        },
        error: function () {
          if (seq !== modalSeq) {
            return;
          }

          console.error('モーダルHTML取得失敗');
          $modalEl.remove();
        }
      });
    },

    /**
     * モーダル表示・クローズ制御
     * @param {jQuery} $modalEl - モーダルのコンテナ要素
     */
    showModal: function ($modalEl) {

      $modalEl.removeClass('hidden');

      // 閉じるボタン（名前空間付き）
      $modalEl.off('click.modal')
        .on('click.modal', '.js-modal-close', function () {
          $modalEl.addClass('hidden');
        });

      // Escキー
      $(document).off('keydown.modal')
        .on('keydown.modal', function (e) {
          if (e.key === 'Escape') {
            $modalEl.addClass('hidden');
            $(document).off('keydown.modal');
          }
        });
    },

    /**
     * フォーム送信または個別パラメータ送信
     * @param {object} params
     * @param {string} params.url - Controllerのエンドポイント
     * @param {string|jQuery} [params.form] - 送信フォームID/セレクタ
     * @param {object} [params.data] - 個別パラメータ { id: 1, no: 2 }
     * @param {string} [params.method='POST'] - HTTPメソッド
     * @param {function} [params.callBack] - 送信後コールバック
     */
    sendForm: function(params) {
      if (!params.url) {
        console.error('sendForm: url が指定されていません');
        return;
      }

      var data = {};
      if (params.form) {
        data = $(params.form).serialize();
      } else if (params.data) {
        data = params.data;
      }

      var method = params.method || 'POST';

      $.ajax({
        url: params.url,
        type: method,
        data: data,
        success: function(res) {
          if (typeof params.callBack === 'function') params.callBack(res);
        },
        error: function(xhr, status, err) {
          console.error('sendForm Ajax error:', err);
        }
      });
    }

  };

})();
