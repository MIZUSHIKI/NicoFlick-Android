package com.mizushiki.nicoflick_a

import android.service.autofill.UserData
import java.lang.Integer.max
import kotlin.properties.Delegates

class musicData {
    var sqlID:Int = 0
    var movieURL:String = ""
    var thumbnailURL:String = ""
        get() { return field }
        set(value) { field = value.pregReplace("http://", "https://") }
    var title:String = ""
    var artist:String = ""
    var movieLength:String = ""
    var tags:String = ""
    var tag:List<String> = listOf()
    var sqlUpdateTime:Int = 0
    var sqlCreateTime:Int = 0

    var favoriteCount:Int = 0
    var favoriteCountTime:Int = 0

    var levelIDs:MutableSet<Int> = mutableSetOf()
}

class levelData {
    var sqlID:Int  by Delegates.notNull()
    var level:Int  by Delegates.notNull()
    // 参考難易度。0 は未提供（表示しない）。既存の level とは独立して扱う。
    var calcLv:Int = 0
    var creator:String  by Delegates.notNull()
    var description:String  by Delegates.notNull()
    var speed:Int  by Delegates.notNull()
    var noteData:String  by Delegates.notNull()
    var sqlUpdateTime:Int  by Delegates.notNull()
    var sqlCreateTime:Int  by Delegates.notNull()
    var playCount:Int  by Delegates.notNull()
    var playCountTime:Int  by Delegates.notNull()
    var favoriteCount:Int  by Delegates.notNull()
    var favoriteCountTime:Int  by Delegates.notNull()
    var commentTime:Int  by Delegates.notNull()
    var scoreTime:Int  by Delegates.notNull()

    var likeCount:Int = 0
    var dislikeCount:Int = 0
    var likeCountTime:Int = 0

    fun getLevelAsString() : String {
        var star = ""
        if(level>10) {
            return "FULL"
        }
        for(i in 0 until 10){
            if(i<level){
                star += "★"
            }else {
                star += "☆"
            }
        }
        return star
    }

    val isEditing: Boolean
        get() = Regex("【編集中:?\\w*】").containsMatchIn(description)
    val isMyEditing: Boolean
        get() {
            if(isEditing){
                val author = Regex("【編集中:?(\\w*)】").find(description)?.groupValues?.get(1) ?: return false
                return author == USERDATA.UserID.take(8) || author == USERDATA.UserID.drop(24)
            }
            return false
        }
}

object MusicDataLists {
    var musics: ArrayList<musicData> = arrayListOf()
    var levels: MutableMap<String,ArrayList<levelData>> = mutableMapOf()
    var taglist: MutableMap<String,Int> = mutableMapOf()
    var levelsSqlIDtoMovieURL: MutableMap<Int,String> = mutableMapOf()

    fun reset(){
        musics = arrayListOf()
        levels = mutableMapOf()
        taglist = mutableMapOf()
        levelsSqlIDtoMovieURL = mutableMapOf()
    }

    fun setMusic(sqlID:Int, movieURL:String, thumbnailURL:String, title:String, artist:String, movieLength:String, tags:String, updateTime:Int, createTime:Int, favoriteCount:Int = 0, favoriteCountTime:Int = 0){
        if(movieURL=="delete" || thumbnailURL == "delete"){
            //削除
            for(index in 0 until musics.size){
                if(musics[index].sqlID == sqlID){
                    musics.removeAt(index)
                    break
                }
            }
            return
        }
        val movieURL = movieURL.pregReplace("\\?.*$","")
        for(index in 0 until musics.size){
            if(musics[index].sqlID == sqlID){
                musics[index].movieURL = movieURL
                musics[index].thumbnailURL = thumbnailURL
                musics[index].title = title
                musics[index].artist = artist
                musics[index].movieLength = movieLength
                musics[index].tags = tags
                musics[index].tag = tags.trim().split(" ")
                musics[index].sqlUpdateTime = updateTime
                musics[index].sqlCreateTime = createTime
                musics[index].favoriteCount = favoriteCount
                musics[index].favoriteCountTime = favoriteCountTime
                return
            }
        }
        val musicdata = musicData()
        musicdata.sqlID = sqlID
        musicdata.movieURL = movieURL
        musicdata.thumbnailURL = thumbnailURL
        musicdata.title = title
        musicdata.artist = artist
        musicdata.movieLength = movieLength
        musicdata.tags = tags
        musicdata.tag = tags.trim().split(" ")
        musicdata.sqlUpdateTime = updateTime
        musicdata.sqlCreateTime = createTime
        musicdata.favoriteCount = favoriteCount
        musicdata.favoriteCountTime = favoriteCountTime
        musics.add(musicdata)
    }

