package com.android.vzexample.cheer_meter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.android.vzexample.databinding.WidgetCheerMeterBinding
import com.bumptech.glide.Glide
import com.livelike.engagementsdk.widget.model.Resource
import com.livelike.engagementsdk.widget.viewModel.BaseViewModel
import com.livelike.engagementsdk.widget.viewModel.CheerMeterViewModel
import com.livelike.engagementsdk.widget.viewModel.CheerMeterWidget
import com.livelike.engagementsdk.widget.viewModel.WidgetStates
import com.livelike.utils.parseISODateTime
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.math.max

class CheerMeterView(context: Context, attr: AttributeSet? = null) :
    LinearLayout(context, attr) {

    private var mShowTeamResults: Boolean = false
    private var lastResult: Resource? = null
    private var viewModel: CheerMeterViewModel? = null

    private var inflated = false
    private var binding: WidgetCheerMeterBinding? = null
    protected val uiScope = MainScope()

    var widgetViewModel: BaseViewModel? = null
        set(value) {
            field = value
            viewModel = value as CheerMeterViewModel
        }

    // Refresh the view when re-attached to the activity
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        resourceObserver(viewModel?.dataFlow?.value)
        uiScope.launch {
            launch {
                viewModel?.resultsFlow?.collect { resultObserver(it) }
            }
            launch {
                viewModel?.voteEndFlow?.collect { endObserver(it) }
            }
        }
    }

    private fun resultObserver(resource: Resource?) {
        (resource ?: viewModel?.dataFlow?.value?.resource)?.let {
            lastResult = it
            val options = it.options ?: return
            if (options.size == 2) {
                val team1 = options[0]
                val team2 = options[1]
                val vote1 = max(team1.voteCount ?: 0, 1)
                val vote2 = max(team2.voteCount ?: 0, 1)
                val totalCount = max(vote1 + vote2, 1)

                binding?.llCheerMeterTeams?.weightSum = totalCount.toFloat()
                binding?.llCheerMeterTeams?.orientation = HORIZONTAL

                binding?.txtCheerMeterTeam1?.layoutParams = LayoutParams(
                    0,
                    LayoutParams.MATCH_PARENT,
                    vote1.toFloat()
                )

                binding?.txtCheerMeterTeam2?.layoutParams = LayoutParams(
                    0,
                    LayoutParams.MATCH_PARENT,
                    vote2.toFloat()
                )

                val voteOnePercent = (vote1 * 100)/totalCount
                binding?.txtCheerMeterTeam1?.text = "${voteOnePercent}%"
                binding?.txtCheerMeterTeam2?.text = "${(100-voteOnePercent)}%"
            }
            if (mShowTeamResults) {
                mShowTeamResults = false
                showTeamResults(it)
            }
        }
    }

    //private var angle = false

    private fun resourceObserver(widget: CheerMeterWidget?) {
        widget?.apply {
            val optionList = resource.getMergedOptions() ?: return
            if (!inflated) {
                inflated = true
                binding = WidgetCheerMeterBinding.inflate(
                    LayoutInflater.from(context),
                    this@CheerMeterView,
                    true
                )
            }

            binding?.txtCheerMeterTitle?.text = resource.question
            resource.interactiveUntil?.parseISODateTime()?.let {
                val epochTimeMs = it.toInstant().toEpochMilli()
                viewModel?.startInteractiveUntilTimeout(epochTimeMs)
            }
            if (optionList.size == 2) {
                binding?.txtCheerMeterTeam1?.text = optionList[0].voteCount.toString()
                Glide.with(context.applicationContext)
                    .load(optionList[0].imageUrl)
                    .into(binding?.imgLogoTeam1!!)

                binding?.txtCheerMeterTeam2?.text = optionList[0].voteCount.toString()

                Glide.with(context.applicationContext)
                    .load(optionList[1].imageUrl)
                    .into(binding?.imgLogoTeam2!!)

                setupTeamCheerRipple(binding?.imgLogoTeam1!!, 0)
                setupTeamCheerRipple(binding?.imgLogoTeam2!!, 1)
            }
            if (widgetViewModel?.widgetStateFlow?.value == null)
                widgetViewModel?.widgetStateFlow?.value = WidgetStates.READY
        }

        if (widget == null) {
            inflated = false
            removeAllViews()
            parent?.let { (it as ViewGroup).removeAllViews() }
        }
    }

    // this method setup the ripple view which animates on tapping up/cheering the team
    @SuppressLint("ClickableViewAccessibility")
    private fun setupTeamCheerRipple(teamView: View, teamIndex: Int) {
        teamView.setOnTouchListener(object : OnTouchListener {
            var handler = Handler(Looper.getMainLooper())
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                // when tapped for first time
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                            if (teamIndex == 0) {
                                binding?.txtCheerMeterTeam1!!.animate().alpha(1F)
                                    .setDuration(30).start()
                            } else {
                                binding?.txtCheerMeterTeam2!!.animate().alpha(1F)
                                    .setDuration(30).start()
                            }
                            handler.removeCallbacksAndMessages(null)
                        return false
                    }
                    MotionEvent.ACTION_DOWN -> {
                            viewModel?.incrementVoteCount(
                                teamIndex,
                                viewModel?.dataFlow?.value?.resource?.getMergedOptions()
                                    ?.get(teamIndex)?.id
                            )

                        return false
                    }
                    else -> return false
                }
            }
        }
        )
    }

    private fun endObserver(it: Boolean?) {
        if (it == true) {
            binding?.txtCheerMeterTeam1?.alpha = 1F
            binding?.txtCheerMeterTeam2?.alpha = 1F
            if (lastResult == null) {
                mShowTeamResults = true
            } else {
                showTeamResults(lastResult!!)
            }
        }
    }

    private fun showTeamResults(resource: Resource): Boolean {
        val options = viewModel?.dataFlow?.value?.resource?.options ?: return true
        if (options.size == 2) {

            val team1 = options[0]
            val team2 = options[1]
            team1.voteCount =
                resource.options?.find { option -> option.id == team1.id }?.voteCount
            team2.voteCount =
                resource.options?.find { option -> option.id == team2.id }?.voteCount
        }
        return false
    }
}
