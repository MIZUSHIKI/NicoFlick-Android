package com.mizushiki.nicoflick_a

/** Reads the existing levelID,score,rank,sent format without changing its numeric conventions. */
internal fun readSavedScoreRecords(
    raw: String,
    onInvalidRecords: () -> Unit
): MutableMap<Int, ArrayList<Int>> {
    val records = mutableMapOf<Int, ArrayList<Int>>()
    var invalid = false
    for (row in raw.split("|")) {
        if (row.isEmpty()) continue // Empty stores and the normal trailing separator.
        val fields = row.split(",")
        val values = fields.map { it.toIntOrNull() }
        if (fields.size != 4 || values.any { it == null }) {
            invalid = true
            continue
        }
        records[values[0]!!] = arrayListOf(values[1]!!, values[2]!!, values[3]!!)
    }
    // Preserve the original before a caller can later save the recovered records.
    if (invalid) onInvalidRecords()
    return records
}
