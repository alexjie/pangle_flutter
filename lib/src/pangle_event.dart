import 'package:flutter/services.dart';

const channelName = 'nullptrx.github.io/pangle/adevent';

class PangleEvent {
  // 将MyRouterObserver改造为单例模式
  static PangleEvent? _instance;
  PangleEvent._internal() {
    this.init();
  }
  factory PangleEvent() => _getInstance();

  static _getInstance() {
    if (_instance == null) {
      _instance = PangleEvent._internal();
    }
    return _instance;
  }

  late EventChannel _eventChannel;
  final Map<String, Function(String?)> _eventMap = Map();

  init() {
    _eventChannel = EventChannel(channelName);
    _eventChannel.receiveBroadcastStream().listen((data) {
      if (data != null) {
        final type = data['type'] ?? null;
        final event = data['event'] ?? null;
        if (type != null && _eventMap.containsKey(type)) {
          _eventMap[type]!(event);
        }
      }
    });
  }

  addEvent(String type, Function(String?) callback) {
    _eventMap[type] = callback;
  }
}
