
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

    $(document).on("keydown", function(e) {
       if (e.key === "Escape" && !$("#modal").hasClass("hidden")) {
           closeModal();
       }
   });

     $("#openNewModalBtn").on("click", function() {
    $("#sproutForm")[0].reset();
    $("#sproutId").val("");
    $("#createdAt").val(getToday());
    $("#deadline").val("");
    $("#sproutForm").attr("action", "/add");
    $("#submitBtn").text("追加");
    $("#modal").removeClass("hidden");

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
          <button
              type="button"
              class="openEditModalBtn ml-2 shrink-0"
              data-id="${item.id}"
              data-title="${item.title}"
              data-tag="${item.tag}"
              data-status="${item.status}"
              data-priority="${item.priority}"
              data-createdat="${item.createdAt}"
              data-deadline="${item.deadline || ''}"
              data-detail="${item.detail}">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
                 stroke="currentColor" class="w-5 h-5 text-gray-600 dark:text-blue-300 hover:text-blue-500">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M15.232 5.232l3.536 3.536M16.5 4.5l3 3L7.5 19.5H4.5v-3L16.5 4.5z" />
            </svg>
          </button>
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

  //編集モーダルの設定
  $(function() {
    $("#sproutForm").on("submit", function(e) {
        e.preventDefault();

        const form = $("#sproutForm");
        const formData = form.serialize(); //フォーム内の値をまとめる
        const actionUrl = form.attr("action"); //th:actionのURLを取得

        $.ajax({
            type: "POST",
            url: actionUrl,
            data: formData,
            dataType: "json" //jsonとして受け取る
        })
        .done(function(newItem) {
            if (!newItem) return; //バリデーションエラー時は何もしない

            const newRow = createRow(newItem);

            if (actionUrl.endsWith("/add")) {
                $("table tbody").prepend(newRow);
            } else if (actionUrl.endsWith("/update")) {
                const id = $("#sproutId").val();
                $(`button[data-id='${id}']`).closest("tr").replaceWith(newRow);
            }

            closeModal();
            $("#sproutForm")[0].reset();
        })
        .fail(function(xhr, status, error) {
            console.error("Ajaxエラー：", status, error);
            console.error("レスポンス：", xhr.responseText);
        })
    });
  });

    $(document).on("click", ".openEditModalBtn", function() {
    const btn =$(this);
    const row = btn.closest("tr");

    //フォームに値をセット
    $("#sproutId").val(btn.data("id"));
    $("#title").val(btn.data("title"));
    $("#tag").val(btn.data("tag"));
    $("#status").val(btn.data("status"));
    $("#priority").val(btn.data("priority"));
    $("#createdAt").val(btn.data("createdAt"));
    $("#deadline").val(btn.data("deadline"));
    $("#detail").val(btn.data("detail"));

    //ボタンとフォームを編集用に切り替え
    $("#submitBtn").text("更新");
    $("#sproutForm").attr("action", "/update");

    $("#modal").removeClass("hidden"); //モーダル表示
  });

  //インライン編集
  $(document).on("dblclick", "td[data-field='title'] .cell-text", function() {
    const span = $(this);
    const td = span.closest("td");
    const row = td.closest("tr");
    const icon = td.find(".openEditModalBtn");
    const originalText = span.text().trim();

    //すでに編集中なら何もしない
    if (td.find("input").length) return;

    span.hide();
    icon.hide();

      //inputを生成
      const input = $(`<input type="text"
                       class="inline-edit absolute top-0 left-0 w-full h-full p-0 m-0 border-blue-400 border box-border text-center"
                       value="${originalText}">`);

      td.find(".cell-wrapper").append(input);
      input.focus();
      const valLength = input.val().length;
      input[0].setSelectionRange(valLength, valLength);

      //Enter、フォーカスアウトで保存
      function save() {
        const newValue = input.val().trim();
        const id = row.find(".openEditModalBtn").data("id");

         if (!newValue) {
             input.focus();
             return; //空文字は保存しない
         }

           //Ajaxで更新
           $.ajax({
               url: "/update", //編集用のコントローラー
               type: "POST",
               data: { id, title: newValue },
               success: function(updateItem) {
                   span.text(updateItem.title).show(); //更新後の値で置き換え
                   input.remove();
                   icon.show();
               },
               error: function() {
                   span.show();
                   input.remove();
                   icon.show();
               }
           });
         }
         input.on("keydown", function(e) {
          if (e.key === "Enter") {
              e.preventDefault();
              save();
          }
         });

         input.on("blur", save);
  });



  //日付セルの対応
  $(document).on("dblclick", "td[data-field='deadline']", function() {
    const td = $(this);
    const originalDate = td.text().trim() || '';

    if (td.find("input").length) return;

    const input = $(`<input type="text" class="inline-edit w-full h-full p-1 text-sm border rounded box-border" value="${originalDate}">`);
    td.html(input);

    flatpickr(input[0], {
        dateFormat: "Y-m-d",
        local: "ja",
        allowInput: true,
        defaultDate: originalDate,
        onClose: function(selectDates, dateStr) {
            saveInlineEdit(input, td.closest("tr").find(".openEditModalBtn").data("id"), "deadline");
        }
    }).open();

    input.focus();
  });
});