package com.yxf.safelivedata;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SafeLiveData<T> extends MutableLiveData<T> {

    private final MutableLiveData<T> realLiveData;

    static Handler handler;

    private static boolean isInMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    private static Handler getHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    private static void runInMainThread(Runnable runnable) {
        boolean inMainThread = isInMainThread();
        if (inMainThread) {
            runnable.run();
        } else {
            getHandler().post(runnable);
        }
    }

    public static <T> void setValueSync(MutableLiveData<T> liveData, T value) {
        if (liveData == null) {
            return;
        }
        if (isInMainThread()) {
            liveData.setValue(value);
            return;
        }
        Runnable runnable = () -> {
            synchronized (liveData) {
                try {
                    liveData.setValue(value);
                } finally {
                    liveData.notify();
                }
            }
        };
        synchronized (liveData) {
            getHandler().post(runnable);
            try {
                liveData.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private List<WeakReference<Observer<? super T>>> observerReferenceList = new ArrayList();

    public SafeLiveData(MutableLiveData<T> liveData) {
        super();
        realLiveData = liveData;
    }

    public SafeLiveData() {
        super();
        realLiveData = new MutableLiveData<T>();
    }


    @MainThread
    private void removeObserverReference(Observer<? super T> observer) {
        int size = observerReferenceList.size();
        for (int i = size - 1; i >= 0; i--) {
            WeakReference reference = observerReferenceList.get(i);
            if (reference.get() == observer) {
                observerReferenceList.remove(i);
            }
        }
    }

    @MainThread
    private boolean isObserverExist(@NonNull Observer<? super T> observer) {
        int size = observerReferenceList.size();
        for (int i = 0; i < size; i++) {
            WeakReference<Observer<? super T>> reference = observerReferenceList.get(i);
            if (reference == observer) {
                return true;
            }
        }
        return false;
    }

    @MainThread
    private void recordObserver(@NonNull Observer<? super T> observer) {
        removeObserverReference(null);
        if (!isObserverExist(observer)) {
            WeakReference<Observer<? super T>> reference = new WeakReference(observer);
            observerReferenceList.add(reference);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        runInMainThread(() -> {
            int size = observerReferenceList.size();
            for (int i = 0; i < size; i++) {
                Observer<? super T> observer = observerReferenceList.get(i).get();
                if (observer != null) {
                    realLiveData.removeObserver(observer);
                }
            }
        });
    }

    @Override
    public void observe(@NonNull @NotNull LifecycleOwner owner, @NonNull @NotNull Observer<? super T> observer) {
        runInMainThread(() -> {
            recordObserver(observer);
            realLiveData.observe(owner, observer);
        });
    }

    @Override
    public void observeForever(@NonNull @NotNull Observer<? super T> observer) {
        runInMainThread(() -> {
            recordObserver(observer);
            realLiveData.observeForever(observer);
        });
    }

    @Override
    public void removeObserver(@NonNull @NotNull Observer<? super T> observer) {
        runInMainThread(() -> {
            realLiveData.removeObserver(observer);
            removeObserverReference(observer);
        });
    }

    @Override
    public void removeObservers(@NonNull @NotNull LifecycleOwner owner) {
        runInMainThread(() -> realLiveData.removeObservers(owner));
    }

    @Override
    public void postValue(T value) {
        realLiveData.postValue(value);
    }

    @Override
    public void setValue(T value) {
        setValueSync(realLiveData, value);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public T getValue() {
        return realLiveData.getValue();
    }

    @Override
    public boolean hasObservers() {
        return realLiveData.hasObservers();
    }

    @Override
    public boolean hasActiveObservers() {
        return realLiveData.hasActiveObservers();
    }
}
