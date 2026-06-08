package com.example.sprout.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.sprout.model.SproutPasswordResetToken;

/**
 * パスワードリセットトークンリポジトリ
 * JPA Repository のため dao パッケージではなく repository パッケージに配置
 * （@MapperScan("com.example.sprout.dao") の走査対象から除外するため）
 */
public interface SproutPasswordResetTokenRepository
    extends JpaRepository<SproutPasswordResetToken, Long> {

  /**
   * トークン文字列でレコードを取得
   */
  Optional<SproutPasswordResetToken> findByToken(String token);

  /**
   * 指定ユーザーの未使用トークンを全て無効化（再発行時に古いトークンを無効化）
   */
  @Modifying
  @Transactional
  @Query("UPDATE SproutPasswordResetToken t SET t.used = true WHERE t.userId = :userId AND t.used = false")
  void invalidateAllByUserId(@Param("userId") Long userId);
}
