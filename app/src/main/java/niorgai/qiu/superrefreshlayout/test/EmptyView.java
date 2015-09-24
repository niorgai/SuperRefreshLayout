package niorgai.qiu.superrefreshlayout.test;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import niorgai.qiu.superrefreshlayout.R;

/**
 * Created by qiu on 9/24/15.
 */
public class EmptyView extends RelativeLayout {

    public EmptyView(Context context) {
        this(context, null);
    }

    public EmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.empty_view, this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 使EmptyView可以支持滚动
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return true;
    }
}
