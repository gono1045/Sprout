package com.example.sprout.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * エラー画面確認用テストコントローラー。
 * localプロファイルでのみ有効（本番環境では起動しない）。
 *
 * 使用方法:
 *   http://localhost:8080/test/403 → 403エラー画面
 *   http://localhost:8080/test/404 → 404エラー画面
 *   http://localhost:8080/test/500 → 500エラー画面
 */
@Controller
@RequestMapping("/test")
@Profile("local")
public class TestController {

    @GetMapping("/403")
    public String test403() {
        throw new AccessDeniedException("テスト用403エラー");
    }

    /**
     * sendError() を使って GlobalExceptionHandler をバイパスし、
     * Spring Boot の ErrorController (→ templates/error/404.html) へ転送する。
     */
    @GetMapping("/404")
    public void test404(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @GetMapping("/500")
    public String test500() {
        throw new RuntimeException("テスト用500エラー");
    }
}
