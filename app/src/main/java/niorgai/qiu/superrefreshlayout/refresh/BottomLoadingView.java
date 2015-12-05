package niorgai.qiu.superrefreshlayout.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import niorgai.qiu.superrefreshlayout.R;


/**
 * Created by qiu on 9/3/15.
 */
public class BottomLoadingView extends CommonLoadingView{

    private RotateAnimation rotateAnimation;

    private ImageView loadingImageView;

    private boolean isAnimating = false;

    public BottomLoadingView(Context context) {
        this(context, null);
    }

    public BottomLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadingImageView = new ImageView(context);
        loadingImageView.setImageResource(R.drawable.loading_rotate);
        addView(loadingImageView);

        rotateAnimation = new RotateAnimation(0, 1080, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setRepeatCount(-1);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(2000);
        rotateAnimation.setFillAfter(true);
    }

    public void setProgress(float pre) {
        loadingImageView.setRotation(pre * 1080);
        if (pre > 0.99) {
            if (!isAnimating) {
                startAnimation();
            }
        } else {
            stopAnimation();
        }
    }

    public void setViewAlpha(float alpha) {
        loadingImageView.setAlpha(alpha);
    }

    public void startAnimation(){
        loadingImageView.startAnimation(rotateAnimation);
        isAnimating = true;
    }

    public void stopAnimation(){
        rotateAnimation.cancel();
        isAnimating = false;
    }

}
