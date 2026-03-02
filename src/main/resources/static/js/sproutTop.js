/**
 * sproutTop.js
 * Top画面 タスク一覧テーブル制御
 */

var SCREEN_ID = 'sproutTop';
var _this = {};

$(function () {
  var showCompleted = false;

  // 画面IDを付与
  sprout.util.applyScreenIdPrefix($('#sproutTopRoot'), SCREEN_ID);

    _this.formId = sprout.util.getId(SCREEN_ID, "form");
    _this.createTaskModalButtonId = sprout.util.getId(SCREEN_ID, "createTaskModalButton");
    _this.modalId = sprout.util.getId(SCREEN_ID, "modal");
    _this.tableId = sprout.util.getId(SCREEN_ID, "sproutTable");
    _this.itemId = sprout.util.getId(SCREEN_ID, 'id');
    _this.toggleCompletedBtnId = sprout.util.getId(SCREEN_ID, 'toggleCompletedBtn');
    _this.taskSearchBtnId = sprout.util.getId(SCREEN_ID, 'taskSearchBtn');
    _this.taskSearchInputId = sprout.util.getId(SCREEN_ID, 'taskSearchInput');
    _this.sproutListId = sprout.util.getId(SCREEN_ID, 'sproutList');
    _this.sproutPrevId = sprout.util.getId(SCREEN_ID, 'sproutPrev');
    _this.sproutNextId = sprout.util.getId(SCREEN_ID, 'sproutNext');
    _this.sproutDetailId = sprout.util.getId(SCREEN_ID, 'sproutDetail');
    _this.sproutDetailRightId = sprout.util.getId(SCREEN_ID, 'sproutDetailRight');
    _this.sproutDetailLeftId = sprout.util.getId(SCREEN_ID, 'sproutDetailLeft');
    _this.userMenuButtonId = sprout.util.getId(SCREEN_ID, 'userMenuButton');
    _this.userMenuDropdownId = sprout.util.getId(SCREEN_ID, 'userMenuDropdown');

  // JSON定義読み込み
  $.getJSON('/json/sproutTop.json', function (json) {

    // columns 定義生成
    const columns = json.columns.map(col => {

      // tags 列だけは空文字にする
      let renderFunc;

      if (col.data === 'tags') {
        renderFunc = function(data, type, row) {
          return `
            <div
              class="sprout-tag-mount absolute inset-0 w-full h-full"
              data-item-id="${row.id}">
            </div>
          `;
        };
      } else if (col.data === 'operation') {
        renderFunc = function(data, type, row) {
          return `
            <div class="flex flex-col justify-center gap-1">
              <button
                type="button"
                class="sprout-row-duplicate px-2 py-1 rounded bg-green-600 text-white text-ms hover:bg-green-700"
                data-item-id="${row.id}">
                複製
              </button>
              <button
                type="button"
                class="sprout-row-delete px-2 py-1 rounded bg-red-600 text-white text-ms hover:bg-red-700"
                data-item-id="${row.id}">
                削除
              </button>
            </div>
          `;
          };
      } else {
        renderFunc = SproutDataTables.getRender(col.inputType);
      }

      return {
        data: col.data,
        title: col.label,
        className: col.data === 'tags'
          ? 'relative min-h-[40px] border border-blue-300'
          : (col.class ? col.class + ' border border-blue-300' : 'border border-blue-300'),
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

    // 完了済みタスクフィルタ
    DataTable.ext.search.push(function(settings, data) {
        const statusCd = Number(data[9]);

        // 完了済みを非表示
        if (!showCompleted && statusCd == 3) {
            return false;
        }
        return true;
    });

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
      searching: true,
      dom: 'tp',
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

  // ユーザーメニュー開閉
  $(function () {
    $(_this.userMenuButtonId).on('click', function () {
      $(_this.userMenuDropdownId).toggleClass('hidden');
    });

    // 外クリックで閉じる
    $(document).on('click', function (e) {
      if (!$(e.target).closest(_this.userMenuButtonId, _this.userMenuDropdownId).length) {
        $(_this.userMenuDropdownId).addClass('hidden');
      }
    });
  });

  // 完了済み表示切り替え
  $(_this.toggleCompletedBtnId)
    .off('click.toggleCompleted')
    .on('click.toggleCompleted', function() {
        showCompleted = !showCompleted;

        $(this).text(showCompleted ? '完了済みを非表示' : '完了済みを表示');

        if(window.sproutTopTable) {
            window.sproutTopTable.draw();
        }
    });

  // 検索クリック
  $(_this.taskSearchBtnId).on('click', function() {
    const keyword = $(_this.taskSearchInputId).val();

    if (window.sproutTopTable) {
        window.sproutTopTable.search(keyword).draw();
    }
  });

  var isComposing = false;
  // 検索inputでEnter
  $(_this.taskSearchInputId)
    .on('compositionstart', function() {
      isComposing = true;
    })
    .on('compositionend', function() {
        isComposing = false;
    })
    .on('keydown', function(e) {
    if (e.key === 'Enter') {
        if (isComposing) return;
        e.preventDefault();
        $(_this.taskSearchBtnId).click();
    }
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

  // 複製・削除イベント
  $(_this.tableId)
    .off('click.sproutRowDuplicate')
    .on('click.sproutRowDuplicate', '.sprout-row-duplicate', function () {

      const itemId = $(this).data('item-id');

      duplicateItem(itemId);
    });

  $(_this.tableId)
    .off('click.sproutRowDelete')
    .on('click.sproutRowDelete', '.sprout-row-delete', function () {

      const itemId = $(this).data('item-id');

      sprout.message.confirmExec({
        messageId: 'WARN001',
        onOk: function () {
          deleteItem(itemId);
        }
      });
    });

  // タスク削除メソッド
  function deleteItem(itemId) {
    $.ajax({
      url: '/task/delete',
      type: 'POST',
      data: { id: itemId }
    })
    .done(function () {
      sprout.message.toast({
        message: '削除しました',
        type: 'success'
      });
      window.sproutTopTable.ajax.reload(null, false);
    })
    .fail(function () {
      sprout.message.toast({
        message: '削除に失敗しました',
        type: 'error'
      });
    });
  }

  // タスク複製メソッド
  function duplicateItem(itemId) {
    $.ajax({
      url: '/task/duplicate',
      type: 'POST',
      data: { id: itemId }
    })
    .done(function () {
      sprout.message.toast({
        message: '複製しました',
        type: 'success'
      });
      window.sproutTopTable.ajax.reload(null, false);
    })
    .fail(function () {
      sprout.message.toast({
        message: '複製に失敗しました',
        type: 'error'
      });
    });
  }

  // sproutArea描画
  function renderSproutList(sprouts) {
    const list = $(_this.sproutListId);
    list.empty();

    if (!sprouts || sprouts.length === 0) {
        sprout.message.inline({
            target: list[0],
            message: `
              タグが登録されていません<br>
              タスクにタグを設定すると植物の成長が始まります
            `
        });
        return;
    }

    sprouts.forEach(sp => {
      const expRate = Math.min(
        Math.floor((sp.exp / sp.nextExp) * 100), 100);
        const $li = $(`
          <li class="w-64 flex-shrink-0 mr-3 sprout-card" data-tag-id="${sp.tagId}">
            <div class="rounded-lg border-2 p-4 bg-white/80 dark:bg-gray-800"
                 style="border-color: ${sp.tagColor};">
              <img src="${sp.imageUrl}"
                   class="w-full h-52 object-contain rounded-md mb-2" />
              <div class="text-center space-y-1">
                <div class="font-semibold">
                  ${sp.tagName} Lv.${sp.level}
                </div>
                <div class="w-full h-2 bg-gray-200 rounded-full overflow-hidden">
                  <div class="h-full"
                       style=" width: ${expRate}%;
                               background-color: ${sp.tagColor};">
                  </div>
                </div>
              </div>
            </div>
          </li>
        `);
        list.append($li);
    });
  }

  // 植物エリア制御用定数
  const VISIBLE_COUNT = 5;
  let currentIndex = 0;
  let itemWidth = 0;
  let visible = 0;
  let total = 0;

  // 植物エリアのスライダーメソッド
  function initSproutSlider() {
    const $list = $(_this.sproutListId);
    const $items = $list.children('li');

    total = $items.length;
    if (total === 0) return;

    visible = Math.min(VISIBLE_COUNT, total);

    const prependClones = $items.slice(-visible).clone();
    const appendClones = $items.slice(0, visible).clone();

    $list.prepend(prependClones);
    $list.append(appendClones);

    itemWidth = $items.outerWidth(true);
    currentIndex = visible;

    updateSproutPosition(true);
  }

  // 画像の位置制御
  function updateSproutPosition(noAnimation) {
    const $list = $(_this.sproutListId);
    const translateX = -currentIndex * itemWidth;

    if (noAnimation) {
      $list.css('transition', 'none');
    } else {
      $list.css('transition', 'transform 0.3s ease');
    }

    $list.css('transform', `translateX(${translateX}px)`);
  }

  let isAdjusting = false;

  // 次へ
  $(_this.sproutNextId).on('click', function () {
    if (isAdjusting) return;

    currentIndex++;
    updateSproutPosition();

    if (currentIndex === total + visible) {
      isAdjusting = true;
      const $list = $(_this.sproutListId);

      $list.one('transitionend', function () {
        currentIndex = visible;
        updateSproutPosition(true);
        isAdjusting = false;
      });
    }
  });

  // 前へ
  $(_this.sproutPrevId).on('click', function () {
    if (isAdjusting) return;

    currentIndex--;
    updateSproutPosition();

    if (currentIndex === 0) {
      isAdjusting = true;
      const $list = $(_this.sproutListId);

      $list.one('transitionend', function () {
        currentIndex = total;
        updateSproutPosition(true);
        isAdjusting = false;
      });
    }
  });

  const sproutPlants = [
    {
      tagId: 1,
      tagName: 'Sprout',
      tagColor: '#4ade80',   // green-400
      level: 12,
      exp: 320,
      nextExp: 500,
      imageUrl: '/img/stage01.PNG'
    },
    {
      tagId: 2,
      tagName: '音楽',
      tagColor: '#60a5fa',   // blue-400
      level: 5,
      exp: 80,
      nextExp: 200,
      imageUrl: '/img/stage02.PNG'
    },
    {
      tagId: 3,
      tagName: '人間関係',
      tagColor: '#f87171',   // red-400
      level: 5,
      exp: 80,
      nextExp: 200,
      imageUrl: '/img/stage03.PNG'
    },
    {
      tagId: 2,
      tagName: '生活',
      tagColor: '#facc15',   // yellow-400
      level: 5,
      exp: 80,
      nextExp: 200,
      imageUrl: '/img/stage04.PNG'
    },
    {
      tagId: 2,
      tagName: '学習',
      tagColor: '#c084fc',   // purple-400
      level: 5,
      exp: 80,
      nextExp: 200,
      imageUrl: '/img/stage05.PNG'
    },
  ];

  renderSproutList(sproutPlants);
  initSproutSlider();
});
