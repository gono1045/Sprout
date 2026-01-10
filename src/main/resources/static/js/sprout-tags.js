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
      itemId: options.itemId,
      tags: [],
      mode: 'view',
      allTags: [],
      finishing: false
    };

    init(state);
  }

  /**
   * 初期化
   */
  function init(state) {
    currentState = state;

    if (!state.itemId) {
      console.warn("itemId is required for sprout.tags");
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
  }

  let currentState = null;
  let saveSortTimer = null;

  function saveSortDebounced() {
    clearTimeout(saveSortTimer);
    saveSortTimer = setTimeout(() => {
      saveTagSortOrders(currentState.allTags);
    }, 800);
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
    var $td = state.el.closest('td');
    $td.removeClass('relative overflow-visible').addClass('cursor-pointer');

    const html = state.tags.map(tag => {
      return `
        <span
          class="tag-chip inline-flex items-center px-2 py-1 rounded text-white text-sm mr-1 ${tag.tagColor}"
        >
          ${tag.tagName}
        </span>
      `;
    }).join("");

    // mount には触らない（サイズも padding もいじらない）
    state.el.html(`
      <div class="sprout-tag-view flex items-center justify-center cursor-pointer min-h-[24px]">
        ${html || '<span class="tag-empty">&nbsp;</span>'}
      </div>
    `);

    $td
      .off('click.sproutTags')
      .on('click.sproutTags', function (e) {
        e.stopPropagation();

        if (state.mode !== 'edit') {
            state.mode = 'edit';
            renderEdit(state);
        }
      });
  }

  /**
   * 編集モード描画
   */
  function renderEdit(state) {
    state.mode = 'edit';
    var $td = state.el.closest('td');
    $td.removeClass('cursor-pointer').off('click.sproutTags')
      .addClass('relative overflow-visible');

    state.el.html(`
      <div class="sprout-tag-edit w-full h-full px-2 py-1">
        <div class="flex flex-wrap gap-1 mb-1">
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
  function renderDropdown(state) {
    // 既存 dropdown があれば削除
    $('.sprout-tag-dropdown-portal').remove();

    // td の位置とサイズを取得
    const rect = state.el.closest('td')[0].getBoundingClientRect();

    const html = `
      <ul
        class="sprout-tag-dropdown-portal fixed bg-white dark:bg-gray-800 border shadow z-[9999]"
        data-item-id="${state.itemId}"
        style="
          left: ${rect.left}px;
          top: ${rect.bottom}px;
          width: ${rect.width}px;
        "
      >
      ${getSelectableTags(state).map((tag, index) => `
          <li
            class="sprout-tag-option group flex items-center gap-2 px-2 py-1 text-sm hover:bg-gray-100 cursor-pointer"
            data-tag-id="${tag.tagId}"
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
    // 既存の action popup を削除
    $('.sprout-tag-action-popup').remove();

    const dropdownRect = $('.sprout-tag-dropdown-portal')[0].getBoundingClientRect();
    const liRect = $li[0].getBoundingClientRect();

    const html = `
      <div
        class="sprout-tag-action-popup fixed bg-white dark:bg-gray-800 border shadow min-w-[120px] z-[10000]"
        data-tag-id="${tagId}"
        style="
          left: ${dropdownRect.right}px;
          top: ${liRect.top}px;
        "
      >
        <div class="flex flex-col gap-1 px-1 py-1">
          <div class="sprout-tag-action-item inline-flex self-center items-center justify-center cursor-pointer rounded
            px-4 py-2 text-sm bg-yellow-500 hover:bg-yellow-600 text-white leading-none" data-action="color">
            色変更
          </div>
          <div class="sprout-tag-action-item inline-flex self-center items-center justify-center cursor-pointer rounded
            px-4 py-2 text-sm bg-red-500 hover:bg-red-600 text-white leading-none" data-action="delete">
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
        console.log('色変更', tagId);
        // TODO: カラーパレット表示
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
                tagId: tagId,
                itemId: itemId
              }
            })
            .done(function() {
              // 操作ポップアップを閉じる
              $('.sprout-tag-action-popup').remove();
              // 再取得してリスト更新
              fetchAllTags(currentState);

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

      if (e.key !== 'Enter') return;
      if (e.isComposing) return; // IME変換Enterを無視

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
   * すべてのタグ取得
   */
  function fetchAllTags(state) {
    $.ajax({
      url: '/tags/all',
      method: 'GET'
    })
    .done(function (form) {
      state.allTags = form.tagList;
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
    if (!currentState) return;
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

    currentState.mode = 'view';
    renderView(currentState);

    if (isSame) {
      return;
    }

    // ===== ここから「本当に変更がある場合のみ保存」 =====
    currentState.finishing = true;

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
      traditional: true, // 配列送信時に重要
      data: {
        tagIds: tagIds
      }
    });
  }

  /**
   * タグソート保存
   */
  function saveTagSortOrders(tags) {
    return $.ajax({
      url: '/tags/sort',
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({
        tagList: tags.map(tag => ({
          tagId: tag.tagId,
          tagSortOrder: tag.tagSortOrder
        }))
      })
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

      // 編集中なら再描画しない
      if (currentState.finishing) return;

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

  return {
    mount: mount
  };

})();
