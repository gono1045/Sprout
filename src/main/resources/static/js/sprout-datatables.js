// sprout-datatables.js

const SproutDataTables = {

  renderers: {

    LINK: function (data, type, row) {
      if (type !== 'display') return data;
      return `<a href="/item/${row.id}" class="text-blue-500 underline">${data}</a>`;
    },

    DATE: function (data) {
      if (!data) return '';
      return dayjs(data).format('YYYY/MM/DD');
    },

    LABEL: function (data) {
      return data ?? '';
    },

    BUTTON: function () {
      return `<button class="btn-measure">⏱</button>`;
    },

    ACTION: function () {
      return `<button class="action-menu">⋯</button>`;
    }

  },

  /**
   * inputType から render を取得
   */
  getRender: function (inputType) {
    return this.renderers[inputType] ?? null;
  }
};
