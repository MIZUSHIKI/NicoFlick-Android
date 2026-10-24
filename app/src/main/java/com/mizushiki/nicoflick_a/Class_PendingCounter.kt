package com.mizushiki.nicoflick_a

/** Unsent deltas: acknowledgment removes only the snapshot that was actually sent. */
class PendingCounter(
    private val storage:String,
    private val readCounts:(String) -> MutableMap<Int,Int> = { USERDATA.readCountMap(it) },
    private val writeCounts:(String, Map<Int,Int>) -> Unit = { key, values -> USERDATA.writeCountMap(key, values) }
) {
    val counter:MutableMap<Int,Int> get() = readCounts(storage)
    fun changeLikeCount(sqlID:Int, change:Int) {
        if (change == 0) return
        val values = counter
        val value = (values[sqlID] ?: 0) + change
        if (value == 0) values.remove(sqlID) else values[sqlID] = value
        writeCounts(storage, values)
    }
    fun addMusicFavoriteCount(sqlID:Int) = changeLikeCount(sqlID, 1)
    fun subMusicFavoriteCount(sqlID:Int) = changeLikeCount(sqlID, -1)
    fun getSendLikeCountStr():String = encode(counter)
    fun getSendMusicFavoriteCountStr():String = encode(counter)
    fun acknowledge(sent:Map<Int,Int>) {
        val values = counter
        sent.forEach { (id, count) ->
            val remaining = (values[id] ?: 0) - count
            if (remaining == 0) values.remove(id) else values[id] = remaining
        }
        writeCounts(storage, values)
    }
    companion object {
        fun encode(values:Map<Int,Int>):String = values.toSortedMap().entries.joinToString("") { "<${it.key},${it.value}>" }
    }
}
