# SuperRefreshLayout
支持上拉、下拉的Layout, SDK >= 14.

由SwipeRefreshLayout的源码更改而来
主要支持以下特性:

1. 支持上拉加载
2. 自定义LoadingView
4. LoadingView可以透明变化
5. 支持`AbsListView.setEmptyView()`方法
6. Activity的onStart方法中调用setRefreshing(true)方法.

# Example
1. 设置不同的加载模式:

		SuperRefreshLayout refreshLayout = (SuperRefreshLayout) findViewById(R.id.refresh_layout);
		
		//Default: Pull_From_Top 从顶部下拉
		refreshLayout.setSwipeDirection(RefreshDirection.PULL_FROM_TOP);
		//Pull_From_Bottom 从底部上拉
		refreshLayout.setSwipeDirection(RefreshDirection.PULL_FROM_BOTTOM);
		refreshLayout.setSuperRefreshListener(new SuperRefreshLayout.SuperRefreshListener() {
            @Override
            public void refreshFromTop() {
                
            }
        });
		
		//BOTH,两种模式
		refreshLayout.setSwipeDirection(RefreshDirection.BOTH);
		refreshLayout.setSuperRefreshListener2(new SuperRefreshLayout.SuperRefreshListener2() {
            @Override
            public void refreshFromTop() {
                
            }

            @Override
            public void refreshFromBottom() {

            }
        });
        
2. 自定义LoadingView:

	继承至CommonLoadingView即可,CommonLoadingView实质是一个FrameLayout.可以根据自己的需要重写以下方法:
		
		//根据不同的下拉Progress设置其变化
	    public void setProgress(float pre) {
	    }
	
	    //开始Loading动作时的动画
	    public void startAnimation(){
	    }
	
	    //Loading结束时取消动画
	    public void stopAnimation(){
	    }
	
	    //根据不同的下拉Progress设置alpha值
	    public void setViewAlpha(float alpha){
	    }
	    
3. LoadingView可以透明变化,默认为true

		refreshLayout.setmAlpha(boolean alpha);
		
4. 支持`AbsListView.setEmptyView()`方法

	`SwipeRefreshLayout`中使用ListView的话,setEmptyView方法很难同步,这里采用的解决办法为:
	
		<niorgai.qiu.superrefreshlayout.refresh.SuperRefreshLayout
			xmlns:android="http://schemas.android.com/apk/res/android"
		    android:layout_width="match_parent"
		    android:layout_height="match_parent"
		    android:orientation="vertical">
		
		    <RelativeLayout
		        android:layout_width="match_parent"
		        android:layout_height="match_parent">
		        <ListView
		            android:id="@+id/list_view"
		            android:layout_width="fill_parent"
		            android:layout_height="fill_parent"/>
		        <niorgai.qiu.superrefreshlayout.test.EmptyView
		            android:id="@+id/empty_view"
		            android:layout_width="match_parent"
		            android:layout_height="match_parent""/>
		    </RelativeLayout>
		
		</niorgai.qiu.superrefreshlayout.refresh.SuperRefreshLayout>
		
	项目中我重写了EmptyView的`onTouchEvent`方法,一直返回true.如果不想重写也可以在EmptyView外面包一层ScrollView.同时设置ScrollView的属性`android:fillViewport="true"`即可.