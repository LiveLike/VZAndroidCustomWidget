package com.android.vzexample

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.livelike.common.AccessTokenDelegate
import com.livelike.common.LiveLikeKotlin
import com.livelike.engagementsdk.*
import com.livelike.engagementsdk.publicapis.LiveLikeCallback
import com.livelike.engagementsdk.widget.viewModel.CheerMeterViewModel
import com.livelike.engagementsdk.widget.widgetModel.PollWidgetModel
import kotlinx.coroutines.Dispatchers


class MainViewModel : ViewModel() {

    lateinit var contentSession: ContentSession

    // Create a LiveData object to hold the data
    private val _livelike = MutableLiveData<LiveLikeKotlin>()
    val liveLike: LiveData<LiveLikeKotlin>
        get() = _livelike

    val viewModel = MutableLiveData<PollWidgetModel>()
    val cheerMeterViewModel = MutableLiveData<CheerMeterViewModel>()

    // Function to do some action and update the LiveData
    fun initLivelike() {
        // Perform some action to update data
        // Update the LiveData
        _livelike.value = LiveLikeKotlin(
            clientId = "8PqSNDgIVHnXuJuGte1HdvOjOqhCFE1ZCR3qhqaS",
            accessTokenDelegate = object: AccessTokenDelegate {
                override fun getAccessToken(): String? {
                    return null
                }
                override fun storeAccessToken(accessToken: String?) {
                }

            } ,
            //uiDispatcher = Dispatchers.Default
        )
    }

    fun fetchWidget() {
        contentSession = liveLike.value?.createContentSession(
            programId = "09d93835-ee52-4757-976c-ea09d6a5798c",
            timecodeGetter = object : LiveLikeKotlin.TimecodeGetterCore {
                override fun getTimecode(): EpochTime {
                    return EpochTime(0)
                }

            },
            isPlatformLocalContentImageUrl = { false },
            uiDispatcher = Dispatchers.Default
        ) as ContentSession

        liveLike.value?.fetchWidgetDetails("25a04ec3-d93f-4fe2-bc18-6e65931618e0",
            "text-poll",
            object : LiveLikeCallback<LiveLikeWidget>() {
                override fun onResponse(result: LiveLikeWidget?, error: String?) {
                    result?.let {
                        viewModel.value =
                            contentSession.getWidgetModelFromLiveLikeWidget(it) as PollWidgetModel
                    }
                    error?.let {
                        println("error---" + it)
                    }
                }
            })

        liveLike.value?.fetchWidgetDetails("4efbb87b-afef-45c8-a767-25efaf5e0256",
            "cheer-meter",
            object : LiveLikeCallback<LiveLikeWidget>() {
                override fun onResponse(result: LiveLikeWidget?, error: String?) {
                    result?.let {
                        cheerMeterViewModel.value =
                            contentSession.getWidgetModelFromLiveLikeWidget(it) as CheerMeterViewModel
                    }
                    error?.let {
                        println("error---" + it)
                    }
                }
            })
    }
}
