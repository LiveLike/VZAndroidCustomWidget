package com.android.vzexample

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Space
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.android.vzexample.cheer_meter.CheerMeterView
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
        viewModel.textPollViewModel1.observe(this, Observer { viewModel ->
            // Update UI with myData
            val widetView = CustomPollWidget(this@MainActivity).apply {
                this.pollWidgetModel = viewModel
            }
            addViews(widetView)

        })

        viewModel.textPollViewModel2.observe(this, Observer { viewModel ->
            // Update UI with myData
            val widetView = CustomPollWidget(this@MainActivity).apply {
                this.pollWidgetModel = viewModel
            }
            addViews(widetView)

        })

        // Observe the LiveData in the ViewModel
        viewModel.cheerMeterViewModel.observe(this, Observer { viewModel ->
            // Update UI with myData
            val  cheerWidetView = CheerMeterView(this@MainActivity).apply {
                this.widgetViewModel = viewModel
            }
            addViews(cheerWidetView)

        })

        // Trigger a function in the ViewModel
        viewModel.initLivelike()
    }

    fun addViews(view: View){
        views.add(view)
        if(views.size == 3){
            for (index in views.indices) {
                runOnUiThread {
                    val space = Space(baseContext)
                    space.minimumHeight = 20
                    binding.mainContainer.addView(views[index])
                    binding.mainContainer.addView(space)
                }
            }
        }
    }
}