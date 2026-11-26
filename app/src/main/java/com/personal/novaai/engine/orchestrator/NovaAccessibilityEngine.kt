package com.personal.novaai.engine.orchestrator

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.personal.novaai.domain.model.PlanStep
import com.personal.novaai.domain.model.StepStatus
import kotlinx.coroutines.*

class NovaAccessibilityEngine : AccessibilityService() {

    companion object {
        var instance: NovaAccessibilityEngine? = null
        private const val TAG = "NovaAccessibilityEngine"
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Nova Accessibility Service CONNECTED successfully!")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.cancel()
    }

    fun clickAt(x: Float, y: Float, onComplete: (() -> Unit)? = null) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 150)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                onComplete?.invoke()
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                onComplete?.invoke()
            }
        }, null)
    }

    fun clickByTextOrToggle(targetText: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val nodes = rootNode.findAccessibilityNodeInfosByText(targetText)
        for (node in nodes) {
            // Check self or ancestors
            var current: AccessibilityNodeInfo? = node
            while (current != null) {
                if (current.isClickable || current.isCheckable) {
                    current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
                current = current.parent
            }

            // Check sibling toggle switch (like hotspot toggle switch next to text)
            node.parent?.let { parent ->
                for (i in 0 until parent.childCount) {
                    val child = parent.getChild(i)
                    if (child != null && (child.isCheckable || child.className?.contains("Switch", true) == true || child.isClickable)) {
                        child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        return true
                    }
                }
            }

            // Fallback click at screen bounds
            val rect = Rect()
            node.getBoundsInScreen(rect)
            if (rect.width() > 0 && rect.height() > 0) {
                // Click slightly to the right to hit toggle switch if present
                val clickX = (rect.right + 120).coerceAtMost(resources.displayMetrics.widthPixels - 40)
                clickAt(clickX.toFloat(), rect.centerY().toFloat())
                return true
            }
        }
        return false
    }

    fun toggleSwitch(): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(rootNode)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node.isCheckable || node.className?.contains("Switch", true) == true) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return false
    }

    fun typeIntoActiveField(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(rootNode)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node.isEditable || node.className?.contains("EditText", true) == true) {
                val args = Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                }
                if (node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) {
                    return true
                }
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return false
    }

    fun clickSendButton(): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(rootNode)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            val desc = (node.contentDescription?.toString() ?: "").lowercase()
            val text = (node.text?.toString() ?: "").lowercase()
            val viewId = (node.viewIdResourceName?.toString() ?: "").lowercase()

            if (desc.contains("send") || text.contains("send") || viewId.contains("send")) {
                var current: AccessibilityNodeInfo? = node
                while (current != null) {
                    if (current.isClickable) {
                        current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        return true
                    }
                    current = current.parent
                }
                val rect = Rect()
                node.getBoundsInScreen(rect)
                if (rect.width() > 0 && rect.height() > 0) {
                    clickAt(rect.centerX().toFloat(), rect.centerY().toFloat())
                    return true
                }
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return false
    }
}
