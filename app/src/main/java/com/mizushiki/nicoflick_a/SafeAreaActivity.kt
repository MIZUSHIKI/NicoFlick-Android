package com.mizushiki.nicoflick_a

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

/**
 * Backgrounds fill the window; interactive content stays inside system bars and cutouts.
 * Uses platform APIs so the app's existing AndroidX dependencies can remain unchanged.
 */
@Suppress("DEPRECATION")
open class SafeAreaActivity : AppCompatActivity() {
    private lateinit var scene: FrameLayout
    private lateinit var safeContent: FrameLayout
    private var topBackground: View? = null
    private var bottomBackground: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        if (Build.VERSION.SDK_INT >= 26) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        if (Build.VERSION.SDK_INT >= 28) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        if (Build.VERSION.SDK_INT >= 29) {
            window.isStatusBarContrastEnforced = false
            // Retain the system's contrast protection for three-button navigation.
            window.isNavigationBarContrastEnforced = true
        }
        if (Build.VERSION.SDK_INT >= 30) window.setDecorFitsSystemWindows(false)
        window.setSoftInputMode((window.attributes.softInputMode and
            WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST.inv()) or
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun setContentView(view: View) {
        scene = FrameLayout(this)
        safeContent = FrameLayout(this)
        val toolbar = view.findViewById<View>(R.id.toolbar2)
            ?: view.findViewById<View>(R.id.toolbar)
        // Move only decorative layers; keep all controls in their original layout.
        val backgroundIds = when (this) {
            is Activity_Selector -> intArrayOf(R.id.playerView, R.id.playerView_Alpha,
                R.id.playerView_SlashShade_layout)
            is MainActivity -> intArrayOf(R.id.title_SlashShade_layout)
            is Activity_GameView -> intArrayOf(R.id.mekakushiView, R.id.playerView,
                R.id.keyboardBackdrop)
            is Activity_RankingComment -> intArrayOf(R.id.RankingCommentView_imageAlpha,
                R.id.RankingCommentView_SlashShade_layout)
            is Activity_Result -> intArrayOf(R.id.ResultView_imageAlpha,
                R.id.ResultView_SlashShade_layout)
            else -> intArrayOf()
        }
        if (this is Activity_RankingComment || this is Activity_Result) {
            scene.background = view.background
            view.background = null
        }
        for (id in backgroundIds) {
            val background = view.findViewById<View>(id) ?: continue
            (background.parent as? ViewGroup)?.removeView(background)
            scene.addView(background, FrameLayout.LayoutParams(-1, -1))
        }
        if (this is Activity_SelectorMenu) {
            // Continue the translucent black panel behind the navigation handle.
            bottomBackground = View(this).apply {
                background = view.findViewById<View>(R.id.view)
                    .background?.constantState?.newDrawable()?.mutate()
            }
            scene.addView(bottomBackground, FrameLayout.LayoutParams(-1, 0, Gravity.BOTTOM))
            if (Build.VERSION.SDK_INT >= 26) {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
            if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        } else if (this is Activity_RankingComment) {
            // Continue the tab bar background below its safe-area controls.
            bottomBackground = View(this).apply {
                background = view.findViewById<View>(R.id.btm_navigation)
                    .background?.constantState?.newDrawable()?.mutate()
            }
            scene.addView(bottomBackground, FrameLayout.LayoutParams(-1, 0, Gravity.BOTTOM))
            if (Build.VERSION.SDK_INT >= 26) {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        }
        safeContent.addView(view, FrameLayout.LayoutParams(-1, -1))
        scene.addView(safeContent, FrameLayout.LayoutParams(-1, -1))
        if (toolbar != null) {
            topBackground = View(this).apply {
                background = toolbar.background?.constantState?.newDrawable()?.mutate()
            }
            scene.addView(topBackground, FrameLayout.LayoutParams(-1, 0, Gravity.TOP))
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        scene.setOnApplyWindowInsetsListener { _, insets ->
            val left: Int
            val top: Int
            val right: Int
            val bottom: Int
            if (Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars() or
                    WindowInsets.Type.displayCutout())
                val ime = insets.getInsets(WindowInsets.Type.ime())
                left = bars.left; top = bars.top; right = bars.right
                bottom = maxOf(bars.bottom, ime.bottom)
            } else {
                if (Build.VERSION.SDK_INT >= 28) {
                    val cutout = insets.displayCutout
                    left = maxOf(insets.systemWindowInsetLeft, cutout?.safeInsetLeft ?: 0)
                    top = maxOf(insets.systemWindowInsetTop, cutout?.safeInsetTop ?: 0)
                    right = maxOf(insets.systemWindowInsetRight, cutout?.safeInsetRight ?: 0)
                    bottom = maxOf(insets.systemWindowInsetBottom, cutout?.safeInsetBottom ?: 0)
                } else {
                    left = insets.systemWindowInsetLeft; top = insets.systemWindowInsetTop
                    right = insets.systemWindowInsetRight; bottom = insets.systemWindowInsetBottom
                }
            }
            val params = safeContent.layoutParams as FrameLayout.LayoutParams
            if (params.leftMargin != left || params.topMargin != top ||
                params.rightMargin != right || params.bottomMargin != bottom) {
                params.setMargins(left, top, right, bottom)
                safeContent.layoutParams = params
            }
            topBackground?.let {
                if (it.layoutParams.height != top) {
                    it.layoutParams = (it.layoutParams as FrameLayout.LayoutParams).apply { height = top }
                }
            }
            bottomBackground?.let {
                val navigationBottom = if (Build.VERSION.SDK_INT >= 30) {
                    insets.getInsets(WindowInsets.Type.navigationBars()).bottom
                } else {
                    minOf(insets.systemWindowInsetBottom, insets.stableInsetBottom)
                }
                if (it.layoutParams.height != navigationBottom) {
                    it.layoutParams = (it.layoutParams as FrameLayout.LayoutParams).apply {
                        height = navigationBottom
                    }
                }
            }
            // Descendants already live in the safe area; avoid Material adding insets twice.
            if (Build.VERSION.SDK_INT >= 30) {
                WindowInsets.CONSUMED
            } else if (Build.VERSION.SDK_INT >= 28) {
                insets.consumeSystemWindowInsets().consumeDisplayCutout()
            } else {
                insets.consumeSystemWindowInsets()
            }
        }
        super.setContentView(scene)
        scene.requestApplyInsets()
    }

    /**
     * Keep the video in the full-window background, independent of safe-area clipping.
     * The empty anchor remains in the controls layout and defines the video's bottom.
     * Call after layout, including after IME, inset, and expansion-mode changes.
     */
    protected fun layoutVideoBackground(background: View, anchor: View, expanded: Boolean) {
        if (!::safeContent.isInitialized || anchor.width == 0 || anchor.height == 0) return
        if (background.parent !== scene) {
            (background.parent as? ViewGroup)?.removeView(background)
            scene.addView(background, scene.indexOfChild(safeContent), FrameLayout.LayoutParams(0, 0))
        }
        val anchorLocation = IntArray(2)
        val sceneLocation = IntArray(2)
        anchor.getLocationInWindow(anchorLocation)
        scene.getLocationInWindow(sceneLocation)
        val left = anchorLocation[0] - sceneLocation[0]
        val anchorTop = anchorLocation[1] - sceneLocation[1]
        val top = if (expanded) 0 else anchorTop
        val height = (anchorTop + anchor.height - top).coerceAtLeast(1)
        fun position(layer: View) {
            val params = layer.layoutParams as FrameLayout.LayoutParams
            if (params.width != anchor.width || params.height != height ||
                params.leftMargin != left || params.topMargin != top) {
                layer.layoutParams = FrameLayout.LayoutParams(anchor.width, height, Gravity.TOP or Gravity.LEFT)
                    .apply { leftMargin = left; topMargin = top }
            }
        }
        position(background)
        // Preserve the white backing used while the video fades in at game start.
        if (this is Activity_GameView) scene.findViewById<View>(R.id.mekakushiView)?.let { position(it) }
        window.decorView.systemUiVisibility = if (expanded) {
            window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        } else {
            window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    /** Fill from the video's progress bar to the physical window bottom, including navigation. */
    protected fun layoutKeyboardBackdrop(background: View, anchor: View) {
        if (!::safeContent.isInitialized || scene.height == 0 || anchor.height == 0) return
        if (background.parent !== scene) {
            (background.parent as? ViewGroup)?.removeView(background)
            scene.addView(background, scene.indexOfChild(safeContent), FrameLayout.LayoutParams(0, 0))
        }
        val anchorLocation = IntArray(2)
        val sceneLocation = IntArray(2)
        anchor.getLocationInWindow(anchorLocation)
        scene.getLocationInWindow(sceneLocation)
        val top = (anchorLocation[1] - sceneLocation[1] + anchor.height).coerceIn(0, scene.height)
        val height = scene.height - top
        val params = background.layoutParams as FrameLayout.LayoutParams
        if (params.width != scene.width || params.height != height || params.topMargin != top) {
            background.layoutParams = FrameLayout.LayoutParams(scene.width, height, Gravity.TOP)
                .apply { topMargin = top }
        }
    }

    override fun addContentView(view: View?, params: ViewGroup.LayoutParams) {
        if (view == null) return
        // Custom keyboards and preparation overlays share the same screen coordinate origin.
        if (::safeContent.isInitialized) {
            safeContent.addView(view, FrameLayout.LayoutParams(params.width, params.height))
        } else {
            super.addContentView(view, params)
        }
    }
}
