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

    private int robbed = 0;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        mNodeInfo = event.getSource();
        if(mNodeInfo == null){
            return;
        }

        //窗口内容有变化，判断是否有红包
        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
            List<AccessibilityNodeInfo> hbs = mNodeInfo.findAccessibilityNodeInfosByText("微信红包");
            if(hbs.size() > 0 && hbs.size() > robbed){
                AccessibilityNodeInfo curNodeInfo = hbs.get(hbs.size() - 1);
                curNodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                robbed += 1;
            }
        }

        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            List<AccessibilityNodeInfo> popWindowList = mNodeInfo.findAccessibilityNodeInfosByText("拆红包");
            if(popWindowList.size() > 0){
                AccessibilityNodeInfo accessibilityNodeInfo = popWindowList.get(popWindowList.size()-1);
                accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }

    }


    @Override
    public void onInterrupt() {

    }
}
