package com.mizushiki.nicoflick_a

import android.app.Application
import android.graphics.drawable.GradientDrawable
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.ViewGroup
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class FadingPickerLayoutTest {
    @Test fun selectorFadesAllPickerLayersWithoutWhiteGradientOverlays() {
        val context = ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.AppTheme)
        val root = LayoutInflater.from(context).inflate(R.layout.activity_selector, null)
        val picker = root.findViewById<ViewGroup>(R.id.frameLayout)
        assertTrue(picker is FadingPickerLayout)
        // The source rows, cropped selection, and gesture receiver must share one mask.
        assertSame(picker, root.findViewById<android.view.View>(R.id.levelpickerContainer).parent)
        assertSame(picker, root.findViewById<android.view.View>(R.id.outputImageView_levelScroller).parent)
        assertSame(picker, root.findViewById<android.view.View>(R.id.scrollview_levelScroller).parent)
        for (index in 0 until picker.childCount) {
            assertFalse(picker.getChildAt(index).background is GradientDrawable)
        }
    }
}
