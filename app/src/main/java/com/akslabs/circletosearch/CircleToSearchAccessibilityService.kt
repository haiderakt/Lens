/*
 *
 *  * Copyright (C) 2025 AKS-Labs (original author)
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.akslabs.circletosearch

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.camera2.CameraManager
import android.graphics.RectF
import android.graphics.Paint
import android.graphics.BitmapShader
import android.graphics.Shader
import android.graphics.Matrix
import androidx.core.graphics.drawable.toBitmap
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Display
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.akslabs.circletosearch.data.BitmapRepository
import com.akslabs.circletosearch.ui.components.CopyTextOverlayManager
import com.akslabs.circletosearch.utils.ImageUtils
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class CircleToSearchAccessibilityService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private val executor: Executor = Executors.newSingleThreadExecutor()
    
    /** Kept by companion so scroll events can re-scan copy-text nodes. */
    internal var copyTextManager: CopyTextOverlayManager? = null
    
    private var bubbleView: View? = null
    private val prefs by lazy { getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    
    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "bubble_enabled") {
            updateBubbleState()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        val info = serviceInfo
        info.flags = info.flags or 
            android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
            android.accessibilityservice.AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY or
            android.accessibilityservice.AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        serviceInfo = info
        
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
        
        updateBubbleState()
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    private fun updateBubbleState() {
        if (prefs.getBoolean("bubble_enabled", false)) {
            showBubble()
        } else {
            hideBubble()
        }
    }

    private fun showBubble() {
        if (bubbleView != null) return // Already shown

        val params = WindowManager.LayoutParams(
            100, 100,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 200

        bubbleView = View(this).apply {
            setBackgroundResource(R.mipmap.ic_launcher)
            elevation = 10f
            
            var initialX = 0
            var initialY = 0
            var initialTouchX = 0f
            var initialTouchY = 0f
            
            @SuppressLint("ClickableViewAccessibility")
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(this, params)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (Math.abs(event.rawX - initialTouchX) < 10 && Math.abs(event.rawY - initialTouchY) < 10) {
                            performCapture()
                        }
                        true
                    }
                    else -> false
                }
            }
        }

        try {
            windowManager?.addView(bubbleView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideBubble() {
        if (bubbleView != null) {
            try {
                windowManager?.removeView(bubbleView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            bubbleView = null
        }
    }

    private fun performCapture(searchModeOverride: Boolean? = null) {
        android.util.Log.d("CircleToSearch", "performCapture called. hasWindowManager=${windowManager != null}")
        
        BitmapRepository.clear()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            takeScreenshot(
                Display.DEFAULT_DISPLAY,
                executor,
                object : TakeScreenshotCallback {
                    override fun onSuccess(screenshot: ScreenshotResult) {
                         try {
                            val hardwareBuffer = screenshot.hardwareBuffer
                            val colorSpace = screenshot.colorSpace
                            
                            val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)
                            if (bitmap == null) {
                                hardwareBuffer.close()
                                return
                            }

                            val copy = bitmap.copy(Bitmap.Config.ARGB_8888, false)
                            hardwareBuffer.close()

                            if (copy == null) {
                                return
                            }
                            
                            BitmapRepository.setScreenshot(copy)
                            launchOverlay(searchModeOverride)
                            
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(errorCode: Int) {
                        android.util.Log.e("CircleToSearch", "Screenshot failed with error code: $errorCode")
                    }
                }
            )
        }
    }

    fun launchOverlay(searchModeOverride: Boolean? = null) {
        val intent = Intent(this, OverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            searchModeOverride?.let { putExtra("EXTRA_SEARCH_MODE_OVERRIDE", it) }
        }
        startActivity(intent)
    }

    private class RoundedImageView(context: android.content.Context) : android.widget.ImageView(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rect = RectF()
        private val matrix = Matrix()
        private val radius = 12f * context.resources.displayMetrics.density

        override fun onDraw(canvas: android.graphics.Canvas) {
            val drawable = drawable ?: return
            val bitmap = try { 
                drawable.toBitmap() 
            } catch (e: Exception) { 
                return 
            }
            
            val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            
            matrix.reset()
            matrix.setScale(width.toFloat() / bitmap.width, height.toFloat() / bitmap.height)
            shader.setLocalMatrix(matrix)
            
            paint.shader = shader
            rect.set(0f, 0f, width.toFloat(), height.toFloat())
            
            canvas.drawRoundRect(rect, radius, radius, paint)
        }
    }

    private fun showPinnedArea(bitmap: Bitmap, rect: android.graphics.Rect) {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val centerX = rect.centerX()
        val centerY = rect.centerY()
        
        val width = bitmap.width
        val height = bitmap.height

        val params = WindowManager.LayoutParams(
            width, height,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = centerX - width / 2
        params.y = centerY - height / 2

        val pinnedView = RoundedImageView(this).apply {
            setImageBitmap(bitmap)
            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            elevation = 0f
            clipToOutline = true
            
            var initialX = 0
            var initialY = 0
            var initialTouchX = 0f
            var initialTouchY = 0f
            var isDragging = false
            var isScaling = false
            var currentMenu: View? = null
            
            var velocityTracker: android.view.VelocityTracker? = null
            var flingAnimator: android.animation.ValueAnimator? = null
            
            val stopFling = {
                flingAnimator?.cancel()
                flingAnimator = null
            }

            val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    isScaling = true
                    val scaleFactor = detector.scaleFactor
                    
                    val newWidth = (params.width * scaleFactor).toInt()
                    val newHeight = (params.height * scaleFactor).toInt()
                    
                    val minDim = (screenWidth * 0.15f).toInt()
                    val maxDim = (screenWidth * 0.95f).toInt()
                    
                    if (newWidth in minDim..maxDim && newHeight in minDim..maxDim) {
                        val focusX = detector.focusX
                        val focusY = detector.focusY
                        
                        params.x -= ((newWidth - params.width) * (focusX / this@apply.width)).toInt()
                        params.y -= ((newHeight - params.height) * (focusY / this@apply.height)).toInt()
                        
                        params.width = newWidth
                        params.height = newHeight
                        windowManager?.updateViewLayout(this@apply, params)
                    }
                    return true
                }
            })

            val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onLongPress(e: MotionEvent) {
                    if (!isDragging) {
                        if (currentMenu == null) {
                            showPinnedActions(this@apply, bitmap, params) { menu ->
                                currentMenu = menu
                            }
                        }
                    }
                }
            })

            @SuppressLint("ClickableViewAccessibility")
            setOnTouchListener { v, event ->
                scaleDetector.onTouchEvent(event)
                gestureDetector.onTouchEvent(event)
                
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragging = false
                        isScaling = false
                        stopFling()
                        velocityTracker?.recycle()
                        velocityTracker = android.view.VelocityTracker.obtain()
                        velocityTracker?.addMovement(event)
                        true
                    }
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        isScaling = true
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (isScaling) return@setOnTouchListener true
                        
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isDragging = true
                            currentMenu?.let { try { windowManager?.removeView(it) } catch(e: Exception) {} }
                            currentMenu = null
                        }
                        
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager?.updateViewLayout(v, params)
                        velocityTracker?.addMovement(event)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isScaling = false
                        if (isDragging) {
                            velocityTracker?.computeCurrentVelocity(1000)
                            val vx = velocityTracker?.xVelocity ?: 0f
                            val vy = velocityTracker?.yVelocity ?: 0f
                            
                            if (Math.abs(vx) > 300 || Math.abs(vy) > 300) {
                                flingAnimator = android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
                                    duration = 2000
                                    interpolator = android.view.animation.LinearInterpolator()
                                    var lastTime = 0f
                                    var currVx = vx * 2.2f
                                    var currVy = vy * 2.2f
                                    
                                    addUpdateListener { anim ->
                                        val faction = anim.animatedFraction
                                        val dt = faction - lastTime
                                        lastTime = faction
                                        
                                        params.x += (currVx * dt * 0.95f).toInt()
                                        params.y += (currVy * dt * 0.95f).toInt()
                                        
                                        val display = resources.displayMetrics
                                        if (params.x < 0 || params.x + params.width > display.widthPixels) {
                                            currVx = -currVx * 0.9f 
                                            params.x = params.x.coerceIn(0, display.widthPixels - params.width)
                                        }
                                        if (params.y < 0 || params.y + params.height > display.heightPixels) {
                                            currVy = -currVy * 0.9f
                                            params.y = params.y.coerceIn(0, display.heightPixels - params.height)
                                        }
                                        
                                        try { 
                                            windowManager?.updateViewLayout(v, params)
                                        } catch(e: Exception) { 
                                            anim.cancel()
                                            flingAnimator = null
                                        }
                                        
                                        currVx *= 0.992f
                                        currVy *= 0.992f
                                        
                                        if (Math.abs(currVx) < 10 && Math.abs(currVy) < 10) {
                                            anim.cancel()
                                            flingAnimator = null
                                        }
                                    }
                                    addListener(object : android.animation.AnimatorListenerAdapter() {
                                        override fun onAnimationEnd(animation: android.animation.Animator) {
                                            flingAnimator = null
                                        }
                                        override fun onAnimationCancel(animation: android.animation.Animator) {
                                            flingAnimator = null
                                        }
                                    })
                                    start()
                                }
                            }
                        }
                        velocityTracker?.recycle()
                        velocityTracker = null
                        true
                    }
                    else -> false
                }
            }
        }

        try {
            windowManager?.addView(pinnedView, params)
            
            pinnedView.scaleX = 0f
            pinnedView.scaleY = 0f
            pinnedView.rotation = -15f
            pinnedView.alpha = 0f
            pinnedView.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .rotation(0f)
                .alpha(1f)
                .setDuration(450)
                .setInterpolator(android.view.animation.OvershootInterpolator(1.4f))
                .withEndAction {
                    pinnedView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start()
                }
                .start()
                
        } catch (e: Exception) {
            android.util.Log.e("CircleToSearch", "Failed to add pinned view", e)
        }
    }

    private fun showPinnedActions(anchorView: View, bitmap: Bitmap, anchorParams: WindowManager.LayoutParams, onMenuCreated: (View) -> Unit) {
        val displayMetrics = resources.displayMetrics
        val menuPadding = (10 * displayMetrics.density).toInt()
        val cornerRadius = 32f * displayMetrics.density

        val isNight = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        
        val toolbarBgColor = try { getColor(android.R.color.system_surface_container_light) } catch(e: Exception) { if (isNight) Color.parseColor("#FF1C1C1C") else Color.parseColor("#FFF3EDF7") }
        val primaryColor = try { getColor(android.R.color.system_accent1_600) } catch(e: Exception) { if (isNight) Color.parseColor("#FFD0BCFF") else Color.parseColor("#FF6750A4") }
        val contentColor = Color.WHITE
        val borderColor = if (isNight) Color.parseColor("#33FFFFFF") else Color.parseColor("#22000000")

        val menuLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            setPadding(menuPadding, menuPadding, menuPadding, menuPadding)
            elevation = 24f
            
            val background = GradientDrawable().apply {
                setColor(toolbarBgColor)
                setCornerRadius(cornerRadius)
                setStroke((1 * displayMetrics.density).toInt(), borderColor)
            }
            setBackground(background)
            
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: android.graphics.Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
                }
            }
            clipToOutline = true
        }

        fun createTextActionButton(label: String, onClick: () -> Unit) = android.widget.Button(this).apply {
            text = label
            setTextColor(contentColor)
            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            
            val btnDrawable = GradientDrawable().apply {
                setColor(primaryColor)
                setCornerRadius(20 * displayMetrics.density)
            }
            background = btnDrawable
            
            setPadding((16 * displayMetrics.density).toInt(), 0, (16 * displayMetrics.density).toInt(), 0)
            
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                (36 * displayMetrics.density).toInt()
            )
            setOnClickListener { onClick() }
        }

        menuLayout.addView(createTextActionButton("SHARE") {
            try {
                val fileName = "share_pin_${java.util.UUID.randomUUID()}.png"
                val path = ImageUtils.saveBitmap(this@CircleToSearchAccessibilityService, bitmap, fileName)
                val file = java.io.File(path)
                val uri = androidx.core.content.FileProvider.getUriForFile(this@CircleToSearchAccessibilityService, "com.akslabs.circletosearch.fileprovider", file)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(Intent.createChooser(shareIntent, "Share Pin").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            } catch (e: Exception) {
                android.util.Log.e("CircleToSearch", "Failed to share pinned image", e)
            }
            try { windowManager?.removeView(menuLayout) } catch (e: Exception) {}
        })

        menuLayout.addView(createTextActionButton("DELETE") {
            try {
                windowManager?.removeView(anchorView)
                windowManager?.removeView(menuLayout)
            } catch (e: Exception) {}
        })

        val saveBtn = createTextActionButton("SAVE") {
            val success = ImageUtils.saveToGallery(this@CircleToSearchAccessibilityService, bitmap)
            android.widget.Toast.makeText(this@CircleToSearchAccessibilityService, if (success) "Saved to Gallery" else "Save failed", android.widget.Toast.LENGTH_SHORT).show()
            try { windowManager?.removeView(menuLayout) } catch (e: Exception) {}
        }
        menuLayout.addView(saveBtn)
        
        for (i in 0 until menuLayout.childCount - 1) {
            (menuLayout.getChildAt(i).layoutParams as android.widget.LinearLayout.LayoutParams).marginEnd = (8 * displayMetrics.density).toInt()
        }

        menuLayout.measure(View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, View.MeasureSpec.AT_MOST), View.MeasureSpec.UNSPECIFIED)
        val measuredMenuWidth = menuLayout.measuredWidth
        val measuredMenuHeight = menuLayout.measuredHeight

        val menuParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        menuParams.gravity = Gravity.TOP or Gravity.START
        
        var targetX = anchorParams.x + (anchorParams.width / 2) - (measuredMenuWidth / 2)
        
        if (targetX < menuPadding) targetX = (menuPadding).toInt()
        if (targetX + measuredMenuWidth > displayMetrics.widthPixels - menuPadding) {
            targetX = (displayMetrics.widthPixels - measuredMenuWidth - menuPadding).toInt()
        }
        menuParams.x = targetX.toInt()
        
        val yPadding = (12 * displayMetrics.density).toInt()
        menuParams.y = if (anchorParams.y > measuredMenuHeight + yPadding) {
            anchorParams.y - measuredMenuHeight - yPadding
        } else {
            anchorParams.y + anchorParams.height + yPadding
        }

        try {
            windowManager?.addView(menuLayout, menuParams)
            onMenuCreated(menuLayout)
        } catch (e: Exception) {
            android.util.Log.e("CircleToSearch", "Failed to add menu view", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            copyTextManager?.rescanNodes()
        }
    }

    override fun onInterrupt() {}

    companion object {
        var instance: CircleToSearchAccessibilityService? = null
            private set
            
        fun setCopyTextManager(manager: CopyTextOverlayManager?) {
            instance?.copyTextManager = manager
        }

        fun triggerCapture() {
            instance?.performCapture(null)
        }

        fun pinArea(bitmap: Bitmap, rect: android.graphics.Rect) {
            instance?.showPinnedArea(bitmap, rect)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
        hideBubble()
    }
}
