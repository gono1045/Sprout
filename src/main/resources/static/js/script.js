
$(function() {

    const availableTags = ["仕事", "勉強", "趣味"];

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

  // 行生成関数
  function formatDeadlineCell(val) {
    if (!val) return "(期限未設定)";
    const diffDays = Math.ceil((new Date(val) - new Date())/(1000*60*60*24));
    return `${val} <div class="text-xs text-gray-600 mt-1 dark:text-gray-400">(あと ${diffDays}日)</div>`;
  }

  function createRow(item) {
    // タグを複数に分割して div に変換
    const tagHtml = item.tag
        ? item.tag.split(',').map(t => `<div class="tag-item px-2 py-1 rounded bg-blue-400 text-white">${t.trim()}</div>`).join('')
        : '';

    const deadlineHtml = item.deadline ? formatDeadlineCell(item.deadline) : "(期限未設定)";
    const doneClass = item.done ? "opacity-50 line-through" : "";
    if(item.tag && !availableTags.includes(item.tag)) availableTags.push(item.tag);

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
        <td class="border-r py-2 px-4 text-center tag relative" data-field="tag"
            data-tags='${JSON.stringify(item.tag ? item.tag.split(",").map(t => ({name: t.trim(), color:"bg-blue-400"})) : [])}'>
        <span class="cell-text block w-full text-center">
          <div class="tag-list flex flex-wrap gap-1 justify-center">
            ${(item.tag || "").split(",").map(t =>
              `<div class="tag-item px-2 py-1 rounded bg-blue-400 text-white" data-tag="${t.trim()}">${t.trim()}</div>`
            ).join("")}
          </div>
        </span>
        <div class="edit-area hidden absolute inset-0 w-full h-full z-20">
            <div class="current-tags flex flex-wrap gap-1 mb-1"></div>
            <input type="text"
                   class="tag-input hidden absolute inset-0 w-full h-full px-2 border border-blue-400 rounded text-center bg-white dark:bg-gray-700 text-gray-900 dark:text-white z-20"
                   list="tag-list">
                   <div class="tag-suggestions hidden absolute top-full left-0 right-0 mt-1 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded shadow max-h-40 overflow-y-auto z-10"></div>        </div>
        </td>
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

  //インライン編集セルごとの設定
  $(function() {

    /**
     * セルをクリックしたら hidden 切り替えで編集モードにする共通関数
     * @param tdSelector : 編集対象のtdセレクタ
     * @param fieldName  : Ajax送信時のフィールド名
     */
    function enableInlineEdit(tdSelector, fieldName, inputClass = ".hidden-input") {
      $(document).on("click", tdSelector + " .cell-text", function(e) {
        const td = $(this).closest("td");
        const span = $(this);                     // 表示用のspan
        const input = td.find(".hidden-input");   // 編集用input
        const icon = td.find(".openEditModalBtn"); // 編集モード中は非表示

        // ---------- 編集モードに切り替え ----------
        span.hide();          // 表示用テキストを隠す
        icon.hide();          // アイコンを隠す
        input.val(span.text()) // 現在の値を input にセット
             .removeClass("hidden")
             .focus();

        // ---------- キーボード操作 ----------
        input.off("keydown").on("keydown", function(ev) {
          if (ev.key === "Enter") save();   // Enterで保存
          else if (ev.key === "Escape") cancel(); // Escでキャンセル
        });

        // ---------- フォーカスアウトでも保存 ----------
        input.off("blur").on("blur", save);

        // ---------- 保存処理 ----------
        function save() {
          const value = input.val();
          const row = td.closest("tr");
          const itemId = row.data("id");

          if (value) {
            span.text(value).show();
            if (fieldName === "tag" && !availableTags.includes(value)) {
                availableTags.push(value);
                console.log("新しいタグの追加：", value);
            }
          } else {
            span.text("").show();
          }

          const payload = { id: itemId };
           if (value !== "") {
             payload[fieldName] = value;
           }

           $.ajax({
             url: "/update",
             type: "POST",
             data: payload,
             dataType: "json",   // ← これ必須
             success: function(updatedItem) {
               span.text(updatedItem[fieldName] || "");
             },
             error: function(xhr) {
               console.error("更新失敗:", xhr.status, xhr.responseText);
               alert("更新に失敗しました");
             },
             complete: function() {
               exitEdit();
             }
          });
        }

        // キャンセル処理
        function cancel() {
          exitEdit();
        }

        // 編集終了時の共通処理
        function exitEdit() {
          input.addClass("hidden"); // 編集用 input を非表示
          span.show();              // 表示用 span を再表示
          td.find(".openEditModalBtn").show();
          icon.show();              // アイコンを戻す
        }
      });
    }

    // セルに適用
    enableInlineEdit("td[data-field='title']", "title");
  });

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
        if (!availableTags.includes(value)) {
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
              <span class="tag-name text-center px-2 py-1 rounded bg-red-400 text-white">${tag}</span>
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
      $(document).on("click", function() {
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
        const tagColor = "bg-blue-400"; // 新規タグは青

        if (!currentTags.some(t => t.name === value)) {
          currentTags.push({ name: value, color: tagColor });
          renderCurrentTags();
        }

        if (options.isNew && !availableTags.includes(value)) {
          availableTags.push(value);
          $("#tag-list-datalist").append(`<option value="${value}">`);
        }
      }

      // 編集終了時に表示へ反映
      function exitEdit() {
        const html = currentTags.map(tag =>
          `<div class="tag-item px-2 py-1 rounded ${tag.color} text-white" data-tag="${tag.name}">${tag.name}</div>`
        ).join("");

        display.html(`<div class="tag-list flex flex-wrap gap-1 justify-center">${html}</div>`);
        editArea.addClass("hidden");
        display.removeClass("hidden");

        // ✅ タグ配列を td に保存
        td.data("tags", currentTags);

        input.val("");
      }
    });
  }

  //上ボタンクリック
  $(document).on("click", ".tag-sort-up", function() {
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
  $(document).on("click", ".tag-sort-down", function() {
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