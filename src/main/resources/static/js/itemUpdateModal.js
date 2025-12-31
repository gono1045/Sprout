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
  function init($modalEl) {
    console.log('itemUpdateModal.init called', $modalEl?.[0]);

    if (!$modalEl || !$modalEl.length) {
      console.error('itemUpdateModal.init: $modalEl が不正です');
      return;
    }

    _this.modalEl = $modalEl;

    _this.form = sprout.util.getId(SCREEN_ID, 'sproutItemListForm');
    _this.deadline = sprout.util.getId(SCREEN_ID, 'deadline');
    _this.deadlineIcon = sprout.util.getId(SCREEN_ID, 'deadlineIcon');

    initDatePicker();
  }

  /**
   * 日付ピッカー初期化
   */
  function initDatePicker() {

    const $deadline = _this.modalEl.find(_this.deadline);
    const $deadlineIcon = _this.modalEl.find(_this.deadlineIcon);

    if ($deadline.length && !$deadline[0]._flatpickr) {
      flatpickr($deadline[0], {
        locale: 'ja',
        dateFormat: 'Y/m/d',
        allowInput: true
      });
    }

    // アイコンクリックでopen
    $deadlineIcon.off('click.modal')
      .on('click.modal', function () {
        $deadline.focus();
      });
  }

  return {
    init: init
  };

})();
