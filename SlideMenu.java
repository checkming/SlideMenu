package com.itheima.slidemenu99;

import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Created by dance on 2017/4/1.
 * 如果你要写的ViewGroup，对测量和布局没有特殊的需求，那么一般就选择继承自系统已有的布局
 * 比如FrameLayout，因为它已经实现了测量和布局
 */

public class SlideMenu extends FrameLayout {
    private static final String TAG = "SlideMenu";
    ViewDragHelper dragHelper;
    private View menu;
    private View main;
    private boolean result;
    private int maxLeft;//最大的左边
    FloatEvaluator floatEval = new FloatEvaluator();
    ArgbEvaluator argbEval = new ArgbEvaluator();
    public SlideMenu(Context context) {
        this(context, null);
    }

    public SlideMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        dragHelper = ViewDragHelper.create(this,callback);



    }

    /**
     * 当读取完布局文件后执行，此时此刻就知道有几个字View了,
     * 注意：改方法执行的时候，并木有进行测量呢，因此不要去尝试获取宽高
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        menu = getChildAt(0);
        main = getChildAt(1);

        //做一些代码strong的判断
        if(getChildCount()!=2){
            throw new IllegalArgumentException("The SlideMenu can only have 2 children! You stupid!");
        }
    }

    /**
     * 该方法是在onMeasure执行完毕之后调用
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        maxLeft = (int) (getMeasuredWidth()*0.6f);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //让ViewDragHelper来帮助我们判断是否应该拦截
        result = dragHelper.shouldInterceptTouchEvent(ev);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        dragHelper.processTouchEvent(event);

        return result;
    }
    Scroller scroller;
    ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        /**
         * 是否要捕获监视view的触摸事件
         * @param child     当前你触摸的子View
         * @param pointerId     触摸点的索引
         * @return  true：监视     false：忽略对的触摸的监视
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child==main || child==menu;
        }

        /**
         * 当View被监视触摸事件就执行
         * @param capturedChild
         * @param activePointerId
         */
        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }

        /**
         * 看起来是获取View水平拖拽范围，然后并木有用。它目前是用来判断是否想强制横向滑动的条件，
         * 如果想强制横向滑动，则返回大于0的任意值，默认返回是0
         * @param child
         * @return
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return 1;
        }


        /**
         * 修正修改VIew水平的位置，用来控制View水平移动的
         * @param child     当前触摸的字View
         * @param left      ViewDragHelper帮我们计算的当前View最新的left。内部的计算是这样的：left=child.getLeft()+dx
         * @param dx        本次水平移动的距离
         * @return   返回值是用来控制View的最终移动 ，返回的值表示child最终的left
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if(child==main){
                //如果当前是main，才进行限制
                left = fixLeft(left);
            }

            return left;
        }

        /**
         * 控制垂直方向的移动
         * @param child
         * @param top   帮我们计算好的最新的top，top=child.getTop()+dy
         * @param dy    本次移动的垂直距离
         * @return   返回的值表示最终的top
         */
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return 0;
        }

        /**
         * 当View位置改变就调用，一般用来实现一些同时移动的效果
         * @param changedView   位置了的View
         * @param left  改变后的left
         * @param top   改变后的top
         * @param dx    水平移动距离
         * @param dy    垂直移动距离
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            //如果当前移动是main，那么则让menu进行伴随移动
            if(changedView==menu){
                //把menu固定死位置
                menu.layout(0,menu.getTop(),menu.getMeasuredWidth(), menu.getBottom());

                //移动main, 并限制main的移动
                int newLeft = main.getLeft() + dx;
                newLeft = fixLeft(newLeft);
                main.layout(newLeft,main.getTop(),newLeft+main.getMeasuredWidth(),main.getBottom());
            }

            //编写动画
            //1.计算View滑动的百分比进度
            float percent = main.getLeft()*1f/maxLeft;
            //2.执行动画
            execAnim(percent);

            if(listener!=null){
                listener.onDragging(percent);
                if(percent==1f){
                    //打开
                    listener.onOpen();
                }else if (percent==0f){
                    //关闭
                    listener.onClose();
                }
            }

        }

        /**
         * 松开手指释放触摸会执行的方法
         * @param releasedChild  释放的那个View
         * @param xvel  抬起手指的时候x方向的速度  px/s
         * @param yvel  抬起手指的时候y方向的速度
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if(main.getLeft()>maxLeft/2){
                //向右
//                scroller.startScroll();
                open();

            }else {
                //向左
                close();
            }
        }
    };

    private void open() {
        dragHelper.smoothSlideViewTo(main, maxLeft, 0);
        ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
    }

    private void close() {
        dragHelper.smoothSlideViewTo(main, 0, 0);
        ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
    }

    /**
     * 根据百分比执行动画
     * @param percent
     */
    private void execAnim(float percent) {
        //percent: 0 -> 1
        //算法:起始值 + (结束值-起始值)*percent
        //1.main缩放动画
        float scaleVal = 1 + (0.8f-1f)*percent;
        main.setScaleX(floatEval.evaluate(percent, 1f, 0.8f));//1->0.8f
        main.setScaleY(floatEval.evaluate(percent, 1f, 0.8f));

        //2.menu的缩放动画
        menu.setScaleX(floatEval.evaluate(percent, 0.3f, 1f));
        menu.setScaleY(floatEval.evaluate(percent, 0.3f, 1f));
        menu.setTranslationX(floatEval.evaluate(percent, -menu.getMeasuredWidth()/2, 0));

        //3.给整个SlideMenu的背景添加阴影遮罩效果
        if(getBackground()!=null){
//            int color = (int) argbEval.evaluate(percent,Color.RED, Color.GREEN);
            int color = (int) argbEval.evaluate(percent,Color.BLACK, Color.TRANSPARENT);
            getBackground().setColorFilter(color, PorterDuff.Mode.SRC_OVER);
        }

    }


    @Override
    public void computeScroll() {
        super.computeScroll();
//        if(scroller.computeScrollOffset()){
//            scrollTo(scroller.getCurrX(),scroller.getCurrY());
//            ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
//        }
        //ViewDragHelpoer的写法
        if(dragHelper.continueSettling(true)){
            ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
        }


    }

    /**
     * 对left的大小值进行限制
     * @param newLeft
     * @return
     */
    private int fixLeft(int newLeft) {
        if(newLeft>maxLeft){
            newLeft = maxLeft;
        }else if(newLeft<0){
            newLeft = 0;
        }
        return newLeft;
    }

    private OnSlideListener listener;
    public void setOnSlideListener(OnSlideListener listener){
        this.listener = listener;
    }

    public void toogle() {
        if(main.getLeft()==maxLeft){
            close();
        }else {
            //打开
            open();
        }
    }

    public interface OnSlideListener{
        void onDragging(float percent);
        void onOpen();
        void onClose();
    }
}
