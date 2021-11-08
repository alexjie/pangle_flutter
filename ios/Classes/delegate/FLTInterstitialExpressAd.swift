//
//  FLTInterstitialAd.swift
//  pangle_flutter
//
//  Created by Jerry on 2020/8/12.
//

import BUAdSDK
import Flutter

internal final class FLTInterstitialExpressAd: NSObject, BUNativeExpresInterstitialAdDelegate {
    typealias Success = () -> Void
    typealias Fail = (Error?) -> Void
    
    let success: Success?
    let fail: Fail?
    private var adClicked: Bool
    
    init(success: Success?, fail: Fail?) {
        self.success = success
        self.fail = fail
        self.adClicked = false
    }
    
    func nativeExpresInterstitialAdDidLoad(_ interstitialAd: BUNativeExpressInterstitialAd) {
        PangleEventStream.shared.emit(adType: "NativeExpressAd", adEvent: "onAdLoaded")
    }
    
    func nativeExpresInterstitialAdRenderSuccess(_ interstitialAd: BUNativeExpressInterstitialAd) {
        let vc = AppUtil.getVC()
        interstitialAd.show(fromRootViewController: vc)
        PangleEventStream.shared.emit(adType: "NativeExpressAd", adEvent: "onRenderSuccess")
    }
    
    func nativeExpresInterstitialAdWillVisible(_ interstitialAd: BUNativeExpressInterstitialAd) {
        PangleEventStream.shared.emit(adType: "NativeExpressAd", adEvent: "onShow")
    }
    
    func nativeExpresInterstitialAd(_ interstitialAd: BUNativeExpressInterstitialAd, didFailWithError error: Error?) {
        PangleEventStream.shared.emit(adType: "NativeExpressAd", adEvent: "onError")
        self.fail?(error)
    }
    
    func nativeExpresInterstitialAdRenderFail(_ interstitialAd: BUNativeExpressInterstitialAd, error: Error?) {
        PangleEventStream.shared.emit(adType: "NativeExpressAd", adEvent: "onRenderFail")
        self.fail?(error)
    }
    
    func nativeExpresInterstitialAdDidClose(_ interstitialAd: BUNativeExpressInterstitialAd) {
        PangleEventStream.shared.emit(adType: "NativeExpressAd", adEvent: self.adClicked ? "onAdClicked" : "onAdDismiss")
        self.success?()
    }
    
    internal func nativeExpresInterstitialAdDidClick(_ interstitialAd: BUNativeExpressInterstitialAd) {
//        PangleEventStream.shared.emit(adType: "NativeExpressAd", adEvent: "onAdClicked")
        self.adClicked = true
    }
    
}
