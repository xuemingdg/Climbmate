package com.hokming.climbmate;

import android.app.Activity;

import cn.nekocode.rxlifecycle.LifecyclePublisher;
import cn.nekocode.rxlifecycle.RxLifecycle;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * RxJava utils(switch threads)
 */

@SuppressWarnings("unused")
public class RxUtil {

    private static String TAG = "RxUtil";

    public static <T> ObservableTransformer<T, T> schedulers(final Activity activity) {
        return upstream -> upstream.compose(RxUtil.<T>lifeCycle(activity))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static <T> ObservableTransformer<T, T> lifeCycle(LifecyclePublisher publisher) {
        return RxLifecycle.bind(publisher).withObservable();
    }

    public static <T> ObservableTransformer<T, T> lifeCycle(Activity activity) {
        return RxLifecycle.bind(activity).withObservable();
    }

    public static void exeTask() {
    }


}
