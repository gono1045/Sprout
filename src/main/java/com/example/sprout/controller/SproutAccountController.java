package com.example.sprout.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sprout.form.SproutPasswordChangeForm;
import com.example.sprout.form.SproutProfileForm;
import com.example.sprout.form.SproutWithdrawForm;
import com.example.sprout.model.SproutUser;
import com.example.sprout.security.SproutUserDetails;
import com.example.sprout.service.SproutAccountService;
import com.example.sprout.service.SproutUserService;

/**
 * アカウント管理コントローラー
 */
@Controller
@RequestMapping("/account")
public class SproutAccountController {

  @Autowired
  private SproutAccountService sproutAccountService;

  @Autowired
  private SproutUserService sproutUserService;

  @Autowired
  private UserDetailsService userDetailsService;

  // =========================================================
  // プロフィール（ユーザー名変更）
  // =========================================================

  /**
   * プロフィール画面 表示
   */
  @GetMapping("/profile")
  public String showProfile(
      @AuthenticationPrincipal SproutUserDetails principal,
      Model model) {

    SproutProfileForm form = new SproutProfileForm();
    form.setLoginId(principal.getLoginId());
    model.addAttribute("sproutProfileForm", form);
    model.addAttribute("loginId", principal.getLoginId());
    return "account/profile";
  }

  /**
   * ユーザー名更新
   */
  @PostMapping("/profile")
  public String updateProfile(
      @Valid @ModelAttribute("sproutProfileForm") SproutProfileForm form,
      BindingResult bindingResult,
      @AuthenticationPrincipal SproutUserDetails principal,
      HttpServletRequest request,
      RedirectAttributes redirectAttrs,
      Model model) {

    model.addAttribute("loginId", principal.getLoginId());

    if (bindingResult.hasErrors()) {
      return "account/profile";
    }

    // 変更なし（更新は行わない）
    if (form.getLoginId().equals(principal.getLoginId())) {
      bindingResult.rejectValue("loginId", "noChange", "現在のユーザー名と同じです");
      return "account/profile";
    }

    // 重複チェック（自分以外）
    if (sproutUserService.existsByLoginId(form.getLoginId())) {
      bindingResult.rejectValue("loginId", null, "このユーザー名は既に使用されています");
      return "account/profile";
    }

    sproutAccountService.updateLoginId(principal.getUserId(), form.getLoginId());

    // SecurityContext を新しい loginId で更新
    var newDetails = userDetailsService.loadUserByUsername(form.getLoginId());
    var newAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
        newDetails, null, newDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(newAuth);
    request.getSession(true).setAttribute(
        org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
        SecurityContextHolder.getContext());

    redirectAttrs.addFlashAttribute("successMessage", "ユーザー名を更新しました");
    return "redirect:/account/profile";
  }

  // =========================================================
  // パスワード変更
  // =========================================================

  /**
   * パスワード変更画面 表示
   */
  @GetMapping("/password")
  public String showPasswordChange(
      @AuthenticationPrincipal SproutUserDetails principal,
      Model model) {

    model.addAttribute("sproutPasswordChangeForm", new SproutPasswordChangeForm());
    model.addAttribute("loginId", principal.getLoginId());
    return "account/password";
  }

  /**
   * パスワード変更処理
   */
  @PostMapping("/password")
  public String changePassword(
      @Valid @ModelAttribute("sproutPasswordChangeForm") SproutPasswordChangeForm form,
      BindingResult bindingResult,
      @AuthenticationPrincipal SproutUserDetails principal,
      RedirectAttributes redirectAttrs,
      Model model) {

    model.addAttribute("loginId", principal.getLoginId());

    // 新パスワード一致チェック
    if (!form.getNewPassword().equals(form.getConfirmPassword())) {
      bindingResult.rejectValue("confirmPassword", null, "新しいパスワードが一致しません");
    }

    if (bindingResult.hasErrors()) {
      return "account/password";
    }

    boolean success = sproutAccountService.updatePassword(
        principal.getUserId(),
        form.getCurrentPassword(),
        form.getNewPassword());

    if (!success) {
      bindingResult.rejectValue("currentPassword", "mismatch", "現在のパスワードが正しくありません");
      return "account/password";
    }

    redirectAttrs.addFlashAttribute("successMessage", "パスワードを変更しました");
    return "redirect:/account/password";
  }

  // =========================================================
  // アカウント設定（退会）
  // =========================================================

  /**
   * アカウント設定画面 表示
   */
  @GetMapping("/settings")
  public String showSettings(
      @AuthenticationPrincipal SproutUserDetails principal,
      Model model) {

    SproutUser user = sproutAccountService.findByUserId(principal.getUserId());
    model.addAttribute("sproutWithdrawForm", new SproutWithdrawForm());
    model.addAttribute("loginId", principal.getLoginId());
    model.addAttribute("email", user.getEmail());
    return "account/settings";
  }

  /**
   * 退会処理
   * パスワード照合後に論理削除してログアウト
   */
  @PostMapping("/withdraw")
  public String withdraw(
      @Valid @ModelAttribute("sproutWithdrawForm") SproutWithdrawForm form,
      BindingResult bindingResult,
      @AuthenticationPrincipal SproutUserDetails principal,
      HttpServletRequest request,
      RedirectAttributes redirectAttrs,
      Model model) {

    model.addAttribute("loginId", principal.getLoginId());

    if (bindingResult.hasErrors()) {
      SproutUser user = sproutAccountService.findByUserId(principal.getUserId());
      model.addAttribute("email", user.getEmail());
      model.addAttribute("showWithdrawModal", true);
      return "account/settings";
    }

    boolean success = sproutAccountService.deactivate(principal.getUserId(), form.getPassword());

    if (!success) {
      bindingResult.rejectValue("password", null, "パスワードが正しくありません");
      SproutUser user = sproutAccountService.findByUserId(principal.getUserId());
      model.addAttribute("email", user.getEmail());
      model.addAttribute("showWithdrawModal", true);
      return "account/settings";
    }

    // セッション無効化・ログアウト
    SecurityContextHolder.clearContext();
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }

    redirectAttrs.addFlashAttribute("withdrawSuccess", true);
    return "redirect:/login";
  }
}
