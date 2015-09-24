package niorgai.qiu.superrefreshlayout.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ProgressBar;

import niorgai.qiu.superrefreshlayout.R;


/**
 * Created by qiu on 9/3/15.
 */
public class BottomLoadingView extends CommonLoadingView{

    private ProgressBar progress;
    private ImageView imageView;

    public BottomLoadingView(Context context) {
        this(context, null);
    }

    public BottomLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.bottom_refresh_layout, this);
        progress = (ProgressBar) findViewById(R.id.progress);
        imageView = (ImageView) findViewById(R.id.img);
        progress.setVisibility(GONE);
    }

    public void setProgress(float pre) {
    }

    public void startAnimation(){
        imageView.setVisibility(GONE);
        progress.setVisibility(VISIBLE);
    }

    public void stopAnimation(){
        imageView.setVisibility(VISIBLE);
        progress.setVisibility(GONE);
    }

    public void setViewAlpha(float alpha) {
        progress.setAlpha(alpha);
    }

}
