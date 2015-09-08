package niorgai.qiu.superrefreshlayout.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

/**
 * Created by qiu on 9/6/15.
 */
public class RefreshProgress extends ImageView {
    //匀速加速器
    private LinearInterpolator lir = new LinearInterpolator();

    private RotateAnimation rotateAnimation;

    public RefreshProgress(Context context) {
        this(context, null);
    }

    public RefreshProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        rotateAnimation = new RotateAnimation(0, 1080, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setRepeatCount(-1);
        rotateAnimation.setInterpolator(lir);
        rotateAnimation.setDuration(3000);
        rotateAnimation.setFillAfter(true);
    }

    public void rotateView(float num){
        setRotation(num * 360);
    }

    public void startAnimation(){
        this.startAnimation(rotateAnimation);
    }

    public void stopAnimation() {
        rotateAnimation.cancel();
    }

}