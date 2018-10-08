package com.yjz.rxjava;

import android.util.Log;

import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;


/**
 * @author YJZ
 * date 2018/10/8
 * description RxJava初步学习
 */
public class RxJavaTestOne {

    private static final String TAG = "TAG";

    /**
     * RxJava正常普通使用
     */
    public static void testNormal()
    {
        // 1.创建被观察者
        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber)
            {
                // 1）被观察者通过 onNext() 方法发送消息给观察者
                subscriber.onNext("发送消息1");
                subscriber.onNext("发送消息2");
                // 2） 当被观察者发生异常或者调用onCompleted()方法之后,再次调用onNext()方法不会传递到观察者中.
                // 被观察者调用的对应方法,在观察者对应的方法中做接受
                subscriber.onError(new NullPointerException("NullPointerException"));
                subscriber.onCompleted();
                subscriber.onNext("发送消息3");
            }
        });

        // 2.创建观察者
        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onCompleted()
            {
                Log.e(TAG, "onCompleted:");
            }

            @Override
            public void onError(Throwable e)
            {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onNext(String s)
            {
                Log.e(TAG, "onNext: " + s);
            }
        };

        // 3. 实现被观察者和观察者的订阅
        observable.subscribe(subscriber);
    }

    /**
     * 特殊的被观察者Single
     */
    public static void testSingle()
    {
        // 在特殊的被观察者Single中,不能像被观察者一样发送onNext()、onError()、onCompleted()方法。
        Single<String> single = Single.create(new Single.OnSubscribe<String>() {
            @Override
            public void call(SingleSubscriber<? super String> singleSubscriber)
            {
                // 只能调用 成功onSuccess()和失败onError()方法,并且还提供了检测观察者和被观察者是否订阅或者取消两者之间的订阅

                // 当程序出现异常或者调用onError()方法时,在观察者中只会调用onError()方法,一旦调用则不会向下执行了
                singleSubscriber.onError(new NullPointerException("NullPointerException"));
                // 调用onSuccess()方法时,在观察者中会调用onNext()和onCompleted()方法,一旦调用则不会向下执行了
                singleSubscriber.onSuccess("onSuccess");
            }
        });

        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onCompleted()
            {
                Log.e(TAG, "onCompleted: ");
            }

            @Override
            public void onError(Throwable e)
            {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onNext(String s)
            {
                Log.e(TAG, "onNext: " + s);
            }
        };

        single.subscribe(subscriber);
    }

    /**
     * 观察者的变体Action
     */
    public static void testAction()
    {
        /*
            在之前,一次完整的订阅通过观察实现onNext()、onError()、onComplete()方法完成。
            但是在有时候我们并不关心onError或者onComplete()方法，此时，我们就可以用Action类来完成。
         */
        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber)
            {
                subscriber.onNext("发送消息1");
                subscriber.onNext("发送消息2");
                subscriber.onCompleted();
            }
        });

        // 通过 Action1类实例来代替传统的Subscriber
        Action1<String> onNextAction = new Action1<String>() {
            @Override
            public void call(String s)
            {
                Log.e(TAG, "call: " + s);
            }
        };

        Action1<Throwable> onErrorAction = new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable)
            {
                Log.e(TAG, "onErrorAction call: " + throwable.getMessage());
            }
        };
        // Action 后的数字代表的时改对象有多少个参数，0，代表没有，1代表一个，....一直到ActionN
        // 与Action相对应的有Func
        Action0 onCompleteAction = new Action0() {
            @Override
            public void call()
            {
                Log.e(TAG, "onCompleteAction call: ");
            }
        };
        observable.subscribe(onNextAction);
        // 如果使用Action来代替Subscribe，调用subscribe（）方法的参数依次代表onNext、onError、onComplete方法。
        observable.subscribe(onNextAction, onErrorAction, onCompleteAction);
    }
}
