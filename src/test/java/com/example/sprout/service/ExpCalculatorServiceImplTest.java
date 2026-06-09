package com.example.sprout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * {@link ExpCalculatorServiceImpl} の単体テスト。
 * <p>
 * Spring コンテキストを使わずに純粋な Java ロジックをテストする。
 * </p>
 */
class ExpCalculatorServiceImplTest {

  private final ExpCalculatorServiceImpl sut = new ExpCalculatorServiceImpl();

  // ===== calcExp =====

  @ParameterizedTest(name = "工数{0}分・達成感★{1} → {2}EXP")
  @CsvSource({
      "60, 1,  60",   // ★1: ボーナス 0
      "60, 2,  65",   // ★2: ボーナス 5
      "60, 3,  70",   // ★3: ボーナス 10
      "60, 4,  80",   // ★4: ボーナス 20
      "60, 5,  90",   // ★5: ボーナス 30
      "0,  5,  30",   // 工数ゼロでも達成感ボーナスは付く
      "90, 3, 100",   // 90分 + 10 = 100EXP
  })
  @DisplayName("calcExp — EXP合計が正しく計算される")
  void calcExp_correct(int durationMin, int satisfaction, int expectedExp) {
    assertThat(sut.calcExp(durationMin, satisfaction)).isEqualTo(expectedExp);
  }

  @Test
  @DisplayName("calcExp — 工数が負の場合は例外")
  void calcExp_negativeDuration_throwsException() {
    assertThatThrownBy(() -> sut.calcExp(-1, 3))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("calcExp — satisfaction が範囲外の場合は例外")
  void calcExp_invalidSatisfaction_throwsException() {
    assertThatThrownBy(() -> sut.calcExp(30, 0))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> sut.calcExp(30, 6))
        .isInstanceOf(IllegalArgumentException.class);
  }

  // ===== calcLv =====

  @ParameterizedTest(name = "累計EXP {0} → Lv {1}")
  @CsvSource({
      "0,      1",    // Lv1  種 (0以上)
      "299,    1",    // Lv1  境界値
      "300,    2",    // Lv2  芽
      "999,    2",    // Lv2  境界値
      "1000,   3",    // Lv3  苗
      "2500,   4",    // Lv4  若木
      "5000,   5",    // Lv5  成木
      "9500,   6",    // Lv6  蕾
      "15600,  7",    // Lv7  開花
      "23000,  8",    // Lv8  結実
      "32000,  9",    // Lv9  豊穣
      "47000,  10",   // Lv10 大樹
      "99999,  10",   // Lv10以降は上限10に据え置き
  })
  @DisplayName("calcLv — 累計EXPに応じたレベルが返る")
  void calcLv_correct(int totalExp, int expectedLv) {
    assertThat(ExpCalculatorServiceImpl.calcLv(totalExp)).isEqualTo(expectedLv);
  }

  // ===== タグ均等分配ルール（ロジック検証のみ） =====

  @Test
  @DisplayName("タグ3件の場合、90EXP → 各30EXP（perTag計算）")
  void distributeExp_perTagCalc() {
    int totalExp = 90;
    int tagCount = 3;
    assertThat(totalExp / tagCount).isEqualTo(30);
  }

  @Test
  @DisplayName("タグ3件の場合、100EXP → 各33EXP（端数切り捨て）")
  void distributeExp_floorRemainder() {
    int totalExp = 100;
    int tagCount = 3;
    assertThat(totalExp / tagCount).isEqualTo(33);
  }
}
