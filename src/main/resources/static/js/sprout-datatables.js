// sprout-datatables.js

const SproutDataTables = {

  renderers: {

    LINK: function (data, type, row) {
      if (type !== 'display') return data;

      return `
        <a href="javascript:void(0)"
           class="sprout-link"
           data-row-id="${row.id}"
           style="
             text-decoration: none;
             padding: 2px 4px;
             border-radius: 4px;
             transition: background-color 0.15s ease;
           "
           onmouseenter="
             this.style.backgroundColor = '#f3f4f6';
           "
           onmouseleave="
             this.style.backgroundColor = 'transparent';
           "
        >
          ${data}
        </a>
      `;
    },

    DATE: function (data) {
      if (!data) return '';
      return dayjs(data).format('YYYY/MM/DD');
    },

    LABEL: function (data) {
      return data ?? '';
    },

    BUTTON: function () {
      return `<button class="btn-measure">⏱</button>`;
    },

    ACTION: function (data, type, row) {
      // display のときだけボタンを生成
      if (type !== 'display') return '';

      // ボタンHTML
      const btnId = `action-btn-${row.id}`;
      const html = `<button id="${btnId}" class="action-menu cursor-pointer">⋯</button>`;

      // setTimeoutで描画後にイベントをバインド
      setTimeout(() => {
        const btn = document.getElementById(btnId);
        if (!btn) return;

        btn.onclick = function(e) {
          e.stopPropagation(); // 他の行選択等と干渉しないように

          // sprout.message の rowActionPopup を呼ぶ
          sprout.message.rowActionPopup({
            anchorEl: btn,
            onDuplicate: function() {
              console.log('複製', row);
              // ここでDBに送信して複製処理
              sprout.util.sendForm({
                url: '/task/duplicate',
                data: { id: row.id },
                callBack: function(newRow) {
                  // DataTables に新しい行を追加
                  if (window.sproutTopTable) {
                    window.sproutTopTable.row.add(newRow).draw(false);
                  }
                  sprout.message.toast({
                    message: '複製しました',
                    type: 'success'
                  });
                }
              });
            },
            onDelete: function() {
              console.log('削除', row);
              // 確認ダイアログ経由で削除
              sprout.message.confirmExec({
                messageId: 'WARN001',
                title: '削除確認',
                onOk: function() {
                  sprout.util.sendForm({
                    url: '/task/delete',
                    data: { id: row.id },
                    callBack: function() {
                      if (window.sproutTopTable) {
                        // DataTables から行を削除
                        const table = window.sproutTopTable;
                        const rowIndex = table.row(btn.closest('tr')).index();
                        table.row(rowIndex).remove().draw(false);
                      }
                      sprout.message.toast({
                        message: '削除しました',
                        type: 'success'
                      });
                    }
                  });
                }
              });
            }
          });
        };
      });

      return html;
    },

    TEXTAREA: function (data, type) {
      if (!data) return '';

      if (type === 'display') {
        return data.replace(/\n/g, '<br>');
      }

      return data;
    },

  },

  /**
   * inputType から render を取得
   */
  getRender: function (inputType) {
    return this.renderers[inputType] ?? null;
  }
};
