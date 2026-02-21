package com.example.sprout.controller;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 権限エラー（他人のデータ操作など）
   */
  @ExceptionHandler(AccessDeniedException.class)
  public String handleAccessDenied(AccessDeniedException ex, Model model) {

    model.addAttribute("errorMessage", "この操作を行う権限がありません");

    return "error/403";
  }

  /**
   * ログイン情報取得失敗
   */
  @ExceptionHandler(IllegalStateException.class)
  public String handleIllegalState(IllegalStateException ex) {

    return "redirect:/login";
  }

  /**
   * 想定外エラー
   */
  @ExceptionHandler(Exception.class)
  public String handleException(Exception ex, Model model) {

    model.addAttribute("errorMessage", "システムエラーが発生しました");

    return "error/500";
  }
}
