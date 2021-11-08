
package io.github.nullptrx.pangleflutter

import io.flutter.plugin.common.EventChannel
import android.os.Handler

object PangleEventStream: EventChannel.StreamHandler {

    private var eventSink: EventChannel.EventSink? = null

    fun emit(adType: String?, adEvent: String?) {
        val data = mapOf("type" to adType, "event" to adEvent)
        eventSink?.success(data)
    }

    override fun onListen(arguments: Any?, events:  EventChannel.EventSink?) {
        if (events != null) {
            eventSink = events
        }
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
    }
}