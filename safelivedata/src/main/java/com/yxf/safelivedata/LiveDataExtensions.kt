package com.yxf.safelivedata

import androidx.lifecycle.MutableLiveData


fun <T> MutableLiveData<T>.setValueSync(value: T) {
    SafeLiveData.setValueSync(this, value)
}