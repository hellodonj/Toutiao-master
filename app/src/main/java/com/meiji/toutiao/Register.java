package com.meiji.toutiao;

import android.support.annotation.NonNull;

import com.meiji.toutiao.bean.LoadingBean;
import com.meiji.toutiao.bean.LoadingEndBean;
import com.meiji.toutiao.bean.joke.JokeCommentBean;
import com.meiji.toutiao.bean.joke.JokeContentBean;
import com.meiji.toutiao.bean.media.MediaChannelBean;
import com.meiji.toutiao.bean.media.MediaProfileBean;
import com.meiji.toutiao.bean.media.MediaWendaBean;
import com.meiji.toutiao.bean.media.MultiMediaArticleBean;
import com.meiji.toutiao.bean.news.MultiNewsArticleDataBean;
import com.meiji.toutiao.bean.news.NewsCommentBean;
import com.meiji.toutiao.bean.photo.PhotoArticleBean;
import com.meiji.toutiao.bean.wenda.WendaArticleDataBean;
import com.meiji.toutiao.bean.wenda.WendaContentBean;
import com.meiji.toutiao.binder.LoadingEndViewBinder;
import com.meiji.toutiao.binder.LoadingViewBinder;
import com.meiji.toutiao.binder.joke.JokeCommentHeaderViewBinder;
import com.meiji.toutiao.binder.joke.JokeCommentViewBinder;
import com.meiji.toutiao.binder.joke.JokeContentViewBinder;
import com.meiji.toutiao.binder.media.MediaArticleHeaderViewBinder;
import com.meiji.toutiao.binder.media.MediaArticleImgViewBinder;
import com.meiji.toutiao.binder.media.MediaArticleTextViewBinder;
import com.meiji.toutiao.binder.media.MediaArticleVideoViewBinder;
import com.meiji.toutiao.binder.media.MediaChannelViewBinder;
import com.meiji.toutiao.binder.media.MediaWendaViewBinder;
import com.meiji.toutiao.binder.news.NewsArticleImgViewBinder;
import com.meiji.toutiao.binder.news.NewsArticleTextViewBinder;
import com.meiji.toutiao.binder.news.NewsArticleVideoViewBinder;
import com.meiji.toutiao.binder.news.NewsCommentViewBinder;
import com.meiji.toutiao.binder.photo.PhotoArticleViewBinder;
import com.meiji.toutiao.binder.search.SearchArticleVideoViewBinder;
import com.meiji.toutiao.binder.video.VideoContentHeaderViewBinder;
import com.meiji.toutiao.binder.wenda.WendaArticleOneImgViewBinder;
import com.meiji.toutiao.binder.wenda.WendaArticleTextViewBinder;
import com.meiji.toutiao.binder.wenda.WendaArticleThreeImgViewBinder;
import com.meiji.toutiao.binder.wenda.WendaContentHeaderViewBinder;
import com.meiji.toutiao.binder.wenda.WendaContentViewBinder;
import com.meiji.toutiao.interfaces.IOnItemLongClickListener;

import me.drakeet.multitype.ClassLinker;
import me.drakeet.multitype.ItemViewBinder;
import me.drakeet.multitype.MultiTypeAdapter;

/**
 * Created by Meiji on 2017/6/9.
 */

public class Register {

    public static void registerNewsArticleItem(@NonNull MultiTypeAdapter adapter) {
        // 一个类型对应多个 ItemViewBinder
        adapter.register(MultiNewsArticleDataBean.class)
                .to(new NewsArticleImgViewBinder(),
                        new NewsArticleVideoViewBinder(),
                        new NewsArticleTextViewBinder())
                .withClassLinker(new ClassLinker<MultiNewsArticleDataBean>() {
                    @NonNull
                    @Override
                    public Class<? extends ItemViewBinder<MultiNewsArticleDataBean, ?>> index(@NonNull MultiNewsArticleDataBean item) {
                        if (item.isHas_video()) {
                            return NewsArticleVideoViewBinder.class;
                        }
                        if (null != item.getImage_list() && item.getImage_list().size() > 0) {
                            return NewsArticleImgViewBinder.class;
                        }
                        return NewsArticleTextViewBinder.class;
                    }
                });
        adapter.register(LoadingBean.class, new LoadingViewBinder());
        adapter.register(LoadingEndBean.class, new LoadingEndViewBinder());
    }

    public static void registerNewsCommentItem(@NonNull MultiTypeAdapter adapter) {
        adapter.register(NewsCommentBean.DataBean.CommentBean.class, new NewsCommentViewBinder());
        adapter.register(LoadingBean.class, new LoadingViewBinder());
        adapter.register(LoadingEndBean.class, new LoadingEndViewBinder());
    }

    public static void registerVideoContentItem(@NonNull MultiTypeAdapter adapter) {
        adapter.register(MultiNewsArticleDataBean.class, new VideoContentHeaderViewBinder());
        adapter.register(NewsCommentBean.DataBean.CommentBean.class, new NewsCommentViewBinder());
        adapter.register(LoadingBean.class, new LoadingViewBinder());
        adapter.register(LoadingEndBean.class, new LoadingEndViewBinder());
    }

    public static void registerVideoArticleItem(@NonNull MultiTypeAdapter adapter) {
        adapter.register(MultiNewsArticleDataBean.class, new NewsArticleVideoViewBinder());
        adapter.register(LoadingBean.class, new LoadingViewBinder());
        adapter.register(LoadingEndBean.class, new LoadingEndViewBinder());
    }

