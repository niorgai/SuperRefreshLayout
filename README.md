# SuperRefreshLayout
支持上拉、下拉的Layout, SDK >= 14.

由SwipeRefreshLayout的源码更改而来
主要支持以下特性:

1. 支持上拉加载
2. 自定义LoadingView
4. LoadingView可以根据拉动的百分比(拉伸\透明)变化.
5. 支持`AbsListView.setEmptyView()`方法
6. 支持设置子View跟随手指滑动(非侵入式)
7. 支持强制显示头部LoadingView.
8. Activity的onStart方法中调用setRefreshing(true)方法.
9. onTouchEvent在滑动过程中联动子View,详情可以查看[滑动冲突解决-联动子View](http://niorgai.github.io/2015/10/12/%E6%BB%91%E5%8A%A8%E5%86%B2%E7%AA%81%E8%A7%A3%E5%86%B3-%E8%81%94%E5%8A%A8%E5%AD%90View/).
10. 兼容ViewPager,解决方案可以查看[滑动冲突解决-更合理的拦截](http://niorgai.github.io/2015/10/15/%E6%BB%91%E5%8A%A8%E5%86%B2%E7%AA%81%E8%A7%A3%E5%86%B3-%E6%9B%B4%E5%90%88%E7%90%86%E7%9A%84%E6%8B%A6%E6%88%AA/).

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
	    
3. LoadingView可以根据拉动的百分比(拉伸\透明)变化.

		refreshLayout.setLoadingViewScale(boolean scale);
		refreshLayout.setLoadingViewAlpha(boolean alpha);
		
4. 支持`AbsListView.setEmptyView()`方法

	`SwipeRefreshLayout`的`onMeasure()`,`onLayout()`方法都只对第一个子View调用,所以它只能放置一个直接子View.这时候如果使用ListView的话,setEmptyView的方法无法实现,这里采用的解决办法为:
	
		<niorgai.qiu.superrefreshlayout.refresh.SuperRefreshLayout
			xmlns:android="http://schemas.android.com/apk/res/android"
		    android:layout_width="match_parent"
		    android:layout_height="match_parent"
		    android:orientation="vertical">
			//当然也可以是其他layout
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
	
5. `SwipeRefreshLayout`是侵入式的设计,即子View不会跟随LoadingView滑动,现在增加方法

		//设置target是否跟随顶部LoadingView滑动,默认为false
		public void setTargetScrollWithTop(boolean mTargetScrollWithTop)
		
		//设置target是否跟随底部LoadingView滑动,默认为false
		public void setTargetScrollWithBottom(boolean mTargetScrollWithBottom)
		
	可以实现非侵入式的加载效果.
	
6. 如果一个Activity只能从底部上拉加载更多,当进入该Activity时,第一次加载数据的操作其实是从顶部下拉刷新,一般处理方法为显示一个Loading遮罩层.但我希望可以在进入时显示顶部的LoadingView,这样可以更加直观.

		refreshLayout.setSwipeDirection(RefreshDirection.PULL_FROM_BOTTOM);
		//强制显示顶部LoadingView
		refreshLayout.setTopForceRefresh();
		...
		//同样在刷新结束后调用
		refreshLayout.setRefreshing(false);
		
