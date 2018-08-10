package top.wzmyyj.zymk.view.panel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xw.repo.BubbleSeekBar;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import top.wzmyyj.wzm_sdk.adapter.ivd.IVD;
import top.wzmyyj.wzm_sdk.adapter.ivd.SingleIVD;
import top.wzmyyj.wzm_sdk.tools.L;
import top.wzmyyj.wzm_sdk.tools.T;
import top.wzmyyj.zymk.R;
import top.wzmyyj.zymk.app.bean.BookBean;
import top.wzmyyj.zymk.app.bean.ChapterBean;
import top.wzmyyj.zymk.app.bean.ComicBean;
import top.wzmyyj.zymk.app.tools.G;
import top.wzmyyj.zymk.presenter.ComicPresenter;
import top.wzmyyj.zymk.view.panel.base.BasePanel;
import top.wzmyyj.zymk.view.panel.base.BaseRecyclerPanel;


/**
 * Created by yyj on 2018/08/06. email: 2209011667@qq.com
 */

public class ComicRecyclerPanel extends BaseRecyclerPanel<ComicBean, ComicPresenter> {

    public ComicRecyclerPanel(Context context, ComicPresenter comicPresenter) {
        super(context, comicPresenter);
    }

    @Override
    protected void initPanels() {
        super.initPanels();
        addPanels(
                mMeunPanel = new ComicMeunPanel(context, mPresenter),
                new ComicLoadPasePanel(context, mPresenter)
        );
    }

    private ComicMeunPanel mMeunPanel;

