package niorgai.qiu.superrefreshlayout.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.FrameLayout;

/**
 * Created by qiu on 9/18/15.
 */
public abstract class CommonLoadingView extends FrameLayout{
    protected Animation.AnimationListener mListener;

    public CommonLoadingView(Context context) {
        this(context, null);
    }

    public CommonLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAnimationListener(Animation.AnimationListener listener) {
        mListener = listener;
    }

    public void setProgress(float pre) {
    }

    public void startAnimation(){
    }

    public void stopAnimation(){
    }

    public void setViewAlpha(float alpha){
    }

    @Override
    public void onAnimationStart() {
        super.onAnimationStart();
        if (mListener != null) {
            mListener.onAnimationStart(getAnimation());
        }
    }

    @Override
    public void onAnimationEnd() {
        super.onAnimationEnd();
        if (mListener != null) {
            mListener.onAnimationEnd(getAnimation());
        }
    }
}
