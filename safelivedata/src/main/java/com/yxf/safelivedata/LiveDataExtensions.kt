package com.yxf.safelivedata

import androidx.lifecycle.MutableLiveData


fun <T> MutableLiveData<T>.setValueSync(value: T) {
    if (SafeLiveData.isInMainThread()) {
        setValue(value)
    } else {
        synchronized(this) {
            val condition = Object()
            SafeLiveData.getHandler().post {
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