    fun setLevel(sqlID:Int, movieURL:String, level:Int, creator:String, description:String, speed:Int, noteData:String, updateTime:Int, createTime:Int, playCount:Int, playCountTime:Int, favoriteCount:Int, favoriteCountTime:Int, commentTime:Int, scoreTime:Int, likeCount:Int = 0, dislikeCount:Int = 0, likeCountTime:Int = 0, calcLv:Int = 0){
        if(level==-1){
            //削除
            //musicsの逆引き用の levelIDsから指定IDを削除する
            for( musicdata in musics){
                if( musicdata.movieURL == movieURL ){
                    musicdata.levelIDs.remove(sqlID)
                    break
                }
            }
            //levelsの
            if(levels[movieURL] != null) {
                levels[movieURL] = ArrayList(levels[movieURL]!!.filter { leveldata -> leveldata.sqlID != sqlID })
            }
            if( levelsSqlIDtoMovieURL[sqlID] != null ){
                levelsSqlIDtoMovieURL.remove(sqlID)
            }
            return
        }
        for(music in musics){
            if(music.movieURL == movieURL) {
                music.levelIDs.add(sqlID)
                break
            }
        }
        if(levels[movieURL] != null) {
            levelsSqlIDtoMovieURL[sqlID] = movieURL
            for(leveldata in levels[movieURL]!!){
                if(leveldata.sqlID == sqlID){
                    leveldata.level = level
                    leveldata.calcLv = calcLv
                    leveldata.creator = creator
                    leveldata.description = description
                    leveldata.speed = speed
                    leveldata.noteData = noteData
                    leveldata.sqlUpdateTime = updateTime
                    leveldata.sqlCreateTime = createTime
                    leveldata.playCount = playCount
                    leveldata.playCountTime = playCountTime
                    leveldata.favoriteCount = favoriteCount
                    leveldata.favoriteCountTime = favoriteCountTime
                    leveldata.commentTime = commentTime
                    leveldata.scoreTime = scoreTime
                    leveldata.likeCount = likeCount
                    leveldata.dislikeCount = dislikeCount
                    leveldata.likeCountTime = likeCountTime
                    return
                }
            }
        }
        val leveldata = levelData()
        leveldata.sqlID = sqlID
        leveldata.level = level
        leveldata.calcLv = calcLv
        leveldata.creator = creator
        leveldata.description = description
        leveldata.speed = speed
        leveldata.noteData = noteData
        leveldata.sqlUpdateTime = updateTime
        leveldata.sqlCreateTime = createTime
        leveldata.playCount = playCount
        leveldata.playCountTime = playCountTime
        leveldata.favoriteCount = favoriteCount
        leveldata.favoriteCountTime = favoriteCountTime
        leveldata.commentTime = commentTime
        leveldata.scoreTime = scoreTime
        leveldata.likeCount = likeCount
        leveldata.dislikeCount = dislikeCount
        leveldata.likeCountTime = likeCountTime
        levels[movieURL] = levels[movieURL] ?: arrayListOf()
        levels[movieURL]!!.add(leveldata)
        levelsSqlIDtoMovieURL[sqlID] = movieURL
    }

    fun setLevel_PlaycountFavorite(sqlID:Int, playCount:Int, playCountTime:Int, favoriteCount:Int, favoriteCountTime:Int, commentTime:Int, scoreTime:Int){
        levelsSqlIDtoMovieURL[sqlID] ?: return
        val movieURL = levelsSqlIDtoMovieURL[sqlID]!!
        levels[movieURL]?.let {
            val levelm = it
            val f = levelm.filter {
                    it.sqlID == sqlID
            }
            if( f.count() > 0 ){ // 実は１個しかない(ハズだ)からfirstで処理
                if( playCount != -1 ){
                    f.first()!!.playCount = playCount
                    f.first()!!.playCountTime = playCountTime
                }
                if( favoriteCount != -1 ){
                    f.first()!!.favoriteCount = favoriteCount
                    f.first()!!.favoriteCountTime = favoriteCountTime
                }
                if( commentTime != -1 ){
                    f.first()!!.commentTime = commentTime
                }
                if( scoreTime != -1 ){
                    f.first()!!.scoreTime = scoreTime
                }
                return
            }
        }
    }

