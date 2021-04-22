package com.mizushiki.nicoflick_a

import android.service.autofill.UserData
import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonArray
import java.lang.Integer.max
import kotlin.properties.Delegates

class musicData {
    var sqlID:Int = 0
    var movieURL:String = ""
    var thumbnailURL:String = ""
    var title:String = ""
    var artist:String = ""
    var movieLength:String = ""
    var tags:String = ""
    var tag:List<String> = listOf()
    var sqlUpdateTime:Int = 0
    var sqlCreateTime:Int = 0

    var levelIDs:MutableSet<Int> = mutableSetOf()
}

class levelData {
    var sqlID:Int  by Delegates.notNull()
    var level:Int  by Delegates.notNull()
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
        get() = Regex("【編集中:?\\w*】").matches(description)
    val isMyEditing: Boolean
        get() {
            if(isEditing){
                return Regex("【編集中:?(\\w*)").find(description)!!.groupValues.get(1) == USERDATA.UserID.substring(24)
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

    fun setMusic(sqlID:Int, movieURL:String, thumbnailURL:String, title:String, artist:String, movieLength:String, tags:String, updateTime:Int, createTime:Int){
        if(movieURL=="delete"){
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
        musics.add(musicdata)
    }

    fun setLevel(sqlID:Int, movieURL:String, level:Int, creator:String, description:String, speed:Int, noteData:String, updateTime:Int, createTime:Int, playCount:Int, playCountTime:Int, favoriteCount:Int, favoriteCountTime:Int, commentTime:Int, scoreTime:Int){
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
                    return
                }
            }
        }
        val leveldata = levelData()
        leveldata.sqlID = sqlID
        leveldata.level = level
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
                println(timetag)
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
        for( tagp in selectCondition.tag ){
            if( tagp.word.startsWith("@g:") ){
                val ids = tagp.word.removePrefix("@g:").split("-").map{ Int(it) }
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
                val ids = tagp.word.removePrefix("@m:").split("-").map{ Int(it) }
                tagps += ids.map{ SelectConditions.tagp(word = "@m:${it}", type = tagp.type) }
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
        println("selectCondition.tag.size="+tagps.size)
        println("musics.size="+ musics.size)
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
            "曲の投稿が新しい順" -> musics.sortBy{ it.sqlCreateTime * -1 }
            "曲の投稿が古い順" -> musics.sortBy { it.sqlCreateTime }
            "ゲームの投稿が新しい曲順" -> {
                val levelp:MutableMap<String,Int> = mutableMapOf() //[movieURL,sqlID]
                for( (key,lvInURL) in levels ){
                    for( leveldata in lvInURL ){
                        if( leveldata.isEditing && !leveldata.isMyEditing ){
                            continue
                        }
                        if( levelp[key] == null ){
                            levelp[key] = leveldata.sqlID
                        }else {
                            if( levelp[key]!! < leveldata.sqlID ) {
                                levelp[key] = leveldata.sqlID
                            }
                        }
                    }
                }
                sortedMusics = ArrayList(musics.filter{ levelp.keys.contains(it.movieURL)})
                sortedMusics.sortBy { levelp[it.movieURL]!! * -1 }
            }
            "ゲームの投稿が古い曲順" -> {
                val levelp:MutableMap<String,Int> = mutableMapOf() //[movieURL,sqlID]
                for( (key,lvInURL) in levels ){
                    for( leveldata in lvInURL ){
                        if( leveldata.isEditing && !leveldata.isMyEditing ){
                            continue
                        }
                        if( levelp[key] == null ){
                            levelp[key] = leveldata.sqlID
                        }else {
                            if( levelp[key]!! > leveldata.sqlID ) {
                                levelp[key] = leveldata.sqlID
                            }
                        }
                    }
                }
                sortedMusics = ArrayList(musics.filter{ levelp.keys.contains(it.movieURL)})
                sortedMusics.sortBy { levelp[it.movieURL]!! }
            }
            "ゲームプレイ回数が多い曲順" -> {
                val levelp:MutableMap<String,Int> = mutableMapOf() //[URL,allPlayCount]
                for( (key,lvInURL) in levels ){
                    levelp[key] = lvInURL.fold(0){sum,leveldata -> sum + leveldata.playCount}
                }
                sortedMusics = ArrayList(musics.filter{ levelp.keys.contains(it.movieURL)})
                sortedMusics.sortBy { levelp[it.movieURL]!! * -1 }
            }
            "ゲームプレイ回数が少ない曲順" -> {
                val levelp:MutableMap<String,Int> = mutableMapOf() //[URL,allPlayCount]
                for( (key,lvInURL) in levels ){
                    levelp[key] = lvInURL.fold(0){sum,leveldata -> sum + leveldata.playCount}
                }
                sortedMusics = ArrayList(musics.filter{ levelp.keys.contains(it.movieURL)})
                sortedMusics.sortBy { levelp[it.movieURL]!! }
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
                val levelp:MutableMap<String,Int> = mutableMapOf() //[URL,allPlayCount]
                for( (key,lvInURL) in levels ){
                    levelp[key] = lvInURL.fold(0){sum,leveldata -> sum + leveldata.favoriteCount}
                }
                sortedMusics = ArrayList(musics.filter{ levelp.keys.contains(it.movieURL)})
                sortedMusics.sortBy { levelp[it.movieURL]!! * -1 }
            }
            "お気に入り数が少ない曲順" -> {
                val levelp:MutableMap<String,Int> = mutableMapOf() //[URL,allPlayCount]
                for( (key,lvInURL) in levels ){
                    levelp[key] = lvInURL.fold(0){sum,leveldata -> sum + leveldata.favoriteCount}
                }
                sortedMusics = ArrayList(musics.filter{ levelp.keys.contains(it.movieURL)})
                sortedMusics.sortBy { levelp[it.movieURL]!! }
            }
            "動画IDが大きい順" -> musics.sortBy{ it.movieURL.pregReplace("[^0-9]","").toInt() * -1 }
            "動画IDが小さい順" -> musics.sortBy { it.movieURL.pregReplace("[^0-9]","").toInt() }
            "タグで選んだ順" -> {
                sortedMusics = musics
            }
            else -> sortedMusics = musics
        }
        //《編集中》 levels[$0.movieURL] のすべてのdescriptionを調べて【非表示】が含まれるものを排除したとき、lelvelの数が0ならmusicもフィルタリングで除外される
        //sortedMusics = sortedMusics.filter({ levels[$0.movieURL]!.count>0 })

        //お気に入りを先頭に集める
        if( USERDATA.MusicSortCondition == 1 ){
            val levelp:MutableMap<String,Boolean> = mutableMapOf() //[URL,allPlayCount]
            for( (key,lvInURL) in levels ){
                levelp[key] = false
                for( level in lvInURL ){
                    if( USERDATA.MyFavoriteAll.contains(level.sqlID) ){
                        levelp[key] = true
                        break
                    }
                }
            }
            sortedMusics = ArrayList(sortedMusics.filter { levelp[it.movieURL]!! } + sortedMusics.filter { !levelp[it.movieURL]!! } )
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
            for( level in getSelectMusicLevels_noSort(selectMovieURL).filter {
                if( it.level > 10 ){
                    return@filter USERDATA.SelectedMusicCondition.sortStars[10]
                }
                if( it.level <= 0 ){
                    return@filter false
                }
                return@filter USERDATA.SelectedMusicCondition.sortStars[it.level-1]
            } ){
                if( USERDATA.MyFavoriteAll.contains(level.sqlID) ){
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

    // musics Json化。 USERDATAに保存用
    fun toMusicsJsonString() : String{
        val jArrary = Json.array()
        for( musicdata in this.musics ){
            val jObject = Json.`object`()
                .add("id",musicdata.sqlID.toString())
                .add("movieURL",musicdata.movieURL)
                .add("thumbnailURL",musicdata.thumbnailURL)
                .add("title",musicdata.title)
                .add("artist",musicdata.artist)
                .add("movieLength",musicdata.movieLength)
                .add("tags",musicdata.tags)
                .add("updateTime",musicdata.sqlUpdateTime.toString())
                .add("createTime",musicdata.sqlCreateTime.toString())

            jArrary.add(jObject)
        }
        println("json output")
        println(jArrary.toString())

        return jArrary.toString()
    }
    // 保存データから読み込み
    fun loadMusicsJsonString(jsonStr:String){
        if(jsonStr==""){ return }
        val jsonArray: JsonArray
        try {
            jsonArray = Json.parse(jsonStr).asArray()
        }catch (e:Exception){
            println(e)
            return
        }
        // ロードした musicデータを処理
        for (i in 0 until jsonArray.size()) {
            val json = jsonArray.get(i).asObject()
            this.setMusic(
                sqlID = json.get("id").asString().toInt(),
                movieURL = json.get("movieURL").asString(),
                thumbnailURL = json.get("thumbnailURL").asString(),
                title = json.get("title").asString(),
                artist = json.get("artist").asString(),
                movieLength = json.get("movieLength").asString(),
                tags = json.get("tags").asString(),
                updateTime = json.get("updateTime").asString().toInt(),
                createTime = json.get("createTime").asString().toInt()
            )
        }
        this.createTaglist()
    }

    // levels Json化。 USERDATAに保存用
    fun toLevelsJsonString() : String{
        val jArrary = Json.array()
        for( (movieURL,leveldatas) in this.levels ){
            for( leveldata in leveldatas ){

                //(sqlID:Int, movieURL:String, level:Int, creator:String, description:String, speed:Int, noteData:String, updateTime:Int, createTime:Int, playCount:Int)
                val jObject = Json.`object`()
                    .add("id",leveldata.sqlID.toString())
                    .add("movieURL",movieURL)
                    .add("level",leveldata.level.toString())
                    .add("creator",leveldata.creator)
                    .add("description",leveldata.description)
                    .add("speed",leveldata.speed.toString())
                    .add("notes",leveldata.noteData)
                    .add("updateTime",leveldata.sqlUpdateTime.toString())
                    .add("createTime",leveldata.sqlCreateTime.toString())
                    .add("playCount",leveldata.playCount.toString())
                    .add("playCountTime",leveldata.playCountTime.toString())
                    .add("favorite",leveldata.favoriteCount.toString())
                    .add("favoriteTime",leveldata.favoriteCountTime.toString())
                    .add("commentTime",leveldata.commentTime.toString())
                    .add("scoreTime",leveldata.scoreTime.toString())

                jArrary.add(jObject)
            }
        }
        println("json output")
        println(jArrary.toString())

        return jArrary.toString()
    }
    // 保存データから読み込み
    fun loadLevelsJsonString(jsonStr:String){
        if(jsonStr==""){ return }
        val jsonArray: JsonArray
        try {
            jsonArray = Json.parse(jsonStr).asArray()
        }catch (e:Exception){
            println(e)
            return
        }
        // ロードした levelデータを処理
        for (i in 0 until jsonArray.size()) {
            val json = jsonArray.get(i).asObject()
            //下位互換
            val playCountTime = json.get("playCountTime")?.asString()?.toInt() ?: 0
            val favoriteCount =  json.get("favorite")?.asString()?.toInt() ?: 0
            val favoriteCountTime = json.get("favoriteTime")?.asString()?.toInt() ?: 0
            val commentTime = json.get("commentTime")?.asString()?.toInt() ?: 0
            val scoreTime = json.get("scoreTime")?.asString()?.toInt() ?: 0
            this.setLevel(
                sqlID = json.get("id").asString().toInt(),
                movieURL = json.get("movieURL").asString(),
                level = json.get("level").asString().toInt(),
                creator = json.get("creator").asString(),
                description = json.get("description").asString(),
                speed = json.get("speed").asString().toInt(),
                noteData = json.get("notes").asString(),
                updateTime = json.get("updateTime").asString().toInt(),
                createTime = json.get("createTime").asString().toInt(),
                playCount = json.get("playCount").asString().toInt(),
                playCountTime = playCountTime,
                favoriteCount = favoriteCount,
                favoriteCountTime = favoriteCountTime,
                commentTime = commentTime,
                scoreTime = scoreTime
            )
        }
    }
}