package com.example.schedule.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class ScheduleItem {

	// --------------------
	// フィールド
	// --------------------

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 必須項目
	@NotNull(message = "タイトルは必須です")
	@Size(min = 1, max = 30, message = "タイトルは1文字以上30文字以内で入力してください")
	private String title;

	@Size(min = 0, max = 255, message = "内容は255文字以内で入力してください")
	private String detail;

	private String status;
	private String priority;

	private LocalDate createdAt;
	private LocalDate deadline;

	@Size(max = 30, message = "タグは30文字以内で入力してください")
	private String tag;

	// フラグ
	@NotNull
	private Boolean done = false;

	// --------------------
	// コンストラクタ
	// --------------------

	// Spring用の空コンストラクタ
	public ScheduleItem() {
	}

	// 必須項目コンストラクタ
	public ScheduleItem(String title) {
		this.title = title;
		this.createdAt = LocalDate.now();
	}

	// --------------------
	// ゲッター
	// --------------------

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDetail() {
		return detail;
	}

	public String getStatus() {
		return status;
	}

	public String getPriority() {
		return priority;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public LocalDate getDeadline() {
		return deadline;
	}

	public String getTag() {
		return tag;
	}

	public Boolean getDone() {
		return done;
	}

	// --------------------
	// セッター
	// --------------------

	public void setId(Long id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public void setDeadline(LocalDate deadline) {
		this.deadline = deadline;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public void setDone(Boolean done) {
		this.done = done;
	}

}
