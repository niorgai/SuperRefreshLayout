package niorgai.qiu.superrefreshlayout.refresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.ScrollView;

import niorgai.qiu.superrefreshlayout.R;

/**
 * Created by qiu on 9/8/15.
 */
public class SuperRefreshLayout extends ViewGroup {

    private static final float MAX_SWIPE_DISTANCE_FACTOR = .6f;

    //开始刷新的距离,距离顶部120dp
    private static final int REFRESH_TRIGGER_DISTANCE = 120;

    private static final String LOG_TAG = SwipeRefreshLayout.class.getSimpleName();

    //刷新的View的大小 = 25dp
    private static final int CIRCLE_DIAMETER = 25;

    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    private static final int INVALID_POINTER = -1;
    private static final float DRAG_RATE = .5f;

    // Default offset in dips from the top of the view to where the progress spinner should stop
    private static final int DEFAULT_CIRCLE_TARGET = 64;

    private View mTarget; // the target of the gesture

    private boolean mRefreshing = false;
    private int mTouchSlop;
    private float mTotalDragDistance = -1;
    private int mMediumAnimationDuration;
    private int mCurrentTargetOffsetTop;
    // Whether or not the starting offset has been determined.
    private boolean mOriginalOffsetCalculated = false;

    private float mInitialMotionY;
    private float mInitialDownY;
    private boolean mIsBeingDragged;
    private int mActivePointerId = INVALID_POINTER;

    // 拉动过程中是否需要对LoadingView进行scale
    private boolean mScale = true;

    // 拉动过程中是否需要对LoadingView透明
    private boolean mAlpha = true;

    // Target is returning to its start offset because it was cancelled or a
    // refresh was triggered.
    private boolean mReturningToStart;
    private final DecelerateInterpolator mDecelerateInterpolator;
    private static final int[] LAYOUT_ATTRS = new int[] {
            android.R.attr.enabled,
            R.styleable.SuperRefreshLayout_direction
    };

    private CommonLoadingView mLoadingView;

    private CommonLoadingView mTopLoadingView;

    private CommonLoadingView mBottomLoadingView;

    private int mTopLoadingViewIndex = -1;

    protected int mFrom;

    private float mStartingScale;

    protected int mOriginalOffsetTop;

    private float mSpinnerFinalOffset;

    private boolean mNotify;

    private int mCircleWidth;

    private int mCircleHeight;

