//
//  FLTSplashExpressAd.swift
//  pangle_flutter
//
//  Created by nullptrX on 2020/8/16.
//

import BUAdSDK

internal final class FLTSplashExpressAd: NSObject, BUNativeExpressSplashViewDelegate {
    typealias Success = () -> Void
    typealias Fail = (Error?) -> Void
    
    let success: Success?
    let fail: Fail?
    
    init(success: Success?, fail: Fail?) {
        self.success = success
        self.fail = fail
    }
    
    func nativeExpressSplashViewDidClickSkip(_ splashAdView: BUNativeExpressSplashView) {
        splashAdView.removeFromSuperview()
        self.success?()
    }
    
    func nativeExpressSplashView(_ splashAdView: BUNativeExpressSplashView, didFailWithError error: Error?) {
        splashAdView.removeFromSuperview()
        self.fail?(error)
    }
    
    func nativeExpressSplashViewDidClose(_ splashAdView: BUNativeExpressSplashView) {
        splashAdView.removeFromSuperview()
        self.success?()
    }
    
    func nativeExpressSplashViewRenderFail(_ splashAdView: BUNativeExpressSplashView, error: Error?) {
        splashAdView.removeFromSuperview()
        self.fail?(error)
    }
    
    func nativeExpressSplashViewDidLoad(_ splashAdView: BUNativeExpressSplashView) {}
    
    func nativeExpressSplashViewRenderSuccess(_ splashAdView: BUNativeExpressSplashView) {}
    
    func nativeExpressSplashViewWillVisible(_ splashAdView: BUNativeExpressSplashView) {}
    
    func nativeExpressSplashViewDidClick(_ splashAdView: BUNativeExpressSplashView) {}
    
    func nativeExpressSplashViewCountdown(toZero splashAdView: BUNativeExpressSplashView) {}
    
    func nativeExpressSplashViewFinishPlayDidPlayFinish(_ splashView: BUNativeExpressSplashView, didFailWithError error: Error) {}
    
    func nativeExpressSplashViewDidCloseOtherController(_ splashView: BUNativeExpressSplashView, interactionType: BUInteractionType) {}
}
