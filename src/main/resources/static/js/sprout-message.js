/**
 * sprout-message.js
 * 共通メッセージ／ダイアログ制御
 */
var sprout = sprout || {};

sprout.message = (function () {

  /**
   * メッセージ定義（共通）
   * ※ 呼び出し側で message を直接指定した場合はそちらを優先
   */
  const MESSAGE_MAP = {
    WARN001: '削除します。よろしいですか？',
    INFO001: '処理が完了しました'
  };

  return {

    /**
     * 確認ダイアログを表示し、結果を Promise で返却
     *
     * @param {object} params
     * @param {string} params.message - 表示メッセージ
     * @param {string} [params.okText='OK'] - OKボタン文言
     * @param {string} [params.cancelText='キャンセル'] - キャンセル文言
     * @param {boolean} [params.danger=true] - 危険操作フラグ（OKボタン赤）
     * @returns {Promise<boolean>} OK:true / Cancel:false
     */
    _confirm: function (params) {
      return new Promise(function (resolve) {

        if (!params || !params.message) {
          console.error('_confirm: message が指定されていません');
          resolve(false);
          return;
        }

        /* overlay */
        const overlay = document.createElement('div');
        overlay.style.cssText =
          'position:fixed;inset:0;z-index:99999;display:flex;align-items:center;justify-content:center;background:rgba(0,0,0,0.45);';

        /* dialog */
        const dialog = document.createElement('div');
        dialog.style.cssText =
          'background:var(--card,#fff);border:1px solid var(--line,#E6DFCD);border-radius:16px;' +
          'box-shadow:0 8px 40px rgba(27,67,50,0.18);min-width:300px;max-width:380px;overflow:hidden;';

        /* ===== header ===== */
        const header = document.createElement('div');
        header.style.cssText =
          'display:flex;justify-content:space-between;align-items:center;padding:18px 20px 12px;';

        const title = document.createElement('h3');
        title.style.cssText = 'font-size:15px;font-weight:700;color:var(--forest,#1B4332);margin:0;';
        title.textContent = params.title || params.messageId || '確認';

        const closeBtn = document.createElement('button');
        closeBtn.style.cssText =
          'background:none;border:none;cursor:pointer;color:var(--ink-3,#8E9A8E);font-size:18px;padding:0;line-height:1;';
        closeBtn.textContent = '×';

        header.append(title, closeBtn);

        /* ===== body ===== */
        const body = document.createElement('div');
        body.style.cssText =
          'padding:8px 20px 20px;font-size:14px;color:var(--ink-2,#4A5E4A);line-height:1.7;' +
          'border-top:1px solid var(--line,#E6DFCD);border-bottom:1px solid var(--line,#E6DFCD);';
        body.textContent = params.message;

        /* ===== footer ===== */
        const footer = document.createElement('div');
        footer.style.cssText = 'display:flex;justify-content:flex-end;gap:8px;padding:14px 20px;';

        const cancelBtn = document.createElement('button');
        cancelBtn.style.cssText =
          'padding:7px 18px;border-radius:8px;border:1.5px solid var(--line,#E6DFCD);' +
          'background:var(--card,#fff);color:var(--ink-2,#4A5E4A);font-size:13px;font-weight:500;cursor:pointer;';
        cancelBtn.textContent = params.cancelText || 'キャンセル';

        const okBtn = document.createElement('button');
        const _isDanger = params.danger !== false;
        okBtn.style.cssText = _isDanger
          ? 'padding:7px 18px;border-radius:8px;border:none;background:#fee2e2;color:#991b1b;font-size:13px;font-weight:600;cursor:pointer;'
          : 'padding:7px 18px;border-radius:8px;border:none;background:linear-gradient(135deg,var(--forest,#1B4332),var(--moss,#3E7A52));color:#fff;font-size:13px;font-weight:600;cursor:pointer;';
        okBtn.textContent = params.okText || 'OK';

        footer.append(cancelBtn, okBtn);

        /* assemble */
        dialog.append(header, body, footer);
        overlay.append(dialog);
        document.body.append(overlay);

        /* 背景クリックでキャンセル */
        overlay.addEventListener('click', function (e) {
          if (e.target === overlay) {
            close(false);
          }
        });

        /* Escでキャンセル */
        document.addEventListener('keydown', function escHandler(e) {
          if (e.key === 'Escape') {
            document.removeEventListener('keydown', escHandler);
            close(false);
          }
        });

        function close(result) {
          overlay.remove();
          resolve(result);
        }

        cancelBtn.onclick = function () { close(false); };
        closeBtn.onclick  = function () { close(false); };
        okBtn.onclick     = function () { close(true); };

      });
    },


    /**
     * 確認ダイアログを表示し、OK / Cancel をコールバックで処理
     *
     * @param {object} params
     * @param {string} [params.messageId] - メッセージID（WARN001など）
     * @param {string} [params.message] - 直接指定メッセージ
     * @param {string} [params.okText] - OKボタン文言
     * @param {string} [params.cancelText] - キャンセル文言
     * @param {boolean} [params.danger=true] - 危険操作フラグ
     * @param {function} [params.onOk] - OK押下時処理
     * @param {function} [params.onCancel] - Cancel押下時処理
     */
    confirmExec: function (params) {

      if (!params) {
        console.error('confirmExec: params が指定されていません');
        return;
      }

      const message =
        params.message ||
        MESSAGE_MAP[params.messageId];

      if (!message) {
        console.error('confirmExec: message が解決できません');
        return;
      }

      sprout.message._confirm({
        title: params.messageId,
        messageId: params.messageId,
        message: message,
        okText: params.okText,
        cancelText: params.cancelText,
        danger: params.danger
      }).then(function (result) {

        if (result) {
          if (typeof params.onOk === 'function') {
            params.onOk();
          }
        } else {
          if (typeof params.onCancel === 'function') {
            params.onCancel();
          }
        }

      });
    },

    /**
     * トーストメッセージ表示
     *
     * @param {object} params
     * @param {string} params.message - 表示メッセージ
     * @param {string} [params.type='info'] - info | success | error
     * @param {number} [params.duration=3000] - 表示時間(ms)
     */
    toast: function (params) {

      if (!params || !params.message) {
        console.error('toast: message が指定されていません');
        return;
      }

      const type = params.type || 'info';

      const toast = document.createElement('div');
      toast.className =
        'fixed top-5 right-5 z-50 px-4 py-3 rounded shadow text-white transition-opacity';

      if (type === 'success') {
        toast.classList.add('bg-green-600');
      } else if (type === 'error') {
        toast.classList.add('bg-red-600');
      } else {
        toast.classList.add('bg-blue-600');
      }

      toast.textContent = params.message;
      document.body.append(toast);

      setTimeout(() => {
        toast.classList.add('opacity-0');
        setTimeout(() => toast.remove(), 300);
      }, params.duration || 3000);
    },

    /**
     * 行操作ポップアップ（複製・削除）表示
     *
     * @param {object} params
     * @param {HTMLElement} params.anchorEl - ポップアップを表示するボタン要素
     * @param {function} [params.onDuplicate] - 複製ボタンクリック時コールバック
     * @param {function} [params.onDelete] - 削除ボタンクリック時コールバック
     */
    rowActionPopup: function(params) {

      if (!params || !params.anchorEl) {
        console.error('rowActionPopup: anchorEl が指定されていません');
        return;
      }

      // 既存ポップアップがあれば削除
      const existing = document.getElementById('sprout-row-action-popup');
      if (existing) existing.remove();

      /* ===== popup ===== */
      const popup = document.createElement('div');
      popup.id = 'sprout-row-action-popup';
      popup.className =
        'absolute z-50 flex flex-col bg-white dark:bg-gray-800 border rounded shadow-md';
      popup.style.minWidth = '120px';
      popup.style.padding = '0.25rem';
      popup.style.gap = '0.25rem';

      /* 複製ボタン */
      const duplicateBtn = document.createElement('button');
      duplicateBtn.className =
        'px-2 py-1 rounded bg-green-600 text-white hover:bg-green-700 text-center';
        duplicateBtn.style.margin = '0 20px';
      duplicateBtn.textContent = '複製';
      duplicateBtn.onclick = function() {
        popup.remove();
        if (typeof params.onDuplicate === 'function') params.onDuplicate();
      };

      /* 削除ボタン */
      const deleteBtn = document.createElement('button');
      deleteBtn.className =
        'px-2 py-1 rounded bg-red-600 text-white hover:bg-red-700 text-center';
        deleteBtn.style.margin = '0 20px';
      deleteBtn.textContent = '削除';
      deleteBtn.id = 'table-popup-delete';
      deleteBtn.onclick = function() {
        popup.remove();
        if (typeof params.onDelete === 'function') params.onDelete();
      };

      popup.append(duplicateBtn, deleteBtn);
      document.body.appendChild(popup);

      /* 表示位置をanchorElの下に配置 */
      const cellEl = params.anchorEl.closest('td') || params.anchorEl;
      const rect = cellEl.getBoundingClientRect();
      popup.style.top  = params.anchorEl.getBoundingClientRect().bottom + window.scrollY + 'px'; // ⋯ボタンの下
      popup.style.left = rect.left + window.scrollX + 'px';

      /* popup外クリックで閉じる */
      const clickHandler = function(e) {
        if (!popup.contains(e.target) && e.target !== params.anchorEl) {
          popup.remove();
          document.removeEventListener('click', clickHandler);
        }
      };
      document.addEventListener('click', clickHandler);
    },

    /**
     * インラインメッセージ表示（エリア内）
     *
     * @param {object} params
     * @param {HTMLElement|string} params.target - 表示先要素 or セレクタ
     * @param {string} params.message - 表示メッセージ
     */
    inline: function (params) {

      if (!params || !params.target || !params.message) {
        console.error('inline: target / message が指定されていません');
        return;
      }

      const targetEl =
        typeof params.target === 'string'
          ? document.querySelector(params.target)
          : params.target;

      if (!targetEl) return;

      targetEl.innerHTML = `
        <div class="w-full h-full flex items-center justify-center">
          <div class="w-auto text-gray-500 text-center leading-relaxed
            rounded-xl border-4 p-6
            bg-white/80 dark:bg-gray-800">
            ${params.message}
          </div>
        </div>
      `;
    },
  };
})();
