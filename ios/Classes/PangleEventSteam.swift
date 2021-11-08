//
//  PangleEventStream.swift
//  pangle_flutter
//
//  Created by Alex on 2021/11/06.
//
public class PangleEventStream: NSObject, FlutterStreamHandler {
    public static let shared = PangleEventStream()
    
    private var pangleEventSink: FlutterEventSink?
    
    override init() {
        pangleEventSink = nil
    }
    
    public func emit(adType: String?, adEvent: String?) {
        if (pangleEventSink != nil) {
            pangleEventSink!(["type": adType, "event": adEvent])
        }
    }
    
    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        pangleEventSink = events;
        return nil
    }
    
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        pangleEventSink = nil
        return nil
    }
    
}