    //刷新动态的Listener,在动画结束时刷新结束
    private Animation.AnimationListener mRefreshListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mRefreshing) {
                mLoadingView.startAnimation();
                // Make sure the progress view is fully visible
                if (mNotify) {
                    if (mBothDirection) {
                        if (mSuperRefreshListener2 != null) {
                            if (mDirection == RefreshDirection.PULL_FROM_TOP) {
                                mSuperRefreshListener2.onRefreshFromTop();
                            } else {
                                mSuperRefreshListener2.onRefreshFromBottom();
                            }
                        }
                    } else {
                        if (mSuperRefreshListener != null) {
                            mSuperRefreshListener.onRefreshFromTop();
                        } else if (mSuperRefreshListener2 != null) {
                            if (mDirection == RefreshDirection.PULL_FROM_TOP) {
                                mSuperRefreshListener2.onRefreshFromTop();
                            } else {
                                mSuperRefreshListener2.onRefreshFromBottom();
                            }
                        }
                    }
                }
            } else {
                mLoadingView.setVisibility(View.GONE);
                // Return the circle to its start position
                if (mScale) {
                    setAnimationProgress(0 /* animation complete and view is hidden */);
                } else {
                    setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCurrentTargetOffsetTop);
                }
            }
            mCurrentTargetOffsetTop = mLoadingView.getTop();
        }
    };

    //自定义变量
    //是否是两种模式(下拉\上拉)都可以刷新
    private boolean mBothDirection;

    //当前刷新的模式
    private RefreshDirection mDirection;

    //刷新接口
    private SuperRefreshListener mSuperRefreshListener;

    private SuperRefreshListener2 mSuperRefreshListener2;

    /**
     * Simple constructor to use when creating a SwipeRefreshLayout from code.
     *
     * @param context
     */
    public SuperRefreshLayout(Context context) {
        this(context, null);
    }

    /**
     * Constructor that is called when inflating SwipeRefreshLayout from XML.
     *
     * @param context
     * @param attrs
     */
    public SuperRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mMediumAnimationDuration = getResources().getInteger(
                android.R.integer.config_mediumAnimTime);

        setWillNotDraw(false);
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);

        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));

        //获取direction
        RefreshDirection direction
                = RefreshDirection.getDirectionFromValue(a.getInt(R.styleable.SuperRefreshLayout_direction, 0));
        if (direction == RefreshDirection.BOTH) {
            mDirection = RefreshDirection.PULL_FROM_TOP;
            mBothDirection = true;
        } else {
            mDirection = RefreshDirection.PULL_FROM_TOP;
            mBothDirection = false;
        }
        a.recycle();

        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mCircleWidth = (int) (CIRCLE_DIAMETER * metrics.density);
        mCircleHeight = (int) (CIRCLE_DIAMETER * metrics.density);

        createTopLoadingView();
        createBottomLoadingView();
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        // the absolute offset has to take into account that the circle starts at an offset
        mSpinnerFinalOffset = DEFAULT_CIRCLE_TARGET * metrics.density;
    }

    //绘制子View的顺序
    protected int getChildDrawingOrder(int childCount, int i) {
        if (mTopLoadingViewIndex < 0) {
            return i;
        } else if (i == childCount - 1) {
            // Draw the selected child last
            return mTopLoadingViewIndex;
        } else if (i >= mTopLoadingViewIndex) {
            // Move the children after the selected child earlier one
            return i + 1;
        } else {
            // Keep the children before the selected child the same
            return i;
        }
    }

    //创建顶部的LoadingView
    private void createTopLoadingView() {
        mTopLoadingView = new TopLoadingView(getContext());
        mTopLoadingView.setVisibility(View.GONE);
        addView(mTopLoadingView);
    }

    //创建底部的LoadingView
    private void createBottomLoadingView() {
        mBottomLoadingView = new BottomLoadingView(getContext());
        mBottomLoadingView.setVisibility(View.GONE);
        addView(mBottomLoadingView);
    }

    /**
     * Notify the widget that refresh state has changed. Do not call this when
     * refresh is triggered by a swipe gesture.
     *
     * @param refreshing Whether or not the view should show refresh progress.
     */
    public void setRefreshing(final boolean refreshing) {
        if (refreshing && mRefreshing != refreshing) {
            post(new Runnable() {
                @Override
                public void run() {
                    // scale and show
                    mRefreshing = refreshing;
                    int endTarget = 0;
                    switch (mDirection) {
                        case PULL_FROM_BOTTOM:
                            endTarget = getMeasuredHeight() - (int) (mSpinnerFinalOffset);
                            break;
                        case PULL_FROM_TOP:
                        default:
                            endTarget = (int) (mSpinnerFinalOffset - Math.abs(mOriginalOffsetTop));
                            break;
                    }
                    setTargetOffsetTopAndBottom(endTarget - mCurrentTargetOffsetTop);
                    mNotify = false;
                    startScaleUpAnimation(mRefreshListener);
                }
            });
        } else {
            setRefreshing(refreshing, false /* notify */);
        }
    }

    /**
     * 设置view旋转的百分比(0-1)
     * @param pro
     */
    private void setAnimationProgress(float pro) {
        //y = 2x/3 + 1/3,设置其从30%开始增长到100%
        float progress = ((pro * 2) / 3) + (1f / 3f);
        if (progress == 1f/3f) {
            progress = 0;
        }
        ViewCompat.setScaleX(mLoadingView, progress);
        ViewCompat.setScaleY(mLoadingView, progress);
        mLoadingView.setProgress(progress);
        if (mAlpha) {
            mLoadingView.setViewAlpha(progress);
        }
    }

    private void setRefreshing(boolean refreshing, final boolean notify) {
        if (mRefreshing != refreshing) {
            mNotify = notify;
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {
                animateOffsetToCorrectPosition(mCurrentTargetOffsetTop, mRefreshListener);
            } else {
                startScaleDownAnimation(mRefreshListener);
                mLoadingView.stopAnimation();
            }
        }
    }

    //开始逐渐放大的动画
    private void startScaleUpAnimation(Animation.AnimationListener listener) {
        mLoadingView.setVisibility(View.VISIBLE);
        Animation mScaleAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                setAnimationProgress(interpolatedTime);
            }
        };
        mScaleAnimation.setDuration(mMediumAnimationDuration);
        if (listener != null) {
            mLoadingView.setAnimationListener(listener);
        }
        mLoadingView.clearAnimation();
        mLoadingView.startAnimation(mScaleAnimation);
    }

    /**
     * Loading动作结束后变小消失的动画
     * 改为向上滑动消失*/
    private void startScaleDownAnimation(Animation.AnimationListener listener) {
//        Animation mScaleDownAnimation = new Animation() {
//            @Override
//            public void applyTransformation(float interpolatedTime, Transformation t) {
//                setAnimationProgress(1 - interpolatedTime);
//            }
//        };
//        //动画时间150ms
//        mScaleDownAnimation.setDuration(150);
//        mLoadingView.setAnimationListener(listener);
//        mLoadingView.clearAnimation();
//        mLoadingView.startAnimation(mScaleDownAnimation);
        animateOffsetToStartPosition(mCurrentTargetOffsetTop, listener);
    }

    /**
     * @return Whether the SwipeRefreshWidget is actively showing refresh
     *         progress.
     */
    public boolean isRefreshing() {
        return mRefreshing;
    }

    //确认子View(刷新的View),同时设置开始刷新的高度
    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTarget == null) {
            for (int i=getChildCount()-1; i>=0; i--) {
                View child = getChildAt(i);
                if (!child.equals(mTopLoadingView) && !child.equals(mBottomLoadingView)) {
                    mTarget = child;
                    break;
                }
            }
        }
        //设置开始Loading的距离
        if (mTotalDragDistance == -1) {
            if (getParent() != null && ((View)getParent()).getHeight() > 0) {
                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                mTotalDragDistance = (int) Math.min(
                        ((View) getParent()) .getHeight() * MAX_SWIPE_DISTANCE_FACTOR,
                        REFRESH_TRIGGER_DISTANCE * metrics.density);
            }
        }
    }

    /**
     * Set the distance to trigger a sync in dips
     *
     * @param distance
     */
    public void setDistanceToTriggerSync(int distance) {
        mTotalDragDistance = distance;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        final View child = mTarget;
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        int circleWidth = mLoadingView.getMeasuredWidth();
        int circleHeight = mLoadingView.getMeasuredHeight();
        mLoadingView.layout((width / 2 - circleWidth / 2), mCurrentTargetOffsetTop,
                (width / 2 + circleWidth / 2), mCurrentTargetOffsetTop + circleHeight);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        mTarget.measure(MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        if (!mOriginalOffsetCalculated) {
            mOriginalOffsetCalculated = true;
            switch (mDirection) {
                case PULL_FROM_BOTTOM:
                    mLoadingView = mBottomLoadingView;
                    mCurrentTargetOffsetTop = mOriginalOffsetTop = getMeasuredHeight();
                    break;
                case PULL_FROM_TOP:
                default:
                    mLoadingView = mTopLoadingView;
                    mCurrentTargetOffsetTop = mOriginalOffsetTop = -mLoadingView.getMeasuredHeight();
                    break;
            }
        }
        if (mLoadingView != null) {
            mLoadingView.measure(MeasureSpec.makeMeasureSpec(mCircleWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mCircleHeight, MeasureSpec.EXACTLY));
        }
        mTopLoadingViewIndex = -1;
        // Get the index of the loadingView.
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == mLoadingView) {
                mTopLoadingViewIndex = index;
                break;
            }
        }
    }

    /**
     * 如果targetView不是可滚动的View,遍历其childView找到第一个可滚动的View
     * 判断该View能否继续往上拉动
     * 否则判断targetView能否继续往上拉动
     */
    public boolean canChildScrollUp() {
        if (!isScrollableChildView(mTarget) && mTarget instanceof ViewGroup) {
            ViewGroup group = ((ViewGroup) mTarget);
            for (int i=0; i<group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (isScrollableChildView(child)) {
                    return ViewCompat.canScrollVertically(child, -1);
                }
            }
        }
        return ViewCompat.canScrollVertically(mTarget, -1);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = MotionEventCompat.getActionMasked(ev);

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        switch (mDirection) {
            case PULL_FROM_BOTTOM:
                mLoadingView = mBottomLoadingView;
                if (!isEnabled() || mReturningToStart || (!mBothDirection && canChildScrollDown()) || mRefreshing) {
                    // Fail fast if we're not in a state where a swipe is possible
                    return false;
                }
                break;
            case PULL_FROM_TOP:
            default:
                mLoadingView = mTopLoadingView;
                if (!isEnabled() || mReturningToStart || (!mBothDirection && canChildScrollUp()) || mRefreshing) {
                    // Fail fast if we're not in a state where a swipe is possible
                    return false;
                }
                break;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTargetOffsetTopAndBottom(mOriginalOffsetTop - mLoadingView.getTop());
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                final float initialDownY = getMotionEventY(ev, mActivePointerId);
                if (initialDownY == -1) {
                    return false;
                }
                mInitialDownY = initialDownY;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
                }

                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                if (mBothDirection) {
                    if (y > mInitialDownY) {
                        setCurrentSwipeDirection(RefreshDirection.PULL_FROM_TOP);
                    } else if (y < mInitialDownY) {
                        setCurrentSwipeDirection(RefreshDirection.PULL_FROM_BOTTOM);
                    }
                    if ((mDirection == RefreshDirection.PULL_FROM_BOTTOM && canChildScrollDown())
                            || (mDirection == RefreshDirection.PULL_FROM_TOP && canChildScrollUp())) {
                        mInitialDownY = y;
                        return false;
                    }
                }
                float yDiff;
                switch (mDirection) {
                    case PULL_FROM_BOTTOM:
                        yDiff = mInitialDownY - y;
                        break;
                    case PULL_FROM_TOP:
                    default:
                        yDiff = y - mInitialDownY;
                        break;
                }
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    switch (mDirection) {
                        case PULL_FROM_BOTTOM:
                            mInitialMotionY = mInitialDownY - mTouchSlop;
                            break;
                        case PULL_FROM_TOP:
                        default:
                            mInitialMotionY = mInitialDownY + mTouchSlop;
                            break;
                    }
                    mIsBeingDragged = true;
                }
                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // Nope.
    }

    private boolean isAnimationRunning(Animation animation) {
        return animation != null && animation.hasStarted() && !animation.hasEnded();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        switch (mDirection) {
            case PULL_FROM_BOTTOM:
                if (!isEnabled() || mReturningToStart || canChildScrollDown() || mRefreshing) {
                    // Fail fast if we're not in a state where a swipe is possible
                    return false;
                }
                break;
            case PULL_FROM_TOP:
            default:
                if (!isEnabled() || mReturningToStart || canChildScrollUp() || mRefreshing) {
                    // Fail fast if we're not in a state where a swipe is possible
                    return false;
                }
                break;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                break;

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                final float y = MotionEventCompat.getY(ev, pointerIndex);
                float overScrollTop;
                switch (mDirection) {
                    case PULL_FROM_BOTTOM:
                        overScrollTop = (mInitialMotionY - y) * DRAG_RATE;
                        break;
                    case PULL_FROM_TOP:
                    default:
                        overScrollTop = (y - mInitialMotionY) * DRAG_RATE;
                        break;
                }
                if (mIsBeingDragged) {
                    float originalDragPercent = overScrollTop / mTotalDragDistance;
                    if (originalDragPercent < 0) {
                        return false;
                    }
                    float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
                    float adjustedPercent = (float) Math.max(dragPercent - .4, 0) * 5 / 3;
                    float extraOS = Math.abs(overScrollTop) - mTotalDragDistance;
                    float slingshotDist = mSpinnerFinalOffset;
                    float tensionSlingshotPercent = Math.max(0,
                            Math.min(extraOS, slingshotDist * 2) / slingshotDist);
                    float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                            (tensionSlingshotPercent / 4), 2)) * 2f;
                    float extraMove = (slingshotDist) * tensionPercent * 2;

                    int targetY;
                    switch (mDirection) {
                        case PULL_FROM_BOTTOM:
                            targetY = mOriginalOffsetTop - (int) ((slingshotDist * dragPercent) + extraMove);
                            break;
                        case PULL_FROM_TOP:
                        default:
                            targetY = mOriginalOffsetTop + (int) ((slingshotDist * dragPercent) + extraMove);
                            break;
                    }
                    // where 1.0f is a full circle
                    if (mLoadingView.getVisibility() != View.VISIBLE) {
                        mLoadingView.setVisibility(View.VISIBLE);
                    }
                    if (!mScale) {
                        ViewCompat.setScaleX(mLoadingView, 1f);
                        ViewCompat.setScaleY(mLoadingView, 1f);
                    }
                    if (overScrollTop < mTotalDragDistance) {
                        if (mScale) {
                            setAnimationProgress(overScrollTop / mTotalDragDistance);
                        }

                    }
                    setTargetOffsetTopAndBottom(targetY - mCurrentTargetOffsetTop);
                }
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mActivePointerId == INVALID_POINTER) {
                    if (action == MotionEvent.ACTION_UP) {
                        Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    }
                    return false;
                }
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                float overScrollTop;
                switch (mDirection) {
                    case PULL_FROM_BOTTOM:
                        overScrollTop = (mInitialMotionY - y) * DRAG_RATE;
                        break;
                    case PULL_FROM_TOP:
                    default:
                        overScrollTop = (y - mInitialMotionY) * DRAG_RATE;
                        break;
                }
                mIsBeingDragged = false;
                if (overScrollTop > mTotalDragDistance) {
                    setRefreshing(true, true /* notify */);
                } else {
                    // cancel refresh
                    mRefreshing = false;
                    Animation.AnimationListener listener = null;
                    if (!mScale) {
                        listener = new Animation.AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                if (!mScale) {
                                    startScaleDownAnimation(null);
                                }
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }

                        };
                    }
                    animateOffsetToStartPosition(mCurrentTargetOffsetTop, listener);
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
        }

        return true;
    }

    /**
     * 从当前位置滑动至Loading的位置
     */
    private void animateOffsetToCorrectPosition(int from, Animation.AnimationListener listener) {
        mFrom = from;
        mAnimateToCorrectPosition.reset();
        //动画时间200ms
        mAnimateToCorrectPosition.setDuration(200);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        if (listener != null) {
            mLoadingView.setAnimationListener(listener);
        }
        mLoadingView.clearAnimation();
        mLoadingView.startAnimation(mAnimateToCorrectPosition);
    }

    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop;
            int endTarget;
            switch (mDirection) {
                case PULL_FROM_BOTTOM:
                    endTarget = getMeasuredHeight() - (int) (mSpinnerFinalOffset);
                    break;
                case PULL_FROM_TOP:
                default:
                    endTarget = (int) (mSpinnerFinalOffset - Math.abs(mOriginalOffsetTop));
                    break;
            }
            targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop - mLoadingView.getTop();
            setTargetOffsetTopAndBottom(offset);
        }
    };

    /**
     * 动画滑动到开始的位置
     */
    private void animateOffsetToStartPosition(int from, Animation.AnimationListener listener) {
        if (mScale) {
            // Scale the item back down
            startScaleDownReturnToStartAnimation(from, listener);
        } else {
            mFrom = from;
            mAnimateToStartPosition.reset();
            //动画时间200ms
            mAnimateToStartPosition.setDuration(200);
            mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
            if (listener != null) {
                mLoadingView.setAnimationListener(listener);
            }
            mLoadingView.clearAnimation();
            mLoadingView.startAnimation(mAnimateToStartPosition);
        }
    }

    private void moveToStart(float interpolatedTime) {
        int targetTop = (mFrom + (int) ((mOriginalOffsetTop - mFrom) * interpolatedTime));
        int offset = targetTop - mLoadingView.getTop();
        setTargetOffsetTopAndBottom(offset);
    }

    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    //没有拖动到滑动位置的情况下,从拖动位置滑动回顶部的动画
    private void startScaleDownReturnToStartAnimation(int from,
                                                      Animation.AnimationListener listener) {
        mFrom = from;
        mStartingScale = ViewCompat.getScaleX(mLoadingView);
        Animation mScaleDownToStartAnimation = new Animation() {
            @Override

            public void applyTransformation(float interpolatedTime, Transformation t) {
                float targetScale = (mStartingScale + (-mStartingScale * interpolatedTime));
                setAnimationProgress(targetScale);
                moveToStart(interpolatedTime);
            }
        };
        //动画时间150ms
        mScaleDownToStartAnimation.setDuration(150);
        if (listener != null) {
            mLoadingView.setAnimationListener(listener);
        }
        mLoadingView.clearAnimation();
        mLoadingView.startAnimation(mScaleDownToStartAnimation);
    }

    private void setTargetOffsetTopAndBottom(int offset) {
        mLoadingView.bringToFront();
        mLoadingView.offsetTopAndBottom(offset);
        mCurrentTargetOffsetTop = mLoadingView.getTop();
    }

    //第二个手指按下
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    //以下是自定义部分

    /**
     * 设置刷新的方向
     * @param direction
     */
    public void setSwipeDirection(RefreshDirection direction) {
        if (direction == RefreshDirection.BOTH) {
            mBothDirection = true;
        } else {
            mBothDirection = false;
            mDirection = direction;
        }

        switch (direction) {
            case PULL_FROM_BOTTOM:
                mLoadingView = mBottomLoadingView;
                mCurrentTargetOffsetTop = mOriginalOffsetTop = getMeasuredHeight();
                break;
            case PULL_FROM_TOP:
            default:
                mLoadingView = mTopLoadingView;
                mCurrentTargetOffsetTop = mOriginalOffsetTop = -mLoadingView.getMeasuredHeight();
                break;
        }
    }

    /**
     * 设置当前(手指滑动时)的滑动模式,以此设置不同的mCurrentTargetOffsetTop值
     * @param direction
     */
    private void setCurrentSwipeDirection(RefreshDirection direction) {
        if (mDirection == direction) {
            return;
        }
        mDirection = direction;
        switch (mDirection) {
            case PULL_FROM_BOTTOM:
                mLoadingView = mBottomLoadingView;
                mCurrentTargetOffsetTop = mOriginalOffsetTop = getMeasuredHeight();
                break;
            case PULL_FROM_TOP:
            default:
                mLoadingView = mTopLoadingView;
                mCurrentTargetOffsetTop = mOriginalOffsetTop = -mLoadingView.getMeasuredHeight();
                break;
        }
    }

    /**
     * 获取当前的刷新方向
     * @return
     */
    public RefreshDirection getSwipeDirection() {
        return mBothDirection ? RefreshDirection.BOTH : mDirection;
    }

    /**
     * 如果targetView不是可滚动的View,遍历其childView找到第一个可滚动的View
     * 判断该View能否继续往下滚动
     * 否则判断targetView能否继续往下滚动
     */
    public boolean canChildScrollDown() {
        if (!isScrollableChildView(mTarget) && mTarget instanceof ViewGroup) {
            ViewGroup group = ((ViewGroup) mTarget);
            for (int i=0; i<group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (isScrollableChildView(child)) {
                    return ViewCompat.canScrollVertically(child, 1);
                }
            }
        }
        return ViewCompat.canScrollVertically(mTarget, 1);
    }

    /**
     * 寻找可滚动的View
     */
    public boolean isScrollableChildView(View view) {
        return view instanceof AbsListView || view instanceof ScrollView || view instanceof RecyclerView;
    }

    /**
     * 设置是否裁剪
     */
    public void setScale(boolean scale) {
        this.mScale = scale;
    }

    /**
     * 设置是否透明
     */
    public void setmAlpha(boolean alpha) {
        this.mAlpha = alpha;
    }

    /**
     * PULL_FROM_TOP or PULL_FROM_BOTTOM
     */
    public interface SuperRefreshListener {

        public void onRefreshFromTop();

    }

    /**
     * BOTH
     */
    public interface SuperRefreshListener2 {

        public void onRefreshFromTop();

        public void onRefreshFromBottom();

    }

    /**
     * 设置双向刷新的Listener
     * @param mSuperRefreshListener2
     */
    public void setSuperRefreshListener2(SuperRefreshListener2 mSuperRefreshListener2) {
        this.mSuperRefreshListener2 = mSuperRefreshListener2;
    }

    /**
     * 单一方向刷新时的listener
     */
    public void setSuperRefreshListener(SuperRefreshListener listener) {
        mSuperRefreshListener = listener;
    }

    /**
     * 设置顶部刷新的LoadingView
     * @param mTopLoadingView
     */
    public void setTopLoadingView(CommonLoadingView mTopLoadingView) {
        this.mTopLoadingView = mTopLoadingView;
    }

    /**
     * 设置底部刷新的LoadingView
     * @param mBottomLoadingView
     */
    public void setBottomLoadingView(CommonLoadingView mBottomLoadingView) {
        this.mBottomLoadingView = mBottomLoadingView;
    }
}
