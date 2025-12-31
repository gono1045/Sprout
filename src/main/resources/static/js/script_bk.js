
$(function() {
  // DataTables 初期化
  $(document).ready(function() {
    table = $('#sprout-table').DataTable({
      language: {
        url: "//cdn.datatables.net/plug-ins/1.13.6/i18n/ja.json"
      },
      // オプション
      paging: true,
      searching: true,
      ordering: false,
      order: [],
      info: true,
      pageLength: 5,
      lengthChange: false,
      dom: 'ltip',
      columnDefs: [
        { orderable: false, targets: [3, 4] } // タグと操作列はソート不可
      ]
    });
  });

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

  // 新規登録モーダル
  $("#createTaskModalBtn").on("click", function() {
    $("#sproutForm")[0].reset();
    $("#sproutId").val("");
    $("#createdAt").val(getToday());
    $("#deadline").val("");
    $("#sproutForm").attr("action", "/add");
    $("#submitBtn").text("追加");
    $("#modal").removeClass("hidden");
  });

  // 追加・更新ボタン押下
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

            if (actionUrl.endsWith("/add")) {
                table.row.add({
                  id: newItem.id,
                  title: newItem.title,
                  tag: formattedTagHtml(newItem.tags),
                  status: newItem.status,
                  priority: newItem.priority,
                  createdAt: newItem.createdAt,
                  deadline: formatDeadlineCell(newItem.deadline),
                  detail: newItem.detail
                }).draw(false);
                const trNode = table.row(':last').node();
                $(trNode)
                .find("td[data-field='tag']").data("tags",
                    newItem.tags.map(t => ({
                        tagId: t.tagId,
                        name: t.name,
                        color: t.tagColor
                    }))
                );
            } else if (actionUrl.endsWith("/update")) {
                const id = $("#sproutId").val();
                const row = table.row($(`tr[data-id="${id}"]`));
                row.data({
                      id: newItem.id,
                      title: newItem.title,
                      tag: formattedTagHtml(newItem.tags),
                      status: newItem.status,
                      priority: newItem.priority,
                      createdAt: newItem.createdAt,
                      deadline: formatDeadlineCell(newItem.deadline),
                      detail: newItem.detail
                    }).draw(false);
                    const trNode = table.row(':last').node();
                    $(trNode).find("td[data-field='tag']").data("tags",
                        newItem.tags.map(t => ({
                            tagId: t.tagId,
                            name: t.name,
                            color: t.tagColor
                        }))
                    );
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

  //編集モーダルの設定
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

  // タグセルのフォーマット
  function formattedTagHtml(tags) {
    if (!tags || tags.length === 0) {
      return `<span class="text-gray-400">タグなし</span>`;
    }

    return tags
      .filter(t => t && t.name)
      .map(t => `<span class="inline-block rounded px-2 py-1 bg-blue-400 text-white">${t.name}</span>`)
      .join(" ");
  }

  // 締切セルの日付フォーマット
  function formatDeadlineCell(dateStr) {
  if (!dateStr) return "(期限未設定)";
  const date = new Date(dateStr.replace(/-/g, '/'));
  if (isNaN(date)) return "(期限未設定)";

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const diffDays = Math.floor((date - today) / (1000 * 60 * 60 * 24));
  const dateClass = "text-base text-gray-900 dark:text-blue-300 font-normal";
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

  // タグ編集機能
  // 編集モード終了で反映
  function saveTags(td) {
      const currentTags = td.data("tags") || [];

      const selectedTagIds = currentTags
          .filter(t => t.tagId)
          .map(t => t.tagId);

      const tr = td.closest("tr");
      const itemId = tr.data("id");

      console.log("タグ保存 itemId:", itemId);
      console.log("タグ保存配列:", selectedTagIds);

      $.ajax({
          url: `items/${itemId}/tags/update`,
          type: "POST",
          contentType: "application/json; charset=utf-8",
          data: JSON.stringify(selectedTagIds),
          success: function(res) {
              console.log("タグ更新成功:", res);
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
      $.post("/tags/delete", { tagId: tagId })
       .done(() => console.log("タグ削除成功"))
       .fail(xhr => console.error("タグ削除失敗", xhr.responseText));
  }

  // タグ更新（背景色など）
  function updateTag(tag) {
      $.ajax({
          url: "/tags/update",
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
        saveTags(td);
    }
    input.off().on("blur", function() {
        saveTags(td);
    });
  });

  // タグセルのインライン編集
  function enableTagInlineEdit(tdSelector) {
    $(document).on("click", tdSelector, function() {
      const td = $(this);
      const display = td.find(".cell-text");
      const editArea = td.find(".edit-area");
      const input = editArea.find(".tag-input");
      const suggestions = td.find(".tag-suggestions");

      $.get("/tags/all", function(tags) {
        availableTags = tags; // サーバから最新のタグリストを取得

        // 候補リスト描画
        suggestions.removeClass("hidden").html(
          availableTags.map(tag =>
            `<div class="tag-suggestions flex items-center justify-between px-2 py-1 hover:bg-gray-200 cursor-pointer">
                <div class="tag-sort-wrapper gap-1">
                  <button class="tag-sort-up cursor-pointer border rounded bg-blue-400 text-white w-6 h-6">↑</button>
                  <button class="tag-sort-down cursor-pointer border rounded bg-blue-400 text-white w-6 h-6">↓</button>
                </div>
                <span class="tag-name text-center px-2 py-1 rounded ${tag.tagColor} text-white">${tag.tagName}</span>
                <button class="tag-settings p-1 hover:bg-gray-300 rounded ml-2">⚙️</button>
            </div>`
          ).join("")
        );
      });

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
        const tagItem = $(this).closest(".tag-suggestions");

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

          const existing = availableTags.find(t => t.name === value);
          const tagId = existing ? existing.tagId : null;
          const tagColor = existing ? existing.color : "bg-blue-400";

          if (!currentTags.some(t => t.name === value)) {
              currentTags.push({ tagId: null, name: value, color: tagColor });
              td.data("tags", currentTags);
              renderCurrentTags(td);
          }

          if (!tagId && options.isNew) {
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
          td.find(".cell-text").html(`<div class="tag-list flex flex-wrap gap-1 justify-center">${html}</div>`);
          td.find(".edit-area").addClass("hidden");
          td.find(".cell-text").removeClass("hidden");
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

  // タグを削除
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

});