    fun getLastUpdateTimeMusic() : Int {
        var time = 0
        for( musicdata in musics ){
            if( time < musicdata.sqlUpdateTime ){
                time = musicdata.sqlUpdateTime
            }
        }
        return time
    }
    fun getLastUpdateTimeLevel() : Int {
        var time = 0
        for( (_,leveldates) in levels ){
            for( leveldata in leveldates){
                if( time < leveldata.sqlUpdateTime ){
                    time = leveldata.sqlUpdateTime
                }
            }
        }
        return time
    }
    fun getLastPlayCountTimeLevel() : Int {
        var time = 0
        for( (_,leveldates) in levels ){
            for( leveldata in leveldates){
                if( time < leveldata.playCountTime ){
                    time = leveldata.playCountTime
                }
            }
        }
        return time
    }
    fun getLastFavoriteCountTimeLevel() : Int {
        var time = 0
        for( (_,leveldates) in levels ){
            for( leveldata in leveldates){
                if( time < leveldata.favoriteCountTime ){
                    time = leveldata.favoriteCountTime
                }
            }
        }
        return time
    }
    fun getLastCommentTimeLevel() : Int {
        var time = 0
        for( (_,leveldates) in levels ){
            for( leveldata in leveldates){
                if( time < leveldata.commentTime ){
                    time = leveldata.commentTime
                }
            }
        }
        return time
    }
    fun getLastScoreTimeLevel() : Int {
        var time = 0
        for( (_,leveldates) in levels ){
            for( leveldata in leveldates){
                if( time < leveldata.scoreTime ){
                    time = leveldata.scoreTime
                }
            }
        }
        return time
    }

    fun createTaglist(){
        taglist = mutableMapOf() //初期化
        for( music in musics ){
            if (!hasPlayableLevel(music.movieURL)) continue
            for( tag in music.tag ){
                if( tag == "" ){ continue }
                if( taglist[tag] != null){
                    taglist[tag] = taglist[tag]!! + 1
                }else {
                    taglist[tag] = 1
                }
            }
        }
    }

    fun getLevelIDtoMusicID(levelID:Int) : Int {
        levelsSqlIDtoMovieURL[levelID]?.let {
            val movieURL = it
            musics.firstOrNull { it.movieURL == movieURL }?.let {
                return it.sqlID
            }
        }
        return 0
    }

    fun getNotesFirstTime(movieURL: String) : Double {
        levels[movieURL]?.let {
            var timetag = ""
            for( levelData in it ){
                timetag = levelData.noteData.pregMatche_firstString("(\\[\\d\\d\\:\\d\\d[\\:|\\.]\\d\\d\\])")
                // println(timetag)
                if( timetag != "" ){
                    val time = timetag.timetagToSeconds()
                    if( time < 0.0 ){
                        return 0.0
                    }
                    return time
                }
            }
        }
        return -0.01
    }

