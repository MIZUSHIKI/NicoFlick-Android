package com.mizushiki.nicoflick_a

// 旧バージョンでFULLとしてプレイされたことが分かっている譜面。
private val legacyFullScoreLevelIDs = setOf(
    6751, 9009, 4533, 3346, 3575, 3831, 794, 3143, 3145, 3322, 3500, 3501,
    3523, 3524, 4919, 5253, 6370, 7374, 4035, 8344, 8190, 7684, 6082, 1486,
    2745, 3925, 6376, 6462, 7342, 7343, 7845, 8973, 8970, 9481, 9477, 9005,
    9007, 6459, 708, 704, 688, 5985, 6325, 4423, 7580, 5522
)

internal fun migratedScoreLevels(
    scoreIDs:Set<Int>,
    savedLevels:Map<Int,Int>,
    currentLevels:Map<Int,Int>
):MutableMap<Int,Int> = savedLevels.toMutableMap().apply {
    for (levelID in scoreIDs) {
        // すでにプレイ時の難易度が保存されていれば変更しない。
        if (containsKey(levelID)) continue
        this[levelID] = if (levelID in legacyFullScoreLevelIDs) 100 else currentLevels[levelID] ?: 100
    }
}
