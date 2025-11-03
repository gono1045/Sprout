
$(function() {
    let availableTags = [];

    const style = document.createElement('style');
    style.textContent = `
      input[list]::-webkit-calendar-picker-indicator {
        display: none !important;
        -webkit-appearance: none;
      }
    `;
    document.head.appendChild(style);

  // 作成日を日本時間で取得
  function getToday() {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
  }

  // flatpickr 初期化
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

  // モーダル操作

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

  // 締切セルの日付フォーマット
  function formatDeadlineCell(dateStr) {
  if (!dateStr) return "(期限未設定)";
  const date = new Date(dateStr.replace(/-/g, '/'));
  if (isNaN(date)) return "(期限未設定)";

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const diffDays = Math.floor((date - today) / (1000 * 60 * 60 * 24));
  // ★ タイトル等と同じTailwindクラスを日付部分に付与
  const dateClass = "text-base text-gray-900 dark:text-blue-300 font-normal"; // 例: タイトルと同じ
  const formatted = `<span class="${dateClass}">${date.getFullYear()}/${String(date.getMonth() + 1).padStart(2, '0')}/${String(date.getDate()).padStart(2, '0')}</span>`;

  let colorClass = "";
  let dayText = "";

  if (diffDays < 0) {
    colorClass = "text-red-500 font-bold";
    dayText = `(期限切れ ${Math.abs(diffDays)}日)`;
  } else if (diffDays === 0) {
    colorClass = "text-orange-500 font-bold";
    dayText = `(今日まで)`;
  } else if (diffDays === 1) {
    colorClass = "text-orange-500";
    dayText = `(あと1日)`;
  } else {
    colorClass = "text-gray-700 dark:text-gray-300";
    dayText = `(あと${diffDays}日)`;
  }

  // ★ 日付部分と残日数部分を分けて返す
  return `
    <div class="flex flex-col items-center">
      ${formatted}
      <span class="text-xs ${colorClass} mt-1">${dayText}</span>
    </div>
  `;
}

  // 行生成関数
  function createRow(item) {
    // タグを複数に分割して div に変換
    const tagHtml = (item.tags || []).map(t =>
        `<div class="tag-item px-2 py-1 rounded ${t.color || 'bg-blue-400'} text-white" data-tag="${t.name}">${t.name}</div>`
    ).join('');

    const deadlineHtml = formatDeadlineCell(item.deadline || "");
    const doneClass = item.done ? "opacity-50 line-through" : "";
    if(item.tag && !availableTags.some(t => t.name === item.tag)) {
        availableTags.push({ tagId: null, name: item.tag, color: 'bg-blue-400' });
    }

    return $(`
      <tr class="border-r border-b border-blue-300 hover:bg-gray-50 dark:border-gray-700 dark:hover:bg-gray-700 ${doneClass}" data-id="${item.id}">
        <td class="w-80 border-r py-2 px-4 text-center title flex items-center" data-field="title">
          <span class="editable flex-1 text-center overflow-hidden whitespace-nowrap text-ellipsis">${item.title}</span>
          <input type="text" class="hidden-input hidden absolute inset-0 w-full h-full px-2 border border-blue-400 rounded text-center bg-white dark:bg-gray-700 text-gray-900 dark:text-white z-20">
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

        <!-- ✅ ここから独立した別セルに -->
        <td class="editable relative w-60 border-r border-b border-blue-300 text-center tag" data-field="tag">
          <span class="cell-text block w-full py-2 px-4">
            <div class="tag-list flex flex-wrap gap-1 justify-center">
              ${(item.tags || []).map(tag =>
                `<div class="tag-item px-2 py-1 rounded ${tag.color || 'bg-blue-400'} text-white" data-tag="${tag.name}">${tag.name}</div>`
              ).join("")}
            </div>
          </span>
          <div class="hidden edit-area w-full h-full">
            <div class="tag-edit-wrapper flex items-center flex-wrap gap-1 w-full h-full border border-blue-400 rounded px-2 py-1 min-h-[2.5rem]">
              <div class="current-tags flex flex-wrap items-center gap-1 w-full h-full">
                ${(item.tags || []).map(tag =>
                  `<div class="tag-item px-2 py-1 rounded bg-blue-400 text-white flex items-center gap-1" data-tag="${tag.name}">
                     ${tag.name}
                   </div>`
                ).join("")}
              </div>
              <input type="text"
                     class="tag-input flex-grow min-w-[4rem] bg-transparent outline-none w-full h-full"
                     placeholder="タグを追加または選択"
                     list="tag-list-datalist">
            </div>
            <div class="tag-suggestions hidden absolute top-full left-0 right-0 mt-1 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded shadow max-h-40 overflow-y-auto z-10" id="tagSuggestions"></div>
          </div>
        </td>

        <!-- 残りのセル -->
        <td class="border-r py-2 px-4 text-center status" data-field="status">${item.status}</td>
        <td class="border-r py-2 px-4 text-center priority" data-field="priority">${item.priority}</td>
        <td class="border-r py-2 px-4 text-gray-600 dark:text-white createdAt" data-field="createdAt">${item.createdAt}</td>
        <td class="border-r py-2 px-4 text-center deadline" data-field="deadline">
          <div class="cell-wrapper text-xs text-gray-600 mt-1 dark:text-gray-400
            <span class="cell-text text-center">${deadlineHtml}</span>
          </div>
        </td>
        <td class="editable detail w-80 border-r py-2 px-4 text-left" data-field="detail">${item.detail}</td>
        <td class="border-r py-2 px-4">
          <form action="/delete/${item.id}" method="post" style="display:inline;">
            <button type="submit" class="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700">削除</button>
          </form>
        </td>
      </tr>
    `);
    }

/* ToDo 新規行作成ボタン実装時にcreateRowと差し替える
  function createNewRow(data = {}) {
      const row = $("#rowTemplate").clone();
      row.removeAttr("id").removeClass("hidden");

      // data-fieldごとの初期値マップ
      const fieldInitMap = {
          title: "",
          tag: "",
          status: "",
          priority: "",
          createdAt: getToday(),
          deadline: "",
          detail: ""
      };

      row.find("[data-field]").each(function() {
          const field = $(this).data("field");
          const value = fieldInitMap[field] ?? ""; // マップに無ければ空
          $(this).html(value);
      });

      return row;
  } */

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

            //data-listを更新
            if(newItem.tag) addTagToDataList(newItem.tag);

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

  /**
   * 共通インライン編集機能
   * tdSelector: 編集対象のセルセレクタ
   * fieldName: 保存時のフィールド名
   * options: セル固有の追加設定（必須、モーダルボタン、select かどうか など）
   */
  function enableInlineEdit(tdSelector, fieldName, options = {}) {
    $(document).on("click", tdSelector, function() {
      const td = $(this);
      const span = td.find(".cell-text");
      const row = td.closest("tr");
      const icon = options.modalBtnSelector ? td.find(options.modalBtnSelector) : null;
      const originalValue = span.text().trim();

      //既に編集中ならスキップ
      if (td.find(".edit-input, .edit-select, .edit-textarea").length > 0) return;
      // 編集用editorを生成
      const editHTML = createEditCellHTMLMap(fieldName, originalValue);
      const $editor = $(editHTML)
        .addClass("absolute left-0 top-0 w-full h-full pl-2 pr-2 box-border");

      td.addClass("relative");
      td.append($editor);
      span.addClass("hidden");
      if (icon) icon.hide();

      //title,detailはカーソルを末尾にセット
      if ($editor.is("input, textarea")) {
        const el = $editor[0]
        $editor.focus();
        const length = el.value.length;
        el.setSelectionRange(length, length);
      } else {
        $editor.focus();
      }

      // deadlineの設定
      if (fieldName === "deadline") {
        const fp = flatpickr($editor[0], {
          dateFormat: "Y-m-d",
          locale: "ja",
          allowInput: true,
          defaultDate: originalValue || null,
          closeOnSelect: false,
          clickOpens: true,
          onValueUpdate: function(selectedDates, dateStr) {
            if (dateStr) {
            // yyyy/MM/dd形式に変換
              const formatted = dateStr.replace(/-/g, "/").replace(/^(\d{4})\/(\d{1,2})\/(\d{1,2})$/,
              (m, y, mth, d) => `${y}/${mth.padStart(2, '0')}/${d.padStart(2, '0')}`);
              $editor.val(formatted);
              // 日付のみをセルに表示
              span.html(`<span class="text-base text-blue-300 font-normal">${formatted}</span>`);
              span.removeClass("hidden");
            }
          },
          onClose: function(selectedDates, dateStr) {
            $editor.trigger("blur");
              $editor.remove();
              span.html(formatDeadlineCell(dateStr));
              span.removeClass("hidden");
          }
        });
        fp.open();
      }

      //保存処理
      function save() {
        const newValue = $editor.val().trim();

        // 必須チェック
        if (options.required && !newValue) {
          alert("必須項目です");
          $editor.remove();
          span.removeClass("hidden");
          if (icon) icon.show();
          return;
        }

        // 値が変わらなければ終了
        if (newValue === originalValue) {
          $editor.remove();
          span.removeClass("hidden");
          if (icon) icon.show();
          return;
        }

        // Ajaxで保存
        $.ajax({
          url: "/update",
          type: "POST",
          data: { id: row.data("id"), [fieldName]: newValue },
          dataType: "json",
          success: function(updatedItem) {
            console.log("✅ 更新結果:", updatedItem);
            if (fieldName === "deadline") {
                const newDeadline = updatedItem.deadline || newValue || null;
                span.html(newDeadline ? formatDeadlineCell(newDeadline) : "(期限未設定)");
            } else if (fieldName === "title" || fieldName === "detail" || fieldName === "status" || fieldName === "priority") {
                span.text(newValue);
            }
          },
          error: function(xhr) {
            alert("更新に失敗しました");
            span.text(originalValue);
          },
          complete: function() {
            if (fieldName === "deadline") {
                const updatedDate = $editor.val().trim();
                if (updatedDate) {
                    span.html(formatDeadlineCell(updatedDate));
                }
            }
            $editor.remove();
            span.removeClass("hidden");
            if (icon) icon.show();
          }
        });
      }

      let isComposing = false;
      $editor.on("compositionstart", () => { isComposing = true; });
      $editor.on("compositionend", () => { isComposing = false; });

      $editor.off("keydown").on("keydown", function(e) {
        // detailセル専用：Shift+Enterで改行
        if (fieldName === "detail" && e.key === "Enter" && e.shiftKey) {
          e.preventDefault();
          const textarea = this;
          const start = textarea.selectionStart;
          const end = textarea.selectionEnd;
          const value = textarea.value;
          textarea.value = value.substring(0, start) + "\n" + value.substring(end);
          textarea.selectionStart = textarea.selectionEnd = start + 1;
          return; // 改行だけ行って終了（保存処理に進まない）
        }

        // Enterキーで保存（Shift押下時はスキップされる）
        if (e.key === "Enter" && !e.shiftKey && !isComposing) {
          e.preventDefault();
          save();
        }

        // Escキーでキャンセル
        if (e.key === "Escape") {
          e.preventDefault();
          // cancel logic here...
        }
      });

      $editor.off("blur").on("blur", function(e) {
        e.preventDefault();
        save();
      });
    });
  }

  // 各セルに有効化
  enableInlineEdit("td[data-field='title']", "title", {
    modalBtnSelector: ".openEditModalBtn",
    required: true,
  });
  enableInlineEdit("td[data-field='status']", "status");
  enableInlineEdit("td[data-field='priority']", "priority");
  enableInlineEdit("td[data-field='deadline']", "deadline");
  enableInlineEdit("td[data-field='detail']", "detail");

  /**
   * 編集用HTML生成マップ
   */
  function createEditCellHTMLMap(field, value = "") {
      const fieldMap = {
          title: val => `<input type="text" class="edit-input w-full text-center px-2 py-1 box-border border border-transparent focus:border-blue-500 rounded focus:outline-none" value="${val}">`,
          status: val => `
              <select class="edit-select w-full text-center px-2 py-1 box-border border border-transparent focus:border-blue-500 focus:outline-none rounded">
                  <option value="">選択してください</option>
                  <option value="未着手" ${val==="未着手"?"selected":""}>未着手</option>
                  <option value="進行中" ${val==="進行中"?"selected":""}>進行中</option>
                  <option value="完了" ${val==="完了"?"selected":""}>完了</option>
              </select>
          `,
          priority: val => `
              <select class="edit-select w-full text-center px-2 py-1 box-border border border-transparent focus:border-blue-500 focus:outline-none rounded">
                  <option value="">選択してください</option>
                  <option value="高" ${val==="高"?"selected":""}>高</option>
                  <option value="中" ${val==="中"?"selected":""}>中</option>
                  <option value="低" ${val==="低"?"selected":""}>低</option>
              </select>
          `,
          deadline: val => `<input type="text" class="edit-date w-full text-center px-2 py-1 box-border border border-transparent focus:border-blue-500 rounded focus:outline-none text-blue-300" value="${val || ''}" placeholder="">`,
          detail: val => `<textarea class="edit-textarea w-full focus:border-blue-500 rounded focus:outline-none">${val}</textarea>`
      };

      return (fieldMap[field] || (v => `<input type="text" class="edit-input w-full" value="${v}">`))(value);
  }

  //ここ以降は未リファイン
  // 編集モード終了で反映
  function saveTags(td) {
      const currentTags = td.data("tags") || [];

      const payload = {
          itemId: td.closest("tr").data("id"),
          selectedTagIds: currentTags.filter(t => t.tagId).map(t => t.tagId),   // 既存タグID
          newTags: currentTags.filter(t => !t.tagId).map(t => ({ name: t.name, color: t.color })) // 新規タグ
      };

      $.ajax({
          url: "/saveTags",
          type: "POST",
          contentType: "application/json; charset=utf-8",
          data: JSON.stringify(payload),
          success: function(res) {
              console.log("タグ更新成功:", res);

              // 新規タグにサーバ側で生成された tagId を付与
              if (res.newTags && res.newTags.length) {
                  res.newTags.forEach(newTag => {
                      const tag = currentTags.find(t => t.name === newTag.name && !t.tagId);
                      if (tag) tag.tagId = newTag.tagId;
                  });
                  td.data("tags", currentTags); // 最新のタグ配列を再保存
              }

              // hidden input にも反映
              updateHiddenInputFromTd(td);
          },
          error: function(xhr) {
              console.error("タグ更新失敗:", xhr.responseText);
              alert("タグ更新に失敗しました");
          }
      });
  }

  function updateHiddenInputFromTd(td) {
      const currentTags = td.data("tags") || [];
      const selectedIds = currentTags
          .filter(t => t.tagId) // null を除外
          .map(t => t.tagId);
      $('#tags-hidden').val(selectedIds.join(','));
  }

  //即時反映
  // タグ削除
  function deleteTag(tagId) {
      $.post("/deleteTag", { tagId: tagId })
       .done(() => console.log("タグ削除成功"))
       .fail(xhr => console.error("タグ削除失敗", xhr.responseText));
  }

  // タグ更新（背景色など）
  function updateTag(tag) {
      $.ajax({
          url: "/updateTag",
          type: "POST",
          data: tag, // name, tagColor, tagId 等
          success: function(updatedTag) {
              console.log("タグ更新成功:", updatedTag);
          },
          error: function(xhr) {
              console.error("タグ更新失敗:", xhr.responseText);
          }
      });
  }

  //タグセル用の編集モード切り替え
  $(document).on("keydown", ".tag-input", function(e) {
    if (e.key === "Enter") {
        e.preventDefault();
        const input = $(this);
        const value = input.val().trim();
        if (!value) return;

        // 既存のタグリストに追加
        const td = input.closest("td");
        const tagList = td.find(".tag-list");

        const newTag = $(`
          <div class="tag-item inline-flex max-w-max gap-2 p-1 rounded bg-blue-400 text-white" data-tag="${value}">
            <div class="tag-sort-wrapper gap-1">
              <button class="tag-sort-up cursor-pointer border rounded bg-blue-400 text-white w-6 h-6">↑</button>
              <button class="tag-sort-down cursor-pointer border rounded bg-blue-400 text-white w-6 h-6">↓</button>
            </div>
            <span class="tag-name">${value}</span>
            <button class="tag-settings p-1 hover:bg-gray-200 rounded">⚙️</button>
          </div>
        `);
        tagList.prepend(newTag);

        //availableTagsにも追加
        if (!availableTags.some(t => t.name === value)) {
            availableTags.push(value);
            $("#tag-list-datalist").append(`<option value="${value}">`);
        }

        input.val(""); //入力欄リセット
    }
  });

  // タグセルのインライン編集
  function enableTagInlineEdit(tdSelector) {
    $(document).on("click", tdSelector, function() {
      const td = $(this);
      const display = td.find(".cell-text");
      const editArea = td.find(".edit-area");
      const input = editArea.find(".tag-input");
      const suggestions = td.find(".tag-suggestions");

      // 現在のタグ配列
      let currentTags = td.data("tags") || [];

      // チップ描画
      function renderCurrentTags() {
        const container = td.find(".current-tags");
        container.empty();

        currentTags.forEach(tag => {
          const chip = $(`
            <div class="tag-chip ${tag.color} text-white px-2 py-1 rounded flex items-center gap-1" data-tag="${tag.name}">
              <span class="tag-name">${tag.name}</span>
            </div>
          `);

          chip.on("mousedown", function(e) {
            e.preventDefault();
          });

          //チップクリックで選択解除
          chip.on("click", function() {
            const tagNameToRemove = tag.name;
            currentTags = currentTags.filter(t => t.name !== tag.name);
            renderCurrentTags();
          });

          container.append(chip);
        });

        // placeholder 表示制御
        if (currentTags.length === 0) {
          input.attr("placeholder", "タグを追加または選択");
        } else {
          input.removeAttr("placeholder");
        }
      }

      // 初期表示
      renderCurrentTags();

      // 編集モード表示
      display.addClass("hidden");
      editArea.removeClass("hidden");
      input.focus();

      // 候補リスト描画
      suggestions.removeClass("hidden").html(
        availableTags.map(tag =>
          `<div class="tag-suggestion flex items-center justify-between px-2 py-1 hover:bg-gray-200 cursor-pointer">
              <div class="tag-sort-wrapper gap-1">
                <button class="tag-sort-up cursor-pointer border rounded bg-blue-400 text-white w-6 h-6">↑</button>
                <button class="tag-sort-down cursor-pointer border rounded bg-blue-400 text-white w-6 h-6">↓</button>
              </div>
              <span class="tag-name text-center px-2 py-1 rounded ${tag.color} text-white">${tag.name}</span>
              <button class="tag-settings p-1 hover:bg-gray-300 rounded ml-2">⚙️</button>
          </div>`
        ).join("")
      );

      //タグのアクションリスト
      let actionList = $(".tag-action-list");
      if (actionList.length === 0) {
        actionList = $(`
            <div class="tag-action-list absolute hidden bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded p-2 z-50">
              <button class="delete-tag-btn text-white bg-red-400 px-2 py-1 rounded mb-1 w-full">削除</button>
              <button class="change-bg-btn text-white bg-green-400 px-2 py-1 rounded w-full">色変更</button>
            </div>
          `);
          $("body").append(actionList);
      }

      let currentTag = null;

      //歯車アイコンクリック
      $(document).on("click", ".tag-settings", function(e) {
        e.stopPropagation();
        const tagItem = $(this).closest(".tag-suggestion");

        if (currentTag && currentTag.is(tagItem)) {
            //同じアイコンクリック
            actionList.toggleClass("hidden");
        } else {
            //別のアイコンクリック
            actionList.removeClass("hidden");
            //現在のタグの右に表示
            const offset = tagItem.offset();
            actionList.css({
                top: offset.top,
                left: offset.left + tagItem.outerWidth() + 4 //右に少し余白
            }).removeClass("hidden");
        }
        currentTag = tagItem;
      });

      //背景クリックで閉じる
      $(document).on("click", function(e) {
        if ($(e.target).closest(".tag-suggestions, .tag-settings, .tag-name").length > 0) {
            return; // タグやメニューをクリックしたときは閉じない
        }

        if (!currentTag || !currentTag.length) return;

        actionList.addClass("hidden");
        // タグセル（td）を探して入力欄にフォーカス
        console.log("currentTag : ", currentTag);
        const td = currentTag.closest("td[data-field='tag']");
        const input = td.find(".tag-input");

        // 編集モードが閉じていない場合 → 単純にフォーカス
        if (!td.find(".edit-area").hasClass("hidden")) {
            input.focus();
        } else {
            // 閉じていたら再度編集モードを開く
            td.find(".cell-text").addClass("hidden");
            td.find(".edit-area").removeClass("hidden");
            input.focus();
        }

        currentTag = null;
      });

      // ESCキーで閉じる
      $(document).on("keydown", function(e) {
        if (e.key === "Escape") {
          if (currentTag) {
            actionList.addClass("hidden");

            // タグセル（td）を探して入力欄にフォーカス
            const td = currentTag.closest("td[data-field='tag']");
            const input = td.find(".tag-input");

            // 編集モードが閉じていない場合 → 単純にフォーカス
            if (!td.find(".edit-area").hasClass("hidden")) {
              input.focus();
            } else {
              // 閉じていたら再度編集モードを開く
              td.find(".cell-text").addClass("hidden");
              td.find(".edit-area").removeClass("hidden");
              input.focus();
            }

            currentTag = null;
          }
        }
      });


      // 候補クリック（タグ名のみ）
      td.find(".tag-suggestions").off("click").on("click", ".tag-name", function(e) {
        e.stopPropagation();
        e.preventDefault();

        currentTag =$(this);

        const tagName = $(this).text().trim();
        const tagColor = $(this).attr("class").match(/bg-\S+/)?.[0] || "bg-blue-400";

        if (currentTags.some(t => t.name === tagName)) {
          // ✅ 選択済みなら解除
          currentTags = currentTags.filter(t => t.name !== tagName);
        } else {
          // ✅ 未選択なら追加
          currentTags.push({ name: tagName, color: tagColor });
        }

        renderCurrentTags();
        input.focus(); // 選択後も編集モード維持
      });

      // Enterキー押下でタグ追加
      input.off("keydown").on("keydown", function(e) {
        if (e.key === "Enter") {
          e.preventDefault();
          const value = $(this).val().trim();

          if (value) {
            addTagToCurrentCell(value, { isNew: true });
            $(this).val("");
          } else {
            exitEdit();
          }
        }
      });

      input.off("blur").on("blur", function() {
          setTimeout(() => {
              if (!editArea.find(":focus").length && !suggestions.is(":hover")) {
                  exitEdit();
              }
          }, 0);
      });

      input.off("keydown.exit").on("keydown.exit", function(e) {
        if (e.key === "Escape") {
            e.preventDefault();
            exitEdit();
        }
      });

      // タグを追加する関数
      function addTagToCurrentCell(value, options = {}) {
          if (!value || !value.trim()) return; // 空文字は無視

          const td = $(".edit-area:visible").closest("td[data-field='tag']");
          let currentTags = td.data("tags") || [];
          const tagColor = "bg-blue-400";

          if (!currentTags.some(t => t.name === value)) {
              currentTags.push({ tagId: null, name: value, color: tagColor });
              td.data("tags", currentTags);
              renderCurrentTags(td);
          }

          if (options.isNew && value.trim() && !availableTags.some(t => t.name === value)) {
              const newTag = { tagId: null, name: value, color: tagColor };
              availableTags.push(newTag);
              $("#tag-list-datalist").append(`<option value="${value}">`);
          }
      }

      // 編集終了時に表示へ反映
      function exitEdit() {
          currentTags = currentTags.filter(t => t.name && t.name.trim() !== ""); // 空タグ削除
          td.data("tags", currentTags);
          saveTags(td);

          const html = currentTags.map(tag =>
            `<div class="tag-item px-2 py-1 rounded ${tag.color} text-white" data-tag="${tag.name}">${tag.name}</div>`
          ).join("");

          display.html(`<div class="tag-list flex flex-wrap gap-1 justify-center">${html}</div>`);
          editArea.addClass("hidden");
          display.removeClass("hidden");

          input.val("");
      }
    });
  }

  // 選択されたタグIDを保持する配列
  let selectedTags = [];

  // タグを追加（例：リスト選択やクリックで呼び出す）
  function addTag(tagId) {
    if ($.inArray(tagId, selectedTags) === -1) {
      selectedTags.push(tagId);
      updateHiddenInput();
    }
  }

  // タグを削除（チップの×ボタンなどで呼び出す）
  function removeTag(tagId) {
    selectedTags = $.grep(selectedTags, function(id) {
      return id !== tagId;
    });
    updateHiddenInput();
  }

  // hidden input の値を更新
  function updateHiddenInput() {
    $('#tags-hidden').val(selectedTags.join(','));
  }

  //上ボタンクリック
  $(document).on("click", ".tag-sort-up", function(e) {
    e.stopPropagation();
    const tagItem = $(this).closest(".tag-item");
    const parent = tagItem.parent();
    const prev = tagItem.prev(".tag-item");
    if (prev.length) {
        tagItem.insertBefore(prev);
    } else {
        //最初なら最後に移動
        tagItem.appendTo(parent);
    }
  });

  //下ボタンクリック
  $(document).on("click", ".tag-sort-down", function(e) {
    e.stopPropagation();
    const tagItem = $(this).closest(".tag-item");
    const parent = tagItem.parent();
    const next = tagItem.next(".tag-item");
    if (next.length) {
        tagItem.insertAfter(next);
    } else {
        //最初なら最後に移動
        tagItem.appendTo(parent);
    }
  });

  // 有効化
  enableTagInlineEdit("td[data-field='tag']");

  //日付セルの対応

});