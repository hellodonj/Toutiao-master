package com.meiji.toutiao.module.photo.content;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.meiji.toutiao.ErrorAction;
import com.meiji.toutiao.InitApp;
import com.meiji.toutiao.RetrofitFactory;
import com.meiji.toutiao.api.INewsApi;
import com.meiji.toutiao.api.IPhotoApi;
import com.meiji.toutiao.bean.news.NewsContentBean;
import com.meiji.toutiao.bean.photo.PhotoGalleryBean;
import com.meiji.toutiao.module.media.home.MediaHomeActivity;
import com.meiji.toutiao.util.SettingUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static android.R.attr.path;

/**
 * Created by Meiji on 2017/2/16.
 */

class PhotoContentPresenter implements IPhotoContent.Presenter {

    private static final String TAG = "PhotoContentPresenter";
    private IPhotoContent.View view;
    private PhotoGalleryBean bean;
    private int position;
    private String shareUrl;

    PhotoContentPresenter(IPhotoContent.View view) {
        this.view = view;
    }

    @Override
    public void doRefresh() {

    }

    @Override
    public void doShowNetError() {
        view.onHideLoading();
        view.onShowNetError();
    }

    @Override
    public void doLoadData(String... category) {

        try {
            this.shareUrl = category[0];
        } catch (Exception e) {
            ErrorAction.print(e);
        }

        Observable
                .create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                        try {
                            Response<ResponseBody> response = RetrofitFactory.getRetrofit().create(IPhotoApi.class)
                                    .getPhotoContentHTML(shareUrl).execute();
                            if (response.isSuccessful()) {
                                e.onNext(response.body().string());
                            } else {
                                e.onError(new Throwable());
                            }
                        } catch (Exception e1) {
                            e.onComplete();
                            ErrorAction.print(e1);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .map(new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull String s) throws Exception {
                        return parseHTML(s);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(view.<Boolean>bindToLife())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Boolean b) {
                        if (b) {
                            view.onHideLoading();
                            view.onSetImageBrowser(bean, 0);
                        } else {
                            // 解析 HTML 失败, 可以用 WebView 加载内容
                            doLoadWebView();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        // 解析 HTML 失败, 可以用 WebView 加载内容
                        doLoadWebView();
                        ErrorAction.print(e);
                    }

                    @Override
                    public void onComplete() {
                        doShowNetError();
                    }
                });
    }

    private void doLoadWebView() {
        Observable
                .create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                        try {
                            Response<ResponseBody> response = RetrofitFactory.getRetrofit().create(INewsApi.class)
                                    .getNewsContentRedirectUrl(shareUrl).execute();
                            // 获取重定向后的 URL 用于拼凑API
                            if (response.isSuccessful()) {
                                String httpUrl = response.raw().request().url().toString();
                                if (!TextUtils.isEmpty(httpUrl) && httpUrl.contains("toutiao")) {
                                    String api = httpUrl + "info/";
                                    e.onNext(api);
                                } else {
                                    e.onError(new Throwable());
                                }
                            } else {
                                e.onError(new Throwable());
                            }
                        } catch (Exception e1) {
                            e.onComplete();
                            ErrorAction.print(e1);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<NewsContentBean>>() {
                    @Override
                    public ObservableSource<NewsContentBean> apply(@NonNull String s) throws Exception {
                        return RetrofitFactory.getRetrofit().create(INewsApi.class).getNewsContent(s);
                    }
                })
                .map(new Function<NewsContentBean, String>() {
                    @Override
                    public String apply(@NonNull NewsContentBean bean) throws Exception {
                        return getHTML(bean);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(view.<String>bindToLife())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        view.onHideLoading();
                        view.onSetWebView(s, true);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        view.onHideLoading();
                        view.onSetWebView(null, false);
                        ErrorAction.print(e);
                    }

                    @Override
                    public void onComplete() {
                        doShowNetError();
                    }
                });
    }

    @Override
    public int doGetImageCount() {
        return bean.getCount();
    }

    @Override
    public void doSetPosition(int position) {
        this.position = position;
    }

