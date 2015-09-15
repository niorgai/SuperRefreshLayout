package niorgai.qiu.superrefreshlayout.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import niorgai.qiu.superrefreshlayout.R;


/**
 * Created by qiu on 9/3/15.
 */
public class TopLoadingView extends FrameLayout{

    private Animation.AnimationListener mListener;

    private RefreshProgress progress;

    public TopLoadingView(Context context) {
        this(context, null);
    }

    public TopLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.refresh_layout, this);
        progress = (RefreshProgress) findViewById(R.id.progress);
    }

    public void setAnimationListener(Animation.AnimationListener listener) {
        mListener = listener;
    }

    public void setProgress(float pre) {
        progress.rotateView(pre);
    }

    public void startAnimation(){
        progress.startAnimation();
    }

    public void stopAnimation(){
        progress.stopAnimation();
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
