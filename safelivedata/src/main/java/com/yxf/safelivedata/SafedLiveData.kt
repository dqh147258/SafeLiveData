package com.yxf.safelivedata

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.lang.ref.WeakReference

open class SafedLiveData<T>(
    private val realLiveData: MutableLiveData<T> = MutableLiveData<T>()
) : MutableLiveData<T>() {

    companion object {

        private val handler by lazy { Handler(Looper.getMainLooper()) }

        private fun runInMainThread(block: () -> Unit) {
            val inMainThread = Looper.getMainLooper() == Looper.myLooper()
            if (inMainThread) {
                block()
            } else {
                handler.post { block() }
            }
        }
    }

    private val observerReferenceList by lazy { ArrayList<WeakReference<Observer<in T>>>() }

    private fun removeObserverReference(observer: Observer<in T>? = null) {
        val size = observerReferenceList.size
        for (i in size - 1 downTo 0) {
            val reference = observerReferenceList[i]
            if (reference.get() == observer) {
                observerReferenceList.removeAt(i)
            }
        }
    }

    private fun recordObserver(observer: Observer<in T>) {
        removeObserverReference()
        val reference = WeakReference(observer)
        observerReferenceList.add(reference)
    }

    protected fun finalize() {
        runInMainThread {
            observerReferenceList.forEach {
                val value = it.get() ?: return@forEach
                realLiveData.removeObserver(value)
            }
        }
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        runInMainThread {
            recordObserver(observer)
            realLiveData.observe(owner, observer)
        }
    }

    override fun observeForever(observer: Observer<in T>) {
        runInMainThread {
            recordObserver(observer)
            realLiveData.observeForever(observer)
        }
    }

    override fun removeObserver(observer: Observer<in T>) {
        runInMainThread {
            realLiveData.removeObserver(observer)
            removeObserverReference(observer)
        }
    }

    override fun removeObservers(owner: LifecycleOwner) {
        runInMainThread {
            realLiveData.removeObservers(owner)
        }
    }

    override fun postValue(value: T) {
        realLiveData.postValue(value)
    }

    override fun setValue(value: T) {
        realLiveData.value = value
    }

    override fun getValue(): T? {
        return realLiveData.value
    }

    override fun hasObservers(): Boolean {
        return realLiveData.hasObservers()
    }

    override fun hasActiveObservers(): Boolean {
        return realLiveData.hasActiveObservers()
    }

}