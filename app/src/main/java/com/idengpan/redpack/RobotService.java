package com.idengpan.redpack;

import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class RobotService extends AccessibilityService {

    //整个窗口视图的快照
    private AccessibilityNodeInfo mNodeInfo = null;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        mNodeInfo = event.getSource();
        if(mNodeInfo == null){
            return;
        }

        //窗口内容有变化，判断是否有红包
        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
            List<AccessibilityNodeInfo> hbs = mNodeInfo.findAccessibilityNodeInfosByText("");
            if(hbs.size() > 0){
                AccessibilityNodeInfo curNodeInfo = hbs.get(hbs.size() - 1);
                curNodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }


    }


    @Override
    public void onInterrupt() {

    }
}
