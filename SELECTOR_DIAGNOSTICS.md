# selector・動画の診断

診断コードは残したまま、既定で無効。設定は DiagnosticSettings.kt に集約する。
通常のアプリ処理・動画キャッシュ・メモリ制限は、診断の有効／無効と関係なく動く。

## 有効化

1. app/src/main/java/com/mizushiki/nicoflick_a/DiagnosticSettings.kt の REQUESTED を true にする。
2. debug ビルドを実機へ再インストールして起動する。
3. 調査後は REQUESTED を false に戻して再ビルドする。

release ビルドでは BuildConfig.DEBUG により常に無効になる。
REQUESTED を true のままにしても release では記録しない。

無効時はファイル出力、監視スレッド、診断用コールバック・プレイヤーリスナー、
通信イベントリスナー、バッファ集計、ログ本文・停止元スタックの生成を行わない。
呼び出し箇所には有効判定などの小さな処理が残るため、完全なゼロコストではない。
既存ログは自動削除しない。無効化中は更新されない。

## 実機で再現したら

1. 発生時刻、操作（結果表示中／OK後など）、画面が操作できたかをメモする。
2. 必要ならアプリを再起動する。Android 11以降では次回起動時に直近のプロセス終了理由を記録する。
3. アプリのアンインストール・データ消去はしない。
4. 実機をAndroid Studioへ接続し、Device Explorerで以下のフォルダーを保存する。

`/data/data/com.mizushiki.nicoflick_a/files/selector-diagnostics/`

`current.log` と `previous.log`（存在する場合）の両方を回収する。
debugビルドなら端末接続後に `adb shell run-as com.mizushiki.nicoflick_a ls files/selector-diagnostics` でも確認できる。
Logcatのタグは `SelectorDiag`。直後のLogcatも残っていれば併せて解析する。

各ファイル約1MiB、2世代。長期間使用すると古い記録は上書きされるため早めに回収する。
ログは機内モードやPC未接続でも保存する。専用の共有ボタンは追加していない。
通常のreleaseビルドではDevice Explorerからアプリ内部を読めないため、調査はdebugビルドで行う。

## 記録

- 時刻、uptime、プロセスID、実行スレッド、Javaヒープ使用量／上限、nativeヒープ
- スコア保存・送信データ作成・送信成功後の保存・プレイ履歴の開始／終了／時間
- 対象ストレージ読み込みの時間と文字数（バイト数ではない）
- 送信データの文字数／件数、応答成功／失敗、送信開始から応答までの時間
- Activity生成／再開／停止／保存／破棄、インスタンス、再生成フラグ、selectorの曲ID／位置
- selector再読込、背景動画の再生指示・停止元・状態・エラー種別
- MEDIA_BUFFERS: ゲーム／selectorのプレイヤー数、サンプルバッファの実使用量、ディスクキャッシュ使用量（プレイヤー状態変更時）
- メインスレッドが4秒以上停止したときのスタック、復帰、メモリ警告
- 次回起動時の過去5件のプロセス終了理由（Android 11以降）
- 未捕捉例外の型と上位スタック（元のクラッシュ処理へ引き継ぐ）

スコア本文・ユーザーID・認証情報・URL・例外メッセージは記録しない。
書き込みは専用スレッド、待ち行列は256件まで。過負荷時はログを破棄しアプリを待たせない。
クラッシュ時のみ同期書き込みを試みる。強制終了時は末尾の非同期ログが欠けることがある。
記録そのものにも小さな負荷がある。ログだけでOOMやANRを常に確定できるわけではない。

## 無効化と削除

一時停止は `DiagnosticSettings.REQUESTED = false` にして再ビルド。
完全削除は以下の呼び出しを取り除き、診断ファイルを削除する。

- NicoFlickApplication.kt: install
- Activity_Result.kt: measureラッパーと送信診断
- Activity_Selector.kt: event呼び出し
- SQLitePreferences.kt: getStringの診断分岐とdiagnosticKeys
- MovieLoadLog.kt: MoviePlaybackCache.recordPlayers()の呼び出し
- MoviePlaybackCache.kt: controlsとrecordPlayers、およびloadControl末尾の診断登録だけ（バッファ制限・ディスクキャッシュ本体は残す）
- SelectorDiagnostics.kt: ファイル全体
- DiagnosticSettings.kt: ファイル全体（MovieLoadLog等からの参照も削除する場合）
- この文書

measureラッパーを除去するときは中の元の処理を残す。
保存済みログは別途Device Explorerで削除できる。
