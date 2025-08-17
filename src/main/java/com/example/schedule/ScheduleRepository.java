package com.example.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.schedule.model.ScheduleItem;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<ScheduleItem, Long> {

	List<ScheduleItem> findAllByOrderByCreatedAtAsc();
}