    //
    fun getSelectMusics( callback:(ArrayList<musicData>) -> Unit ) {
        //val selectCondition = USERDATA.SelectedMusicCondition //userData読み出し
        val outputMusics = _getSelectMusics(selectCondition= USERDATA.SelectedMusicCondition)

        getSortedMusics(outputMusics, callback)
        return
    }
    fun _getSelectMusics( selectCondition:SelectConditions, isMe:Boolean = true ) : ArrayList<musicData> {
        val tagps:ArrayList<SelectConditions.tagp> = arrayListOf()
        var sita = 0
        var ue = 99 * 60 + 99
        for( tagp in selectCondition.tag ){
            if( tagp.word.startsWith("@g:") ){
                val ids = tagp.word.removePrefix("@g:").split("_", "-").mapNotNull{ it.toIntOrNull() }
                // @mへの変換
                for( id in ids ){
                    levelsSqlIDtoMovieURL[id]?.let {
                        val movieURL = it
                        musics.firstOrNull({ it.movieURL == movieURL })?.let {
                            val t = SelectConditions.tagp(word = "@m:${it.sqlID}", type = tagp.type)
                            tagps.add(t)
                        }
                    }
                }
                continue
            }else if( tagp.word.startsWith("@m:") ){
                val ids = tagp.word.removePrefix("@m:").split("_", "-").mapNotNull{ it.toIntOrNull() }
                tagps += ids.map{ SelectConditions.tagp(word = "@m:${it}", type = tagp.type) }
                continue
            }else if (tagp.word.startsWith("@sm:")) {
                val ids = tagp.word.removePrefix("@sm:").split("-").mapNotNull { it.toLongOrNull() }
                if (ids.size >= 2) {
                    for (music in musics) {
                        val number = music.movieURL.pregMatche_firstString("sm(\\d+)$").toLongOrNull() ?: continue
                        if (number in ids[0]..ids[1]) tagps.add(SelectConditions.tagp("@m:${music.sqlID}", tagp.type))
                    }
                }
                continue
            }else if( tagp.word.startsWith("@t:") ){
                var ans:ArrayList<String> = arrayListOf()
                if( tagp.word.pregMatche(pattern= "@t:(\\d+:\\d+)-(\\d+:\\d+)$", matche= ans) ){
                    sita = ans[1].timeToSec(0)
                    ue = ans[2].timeToSec(99*60+99)
                }else if( tagp.word.pregMatche(pattern= "@t:(\\d+:\\d+)-$", matche= ans) ){
                    sita = ans[1].timeToSec(0)
                }else if( tagp.word.pregMatche(pattern= "@t:-?(\\d+:\\d+)$", matche= ans) ){
                    ue = ans[1].timeToSec(99*60+99)
                }
                if( sita > ue ){
                    val swap = sita
                    ue = sita
                    sita = swap
                }
                // println("sita=$sita, ue=$ue")
                continue
            }else if( tagp.word == "@初期楽曲" ){
                //
            }else if( tagp.word.startsWith("@") ){
                val w = tagp.word.removePrefix("@")
                //if let music =
                musics.firstOrNull({ it.movieURL.endsWith(w) })?.let{
                    val t = SelectConditions.tagp(word = "@m:${it.sqlID}", type = tagp.type)
                    tagps.add(t)
                }
                continue
            }
            tagps.add(tagp)
        }

        var extractMusics:ArrayList<musicData> = arrayListOf()
        // println("selectCondition.tag.size="+tagps.size)
        // println("musics.size="+ musics.size)
        if( tagps.size == 0 ){
            extractMusics = musics.clone() as ArrayList<musicData>
        }else {
            var remainMusics = musics.clone() as ArrayList<musicData>

            loop@ for( tagp in tagps ){
                //println(""+tagp.type+", "+tagp.word)
                when( tagp.type ){
                    "or" -> {
                        val rmCount = remainMusics.size
                        //println("rmCount="+rmCount)
                        if( rmCount == 0 ){
                            continue@loop
                        }
                        for( bindex in 1 .. rmCount ){
                            val index = rmCount - bindex
                            //println(remainMusics[index].title+" - remainMusics[$index].tag.contains(${tagp.word})="+remainMusics[index].tag.contains(tagp.word))
                            if( tagp.word == "@初期楽曲" ) {

                                if (remainMusics[index].sqlID > 14) {
                                    continue
                                }
                            }else if( tagp.word.startsWith("@m:") ){
                                if( Int(tagp.word.removePrefix("@m:")) != remainMusics[index].sqlID ){
                                    continue
                                }
                            }else if( !remainMusics[index].tag.contains(tagp.word) ){
                                continue
                            }
                            //println("たす")
                            val r = remainMusics[index]
                            extractMusics.add(r)
                            //remainMusics.filterIndexed { i, musicData -> i != index }
                            remainMusics.removeAt(index)
                        }
                    }
                    "and" -> {
                        val emCount = extractMusics.size
                        if( emCount == 0 ){
                            continue@loop
                        }
                        for( bindex in 1 .. emCount ){
                            val index = emCount - bindex
                            if( tagp.word == "@初期楽曲" ){
                                if( extractMusics[index].sqlID <= 14 ){
                                    continue
                                }
                            }else if( tagp.word.startsWith("@m:") ){
                                if( Int(tagp.word.removePrefix("@m:")) == extractMusics[index].sqlID ){
                                    continue
                                }
                            }else if( extractMusics[index].tag.contains(tagp.word) ){
                                continue
                            }
                            val r = extractMusics[index]
                            remainMusics.add(r)
                            extractMusics.removeAt(index)
                        }
                    }
                    "-" -> {
                        if( tagp.word == "@初期楽曲" ) {
                            extractMusics = ArrayList(extractMusics.filter { it.sqlID > 14 })
                            remainMusics = ArrayList(remainMusics.filter { it.sqlID > 14 })
                        }else if( tagp.word.startsWith("@m:") ){
                            Int(tagp.word.removePrefix("@m:"))?.let {
                                val id = it
                                extractMusics = ArrayList(extractMusics.filter { (it.sqlID != id) })
                                remainMusics = ArrayList(remainMusics.filter { it.sqlID != id })
                            }
                        }else{
                            extractMusics = ArrayList(extractMusics.filter{!it.tag.contains(tagp.word)})
                            remainMusics = ArrayList(remainMusics.filter{!it.tag.contains(tagp.word)})
                        }
                    }
                }
            }
        }
        if( sita != 0 || ue != 99*60+99 ){
            extractMusics = ArrayList(extractMusics.filter { it.movieLength.timeToSec() in sita..ue })
        }
        // 【編集中：】でレベル数が0になる場合、その musicは弾いて表示させないようにする。
        val outputMusics:ArrayList<musicData> = arrayListOf()
        for( musicdata in extractMusics ){
            if( getSelectMusicLevels_noSort(musicdata.movieURL, isMe).size == 0 ){
                continue
            }
            var flg = false
            levels[musicdata.movieURL]?.let {
                if(it.filter {
                    if( it.level > 10 ){
                        return@filter selectCondition.sortStars[10]
                    }
                    if( it.level <= 0 ){
                        return@filter false
                    }
                        return@filter selectCondition.sortStars[it.level-1]
                }.size == 0){
                    flg = true
                }
            }
            if(flg){
                continue
            }
            outputMusics.add(musicdata)
            //println(musicdata.title)
        }
        return outputMusics
    }

