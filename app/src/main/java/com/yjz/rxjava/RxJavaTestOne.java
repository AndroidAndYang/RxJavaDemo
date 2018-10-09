package com.yjz.rxjava;

import android.support.annotation.NonNull;
import android.util.Log;

import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.AsyncSubject;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;


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

        Subscriber<String> subscriber = getSubscriber();

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

    /**
     * ⼀个AsyncSubject只在原始Observable完成后，发射来⾃原始Observable的最后⼀个值。它会把这最后⼀个值发射给任何后续的观察者。
     * AsyncSubject只会发送最后一个onNext()事件,如果此前存在onNext()方法,则不会发送。
     * 调用onError()或者onComplete()方法终止。
     */
    public static void testAsyncSubject()
    {
        AsyncSubject<String> asyncSubject = AsyncSubject.create();
        Subscriber<String> subscriber = getSubscriber();
        asyncSubject.subscribe(subscriber);
        //被观察者发出事件 如果调⽤onCompleted(),onNext()则会打印最后⼀个事件；如果没有,onNext()则不打印任何事件。
        asyncSubject.onNext("asyncSubject1");
        asyncSubject.onNext("asyncSubject2");
        asyncSubject.onCompleted();
        asyncSubject.onNext("asyncSubject3");
    }

    /**
     * 当观察者订阅BehaviorSubject时，他会将订阅前最后⼀次发送的事件和订阅后的所有发送事件都打印出来。
     * 如果订阅前无发送事件，则会默认接收构造器create(T)⾥⾯的对象和订阅后的所有事件。
     */
    public static void testBehaviorSubject()
    {
        BehaviorSubject<String> behaviorSubject = BehaviorSubject.create("Normal");
        Subscriber<String> subscriber = getSubscriber();
        behaviorSubject.onNext("behaviorSubject1");
        // 接受订阅前的最后一次onNext()事件。
        behaviorSubject.onNext("behaviorSubject2");
        behaviorSubject.subscribe(subscriber);
        // 以及订阅后的所有的onNext()事件。
        behaviorSubject.onNext("behaviorSubject3");
        behaviorSubject.onNext("behaviorSubject4");
    }

    /**
     * PublishSubject只会把在订阅发⽣的时间点之后来⾃原始Observable的数据发射给观察者。
     */
    public static void testPublishSubject()
    {
        PublishSubject<String> publishSubject = PublishSubject.create();

        // 不会发送给观察者
        publishSubject.onNext("publishSubject1");

        Action1<String> onNextAction1 = new Action1<String>() {
            @Override
            public void call(String s)
            {
                Log.e(TAG, "onNextAction1 call: " + s);
            }
        };
        Action1<String> onNextAction2 = new Action1<String>() {
            @Override
            public void call(String s)
            {
                Log.e(TAG, "onNextAction2 call: " + s);
            }
        };
        // 不会发送给观察者
        publishSubject.onNext("publishSubject1");
        // PublishSubject可能会⼀创建完成就⽴刻开始发射数据，因此这⾥有⼀个⻛险：在Subject被
        //创建后到有观察者订阅它之前这个时间段内，⼀个或多个数据可能会丢失。
        publishSubject.subscribe(onNextAction1);
        publishSubject.onNext("publishSubject2");
        publishSubject.subscribe(onNextAction2);
        publishSubject.onNext("publishSubject3");
    }

    /**
     * ReplaySubject会发射所有来⾃原始Observable的数据给观察者，⽆论它们是何时订阅的。
     */
    public static void testReplaySubject()
    {
        ReplaySubject<String> replaySubject = ReplaySubject.create();

        replaySubject.onNext("replaySubject1");

        Subscriber<String> subscriber = getSubscriber();

        replaySubject.onNext("replaySubject2");

        replaySubject.subscribe(subscriber);

        replaySubject.onNext("replaySubject3");
    }

    /*
        subject总结：
            1）AsyncSubject：⽆论何时订阅观察者只会接受订阅之前最后一次onNext()方法。
            2）BehaviorSubject：观察者会接受订阅之前的最后一次onNext()方法以及订阅之后所有的onNext()方法。如果订阅之前没有onNext()则调⽤默
                认creat(T)传⼊的对象。
            3）PublishSubject：观察者只会接受订阅之后所有的onNext()方法。
            4）ReplySubject: 观察者会接受任何事件被观察者所发出的onNext()方法。

     */

    @NonNull
    private static Subscriber<String> getSubscriber()
    {
        return new Subscriber<String>() {
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
    }
}
