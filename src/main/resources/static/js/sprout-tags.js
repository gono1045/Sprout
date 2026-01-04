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
      allTags: []
    };

    init(state);
  }

  /**
   * 初期化
   */
  function init(state) {
    if (!state.itemId) {
      console.warn("itemId is required for sprout.tags");
      return;
    }

    fetchItemTags(state.itemId)
      .done(function(tags) {
        state.tags = tags;
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
   * 表示モード描画
   */
  function renderView(state) {
    $('.sprout-tag-dropdown-portal').remove();
    var $td = state.el.closest('td');
    $td.removeClass('relative overflow-visible').addClass('cursor-pointer');

    const html = state.tags.map(tag => {
      return `
        <span
          class="tag-chip inline-flex px-2 py-1 rounded text-white text-xs mr-1 ${tag.tagColor}"
        >
          ${tag.tagName}
        </span>
      `;
    }).join("");

    // mount には触らない（サイズも padding もいじらない）
    state.el.html(`
      <div class="sprout-tag-view flex items-center cursor-pointer min-h-[24px]">
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
    var $td = state.el.closest('td');
    $td.removeClass('cursor-pointer').off('click.sproutTags')
      .addClass('relative overflow-visible');

    state.el.html(`
      <div class="sprout-tag-edit w-full h-full">
        <div class="sprout-tag-select cursor-pointer w-full h-full px-2 py-1 flex text-sm">
          タグを選択…
        </div>
      </div>
    `);

    fetchAllTags(state);
  }

  /**
   * タグリストから選択
   */
  $(document)
    .off('click.sproutTagOption')
    .on('click.sproutTagOption', '.sprout-tag-option', function (e) {
      e.stopPropagation();

      const tagId = $(this).data('tag-id');
      const itemId = $(this).closest('.sprout-tag-dropdown-portal').data('item-id');

      saveItemTags(itemId, [tagId])
        .done(() => {
          $('.sprout-tag-dropdown-portal').remove();
          const $cell = $(`[data-item-id="${itemId}"]`);
          if ($cell.length) {
            sprout.tags.mount({
                el: $cell,
                itemId: itemId
            });
          }
        })
        .fail(() => {
          console.error('タグ保存失敗');
        });
    });

    /**
     * フォーカスアウトで編集終了
     */
    $(document).on('click.sproutTagsClose', function () {
      $('.sprout-tag-dropdown-portal').remove();
      $('.sprout-tag-edit').each(function () {
        const $el = $(this).closest('[data-item-id]');
        if ($el.length) {
          sprout.tags.mount({
            el: $el,
            itemId: $el.data('item-id')
          });
        }
      });
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
        ${state.allTags.map((tag, index) => `
          <li
            class="sprout-tag-option group flex items-center gap-2 px-2 py-1 text-sm hover:bg-gray-100 cursor-pointer"
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

            <div class="text-gray-400 select-none">⚙</div>
          </li>
        `).join('')}
      </ul>
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
      const index = Number($li.data('index'));
      const isUp = $(this).hasClass('sprout-tag-move-up');

      moveTag(currentState, index, isUp ? 'up' : 'down');

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
  function moveTag(state, index, direction) {
    const list = [...state.allTags];
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

  return {
    mount: mount
  };

})();