    fun getSortedMusics(musics:ArrayList<musicData>, callback:(ArrayList<musicData>) -> Unit) {
        val selectCondition = USERDATA.SelectedMusicCondition //userData読み出し

        var sortedMusics:ArrayList<musicData> = musics//arrayListOf()
        when( selectCondition.sortItem.pregReplace(" [★☆]{10}[■□]$","") ){
            "曲の投稿日時 降順" -> musics.sortBy{ it.sqlCreateTime * -1 }
            "曲の投稿日時 昇順" -> musics.sortBy { it.sqlCreateTime }
            "ゲームの投稿日時 降順" -> {
                val levelp:MutableMap<String,Int> = mutableMapOf() //[movieURL,sqlCreateTime]
                for( (key,lvInURL) in levels ){
                    for( leveldata in lvInURL ){
                        if( leveldata.isEditing && !leveldata.isMyEditing ){
                            continue
                        }
                        if( levelp[key] == null ){
                            levelp[key] = leveldata.sqlCreateTime
                        }else {
                            if( levelp[key]!! < leveldata.sqlCreateTime ) {
                                levelp[key] = leveldata.sqlCreateTime
                            }
                        }
                    }
                }
                sortedMusics = ArrayList(musics.filter{ levelp.keys.contains(it.movieURL)})
                sortedMusics.sortByDescending { levelp[it.movieURL]!! }
            }
            "ゲームの投稿日時 昇順" -> {
                val levelp:MutableMap<String,Int> = mutableMapOf() //[movieURL,sqlCreateTime]
                for( (key,lvInURL) in levels ){
                    for( leveldata in lvInURL ){
                        if( leveldata.isEditing && !leveldata.isMyEditing ){
                            continue
                        }
                        if( levelp[key] == null ){
                            levelp[key] = leveldata.sqlCreateTime
                        }else {
                            if( levelp[key]!! > leveldata.sqlCreateTime ) {
                                levelp[key] = leveldata.sqlCreateTime
                            }
                        }
                    }
                }
                sortedMusics = ArrayList(musics.filter{ levelp.keys.contains(it.movieURL)})
                sortedMusics.sortBy { levelp[it.movieURL]!! }
            }
            "みんなのプレイ回数が多い曲順" -> {
                val levelp:MutableMap<String,Int> = mutableMapOf() //[URL,allPlayCount]
                for( (key,lvInURL) in levels ){
                    levelp[key] = lvInURL.fold(0){sum,leveldata -> sum + leveldata.playCount}
                }
                sortedMusics = ArrayList(musics.filter{ levelp.keys.contains(it.movieURL)})
                sortedMusics.sortBy { levelp[it.movieURL]!! * -1 }
            }
            "みんなのプレイ回数が少ない曲順" -> {
                val levelp:MutableMap<String,Int> = mutableMapOf() //[URL,allPlayCount]
                for( (key,lvInURL) in levels ){
                    levelp[key] = lvInURL.fold(0){sum,leveldata -> sum + leveldata.playCount}
                }
                sortedMusics = ArrayList(musics.filter{ levelp.keys.contains(it.movieURL)})
                sortedMusics.sortBy { levelp[it.movieURL]!! }
            }
            "最近プレイした曲順" -> {
                val histories = USERDATA.levelPlayHistories
                val lastPlayedByURL = levels.mapValues { (_, musicLevels) ->
                    musicLevels.fold(0L) { latest, level ->
                        maxOf(latest, histories[level.sqlID]?.lastPlayedAt?.time ?: 0L)
                    }
                }
                sortedMusics = ArrayList(musics.sortedByDescending { lastPlayedByURL[it.movieURL] ?: 0L })
            }
            "あなたのプレイ回数が多い曲順" -> {
                val histories = USERDATA.levelPlayHistories
                val playCountByURL = levels.mapValues { (_, musicLevels) ->
                    musicLevels.fold(0L) { count, level ->
                        count + (histories[level.sqlID]?.playCount ?: 0).toLong()
                    }
                }
                sortedMusics = ArrayList(musics.sortedByDescending { playCountByURL[it.movieURL] ?: 0L })
            }
            "最近ハイスコアが更新された曲順" -> {
                val levelp:MutableMap<String,Int> = mutableMapOf() //[URL,allPlayCount]
                for( (key,lvInURL) in levels ){
                    levelp[key] = lvInURL.fold(0){sum,leveldata -> max(sum, leveldata.scoreTime)}
                }
                sortedMusics = ArrayList(musics.filter{ levelp.keys.contains(it.movieURL)})
                sortedMusics.sortBy { levelp[it.movieURL]!! * -1 }
            }
            "最近コメントされた曲順" -> {
                val levelp:MutableMap<String,Int> = mutableMapOf() //[URL,allPlayCount]
                for( (key,lvInURL) in levels ){
                    levelp[key] = lvInURL.fold(0){sum,leveldata -> max(sum, leveldata.commentTime)}
                }
                sortedMusics = ArrayList(musics.filter{ levelp.keys.contains(it.movieURL)})
                sortedMusics.sortBy { levelp[it.movieURL]!! * -1 }
            }
            "お気に入り数が多い曲順" -> {
                sortedMusics = ArrayList(musics.sortedByDescending { it.favoriteCount })
            }
            "お気に入り数が少ない曲順" -> {
                sortedMusics = ArrayList(musics.sortedBy { it.favoriteCount })
            }
            "動画ID 降順" -> {
                val nonDigits = Regex("[^0-9]")
                val videoIDs = musics.associate { it.movieURL to nonDigits.replace(it.movieURL, "").toInt() }
                musics.sortBy { videoIDs.getValue(it.movieURL) * -1 }
            }
            "動画ID 昇順" -> {
                val nonDigits = Regex("[^0-9]")
                val videoIDs = musics.associate { it.movieURL to nonDigits.replace(it.movieURL, "").toInt() }
                musics.sortBy { videoIDs.getValue(it.movieURL) }
            }
            "タグで選んだ順" -> {
                sortedMusics = musics
            }
            else -> sortedMusics = musics
        }
        //《編集中》 levels[$0.movieURL] のすべてのdescriptionを調べて【非表示】が含まれるものを排除したとき、lelvelの数が0ならmusicもフィルタリングで除外される
        //sortedMusics = sortedMusics.filter({ levels[$0.movieURL]!.count>0 })

        //お気に入りを先頭に集める
        if( USERDATA.MusicSortCondition == 1 ){
            val favorites = USERDATA.MyFavorite3
            sortedMusics = ArrayList(sortedMusics.filter { it.sqlID in favorites } + sortedMusics.filter { it.sqlID !in favorites })
        }

        callback(sortedMusics)
    }

