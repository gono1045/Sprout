package com.example.schedule.controller;

import com.example.schedule.model.ScheduleItem;
import com.example.schedule.ScheduleRepository;
import com.example.schedule.dto.ScheduleViewDto;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Controller
public class ScheduleController {

	private final ScheduleRepository repository;

	// 新規タグ候補リストを保持
	private List<String> tagCandidates = new ArrayList<>();

	// コンストラクタインジェクション
	public ScheduleController(ScheduleRepository repository) {
		this.repository = repository;
	}

	@GetMapping("/")
	public String index(
			@RequestParam(name = "keyword", required = false) String keyword,
			//@RequestParam(name = "tag", required = false) String tag,
			//@RequestParam(name = "showCompleted", required = false, defaultValue = "true") boolean showCompleted,
			Model model) {

		//DBから全件取得
		List<ScheduleItem> scheduleList = repository.findAllByOrderByCreatedAtAsc();

		// タグ指定があれば絞り込み
		//		if (tag != null && !tag.isEmpty()) {
		//			items = items.stream()
		//					.filter(item -> tag.equals(item.getTag()))
		//					.toList();
		//		}

		if (keyword != null && !keyword.isEmpty()) {
			scheduleList = scheduleList.stream()
					.filter(item -> item.getTitle().contains(keyword))
					.toList();
		}

		// デバッグ用：検索結果出力
		System.out.println("=== Search Result ===");
		scheduleList.forEach(System.out::println);
		System.out.println("===================");

		//DTOに変換
		List<ScheduleViewDto> displayList = scheduleList.stream()
				.map(item -> new ScheduleViewDto(
						item.getId(),
						item.getTitle(),
						item.getTag(),
						item.getStatus(),
						item.getPriority(),
						item.getCreatedAt(),
						item.getDeadline(),
						item.getDetail(),
						item.getDone()))
				.toList();

		model.addAttribute("schedules", displayList);
		model.addAttribute("scheduleItem", new ScheduleItem());
		model.addAttribute("tagCandidates", tagCandidates);
		model.addAttribute("keyword", keyword);
		//		model.addAttribute("tag", tag);
		//		model.addAttribute("selectedTag", tag);
		model.addAttribute("showCompleted", true);

		return "index";
	}

	@PostMapping("/add")
	public String addSchedule(@Valid @ModelAttribute ScheduleItem scheduleItem, BindingResult result) {
		if (!result.hasErrors()) {
			// 作成日が null なら今日に設定
			if (scheduleItem.getCreatedAt() == null) {
				scheduleItem.setCreatedAt(LocalDate.now());
			}

			// タグ候補リストに追加
			if (scheduleItem.getTag() != null && !scheduleItem.getTag().isEmpty()
					&& !tagCandidates.contains(scheduleItem.getTag())) {
				tagCandidates.add(scheduleItem.getTag());
			}

			ScheduleItem savedItem = repository.saveAndFlush(scheduleItem);
			System.out.println("Saved ID: " + savedItem.getId());

		} else {
			System.out.println("Validation errors: " + result.getAllErrors());
		}
		return "redirect:/";

	}

	@PostMapping("/delete/{id}")
	public String deleteSchedule(@PathVariable Long id) {
		repository.deleteById(id);
		return "redirect:/";
	}

	@GetMapping("/edit/{id}")
	public String editSchedule(@PathVariable Long id, Model model) {
		ScheduleItem item = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("無効なID：" + id));
		model.addAttribute("scheduleItem", item);

		// 編集画面にもタグ候補リストを渡す
		model.addAttribute("tagCandidates", tagCandidates);

		return "edit";
	}

	@PostMapping("/update")
	public String updateSchedule(@ModelAttribute ScheduleItem scheduleItem) {
		repository.save(scheduleItem);
		return "redirect:/";
	}

	@PostMapping("/toggle/{id}")
	public String toggleDone(@PathVariable Long id) {
		ScheduleItem item = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("無効なID：" + id));
		item.setDone(!item.getDone());
		repository.save(item);
		return "redirect:/";
	}

	@PostMapping("/inline-update")
	public String inlineUpdate(
			@RequestParam Long id,
			@RequestParam(required = false) String title,
			@RequestParam(required = false) String deadline) {
		ScheduleItem item = repository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("無効ID" + id));

		if (title != null) {
			item.setTitle(title);
		}

		if (deadline != null && !deadline.isEmpty()) {
			//datetime-local形式で送られてくるので変換が必要
			item.setDeadline(LocalDate.parse(deadline));
		}

		repository.save(item);
		return "redirect:/";
	}
}
