package com.android.vzexample.poll

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.vzexample.R
import com.android.vzexample.databinding.CustomPollWidgetBinding
import com.android.vzexample.databinding.PollTextListItemBinding
import com.livelike.engagementsdk.OptionsItem
import com.livelike.engagementsdk.widget.widgetModel.PollWidgetModel


class CustomPollWidget : ConstraintLayout {
    var pollWidgetModel: PollWidgetModel? = null
    private lateinit var binding: CustomPollWidgetBinding

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
        binding = CustomPollWidgetBinding.inflate(LayoutInflater.from(context))
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
        pollWidgetModel?.widgetData?.let { liveLikeWidget ->
            binding.txtTitle.text = liveLikeWidget.question
            binding.pollTitle.text = "TEXT POLL"


            liveLikeWidget.options?.let { list ->
                binding.rcylPollList.layoutManager =
                    LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                val adapter =
                    PollListAdapter(
                        context,
                        ArrayList(list.map { item -> item!! })
                    )
                binding.rcylPollList.adapter = adapter
                list.forEach { op ->
                    op?.let {
                        adapter.optionIdCount[op.id!!] = op.voteCount ?: 0
                    }
                }

                adapter.pollListener = object : PollListAdapter.PollListener {
                    override fun onSelectOption(id: String) {
                        pollWidgetModel?.submitVote(id)
                    }
                }
                pollWidgetModel?.voteResults?.subscribe(this) { result ->
                    result?.choices?.let { options ->
                        options.forEach { op ->
                            adapter.optionIdCount[op.id!!] = op.voteCount ?: 0
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pollWidgetModel?.voteResults?.unsubscribe(this)
    }
}

class PollListAdapter(
    private val context: Context,
    private val list: ArrayList<OptionsItem>
) :
    RecyclerView.Adapter<PollListAdapter.PollListItemTextViewHolder>() {
    var selectedIndex = -1
    val optionIdCount: HashMap<String, Int> = hashMapOf()
    var pollListener: PollListener? = null


    interface PollListener {
        fun onSelectOption(id: String)
    }

    class PollListItemTextViewHolder(var itemTextBinding: PollTextListItemBinding) :
        RecyclerView.ViewHolder(itemTextBinding.root)

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): PollListItemTextViewHolder {
        val itemTextBinding =
            PollTextListItemBinding.inflate(LayoutInflater.from(p0.context), p0, false)

        return PollListItemTextViewHolder(itemTextBinding)
    }

    override fun onBindViewHolder(holder: PollListItemTextViewHolder, index: Int) {
        val item = list[index]
        
        if (optionIdCount.containsKey(item.id)) {
            holder.itemTextBinding.txtPercent.visibility = View.VISIBLE
            holder.itemTextBinding.progressBarText.visibility = View.VISIBLE
            val total = optionIdCount.values.reduce { acc, i -> acc + i }
            val percent = when (total > 0) {
                true -> (optionIdCount[item.id!!]!!.toFloat() / total.toFloat()) * 100
                else -> 0F
            }
            holder.itemTextBinding.txtPercent.text = "${percent.toInt()} %"
            holder.itemTextBinding.progressBarText.progress = percent.toInt()
        } else {
            holder.itemTextBinding.txtPercent.visibility = View.INVISIBLE
            holder.itemTextBinding.progressBarText.visibility = View.INVISIBLE
        }
        holder.itemTextBinding.textPollItem.text = "${item.description}"
        if (selectedIndex == index) {
            holder.itemTextBinding.layPollTextOption.setBackgroundResource(R.drawable.image_option_background_selected_drawable)
            holder.itemTextBinding.textPollItem.setTextColor(Color.WHITE)
            holder.itemTextBinding.txtPercent.setTextColor(Color.WHITE)
//            holder.itemTextBinding.progressBarText.progressDrawable = ContextCompat.getDrawable(
//                context,
//                R.drawable.custom_progress_color_options_selected
//            )
        } else {
            holder.itemTextBinding.layPollTextOption.setBackgroundResource(R.drawable.image_option_background_stroke_drawable)
//            holder.itemTextBinding.textPollItem.setTextColor(Color.BLACK)
//            holder.itemTextBinding.txtPercent.setTextColor(Color.BLACK)
            holder.itemTextBinding.progressBarText.progressDrawable = ContextCompat.getDrawable(
                context,
                R.drawable.custom_progress_color_options
            )
        }

        holder.itemTextBinding.layPollTextOption.setOnClickListener {
            selectedIndex = holder.adapterPosition
            pollListener?.onSelectOption(item.id!!)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = list.size
}
