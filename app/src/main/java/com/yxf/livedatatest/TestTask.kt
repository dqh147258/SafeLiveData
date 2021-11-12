package com.yxf.livedatatest

import androidx.lifecycle.MutableLiveData

class TestTask<T>(val liveData: MutableLiveData<T>) {

    fun setData(t: T) {
        liveData.value = t
    }


}