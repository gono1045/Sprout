package com.example.sproutTest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.sprout.SproutAppApplication;

@Disabled("DB接続が必要なため、ローカルDB起動時のみ実行可能")
@SpringBootTest(classes = SproutAppApplication.class)
class ScheduleAppApplicationTests {

  @Test
  void contextLoads() {
  }
}
