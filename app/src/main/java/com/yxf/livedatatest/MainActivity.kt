package com.yxf.livedatatest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.yxf.livedatatest.databinding.ActivityMainBinding
import com.yxf.safelivedata.SafeLiveData

class MainActivity : AppCompatActivity() {

    private val vb by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vb.root)

        vb.run {
            normal.setOnClickListener {
                generateTask(MutableLiveData())
            }
            safe.setOnClickListener {
                generateTask(SafeLiveData())
            }
        }
    }

    private fun generateTask(liveData: MutableLiveData<ByteArray>) {
        System.gc()
        val task = TestTask(liveData)
        task.liveData.observe(this) {
            vb.status.text = String.format(
                "total memory : %#.3f , left : %#.3f",
                Runtime.getRuntime().totalMemory() * 1.0 / 1024 / 1024, Runtime.getRuntime().freeMemory() * 1.0 / 1024 / 1024
            )
        }
        task.setData(ByteArray(1024 * 1024 * 10))
    }
}