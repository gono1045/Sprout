package com.example.sprout.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
   * 静的リソースが見つからない（favicon.ico等のブラウザ自動リクエストを含む）
   * 想定内のため警告ログのみとし、404を返す
   */
  @ExceptionHandler(NoResourceFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public void handleNoResourceFound(NoResourceFoundException ex) {
    // ログ出力不要（favicon.ico等のノイズになるため）
  }

  /**
   * 想定外エラー
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String handleException(Exception ex, Model model) {

    log.error("想定外のエラーが発生しました", ex);

    model.addAttribute("errorMessage", "システムエラーが発生しました");

    return "error/500";
  }
}
