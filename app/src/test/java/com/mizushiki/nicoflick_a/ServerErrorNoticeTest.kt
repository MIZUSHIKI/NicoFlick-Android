package com.mizushiki.nicoflick_a

import org.junit.Assert.assertEquals
import org.junit.Test

class ServerErrorNoticeTest {
    @Test fun platformTagsAtEitherEnd() {
        for (tag in listOf("[Android]", "[Android,iOS]", "[iOS, Android]")) {
            assertEquals("本文", ServerErrorNotice.parse(tag + "本文").message)
            assertEquals("本文", ServerErrorNotice.parse("本文" + tag).message)
        }
        assertEquals("", ServerErrorNotice.parse("[iOS]本文").message)
        assertEquals("", ServerErrorNotice.parse("本文[iOS]").message)
        assertEquals("本文", ServerErrorNotice.parse("本文").message)
        assertEquals("本文中の[iOS]表記", ServerErrorNotice.parse("本文中の[iOS]表記").message)
        assertEquals("本文[And]", ServerErrorNotice.parse("本文[And]").message)
    }

    @Test fun titleAndPlatformCanBeCombined() {
        for (raw in listOf(
            "{title:サーバの一時停止}[Android]本文",
            "[Android]{title:サーバの一時停止}本文",
            "本文[iOS,Android]{title:サーバの一時停止}"
        )) {
            assertEquals(ServerErrorNotice("サーバの一時停止", "本文"), ServerErrorNotice.parse(raw))
        }
        assertEquals("", ServerErrorNotice.parse("{title:停止}[iOS]本文").message)
        assertEquals("サーバエラー", ServerErrorNotice.parse("本文").title)
        assertEquals("サーバエラー", ServerErrorNotice.parse("{title:}本文").title)
    }

    @Test fun platformTagsAtBothEndsAreCombined() {
        assertEquals("本文", ServerErrorNotice.parse("[iOS]本文[Android]").message)
    }
}
