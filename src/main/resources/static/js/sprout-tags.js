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
      dropdownIndex: -1
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

    // 内部 div で上下中央に揃える
    state.el.html(`
      <div class="sprout-tag-view flex items-center justify-center w-full h-full cursor-pointer">
        <div class="flex items-center justify-center">
          ${html || '<span class="tag-empty inline-block w-full h-full">&nbsp;</span>'}
        </div>
      </div>
    `);

    $base
      .off('click.sproutTags')
      .on('click.sproutTags', '.sprout-tag-view', function (e) {
        e.stopPropagation();
        renderEdit(state);
      });
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

  /**
   * ドロップダウンリスト描画
   */
  function renderDropdown(state, filterText = '') {
    $('.sprout-tag-dropdown-portal').remove();

    const rect = state.anchorEl.getBoundingClientRect();
    const scrollX = window.pageXOffset;
    const scrollY = window.pageYOffset;

    const selectableTags = getSelectableTags(state)
      .filter(tag => tag.tagName.toLowerCase().includes(filterText.toLowerCase()));

    if (!selectableTags.length) return;

    if (state.dropdownIndex >= selectableTags.length) {
      state.dropdownIndex = -1;
    }

    const html = `
      <ul
        class="sprout-tag-dropdown-portal fixed bg-white dark:bg-gray-800 border shadow z-[9999]"
        style="
          left: ${rect.left + scrollX}px;
          top: ${rect.bottom + scrollY}px;
          width: ${rect.width}px;
        "
      >
      ${selectableTags.map((tag, index) => `
          <li
            class="sprout-tag-option group flex items-center gap-2 px-2 py-1 text-sm hover:bg-gray-100 cursor-pointer
              ${index === state.dropdownIndex ? 'bg-gray-100' : 'hover:bg-gray-100'}"
            data-tag-id="${tag.tagId}"
            data-index="${index}"
          >
            <div class="flex flex-col text-gray-400 text-xs leading-none select-none">
              <span class="sprout-tag-move-up cursor-pointer hover:text-black">▲</span>
              <span class="sprout-tag-move-down cursor-pointer hover:text-black">▼</span>
            </div>

            <div
              class="min-w-0 max-w-[60%] mx-auto px-2 py-1 rounded text-white truncate flex items-center justify-center min-h-[24px] ${tag.tagColor}"
            >
              ${tag.tagName}
            </div>
            <div class="sprout-tag-gear text-gray-400 cursor-pointer select-none">⚙</div>
          </li>
        `).join('')}
      </ul>
    `;

    $('body').append(html);
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
        class="sprout-tag-action-popup fixed bg-white dark:bg-gray-800 border shadow z-[10000]"
        data-tag-id="${tagId}"
        style="
          left: ${dropdownRect.right}px;
          top: ${liRect.top}px;
        "
      >
        <!-- カラーパレット（最初から表示） -->
        <div class="sprout-tag-color-palette grid grid-cols-6 gap-1 p-2">
          ${TAG_COLORS.map(color => `
            <div
              class="sprout-tag-color-item w-6 h-6 rounded cursor-pointer ${color}"
              data-tag-id="${tagId}"
              data-color="${color}"
            ></div>
          `).join('')}
        </div>

        <div class="px-2 pb-2">
          <div
            class="sprout-tag-action-item w-full text-center cursor-pointer rounded
            px-2 py-1 text-sm bg-red-500 hover:bg-red-600 text-white"
            data-action="delete"
          >
            削除
          </div>
        </div>
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
        console.log('削除', tagId);

        sprout.message.confirmExec({
          messageId: "WARN001",
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

    saveItemTags(currentState.itemId, currentIds)
      .done(function () {
        fetchItemTags(currentState.itemId).done(function (tags) {
          currentState.tags = tags;
          currentState.originalTagIds = tags.map(t => t.tagId); // ★ 状態更新
          currentState.mode = 'view';
          currentState.finishing = false;
          renderView(currentState);
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

      if (currentState.itemId) {
        saveItemTags(
          currentState.itemId,
          currentState.tags.map(t => t.tagId)
        );
      }

      // originalを更新
      currentState.originalTagIds =
          currentState.tags.map(t => t.tagId);

      // 再描画（閉じない）
      renderEdit(currentState);
      renderDropdown(currentState);
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

  return {
    mount: mount
  };

})();
