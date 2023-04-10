package com.android.vzexample.cheer_meter

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.vzexample.databinding.CustomCheerMeterBinding
import com.bumptech.glide.Glide
import com.livelike.engagementsdk.widget.widgetModel.CheerMeterWidgetmodel

class CustomCheerMeter : ConstraintLayout {

    lateinit var cheerMeterWidgetmodel: CheerMeterWidgetmodel
    private lateinit var binding: CustomCheerMeterBinding

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }


    private fun init(attrs: AttributeSet?, defStyle: Int) {
        binding = CustomCheerMeterBinding.inflate(LayoutInflater.from(context))
        binding.root.setLayoutParams(
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        addView(binding.root)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        cheerMeterWidgetmodel.widgetData.let { likeWidget ->

                //binding.timeBar.visibility = View.INVISIBLE
                val op1 = likeWidget.options?.get(0)
                val op2 = likeWidget.options?.get(1)
                val vt1 = op1?.voteCount ?: 0
                val vt2 = op2?.voteCount ?: 0
                val total = vt1 + vt2
                if (total > 0) {
                    val perVt1 = (vt1.toFloat() / total) * 100
                    val perVt2 = (vt2.toFloat() / total) * 100
                    binding.prgCheerTeam1
                    binding.prgCheerTeam1.progress = perVt1.toInt()
                    binding.prgCheerTeam2.progress = perVt2.toInt()
//                    winnerOptionItem = if (perVt1 > perVt2) {
//                        likeWidget.options?.get(0)
//                    } else {
//                        likeWidget.options?.get(1)
//                    }
                    //showWinnerAnimation()
                }



            binding.txtTitle.text = likeWidget.question
//            binding.vsAnim.setAnimation("vs-1-light.json")
//            binding.vsAnim.playAnimation()
            likeWidget.options?.let { options ->
                if (options.size == 2) {
                    options[0]?.let { op ->
                        Glide.with(context)
                            .load(op.imageUrl)
                            .into(binding.imgCheerTeam1)

                        binding.frameCheerTeam1.setOnClickListener {
                            cheerMeterWidgetmodel.submitVote(op.id!!)
                        }
                    }
                    options[1]?.let { op ->
                        Glide.with(context)
                            .load(op.imageUrl)
                            .into(binding.imgCheerTeam2)

                        binding.frameCheerTeam2.setOnClickListener {
                            cheerMeterWidgetmodel.submitVote(op.id!!)
                        }
                    }
                    cheerMeterWidgetmodel.voteResults.subscribe(this) {
                        it?.let {
                            val op1 = it.choices?.get(0)
                            val op2 = it.choices?.get(1)
                            val vt1 = op1?.voteCount ?: 0
                            val vt2 = op2?.voteCount ?: 0
                            val total = vt1 + vt2
                            if (total > 0) {
                                val perVt1 = (vt1.toFloat() / total) * 100
                                val perVt2 = (vt2.toFloat() / total) * 100
                                binding.prgCheerTeam1.progress = perVt1.toInt()
                                binding.prgCheerTeam2.progress = perVt2.toInt()
//                                winnerOptionItem = if (perVt1 > perVt2) {
//                                    options[0]
//                                } else {
//                                    options[1]
//                                }
                            }
                        }
                    }
                }
            }

        }
    }

//    private fun showWinnerAnimation() {
//        winnerOptionItem?.let { op ->
//            cheer_result_team.visibility = View.VISIBLE
//            Glide.with(this@CustomCheerMeter.context)
//                .load(op.imageUrl)
//                .into(img_winner_team)
//            img_winner_anim.setAnimation("winner_animation.json")
//            img_winner_anim.playAnimation()
//        }
//    }
}