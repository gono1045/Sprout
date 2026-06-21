/**
 * itemUpdateModal.js
 * タスク作成・更新モーダル制御
 */

var itemUpdateModal = (function () {

  const SCREEN_ID = 'itemUpdateModal';
  let _this = {};

  /**
   * 初期化
   * @param {jQuery} $modalEl - openModal から渡されるモーダル要素
   */
  function init($modalEl, options) {
    options = options || {};
    console.log('itemUpdateModal.init called', $modalEl?.[0]);

    if (!$modalEl || !$modalEl.length) {
      console.error('itemUpdateModal.init: $modalEl が不正です');
      return;
    }

    _this.modalEl = $modalEl;
    _this.formId = sprout.util.getId(SCREEN_ID, 'sproutItemListForm');
    _this.deadlineId = sprout.util.getId(SCREEN_ID, 'deadline');
    _this.deadlineIconId = sprout.util.getId(SCREEN_ID, 'deadlineIcon');
    _this.submitBtnId = sprout.util.getId(SCREEN_ID, 'submitBtn');
    _this.modalFlgId = sprout.util.getId(SCREEN_ID, 'modalFlg');
    _this.deleteBtnId = sprout.util.getId(SCREEN_ID, 'deleteBtn');
    _this.itemId = sprout.util.getId(SCREEN_ID, 'id');
    _this.tagId = sprout.util.getId(SCREEN_ID, 'tagId');

    initDatePicker();

    // 追加・更新ボタンクリック
    $(_this.submitBtnId).off('click.modal').on('click.modal', function(e) {
      e.preventDefault();
      var modalFlg = $(_this.modalFlgId).val();

      // 登録
      if (modalFlg == 0) {
        sprout.util.sendForm({
          url: "/task/new",
          form: _this.formId,
          callBack: function(res) {
            const itemId = res.id;
            const tagIds = _this.modalEl.find('.sprout-tag-mount').data('sproutTagsState')?.tags.map(t => t.tagId) ?? [];

            if (tagIds.length) {
              $.post(`/items/${itemId}/tags`, { tagIds: tagIds })
                .done(closeModalCallBack)
                .fail(() => alert('タグ登録に失敗しました'));
            } else {
              closeModalCallBack();
            }
          }
        });
      }

      // 更新
      if (modalFlg == 1) {
        sprout.util.sendForm({
            url: "/task/update",
            form: _this.formId,
            callBack: closeModalCallBack
        })
      }
    });

    function closeModalCallBack() {
        $modalEl.remove();
        $(document).trigger('sprout:task-updated');
    };

    // 削除ボタンクリック
    $(_this.deleteBtnId).off('click.modal').on('click.modal', function() {
      var id = $(_this.itemId).val();

      sprout.message.confirmExec({
        messageId: 'WARN001',
        title: '削除確認',
        onOk: function() {
          sprout.util.sendForm({
            url: '/task/delete',
            data: {id: id},
            callBack: function() {
              closeModalCallBack();
              sprout.message.toast({
                message: '削除が完了しました',
                type: 'success'
              });
            }
          });
        }
      });
    });

    const $tagEl = $modalEl.find('.sprout-tag-mount');
    const itemId = $(_this.itemId).val();

    if ($tagEl.length) {
      $tagEl.each(function() {
        if ($(this).data('mounted')) return;

        sprout.tags.mount({
          el: this,
          itemId: itemId || null,
          presetTags: (itemId ? [] : (options.presetTags || []))
        });

        $(this).data('mounted', true);
      });
    }
  }

  /**
   * 日付ピッカー初期化
   */
  function initDatePicker() {

    if ($(_this.deadlineId).length && !$(_this.deadlineId)[0]._flatpickr) {
      flatpickr($(_this.deadlineId)[0], {
        locale: 'ja',
        dateFormat: 'Y/m/d',
        allowInput: true
      });
    }

    // アイコンクリックでopen
    $(_this.deadlineIconId).off('click.modal')
      .on('click.modal', function () {
        $(_this.deadlineId).focus();
      });
  }

  return {
    init: init
  };

})();
