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
    var _allItems        = [];
    var _activeTagId     = null;
    var _selectedFeatTag = null; // Featured パネルで選択中のタグ ID
    var _allTagData      = null; // /tags/all レスポンスキャッシュ
    var _selectedTagIds  = new Set(); // タグフィルター
    var _activeInlineClose = null; // 現在開いているインライン編集（テキスト/ドロップダウン/日付）を確定保存して閉じる関数

    function _closeActiveInline() {
      if (_activeInlineClose) {
        var fn = _activeInlineClose;
        _activeInlineClose = null;
        fn();
      }
    }

    // sprout-tags.js から呼べるように公開（タグ編集開始時に他のインライン編集を閉じるため）
    window.sproutTopInline = { closeActive: _closeActiveInline };

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
        _appendAddRow($body);
        return;
      }

      $.each(items, function (i, item) {
        var deadline = item.deadline ? dayjs(item.deadline).format('MM/DD') : '-';

        var $row = $('<div></div>')
          .addClass('tbl-row tbl-data-row border-b border-gray-100 dark:border-gray-700')
          .attr('data-item-id', item.id);

        var detailText = item.detail || '';
        var detailDisplay = detailText.length > 40
          ? escapeHtml(detailText.substring(0, 40)) + '…'
          : (escapeHtml(detailText) || '-');

        $row.html(
          '<div class="tbl-cell"></div>' +
          '<div class="tbl-cell" data-inline-field="title" style="position:relative;">' +
            '<span class="sprout-link" style="display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;padding-right:20px;">' + escapeHtml(item.title) + '</span>' +
            '<button class="btn-open-modal" data-row-id="' + item.id + '" title="詳細を開く">' +
              '<i data-lucide="arrow-up-right" style="width:15px;height:15px;"></i>' +
            '</button>' +
          '</div>' +
          '<div class="tbl-cell">' +
            '<div class="sprout-tag-mount" data-item-id="' + item.id + '"></div>' +
          '</div>' +
          '<div class="tbl-cell" data-inline-field="statusCd">' +
            renderStatusPill(item.statusCd, item.statusName) +
          '</div>' +
          '<div class="tbl-cell" data-inline-field="priorityCd">' +
            renderPriorityPill(item.priorityCd, item.priorityName) +
          '</div>' +
          '<div class="tbl-cell text-xs text-gray-500" data-inline-field="deadline">' + deadline + '</div>' +
          '<div class="tbl-cell text-xs sprout-link" data-inline-field="detail" title="' + escapeHtml(detailText) + '">' + detailDisplay + '</div>' +
          '<div class="tbl-cell text-xs text-gray-500">-</div>' +
          '<div class="tbl-cell">' +
            (item._tagIds && item._tagIds.length > 0
              ? '<button class="btn-measure flex items-center justify-center w-7 h-7 rounded hover:bg-green-100 dark:hover:bg-green-900/30 text-gray-400 hover:text-green-600 transition" data-item-id="' + item.id + '" data-item-title="' + escapeHtml(item.title) + '" aria-label="工数計測" title="工数を計測する">' +
                  '<i data-lucide="timer" style="width:15px;height:15px;"></i>' +
                '</button>'
              : '<button class="btn-measure flex items-center justify-center w-7 h-7 rounded text-gray-300 dark:text-gray-600 cursor-not-allowed" disabled aria-label="工数計測（タグなし）" title="タグを設定すると計測できます">' +
                  '<i data-lucide="timer" style="width:15px;height:15px;"></i>' +
                '</button>'
            ) +
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

      // lucide アイコン初期化（動的生成後に必要）
      if (typeof lucide !== 'undefined') {
        lucide.createIcons({ nodes: $body[0] ? [$body[0]] : [] });
      }

      // 「+ タスクを追加」行
      _appendAddRow($body);
    }

    function _appendAddRow($body) {
      var $addRow = $('<div></div>')
        .addClass('tbl-row border-b border-gray-100 dark:border-gray-700 tbl-add-row')
        .css('cursor', 'pointer');

      $addRow.html(
        '<div class="tbl-cell"></div>' +
        '<div class="tbl-cell" style="grid-column: 2 / span 8;">' +
          '<span class="tbl-add-placeholder text-xs text-gray-400 dark:text-gray-500 flex items-center gap-1.5 select-none">' +
            '<i data-lucide="plus" style="width:13px;height:13px;"></i> タスクを追加' +
          '</span>' +
          '<input type="text" class="tbl-add-input inline-edit-input hidden" ' +
                 'placeholder="タスク名を入力して Enter" style="width:100%;max-width:400px;" />' +
        '</div>'
      );

      $body.append($addRow);
      if (typeof lucide !== 'undefined') {
        lucide.createIcons({ nodes: [$addRow[0]] });
      }

      // クリックで入力欄を表示
      $addRow.off('click.addRow').on('click.addRow', function(e) {
        if ($(e.target).is('input')) return;
        $addRow.find('.tbl-add-placeholder').addClass('hidden');
        var $inp = $addRow.find('.tbl-add-input').removeClass('hidden').focus();

        $inp.off('keydown.addRow').on('keydown.addRow', function(ev) {
          if (ev.key === 'Enter' && !ev.isComposing) {
            ev.preventDefault();
            _quickCreateTask($inp.val().trim());
          }
          if (ev.key === 'Escape') {
            $inp.val('').addClass('hidden');
            $addRow.find('.tbl-add-placeholder').removeClass('hidden');
          }
        });

        $inp.off('blur.addRow').on('blur.addRow', function() {
          var title = $inp.val().trim();
          if (title) {
            _quickCreateTask(title);
          } else {
            $inp.addClass('hidden');
            $addRow.find('.tbl-add-placeholder').removeClass('hidden');
          }
        });
      });
    }

    function _quickCreateTask(title) {
      if (!title) return;

      // フィルター中のタグ ID を取得
      var tagIds = [];
      _selectedTagIds.forEach(function(id) { tagIds.push(id); });

      $.ajax({
        url: '/task/new',
        type: 'POST',
        data: { title: title, statusCd: 1, priorityCd: 1 }
      })
      .done(function(res) {
        var itemId = res.id;
        var afterLoad = function() {
          sprout.message.toast({ message: 'タスクを追加しました', type: 'success' });
        };
        if (itemId && tagIds.length > 0) {
          $.post('/items/' + itemId + '/tags', { tagIds: tagIds })
            .always(function() { loadItems(); afterLoad(); });
        } else {
          loadItems();
          afterLoad();
        }
      })
      .fail(function() {
        sprout.message.toast({ message: 'タスクの追加に失敗しました', type: 'error' });
      });
    }

    function renderStatusPill(statusCd, statusName) {
      var cls = 'status-pill';
      if      (statusCd === 1) cls += ' status-pill--todo';
      else if (statusCd === 2) cls += ' status-pill--doing';
      else if (statusCd === 3) cls += ' status-pill--done';
      return '<span class="' + cls + '">' + escapeHtml(statusName || '-') + '</span>';
    }

    function renderPriorityPill(priorityCd, priorityName) {
      var cls = 'priority-pill';
      if      (priorityCd === 3) cls += ' priority-pill--high';
      else if (priorityCd === 2) cls += ' priority-pill--mid';
      else                       cls += ' priority-pill--low';
      return '<span class="' + cls + '">' + escapeHtml(priorityName || '-') + '</span>';
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
      // Lv.内のExp進捗(%) 実際のステージ閾値ベースで計算
      var expPct = _calcExpPct(tag.exp || 0, tag.lv || 1);
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
        '<p class="text-center text-xs text-sprout-content opacity-70">Lv.' + (tag.lv || 1) + ' / ' + escapeHtml(tag.stageName || '種') + '</p>' +
        '<div class="w-full h-0.5 bg-gray-200 dark:bg-gray-700 rounded-full mt-1.5 overflow-hidden">' +
          '<div class="h-full bg-green-400 rounded-full" style="width:' + expPct + '%"></div>' +
        '</div>'
      );
      
      $list.append($card);
    });

    // GroveカードクリックでFeature反映＋タグフィルター
    $list.off('click.grove').on('click.grove', '.grove-card', function() {
      var tagId = $(this).data('tag-id');
      // 最新データ (_allTagData) から検索して常に最新 EXP を取得
      var currentList = (_allTagData && _allTagData.tagList) ? _allTagData.tagList : tagList;
      var tag = currentList.find(function(t) { return t.tagId === tagId; });
      if (!tag) return;

      _selectedFeatTag = tag.tagId;
      updateFeatured(tag);

      if (typeof filterByTag === 'function') {
        filterByTag(tagId);
      }
    });
  }

  // ステージ閾値（SproutStage enum と同値）
  var _STAGE_THRESHOLDS = [0, 300, 1000, 2500, 5000, 9500, 15600, 23000, 32000, 47000];

  /** 累計 EXP と現 Lv から、現レベル内の進捗(%)を返す */
  function _calcExpPct(exp, lv) {
    var idx = Math.min(Math.max(lv, 1), 10) - 1; // 0-based
    var cur  = _STAGE_THRESHOLDS[idx];
    var next = idx < 9 ? _STAGE_THRESHOLDS[idx + 1] : null;
    if (!next) return 100; // Lv10 は常に満タン
    return Math.min(100, Math.max(0, ((exp - cur) / (next - cur)) * 100));
  }

  /** 現 Lv 内の EXP（表示用）を返す */
  function _expInLv(exp, lv) {
    var idx = Math.min(Math.max(lv, 1), 10) - 1;
    return exp - _STAGE_THRESHOLDS[idx];
  }

  /** 次 Lv に必要な EXP 幅（表示用）を返す */
  function _expToNext(lv) {
    var idx = Math.min(Math.max(lv, 1), 10) - 1;
    if (idx >= 9) return null; // Lv10 以上
    return _STAGE_THRESHOLDS[idx + 1] - _STAGE_THRESHOLDS[idx];
  }

  // Featured更新
  function updateFeatured(tag) {
    var exp = tag.exp || 0;
    var lv  = tag.lv || 1;
    var expPct     = _calcExpPct(exp, lv);
    var inLv       = _expInLv(exp, lv);
    var toNext     = _expToNext(lv);
    var expDisplay = toNext != null ? (inLv + ' / ' + toNext) : ('MAX (' + exp + ')');
    var circumference = 427.3; // 2π × r(68)

    $(_this.featNameId).text(tag.tagName).css('color', '');
    $(_this.featStageId).text(tag.stageName || '種');
    $(_this.featExpId).text(expDisplay);
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
    $(_this.featExpId).text('— / —'); // eslint-disable-line no-irregular-whitespace
    $(_this.featBarId).css('width', '0%');
    $(_this.arcRingId).css('stroke-dashoffset', 427.3);
    _selectedFeatTag = null;
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

      // フィルター中のタグがあれば取得（複数選択の場合は全て渡す）
      var presetTagObjs = [];
      if (_selectedTagIds.size > 0 && typeof _allTagData !== 'undefined' && _allTagData) {
        _selectedTagIds.forEach(function(tid) {
          var tag = (_allTagData.tagList || []).find(function(t) { return String(t.tagId) === String(tid); });
          if (tag) presetTagObjs.push(tag);
        });
      }

      sprout.util.openModal({
        modalId: 'itemUpdateModal',
        url: '/modal/update',
        data: { modalFlg: 0 },
        callBack: function ($modalEl) {
          itemUpdateModal.init($modalEl, { presetTags: presetTagObjs });
        }
      });
    });

  // 更新モーダル（タイトル右端のホバーボタンから開く）
  $(_this.tableId)
    .off('click.openUpdateModal')
    .on('click.openUpdateModal', '.btn-open-modal', function (e) {
      e.stopPropagation();
      const itemId = $(this).data('row-id');
      if (!itemId) return;
      sprout.util.openModal({
        modalId: 'itemUpdateModal',
        url: '/modal/update',
        data: { modalFlg: 1, id: itemId },
        callBack: function ($modalEl) { itemUpdateModal.init($modalEl); }
      });
    });

  // タイマーモーダル起動
  $(_this.tableId)
    .off('click.openTimerModal')
    .on('click.openTimerModal', '.btn-measure:not([disabled])', function (e) {
      e.stopPropagation();

      var itemId    = $(this).data('item-id');
      var itemTitle = $(this).data('item-title');
      if (!itemId) return;

      sprout.util.openModal({
        modalId:       'timerModal',
        url:           '/work-log/modal',
        data:          { itemId: itemId, itemTitle: itemTitle },
        skipFormCheck: true,
        callBack:      function ($modalEl) {
          // デフォルトのクローズハンドラーを上書き:
          // 計測前(idle)のみ背景クリック・Esc で閉じられる
          $modalEl.off('click.modal-bg').on('click.modal-bg', function(ev) {
            if (ev.target !== this) return;
            var state = localStorage.getItem('sprout_timer_state') || 'idle';
            if (state === 'idle') { $modalEl.remove(); }
          });
          $(document).off('keydown.modal').on('keydown.modal', function(ev) {
            if (ev.key !== 'Escape') return;
            var state = localStorage.getItem('sprout_timer_state') || 'idle';
            if (state === 'idle') {
              $(document).off('keydown.modal');
              $modalEl.remove();
            }
          });

          // タイマーモーダル初期化（ID プレフィックス付与後に呼ぶ）
          timerModal.init($modalEl);
        }
      });
    });

  function loadGrove() {
    return $.ajax({ url: '/tags/all', type: 'GET', dataType: 'json', cache: false })
      .done(function(tagData) {
        _allTagData = tagData;
        var tagList = tagData.tagList || [];
        initGrove(tagList);
        // 選択中タグの Featured パネルも更新
        if (_selectedFeatTag) {
          var updated = tagList.find(function(t) { return t.tagId === _selectedFeatTag; });
          if (updated) updateFeatured(updated);
        }
      });
  }

  $(document).on('sprout:tag-deleted', function (e, data) {
    loadItems();
    loadGrove();
  });

  // タグ付け変更後（追加・選択解除）にGroveを再描画
  $(document).on('sprout:tags-updated', function() {
    loadGrove();
  });

  $(document).on('sprout:task-updated', function () {
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

  // ===== インライン編集 =====

  var INLINE_STATUS_OPTS = [
    { cd: 1, name: '未着手', cls: 'status-pill status-pill--todo' },
    { cd: 2, name: '進行中', cls: 'status-pill status-pill--doing' },
    { cd: 3, name: '完了',   cls: 'status-pill status-pill--done' }
  ];
  var INLINE_PRIORITY_OPTS = [
    { cd: 3, name: '高', cls: 'priority-pill priority-pill--high' },
    { cd: 2, name: '中', cls: 'priority-pill priority-pill--mid' },
    { cd: 1, name: '低', cls: 'priority-pill priority-pill--low' }
  ];

  // DBに保存（全フィールド送信）
  function inlineSave(item, changes) {
    var updated = $.extend({}, item, changes);
    var deadlineStr = '';
    if (updated.deadline) {
      deadlineStr = dayjs(updated.deadline).format('YYYY/MM/DD');
    }
    return $.ajax({
      url: '/task/update',
      type: 'POST',
      data: {
        id: updated.id,
        title: updated.title || '',
        statusCd: updated.statusCd,
        priorityCd: updated.priorityCd,
        deadline: deadlineStr,
        detail: updated.detail || ''
      }
    }).done(function() {
      var idx = _allItems.findIndex(function(i) { return i.id === item.id; });
      if (idx !== -1) $.extend(_allItems[idx], changes);
      sprout.message.toast({ message: '保存しました', type: 'success' });
    }).fail(function() {
      sprout.message.toast({ message: '保存に失敗しました', type: 'error' });
    });
  }

  // テキスト系インライン編集（タイトル・詳細）
  function startTextEdit($cell, currentVal, onSave, onCancel) {
    var $input = $('<input type="text" class="inline-edit-input">').val(currentVal || '');
    $cell.html($input);
    $input.focus().select();
    var committed = false;

    function doSave() {
      if (committed) return;
      committed = true;
      _activeInlineClose = null;
      $input.off('blur.inlineEdit');
      onSave($input.val());
    }
    function doCancel() {
      if (committed) return;
      committed = true;
      _activeInlineClose = null;
      $input.off('blur.inlineEdit');
      onCancel();
    }

    _activeInlineClose = doSave;

    $input
      .on('blur.inlineEdit', function() { doSave(); })
      .on('keydown.inlineEdit', function(e) {
        if (e.key === 'Escape') { doCancel(); }
        else if (e.key === 'Enter' && !e.isComposing) { e.preventDefault(); doSave(); }
      });
  }

  // ドロップダウン選択（ステータス・優先度）
  function startDropdownEdit($cell, options, onSave) {
    var rect = $cell[0].getBoundingClientRect();

    var optHtml = options.map(function(opt) {
      return '<div class="sprout-inline-opt" data-cd="' + opt.cd + '" ' +
        'style="padding:5px 10px;cursor:pointer;border-radius:7px;">' +
        '<span class="' + opt.cls + '">' + opt.name + '</span></div>';
    }).join('');

    // position:fixed はビューポート基準のため scroll 量を加算しない。
    // 画面下端をはみ出す場合は行の上側に表示してスクロール可能にする。
    var maxHeight = 220;
    var top = rect.bottom + 2;
    if (top + maxHeight > window.innerHeight) {
      top = Math.max(8, rect.top - maxHeight - 2);
    }

    var $dd = $('<div id="sprout-inline-dropdown" style="' +
      'position:fixed;' +
      'left:' + rect.left + 'px;' +
      'top:' + top + 'px;' +
      'max-height:' + maxHeight + 'px;' +
      'overflow-y:auto;' +
      'z-index:99999;' +
      'background:var(--card,#fff);' +
      'border:1px solid var(--line,#E6DFCD);' +
      'border-radius:10px;' +
      'box-shadow:0 4px 20px rgba(27,67,50,0.12);' +
      'padding:6px;' +
      'display:flex;flex-direction:column;gap:3px;' +
    '">' + optHtml + '</div>');
    $('body').append($dd);

    function closeDropdown() {
      if (_activeInlineClose === closeDropdown) _activeInlineClose = null;
      $dd.remove();
      $(document).off('click.inlineDropdown keydown.inlineDropdownEsc');
    }

    _activeInlineClose = closeDropdown;

    $(document)
      .off('click.inlineDropdown')
      .on('click.inlineDropdown', function(e) {
        var $opt = $(e.target).closest('.sprout-inline-opt');
        if ($opt.length) {
          var cd = Number($opt.data('cd'));
          closeDropdown();
          onSave(cd);
          return;
        }
        if (!$(e.target).closest('#sprout-inline-dropdown').length) {
          closeDropdown();
        }
      });

    $(document)
      .off('keydown.inlineDropdownEsc')
      .on('keydown.inlineDropdownEsc', function(e) {
        if (e.key === 'Escape') { closeDropdown(); }
      });
  }

  // 日付インライン編集（flatpickr）
  function startDateEdit($cell, currentDeadline, onSave, onCancel) {
    var defaultDate = currentDeadline ? dayjs(currentDeadline).toDate() : null;
    var $input = $('<input type="text" class="inline-edit-input" readonly placeholder="日付を選択">');
    $cell.html($input);
    var committed = false;

    var fp = flatpickr($input[0], {
      locale: 'ja',
      dateFormat: 'Y/m/d',
      defaultDate: defaultDate,
      disableMobile: true,
      allowInput: false,
      onClose: function(selectedDates) {
        if (committed) return;
        committed = true;
        _activeInlineClose = null;
        fp.destroy();
        if (selectedDates.length > 0) {
          onSave(dayjs(selectedDates[0]).format('YYYY-MM-DD'));
        } else {
          onCancel();
        }
      }
    });

    _activeInlineClose = function() {
      if (committed) return;
      fp.close();
    };

    $input.on('keydown.inlineEdit', function(e) {
      if (e.key === 'Escape') {
        if (committed) return;
        committed = true;
        _activeInlineClose = null;
        fp.close();
        fp.destroy();
        onCancel();
      }
    });

    fp.open();
  }

  // インライン編集クリックハンドラー
  $(_this.tableId)
    .off('click.inlineEdit')
    .on('click.inlineEdit', '.tbl-cell[data-inline-field]', function(e) {
      e.stopPropagation();

      // モーダルオープンボタンがクリックされた場合は無視
      if ($(e.target).closest('.btn-open-modal').length) return;
      // 既に編集中のセルなら無視（継続して入力させる）
      if ($(this).find('input, textarea').length) return;

      // 他のインライン編集（テキスト/日付/ドロップダウン）が開いていれば確定保存して閉じる
      _closeActiveInline();
      // タグ編集が開いていれば確定保存して閉じる
      if (typeof sprout !== 'undefined' && sprout.tags && typeof sprout.tags.closeActiveEdit === 'function') {
        sprout.tags.closeActiveEdit();
      }

      var field = $(this).data('inline-field');
      var $cell = $(this);
      var itemId = Number($cell.closest('.tbl-data-row').data('item-id'));
      var item = _allItems.find(function(i) { return i.id === itemId; });
      if (!item) return;

      var origHtml = $cell.html();

      switch (field) {
        case 'title':
          startTextEdit($cell, item.title,
            function(newVal) {
              if (!newVal.trim()) { $cell.html(origHtml); return; }
              inlineSave(item, { title: newVal.trim() })
                .done(function() {
                  var u = _allItems.find(function(i) { return i.id === itemId; });
                  $cell.html('<span class="sprout-link">' + escapeHtml(u.title) + '</span>');
                })
                .fail(function() { $cell.html(origHtml); });
            },
            function() { $cell.html(origHtml); }
          );
          break;

        case 'detail':
          startTextEdit($cell, item.detail || '',
            function(newVal) {
              inlineSave(item, { detail: newVal })
                .done(function() {
                  var u = _allItems.find(function(i) { return i.id === itemId; });
                  var dt = u.detail || '';
                  var disp = dt.length > 40 ? escapeHtml(dt.substring(0, 40)) + '…' : (escapeHtml(dt) || '-');
                  $cell.html('<span title="' + escapeHtml(dt) + '">' + disp + '</span>');
                  $cell.attr('title', escapeHtml(dt));
                })
                .fail(function() { $cell.html(origHtml); });
            },
            function() { $cell.html(origHtml); }
          );
          break;

        case 'statusCd':
          startDropdownEdit($cell, INLINE_STATUS_OPTS, function(newCd) {
            var opt = INLINE_STATUS_OPTS.find(function(o) { return o.cd === newCd; });
            inlineSave(item, { statusCd: newCd, statusName: opt ? opt.name : '' })
              .done(function() {
                // 完了フィルターに引っかかる可能性があるためテーブル全体を再描画
                renderTable(getFilteredItems());
              })
              .fail(function() { $cell.html(origHtml); });
          });
          break;

        case 'priorityCd':
          startDropdownEdit($cell, INLINE_PRIORITY_OPTS, function(newCd) {
            var opt = INLINE_PRIORITY_OPTS.find(function(o) { return o.cd === newCd; });
            inlineSave(item, { priorityCd: newCd, priorityName: opt ? opt.name : '' })
              .done(function() {
                var u = _allItems.find(function(i) { return i.id === itemId; });
                $cell.html(renderPriorityPill(u.priorityCd, u.priorityName));
              })
              .fail(function() { $cell.html(origHtml); });
          });
          break;

        case 'deadline':
          startDateEdit($cell, item.deadline,
            function(newIsoDate) {
              inlineSave(item, { deadline: newIsoDate })
                .done(function() {
                  var u = _allItems.find(function(i) { return i.id === itemId; });
                  $cell.html(u.deadline ? dayjs(u.deadline).format('MM/DD') : '-');
                })
                .fail(function() { $cell.html(origHtml); });
            },
            function() { $cell.html(origHtml); }
          );
          break;
      }
    });

  // ===== インライン編集ここまで =====

  loadGrove();
  loadItems();
});