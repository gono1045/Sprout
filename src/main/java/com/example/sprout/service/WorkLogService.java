package com.example.sprout.service;

import com.example.sprout.form.WorkLogForm;
import com.example.sprout.model.WorkLogResult;

/**
 * 工数記録サービス
 */
public interface WorkLogService {

  /**
   * 工数ログを保存し、対象タグへ EXP を付与する。
   *
   * @param form フロントエンドから受け取った工数記録フォーム
   * @return EXP 付与結果
   */
  WorkLogResult save(WorkLogForm form);
}
