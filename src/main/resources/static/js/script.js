
$(function() {
	//モーダルを開く
	$("#openModalBtn").on("click", function() {
		$("#modal").removeClass("hidden");
	});
	
	//モーダルを閉じる(×ボタン)
	$("#closeModalBtn").on("click", function() {
		$("#modal").addClass("hidden");
	});

	//モーダルを閉じる（キャンセルボタン）
	$("#cancelBtn").on("click", function() {
		$("#modal").addClass("hidden");
	});
	
	//背景クリックでも閉じる
	$("#modal").on("click", function(e) {
		if ($(e.target).is("#modal")) {
			$("#modal").addClass("hidden");
		}
	});
});