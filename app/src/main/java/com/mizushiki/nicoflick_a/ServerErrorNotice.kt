package com.mizushiki.nicoflick_a

internal data class ServerErrorNotice(val title: String, val message: String) {
    companion object {
        fun parse(rawMessage: String): ServerErrorNotice {
            val titlePattern = Regex("""\{title:([^}]*)\}""")
            val title = titlePattern.find(rawMessage)?.groupValues?.get(1)
                ?.takeIf { it.isNotBlank() } ?: "サーバエラー"
            var message = titlePattern.replace(rawMessage, "").trim()
            val platformPattern = Regex("""\[\s*(Android|iOS)(?:\s*,\s*(?:Android|iOS))*\s*\]""")
            val platforms = mutableSetOf<String>()
            // 本文中の表記は残し、先頭・末尾の対象端末タグだけを取り除く。
            while (true) {
                val tag = platformPattern.findAll(message).firstOrNull {
                    it.range.first == 0 || it.range.last == message.lastIndex
                } ?: break
                platforms.addAll(tag.value.removeSurrounding("[", "]").split(",").map { it.trim() })
                message = message.removeRange(tag.range).trim()
            }
            if (platforms.isNotEmpty() && "Android" !in platforms) message = ""
            return ServerErrorNotice(title, message)
        }
    }
}
