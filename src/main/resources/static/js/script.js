
$(function() {

    //作成日を日本時間で取得
    function getToday() {
        const d = new Date();
        return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
    }

    //flatpickr初期化
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
    const createdAtPicker =initDatePicker("#createdAt", "createdAtIcon", getToday());

    //モーダルを閉じる
    function closeModal() {
        $("#modal").addClass("hidden");
    }

    $("#closeModalBtn, #cancelBtn").on("click", closeModal);
    $("#modal").on("click", e => { if ($(e.target).is("#modal")) closeModal(); });

        //新規登録モーダルを開く
    $("#openNewModalBtn").on("click", function() {
        $("#createdAt").val(getToday());
        $("#deadline").val("");

        //フォームをリセット
        $("#sproutForm")[0].reset();
        $("#sproutId").val("");
        $("#sproutForm").attr("action", "/add");
        $("#submitBtn").text("追加");

        $("#modal").removeClass("hidden");
    });

    //背景クリックでも閉じる
    $("#modal").on("click", function(e) {
        if ($(e.target).is("#modal")) {
            $("#modal").addClass("hidden");
        }
    });

    //編集用モーダル
    $(document).on("click", ".openEditModalBtn", function() {
        const itemId = $(this).data("id");
        const row = $(this).closest("tr");

        $("#sproutId").val(itemId);

        //各値を取得してセット
        $("#title").val(row.find(".title").text().trim());
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

    $("#sproutForm").on("submit", function(e) {
        e.preventDefault(); //通常送信を止める

        const formData = $(this).serialize();

        $.post($(this).attr("action"), formData, function() {
            //更新成功時

            const id = $("#sproutId").val();
            const row = $(`button[data-id='${id}']`).closest("tr");

            //各セルを更新
            const fields = ["title", "tag", "status", "priority", "createdAt", "deadline", "detail"];
            fields.forEach(field => {
                if (field === "deadline") {
                    row.find(".deadline").html(formatDeadlineCell($(`#${field}`).val()));
                    } else {
                        row.find(`.${field}`).text($(`#${field}`).val());
                    }
            });

            //モーダルを閉じる
            $("#modal").addClass("hidden");
        });
    });

    function formatDeadlineCell(val) {
        if (!val) return "(期限未設定)";
        const deadlineDate = new Date(val);
        const today = new Date();
        const diffDays = Math.ceil((deadlineDate - today) / (1000 * 60 * 60 *24));
        return `${val} <div class="text-xs text-gray-600 mt-1 dark:text-gray-400">(あと ${diffDays}日)</div>`;
    }

    //フォーカスアウトで更新
    $(document).on("blur change", ".editable", function() {
        const cell = $(this);
        const row = cell.closest("tr");
        const id = row.find(".openEditModalBtn").data("id");

        const updateData = { id };
            row.find(".editable").each(function() {
                const field = $(this).data("field");
                if ($(this).is("select")) {
                    updateData[field] = $(this).val();
                }else {
                    updateData[field] =$(this).text().trim();
                }
            });

        //保存先を反映
        $.post("/update", updateData, function() {
            console.log("更新完了");
            //残日数を再計算して表示更新
            const deadlineVal = updateData.deadline;
            row.find(".deadline").html(formatDeadlineCell(deadlineVal));
        });
    });

    $(document).on("click", "td.title", function() {
        let td = $(this);

        //既にinputがある場合は何もしない
        if (td.find("textarea").length > 0) return;

        let original = td.text().trim();
        td.empty();

        let textarea = $(`<textarea class="border rounded px-1 w-full resize-none"></textarea>`)
        .val(original)
        .appendTo(td)
        .focus();

        //Shift+Enterで改行、Enterで保存
        textarea.on("keydown", function(e) {
            if (e.key === "Enter") {
                if (e.shiftKey) {
                    //改行
                    let val = $(this).val();
                    let pos = this.selectionStart;
                    $(this).val(val.slice(0, pos) + "\n" + val.slice(pos));
                    this.selectionStart = this.selectionEnd = pos + 1;
                    e.preventDefault();
                } else {
                    //保存
                    e.preventDefault();
                    $(this).blur();
                }
            }
        });

        td.empty().append(textarea);
        textarea.focus();

        //フォーカスアウトしたら保存
        textarea.on("blur", function() {
            let newValue = $(this).val().trim();
            td.text(newValue);
            //ここでAjax保存処理を追加予定
        });
    });
});