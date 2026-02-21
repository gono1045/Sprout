package com.example.sprout.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

  @GetMapping("/login")
  public String login(HttpServletRequest request, Model model) {
    // セッションからエラーメッセージを取得
    Object loginError = request.getSession().getAttribute("loginError");
    if (loginError != null) {
      model.addAttribute("loginError", loginError);
      // 一度取得したら削除するのでリロードしても消える
      request.getSession().removeAttribute("loginError");
    }
    return "login";
  }
}
