package com.example.sprout.controller;

import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sprout.form.SproutUserForm;
import com.example.sprout.model.SproutUser;
import com.example.sprout.service.SproutUserService;

/**
 * ユーザー登録画面コントローラー
 */
@Controller
@RequestMapping("/register")
public class SproutUserController {

  @Autowired
  private SproutUserService sproutUserService;

  @Autowired
  private AuthenticationManager authenticationManager;

  /**
   * 登録画面表示
   * @param form 登録フォーム
   * @return 登録画面テンプレート名
   */
  @GetMapping
  public String showRegisterForm(@ModelAttribute("sproutUserForm") SproutUserForm form) {

    if (form == null || form.getLoginId() == null) {
      form = new SproutUserForm();
    }

    return "register";
  }

  /**
   * ユーザー登録
   * パスワード一致チェック、loginId/email重複チェック後に登録
   * 登録成功時は自動ログイン
   * @param form 登録フォーム
   * @param bindingResult バリデーション結果
   * @param model View用モデル
   * @param request HTTPリクエスト
   * @return 登録画面テンプレート名
   */
  @PostMapping
  public String registerUser(
      @Valid @ModelAttribute("sproutUserForm") SproutUserForm form,
      BindingResult bindingResult, HttpServletRequest request,
      RedirectAttributes redirectAttrs) {

    // パスワード確認
    if (!form.getPassword().equals(form.getConfirmPassword())) {
      bindingResult.rejectValue("confirmPassword", null, "パスワードが一致しません");
    }

    // loginId/email 重複チェック
    if (sproutUserService.existsByLoginId(form.getLoginId())) {
      bindingResult.rejectValue("loginId", null, "このログインIDは既に使用されています");
    }
    if (sproutUserService.existsByEmail(form.getEmail())) {
      bindingResult.rejectValue("email", null, "このメールアドレスは既に使用されています");
    }

    // バリデーションエラー時
    if (bindingResult.hasErrors()) {
      return "register";
    }

    // 登録処理
    SproutUser user = form.createModel();
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    sproutUserService.registerUser(user);

    // 自動ログイン
    var auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(form.getLoginId(), form.getPassword()));
    SecurityContextHolder.getContext().setAuthentication(auth);
    request.getSession(true).setAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
        SecurityContextHolder.getContext());

    // 成功メッセージを Flash で渡してリダイレクト
    redirectAttrs.addFlashAttribute("successMessage", "アカウント作成が完了しました");
    return "redirect:/register";
  }
}
