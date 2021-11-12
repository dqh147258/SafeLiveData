package com.yxf.safelivedata

import androidx.lifecycle.MutableLiveData
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock


fun <T> MutableLiveData<T>.setValueSync(value: T) {
    if (SafeLiveData.isInMainThread()) {
        setValue(value)
    } else {
        synchronized(this) {
            val condition = Object()
            SafeLiveData.handler.post {
                try {
                    this.value = value
                } finally {
                    condition.notifyAll()
                }
            }
            condition.wait()
        }
    }
}