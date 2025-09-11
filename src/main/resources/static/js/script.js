$(function() {

  /*** ===== 共通ユーティリティ ===== ***/

  // 作成日を日本時間で取得
  function getToday() {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
  }

  /*** ===== flatpickr 初期化 ===== ***/
  function initDatePicker(selector, iconId, defaultDate = null) {
    const picker = flatpickr(selector, {
      dateFormat: "Y-m-d",
      locale: "ja",
      defaultDate: defaultDate,
      allowInput: true
    });
    $(`#${iconId}`).on("click", () => picker.open());
    return picker;
  }

  const deadlinePicker = initDatePicker("#deadline", "deadlineIcon");
  const createdAtPicker = initDatePicker("#createdAt", "createdAtIcon", getToday());

  /*** ===== モーダル操作 ===== ***/

  function closeModal() {
    $("#modal").addClass("hidden");
  }

  $("#closeModalBtn, #cancelBtn, #modal").on("click", function(e) {
    if (e.target === this) closeModal();
  });

  $("#openNewModalBtn").on("click", function() {
    $("#sproutForm")[0].reset();
    $("#sproutId").val("");
    $("#createdAt").val(getToday());
    $("#deadline").val("");
    $("#sproutForm").attr("action", "/add");
    $("#submitBtn").text("追加");
    $("#modal").removeClass("hidden");

    $(document).on("keydown", function(e) {
        if (e.key === "Escape" && !$("#modal").hasClass("hidden")) {
            closeModal();
        }
    });
  });

  /*** ===== 行生成関数 ===== ***/
  function formatDeadlineCell(val) {
    if (!val) return "(期限未設定)";
    const diffDays = Math.ceil((new Date(val) - new Date())/(1000*60*60*24));
    return `${val} <div class="text-xs text-gray-600 mt-1 dark:text-gray-400">(あと ${diffDays}日)</div>`;
  }

  function createRow(item) {
    const deadlineHtml = item.deadline ? formatDeadlineCell(item.deadline) : "(期限未設定)";
    const doneClass = item.done ? "opacity-50 line-through" : "";

    return $(`
      <tr class="border-r border-b border-blue-300 hover:bg-gray-50 dark:border-gray-700 dark:hover:bg-gray-700 ${doneClass}">
        <td class="w-80 border-r py-2 px-4 text-center title flex items-center" data-field="title">
          <span class="editable flex-1 text-center overflow-hidden whitespace-nowrap text-ellipsis">${item.title}</span>
          <button data-id="${item.id}" class="openEditModalBtn ml-2 shrink-0">編集</button>
        </td>
        <td class="border-r py-2 px-4 text-center tag" data-field="tag">${item.tag}</td>
        <td class="border-r py-2 px-4 text-center status" data-field="status">${item.status}</td>
        <td class="border-r py-2 px-4 text-center priority" data-field="priority">${item.priority}</td>
        <td class="border-r py-2 px-4 text-gray-600 dark:text-white createdAt" data-field="createdAt">${item.createdAt}</td>
        <td class="border-r py-2 px-4 text-center deadline" data-field="deadline">${deadlineHtml}</td>
        <td class="editable detail w-80 border-r py-2 px-4 text-left" data-field="detail">${item.detail}</td>
        <td class="border-r py-2 px-4">
          <form action="/delete/${item.id}" method="post" style="display:inline;">
            <button type="submit" class="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700">削除</button>
          </form>
        </td>
      </tr>
    `);
  }

  /*** ===== 編集モーダル ===== ***/
  $(document).on("click", ".openEditModalBtn", function() {
    const row = $(this).closest("tr");
    const itemId = $(this).data("id");

    $("#sproutId").val(itemId);
    $("#title").val(row.find(".title span.editable").text().trim());
    $("#tag").val(row.find(".tag").text().trim());
    $("#status").val(row.find(".status").text().trim());
    $("#priority").val(row.find(".priority").text().trim());
    $("#createdAt").val(row.find(".createdAt").text().trim().replace(/\//g, "-"));
    $("#deadline").val(row.find(".deadline").text().trim().replace(/\//g, "-"));
    $("#detail").val(row.find(".detail").text().trim());

    $("#submitBtn").text("更新");
    $("#sproutForm").attr("action", "/update");
    $("#modal").removeClass("hidden");
  });

  /*** ===== 新規登録・更新 Ajax ===== ***/
  $("#sproutForm").on("submit", function(e) {
    e.preventDefault();
    const formData = $(this).serialize();
    const actionUrl = $(this).attr("action");

    $.post(actionUrl, formData, function(newItem) {
      if (!newItem) return;
      const newRow = createRow(newItem);

      if ($("#sproutId").val()) {
        const row = $(`button[data-id='${newItem.id}']`).closest("tr");
        row.replaceWith(newRow);
      } else {
        $("table tbody").prepend(newRow);
      }

      closeModal();
      $("#sproutForm")[0].reset();
    });
  });

  /*** ===== インライン編集（タイトル） ===== ***/
  $(document).on("click", "td.title span.editable", function() {
    const span = $(this);
    const td = span.closest("td");
    const original = span.text().trim();

    if (td.find("textarea").length > 0) return;

    const textarea = $(`<textarea class="border rounded px-1 w-full resize-none mr-2"></textarea>`).val(original);
    span.hide();
    td.prepend(textarea);
    textarea.focus();

    textarea.on("blur", function() {
      const newVal = $(this).val().trim();
      span.text(newVal).show();
      $(this).remove();

      // Ajaxで保存
      const row = td.closest("tr");
      const id = row.find(".openEditModalBtn").data("id");
      $.post("/update", { id, title: newVal });
    });

    textarea.on("keydown", function(e) {
      if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        $(this).blur();
      }
    });
  });

  /*** ===== 他セル即時反映 ===== ***/
  $(document).on("blur change", ".editable:not(.title)", function() {
    const cell = $(this);
    const row = cell.closest("tr");
    const id = row.find(".openEditModalBtn").data("id");
    const field = cell.data("field");
    const value = cell.is("select") ? cell.val() : cell.text().trim();

    $.post("/update", { id, [field]: value }, function() {
      if (field === "deadline") {
        row.find(".deadline").html(formatDeadlineCell(value));
      }
    });
  });

});