    @Override
    protected void initView() {
        super.initView();
        mFrameLayout.addView(getPanelView(0));
        mFrameLayout.addView(getPanelView(1));

        // 消除mRecyclerView刷新的动画。
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private long mChapterId = 0;

    @Override
    protected void initData() {
        super.initData();
        mChapterId = mPresenter.getChapter_id();
    }

    @Override
    protected void setData() {

    }

    private final static int Definition_Low = 1;
    private final static int Definition_Middle = 2;
    private final static int Definition_High = 3;

    private int Definition = Definition_Middle;

    @Override
    protected void setIVD(List<IVD<ComicBean>> ivd) {
        ivd.add(new SingleIVD<ComicBean>() {
            @Override
            public int getItemViewLayoutId() {
                return R.layout.layout_comic_image;
            }

            @Override
            public void convert(ViewHolder holder, ComicBean comicBean, int position) {

                ImageView img_comic = holder.getView(R.id.img_comic);

                if (comicBean.getChapter_id() == -1) {
                    G.imgfix(context, R.mipmap.pic_comic_end, img_comic);
                    return;
                }


                if (comicBean.getPrice() == 0) {
                    switch (Definition) {
                        case Definition_Low:
                            G.imgfix(context, comicBean.getImg_low(), img_comic);
                            break;
                        case Definition_Middle:
                            G.imgfix(context, comicBean.getImg_middle(), img_comic);
                            break;
                        case Definition_High:
                            G.imgfix(context, comicBean.getImg_high(), img_comic);
                            break;
                    }
                } else {
                    G.imgfix(context, R.mipmap.pic_need_money, img_comic);
                }


            }
        });
    }


    @Override
    public void viewRecycled(@NonNull ViewHolder holder) {
        if (holder != null) {
            G.clear(context, (ImageView) holder.getView(R.id.img_comic));
        }
        super.viewRecycled(holder);
    }


    @Override
    protected void initListener() {
        super.initListener();
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            // 当前屏幕显示最上面一行的position。
            private int load_position_now = 0;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 10) {
                    mMeunPanel.close();
                } else if (dy < -10) {
                    mMeunPanel.show();
                }

                int p = mRecyclerView.getChildAdapterPosition(mRecyclerView.getChildAt(0));

                if (p == load_position_now) return;
                load_position_now = p;
                mMeunPanel.setMenu(mData.get(p));
                if (load_position_now < 3) {
                    mHandler.sendEmptyMessage(1);
                } else if (load_position_now > mData.size() - 5) {
                    mHandler.sendEmptyMessage(2);
                }

            }
        });
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int w = msg.what;
            if (w == 1) {
                addPrevious();
            } else if (w == 2) {
                addAfter();
            } else {
                mHandler.removeMessages(1);
                mHandler.removeMessages(2);
            }
        }
    };


    @Override
    public void update() {
        mPresenter.loadData();
    }

    private BookBean mBook;
    private List<ChapterBean> mChapterList = new ArrayList<>();
    private List<BookBean> mBookList = new ArrayList<>();
    private List<ComicBean> mComicList = new ArrayList<>();

    private long chapter_id_previous;
    private long chapter_id_after;


    @Override
    public Object f(int w, Object... objects) {
        L.d(w + "");

        getPanel(1).f(w, objects);
        if (w == -1) {
            return null;
        }
        BookBean book = (BookBean) objects[0];
        if (book != null) {
            mBook = book;
        }

        List<ChapterBean> chapterList = (List<ChapterBean>) objects[1];
        if (chapterList != null && chapterList.size() > 0) {
            mChapterList.clear();
            mChapterList.addAll(chapterList);
        }

        List<BookBean> bookList = (List<BookBean>) objects[2];
        if (bookList != null && bookList.size() > 0) {
            mBookList.clear();
            mBookList.addAll(bookList);
        }

        List<ComicBean> comicList = (List<ComicBean>) objects[3];
        if (comicList != null && comicList.size() > 0) {
            mComicList.clear();
            mComicList.addAll(comicList);
        }

        addOnce();
        mHandler.sendEmptyMessageDelayed(1, 500);
        return super.f(w, objects);
    }

    // 第一次添加数据。
    private void addOnce() {
        if (mChapterId == 0) {
            mChapterId = mChapterList.get(0).getChapter_id();
        }
        long chapter_id = mChapterId;
        mChapterId = 0;
        chapter_id_previous = 0;
        chapter_id_after = 0;

        List<ComicBean> comicList = new ArrayList<>();
        for (ComicBean comic : mComicList) {
            if (comic.getChapter_id() == chapter_id) {
                comicList.add(comic);
            }
        }
        mData.clear();
        mData.addAll(0, comicList);
        notifyDataSetChanged();
        mMeunPanel.setMenu(mData.get(0));

        // 找上一章和下一章的ID
        for (int i = 0; i < mChapterList.size(); i++) {
            if (mChapterList.get(i).getChapter_id() == chapter_id) {
                if (i > 0) {
                    chapter_id_previous = mChapterList.get(i - 1).getChapter_id();
                }
                if (i < mChapterList.size() - 1) {
                    chapter_id_after = mChapterList.get(i + 1).getChapter_id();
                }
                break;
            }
        }

    }

    // 向前加载一章。
    private void addPrevious() {
        if (chapter_id_previous == 0) return;
        long previous = chapter_id_previous;
        chapter_id_previous = 0;


        List<ComicBean> comicList = new ArrayList<>();
        for (ComicBean comic : mComicList) {
            if (comic.getChapter_id() == previous) {
                comicList.add(comic);
            }
        }
        mData.addAll(0, comicList);
        mHeaderAndFooterWrapper.notifyItemRangeInserted(0, comicList.size());

        // 找上一章ID
        for (int i = 0; i < mChapterList.size(); i++) {
            if (mChapterList.get(i).getChapter_id() == previous) {
                if (i > 0) {
                    chapter_id_previous = mChapterList.get(i - 1).getChapter_id();
                }
                break;
            }
        }
    }

    // 向后加载一章。
    private void addAfter() {
        if (chapter_id_after == 0) return;

        long after = chapter_id_after;
        chapter_id_after = 0;

        List<ComicBean> comicList = new ArrayList<>();
        for (ComicBean comic : mComicList) {
            if (comic.getChapter_id() == after) {
                comicList.add(comic);
            }
        }
        mData.addAll(comicList);
        notifyDataSetChanged();

        // 找下一章ID
        for (int i = 0; i < mChapterList.size(); i++) {
            if (mChapterList.get(i).getChapter_id() == after) {
                if (i < mChapterList.size() - 1) {
                    chapter_id_after = mChapterList.get(i + 1).getChapter_id();
                }
                break;
            }
        }

    }


    public void notifyItemShoewRangeChanged() {
        // 只刷新当前显示的item，防止图片跳闪。
        int a = mRecyclerView.getChildAdapterPosition(mRecyclerView.getChildAt(0));
        int b = mRecyclerView.getChildAdapterPosition(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1));
        if (a == -1 || b == -1) return;
        mHeaderAndFooterWrapper.notifyItemRangeChanged(a, b);
    }

    // mRecyclerView滑到指定position的位置。
    public void scrollTo(int p) {
        if (p < 0 || p > mData.size() - 1) return;// 防止越界。
        LinearLayoutManager mLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        mLayoutManager.scrollToPositionWithOffset(p, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.sendEmptyMessage(0);
    }


    // 内部类panel.菜单面板。
    public class ComicMeunPanel extends BasePanel<ComicPresenter> {

        public ComicMeunPanel(Context context, ComicPresenter p) {
            super(context, p);
        }

        @Override
        protected int getLayoutId() {
            return R.layout.layout_comic_menu;
        }

        @BindView(R.id.fl_top)
        FrameLayout fl_top;

        @OnClick(R.id.img_back)
        public void back() {
            mPresenter.finish();
        }

        @BindView(R.id.tv_chapter_name)
        TextView tv_chapter_name;

        @BindView(R.id.tv_chapter_var)
        TextView tv_chapter_var;

        @BindView(R.id.ll_bottom)
        LinearLayout ll_bottom;

        ///////////////////////////////////////////////////// menu 1，跳转到设置页面。
        @OnClick(R.id.ll_menu_1)
        public void menu_1() {

        }

        ///////////////////////////////////////////////////// menu 2，设置自动滑动和停止。
        @OnClick(R.id.ll_menu_2)
        public void menu_2() {

        }

        @BindView(R.id.img_auto)
        ImageView img_auto;

        ///////////////////////////////////////////////////// menu 3，设置画质。
        @OnClick(R.id.ll_menu_3)
        public void menu_3(View v) {
            showMenuDefinition(v);
        }

        @BindView(R.id.img_definition)
        ImageView img_definition;

        public void showMenuDefinition(View v) {

        }

        public void closeMenuDefinition() {

        }


        // 流畅画质
        public void setDefinitionLow() {
            Definition = Definition_Low;
            T.s("已切换到流畅画质");
            notifyItemShoewRangeChanged();
            img_definition.setImageResource(R.mipmap.ic_read_definition_low);
            closeMenuDefinition();
        }

        // 标清画质
        public void setDefinitionMiddle() {
            Definition = Definition_Middle;
            T.s("已切换到标清画质");
            notifyItemShoewRangeChanged();
            img_definition.setImageResource(R.mipmap.ic_read_definition_middle);
            closeMenuDefinition();
        }

        // 高清画质
        public void setDefinitionHigh() {
            Definition = Definition_High;
            T.s("已切换到高清画质");
            notifyItemShoewRangeChanged();
            img_definition.setImageResource(R.mipmap.ic_read_definition_high);
            closeMenuDefinition();
        }


        ///////////////////////////////////////////////////// menu 4，设置亮度
        @OnClick(R.id.ll_menu_4)
        public void menu_4() {

        }

        @BindView(R.id.v_brightness)
        View v_brightness;

        ///////////////////////////////////////////////////// menu 5，显示章节目录。
        @OnClick(R.id.ll_menu_5)
        public void menu_5() {

        }

        @BindView(R.id.bsb_1)
        BubbleSeekBar mBsb;

        @BindView(R.id.tv_chapter_var2)
        TextView tv_chapter_var2;


        @Override
        protected void initView() {
            super.initView();
            view.setVisibility(View.GONE);
            View menuDefinitionView = mInflater.inflate(R.layout.layout_menu_definition, null);
        }

        // 标记mBsb是非被触碰。
        private boolean isBsbOnTouch = false;

        @Override
        protected void initListener() {
            super.initListener();
            mBsb.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_MOVE:
                            isBsbOnTouch = true;
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            isBsbOnTouch = false;
                            break;

                    }
                    return false;
                }
            });
            mBsb.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
                @Override
                public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                    if (isBsbOnTouch) {// 被点击时，判断是否由于自身被触摸而引发的改变。
                        progressChanged(progress);
                    }
                }

                @Override
                public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

                }

                @Override
                public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

                }
            });
        }

        public void setMenu(ComicBean comic) {
            int max = comic.getVar_size();
            int p = comic.getVar();

            tv_chapter_name.setText(comic.getChapter_name());

            String var = p + "/" + max;
            tv_chapter_var.setText(var);
            tv_chapter_var2.setText(var);

            if (isBsbOnTouch) return;// bsb_1正在被点击时，不设置它。
            mBsb.getConfigBuilder()
                    .max(max)
                    .min(1)
                    .progress(p)
                    .build();
        }

        // mBsb主动控制mRecyclerView滑动。
        public void progressChanged(int p) {
            // 滑很快的话，holder可能等于null。返回NO_POSITION=-1。
            int a = mRecyclerView.getChildAdapterPosition(mRecyclerView.getChildAt(0));
            if (a == -1) return;// 防止越界。
            ComicBean comic = mData.get(a);
            int var = comic.getVar();
            int g = p - var;
            if (g == 0) return;// 防止无意义操作。
            int c = a + g;
            scrollTo(c);
        }


        private boolean isShow = false;

        public void show() {
            if (isShow) return;
            isShow = true;
            view.setVisibility(View.VISIBLE);

        }

        public void close() {
            if (!isShow) return;
            isShow = false;
            view.setVisibility(View.GONE);
        }
    }
}
