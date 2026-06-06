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
    _this.tagFilterChipsId = sprout.util.getId(SCREEN_ID, 'tagFilterChips');
    _this.taskSearchInputId = sprout.util.getId(SCREEN_ID, 'taskSearchInput');
    _this.sproutListId = sprout.util.getId(SCREEN_ID, 'sproutList');
    _this.sproutPrevId = sprout.util.getId(SCREEN_ID, 'sproutPrev');
    _this.sproutNextId = sprout.util.getId(SCREEN_ID, 'sproutNext');
    _this.sproutDetailId = sprout.util.getId(SCREEN_ID, 'sproutDetail');
    _this.sproutDetailRightId = sprout.util.getId(SCREEN_ID, 'sproutDetailRight');
    _this.sproutDetailLeftId = sprout.util.getId(SCREEN_ID, 'sproutDetailLeft');
    _this.userMenuButtonId = sprout.util.getId(SCREEN_ID, 'userMenuButton');
    _this.userMenuDropdownId = sprout.util.getId(SCREEN_ID, 'userMenuDropdown');
    _this.themeToggleId = sprout.util.getId(SCREEN_ID, 'themeToggle');
    _this.groveListId = sprout.util.getId(SCREEN_ID, 'sproutTopGroveList');
    _this.featImgId = sprout.util.getId(SCREEN_ID, 'sproutTopFeatImg');
    _this.featStageId = sprout.util.getId(SCREEN_ID, 'sproutTopFeatStage');
    _this.featExpId = sprout.util.getId(SCREEN_ID, 'sproutTopFeatExp');
    _this.featBarId = sprout.util.getId(SCREEN_ID, 'sproutTopFeatBar');
    _this.featClearId = sprout.util.getId(SCREEN_ID, 'sproutTopFeatClear');
    _this.arcRingId = sprout.util.getId(SCREEN_ID, 'sproutTopArcRing');
    _this.featNameId = sprout.util.getId(SCREEN_ID, 'sproutTopFeatName');

    // カスタムテーブル用変数
    var _allItems    = [];
    var _activeTagId = null;

    // データロード（タグをアイテムごとにプリフェッチして item._tagIds にセット）
    function loadItems() {
      $.getJSON('/task/list', function (data) {
        _allItems = data || [];
        var requests = _allItems.map(function(item) {
          return $.getJSON('/items/' + item.id + '/tags').then(function(tags) {
            item._tagIds = (tags || []).map(function(t) { return String(t.tagId); });
          });
        });
        $.when.apply($, requests).always(function() {
          renderTable(getFilteredItems());
        });
      });
    }

    // 現在のフィルター状態（完了済み + タグ）を適用したアイテム一覧を返す
    function getFilteredItems() {
      var items = _allItems;
      if (!showCompleted) {
        items = items.filter(function(item) { return item.statusCd !== 3; });
      }
      if (_selectedTagIds.size > 0) {
        items = items.filter(function(item) {
          return item._tagIds && item._tagIds.some(function(id) {
            return _selectedTagIds.has(id);
          });
        });
      }
      return items;
    }

    // テーブルレンダリング
    function renderTable(items) {
      var $body = $(sprout.util.getId(SCREEN_ID, 'sproutTableBody'));
      $body.empty();

      if (!items || items.length === 0) {
        $body.append(
          '<div class="text-center py-8 text-gray-400 text-sm">タスクがありません</div>'
        );
        return;
      }

      $.each(items, function (i, item) {
        var deadline = item.deadline ? dayjs(item.deadline).format('MM/DD') : '-';

        var $row = $('<div></div>')
          .addClass('tbl-row tbl-data-row border-b border-gray-100 dark:border-gray-700')
          .attr('data-item-id', item.id);

        $row.html(
          '<div class="tbl-cell"></div>' +
          '<div class="tbl-cell">' +
            '<a href="javascript:void(0)" class="sprout-link hover:text-green-700" data-row-id="' + item.id + '">' +
              escapeHtml(item.title) +
            '</a>' +
          '</div>' +
          '<div class="tbl-cell">' +
            '<div class="sprout-tag-mount" data-item-id="' + item.id + '"></div>' +
          '</div>' +
          '<div class="tbl-cell">' + renderStatusPill(item.statusCd, item.statusName) + '</div>' +
          '<div class="tbl-cell text-xs text-gray-500">' + deadline + '</div>' +
          '<div class="tbl-cell text-xs">' + renderPriorityDot(item.priorityCd) + (item.priorityName || '-') + '</div>' +
          '<div class="tbl-cell text-xs text-gray-500">-</div>' +
          '<div class="tbl-cell">' +
            '<button class="btn-measure text-sm hover:text-green-600" data-item-id="' + item.id + '">⏱</button>' +
          '</div>'
        );

        $body.append($row);
      });

      $body.find('.sprout-tag-mount').each(function() {
        var $el = $(this);
        if ($el.data('mounted')) return;
        sprout.tags.mount({ el: this, itemId: $el.data('item-id') });
        $el.data('mounted', true);
      });
    }

    function renderStatusPill(statusCd, statusName) {
      var cls = 'status-pill';
      if      (statusCd === 1) cls += ' status-pill--todo';
      else if (statusCd === 2) cls += ' status-pill--doing';
      else if (statusCd === 3) cls += ' status-pill--done';
      return '<span class="' + cls + '">' + escapeHtml(statusName || '-') + '</span>';
    }

    function renderPriorityDot(priorityCd) {
      var cls = 'priority-dot';
      if      (priorityCd === 1) cls += ' priority-dot--high';
      else if (priorityCd === 2) cls += ' priority-dot--mid';
      else                       cls += ' priority-dot--low';
      return '<span class="' + cls + '"></span>';
    }

    function escapeHtml(str) {
      if (!str) return '';
      return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
    }

  // ダークモード切替ロジック
  var $html = $('html');

  function _applyTheme(theme) {
      $html.toggleClass('dark', theme === 'dark');
      $html.attr('data-theme', theme);
      var iconName = theme === 'dark' ? 'sun' : 'moon';
      $(_this.themeToggleId).html('<i data-lucide="' + iconName + '"></i>');
      if (typeof lucide !== 'undefined') {
          lucide.createIcons();
      }
  }

  // 保存済みテーマの適用
  var savedTheme = localStorage.getItem('sprout-theme') || 'light';
  _applyTheme(savedTheme);

  $(_this.themeToggleId).off('click.toggleTheme').on('click.toggleTheme', function() {
    var newTheme = $html.hasClass('dark') ? 'light' : 'dark';
    _applyTheme(newTheme);
    localStorage.setItem('sprout-theme', newTheme);
  });

  // Groveエリア初期化
  function initGrove(tagList) {
    var $list = $(_this.groveListId);
    $list.empty();
    
    $.each(tagList, function(i, tag) {
      // Lv.内のExp進捗(%) ExpやLvが0のときは0%表示
      var expPct = (tag.exp && tag.lv) ? Math.min(100, ((tag.exp % 100) / 100) * 100) : 0;
      var $card = $('<li></li>').addClass('grove-card flex-none bg-white/80 dark:bg-gray-800/80 ' +
        'rounded-xl p-2.5 shadow cursor-pointer hover:shadow-md transition border border-transparent ' +
        'hover:border-gray-200 dark:hover:border-gray-600')
        .css({'width': '126px', 'scroll-snap-align': 'start'})
        .attr('data-tag-id', tag.tagId);

      $card.html(
        '<div class="flex items-end justify-center mb-2" style="height:74px;">' +
          '<img src="/img/plant_lv' + (tag.lv || 1) + '.png" ' +
               'onerror="this.src=\'/img/plant_default.png\'" ' +
               'style="width:68px;height:68px;" class="object-contain" />' +
        '</div>' +
        '<p class="text-center text-xs font-semibold truncate mb-0.5 text-sprout-content">' + escapeHtml(tag.tagName) + '</p>' +
        '<p class="text-center text-xs text-sprout-content opacity-70">Lv.' + (tag.lv || 1) + ' / ' + escapeHtml(tag.stage || '種') + '</p>' +
        '<div class="w-full h-0.5 bg-gray-200 dark:bg-gray-700 rounded-full mt-1.5 overflow-hidden">' +
          '<div class="h-full bg-green-400 rounded-full" style="width:' + expPct + '%"></div>' +
        '</div>'
      );
      
      $list.append($card);
    });

    // GroveカードクリックでFeature反映＋タグフィルター
    $list.off('click.grove').on('click.grove', '.grove-card', function() {
      var tagId = $(this).data('tag-id');
      var tag = tagList.find(function(t) { return t.tagId === tagId; });
      if (!tag) return;

      updateFeatured(tag);

      if (typeof filterByTag === 'function') {
        filterByTag(tagId);
      }
    });
  }

  // Featured更新
  function updateFeatured(tag) {
    var EXP_PER_LV = 100;
    var exp = tag.exp || 0;
    var lv = tag.lv || 1;
    var expInLv = exp % EXP_PER_LV;
    var expPct = (expInLv / EXP_PER_LV) * 100;
    var circumference = 427.3; // 2π × r(68)

    $(_this.featNameId).text(tag.tagName).css('color', '');
    $(_this.featStageId).text(tag.stage || '種');
    $(_this.featExpId).text(expInLv + ' / ' + EXP_PER_LV);
    $(_this.featBarId).css('width', expPct + '%');
    $(_this.featImgId).attr('src', '/img/plant_lv' + lv + '.png')
      .off('error.feat').on('error.feat', function() {
        $(this).attr('src', '/img/plant_default.png');
      });

    var offset = circumference - (expPct / 100) * circumference;
    $(_this.arcRingId).css('stroke-dashoffset', offset);
    $(_this.featClearId).removeClass('hidden');
  }

  // 選択解除
  $(_this.featClearId).off('click.clearFeatured').on('click.clearFeatured', function() {
    $(_this.featNameId).text('タグを選択してください').css('color', '');
    $(_this.featStageId).text('---');
    $(_this.featExpId).text('— / —');
    $(_this.featBarId).css('width', '0%');
    $(_this.arcRingId).css('stroke-dashoffset', 427.3);
    $(this).addClass('hidden');
    if (typeof clearTagFilter === 'function') {
        clearTagFilter();
    }
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

  // タグフィルター状態
  var _selectedTagIds = new Set();

  // タグでフィルタリング
  function filterByTag(tagId) {
    _selectedTagIds.clear();
    _selectedTagIds.add(String(tagId));
    renderTable(getFilteredItems());
  }

  // フィルタークリア
  function clearTagFilter() {
    _selectedTagIds.clear();
    renderTable(getFilteredItems());
  }

  // 完了済み表示切り替え
  $(_this.toggleCompletedBtnId)
    .off('click.toggleCompleted')
    .on('click.toggleCompleted', function() {
        showCompleted = !showCompleted;
        $(this).text(showCompleted ? '完了済みを非表示' : '完了済みを表示');
        renderTable(getFilteredItems());
    });

  // 検索
  $(_this.taskSearchInputId)
    .off('input.search')
    .on('input.search', function () {
      var keyword = $(this).val().toLowerCase().trim();
      var base = getFilteredItems();

      if (!keyword) {
        renderTable(base);
        return;
      }

      renderTable(base.filter(function (item) {
        return item.title && item.title.toLowerCase().indexOf(keyword) !== -1;
      }));
    });

  // ⌘K / Ctrl+K で検索フォーカス
  $(document).off('keydown.cmdK').on('keydown.cmdK', function (e) {
    if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
      e.preventDefault();
      $(_this.taskSearchInputId).focus().select();
    }
  });

  // Escape で検索クリア
  $(_this.taskSearchInputId)
    .off('keydown.esc')
    .on('keydown.esc', function (e) {
      if (e.key === 'Escape') {
        $(this).val('').trigger('input.search');
        $(this).blur();
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

  $(document).on('sprout:tag-deleted', function (e, data) {
    console.log('タグ削除イベント受信', data);
    loadItems();
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
      loadItems();
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
      loadItems();
    })
    .fail(function () {
      sprout.message.toast({
        message: '複製に失敗しました',
        type: 'error'
      });
    });
  }

  $.getJSON('/tags/all', function(data) {
    var tagList = data.tagList || [];
    initGrove(tagList);
  });
  loadItems();
});