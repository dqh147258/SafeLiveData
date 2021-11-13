package com.yxf.livedatatest

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.yxf.safelivedata.SafeLiveData

class TestTask<T>(val liveData: MutableLiveData<T>) {

    val yourLiveData = MediatorLiveData<Int>()
    val infoLiveData = SafeLiveData<Int>(yourLiveData).apply { value = 0 }

    fun setData(t: T) {
        liveData.value = t
    }

}

object Manager {

    init {
        Setting.settingLiveData.observeForever{

        }
    }

}

object Setting {

    val settingLiveData = MutableLiveData<Boolean>()

}