/*
 * Copyright 2018 KoFuk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.plusassign.odd

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator

/**
 * Oh my goodness! What an odd character he is! Ah, he's staring at me... Who on earth is him?
 *
 * OddView from https://gist.github.com/KoFuk/72ce736517ea0d07ed21f5458b92b27c
 */
class OddView(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
    : View(context, attrs, defStyleAttr) {
    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private val bodyPaint = instantiatePaint(Paint.ANTI_ALIAS_FLAG, 0xff0c6d11.toInt())
    private val eyeBasePaint = instantiatePaint(Paint.ANTI_ALIAS_FLAG, 0xfff1e1e9.toInt())
    private val eyeIrisPaint = instantiatePaint(Paint.ANTI_ALIAS_FLAG, 0xff1b1815.toInt())
    private val legArmPaint = instantiatePaint(Paint.ANTI_ALIAS_FLAG, 0xffbdaa71.toInt()).apply {
        style = Paint.Style.STROKE
    }
    private val legArmOvalPaint = instantiatePaint(Paint.ANTI_ALIAS_FLAG, 0xffbdaa71.toInt())
    private val footPaint = instantiatePaint(Paint.ANTI_ALIAS_FLAG, 0xff251f0e.toInt())
    private val handPaint = instantiatePaint(Paint.ANTI_ALIAS_FLAG, 0xff5f5946.toInt())

    // his green body
    private val body = RectF()
    // eyes
    private val eyeRBase = RectF()
    private val eyeLBase = RectF()
    private val eyeRIris = RectF()
    private val eyeLIris = RectF()
    // legs, foots and circle to hide end of the paths
    private val legR = Path()
    private val legL = Path()
    private val legROval = RectF()
    private val legLOval = RectF()
    private val footR = RectF()
    private val footL = RectF()
    // arms, hands and circle to hide end of the paths
    private val armR = Path()
    private val armL = Path()
    private val armROval = RectF()
    private val armLOval = RectF()
    private val handR = RectF()
    private val handL = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRoundRect(body, body.width() / 2, body.width() / 2, bodyPaint)
        canvas.drawOval(eyeRBase, eyeBasePaint)
        canvas.drawOval(eyeLBase, eyeBasePaint)
        canvas.drawOval(eyeRIris, eyeIrisPaint)
        canvas.drawOval(eyeLIris, eyeIrisPaint)
        canvas.drawPath(legR, legArmPaint)
        canvas.drawPath(legL, legArmPaint)
        canvas.drawOval(legROval, legArmOvalPaint)
        canvas.drawOval(legLOval, legArmOvalPaint)
        canvas.drawOval(footR, footPaint)
        canvas.drawOval(footL, footPaint)
        canvas.drawPath(armR, legArmPaint)
        canvas.drawPath(armL, legArmPaint)
        canvas.drawOval(armROval, legArmOvalPaint)
        canvas.drawOval(armLOval, legArmOvalPaint)
        canvas.drawOval(handR, handPaint)
        canvas.drawOval(handL, handPaint)
    }

    private var strokeWidth = 0f
    private var strokeWidthHalf = 0f
    private var strokeWidthThreeHalf = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initializePose()
    }

    /**
     * Initializes Path and RectF to provide initial pose in next invalidation.
     * Note: This method do NOT redraw. You have to invalidate to show updated pose.
     */
    private fun initializePose() {
        strokeWidth = wf / 22
        legArmPaint.strokeWidth = strokeWidth
        body.set(wf * 3 / 11, hf * 1 / 11, wf * 8 / 11, hf * 8 / 11)
        eyeRBase.set(wf * 6 / 11, hf * 3 / 22, wf * 7 / 11, hf * 5 / 22)
        eyeLBase.set(wf * 4 / 11, hf * 3 / 22, wf * 5 / 11, hf * 5 / 22)
        eyeRIris.set(wf * 25 / 44, hf * 7 / 44, wf * 27 / 44, hf * 9 / 44)
        eyeLIris.set(wf * 17 / 44, hf * 7 / 44, wf * 19 / 44, hf * 9 / 44)
        legR.reset()
        legR.moveTo(wf * 4 / 11, hf * 7 / 11)
        legR.lineTo(wf * 4 / 11, hf * 10 / 11)
        legL.reset()
        legL.moveTo(wf * 7 / 11, hf * 7 / 11)
        legL.lineTo(wf * 7 / 11, hf * 10 / 11)
        strokeWidthHalf = strokeWidth / 2
        legROval.set(wf * 4 / 11 - strokeWidthHalf, hf * 7 / 11 - strokeWidthHalf,
                wf * 4 / 11 + strokeWidthHalf, wf * 7 / 11 + strokeWidthHalf)
        legLOval.set(wf * 7 / 11 - strokeWidthHalf, hf * 7 / 11 - strokeWidthHalf,
                wf * 7 / 11 + strokeWidthHalf, wf * 7 / 11 + strokeWidthHalf)
        strokeWidthThreeHalf = strokeWidth * 3 / 2
        footR.set(wf * 4 / 11 - strokeWidthThreeHalf, hf * 10 / 11 - strokeWidthThreeHalf,
                wf * 4 / 11 + strokeWidthThreeHalf, hf * 10 / 11 + strokeWidthThreeHalf)
        footL.set(wf * 7 / 11 - strokeWidthThreeHalf, hf * 10 / 11 - strokeWidthThreeHalf,
                wf * 7 / 11 + strokeWidthThreeHalf, hf * 10 / 11 + strokeWidthThreeHalf)
        armR.reset()
        armR.moveTo(wf * 15 / 22, hf * 3 / 11)
        armR.quadTo(wf * 19 / 22, hf * 3 / 11, wf * 9 / 11, hf * 6 / 11)
        armL.reset()
        armL.moveTo(wf * 7 / 22, hf * 3 / 11)
        armL.quadTo(wf * 3 / 22, hf * 3 / 11, wf * 2 / 11, hf * 6 / 11)
        armROval.set(wf * 15 / 22 - strokeWidthHalf, hf * 3 / 11 - strokeWidthHalf,
                wf * 15 / 22 + strokeWidthHalf, hf * 3 / 11 + strokeWidthHalf)
        armLOval.set(wf * 7 / 22 - strokeWidthHalf, hf * 3 / 11 - strokeWidthHalf,
                wf * 7 / 22 + strokeWidthHalf, hf * 3 / 11 + strokeWidthHalf)
        handR.set(wf * 9 / 11 - strokeWidthThreeHalf, hf * 6 / 11 - strokeWidthThreeHalf,
                wf * 9 / 11 + strokeWidthThreeHalf, hf * 6 / 11 + strokeWidthThreeHalf)
        handL.set(wf * 2 / 11 - strokeWidthThreeHalf, hf * 6 / 11 - strokeWidthThreeHalf,
                wf * 2 / 11 + strokeWidthThreeHalf, hf * 6 / 11 + strokeWidthThreeHalf)
    }

    private var eyeAnimator: AnimatorSet? = null

    /**
     * Provides action that show face from parent's bottom and look around.
     */
    fun showFace() {
        ensureVisible()
        transformEye(CoordinateHolder(wf * 25 / 44, hf * 2 / 11),
                CoordinateHolder(wf * 17 / 44, hf * 2 / 11))
        transformArm(CoordinateHolder(wf * 9 / 11, hf * 6 / 11),
                CoordinateHolder(wf * 2 / 11, hf * 6 / 11))
        val parent = parent as View
        translationX = parent.width.toFloat() / 2 - width / 2
        translationY = parent.height.toFloat()
        animate().translationY(parent.height - height.toFloat() / 3).setDuration(300)
                .withEndAction {
                    val r = ValueAnimator.ofFloat(wf * 25 / 44, wf * 27 / 44)
                    r.repeatCount = 1
                    r.repeatMode = ValueAnimator.REVERSE
                    r.duration = 1000
                    val l = ValueAnimator.ofFloat(wf * 17 / 44, wf * 19 / 44)
                    l.repeatCount = 1
                    l.repeatMode = ValueAnimator.REVERSE
                    l.duration = 1000
                    l.addUpdateListener {
                        transformEye(CoordinateHolder(l.animatedValue as Float, hf * 2 / 11),
                                CoordinateHolder(r.animatedValue as Float, hf * 2 / 11))
                    }
                    eyeAnimator = AnimatorSet()
                    eyeAnimator!!.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            if (!isTouchMoved) {
                                animate().translationY(parent.height.toFloat()).setDuration(100).withEndAction {
                                    ensureGone()
                                }.start()
                            }
                        }
                    })
                    eyeAnimator!!.playTogether(r, l)
                    eyeAnimator!!.start()
                }.start()
        transformArm(CoordinateHolder(wf * 13 / 22, hf / 3), CoordinateHolder(wf * 9 / 22, hf / 3))
    }

    /**
     * Provides action that jump from parent's bottom to its medium position.
     */
    fun jump() {
        initializePose()
        invalidate()
        ensureVisible()
        animate().translationY((parent as View).height.toFloat() / 2).withEndAction {
            animate().translationY((parent as View).height.toFloat()).withEndAction {
                ensureGone()
            }.start()
        }.start()
    }

    private fun transformArm(handR: CoordinateHolder, handL: CoordinateHolder) {
        val strokeWidthThreeHalf = width.toFloat() * 3 / 44
        armR.reset()
        armL.reset()
        armR.moveTo(wf * 15 / 22, hf * 3 / 11)
        armR.quadTo(wf * 19 / 22, hf * 3 / 11, handR.x, handR.y)
        armL.moveTo(wf * 7 / 22, hf * 3 / 11)
        armL.quadTo(wf * 3 / 22, hf * 3 / 11, handL.x, handL.y)
        this.handR.set(handR.x - strokeWidthThreeHalf, handR.y - strokeWidthThreeHalf,
                handR.x + strokeWidthThreeHalf, handR.y + strokeWidthThreeHalf)
        this.handL.set(handL.x - strokeWidthThreeHalf, handL.y - strokeWidthThreeHalf,
                handL.x + strokeWidthThreeHalf, handL.y + strokeWidthThreeHalf)
        invalidate()
    }

    private fun transformFoot(footR: CoordinateHolder, footL: CoordinateHolder) {
        legR.reset()
        legL.reset()
        legR.moveTo(wf * 4 / 11, hf * 7 / 11)
        legR.quadTo(wf * 4 / 11, hf * 17 / 22, footR.x, footR.y)
        legL.moveTo(wf * 7 / 11, hf * 7 / 11)
        legL.quadTo(wf * 7 / 11, hf * 17 / 22, footL.x, footL.y)
        this.footR.set(footR.x - strokeWidthThreeHalf, footR.y - strokeWidthThreeHalf,
                footR.x + strokeWidthThreeHalf, footR.y + strokeWidthThreeHalf)
        this.footL.set(footL.x - strokeWidthThreeHalf, footL.y - strokeWidthThreeHalf,
                footL.x + strokeWidthThreeHalf, footL.y + strokeWidthThreeHalf)
        invalidate()
    }

    private fun transformEye(eyeR: CoordinateHolder, eyeL: CoordinateHolder) {
        val eyeRadius = wf / 44
        eyeRIris.set(eyeR.x - eyeRadius, eyeR.y - eyeRadius, eyeR.x + eyeRadius, eyeR.y + eyeRadius)
        eyeLIris.set(eyeL.x - eyeRadius, eyeL.y - eyeRadius, eyeL.x + eyeRadius, eyeL.y + eyeRadius)
        invalidate()
    }

    private val wf: Float
        get() = width.toFloat()

    private val hf: Float
        get() = height.toFloat()

    private fun instantiatePaint(flag: Int, color: Int): Paint {
        return Paint(flag).apply { this.color = color }
    }

    private var isTouchMoved = false

    private var lastX = 0f
    private var lastY = 0f
    private var moveX = 0f
    private var moveY = 0f

    @Suppress("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            lastX = event.x
            lastY = event.y
            moveX = translationX
            moveY = translationY
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            animate().cancel()
            moveX += event.x - lastX
            moveY += event.y - lastY
            translationX = moveX
            translationY = moveY
            if (!isTouchMoved) {
                isTouchMoved = true
                // we cancel it as eyes should be kept centered
                eyeAnimator?.cancel()
                // whoa! Do stop what you d...
                struggle()
            }
        } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            if (isTouchMoved) {
                isTouchMoved = false
                cancelStruggle()
                // "Down, down, down; (alice came to wonderland)"
                animate().translationY((parent as View).height.toFloat()).setDuration(100).withEndAction {
                    // let's look out once again
                    animate().translationY((parent as View).height - height.toFloat() / 5).withEndAction {
                        animate().translationY((parent as View).height.toFloat()).setStartDelay(500).withEndAction {
                            ensureGone()
                        }.start()
                    }.setStartDelay(800).start()
                }.start()
            }
        }
        return true
    }

    private val struggleAnimators = mutableListOf<Animator>()

    private fun struggle() {
        ensureVisible()
        transformEye(CoordinateHolder(wf * 9 / 22, hf * 2 / 11),
                CoordinateHolder(wf * 13 / 22, hf * 2 / 11))
        val armR = ValueAnimator.ofFloat(wf * 10 / 11, wf * 7 / 11).apply {
            duration = 100
            repeatCount = -1
            repeatMode = ValueAnimator.REVERSE
        }
        struggleAnimators.add(armR)
        armR.start()
        val armL = ValueAnimator.ofFloat(wf / 11, wf * 4 / 11).apply {
            duration = 90
            repeatCount = -1
            repeatMode = ValueAnimator.REVERSE
        }
        struggleAnimators.add(armL)
        armL.start()
        val footR = ValueAnimator.ofFloat(wf * 2 / 11, wf * 5 / 11).apply {
            duration = 95
            repeatCount = -1
            repeatMode = ValueAnimator.REVERSE
        }
        struggleAnimators.add(footR)
        footR.start()
        val footL = ValueAnimator.ofFloat(wf * 9 / 11, wf * 6 / 11).apply {
            duration = 85
            repeatCount = -1
            repeatMode = ValueAnimator.REVERSE
        }
        struggleAnimators.add(footL)
        footL.start()
        armR.addUpdateListener {
            transformArm(CoordinateHolder(armR.animatedValue as Float, hf * 6 / 11),
                    CoordinateHolder(armL.animatedValue as Float, hf * 6 / 11))
            transformFoot(CoordinateHolder(footR.animatedValue as Float, hf * 10 / 11),
                    CoordinateHolder(footL.animatedValue as Float, hf * 10 / 11))
        }
    }

    private fun cancelStruggle() {
        struggleAnimators.forEach {
            it.cancel()
        }
        struggleAnimators.clear()
    }

    override fun animate(): ViewPropertyAnimator {
        val a = super.animate()
        return a.setStartDelay(0).setDuration(200)
    }

    private data class CoordinateHolder(val x: Float, val y: Float)

    private fun ensureVisible() {
        visibility = VISIBLE
    }

    private fun ensureGone() {
        visibility = GONE
    }
}
