package com.android.vzexample

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.android.vzexample.cheer_meter.CustomCheerMeter
import com.android.vzexample.databinding.ActivityMainBinding
import com.android.vzexample.poll.CustomPollWidget


class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    val viewModel: MainViewModel by viewModels()
    val views: ArrayList<View?> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout for this activity
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observe the LiveData in the ViewModel
        viewModel.liveLike.observe(this, Observer { livelikeInstance ->
            // Update UI with myData
            viewModel.fetchWidget()
        })

        // Observe the LiveData in the ViewModel
        viewModel.viewModel.observe(this, Observer { viewModel ->
            // Update UI with myData
            val widetView = CustomPollWidget(this@MainActivity).apply {
                this.pollWidgetModel = viewModel
            }
            addViews(widetView)

        })

        // Observe the LiveData in the ViewModel
        viewModel.cheerMeterViewModel.observe(this, Observer { viewModel ->
            // Update UI with myData
            val  cheerWidetView = CustomCheerMeter(this@MainActivity).apply {
                this.cheerMeterWidgetmodel = viewModel
            }
            addViews(cheerWidetView)

        })

        // Trigger a function in the ViewModel
        viewModel.initLivelike()
    }

    fun addViews(view: View){
        views.add(view)
        if(views.size == 2){
            var lastView: View? = null
            for (index in views.indices) {
                runOnUiThread {
//                    val layoutParams = FrameLayout.LayoutParams(
//                        FrameLayout.LayoutParams.MATCH_PARENT,
//                        FrameLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    if (lastView != null) {
//                        layoutParams.topMargin = lastView!!.bottom + 20 // Set the top margin based on the bottom of the previous view
//                    }
//                    views[index]?.layoutParams = layoutParams
                    binding.root.addView(views[index], index)
                    //lastView = views[index]!!
                }
            }
//            for (view in views){
//                binding.root.addView(view,0)
//            }
        }

    }
}