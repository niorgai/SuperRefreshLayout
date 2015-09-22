package niorgai.qiu.superrefreshlayout.refresh;

import android.content.Context;
import android.util.AttributeSet;

import niorgai.qiu.superrefreshlayout.R;


/**
 * Created by qiu on 9/3/15.
 */
public class BottomLoadingView extends CommonLoadingView{

    private RefreshProgress progress;

    public BottomLoadingView(Context context) {
        this(context, null);
    }

    public BottomLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.refresh_layout, this);
        progress = (RefreshProgress) findViewById(R.id.progress);
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

    public void setViewAlpha(float alpha) {
        progress.setAlpha(alpha);
    }

}
