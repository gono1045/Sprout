package com.example.sprout.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.sprout.dao.SproutItemListDao;
import com.example.sprout.dao.SproutTagListDao;

@Service
public class AccessControlService {

  @Autowired
  private SproutItemListDao sproutItemListDao;

  @Autowired
  private SproutTagListDao sproutTagListDao;

  /* ログインユーザー取得 */
  private SproutUserDetails getUserDetails() {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null ||
        !(auth.getPrincipal() instanceof SproutUserDetails)) {

      throw new IllegalStateException("ログインユーザーが取得できません");
    }

    return (SproutUserDetails) auth.getPrincipal();
  }

  public Long getLoginUserId() {
    return getUserDetails().getUser().getUserId();
  }

  public String getLoginId() {
    return getUserDetails().getUser().getLoginId();
  }

  /* Item所有チェック */
  public void checkItemOwner(Long itemId) {

    Long userId = getLoginUserId();

    if (sproutItemListDao.selectByItemId(itemId, userId) == null) {
      throw new AccessDeniedException("他ユーザーのタスクにはアクセスできません");
    }
  }

  /* Tag所有チェック */
  public void checkTagOwner(Long tagId) {

    Long userId = getLoginUserId();

    if (sproutTagListDao.selectByTagId(tagId, userId) == null) {
      throw new SecurityException("他ユーザーのタグにはアクセスできません");
    }
  }
}
