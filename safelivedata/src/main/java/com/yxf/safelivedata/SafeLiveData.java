package com.yxf.safelivedata;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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

    private static Handler handler;

    private static void runInMainThread(Runnable runnable) {
        boolean inMainThread = Looper.getMainLooper() == Looper.myLooper();
        if (inMainThread) {
            runnable.run();
        } else {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            handler.post(runnable);
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


    private void removeObserverReference(Observer<? super T> observer) {
        int size = observerReferenceList.size();
        for (int i = size - 1; i >= 0; i--) {
            WeakReference reference = observerReferenceList.get(i);
            if (reference.get() == observer) {
                observerReferenceList.remove(i);
            }
        }
    }

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
                    removeObserver(observer);
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
        realLiveData.setValue(value);
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
