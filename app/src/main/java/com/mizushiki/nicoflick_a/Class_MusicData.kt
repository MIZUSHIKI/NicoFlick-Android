package com.mizushiki.nicoflick_a

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonArray
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

    fun setMusic(sqlID:Int, movieURL:String, thumbnailURL:String, title:String, artist:String, movieLength:String, tags:String, updateTime:Int, createTime:Int){
        if(movieURL=="delete"){
            //削除
            for(index in 0 until musics.size){
                if(musics[index].sqlID == sqlID){
                    musics.removeAt(index)
                }
            }
            return
        }
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

    fun setLevel(sqlID:Int, movieURL:String, level:Int, creator:String, description:String, speed:Int, noteData:String, updateTime:Int, createTime:Int, playCount:Int){
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
            return
        }
        for(music in musics){
            if(music.movieURL == movieURL) {
                music.levelIDs.add(sqlID)
                break
            }
        }
        if(levels[movieURL] != null) {
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
        levels[movieURL] = levels[movieURL] ?: arrayListOf()
        levels[movieURL]!!.add(leveldata)
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

    //
    fun getSelectMusics( callback:(ArrayList<musicData>) -> Unit ){

        val selectCondition = USERDATA.SelectedMusicCondition //userData読み出し

        var extractMusics:ArrayList<musicData> = arrayListOf()
        println("selectCondition.tag.size="+selectCondition.tag.size)
        println("musics.size="+ musics.size)
        if( selectCondition.tag.size == 0 ){
            extractMusics = musics.clone() as ArrayList<musicData>
        }else {
            var remainMusics = musics.clone() as ArrayList<musicData>

            loop@ for( tagp in selectCondition.tag ){
                println(""+tagp.type+", "+tagp.word)
                when( tagp.type ){
                    "or" -> {
                        if(tagp.word == "@初期楽曲"){

                        }

                        val rmCount = remainMusics.size
                        println("rmCount="+rmCount)
                        if( rmCount == 0 ){
                            continue@loop
                        }
                        for( bindex in 1 .. rmCount ){
                            val index = rmCount - bindex
                            println(remainMusics[index].title+" - remainMusics[$index].tag.contains(${tagp.word})="+remainMusics[index].tag.contains(tagp.word))
                            if( tagp.word == "@初期楽曲" ){

                                if( remainMusics[index].sqlID > 14 ){
                                    continue
                                }
                            }else if( !remainMusics[index].tag.contains(tagp.word) ){
                                continue
                            }
                            println("たす")
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
                            }else if( extractMusics[index].tag.contains(tagp.word) ){
                                continue
                            }
                            val r = extractMusics[index]
                            remainMusics.add(r)
                            extractMusics.removeAt(index)
                        }
                    }
                    "-" -> {
                        if( tagp.word == "@初期楽曲" ){
                            extractMusics = ArrayList(extractMusics.filter{it.sqlID > 14})
                            remainMusics = ArrayList(remainMusics.filter{it.sqlID > 14})
                        }else{
                            extractMusics = ArrayList(extractMusics.filter{!it.tag.contains(tagp.word)})
                            remainMusics = java.util.ArrayList(remainMusics.filter{!it.tag.contains(tagp.word)})
                        }
                    }
                }
            }
        }
        // 【編集中：】でレベル数が0になる場合、その musicは弾いて表示させないようにする。
        val outputMusics:ArrayList<musicData> = arrayListOf()
        for( musicdata in extractMusics ){
            if( getSelectMusicLevels_noSort(musicdata.movieURL).size == 0 ){
                continue
            }
            outputMusics.add(musicdata)
            println(musicdata.title)
        }
        getSortedMusics(outputMusics, callback)
        return
    }

    fun getSortedMusics(musics:ArrayList<musicData>, callback:(ArrayList<musicData>) -> Unit) {
        val selectCondition = USERDATA.SelectedMusicCondition //userData読み出し

        var sortedMusics:ArrayList<musicData> = musics//arrayListOf()
        when( selectCondition.sortItem ){
            "曲の投稿が新しい順" -> musics.sortBy{ it.sqlID * -1 }
            "曲の投稿が古い順" -> musics.sortBy { it.sqlID }
            "ゲームの投稿が新しい曲順" -> {
                val levelp:MutableMap<String,Int> = mutableMapOf() //[movieURL,sqlID]
                for( (key,lvInURL) in levels ){
                    for( leveldata in lvInURL ){
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
                //スコアデータ更新取得
                ServerDataHandler().DownloadScoreData {
                    if(it!=null){
                        print("error")
                    }
                    val scoreDatas = ScoreDataLists
                    //すべてのスコアを処理。レベルIDをkeyにしてそのレベルで一番スコアの高いものを保持する。(でないと、スコアが低くてもUpdateTimeが新しいものを抽出してしまうことになる)
                    val highScores:MutableMap<Int,scoreData> = mutableMapOf()
                    for( scoredata in scoreDatas.scores ){
                        if( highScores[scoredata.levelID] == null ){
                            highScores[scoredata.levelID] = scoredata
                        }else {
                            if( highScores[scoredata.levelID]!!.score < scoredata.score ){
                                highScores[scoredata.levelID] = scoredata
                            }
                        }
                    }
                    //これでスコアがタイム順で並んだ
                    val sortedHighScores = highScores.toList().sortedBy{ it.second.sqlUpdateTime }.toMap()

                    //レベルから曲を逆引き(曲が複数選ばれないようにしなくてはならない)
                    for( (levelID, _) in sortedHighScores ) {
                        var music:musicData? = null
                        for( m in musics ){
                            if( m.levelIDs.contains(levelID) ){
                                music = m
                                break
                            }
                        }
                        if( music == null ){
                            continue
                        }
                        var appendable = true
                        for( m in sortedMusics ){
                            if( m.sqlID == music.sqlID ){
                                //既にこの曲は追加済み
                                appendable = false
                                break
                            }
                        }
                        if( appendable ){
                            sortedMusics.add(music)
                        }
                    }
                    callback(sortedMusics)
                }
            }
            "最近コメントされた曲順" -> {
                //コメントデータ更新
                ServerDataHandler().DownloadCommentData {
                    if(it!=null){
                        print("error")
                    }
                    val sortedComments = CommentDataLists.comments

                    //これでコメントがタイム順で並んだ(ハイスコア順と違い純粋にUpdateTime順)
                    sortedComments.sortBy{ it.sqlUpdateTime * -1 }

                    //レベルから曲を逆引き(曲が複数選ばれないようにしなくてはならない)
                    for( comment in sortedComments ){
                        val levelID = comment.levelID
                        var music:musicData? = null
                        for( m in musics ){
                            if( m.levelIDs.contains(levelID) ){
                                music = m
                                break
                            }
                        }
                        if( music == null ){
                            continue
                        }
                        var appendable = true
                        for( m in sortedMusics ){
                            if( m.sqlID == music!!.sqlID ){
                                //既にこの曲は追加済み
                                appendable = false
                                break
                            }
                        }
                        if( appendable ){
                            sortedMusics.add(music!!)
                        }
                    }
                    callback(sortedMusics)
                }
            }

            else -> sortedMusics = musics
        }
        //《編集中》 levels[$0.movieURL] のすべてのdescriptionを調べて【非表示】が含まれるものを排除したとき、lelvelの数が0ならmusicもフィルタリングで除外される
        //sortedMusics = sortedMusics.filter({ levels[$0.movieURL]!.count>0 })
        callback(sortedMusics)
    }

    //とりあえずレベル取り出しを作った。現状level順ソートだけしか必要無い。
    fun getSelectMusicLevels(selectMovieURL:String) : ArrayList<levelData>{
        val sortedLevels = getSelectMusicLevels_noSort(selectMovieURL)
        sortedLevels.sortBy { it.level }
        return sortedLevels
    }
    fun getSelectMusicLevels_noSort(selectMovieURL:String) : ArrayList<levelData>{
        val selectLevels:ArrayList<levelData> = arrayListOf()
        if( levels[selectMovieURL] == null ) {
            return selectLevels
        }
        for( leveldata in levels[selectMovieURL]!!){
            if( leveldata.isEditing && !leveldata.isMyEditing ){
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
                playCount = json.get("playCount").asString().toInt()
            )
        }
    }
}