    //とりあえずレベル取り出しを作った。現状level順ソートだけしか必要無い。
    fun getSelectMusicLevels(selectMovieURL:String) : ArrayList<levelData>{
        if( USERDATA.LevelSortCondition == 0 ){
            val sortedLevels = ArrayList(getSelectMusicLevels_noSort(selectMovieURL).filter {
                if( it.level > 10 ){
                    return@filter USERDATA.SelectedMusicCondition.sortStars[10]
                }
                if( it.level <= 0 ){
                    return@filter false
                }
                return@filter USERDATA.SelectedMusicCondition.sortStars[it.level-1]
            })
            sortedLevels.sortBy { it.level }
            return sortedLevels
        }else {
            var a:MutableList<levelData> = mutableListOf()
            var b:MutableList<levelData> = mutableListOf()
            val myLikes = USERDATA.MyLike // 保存済みLike一覧の解析は並べ替えごとに1回だけ。
            for( level in getSelectMusicLevels_noSort(selectMovieURL).filter {
                if( it.level > 10 ){
                    return@filter USERDATA.SelectedMusicCondition.sortStars[10]
                }
                if( it.level <= 0 ){
                    return@filter false
                }
                return@filter USERDATA.SelectedMusicCondition.sortStars[it.level-1]
            } ){
                if (myLikes[level.sqlID]?.let { it == -2 || it > 0 } == true) {
                    a.add(level)
                }else {
                    b.add(level)
                }
            }
            a.sortBy { it.level }
            b.sortBy { it.level }
            return ArrayList( a + b )
        }
    }
    fun getSelectMusicLevels_noSort(selectMovieURL:String, isMe:Boolean = true) : ArrayList<levelData>{
        val selectLevels:ArrayList<levelData> = arrayListOf()
        if( levels[selectMovieURL] == null ) {
            return selectLevels
        }
        for( leveldata in levels[selectMovieURL]!!){
            if( leveldata.isEditing && !(leveldata.isMyEditing && isMe) ){
                continue
            }
            selectLevels.add(leveldata)
        }
        return selectLevels
    }

