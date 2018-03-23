package com.chronoscoper.android.classschedule2.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.support.annotation.MainThread
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView

class ImageClipRangeView(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
    : ImageView(context, attrs, defStyleAttr) {
    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    init {
        isDrawingCacheEnabled = true
    }

    private val rangePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }
    private var drawRangeIndicator = true
    private val clipRange = Rect()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val centerX = left + w / 2
        val centerY = top + h / 2
        val radius = Math.min(w, h) / 2
        clipRange.set(centerX - radius, centerY - radius,
                centerX + radius, centerY + radius)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.clipRect(clipRange)
        super.onDraw(canvas)
        if (drawRangeIndicator) canvas.drawRect(clipRange, rangePaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_MOVE) {
            handleDrag(event.x.toInt(), event.y.toInt())
        }
        return true
    }

    private val backup = Rect()

    private fun handleDrag(x: Int, y: Int) {
        backup.set(clipRange)
        if (x in clipRange.left + 50..clipRange.right - 50
                && y in clipRange.top + 50..clipRange.bottom - 50) {
            val diffX = x - clipRange.centerX()
            val diffY = y - clipRange.centerY()
            clipRange.offset(diffX, diffY)
        } else {
            if (y in clipRange.top..clipRange.bottom) {
                if (x in clipRange.left - 50..clipRange.left + 50) {
                    clipRange.left = x
                } else if (x in clipRange.right - 50..clipRange.right + 50) {
                    clipRange.right = x
                }
                clipRange.bottom = clipRange.top + clipRange.width()
            }
            if (x in clipRange.left..clipRange.right) {
                if (y in clipRange.top - 50..clipRange.top + 50) {
                    clipRange.top = y
                } else if (y in clipRange.bottom - 50..clipRange.bottom + 50) {
                    clipRange.bottom = y
                }
                clipRange.right = clipRange.left + clipRange.height()
            }
        }
        if (clipRange.width() > width) {
            val over = (clipRange.width() - width) / 2
            clipRange.inset(over, over)
        } else if (clipRange.height() > height) {
            val over = (clipRange.height() - height) / 2
            clipRange.inset(over, over)
        }
        if (clipRange.left < left) {
            clipRange.offset(left - clipRange.left, 0)
        } else if (clipRange.right > right) {
            clipRange.offset(right - clipRange.right, 0)
        }
        if (clipRange.top < top) {
            clipRange.offset(0, top - clipRange.top)
        } else if (clipRange.bottom > bottom) {
            clipRange.offset(0, bottom - clipRange.bottom)
        }
        if (clipRange.width() < 200) {
            clipRange.set(backup)
        }
        invalidate()
    }

    @MainThread
    fun getClippedImage(): Bitmap {
        drawRangeIndicator = false
        invalidate()
        val drawing = drawingCache
        drawRangeIndicator = true
        return Bitmap.createBitmap(drawing, clipRange.left, clipRange.top,
                clipRange.width(), clipRange.height())
    }
}
