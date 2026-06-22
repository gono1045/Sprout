/**
 * sprout-tags.js
 * タグ表示・編集コンポーネント
 */
var sprout = sprout || {};

sprout.tags = (function() {

  /**
   * 公開API
   */
  function mount(options) {
    const state = {
      el: $(options.el),
      anchorEl: null,
      itemId: options.itemId,
      tags: [],
      mode: 'view',
      allTags: [],
      finishing: false,
      dropdownIndex: -1,
      readonly: options.readonly === true,
      presetTags: options.presetTags || [],
      // 呼び出し元が既にタグ情報を持っている場合（テーブル一覧の一括取得結果など）に
      // 渡すと、行ごとの個別フェッチ（N+1）を回避できる
      initialTags: options.initialTags || null
    };

    state.el.data('sproutTagsState', state);
    state.isActive = false;
    init(state);
  }

  const TAG_COLORS = [
    'bg-red-400',
    'bg-orange-400',
    'bg-amber-400',
    'bg-yellow-400',
    'bg-lime-400',
    'bg-green-400',
    'bg-emerald-400',
    'bg-teal-400',
    'bg-cyan-400',
    'bg-sky-400',
    'bg-blue-400',
    'bg-purple-400'
  ];

  /**
   * 初期化
   */
  function init(state) {
    state.tags = [];
    state.originalTagIds = [];

    // DBに紐づく既存タグがあれば取得
    if (state.itemId) {
      if (state.initialTags) {
        // 呼び出し元から渡された一括取得済みデータを使う（個別フェッチを省略）
        state.tags = state.initialTags;
        state.originalTagIds = state.initialTags.map(t => t.tagId);
        renderView(state);
        return;
      }
      fetchItemTags(state.itemId)
        .done(function(tags) {
          state.tags = tags;
          state.originalTagIds = tags.map(t => t.tagId);
          renderView(state);
        })
        .fail(function() {
          console.error("タグ取得失敗 itemId:", state.itemId);
        });
    } else {
      // 新規作成時: presetTags が指定されていれば初期値として設定
      if (state.presetTags && state.presetTags.length > 0) {
        state.tags = state.presetTags.slice();
      }
      renderView(state);
    }
  }

  let currentState = null;
  let saveSortTimer = null;

  function saveSortDebounced() {
    clearTimeout(saveSortTimer);
    saveSortTimer = setTimeout(() => {
      if (!currentState?.allTags) return;
      saveTagSortOrders(currentState.allTags);
    }, 800);
  }

  function saveTagSortOrders(tags) {
    tags.forEach(tag => {
      updateTag(tag);
    });
  }

  /**
   * 未選択タグを返す
   */
  function getSelectableTags(state) {
    const selectedIds = new Set(state.tags.map(t => t.tagId));
    return state.allTags.filter(tag => !selectedIds.has(tag.tagId));
  }

  /**
   * 表示モード描画
   */
  function renderView(state) {
    $('.sprout-tag-dropdown-portal').remove();
    const $base = state.el;
    $base.removeClass('relative overflow-visible').addClass('cursor-pointer');

    const html = state.tags.map(tag => {
      return `
        <span
          class="tag-chip inline-flex items-center px-2 py-1 rounded text-white text-sm mr-1 ${tag.tagColor}"
        >
          ${tag.tagName}
        </span>
      `;
    }).join("");

    // 内部 div で上下中央に揃える（ヘッダーと左揃えを合わせる）
    // padding はセル側（.tbl-cell）の標準値に揃え、ここでは w-full / h-full で
    // セルの content box を満たすだけにする（タグなし時もクリック領域を広く確保）
    state.el.html(`
      <div class="sprout-tag-view flex items-center justify-center cursor-pointer w-full h-full">
        <div class="flex items-center justify-center gap-1 flex-wrap w-full">
          ${html || '<span class="tag-empty">&nbsp;</span>'}
        </div>
      </div>
    `);

    $base.off('click.sproutTags');

    // readonly モードではクリックしても編集モードに入らない
    if (state.readonly) {
      $base.removeClass('cursor-pointer').addClass('cursor-default');
    } else {
      $base.on('click.sproutTags', '.sprout-tag-view', function (e) {
        e.stopPropagation();
        // 他のインライン編集（テキスト/ドロップダウン/日付）が開いていれば確定保存して閉じる
        if (window.sproutTopInline && typeof window.sproutTopInline.closeActive === 'function') {
          window.sproutTopInline.closeActive();
        }
        renderEdit(state);
      });
    }
  }

  /**
   * 編集モード描画
   */
  function renderEdit(state) {
    currentState = state;
    state.isActive = true;
    state.mode = 'edit';
    var $base = state.el;
    $base.removeClass('cursor-pointer').off('click.sproutTags')
      .addClass('relative overflow-visible');

    state.el.html(`
      <div class="sprout-tag-edit w-full h-full px-2 py-1 flex items-center">
        <div class="flex flex-wrap gap-1 items-center w-full">
          ${state.tags.map(tag => `
            <span
              class="sprout-tag-chip inline-flex items-center px-2 py-1 rounded text-white text-sm cursor-pointer
                ${tag.tagColor}"
              data-tag-id="${tag.tagId}"
            >
              ${tag.tagName}
            </span>
          `).join('')}
          <input type="text" class="sprout-tag-input min-w-[60px] flex-1 text-sm bg-transparent
              outline-none border-none pl-1 focus:ring-0" placeholder="タグを追加" />
        </div>
      </div>
    `);

    state.anchorEl = state.el.parent()[0];
    fetchAllTags(state);

    // input に自動フォーカス
    requestAnimationFrame(() => {
      const $input = state.el.find('.sprout-tag-input');
      if ($input.length) {
        $input.focus();
      }
    });
  }

  /**
   * タグリストから選択
   */
  $(document)
    .off('click.sproutTagOption')
    .on('click.sproutTagOption', '.sprout-tag-option', function (e) {
      e.stopPropagation();

      const tagId = $(this).data('tag-id');
      const tag = currentState.allTags.find(t => t.tagId === tagId);

      if (!tag) return;

      // 選択状態に追加
      currentState.tags.push(tag);

      // 再描画（閉じない）
      renderEdit(currentState);
      renderDropdown(currentState);
    });

    /**
     * フォーカスアウトで編集終了
     */
    $(document)
      .off('click.sproutTagsClose')
      .on('click.sproutTagsClose', function (e) {

        if (!currentState || currentState.mode !== 'edit') return;

        // 編集エリア内なら無視
        if ($(e.target).closest('.sprout-tag-edit').length) return;
        if ($(e.target).closest('.sprout-tag-dropdown-portal').length) return;
        if ($(e.target).closest('.sprout-tag-action-popup').length) return;

        finishEdit();
      });

  /**
   * 編集エリア内のクリックはフォーカスアウトに判定させない
   */
  $(document)
    .off('click.sproutTagEditInner')
    .on('click.sproutTagEditInner', '.sprout-tag-edit', function (e) {
      e.stopPropagation();
    });

  // position:fixed のドロップダウンはページ側のスクロールで位置がズレるため閉じる。
  // ただしドロップダウン自身の内部スクロール（候補が多い場合）では閉じない。
  // 単に portal を消すだけだと編集モードが宙に浮いて再編集できなくなるため、
  // finishEdit() で編集状態ごと正しく終了させる。
  document.addEventListener('scroll', function(e) {
    var $portal = $('.sprout-tag-dropdown-portal');
    if (!$portal.length) return;
    if ($portal[0].contains(e.target)) return;
    finishEdit();
  }, true);

  /**
   * ドロップダウンリスト描画
   */
  function renderDropdown(state, filterText = '') {
    $('.sprout-tag-dropdown-portal').remove();

    const rect = state.anchorEl.getBoundingClientRect();

    const selectableTags = getSelectableTags(state)
      .filter(tag => tag.tagName.toLowerCase().includes(filterText.toLowerCase()));

    if (!selectableTags.length) return;

    if (state.dropdownIndex >= selectableTags.length) {
      state.dropdownIndex = -1;
    }

    // position:fixed はビューポート基準のため scroll 量を加算しない。
    // 画面下端をはみ出す場合は行の上側に表示してスクロール可能にする。
    var maxHeight = 240;
    var top = rect.bottom + 4;
    if (top + maxHeight > window.innerHeight) {
      top = Math.max(8, rect.top - maxHeight - 4);
    }

    const html = `
      <ul class="sprout-tag-dropdown-portal" style="
        position: fixed;
        left: ${rect.left}px;
        top: ${top}px;
        width: ${rect.width}px;
        max-height: ${maxHeight}px;
        overflow-y: auto;
        background: var(--card, #fff);
        border: 1px solid var(--line, #E6DFCD);
        border-radius: 10px;
        box-shadow: 0 4px 20px rgba(27,67,50,0.14);
        z-index: 99999;
        padding: 4px 0;
        margin: 0;
        list-style: none;
      ">
      ${selectableTags.map((tag, index) => `
        <li
          class="sprout-tag-option"
          style="
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 6px 10px;
            cursor: pointer;
            ${index === state.dropdownIndex ? 'background: rgba(167,243,200,0.2);' : ''}
          "
          data-tag-id="${tag.tagId}"
          data-index="${index}"
        >
          <div style="display:flex;flex-direction:column;gap:1px;opacity:0.45;user-select:none;">
            <span class="sprout-tag-move-up" style="cursor:pointer;font-size:9px;line-height:1.2;">▲</span>
            <span class="sprout-tag-move-down" style="cursor:pointer;font-size:9px;line-height:1.2;">▼</span>
          </div>
          <span
            class="tag-chip ${tag.tagColor}"
            style="flex:1;padding:3px 10px;border-radius:9999px;color:#fff;font-size:12px;font-weight:500;
              overflow:hidden;text-overflow:ellipsis;white-space:nowrap;text-align:center;"
          >${tag.tagName}</span>
          <button class="sprout-tag-gear" style="background:none;border:none;padding:2px 3px;cursor:pointer;
            opacity:0.4;display:flex;align-items:center;" title="編集">
            <i data-lucide="settings-2" style="width:13px;height:13px;"></i>
          </button>
        </li>
      `).join('')}
      </ul>
    `;

    $('body').append(html);
    if (typeof lucide !== 'undefined') {
      lucide.createIcons({ nodes: [$('.sprout-tag-dropdown-portal')[0]] });
    }
  }

  /**
   * 操作ポップアップ描画
   */
  function renderActionPopup(state, tagId, $li) {
    $('.sprout-tag-action-popup').remove();

    const dropdownRect = $('.sprout-tag-dropdown-portal')[0].getBoundingClientRect();
    const liRect = $li[0].getBoundingClientRect();

    const html = `
      <div
        class="sprout-tag-action-popup"
        data-tag-id="${tagId}"
        style="
          position: fixed;
          left: ${dropdownRect.right + 4}px;
          top: ${liRect.top}px;
          background: var(--card, #fff);
          border: 1px solid var(--line, #E6DFCD);
          border-radius: 10px;
          box-shadow: 0 4px 20px rgba(27,67,50,0.14);
          z-index: 99999;
          padding: 10px;
          min-width: 160px;
        "
      >
        <div class="sprout-tag-color-palette" style="display:grid;grid-template-columns:repeat(6,1fr);gap:5px;margin-bottom:10px;">
          ${TAG_COLORS.map(color => `
            <div
              class="sprout-tag-color-item ${color}"
              data-tag-id="${tagId}"
              data-color="${color}"
              style="width:22px;height:22px;border-radius:50%;cursor:pointer;"
            ></div>
          `).join('')}
        </div>
        <button
          class="sprout-tag-action-item"
          data-action="delete"
          style="
            width:100%;padding:6px 0;border-radius:8px;border:none;
            background:#fee2e2;color:#991b1b;font-size:12px;font-weight:600;cursor:pointer;
          "
        >削除</button>
      </div>
    `;

    $('body').append(html);
  }

  /**
   * 並べ替え
   */
  $(document)
    .off('click.sproutTagMove')
    .on('click.sproutTagMove', '.sprout-tag-move-up, .sprout-tag-move-down', function (e) {
      e.stopPropagation();

      const $li = $(this).closest('li');
      const tagId = Number($li.data('tag-id'));
      const isUp = $(this).hasClass('sprout-tag-move-up');

      moveTag(currentState, tagId, isUp ? 'up' : 'down');

      renderDropdown(currentState);

      // フォーカス追従
      requestAnimationFrame(() => {
        $(`.sprout-tag-option[data-tag-id="${$li.data('tag-id')}"]`)
          .find(isUp ? '.sprout-tag-move-up' : '.sprout-tag-move-down')
          .focus();
      });

      saveSortDebounced();
    });

  /**
   * 循環ロジック
   */
  function moveTag(state, tagId, direction) {
    const list = [...state.allTags];
    const index = list.findIndex(t => t.tagId === tagId)
    const length = list.length;

    let newIndex;

    if (direction === 'up') {
      newIndex = index === 0 ? length - 1 : index - 1;
    } else {
      newIndex = index === length - 1 ? 0 : index + 1;
    }

    const [moved] = list.splice(index, 1);
    list.splice(newIndex, 0, moved);

    // 並び順再採番
    list.forEach((tag, i) => {
      tag.tagSortOrder = i + 1;
    });

    state.allTags = list;
  }

  /**
   * 歯車アイコンクリック
   */
  $(document)
    .off('click.sproutTagGear')
    .on('click.sproutTagGear', '.sprout-tag-gear', function (e) {
      e.stopPropagation();

      const $li = $(this).closest('li');
      const tagId = $li.data('tag-id');

      // 同じタグのポップアップが開いていれば閉じる
      const $existing = $('.sprout-tag-action-popup');
      if ($existing.length && $existing.data('tag-id') === tagId) {
        $existing.remove();
        return;
      }

      renderActionPopup(currentState, tagId, $li);
    });

  /**
   * Escキーで閉じる
   */
  $(document)
    .off('keydown.sproutTagsEsc')
    .on('keydown.sproutTagsEsc', function (e) {
      if (e.key !== 'Escape') return;

      // inputフォーカス中なら一気に編集終了
      if ($(document.activeElement).hasClass('sprout-tag-input')) {
        finishEdit();
        return;
      }
      // 操作ポップアップ
      if ($('.sprout-tag-action-popup').length) {
        $('.sprout-tag-action-popup').remove();
        return;
      }

      // ドロップダウン
      if ($('.sprout-tag-dropdown-portal').length) {
        $('.sprout-tag-dropdown-portal').remove();
        return;
      }

      // 編集モード終了
      finishEdit();
    });

  /**
   * 操作ポップアップ内クリック
   */
  $(document)
    .off('click.sproutTagAction')
    .on('click.sproutTagAction', '.sprout-tag-action-item', function (e) {
      e.stopPropagation();

      const action = $(this).data('action');
      const $popup = $(this).closest('.sprout-tag-action-popup');
      const tagId = $popup.data('tag-id');
      const itemId = currentState.itemId;

      if (action === 'color') {
        renderColorPalette(tagId, $popup);
      }

      if (action === 'delete') {

        sprout.message.confirmExec({
          message: 'このタグを削除すると、蓄積したEXP・Lvが失われます。\n本当に削除しますか？',
          okText: '削除する',
          danger: true,
          onOk: function() {
            $.ajax({
              url: '/tags/delete',
              method: 'POST',
              data: {
                tagId: tagId
              }
            })
            .done(function() {

              // stateから完全削除
              currentState.tags = currentState.tags.filter(
                tag => tag.tagId !== tagId
              );

              currentState.allTags = currentState.allTags.filter(
                tag => tag.tagId !== tagId
              );

              currentState.originalTagIds =
                currentState.tags.map(t => t.tagId);

              // 操作ポップアップを閉じる
              $('.sprout-tag-action-popup').remove();
              finishEdit();
              $(document).trigger('sprout:tag-deleted', { tagId });

              sprout.message.toast({
                message: 'タグを削除しました',
                type: 'success'
              });
            })
            .fail(function() {
              sprout.message.toast({
                message: '削除に失敗しました',
                type: 'error'
              });
            });
          }
        });
      }
    });

    /**
     * カラーパレット操作
     */
    $(document)
      .off('click.sproutTagColor')
      .on('click.sproutTagColor', '.sprout-tag-color-item', function (e) {
        e.preventDefault();
        e.stopPropagation();

        const tagId = Number($(this).data('tag-id'));
        const color = $(this).data('color');

        // allTags 側を更新
        const tag = currentState.allTags.find(t => t.tagId === tagId);
        if (!tag) return;

        tag.tagColor = color;

        // 選択済みタグにも反映
        currentState.tags.forEach(t => {
          if (t.tagId === tagId) {
            t.tagColor = color;
          }
        });

        // 再描画（閉じない）
        renderEdit(currentState);
        renderDropdown(currentState);

        // 保存
        updateTag(tag);
      });

    /**
     * タグチップクリックで選択解除
     */
    $(document)
      .off('click.sproutTagChip')
      .on('click.sproutTagChip', '.sprout-tag-chip', function (e) {
        e.stopPropagation();

        const tagId = $(this).data('tag-id');

        // state.tags から削除
        currentState.tags = currentState.tags.filter(
          tag => tag.tagId !== tagId
        );

        // 再描画
        renderEdit(currentState);
        renderDropdown(currentState);
      });

  /**
   * 新規タグ登録input
   */
  $(document)
    .off('keydown.sproutTagInput')
    .on('keydown.sproutTagInput', '.sprout-tag-input', function (e) {

      if (e.originalEvent && e.originalEvent.isComposing) return;
      if (e.keyCode === 229) return;
      if (e.key !== 'Enter') return;

      // dropdown選択中なら「新規作成しない」
      if (currentState && currentState.dropdownIndex >= 0) {
        return;
      }

      e.preventDefault();

      const name = $(this).val().trim();
      if (!name) {
        finishEdit();
        return;
      }

      createTag(name);
      $(this).val('');
    });

  /**
   * autoComplete用inputイベント
   */
  $(document)
    .off('input.sproutTagInput')
    .on('input.sproutTagInput', '.sprout-tag-input', function() {
      const text = $(this).val().trim();
      currentState.dropdownIndex = -1;
      renderDropdown(currentState, text);
    });

    $(document)
      .off('keydown.sproutTagInputNav')
      .on('keydown.sproutTagInputNav', '.sprout-tag-input', function (e) {

        if (!currentState) return;

        const $options = $('.sprout-tag-option');
        if (!$options.length) return;

        // ↓
        if (e.key === 'ArrowDown') {
          e.preventDefault();
          currentState.dropdownIndex =
            (currentState.dropdownIndex + 1) % $options.length;
          renderDropdown(currentState, $(this).val().trim());
          return;
        }

        // ↑
        if (e.key === 'ArrowUp') {
          e.preventDefault();
          currentState.dropdownIndex =
            currentState.dropdownIndex <= 0
              ? $options.length - 1
              : currentState.dropdownIndex - 1;
          renderDropdown(currentState, $(this).val().trim());
          return;
        }

        // Enter（選択中がある場合）
        if (e.key === 'Enter' && currentState.dropdownIndex >= 0) {
          e.preventDefault();
          $options.eq(currentState.dropdownIndex).trigger('click');
          currentState.dropdownIndex = -1;
          return;
        }
      });

  /**
   * すべてのタグ取得
   */
  function fetchAllTags(state) {
    $.ajax({
      url: '/tags/all',
      method: 'GET'
    })
    .done(function (form) {
      if (!state.allTags || !state.allTags.length) {
        state.allTags = form.tagList;
      }
      currentState = state;
      renderDropdown(state);
    })
    .fail(function () {
      console.error('タグ一覧取得失敗');
    });
  }

  /**
   * 編集モードを終了する
   */
  function finishEdit() {
    if (!currentState?.isActive) return;
    currentState.isActive = false;

    if (currentState.mode !== 'edit') return;
    if (currentState.finishing) return;

    const currentIds = currentState.tags.map(t => t.tagId);
    const originalIds = currentState.originalTagIds || [];

    // ★ 並びも含めて完全一致なら「保存せず終了」
    const isSame =
      currentIds.length === originalIds.length &&
      currentIds.every((id, i) => id === originalIds[i]);

    // UIクリーンアップは常に行う
    $('.sprout-tag-dropdown-portal').remove();
    $('.sprout-tag-action-popup').remove();

    if (isSame) {
      currentState.mode = 'view';
      currentState.finishing = false;
      currentState.originalTagIds = currentState.tags.map(t => t.tagId);
      renderView(currentState);
      return;
    }

    currentState.finishing = true;

    if (!currentState.itemId) {
      currentState.mode = 'view';
      currentState.finishing = false;
      renderView(currentState);
      return;
    }

    var targetState = currentState;

    // 既存タスク（originalTagIdsが存在＝EXP付与済みの可能性あり）のタグを変更する場合は、
    // 以後のEXPが切り替え先のタグに蓄積されることを警告する
    if (originalIds.length > 0) {
      var addedTags = currentState.tags.filter(t => !originalIds.includes(t.tagId));
      var addedNames = addedTags.map(t => t.tagName).join('、');
      var warnMessage = addedNames
        ? `タグを変更すると、今後の工数計測で得られるEXPは「${addedNames}」に蓄積されます。よろしいですか？`
        : 'タグを変更すると、今後の工数計測で得られるEXPは変更後のタグに蓄積されます。よろしいですか？';

      sprout.message.confirmExec({
        message: warnMessage,
        okText: '変更する',
        cancelText: 'キャンセル',
        onOk: function () { _saveTagChange(targetState, currentIds); },
        onCancel: function () {
          // 変更前のタグ状態に戻す
          targetState.tags = (targetState.allTags || []).filter(t => originalIds.includes(t.tagId));
          if (!targetState.tags.length) {
            // allTagsに無ければサーバーから再取得して復元
            fetchItemTags(targetState.itemId).done(function (tags) {
              targetState.tags = tags;
              targetState.mode = 'view';
              targetState.finishing = false;
              renderView(targetState);
            });
            return;
          }
          targetState.mode = 'view';
          targetState.finishing = false;
          renderView(targetState);
        }
      });
      return;
    }

    _saveTagChange(targetState, currentIds);
  }

  /**
   * タグ紐付けをサーバーに保存し、画面を更新する
   */
  function _saveTagChange(targetState, currentIds) {
    saveItemTags(targetState.itemId, currentIds)
      .done(function () {
        fetchItemTags(targetState.itemId).done(function (tags) {
          targetState.tags = tags;
          targetState.originalTagIds = tags.map(t => t.tagId);
          targetState.mode = 'view';
          targetState.finishing = false;
          renderView(targetState);
          // Grove（マイガーデン）再描画トリガー
          $(document).trigger('sprout:tags-updated');
          // タグ件数の変化でタイマーボタンの活性状態が変わるためテーブルも再読込
          $(document).trigger('sprout:task-updated');
        });
      })
      .fail(function () {
        targetState.finishing = false;
        sprout.message.toast({
          message: 'タグの保存に失敗しました',
          type: 'error'
        });
        // サーバーの実際の状態を再取得して表示を復元する
        fetchItemTags(targetState.itemId).done(function (tags) {
          targetState.tags = tags;
          targetState.originalTagIds = tags.map(t => t.tagId);
          targetState.mode = 'view';
          renderView(targetState);
        });
      });
  }

  /**
   * Itemに紐づくタグ取得
   */
  function fetchItemTags(itemId) {
    return $.ajax({
      url: `/items/${itemId}/tags`,
      method: "GET"
    });
  }

  /**
   * Itemに紐づくタグ保存
   */
  function saveItemTags(itemId, tagIds) {
    return $.ajax({
      url: `/items/${itemId}/tags`,
      method: 'POST',
      traditional: true,
      data: {
        tagIds: tagIds
      }
    });
  }

  /**
   * 新規タグを作成
   */
  function createTag(tagName) {
    $.ajax({
      url: '/tags',
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({
        tagName: tagName
      })
    })
    .done(function (tag) {

      // 全タグに追加
      currentState.allTags.push(tag);
      // 選択状態にも追加
      currentState.tags.push(tag);

      // originalを更新
      currentState.originalTagIds =
          currentState.tags.map(t => t.tagId);

      // 再描画（閉じない）
      renderEdit(currentState);
      renderDropdown(currentState);

      // 新規タグをマイガーデンに即時反映
      $(document).trigger('sprout:tags-updated');

      if (currentState.itemId) {
        saveItemTags(
          currentState.itemId,
          currentState.tags.map(t => t.tagId)
        ).done(function () {
          // タグ件数の変化でタイマーボタンの活性状態が変わるためテーブルも再読込
          $(document).trigger('sprout:task-updated');
        });
      }
    })
    .fail(function () {
      sprout.message.toast({
        message: 'タグ作成に失敗しました',
        type: 'error'
      });
    });
  }

  /**
   * タグ更新
   */
  function updateTag(tag) {
    return $.ajax({
      url: '/tags/update',
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({
        tagId: tag.tagId,
        tagName: tag.tagName,
        tagColor: tag.tagColor,
        tagSortOrder: tag.tagSortOrder
      })
    });
  }

  /**
   * 他コンポーネントから呼ばれる: 現在タグ編集中なら確定保存して閉じる
   */
  function closeActiveEdit() {
    if (currentState && currentState.mode === 'edit') {
      finishEdit();
    }
  }

  return {
    mount: mount,
    closeActiveEdit: closeActiveEdit
  };

})();
