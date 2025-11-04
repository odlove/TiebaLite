package com.huanchengfly.tieba.post

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.annotation.Keep
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.SketchFactory
import com.github.panpf.sketch.decode.GifAnimatedDrawableDecoder
import com.github.panpf.sketch.decode.GifMovieDrawableDecoder
import com.github.panpf.sketch.decode.HeifAnimatedDrawableDecoder
import com.github.panpf.sketch.decode.WebpAnimatedDrawableDecoder
import com.github.panpf.sketch.http.OkHttpStack
import com.github.panpf.sketch.request.PauseLoadWhenScrollingDrawableDecodeInterceptor
import com.huanchengfly.tieba.post.activities.BaseActivity
import com.huanchengfly.tieba.core.runtime.RuntimeInitializer
import com.microsoft.appcenter.distribute.Distribute
import com.microsoft.appcenter.distribute.DistributeListener
import com.microsoft.appcenter.distribute.ReleaseDetails
import com.microsoft.appcenter.distribute.UpdateAction
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil
import com.huanchengfly.tieba.post.utils.appPreferences
import dagger.hilt.android.HiltAndroidApp
import net.swiftzer.semver.SemVer
import javax.inject.Inject


@HiltAndroidApp
class App : Application(), SketchFactory {
    private val mActivityList: MutableList<Activity> = mutableListOf()

    @Inject
    lateinit var runtimeInitializer: RuntimeInitializer

    override fun onCreate() {
        INSTANCE = this
        super.onCreate()
        runtimeInitializer.initialize(this)
    }

    //解决魅族 Flyme 系统夜间模式强制反色
    @Keep
    fun mzNightModeUseOf(): Int = 2

    //禁止app字体大小跟随系统字体大小调节
    override fun getResources(): Resources {
        //INSTANCE = this
        val fontScale = appPreferences.fontScale
        val resources = super.getResources()
        if (resources.configuration.fontScale != fontScale) {
            val configuration = resources.configuration
            configuration.fontScale = fontScale
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        return resources
    }

    /**
     * 添加Activity
     */
    fun addActivity(activity: Activity) {
        // 判断当前集合中不存在该Activity
        if (!mActivityList.contains(activity)) {
            mActivityList.add(activity) //把当前Activity添加到集合中
        }
    }

    /**
     * 销毁单个Activity
     */
    @JvmOverloads
    fun removeActivity(activity: Activity, finish: Boolean = false) {
        //判断当前集合中存在该Activity
        if (mActivityList.contains(activity)) {
            mActivityList.remove(activity) //从集合中移除
            if (finish) activity.finish() //销毁当前Activity
        }
    }

    /**
     * 销毁所有的Activity
     */
    fun removeAllActivity() {
        //通过循环，把集合中的所有Activity销毁
        for (activity in mActivityList) {
            activity.finish()
        }
    }

    class MyDistributeListener : DistributeListener {
        override fun onReleaseAvailable(
            activity: Activity,
            releaseDetails: ReleaseDetails
        ): Boolean {
            val versionName = releaseDetails.shortVersion
            val newSemVer = SemVer.parse(versionName)
            val currentSemVer = SemVer.parse(BuildConfig.VERSION_NAME)
            if (newSemVer <= currentSemVer) {
                return true
            }
            val releaseNotes = releaseDetails.releaseNotes
            if (activity is BaseActivity) {
                activity.showDialog {
                    setTitle(activity.getString(R.string.title_dialog_update, versionName))
                    setMessage(releaseNotes)
                    setCancelable(!releaseDetails.isMandatoryUpdate)
                    setPositiveButton(R.string.appcenter_distribute_update_dialog_download) { _, _ ->
                        Distribute.notifyUpdateAction(UpdateAction.UPDATE)
                    }
                    if (!releaseDetails.isMandatoryUpdate) {
                        setNeutralButton(R.string.appcenter_distribute_update_dialog_postpone) { _, _ ->
                            Distribute.notifyUpdateAction(UpdateAction.POSTPONE)
                        }
                        setNegativeButton(R.string.button_next_time, null)
                    }
                }
            }
            return true
        }

        override fun onNoReleaseAvailable(activity: Activity) {}
    }

    companion object {
        const val TAG = "App"

        private val packageName: String
            get() = INSTANCE.packageName

        @JvmStatic
        lateinit var INSTANCE: App
            private set

        val isInitialized: Boolean
            get() = this::INSTANCE.isInitialized

        val isSystemNight: Boolean
            get() = nightMode == Configuration.UI_MODE_NIGHT_YES

        val isFirstRun: Boolean
            get() = SharedPreferencesUtil.get(SharedPreferencesUtil.SP_APP_DATA)
                .getBoolean("first", true)

        private val nightMode: Int
            get() = INSTANCE.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    }


    override fun createSketch(): Sketch = Sketch.Builder(this).apply {
        httpStack(OkHttpStack.Builder().apply {
            userAgent(System.getProperty("http.agent"))
        }.build())
        components {
            addDrawableDecodeInterceptor(PauseLoadWhenScrollingDrawableDecodeInterceptor())
            addDrawableDecoder(
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> GifAnimatedDrawableDecoder.Factory()
                    else -> GifMovieDrawableDecoder.Factory()
                }
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                addDrawableDecoder(WebpAnimatedDrawableDecoder.Factory())
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                addDrawableDecoder(HeifAnimatedDrawableDecoder.Factory())
            }
        }
    }.build()
}
