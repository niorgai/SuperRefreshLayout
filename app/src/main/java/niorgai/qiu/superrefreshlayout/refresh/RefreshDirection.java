package niorgai.qiu.superrefreshlayout.refresh;

/**
 * 支持刷新的方向
 * Created by qiu on 9/4/15.
 */
public enum RefreshDirection {

    //从顶部下拉刷新
    PULL_FROM_START(0),

    //从底部上拉加载
    PULL_FROM_END(1),

    //两种方式均支持
    BOTH(2);

    private int mValue;

    RefreshDirection(int value) {
        this.mValue = value;
    }

    public static RefreshDirection getDirectionFromValue(int value) {
        for (RefreshDirection direction : RefreshDirection.values()) {
            if (direction.mValue == value) {
                return direction;
            }
        }
        return PULL_FROM_START;
    }
}
