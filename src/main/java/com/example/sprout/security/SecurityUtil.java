package com.example.sprout.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

  public static SproutUserDetails getLoginUser() {
    Authentication auth =
        SecurityContextHolder.getContext().getAuthentication();

    return (SproutUserDetails) auth.getPrincipal();
  }

  public static Long getLoginUserId() {
    return getLoginUser().getUserId();
  }
}
