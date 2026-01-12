/**
 * sproutTop.js
 * Top画面 タスク一覧テーブル制御
 */

var SCREEN_ID = 'sproutTop';
var _this = {};

$(function () {
  // 画面IDを付与
  sprout.util.applyScreenIdPrefix($('#sproutTopRoot'), SCREEN_ID);

    _this.formId = sprout.util.getId(SCREEN_ID, "form");
    _this.createTaskModalButtonId = sprout.util.getId(SCREEN_ID, "createTaskModalButton");
    _this.modalId = sprout.util.getId(SCREEN_ID, "modal");
    _this.tableId = sprout.util.getId(SCREEN_ID, "sproutTable");
    _this.itemId = sprout.util.getId(SCREEN_ID, 'id');

  // JSON定義読み込み
  $.getJSON('/json/sproutTop.json', function (json) {

    // columns 定義生成
    const columns = json.columns.map(col => {

      // tags 列だけは空文字にする
      let renderFunc;

      if (col.data === 'tags') {
        renderFunc = function(data, type, row) {
          return `
            <span
              class="sprout-tag-mount"
              data-item-id="${row.id}">
            </span>
          `;
        };
      } else {
        renderFunc = SproutDataTables.getRender(col.inputType);
      }

      return {
        data: col.data,
        title: col.label,
        className: col.class ? col.class + ' border border-blue-300' : 'border border-blue-300',
        render: renderFunc
      };
    });

    const columnDefs = [

      {
        targets: 0,
        width: '200px',
        orderable: true
      }, // タイトル

      {
        targets: 1,
        width: '150px'
      }, // タグ

      {
        targets: 2,
        width: '100px',
        orderable: true
      }, // ステータス

      {
        targets: 3,
        width: '100px',
        orderable: true
      }, // 優先度

      {
        targets: 4,
        width: '150px',
        orderable: true
      }, // 作成日

      {
        targets: 5,
        width: '150px',
        orderable: true
      }, // 締切

      {
        targets: 6,
        width: '250px',
        className: 'text-left'
      }, // 詳細

      {
        targets: 7,
        width: '50px',
        orderable: false
      }, // 計測

      {
        targets: 8,
        width: '50px',
        orderable: false
      }, // 操作
      {
        targets: 9,
        visible: false
      }, // ステータスコード

      {
        targets: 10,
        visible: false
      } // 優先度コード
    ];

    // DataTable 初期化
    window.sproutTopTable = $(_this.tableId).DataTable({
      ajax: {
        url: '/task/list',
        dataSrc: ''
      },
      columns: columns,
      columnDefs: columnDefs,
      paging: true,
      pageLength: 5,
      searching: false,
      info: false,
      autoWidth: false,
      lengthChange: false,
      ordering: false,
      drawCallback: function () {
        mountTagsInTable();
      }
    });

    // ヘッダ行のみ中央揃え
    $(_this.tableId + ' thead th').css('text-align', 'center');
  });

  // 新規登録モーダル
  $(_this.createTaskModalButtonId)
    .off('click.openCreateModal')
    .on('click.openCreateModal', function () {

      console.log('新規登録ボタンが押下されました');

      sprout.util.openModal({
        modalId: 'itemUpdateModal',
        url: '/modal/update',
        data: { modalFlg: 0 },
        callBack: function ($modalEl) {
          console.log('モーダル表示成功', $modalEl[0]);
          itemUpdateModal.init($modalEl);
        }
      });
    });

  // 更新モーダル
  $(_this.tableId)
    .off('click.openUpdateModal')
    .on('click.openUpdateModal', '.sprout-link', function () {

      const itemId = $(this).data('row-id');

      sprout.util.openModal({
        modalId: 'itemUpdateModal',
        url: '/modal/update',
        data: {
          modalFlg: 1,
          id: itemId
        },
        callBack: function ($modalEl) {
          itemUpdateModal.init($modalEl);
        }
      });
    });

  function mountTagsInTable() {
    $(_this.tableId)
      .find('.sprout-tag-mount')
      .each(function () {

        const $el = $(this);

        // 再描画時の二重初期化防止
        if ($el.data('mounted')) return;

　       sprout.tags.mount({
          el: this,
          itemId: $el.data('item-id')
        });

        $el.data('mounted', true);
      });
  }

  $(document).on('sprout:tag-deleted', function (e, data) {
    console.log('タグ削除イベント受信', data);

    if (window.sproutTopTable) {
      window.sproutTopTable.ajax.reload(null, false);
    }
  });
});
