package com.example.sprout.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sprout.service.SproutPasswordResetService;

/**
 * パスワードリセットコントローラー
 */
@Controller
@RequestMapping("/password-reset")
public class SproutPasswordResetController {

  @Autowired
  private SproutPasswordResetService passwordResetService;

  // =========================================================
  // メールアドレス入力画面
  // =========================================================

  /**
   * リセットリクエスト画面 表示
   */
  @GetMapping("/request")
  public String showRequest(Model model) {
    model.addAttribute("resetRequestForm", new ResetRequestForm());
    return "password-reset/request";
  }

  /**
   * リセットメール送信
   */
  @PostMapping("/request")
  public String sendResetMail(
      @Valid @ModelAttribute("resetRequestForm") ResetRequestForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttrs) {

    if (bindingResult.hasErrors()) {
      return "password-reset/request";
    }

    // ユーザー存在有無にかかわらず同じ画面に遷移（セキュリティ上）
    passwordResetService.sendResetMail(form.getEmail());

    return "redirect:/password-reset/sent";
  }

  /**
   * 送信完了画面
   */
  @GetMapping("/sent")
  public String showSent() {
    return "password-reset/sent";
  }

  // =========================================================
  // パスワード再設定画面
  // =========================================================

  /**
   * パスワード再設定画面 表示
   * トークンが無効な場合はエラー画面へ
   */
  @GetMapping("/confirm")
  public String showConfirm(@RequestParam("token") String token, Model model) {
    if (!passwordResetService.isValidToken(token)) {
      model.addAttribute("errorMessage", "このリンクは無効または期限切れです。再度リセットをお申し込みください。");
      return "password-reset/invalid";
    }
    model.addAttribute("token", token);
    model.addAttribute("resetConfirmForm", new ResetConfirmForm());
    return "password-reset/confirm";
  }

  /**
   * パスワード再設定処理
   */
  @PostMapping("/confirm")
  public String resetPassword(
      @RequestParam("token") String token,
      @Valid @ModelAttribute("resetConfirmForm") ResetConfirmForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttrs,
      Model model) {

    // 新パスワード一致チェック
    if (!form.getNewPassword().equals(form.getConfirmPassword())) {
      bindingResult.rejectValue("confirmPassword", null, "パスワードが一致しません");
    }

    if (bindingResult.hasErrors()) {
      model.addAttribute("token", token);
      return "password-reset/confirm";
    }

    boolean success = passwordResetService.resetPassword(token, form.getNewPassword());

    if (!success) {
      model.addAttribute("errorMessage", "このリンクは無効または期限切れです。再度リセットをお申し込みください。");
      return "password-reset/invalid";
    }

    redirectAttrs.addFlashAttribute("resetSuccess", true);
    return "redirect:/login";
  }

  // =========================================================
  // 内部フォームクラス
  // =========================================================

  /** メールアドレス入力フォーム */
  public static class ResetRequestForm {
    @NotBlank(message = "メールアドレスを入力してください")
    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
  }

  /** パスワード再設定フォーム */
  public static class ResetConfirmForm {
    @NotBlank(message = "新しいパスワードを入力してください")
    private String newPassword;

    @NotBlank(message = "確認用パスワードを入力してください")
    private String confirmPassword;

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
  }
}
