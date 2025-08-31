package com.example.sprout;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sprout.model.SproutItem;

import java.util.List;

public interface SproutRepository extends JpaRepository<SproutItem, Long> {

	List<SproutItem> findAllByOrderByCreatedAtAsc();
}