    fun hasPlayableLevel(selectMovieURL:String, isMe:Boolean = true):Boolean =
        levels[selectMovieURL]?.any { !it.isEditing || (isMe && it.isMyEditing) } == true

    fun setMusic_Favoritecount(sqlID:Int, favoriteCount:Int, favoriteCountTime:Int) {
        val music = musics.firstOrNull { it.sqlID == sqlID } ?: return
        if (favoriteCount != -1) {
            music.favoriteCount = favoriteCount
            music.favoriteCountTime = favoriteCountTime
        }
    }
    fun getLastFavoriteCountTimeMusic():Int = musics.map { it.favoriteCountTime }.maxOrNull() ?: 0
    fun getLastLikeCountTimeLevel():Int = levels.values.flatten().map { it.likeCountTime }.maxOrNull() ?: 0
    fun setLevel_PlaycountLike(sqlID:Int, playCount:Int, playCountTime:Int, commentTime:Int, scoreTime:Int,
                              likeCount:Int, dislikeCount:Int, likeCountTime:Int) {
        val level = levels.values.asSequence()
            .flatMap { it.asSequence() }
            .firstOrNull { it.sqlID == sqlID } ?: return
        if (playCount != -1) { level.playCount = playCount; level.playCountTime = playCountTime }
        if (likeCount != -1) level.likeCount = likeCount
        if (dislikeCount != -1) level.dislikeCount = dislikeCount
        if (likeCount != -1 || dislikeCount != -1) level.likeCountTime = likeCountTime
        if (commentTime != -1) level.commentTime = commentTime
        if (scoreTime != -1) level.scoreTime = scoreTime
    }

    // musics Json化。 USERDATAに保存用
    // 永続化はMetadataStorageがSQLiteの行として行う。

}