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

  /** z-index定義 */
  const Z_INDEX = {
    dropdown: 9999,
    popup: 10000,
    modal: 20000,
    toast: 20000
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
        overlay.className =
          'fixed inset-0 flex items-center justify-center bg-black/40';
        overlay.style.zIndex = Z_INDEX.modal;

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
        'fixed top-5 right-5 px-4 py-3 rounded shadow text-white transition-opacity';
      toast.style.zIndex = Z_INDEX.toast;

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
