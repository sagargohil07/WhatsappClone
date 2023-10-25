package com.thinkwik.communimate.widget.story

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.thinkwik.communimate.R

class StoryView : View {
    companion object {
        const val STORY_IMAGE_RADIUS_IN_DP = 6
        const val STORY_INDICATOR_WIDTH_IN_DP = 4
        const val SPACE_BETWEEN_IMAGE_AND_INDICATOR = 4
        const val START_ANGLE = 270
        const val PENDING_INDICATOR_COLOR = "#009988"
        const val VISITED_INDICATOR_COLOR = "#757C85"
        var ANGEL_OF_GAP = 15
    }

    private lateinit var storyPreference: StoryPreference

    private var mStoryImageRadiusInPx: Int = 0
    private var mStoryIndicatorWidthInPx: Int = 0
    private var mSpaceBetweenImageAndIndicator: Int = 0
    private var mPendingIndicatorColor: Int = 0
    private var mVisitedIndicatorColor: Int = 0

    private var mViewWidth: Int = 0
    private var mViewHeight: Int = 0

    private var mIndicatoryOffset: Int = 0
    private var mIndicatorImageOffset: Int = 0
    private var mIndicatorImageRect: Rect = Rect()

    private lateinit var resources: Resources
    private var storyImageUris: ArrayList<StoryModel> = ArrayList()

    private lateinit var mIndicatorPaint: Paint
    private var indicatorCount: Int = 0
    private var indicatorSweepAngle: Int = 0
    private var mIndicatorImageBitmap: Bitmap? = null

    private lateinit var mContext: AppCompatActivity

    constructor(context: Context) : super(context) {
        Log.d("StoryView", "constructor(context: Context) : ")
        init(context)
        setDefaults()
    }

    private fun init(context: Context) {
        Log.d("StoryView", "init: ")
        storyPreference = StoryPreference(context)
        resources = context.resources
        storyImageUris = ArrayList()
        mIndicatorPaint = Paint()
        mIndicatorPaint.isAntiAlias = true
        mIndicatorPaint.style = Paint.Style.STROKE
        mIndicatorPaint.strokeCap = Paint.Cap.ROUND
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
        Log.d("StoryView", "constructor(context: Context, attrs: AttributeSet?): ")
        val ta = context.obtainStyledAttributes(attrs, R.styleable.StoryView, 0, 0)
        try {
            mStoryImageRadiusInPx = getPxFromDp(
                ta.getDimension(
                    R.styleable.StoryView_storyImageRadius,
                    STORY_IMAGE_RADIUS_IN_DP.toFloat()
                ).toInt()
            )
            mStoryIndicatorWidthInPx = getPxFromDp(
                ta.getDimension(
                    R.styleable.StoryView_storyItemIndicatorWidth,
                    STORY_INDICATOR_WIDTH_IN_DP.toFloat()
                ).toInt()
            )
            mSpaceBetweenImageAndIndicator = getPxFromDp(
                ta.getDimension(
                    R.styleable.StoryView_spaceBetweenImageAndIndicator,
                    SPACE_BETWEEN_IMAGE_AND_INDICATOR.toFloat()
                ).toInt()
            )
            mPendingIndicatorColor = ta.getColor(
                R.styleable.StoryView_pendingIndicatorColor,
                Color.parseColor(PENDING_INDICATOR_COLOR)
            )
            mVisitedIndicatorColor = ta.getColor(
                R.styleable.StoryView_visitedIndicatorColor,
                Color.parseColor(VISITED_INDICATOR_COLOR)
            )
        } finally {
            ta.recycle()
        }
        prepareValues()
    }

    private fun prepareValues() {
        Log.d("StoryView", "prepareValues: ")
        mViewHeight =
            2 * (mStoryIndicatorWidthInPx + mSpaceBetweenImageAndIndicator + mStoryImageRadiusInPx)
        mViewWidth = mViewHeight
        mIndicatoryOffset = mStoryIndicatorWidthInPx / 2
        mIndicatorImageOffset = mStoryIndicatorWidthInPx + mSpaceBetweenImageAndIndicator
        mIndicatorImageRect.set(
            mIndicatorImageOffset,
            mIndicatorImageOffset,
            mViewWidth - mIndicatorImageOffset,
            mViewHeight - mIndicatorImageOffset
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d("StoryView", "onMeasure: ")
        val width = paddingStart + paddingEnd + mViewWidth
        val height = paddingTop + paddingBottom + mViewHeight
        val w = resolveSizeAndState(width, widthMeasureSpec, 0)
        val h = resolveSizeAndState(height, heightMeasureSpec, 0)
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("StoryView", "onDraw: ")
        mIndicatorPaint.color = mPendingIndicatorColor
        mIndicatorPaint.strokeWidth = mStoryIndicatorWidthInPx.toFloat()
        var startAngle = START_ANGLE + ANGEL_OF_GAP / 2
        for (i in 0 until indicatorCount) {
            mIndicatorPaint.color = getIndicatorColor(i)
            canvas.drawArc(
                mIndicatoryOffset.toFloat(),
                mIndicatoryOffset.toFloat(),
                (mViewWidth - mIndicatoryOffset).toFloat(),
                (mViewHeight - mIndicatoryOffset).toFloat(),
                startAngle.toFloat(),
                (indicatorSweepAngle - ANGEL_OF_GAP / 2).toFloat(),
                false,
                mIndicatorPaint
            )
            startAngle += (indicatorSweepAngle + ANGEL_OF_GAP / 2)
        }
        mIndicatorImageBitmap?.let {
            canvas.drawBitmap(it, null, mIndicatorImageRect, null)
        }
    }

    private fun setDefaults() {
        Log.d("StoryView", "setDefaults: ")
        mStoryImageRadiusInPx = getPxFromDp(STORY_IMAGE_RADIUS_IN_DP)
        mStoryIndicatorWidthInPx = getPxFromDp(STORY_INDICATOR_WIDTH_IN_DP)
        mSpaceBetweenImageAndIndicator = getPxFromDp(SPACE_BETWEEN_IMAGE_AND_INDICATOR)
        mPendingIndicatorColor = Color.parseColor(PENDING_INDICATOR_COLOR)
        mVisitedIndicatorColor = Color.parseColor(VISITED_INDICATOR_COLOR)
        prepareValues()
    }

    private fun getPxFromDp(dpValue: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpValue.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    fun setActivityContext(activityContext: AppCompatActivity) {
        this.mContext = activityContext
    }

    fun resetStoryVisits() {
        storyPreference.clearStoryPreferences()
    }

    fun getImageUris(): ArrayList<StoryModel> {
        return this.storyImageUris
    }

    fun setImageUris(imageUris: ArrayList<StoryModel>) {
        this.storyImageUris = imageUris
        this.indicatorCount = imageUris.size
        calculateSweepAngle(indicatorCount)
        invalidate()
        loadFirstImageBitmap()
    }

    private fun calculateSweepAngle(itemCounts: Int) {
        if (itemCounts == 1) {
            ANGEL_OF_GAP = 0
        }
        this.indicatorSweepAngle = (360 / itemCounts) - ANGEL_OF_GAP / 2
    }

    private fun loadFirstImageBitmap() {
        Glide.with(this)
            .asBitmap()
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .load(storyImageUris[0].imageUrl)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    mIndicatorImageBitmap = resource
                    invalidate()
                }
            })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            return true
        }
        return true
    }

    private fun getIndicatorColor(index: Int): Int {
        return if (storyPreference.isStoryVisited(storyImageUris[index].imageUrl!!)) mVisitedIndicatorColor else mPendingIndicatorColor
    }

}
