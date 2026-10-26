package com.mizushiki.nicoflick_a

import android.app.Application
import android.graphics.Insets
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.FrameLayout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = Application::class)
class SafeAreaActivityTest {
    @Suppress("DEPRECATION")
    @Test fun contentAndKeyboardStayInsideInsetsWithoutAccumulatingMargins() {
        val controller = Robolectric.buildActivity(SafeAreaActivity::class.java)
        val activity = controller.get()
        activity.setTheme(R.style.AppTheme)
        controller.setup()
        val content = FrameLayout(activity).apply { setPadding(8, 2, 8, 3) }
        activity.setContentView(content)
        val safe = content.parent as FrameLayout
        val scene = safe.parent as FrameLayout
        val keyboard = View(activity)
        activity.addContentView(keyboard, ViewGroup.LayoutParams(-1, -1))
        assertSame(safe, keyboard.parent)

        val insets = WindowInsets.Builder().setSystemWindowInsets(Insets.of(5, 24, 7, 48)).build()
        repeat(2) { scene.dispatchApplyWindowInsets(insets) }
        val margins = safe.layoutParams as FrameLayout.LayoutParams
        assertEquals(5, margins.leftMargin)
        assertEquals(24, margins.topMargin)
        assertEquals(7, margins.rightMargin)
        assertEquals(48, margins.bottomMargin)
        assertEquals(8, content.paddingLeft)
        assertEquals(2, content.paddingTop)

        // A hidden navigation bar must release its former space.
        scene.dispatchApplyWindowInsets(WindowInsets.Builder().setSystemWindowInsets(Insets.of(0, 24, 0, 0)).build())
        assertEquals(0, (safe.layoutParams as FrameLayout.LayoutParams).bottomMargin)
        controller.pause().stop().destroy()
    }

    class VideoTestActivity : SafeAreaActivity() {
        fun expand(video: View, anchor: View, enabled: Boolean) = layoutVideoBackground(video, anchor, enabled)
        fun coverKeyboard(background: View, anchor: View) = layoutKeyboardBackdrop(background, anchor)
    }

    @Test fun keyboardBackdropCoversNavigationAreaWithoutMovingControls() {
        val controller = Robolectric.buildActivity(VideoTestActivity::class.java)
        val activity = controller.get()
        activity.setTheme(R.style.AppTheme)
        controller.setup()
        val root = activity.layoutInflater.inflate(R.layout.activity_game_view, null) as ViewGroup
        val backdrop = root.findViewById<View>(R.id.keyboardBackdrop)
        val progress = root.findViewById<View>(R.id.progressBar_Loaded)
        activity.setContentView(root)
        val scene = root.parent.parent as FrameLayout
        fun layout() {
            scene.measure(View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY))
            scene.layout(0, 0, 400, 800)
        }
        for (bottomInset in intArrayOf(48, 0)) {
            scene.dispatchApplyWindowInsets(WindowInsets.Builder()
                .setSystemWindowInsets(Insets.of(0, 24, 0, bottomInset)).build())
            layout()
            val menu = root.findViewById<View>(R.id.button_Menu)
            val menuTop = menu.top
            backdrop.visibility = View.VISIBLE
            repeat(2) {
                activity.coverKeyboard(backdrop, progress)
                layout()
            }
            val location = IntArray(2)
            progress.getLocationInWindow(location)
            assertSame(scene, backdrop.parent)
            assertEquals(location[1] + progress.height, backdrop.top)
            assertEquals(800, backdrop.bottom)
            assertEquals(400, backdrop.width)
            assertEquals(menuTop, menu.top)
            backdrop.visibility = View.GONE
            layout()
            assertEquals(View.GONE, backdrop.visibility)
        }
        controller.pause().stop().destroy()
    }

    @Test fun expandedVideoReachesWindowTopWithoutMovingItsBottomOrControls() {
        val controller = Robolectric.buildActivity(VideoTestActivity::class.java)
        val activity = controller.get()
        activity.setTheme(R.style.AppTheme)
        controller.setup()
        // Use the production layout and keep clipping enabled on all safe-area parents.
        val root = activity.layoutInflater.inflate(R.layout.activity_game_view, null) as ViewGroup
        val anchor = root.findViewById<FrameLayout>(R.id.gameMovieFrame)
        anchor.layoutParams.height = 300
        val video = root.findViewById<com.google.android.exoplayer2.ui.PlayerView>(R.id.playerView)
        assertTrue(video.videoSurfaceView is android.view.TextureView)
        activity.setContentView(root)
        val scene = root.parent.parent as FrameLayout
        scene.dispatchApplyWindowInsets(WindowInsets.Builder()
            .setSystemWindowInsets(Insets.of(0, 24, 0, 48)).build())
        fun layout() {
            scene.measure(View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY))
            scene.layout(0, 0, 400, 800)
        }
        layout()
        val anchorLocation = IntArray(2)
        anchor.getLocationInWindow(anchorLocation)
        val bottomBefore = anchorLocation[1] + anchor.height
        val menu = root.findViewById<View>(R.id.button_Menu)
        val menuTopBefore = menu.top
        activity.expand(video, anchor, true)
        layout()
        assertSame(scene, video.parent)
        assertEquals(0, video.top)
        assertEquals(bottomBefore, video.height)
        assertEquals(bottomBefore, video.bottom)
        assertEquals(300, anchor.height)
        assertTrue(root.clipChildren)
        assertTrue((root.parent as ViewGroup).clipChildren)
        assertEquals(menuTopBefore, menu.top)
        activity.expand(video, anchor, false)
        layout()
        assertEquals(anchorLocation[1], video.top)
        assertEquals(300, video.height)
        assertEquals(bottomBefore, video.bottom)
        assertEquals(menuTopBefore, menu.top)

        // Insets and keyboard-driven anchor resizing must update the background too.
        scene.dispatchApplyWindowInsets(WindowInsets.Builder()
            .setSystemWindowInsets(Insets.of(0, 48, 0, 48)).build())
        anchor.layoutParams.height = 220
        anchor.requestLayout()
        layout()
        repeat(2) {
            activity.expand(video, anchor, true)
            layout()
        }
        anchor.getLocationInWindow(anchorLocation)
        assertEquals(0, video.top)
        assertEquals(anchorLocation[1] + 220, video.bottom)
        assertEquals(220, anchor.height)
        assertSame(scene, video.parent)
        controller.pause().stop().destroy()
    }
}
