package com.example.schedule.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ScheduleViewDto {

	private Long id;
	private String title;
	private String tag;
	private String status;
	private String priority;
	private LocalDate createdAt;
	private LocalDate deadline;
	private String detail;
	private Boolean done;
	private String deadlineFormatted;

	public ScheduleViewDto(Long id, String title, String tag, String status, String priority,
			LocalDate createdAt, LocalDate deadline, String detail, Boolean done) {
		this.id = id;
		this.title = title;
		this.tag = tag;
		this.status = status;
		this.priority = priority;
		this.createdAt = createdAt;
		this.deadline = deadline;
		this.detail = detail;
		this.done = done;
		this.deadlineFormatted = (deadline != null)
				? deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
				: "";
	}

	// Getterのみ
	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getTag() {
		return tag;
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

	public String getDetail() {
		return detail;
	}

	public Boolean getDone() {
		return done;
	}

	public String getDeadlineFormatted() {
		return deadlineFormatted;
	}
}
