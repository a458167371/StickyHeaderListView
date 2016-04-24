package com.sunfusheng.StickyHeaderListView.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunfusheng.StickyHeaderListView.R;
import com.sunfusheng.StickyHeaderListView.adapter.TravelingAdapter;
import com.sunfusheng.StickyHeaderListView.model.ChannelEntity;
import com.sunfusheng.StickyHeaderListView.model.FilterData;
import com.sunfusheng.StickyHeaderListView.model.OperationEntity;
import com.sunfusheng.StickyHeaderListView.model.TravelingEntity;
import com.sunfusheng.StickyHeaderListView.util.ColorUtil;
import com.sunfusheng.StickyHeaderListView.util.DensityUtil;
import com.sunfusheng.StickyHeaderListView.util.ModelUtil;
import com.sunfusheng.StickyHeaderListView.view.FilterView;
import com.sunfusheng.StickyHeaderListView.view.HeaderAdViewView;
import com.sunfusheng.StickyHeaderListView.view.HeaderChannelViewView;
import com.sunfusheng.StickyHeaderListView.view.HeaderDividerViewView;
import com.sunfusheng.StickyHeaderListView.view.HeaderFilterViewView;
import com.sunfusheng.StickyHeaderListView.view.HeaderOperationViewView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.listView)
    ListView listView;
    @Bind(R.id.fv_top_filter)
    FilterView fvTopFilter;
    @Bind(R.id.rl_bar)
    RelativeLayout rlBar;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.view_title_bg)
    View viewTitleBg;
    @Bind(R.id.view_action_more_bg)
    View viewActionMoreBg;
    @Bind(R.id.fl_action_more)
    FrameLayout flActionMore;

    private Context mContext;
    private Activity mActivity;

    private List<String> adList = new ArrayList<>(); // 广告数据
    private List<ChannelEntity> channelList = new ArrayList<>(); // 频道数据
    private List<OperationEntity> operationList = new ArrayList<>(); // 运营数据
    private List<TravelingEntity> travelingList = new ArrayList<>(); // ListView数据

    private HeaderAdViewView listViewAdHeaderView; // 广告视图
    private HeaderChannelViewView headerChannelView; // 频道视图
    private HeaderOperationViewView headerOperationViewView; // 运营视图
    private HeaderDividerViewView headerDividerViewView; // 分割线占位图
    private HeaderFilterViewView headerFilterViewView; // 分类筛选视图
    private FilterData filterData; // 筛选数据

    private boolean isStickyTop = false; // 是否吸附在顶部
    private boolean isSmooth = false; // 没有吸附的前提下，是否在滑动
    private int titleViewHeight = 50; // 标题栏的高度
    private int filterPosition = -1;

    private int adViewHeight = 180; // 广告视图的高度
    private int adViewTopSpace; // 广告视图距离顶部的距离

    private int filterViewPosition = 4; // 筛选视图的位置
    private int filterViewTopSpace; // 筛选视图距离顶部的距离

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initData();
        initView();
        initListener();
    }

    private void initData() {
        mContext = this;
        mActivity = this;

        // 筛选数据
        filterData = new FilterData();
        filterData.setCategory(ModelUtil.getCategoryData());
        filterData.setSorts(ModelUtil.getSortData());
        filterData.setFilters(ModelUtil.getFilterData());

        // 广告数据
        adList = ModelUtil.getAdData();

        // 频道数据
        channelList = ModelUtil.getChannelData();

        // 运营数据
        operationList = ModelUtil.getOperationData();

        // ListView数据
        travelingList = ModelUtil.getTravelingData();
    }

    private void initView() {
        fvTopFilter.setVisibility(View.INVISIBLE);

        // 设置筛选数据
        fvTopFilter.setFilterData(mActivity, filterData);

        // 设置广告数据
        listViewAdHeaderView = new HeaderAdViewView(this);
        listViewAdHeaderView.fillView(adList, listView);

        // 设置频道数据
        headerChannelView = new HeaderChannelViewView(this);
        headerChannelView.fillView(channelList, listView);

        // 设置运营数据
        headerOperationViewView = new HeaderOperationViewView(this);
        headerOperationViewView.fillView(operationList, listView);

        // 设置分割线
        headerDividerViewView = new HeaderDividerViewView(this);
        headerDividerViewView.fillView("", listView);

        // 设置筛选数据
        headerFilterViewView = new HeaderFilterViewView(this);
        headerFilterViewView.fillView(new Object(), listView);

        // 设置ListView数据
        TravelingAdapter adapter = new TravelingAdapter(this, travelingList);
        listView.setAdapter(adapter);

        filterViewPosition = listView.getHeaderViewsCount() - 1;
    }

    private void initListener() {
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                View adView = listView.getChildAt(0 - firstVisibleItem);
                if (adView != null) {
                    adViewTopSpace = DensityUtil.px2dip(mContext, adView.getTop());
                    adViewHeight = DensityUtil.px2dip(mContext, adView.getHeight());
                }

                View filterView = listView.getChildAt(filterViewPosition - firstVisibleItem);
                if (filterView != null) {
                    filterViewTopSpace = DensityUtil.px2dip(mContext, filterView.getTop());
                }

                // 处理筛选是否吸附在顶部
                if (filterViewTopSpace > titleViewHeight) {
                    isStickyTop = false; // 没有吸附在顶部
                    fvTopFilter.setVisibility(View.INVISIBLE);
                } else {
                    isStickyTop = true; // 吸附在顶部
                    fvTopFilter.setVisibility(View.VISIBLE);
                }

                if (isSmooth && isStickyTop) {
                    isSmooth = false;
                    fvTopFilter.showFilterLayout(filterPosition);
                }

                fvTopFilter.setStickyTop(isStickyTop);

                // 处理标题栏颜色渐变
                handleTitleBarColorEvaluate();
            }
        });

        headerFilterViewView.setOnFilterClickListener(new HeaderFilterViewView.OnFilterClickListener() {
            @Override
            public void onFilterClick(int position) {
                if (!isStickyTop) {
                    filterPosition = position;
                    isSmooth = true;
                    listView.smoothScrollToPositionFromTop(filterViewPosition, DensityUtil.dip2px(mContext, titleViewHeight));
                }
            }
        });
        fvTopFilter.setOnFilterClickListener(new FilterView.OnFilterClickListener() {
            @Override
            public void onFilterClick(int position) {
                if (isStickyTop) {
                    filterPosition = position;
                    fvTopFilter.showFilterLayout(position);
                }
            }
        });

        flActionMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mActivity, AboutActivity.class));
            }
        });
    }

    // 处理标题栏颜色渐变
    private void handleTitleBarColorEvaluate() {
        float space = Math.abs(adViewTopSpace) * 1f;
        float fraction = space / (adViewHeight - titleViewHeight);
        if (fraction > 1.0f) {
            fraction = 1.0f;
        }
        viewTitleBg.setAlpha(1.0f - fraction);
        viewActionMoreBg.setAlpha(1.0f - fraction);
        rlBar.setBackgroundColor(ColorUtil.getNewColorByStartEndColor(mContext, fraction, R.color.transparent, R.color.orange));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listViewAdHeaderView != null) {
            listViewAdHeaderView.stopADRotate();
        }
    }

    @Override
    public void onBackPressed() {
        if (!fvTopFilter.isShowing()) {
            super.onBackPressed();
        } else {
            fvTopFilter.resetAllStatus();
        }
    }
}