    public static void registerJokeContentItem(@NonNull MultiTypeAdapter adapter) {
        adapter.register(JokeContentBean.DataBean.GroupBean.class, new JokeContentViewBinder());
        adapter.register(LoadingBean.class, new LoadingViewBinder());
        adapter.register(LoadingEndBean.class, new LoadingEndViewBinder());
    }

    public static void registerJokeCommentItem(@NonNull MultiTypeAdapter adapter) {
        adapter.register(JokeContentBean.DataBean.GroupBean.class, new JokeCommentHeaderViewBinder());
        adapter.register(JokeCommentBean.DataBean.RecentCommentsBean.class, new JokeCommentViewBinder());
        adapter.register(LoadingBean.class, new LoadingViewBinder());
        adapter.register(LoadingEndBean.class, new LoadingEndViewBinder());
    }

    public static void registerPhotoArticleItem(@NonNull MultiTypeAdapter adapter) {
        adapter.register(PhotoArticleBean.DataBean.class, new PhotoArticleViewBinder());
        adapter.register(LoadingBean.class, new LoadingViewBinder());
        adapter.register(LoadingEndBean.class, new LoadingEndViewBinder());
    }

    public static void registerWendaArticleItem(@NonNull MultiTypeAdapter adapter) {
        // 一个类型对应多个 ItemViewBinder
        adapter.register(WendaArticleDataBean.class)
                .to(new WendaArticleTextViewBinder(),
                        new WendaArticleOneImgViewBinder(),
                        new WendaArticleThreeImgViewBinder())
                .withClassLinker(new ClassLinker<WendaArticleDataBean>() {
                    @NonNull
                    @Override
                    public Class<? extends ItemViewBinder<WendaArticleDataBean, ?>> index(@NonNull WendaArticleDataBean item) {
                        if (null != item.getExtraBean().getWenda_image() &&
                                null != item.getExtraBean().getWenda_image().getThree_image_list() &&
                                item.getExtraBean().getWenda_image().getThree_image_list().size() > 0) {
                            return WendaArticleThreeImgViewBinder.class;
                        }
                        if (null != item.getExtraBean().getWenda_image() &&
                                null != item.getExtraBean().getWenda_image().getLarge_image_list() &&
                                item.getExtraBean().getWenda_image().getLarge_image_list().size() > 0) {
                            return WendaArticleOneImgViewBinder.class;
                        }
                        return WendaArticleTextViewBinder.class;
                    }
                });
        adapter.register(LoadingBean.class, new LoadingViewBinder());
        adapter.register(LoadingEndBean.class, new LoadingEndViewBinder());
    }

    public static void registerWendaContentItem(@NonNull MultiTypeAdapter adapter) {
        adapter.register(WendaContentBean.QuestionBean.class, new WendaContentHeaderViewBinder());
        adapter.register(WendaContentBean.AnsListBean.class, new WendaContentViewBinder());
        adapter.register(LoadingBean.class, new LoadingViewBinder());
        adapter.register(LoadingEndBean.class, new LoadingEndViewBinder());
    }

    public static void registerMediaChannelItem(@NonNull MultiTypeAdapter adapter, @NonNull IOnItemLongClickListener listener) {
        adapter.register(MediaChannelBean.class, new MediaChannelViewBinder(listener));
    }

    public static void registerSearchItem(@NonNull MultiTypeAdapter adapter) {
        adapter.register(MultiNewsArticleDataBean.class)
                .to(new NewsArticleImgViewBinder(),
                        new SearchArticleVideoViewBinder(),
                        new NewsArticleTextViewBinder())
                .withClassLinker(new ClassLinker<MultiNewsArticleDataBean>() {
                    @NonNull
                    @Override
                    public Class<? extends ItemViewBinder<MultiNewsArticleDataBean, ?>> index(@NonNull MultiNewsArticleDataBean item) {
                        if (item.isHas_video()) {
                            return SearchArticleVideoViewBinder.class;
                        }
                        if (null != item.getImage_list() && item.getImage_list().size() > 0) {
                            return NewsArticleImgViewBinder.class;
                        }
                        return NewsArticleTextViewBinder.class;
                    }
                });
        adapter.register(LoadingBean.class, new LoadingViewBinder());
        adapter.register(LoadingEndBean.class, new LoadingEndViewBinder());
    }

    public static void registerMediaArticleItem(@NonNull MultiTypeAdapter adapter) {
        adapter.register(MultiMediaArticleBean.DataBean.class)
                .to(new MediaArticleImgViewBinder(),
                        new MediaArticleVideoViewBinder(),
                        new MediaArticleTextViewBinder())
                .withClassLinker(new ClassLinker<MultiMediaArticleBean.DataBean>() {
                    @NonNull
                    @Override
                    public Class<? extends ItemViewBinder<MultiMediaArticleBean.DataBean, ?>> index(@NonNull MultiMediaArticleBean.DataBean item) {
                        if (item.isHas_video()) {
                            return MediaArticleVideoViewBinder.class;
                        }
                        if (null != item.getImage_list() && item.getImage_list().size() > 0) {
                            return MediaArticleImgViewBinder.class;
                        }
                        return MediaArticleTextViewBinder.class;
                    }
                });
        adapter.register(MediaProfileBean.DataBean.class, new MediaArticleHeaderViewBinder());
        adapter.register(LoadingBean.class, new LoadingViewBinder());
        adapter.register(LoadingEndBean.class, new LoadingEndViewBinder());
    }

    public static void registerMediaWendaItem(@NonNull MultiTypeAdapter adapter) {
        adapter.register(MediaWendaBean.AnswerQuestionBean.class, new MediaWendaViewBinder());
        adapter.register(LoadingBean.class, new LoadingViewBinder());
        adapter.register(LoadingEndBean.class, new LoadingEndViewBinder());
    }
}