    @Override
    public void doSaveImage() {

        Observable
                .create(new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<Boolean> e) throws Exception {
                        List<PhotoGalleryBean.SubImagesBean> sub_images = bean.getSub_images();
                        final String url = sub_images.get(position).getUrl();
                        Log.d(TAG, "doSaveImage: " + url);
                        e.onNext(saveImage(url));
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(view.<Boolean>bindToLife())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            view.onShowSaveSuccess();
                        } else {
                            view.onShowNetError();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        view.onShowNetError();
                        ErrorAction.print(throwable);
                    }
                });
    }

    private Boolean parseHTML(String HTML) {
        boolean flag = false;
        Document doc = Jsoup.parse(HTML);
        // 取得所有的script tag
        Elements scripts = doc.getElementsByTag("script");
        for (Element e : scripts) {
            // 过滤字符串
            String script = e.toString();
            if (script.contains("var gallery = {")) {
                // 只取得script的內容
                script = e.childNode(0).toString();
                // 取得JS变量数组
                String[] vars = script.split("var ");
                // 取得单个JS变量
                for (String var : vars) {
                    // 取到满足条件的JS变量
                    if (var.contains("gallery = ")) {
                        int start = var.indexOf("=");
                        int end = var.lastIndexOf(";");
                        String json = var.substring(start + 1, end + 1);
                        // 处理特殊符号
                        JsonReader reader = new JsonReader(new StringReader(json));
                        reader.setLenient(true);
                        Log.d(TAG, "parseHTML: " + reader);
                        bean = new Gson().fromJson(reader, PhotoGalleryBean.class);
                        flag = true;
                    }
                }
            }
        }
        return flag;
    }

    private Boolean saveImage(String url) {
        boolean flag = false;
        try {
            // 获取 bitmap
            Bitmap bitmap = Glide.with(InitApp.AppContext).load(url).asBitmap()
                    .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
            // http://stormzhang.com/android/2014/07/24/android-save-image-to-gallery/
            if (bitmap != null) {
                // 首先保存图片
                File appDir = new File(Environment.getExternalStorageDirectory(), "Toutiao");
                if (!appDir.exists()) {
                    appDir.mkdir();
                }
                String fileName = System.currentTimeMillis() + ".jpg";
                File file = new File(appDir, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();

                // 其次把文件插入到系统图库
//                MediaStore.Images.Media.insertImage(InitApp.AppContext.getContentResolver(), file.getAbsolutePath(), fileName, null);
                // 最后通知图库更新
                InitApp.AppContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));

                flag = true;
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            ErrorAction.print(e);
        }
        return flag;
    }

    private String getHTML(NewsContentBean bean) {
        String title = bean.getData().getTitle();
        String content = bean.getData().getContent();
        if (content != null) {

            String css = "<link rel=\"stylesheet\" href=\"file:///android_asset/toutiao_light.css\" type=\"text/css\">";
            if (SettingUtil.getInstance().getIsNightMode()) {
                css = css.replace("toutiao_light", "toutiao_dark");
            }

            String html = "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">" +
                    css +
                    "<body>\n" +
                    "<article class=\"article-container\">\n" +
                    "    <div class=\"article__content article-content\">" +
                    "<h1 class=\"article-title\">" +
                    title +
                    "</h1>" +
                    content +
                    "    </div>\n" +
                    "</article>\n" +
                    "</body>\n" +
                    "</html>";

            return html;
        } else {
            return null;
        }
    }

    @Override
    public void doGoMediaHome(final String media_url) {
        Observable
                .create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                        try {
                            Response<ResponseBody> response = RetrofitFactory.getRetrofit().create(INewsApi.class)
                                    .getNewsContentRedirectUrl(shareUrl).execute();
                            // 获取重定向后的 URL 用于拼凑API
                            if (response.isSuccessful()) {
                                String httpUrl = response.raw().request().url().toString();
                                if (!TextUtils.isEmpty(httpUrl) && httpUrl.contains("toutiao")) {
                                    String api = httpUrl + "info/";
                                    e.onNext(api);
                                } else {
                                    e.onComplete();
                                }
                            } else {
                                e.onComplete();
                            }
                        } catch (Exception e1) {
                            e.onComplete();
                            ErrorAction.print(e1);
                        }
                    }
                })
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .switchMap(new Function<String, ObservableSource<NewsContentBean>>() {
                    @Override
                    public ObservableSource<NewsContentBean> apply(@NonNull String s) throws Exception {
                        return RetrofitFactory.getRetrofit().create(INewsApi.class).getNewsContent(s);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(view.<NewsContentBean>bindToLife())
                .subscribe(new Consumer<NewsContentBean>() {
                    @Override
                    public void accept(@NonNull NewsContentBean bean) throws Exception {
                        String id = bean.getData().getMedia_user().getId() + "";
                        MediaHomeActivity.launch(id);
                    }
                }, ErrorAction.error());
    }
}
