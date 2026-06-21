/**
 * timerModal.js
 * 工数計測タイマーモーダル
 *
 * 状態は localStorage で管理する（リロード・ブラウザクローズで消える）。
 *
 * 状態遷移:
 *   idle → running → paused → running → completing → (登録) → 閉じる
 *
 * NOTE: sprout-util の applyScreenIdPrefix により、モーダル内の ID には
 *       "timerModal_" プレフィックスが付与される。
 *       そのため getElementById は使わず、$modalEl スコープの jQuery セレクターを使う。
 */
var timerModal = (function () {

  var LS_STATE      = 'sprout_timer_state';       // idle | running | paused | completing
  var LS_ITEM_ID    = 'sprout_timer_item_id';
  var LS_STARTED_AT = 'sprout_timer_started_at';
  var LS_DURATION   = 'sprout_timer_duration_ms'; // 累積計測時間(ms)
  var LS_LAP_START  = 'sprout_timer_lap_start';   // 現在の計測区間開始時刻

  var _tickTimer    = null;
  var _satisfaction = 0;
  var _$modal       = null; // $modalEl への参照

  // ===== public =====

  function init($modalEl) {
    _$modal = $modalEl;
    _satisfaction = 0;

    if (typeof lucide !== 'undefined') lucide.createIcons();

    _bindEvents();

    var state = _getState();
    if (state === 'running' || state === 'paused') {
      _restoreState(state);
    } else if (state === 'completing') {
      _showCompletePhase();
    } else {
      _setState('idle');
      _renderIdle();
    }
  }

  // ===== private: $el ヘルパー（プレフィックス対応） =====

  function _$id(id) {
    // 実際の ID は "timerModal_<id>" になっている
    return _$modal.find('[id$="_' + id + '"], #' + id);
  }

  // ===== private: state =====

  function _getState() {
    return localStorage.getItem(LS_STATE) || 'idle';
  }

  function _setState(s) {
    localStorage.setItem(LS_STATE, s);
  }

  function _getDurationMs() {
    return parseInt(localStorage.getItem(LS_DURATION) || '0', 10);
  }

  function _clearStorage() {
    [LS_STATE, LS_ITEM_ID, LS_STARTED_AT, LS_DURATION, LS_LAP_START]
      .forEach(function(k) { localStorage.removeItem(k); });
  }

  // ===== private: render =====

  function _renderIdle() {
    _updateDisplay(0);
    _$id('timerStatusLabel').text('準備中');
    _$id('timerCloseBtn').show();
    _showButtons({ start: true });
    _$id('timerDisplay').attr('class', 'timer-clock');
  }

  function _renderRunning() {
    _$id('timerStatusLabel').text('計測中');
    _$id('timerCloseBtn').hide();
    _showButtons({ pause: true, finish: true });
    _$id('timerDisplay').attr('class', 'timer-clock is-running');
    _startTick();
  }

  function _renderPaused() {
    _stopTick();
    _updateDisplay(_getDurationMs());
    _$id('timerStatusLabel').text('一時停止中');
    _$id('timerCloseBtn').hide();
    _showButtons({ resume: true, finish: true });
    _$id('timerDisplay').attr('class', 'timer-clock is-paused');
  }

  function _showCompletePhase() {
    _stopTick();
    _$id('timerPhaseTimer').hide();
    _$id('timerPhaseComplete').show();
    _$id('timerCloseBtn').hide();
    _setState('completing');

    var min = Math.max(1, Math.floor(_getDurationMs() / 60000));
    _$id('timerSummaryDuration').text(_formatMin(min));
  }

  function _updateDisplay(ms) {
    _$id('timerDisplay').text(_formatMs(ms));
  }

  function _showButtons(flags) {
    _$id('timerBtnStart').hide();
    _$id('timerBtnPause').hide();
    _$id('timerBtnResume').hide();
    _$id('timerBtnFinish').hide();
    if (flags.start)  _$id('timerBtnStart').show();
    if (flags.pause)  _$id('timerBtnPause').show();
    if (flags.resume) _$id('timerBtnResume').show();
    if (flags.finish) _$id('timerBtnFinish').show();
  }

  // ===== private: tick =====

  function _startTick() {
    _stopTick();
    _tickTimer = setInterval(function() {
      var lapStart = localStorage.getItem(LS_LAP_START);
      if (!lapStart) return;
      var lapMs = Date.now() - new Date(lapStart).getTime();
      _updateDisplay(_getDurationMs() + lapMs);
    }, 500);
  }

  function _stopTick() {
    if (_tickTimer) { clearInterval(_tickTimer); _tickTimer = null; }
  }

  // ===== private: state restore =====

  function _restoreState(state) {
    if (state === 'running') _renderRunning();
    else _renderPaused();
  }

  // ===== private: events =====

  function _bindEvents() {

    // スタート
    _$id('timerBtnStart').off('click.timer').on('click.timer', function() {
      var now = new Date().toISOString();
      localStorage.setItem(LS_ITEM_ID,    _$id('timerItemId').val());
      localStorage.setItem(LS_STARTED_AT, now);
      localStorage.setItem(LS_LAP_START,  now);
      localStorage.setItem(LS_DURATION,   '0');
      _setState('running');
      _renderRunning();
    });

    // 一時停止
    _$id('timerBtnPause').off('click.timer').on('click.timer', function() {
      var lapStart = localStorage.getItem(LS_LAP_START);
      if (lapStart) {
        var lapMs = Date.now() - new Date(lapStart).getTime();
        localStorage.setItem(LS_DURATION, String(_getDurationMs() + lapMs));
        localStorage.removeItem(LS_LAP_START);
      }
      _setState('paused');
      _renderPaused();
    });

    // 再開
    _$id('timerBtnResume').off('click.timer').on('click.timer', function() {
      localStorage.setItem(LS_LAP_START, new Date().toISOString());
      _setState('running');
      _renderRunning();
    });

    // 完了
    _$id('timerBtnFinish').off('click.timer').on('click.timer', function() {
      var lapStart = localStorage.getItem(LS_LAP_START);
      if (lapStart) {
        var lapMs = Date.now() - new Date(lapStart).getTime();
        localStorage.setItem(LS_DURATION, String(_getDurationMs() + lapMs));
        localStorage.removeItem(LS_LAP_START);
      }
      _showCompletePhase();
    });

    // ★ 達成感
    _$id('timerStars').off('click.timer').on('click.timer', '.timer-star', function() {
      _satisfaction = parseInt($(this).data('star'), 10);
      _$id('timerStars').find('.timer-star').each(function() {
        $(this).toggleClass('active', parseInt($(this).data('star'), 10) <= _satisfaction);
      });
      _$id('timerBtnRegister').prop('disabled', false);
    });

    // 登録
    _$id('timerBtnRegister').off('click.timer').on('click.timer', _register);

    // × 閉じる（idle 時のみ有効）
    _$id('timerCloseBtn').off('click.timer').on('click.timer', function() {
      _clearStorage();
      _closeModal();
    });

    // ガーデンを確認する（結果フェーズ）
    _$id('timerBtnDone').off('click.timer').on('click.timer', function() {
      $(document).trigger('sprout:tags-updated');
      $(document).trigger('sprout:task-updated');
      _closeModal();
    });
  }

  // ===== private: register =====

  function _register() {
    var itemId      = localStorage.getItem(LS_ITEM_ID);
    var startedAt   = localStorage.getItem(LS_STARTED_AT);
    var durationMs  = _getDurationMs();
    var durationMin = Math.max(1, Math.floor(durationMs / 60000));
    var endedAt     = new Date().toISOString();
    var memo        = _$id('timerMemo').val().trim();

    var $btn = _$id('timerBtnRegister');
    $btn.prop('disabled', true).text('保存中…');

    $.ajax({
      url: '/work-log/save',
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({
        itemId:       parseInt(itemId, 10),
        startedAt:    startedAt,
        endedAt:      endedAt,
        durationMin:  durationMin,
        satisfaction: _satisfaction,
        memo:         memo
      })
    })
    .done(function(result) {
      _clearStorage();
      _showResultPhase(result);
    })
    .fail(function() {
      $btn.prop('disabled', false).html(
        '<i data-lucide="check" style="width:15px;height:15px;display:inline-block;vertical-align:middle;"></i>&nbsp; 記録する'
      );
      sprout.message.toast({ message: '保存に失敗しました', type: 'error' });
    });
  }

  // ===== private: result phase =====

  var _MOTIVATIONAL_MSGS = [
    'コツコツが実を結ぶ。素晴らしい集中力でした！',
    '今日の努力は確実に根を張っています。',
    '着実に成長しています。この調子で！',
    '一歩一歩が大きな木になる。お疲れさまでした！',
    'よく頑張りました！あなたのスキルが育っています。'
  ];

  /** レベルに対応するラベル */
  var _LV_NAMES = {
    1: '種', 2: '芽', 3: '苗', 4: '若木', 5: '成木',
    6: '蕾', 7: '開花', 8: '結実', 9: '豊穣', 10: '大樹'
  };

  /** LV 閾値（バックエンドと同値）*/
  var _LV_THRESHOLDS = [0, 300, 1000, 2500, 5000, 9500, 15600, 23000, 32000, 47000];

  /** EXP → 次 LV の閾値を返す（LV10 の場合は null） */
  function _nextLvThreshold(lv) {
    return lv < 10 ? _LV_THRESHOLDS[lv] : null; // lv は 1-based; インデックス lv で次のLVの値を取得
  }

  /** 現 LV の開始閾値 */
  function _curLvThreshold(lv) {
    return _LV_THRESHOLDS[lv - 1];
  }

  /**
   * 結果フェーズを表示する。
   * @param {Object} result  WorkLogResult JSON
   *   { expTotal: number, durationMin: number, tagResults: [...] }
   */
  function _showResultPhase(result) {
    // フェーズ切替
    _$id('timerPhaseComplete').hide();
    _$id('timerPhaseTimer').hide();
    _$id('timerCloseBtn').hide();
    _$id('timerPhaseResult').show();

    if (typeof lucide !== 'undefined') lucide.createIcons();

    // EXP 数値アニメーション
    var targetExp = result.expTotal || 0;
    _$id('timerResultExp').html('0&nbsp;<span>EXP</span>');
    var start = null;
    var duration = 600;
    function animateExp(ts) {
      if (!start) start = ts;
      var progress = Math.min((ts - start) / duration, 1);
      var val = Math.floor(progress * targetExp);
      _$id('timerResultExp').html(val + '&nbsp;<span>EXP</span>');
      if (progress < 1) requestAnimationFrame(animateExp);
    }
    requestAnimationFrame(animateExp);

    // 励ましメッセージ
    var msg = _MOTIVATIONAL_MSGS[Math.floor(Math.random() * _MOTIVATIONAL_MSGS.length)];
    _$id('timerResultMsg').text(msg);

    // タグ別内訳
    var tagResults = result.tagResults || [];
    var $tagsEl = _$id('timerResultTags').empty();

    tagResults.forEach(function(t) {
      var lv     = t.newLv || 1;
      var lvName = _LV_NAMES[lv] || '';
      var curThreshold  = _curLvThreshold(lv);
      var nextThreshold = _nextLvThreshold(lv);
      var barPct = nextThreshold
        ? Math.min(100, Math.round((t.newExp - curThreshold) / (nextThreshold - curThreshold) * 100))
        : 100;

      var lvupHtml = t.leveledUp
        ? '<span class="timer-result-lvup"><i data-lucide="trending-up" style="width:10px;height:10px;display:inline-block;vertical-align:middle;margin-right:2px;"></i>LV UP</span>'
        : '';

      // タグカラーを CSS 変数から解決
      var colorMap = {
        'GREEN':  '#3E7A52', 'BLUE':   '#3B82F6', 'YELLOW': '#F59E0B',
        'RED':    '#EF4444', 'PURPLE': '#8B5CF6', 'PINK':   '#EC4899',
        'ORANGE': '#F97316', 'GRAY':   '#6B7280'
      };
      var bgColor = colorMap[(t.tagColor || '').toUpperCase()] || '#3E7A52';

      var rowHtml =
        '<div class="timer-result-tag-row">' +
          '<span class="timer-result-tag-chip" style="background:' + bgColor + ';">' +
            t.tagName +
          '</span>' +
          '<div class="timer-result-tag-exp-bar-wrap">' +
            '<div class="timer-result-tag-exp-bar" style="width:0%" data-pct="' + barPct + '"></div>' +
          '</div>' +
          '<span class="timer-result-tag-lv">Lv' + lv + ' ' + lvName + '</span>' +
          lvupHtml +
        '</div>';

      $tagsEl.append(rowHtml);
    });

    // LV UP トースト（Lv アップしたタグがあれば通知）
    var leveledUpTags = tagResults.filter(function(t) { return t.leveledUp; });
    if (leveledUpTags.length > 0) {
      setTimeout(function() {
        leveledUpTags.forEach(function(t) {
          var stageName = _LV_NAMES[t.newLv] || ('Lv' + t.newLv);
          sprout.message.toast({
            message: '「' + t.tagName + '」が ' + stageName + ' にレベルアップしました！',
            type: 'success'
          });
        });
      }, 800);
    }

    // バーアニメーション（少し遅らせてから）
    setTimeout(function() {
      $tagsEl.find('.timer-result-tag-exp-bar').each(function() {
        var pct = $(this).data('pct');
        $(this).css('width', pct + '%');
      });
      if (typeof lucide !== 'undefined') lucide.createIcons();
    }, 150);
  }

  // ===== private: close =====

  function _closeModal() {
    _stopTick();
    _$modal && _$modal.remove();
    $(document).off('keydown.modal');
  }

  // ===== private: util =====

  function _formatMs(ms) {
    var totalSec = Math.floor(ms / 1000);
    var h = Math.floor(totalSec / 3600);
    var m = Math.floor((totalSec % 3600) / 60);
    var s = totalSec % 60;
    return _pad(h) + ':' + _pad(m) + ':' + _pad(s);
  }

  function _formatMin(min) {
    if (min < 60) return min + '分';
    var h = Math.floor(min / 60);
    var m = min % 60;
    return h + '時間' + (m > 0 ? m + '分' : '');
  }

  function _pad(n) { return n < 10 ? '0' + n : String(n); }

  return { init: init };

})();
