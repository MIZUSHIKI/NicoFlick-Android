package com.mizushiki.nicoflick_a

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.Html
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.*
import kotlin.coroutines.resume

/** Android adaptation of EditorView / EditorMenu / EditorSettings. */

// AIに「iPhone版を真似てマージしてみて」のままのコード
// - そのままではおおよそ使えないので修正する必要がある。
// - 現状はコードのみ残し封印。将来に期待。
class Activity_Editor : SafeAreaActivity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var music: musicData
    private var source: levelData? = null
    private var levelID = 0
    private lateinit var storage: EditorStorage
    private var document: EditorDocument? = null
    private lateinit var editorRoot: FrameLayout
    private var editorGame: EditorGame? = null
    private lateinit var playerView: PlayerView
    private var player: SimpleExoPlayer? = null
    private lateinit var timeline: EditorTimeline
    private lateinit var status: TextView
    private lateinit var playButton: Button
    private lateinit var stampButton: Button
    private lateinit var noteMode: Switch
    private lateinit var seek: SeekBar
    private lateinit var rateLabel: TextView
    private lateinit var rateBar: SeekBar
    private lateinit var undoButton: Button
    private lateinit var redoButton: Button
    private lateinit var editControls: FrameLayout
    private lateinit var testButton: Button
    private lateinit var removeButton: Button
    private var password = ""
    private var rate = 1f
    private var stampOffsetMs = -115
    private var rateFactor = 0.07
    private var positionMs = 0L
    private var dragging = false
    private var resumed = false
    private var busy = false
    private var completed = false
    private var autoPlay = false
    private var exportText: String? = null
    private var sessionInvalidated = false
    private val ticker = object : Runnable {
        override fun run() {
            if (!resumed || isFinishing) return
            player?.let {
                positionMs = it.currentPosition
                if (!dragging && it.duration > 0) seek.progress = (positionMs * 10000 / it.duration).toInt()
                timeline.playbackMs = positionMs; timeline.invalidate()
                playButton.text = if (it.playWhenReady) "停止" else "再生"
            }
            updateStatus()
            handler.postDelayed(this, 66)
        }
    }
    private val saveTimer = object : Runnable {
        override fun run() {
            if (!resumed || completed || isFinishing) return
            saveLocal(showMessage = false); handler.postDelayed(this, 10000)
        }
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        GLOBAL.APPLICATIONCONTEXT = applicationContext
        exportText = state?.getString("editorExportText")
        levelID = intent.getIntExtra("levelID", 0)
        val musicID = intent.getIntExtra("musicID", 0)
        if (MusicDataLists.musics.isEmpty()) {
            MetadataStorage.load()
        }
        music = MusicDataLists.musics.firstOrNull { it.sqlID == musicID } ?: run { finish(); return }
        source = MusicDataLists.levels[music.movieURL]?.firstOrNull { it.sqlID == levelID }
        if (levelID > 0 && source == null) { finish(); return }
        storage = EditorStorage(this, music.sqlID, levelID)
        createUi()
        title = "譜面エディター：${music.title}"
        val restored = try { storage.read() } catch (e: Exception) {
            showMessage("下書きを読み込めませんでした。下書きファイルは保持しています。\n${e.message}"); null
        }
        if (restored != null) {
            document = EditorDocument(restored.text).apply { cursor = restored.cursor }
            positionMs = restored.positionMs; rate = restored.rate.coerceIn(0.1f, 2f)
            stampOffsetMs = restored.stampOffsetMs; rateFactor = restored.rateFactor
            noteMode.isChecked = restored.noteMode
            finishLoading()
        } else if (levelID == 0) {
            val baseID = intent.getIntExtra("baseLevelID", 0)
            val base = MusicDataLists.levels[music.movieURL]?.firstOrNull { it.sqlID == baseID }
            if (base == null) initializeDocument("") else downloadDocument(base, newChart = true)
        } else {
            askPassword("譜面を編集", "投稿時に設定したパスワードを入力してください。") { pass ->
                runBusy {
                    if (!EditorApi.checkPassword(levelID, pass, USERDATA.UserID)) {
                        showMessage("パスワードが違うか、通信に失敗しました。メニューの「再読込」から再試行できます。")
                    } else { password = pass; downloadDocument(source!!, newChart = false) }
                }
            }
        }
        loadMovie()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun createUi() {
        supportActionBar?.hide()
        editorRoot = FrameLayout(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.WHITE)
            setPadding(dp(8), 0, dp(8), 0)
        }
        editorRoot.addView(root, FrameLayout.LayoutParams(-1, -1))
        val header = horizontal(root)
        button(header, "× 戻る") { onBackPressed() }
        button(header, "歌詞") { showLyricsMenu() }
        button(header, "☰ メニュー") { showMenu() }
        val movieRow = horizontal(root)
        playerView = PlayerView(this).apply {
            useController = false; setBackgroundColor(Color.BLACK)
            setOnClickListener { player?.let { it.playWhenReady = !it.playWhenReady }; updateStatus() }
        }
        movieRow.addView(playerView, LinearLayout.LayoutParams(0, dp(112), 1.6f))
        val side = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(8), 0, 0, 0) }
        movieRow.addView(side, LinearLayout.LayoutParams(0, -2, 1f))
        noteMode = Switch(this).apply { text = "フリック\nノーツ"; textSize = 12f; setTextColor(Color.BLACK) }
        side.addView(noteMode, LinearLayout.LayoutParams(-1, dp(48)))
        noteMode.setOnCheckedChangeListener { _, _ -> updateStatus() }
        testButton = button(horizontal(side), "テストプレイ") { testPlay() }.apply {
            setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(0, 157, 245)); textSize = 14f
        }
        val transport = horizontal(root)
        button(transport, "◀◀") { shift(-1) }
        playButton = button(transport, "再生") { player?.let { it.playWhenReady = !it.playWhenReady }; updateStatus() }
        button(transport, "▶▶") { shift(1) }
        rateLabel = TextView(this).apply { textSize = 12f; setTextColor(Color.BLACK); gravity = Gravity.CENTER }
        transport.addView(rateLabel, LinearLayout.LayoutParams(dp(64), dp(44)))
        rateBar = SeekBar(this).apply { max = 19; progress = 9; contentDescription = "再生速度" }
        transport.addView(rateBar, LinearLayout.LayoutParams(0, dp(44), 2f))
        rateBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
                rate = (value + 1) / 10f; rateLabel.text = "×%.1f".format(rate)
                player?.setPlaybackParameters(PlaybackParameters(rate))
            }
            override fun onStartTrackingTouch(bar: SeekBar?) {}
            override fun onStopTrackingTouch(bar: SeekBar?) {}
        })
        rateLabel.text = "×%.1f".format(rate)
        seek = SeekBar(this).apply { max = 10000; contentDescription = "動画の再生位置" }
        root.addView(seek, LinearLayout.LayoutParams(-1, dp(28)))
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
                if (fromUser) player?.let { if (it.duration > 0) seekTo(it.duration * value / 10000) }
            }
            override fun onStartTrackingTouch(bar: SeekBar?) { dragging = true }
            override fun onStopTrackingTouch(bar: SeekBar?) { dragging = false }
        })
        status = TextView(this).apply { textSize = 11f; setTextColor(Color.BLACK); setPadding(0, 0, 0, dp(6)) }
        root.addView(status, LinearLayout.LayoutParams(-1, -2))
        val lyricsArea = FrameLayout(this)
        root.addView(lyricsArea, LinearLayout.LayoutParams(-1, 0, 1f))
        timeline = EditorTimeline(this)
        timeline.onCursor = { updateStatus() }; timeline.onSeek = { seekTo(it.toLong()) }; timeline.onEdit = { editCharacter() }
        lyricsArea.addView(timeline, FrameLayout.LayoutParams(-1, -1))
        editControls = FrameLayout(this)
        lyricsArea.addView(editControls, FrameLayout.LayoutParams(-1, dp(220), Gravity.BOTTOM))
        fun floating(text: String, color: Int, size: Int, gravity: Int, bottom: Int, action: () -> Unit): Button {
            return Button(this).apply {
                this.text = text; textSize = if (size > 100) 18f else 12f
                setTextColor(Color.WHITE); setPadding(0, 0, 0, 0)
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL; setColor(color)
                }
                editControls.addView(this, FrameLayout.LayoutParams(dp(size), dp(size), gravity).apply { bottomMargin = dp(bottom) })
                setOnClickListener { if (!busy) action() }
            }
        }
        stampButton = floating("Timetag", Color.argb(215, 0, 205, 130), 168, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 8) { stamp() }
        removeButton = floating("−", Color.argb(215, 255, 45, 125), 72, Gravity.TOP or Gravity.RIGHT, 0) { removePrevious() }
        undoButton = floating("Undo", Color.argb(210, 150, 150, 150), 56, Gravity.TOP or Gravity.LEFT, 0) { document?.undo(); changed() }
        redoButton = floating("Redo", Color.argb(210, 150, 150, 150), 48, Gravity.TOP or Gravity.LEFT, 0) { document?.redo(); changed() }
        (redoButton.layoutParams as FrameLayout.LayoutParams).leftMargin = dp(60)
        floating("◀◀", Color.argb(215, 0, 157, 245), 52, Gravity.BOTTOM or Gravity.LEFT, 58) { shift(-1) }
        floating("▶▶", Color.argb(215, 0, 157, 245), 52, Gravity.BOTTOM or Gravity.RIGHT, 58) { shift(1) }
        setContentView(editorRoot)
    }
    private fun horizontal(parent: LinearLayout): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL; parent.addView(this, LinearLayout.LayoutParams(-1, -2))
    }
    private fun button(parent: LinearLayout, text: String, action: () -> Unit): Button = Button(this).apply {
        this.text = text; textSize = 11f; minWidth = 0; minimumWidth = 0; setPadding(dp(2), 0, dp(2), 0)
        parent.addView(this, LinearLayout.LayoutParams(0, dp(44), 1f))
        setOnClickListener { if (!busy) action() }
    }
    private fun initializeDocument(text: String) {
        document = EditorDocument(text).apply {
            tags.putIfAbsent("Level", (source?.level ?: 100).toString())
            tags.putIfAbsent("Speed", (source?.speed ?: 150).toString())
            tags.putIfAbsent("Creator", if (levelID == 0) USERDATA.UserName else source!!.creator)
            tags.putIfAbsent("Description", if (levelID == 0 || source?.isEditing == true) "" else source!!.description)
            tags.putIfAbsent("Offset", "0")
        }
        finishLoading(); saveLocal(false)
    }
    private fun finishLoading() {
        timeline.document = document; rateBar.progress = (rate * 10).toInt() - 1
        changed(); status.text = "下書きを復元しました。"
    }
    private fun downloadDocument(level: levelData, newChart: Boolean, resolvingBase: Boolean = false) {
        busy = true; status.text = "譜面を取得しています…"
        ServerDataHandler().DownloadTimetag(level) { error ->
            if (isFinishing || isDestroyed) return@DownloadTimetag
            busy = false
            if (error != null) { showMessage("譜面の取得に失敗しました。下書きがある場合は次回起動時に復元されます。"); return@DownloadTimetag }
            val parsed = EditorDocument(level.noteData)
            val baseID = parsed.tags["BaseDataNo"]?.toIntOrNull()
            if (baseID != null) {
                if (resolvingBase) { showMessage("基の譜面データが循環しています。"); return@DownloadTimetag }
                val base = MusicDataLists.levels.values.flatten().firstOrNull { it.sqlID == baseID }
                    ?: levelData().apply { sqlID = baseID; noteData = "" }
                downloadDocument(base, newChart, true); return@DownloadTimetag
            }
            if (newChart) {
                val original = MusicDataLists.levels[music.movieURL]?.firstOrNull { it.sqlID == intent.getIntExtra("baseLevelID", 0) }
                parsed.tags["Level"] = original?.level?.toString() ?: "100"; parsed.tags["Speed"] = original?.speed?.toString() ?: "150"
                parsed.tags["Creator"] = USERDATA.UserName; parsed.tags["Description"] = ""
            }
            initializeDocument(parsed.export(EditorDocument.Export.LOCAL))
        }
    }
    private fun loadMovie() {
        val sm = Regex("watch/([^?/#]+)").find(music.movieURL)?.groupValues?.get(1) ?: return
        GLOBAL.Selector_instance?.ThumbMovieStop(); CachedThumbMovies.allRelease()
        MovieAccess().StreamingUrlNicoAccess(sm) { url ->
            if (isFinishing || isDestroyed) return@StreamingUrlNicoAccess
            if (url.isBlank() || url == "error") { showMessage("動画を読み込めませんでした。歌詞編集と端末保存は利用できます。"); return@StreamingUrlNicoAccess }
            try {
                player = CachedMovies.access(url, sm).apply {
                    playWhenReady = false; setSeekParameters(SeekParameters.EXACT)
                    setPlaybackParameters(PlaybackParameters(rate)); seekTo(positionMs)
                }
                playerView.player = player
                if (!resumed) player?.playWhenReady = false
            } catch (e: Exception) { showMessage("動画を読み込めませんでした。歌詞編集と端末保存は利用できます。") }
        }
    }
    private fun stamp() {
        val doc = document ?: return
        val p = player ?: run { showMessage("動画の読み込み完了後に時刻を付けられます。"); return }
        if (p.playbackState != Player.STATE_READY || dragging) return
        val corrected = EditorDocument.correctedStamp(p.currentPosition, rate, stampOffsetMs, rateFactor)
        if (noteMode.isChecked) doc.markAt((corrected - doc.offsetMs).coerceAtLeast(0), p.playWhenReady)
        else doc.stamp(corrected)
        changed(); saveLocal(false)
    }
    private fun removePrevious() {
        val doc = document ?: return
        val oldTime = doc.removePrevious(noteMode.isChecked) ?: return
        if (!noteMode.isChecked) seekTo((oldTime - 3000 * rate).toLong().coerceAtLeast(0))
        changed(); saveLocal(false)
    }
    private fun shift(direction: Int) { seekTo(positionMs + (direction * 3000 * rate).toLong()) }
    private fun seekTo(time: Long) {
        val duration = player?.duration ?: C.TIME_UNSET
        positionMs = time.coerceAtLeast(0).let { if (duration > 0) it.coerceAtMost(duration - 1) else it }
        player?.seekTo(positionMs); timeline.playbackMs = positionMs; timeline.invalidate()
    }
    private fun changed() { timeline.rebuild(true); updateStatus() }
    private fun updateStatus() {
        val doc = document ?: run { status.text = if (busy) "読み込み中…" else "メニューから譜面を読み込んでください"; return }
        val current = doc.characters[doc.cursor]
        val duration = player?.duration ?: 0L
        val selectedTime = if (current.timeMs < 0) "時刻なし" else EditorDocument.formatTime(current.timeMs.toLong(), current.note)
        status.text = "${EditorDocument.formatTime(positionMs)}   $selectedTime   / ${if (duration > 0) EditorDocument.formatTime(duration) else "--:--"}"
        val playing = player?.playWhenReady == true
        editControls.visibility = if (editorGame == null && (playing || noteMode.isChecked)) View.VISIBLE else View.GONE
        stampButton.text = if (noteMode.isChecked) "フリックノーツ\n$selectedTime" else "Timetag"
        (stampButton.background as android.graphics.drawable.GradientDrawable)
            .setColor(if (noteMode.isChecked) Color.argb(215, 255, 155, 0) else Color.argb(215, 0, 205, 130))
        testButton.isEnabled = !busy && player?.playbackState == Player.STATE_READY
        undoButton.isEnabled = doc.canUndo; redoButton.isEnabled = doc.canRedo
    }
    private fun saveLocal(showMessage: Boolean = true): Boolean {
        if (completed) return true
        val doc = document ?: return false
        return try {
            val text = doc.export(EditorDocument.Export.LOCAL)
            storage.write(EditorStorage.Draft(text, doc.cursor, positionMs, rate, stampOffsetMs, rateFactor, noteMode.isChecked))
            if (showMessage) Toast.makeText(this, "端末に下書きを保存しました", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            if (showMessage) showMessage("端末に保存できませんでした。テキスト書出しでデータを保存してください。\n${e.message}")
            false
        }
    }
    private fun editCharacter() {
        val doc = document ?: return
        val c = doc.characters[doc.cursor]
        val fields = vertical()
        val word = input(fields, "文字（改行は空欄）", if (c.word == "\n") "" else c.word)
        val time = input(fields, "時刻（ミリ秒、空欄で削除）", if (c.timeMs < 0) "" else c.timeMs.toString(), signedNumber())
        val note = CheckBox(this).apply { text = "フリック対象"; isChecked = c.note }; fields.addView(note)
        val dialog = AlertDialog.Builder(this).setTitle("選択文字を編集").setView(fields)
            .setPositiveButton("OK", null).setNegativeButton("キャンセル", null).create()
        dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val number = time.text.toString().trim()
            val ms = if (number.isEmpty()) -1 else number.toIntOrNull()
            if (ms == null || ms < -1 || ms >= 6000000) { time.error = "0〜5999999、または空欄"; return@setOnClickListener }
            val entered = word.text.toString()
            if (entered.isNotEmpty() && EditorDocument(entered).characters.size != 2) { word.error = "1文字を入力してください"; return@setOnClickListener }
            doc.editCharacter(entered.ifEmpty { "\n" }, ms, note.isChecked)
            changed(); saveLocal(false); dialog.dismiss()
        } }; dialog.show()
    }
    private fun vertical() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(6), dp(18), dp(6)) }
    private fun input(parent: LinearLayout, hint: String, value: String, type: Int = InputType.TYPE_CLASS_TEXT): EditText = EditText(this).apply {
        this.hint = hint; inputType = type; setText(value); parent.addView(this, LinearLayout.LayoutParams(-1, -2))
    }
    private fun signedNumber() = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
    private fun scroll(view: View) = ScrollView(this).apply { addView(view) }
    private fun showLyricsMenu() {
        if (document == null) return
        AlertDialog.Builder(this).setTitle("歌詞・譜面テキスト").setItems(arrayOf("歌詞を挿入", "歌詞・タイムタグ全文を編集", "選択文字を編集", "直前の文字を削除", "テキストファイルを読み込む", "テキストファイルへ書き出す", "初音ミクWikiから歌詞を取得")) { _, which ->
            when (which) {
                0 -> lyricsDialog(insert = true)
                1 -> lyricsDialog(insert = false)
                2 -> editCharacter()
                3 -> { document?.backDelete(); changed(); saveLocal(false) }
                4 -> startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "text/*"; addCategory(Intent.CATEGORY_OPENABLE) }, 201)
                5 -> exportFile()
                6 -> wikiLyrics()
            }
        }.show()
    }
    private fun lyricsDialog(insert: Boolean, initial: String? = null) {
        val doc = document ?: return
        val layout = vertical()
        val lyrics = input(layout, "歌詞 / [00:00.00]形式のテキスト", initial ?: if (insert) "" else doc.export(EditorDocument.Export.LOCAL),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
        lyrics.minLines = 6; lyrics.maxLines = 12; lyrics.gravity = Gravity.TOP
        val original = TextView(this).apply { textSize = 11f; visibility = View.GONE }; layout.addView(original)
        val convert = button(layout, "漢字・カタカナをひらがなに変換") {
            val before = lyrics.text.toString()
            if (before.length > 100000) { showMessage("一度に変換できるのは10万文字までです。"); return@button }
            runBusy {
                val result = withContext(Dispatchers.Default) { EditorLyrics.hiragana(before) }
                original.text = "変換前：\n$before"; original.visibility = View.VISIBLE; lyrics.setText(result)
            }
        }
        convert.layoutParams = LinearLayout.LayoutParams(-1, dp(44))
        val dialog = AlertDialog.Builder(this).setTitle(if (insert) "カーソル位置に挿入" else "全文を編集")
            .setMessage("かなの読みは確認・修正してください。時刻の : は表示のみ、. はフリック対象です。")
            .setView(scroll(layout)).setPositiveButton(if (insert) "挿入" else "反映", null).setNegativeButton("キャンセル", null).create()
        dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (busy) return@setOnClickListener
            val text = lyrics.text.toString()
            if (text.length > 250000) { lyrics.error = "25万文字以下にしてください"; return@setOnClickListener }
            if (insert) doc.insertText(text) else doc.replaceText(text)
            changed(); saveLocal(false); dialog.dismiss()
        } }; dialog.show()
    }
    private fun wikiLyrics() {
        val field = EditText(this).apply { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI; setText("https://w.atwiki.jp/hmiku/pages/") }
        val dialog = AlertDialog.Builder(this).setTitle("初音ミクWikiのページURL").setView(field)
            .setPositiveButton("取得", null).setNegativeButton("キャンセル", null).create()
        dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val url = field.text.toString().trim(); val uri = Uri.parse(url)
            if (uri.scheme != "https" || uri.host != "w.atwiki.jp" || !uri.path.orEmpty().startsWith("/hmiku/pages/")) { field.error = "初音ミクWikiのhttpsページURLを入力してください"; return@setOnClickListener }
            dialog.dismiss()
            runBusy {
                val html = withContext(Dispatchers.IO) { HttpUtil.httpGET(url, noCache = true) }
                val section = html?.let { EditorLyrics.wikiSection(it) }
                if (section == null) showMessage("歌詞を取得できませんでした。歌詞をコピーして挿入してください。")
                else lyricsDialog(true, Html.fromHtml(section, Html.FROM_HTML_MODE_LEGACY).toString().trim())
            }
        } }; dialog.show()
    }
    private fun exportFile() {
        val doc = document ?: return
        exportText = doc.export(EditorDocument.Export.LOCAL)
        startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = "text/plain"; addCategory(Intent.CATEGORY_OPENABLE); putExtra(Intent.EXTRA_TITLE, "NicoFlick_${music.sqlID}_${levelID}.txt")
        }, 202)
    }
    private fun showMenu() {
        val choices = if (document == null) arrayOf("再読込", "操作説明") else arrayOf("譜面情報・難易度・速度・オフセット", "打刻補正の設定", "端末に保存", "編集中としてサーバーに仮保存", "本投稿", "サーバーの譜面を再読込", "下書きを破棄", "データベースから削除", "操作説明", "ライセンス")
        AlertDialog.Builder(this).setTitle("エディターメニュー").setItems(choices) { _, index ->
            if (document == null) { if (index == 0) reload() else help(); return@setItems }
            when (index) {
                0 -> metadata()
                1 -> correctionSettings()
                2 -> saveLocal()
                3 -> submit(true)
                4 -> submit(false)
                5 -> AlertDialog.Builder(this).setMessage("端末の下書きをサーバーの譜面で置き換えますか？").setPositiveButton("再読込") { _, _ -> reload() }.setNegativeButton("キャンセル", null).show()
                6 -> AlertDialog.Builder(this).setMessage("端末の下書きを破棄して戻りますか？サーバーの譜面は変更しません。").setPositiveButton("破棄") { _, _ -> storage.delete(); completed = true; finish() }.setNegativeButton("キャンセル", null).show()
                7 -> deleteRemote()
                8 -> help()
                9 -> { val text = assets.open("editor_licenses.txt").bufferedReader().use { it.readText() }; showMessage(text) }
            }
        }.show()
    }
    private fun reload() {
        if (levelID == 0) { initializeDocument(""); return }
        askPassword("譜面を再読込", "投稿パスワードを入力してください。") { pass -> runBusy {
            if (EditorApi.checkPassword(levelID, pass, USERDATA.UserID)) { password = pass; downloadDocument(source!!, false) }
            else showMessage("パスワードが違うか、通信に失敗しました。端末の下書きは残っています。")
        } }
    }
    private fun metadata() {
        val doc = document ?: return
        val layout = vertical()
        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@Activity_Editor, android.R.layout.simple_spinner_dropdown_item, (1..10).map { "★$it" } + "FULL")
            setSelection(if (doc.level in 1..10) doc.level - 1 else 10)
        }; layout.addView(spinner)
        val creator = input(layout, "Creator", doc.tags["Creator"] ?: USERDATA.UserName)
        val description = input(layout, "説明", doc.tags["Description"] ?: "")
        val speed = input(layout, "ノーツ速度（50〜500）", doc.speed.toString(), InputType.TYPE_CLASS_NUMBER)
        val offset = input(layout, "譜面全体のオフセット（ms）", doc.offsetMs.toString(), signedNumber())
        val reference = TextView(this).apply { text = "難易度の参考値：★${doc.estimatedLevel()}" }; layout.addView(reference)
        val dialog = AlertDialog.Builder(this).setTitle("譜面情報").setView(scroll(layout)).setPositiveButton("保存", null).setNegativeButton("キャンセル", null).create()
        dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val speedValue = speed.text.toString().toIntOrNull(); val offsetValue = offset.text.toString().toIntOrNull()
            if (speedValue == null || speedValue !in 50..500) { speed.error = "50〜500を入力してください"; return@setOnClickListener }
            if (offsetValue == null || offsetValue !in -600000..600000) { offset.error = "−600000〜600000を入力してください"; return@setOnClickListener }
            doc.setMetadata(linkedMapOf("Level" to if (spinner.selectedItemPosition == 10) "100" else "${spinner.selectedItemPosition + 1}",
                "Creator" to creator.text.toString(), "Description" to description.text.toString(), "Speed" to "$speedValue", "Offset" to "$offsetValue"))
            changed(); saveLocal(false); dialog.dismiss()
        } }; dialog.show()
    }
    private fun correctionSettings() {
        val layout = vertical()
        val offset = input(layout, "打刻補正（ms）", stampOffsetMs.toString(), signedNumber())
        val factor = input(layout, "再生速度補正の係数（秒）", rateFactor.toString(), InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED)
        val dialog = AlertDialog.Builder(this).setTitle("打刻時刻の補正").setMessage("時刻 = 再生位置 + 係数 × (1−再生速度) + 打刻補正\n初期値：−115ms / 0.07")
            .setView(layout).setPositiveButton("保存", null).setNegativeButton("キャンセル", null).create()
        dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val a = offset.text.toString().toIntOrNull(); val b = factor.text.toString().toDoubleOrNull()
            if (a == null || a !in -5000..5000) { offset.error = "−5000〜5000を入力してください"; return@setOnClickListener }
            if (b == null || !b.isFinite() || b !in -5.0..5.0) { factor.error = "−5〜5を入力してください"; return@setOnClickListener }
            stampOffsetMs = a; rateFactor = b; saveLocal(false); dialog.dismiss()
        } }; dialog.show()
    }
    private fun testPlay() {
        val doc = document ?: return
        val movie = player ?: run { showMessage("動画の読み込み完了後に試してください。"); return }
        if (editorGame != null) return
        if (doc.characters.none { it.timeMs >= 0 && it.checkable }) { showMessage("先にかな文字に時刻を付けてください。"); return }
        if (!saveLocal(false)) return
        editorGame = EditorGame(this, movie, doc, rateFactor, autoPlay) { automatic ->
            autoPlay = automatic
            val game = editorGame
            editorGame = null
            if (game != null) editorRoot.removeView(game)
            positionMs = movie.currentPosition
            rate = movie.playbackParameters.speed
            rateBar.progress = (rate * 10).toInt() - 1
            changed(); saveLocal(false)
        }
        editControls.visibility = View.GONE
        editorRoot.addView(editorGame, FrameLayout.LayoutParams(-1, -1))
    }
    private fun askPassword(title: String, message: String, callback: (String) -> Unit) {
        val field = EditText(this).apply { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD; setText(password) }
        val dialog = AlertDialog.Builder(this).setTitle(title).setMessage(message).setView(field)
            .setPositiveButton("OK", null).setNegativeButton("キャンセル", null).create()
        dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val pass = field.text.toString()
            if (pass.isEmpty()) { field.error = "パスワードを入力してください"; return@setOnClickListener }
            dialog.dismiss(); callback(pass)
        } }; dialog.show()
    }
    private fun submit(draft: Boolean) {
        val doc = document ?: return
        if (sessionInvalidated) { showMessage("この譜面は送信済みです。選曲画面に戻って更新された譜面を開いてください。"); return }
        if (!draft) doc.publicationProblem()?.let { showMessage(it); return }
        if (!saveLocal()) return
        if (draft && levelID > 0 && source?.isEditing != true) {
            showMessage("公開済みの譜面を編集中に戻すことはできません。端末保存、または本投稿を使ってください。"); return
        }
        askPassword(if (levelID == 0) "投稿パスワードの設定" else "投稿パスワードの確認", "${if (draft) "編集中として仮保存" else "本投稿"}します。${if (levelID == 0) "このパスワードは今後の編集・削除に必要です。" else ""}") { pass ->
            AlertDialog.Builder(this).setTitle(if (draft) "仮保存" else "本投稿").setMessage("${music.title}\n${doc.tags["Creator"]}\n${if (doc.level == 100) "FULL" else "★${doc.level}"}\nサーバーへ送信しますか？")
                .setPositiveButton("送信") { _, _ -> runBusy {
                    val response = EditorApi.save(music, levelID, doc, pass, draft)
                    if (!EditorApi.succeeded(response)) { showMessage(EditorApi.errorMessage(response)); return@runBusy }
                    password = pass; sessionInvalidated = true
                    storage.delete(); completed = true
                    val refreshError = refresh()
                    showMessage("${if (draft) "仮保存" else "本投稿"}しました。${if (refreshError != null) "一覧の更新に失敗したため、選曲画面を開き直してください。" else ""}") { setResult(RESULT_OK); finish() }
                } }.setNegativeButton("キャンセル", null).show()
        }
    }
    private fun deleteRemote() {
        if (levelID <= 0) { showMessage("まだサーバーに投稿していません。端末の下書きは「下書きを破棄」で削除できます。"); return }
        askPassword("データベースから削除", "この譜面の投稿パスワードを入力してください。") { pass ->
            AlertDialog.Builder(this).setTitle("削除の確認").setMessage("本当にこの譜面をデータベースから削除しますか？")
                .setPositiveButton("削除") { _, _ -> runBusy {
                    val response = EditorApi.delete(levelID, pass)
                    if (!EditorApi.succeeded(response)) { showMessage(EditorApi.errorMessage(response)); return@runBusy }
                    storage.delete(); completed = true; sessionInvalidated = true
                    val error = refresh()
                    showMessage("削除しました。${if (error != null) "一覧は選曲画面を開き直して更新してください。" else ""}") { setResult(RESULT_OK); finish() }
                } }.setNegativeButton("キャンセル", null).show()
        }
    }
    private suspend fun refresh(): String? = suspendCancellableCoroutine { continuation ->
        ServerDataHandler().DownloadMusicDataAndUserNameData { error -> if (continuation.isActive) continuation.resume(error) }
    }
    private fun runBusy(block: suspend () -> Unit) {
        if (busy || isFinishing) return
        busy = true; timeline.isEnabled = false; player?.playWhenReady = false
        scope.launch {
            try { block() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { showMessage("処理に失敗しました。端末の下書きは残っています。\n${e.message}") }
            finally { busy = false; timeline.isEnabled = true; if (!isFinishing) updateStatus() }
        }
    }
    private fun showMessage(message: String, after: (() -> Unit)? = null) {
        if (!isFinishing && !isDestroyed) AlertDialog.Builder(this).setMessage(message).setPositiveButton("OK") { _, _ -> after?.invoke() }
            .apply { if (after != null) setCancelable(false) }.show()
    }
    private fun help() = showMessage("1.「歌詞」から歌詞を挿入します。漢字の読みをひらがなへ変換できます。\n2.動画を再生し、歌に合わせて「時刻を付ける」を押します。次のかなへカーソルが進みます。\n3.「ノーツ」をONにしてフリック対象を指定します。再生中は近い時刻の文字、停止中は選択文字に付きます。\n4.試しプレイで速度・オフセットを調整し、メニューから本投稿します。\n\nタップ：選択 / 長押し：文字・時刻編集 / ダブルタップ：その時刻へ移動\nドラッグ：歌詞をスクロール / ピンチ：拡大縮小\nUndo/Redo：操作を取り消す・やり直す\n下書きは10秒ごとと画面を離れる時に端末へ保存します。")
    override fun onBackPressed() {
        editorGame?.let { it.close(); return }
        if (busy) { Toast.makeText(this, "通信処理が終わるまでお待ちください", Toast.LENGTH_SHORT).show(); return }
        if (completed || document == null) { finish(); return }
        if (!saveLocal()) return
        if (source?.isEditing == true || levelID == 0) {
            AlertDialog.Builder(this).setTitle("編集を終了")
                .setMessage("下書きは端末に保存しました。サーバーにも編集中として仮保存しますか？")
                .setPositiveButton("仮保存") { _, _ -> submit(true) }
                .setNeutralButton("端末保存で戻る") { _, _ -> finish() }
                .setNegativeButton("編集を続ける", null).show()
        } else finish()
    }
    override fun onResume() { super.onResume(); resumed = true; handler.post(ticker); handler.postDelayed(saveTimer, 10000) }
    override fun onPause() {
        editorGame?.close()
        positionMs = player?.currentPosition ?: positionMs
        saveLocal(false); resumed = false; player?.playWhenReady = false
        handler.removeCallbacks(ticker); handler.removeCallbacks(saveTimer); super.onPause()
    }
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null); scope.cancel(); player?.playWhenReady = false
        if (::playerView.isInitialized) playerView.player = null
        player = null; super.onDestroy()
    }
    override fun onSaveInstanceState(out: Bundle) { saveLocal(false); out.putString("editorExportText", exportText); super.onSaveInstanceState(out) }
    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
        super.onActivityResult(request, result, data)
        if (result != RESULT_OK) return
        when (request) {
            201 -> data?.data?.let { uri -> runBusy {
                val text = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { reader ->
                        val buffer = CharArray(250001); var count = 0
                        while (count < buffer.size) { val n = reader.read(buffer, count, buffer.size - count); if (n < 0) break; count += n }
                        require(count <= 250000) { "ファイルは25万文字以下にしてください" }; String(buffer, 0, count).removePrefix("\uFEFF")
                    }
                }
                if (text != null) lyricsDialog(false, text)
            } }
            202 -> data?.data?.let { uri ->
                val text = exportText ?: document?.export(EditorDocument.Export.LOCAL) ?: return
                runBusy { withContext(Dispatchers.IO) { contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray(Charsets.UTF_8)) } ?: error("ファイルを開けませんでした") }; showMessage("テキストを書き出しました。") }
            }

        }
    }
}
