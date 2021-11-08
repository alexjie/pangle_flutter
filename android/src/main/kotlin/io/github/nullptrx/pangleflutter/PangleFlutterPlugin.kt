package io.github.nullptrx.pangleflutter

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.bytedance.sdk.openadsdk.TTAdConstant
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry
import io.github.nullptrx.pangleflutter.common.PangleLoadingType
import io.github.nullptrx.pangleflutter.common.PangleOrientation
import io.github.nullptrx.pangleflutter.common.TTSize
import io.github.nullptrx.pangleflutter.common.TTSizeF
import io.github.nullptrx.pangleflutter.delegate.FLTInterstitialExpressAd
import io.github.nullptrx.pangleflutter.delegate.FLTSplashAd
import io.github.nullptrx.pangleflutter.util.asMap
import io.github.nullptrx.pangleflutter.view.BannerViewFactory
import io.github.nullptrx.pangleflutter.view.FeedViewFactory
import io.github.nullptrx.pangleflutter.view.NativeBannerViewFactory
import io.github.nullptrx.pangleflutter.view.SplashViewFactory

import io.flutter.plugin.common.*

/** PangleFlutterPlugin */
open class PangleFlutterPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
  companion object {
    val kDefaultBannerAdCount = 3
    val kDefaultFeedAdCount = 3
    val kChannelName = "nullptrx.github.io/pangle"
    val kEventChannelName = "nullptrx.github.io/pangle/adevent"

    @JvmStatic
    fun registerWith(registrar: PluginRegistry.Registrar) {

      PangleFlutterPlugin().apply {

        val messenger = registrar.messenger()
        val activity = registrar.activity()
        this.activity = activity
        this.context = registrar.context().applicationContext

        methodChannel = MethodChannel(messenger, kChannelName)
        methodChannel?.setMethodCallHandler(this)

        eventChannel = EventChannel(messenger, kEventChannelName)
        eventChannel?.setStreamHandler(PangleEventStream)

        bannerViewFactory = BannerViewFactory(messenger)
        registrar.platformViewRegistry().registerViewFactory(
          "nullptrx.github.io/pangle_bannerview", bannerViewFactory
        )
        feedViewFactory = FeedViewFactory(messenger)
        registrar.platformViewRegistry().registerViewFactory(
          "nullptrx.github.io/pangle_feedview", feedViewFactory
        )

        val splashViewFactory = SplashViewFactory(messenger)
        registrar.platformViewRegistry().registerViewFactory(
          "nullptrx.github.io/pangle_splashview", splashViewFactory
        )

        val nativeBannerViewFactory = NativeBannerViewFactory(messenger)
        registrar.platformViewRegistry().registerViewFactory(
          "nullptrx.github.io/pangle_nativebannerview", nativeBannerViewFactory
        )

        feedViewFactory?.attachActivity(activity)
        bannerViewFactory?.attachActivity(activity)

      }
    }
  }

  private var methodChannel: MethodChannel? = null
  private var activity: Activity? = null
  private var context: Context? = null
  private var bannerViewFactory: BannerViewFactory? = null
  private var feedViewFactory: FeedViewFactory? = null
  private val handler = Handler(Looper.getMainLooper())

  // 添加EventChannel处理消息各种事件回调
  private var eventChannel: EventChannel? = null

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
    feedViewFactory?.attachActivity(binding.activity)
    bannerViewFactory?.attachActivity(binding.activity)
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activity = binding.activity
    feedViewFactory?.attachActivity(binding.activity)
    bannerViewFactory?.attachActivity(binding.activity)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    feedViewFactory?.detachActivity()
    bannerViewFactory?.detachActivity()
    activity = null
  }

  override fun onDetachedFromActivity() {
    feedViewFactory?.detachActivity()
    bannerViewFactory?.detachActivity()
    activity = null
  }

  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {

    context = binding.applicationContext
    val context = binding.applicationContext

    methodChannel = MethodChannel(binding.binaryMessenger, kChannelName)
    methodChannel?.setMethodCallHandler(this)

    bannerViewFactory = BannerViewFactory(binding.binaryMessenger)
    binding.platformViewRegistry.registerViewFactory(
      "nullptrx.github.io/pangle_bannerview", bannerViewFactory
    )
    feedViewFactory = FeedViewFactory(binding.binaryMessenger)
    binding.platformViewRegistry.registerViewFactory(
      "nullptrx.github.io/pangle_feedview", feedViewFactory
    )

    val splashViewFactory = SplashViewFactory(binding.binaryMessenger)
    binding.platformViewRegistry.registerViewFactory(
      "nullptrx.github.io/pangle_splashview", splashViewFactory
    )

    val nativeBannerViewFactory = NativeBannerViewFactory(binding.binaryMessenger)
    binding.platformViewRegistry.registerViewFactory(
      "nullptrx.github.io/pangle_nativebannerview", nativeBannerViewFactory
    )
    
    eventChannel = EventChannel(binding.binaryMessenger, kEventChannelName)
    eventChannel?.setStreamHandler(PangleEventStream)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    methodChannel?.setMethodCallHandler(null)
    methodChannel = null

    eventChannel?.setStreamHandler(null)
    eventChannel = null;
  }

  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    activity ?: return
    val pangle = PangleAdManager.shared
    when (call.method) {
      "getSdkVersion" -> {
        val version = pangle.getSdkVersion()
        result.success(version)
      }
      "init" -> {
        pangle.initialize(activity, call.arguments.asMap() ?: mapOf()) {
          handler.post {
            result.success(it)
          }
        }
      }

      "requestPermissionIfNecessary" -> {
        context?.also {
          pangle.requestPermissionIfNecessary(it)
        }
      }
      "loadSplashAd" -> {
        val slotId =
          call.argument<String>("slotId")!! // val isExpress = call.argument<Boolean>("isExpress") ?: false
        val tolerateTimeout = call.argument<Double>("tolerateTimeout")
        val hideSkipButton = call.argument<Boolean>("hideSkipButton")
        val isSupportDeepLink = call.argument<Boolean>("isSupportDeepLink") ?: true
        val imgSize = TTSize(1080, 1920)
        val splashButtonType =
          call.argument<Int>("splashButtonType") ?: TTAdConstant.SPLASH_BUTTON_TYPE_FULL_SCREEN
        val downloadType = call.argument<Int>("downloadType") ?: TTAdConstant.DOWNLOAD_TYPE_NO_POPUP
        val adSlot = PangleAdSlotManager.getSplashAdSlot(
          slotId, imgSize, isSupportDeepLink, splashButtonType, downloadType
        )
        pangle.loadSplashAd(adSlot, FLTSplashAd(hideSkipButton, activity) {
          result.success(it)
        }, tolerateTimeout)
      }
      "loadRewardedVideoAd" -> {

        val loadingTypeIndex = call.argument<Int>("loadingType") ?: 0
        val loadingType = PangleLoadingType.values()[loadingTypeIndex]

        if (PangleLoadingType.preload == loadingType || PangleLoadingType.normal == loadingType) {
          val slotId = call.argument<String>("slotId")!!
          val loadResult = pangle.showRewardedVideoAd(slotId, activity) {
            if (PangleLoadingType.preload == loadingType) {
              loadRewardedVideoAdOnly(call, PangleLoadingType.preload_only)
            }
            result.success(it)
          }
          if (!loadResult) {
            loadRewardedVideoAdOnly(call, PangleLoadingType.normal, result)
          }

        } else {
          loadRewardedVideoAdOnly(call, PangleLoadingType.preload_only, result)
        }

      }

      "loadBannerAd" -> {
        val slotId = call.argument<String>("slotId")!!
        val count = call.argument<Int>("count") ?: kDefaultBannerAdCount
        val isSupportDeepLink = call.argument<Boolean>("isSupportDeepLink") ?: true

        val expressArgs = call.argument<Map<String, Double>>("expressSize") ?: mapOf()
        val w: Float = expressArgs.getValue("width").toFloat()
        val h: Float = expressArgs.getValue("height").toFloat()
        val expressSize = TTSizeF(w, h)
        val downloadType = call.argument<Int>("downloadType") ?: TTAdConstant.DOWNLOAD_TYPE_NO_POPUP
        val adSlot = PangleAdSlotManager.getBannerAdSlot(
          slotId, expressSize, count, isSupportDeepLink, downloadType
        )
        pangle.loadBanner2ExpressAd(adSlot) {
          result.success(it)
        }
      }

      "loadFeedAd" -> {
        val slotId = call.argument<String>("slotId")!!
        val count = call.argument<Int>("count") ?: kDefaultFeedAdCount
        val isSupportDeepLink = call.argument<Boolean>("isSupportDeepLink") ?: true
        val expressArgs = call.argument<Map<String, Double>>("expressSize") ?: mapOf()
        val w: Float = expressArgs.getValue("width").toFloat()
        val h: Float = expressArgs.getValue("height").toFloat()
        val expressSize = TTSizeF(w, h)
        val downloadType = call.argument<Int>("downloadType") ?: TTAdConstant.DOWNLOAD_TYPE_NO_POPUP
        val adSlot = PangleAdSlotManager.getFeedAdSlot(
          slotId, expressSize, count, isSupportDeepLink, downloadType
        )
        pangle.loadFeedExpressAd(adSlot) {
          result.success(it)
        }

      }
      "removeFeedAd" -> {
        val feedIds = call.arguments<List<String>>()
        var count = 0
        for (feedId in feedIds) {
          val success = PangleAdManager.shared.removeExpressAd(feedId)
          if (success) {
            count++
          }
        }
        result.success(count)
      }

      "loadInterstitialAd" -> {
        val slotId = call.argument<String>("slotId")!!
        val isSupportDeepLink = call.argument<Boolean>("isSupportDeepLink") ?: true
        val expressArgs = call.argument<Map<String, Double>>("expressSize") ?: mapOf()
        val w: Float = expressArgs.getValue("width").toFloat()
        val h: Float = expressArgs.getValue("height").toFloat()
        val expressSize = TTSizeF(w, h)
        val downloadType = call.argument<Int>("downloadType") ?: TTAdConstant.DOWNLOAD_TYPE_NO_POPUP

        val adSlot = PangleAdSlotManager.getInterstitialAdSlot(
          slotId, expressSize, isSupportDeepLink, downloadType
        )
        pangle.loadInteractionExpressAd(adSlot, FLTInterstitialExpressAd(activity) {
          result.success(it)
        })
      }

      "loadFullscreenVideoAd" -> {

        val loadingTypeIndex = call.argument<Int>("loadingType") ?: 0
        val loadingType = PangleLoadingType.values()[loadingTypeIndex]

        if (PangleLoadingType.preload == loadingType || PangleLoadingType.normal == loadingType) {
          val slotId = call.argument<String>("slotId")!!
          val loadResult = pangle.showFullScreenVideoAd(slotId, activity) {
            if (PangleLoadingType.preload == loadingType) {
              loadFullscreenVideoAdOnly(call, PangleLoadingType.preload_only)
            }
            result.success(it)
          }
          if (!loadResult) {
            loadFullscreenVideoAdOnly(call, PangleLoadingType.normal, result)
          }

        } else {
          loadFullscreenVideoAdOnly(call, PangleLoadingType.preload_only, result)
        }


      }
      "setThemeStatus" -> {
        var theme: Int = call.arguments()
        pangle.setThemeStatus(theme)
        theme = pangle.getThemeStatus()
        result.success(theme)
      }
      "getThemeStatus" -> {
        val theme = pangle.getThemeStatus()
        result.success(theme)
      }

      else -> result.notImplemented()
    }

  }

  private fun loadRewardedVideoAdOnly(
    call: MethodCall, loadingType: PangleLoadingType, result: MethodChannel.Result? = null
  ) {

    val slotId = call.argument<String>("slotId")!!
    val userId = call.argument<String>("userId")
    val rewardName = call.argument<String>("rewardName")
    val rewardAmount = call.argument<Int>("rewardAmount")
    val extra = call.argument<String>("extra")
    val isVertical = call.argument<Boolean>("isVertical") ?: true
    val isSupportDeepLink = call.argument<Boolean>("isSupportDeepLink") ?: true
    val expressArgs = call.argument<Map<String, Double>>("expressSize") ?: mapOf()
    val w: Float = expressArgs.getValue("width").toFloat()
    val h: Float = expressArgs.getValue("height").toFloat()
    val expressSize = TTSizeF(w, h)
    val downloadType = call.argument<Int>("downloadType") ?: TTAdConstant.DOWNLOAD_TYPE_NO_POPUP
    val adSlot = PangleAdSlotManager.getRewardVideoAdSlot(
      slotId,
      expressSize,
      userId,
      rewardName,
      rewardAmount,
      isVertical,
      isSupportDeepLink,
      extra,
      downloadType
    )

    PangleAdManager.shared.loadRewardVideoAd(adSlot, activity, loadingType) {
      if (PangleLoadingType.preload_only == loadingType || PangleLoadingType.normal == loadingType) {
        result?.success(it)
      }
    }
  }

  private fun loadFullscreenVideoAdOnly(
    call: MethodCall, loadingType: PangleLoadingType, result: MethodChannel.Result? = null
  ) {
    val slotId = call.argument<String>("slotId")!!
    val orientationIndex = call.argument<Int>("orientation") ?: PangleOrientation.veritical.ordinal
    val orientation = PangleOrientation.values()[orientationIndex]
    val isSupportDeepLink = call.argument<Boolean>("isSupportDeepLink") ?: true
    val expressArgs = call.argument<Map<String, Double>>("expressSize") ?: mapOf()
    val w: Float = expressArgs.getValue("width").toFloat()
    val h: Float = expressArgs.getValue("height").toFloat()
    val expressSize = TTSizeF(w, h)
    val downloadType = call.argument<Int>("downloadType") ?: TTAdConstant.DOWNLOAD_TYPE_NO_POPUP
    val adSlot = PangleAdSlotManager.getFullScreenVideoAdSlot(
      slotId, expressSize, orientation, isSupportDeepLink, downloadType
    )

    PangleAdManager.shared.loadFullscreenVideoAd(adSlot, activity, loadingType) {
      if (PangleLoadingType.preload_only == loadingType || PangleLoadingType.normal == loadingType) {
        result?.success(it)
      }
    }
  }
}
