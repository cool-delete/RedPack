package com.idengpan.redpack;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class RobotService extends AccessibilityService {

    //整个窗口视图的快照
    private AccessibilityNodeInfo mNodeInfo = null;


    private void processWeixin(){
        List localList = getRootInActiveWindow().findAccessibilityNodeInfosByText("领取红包");
        if(localList != null && localList.size()>0){
            AccessibilityNodeInfo parent = ((AccessibilityNodeInfo)localList.get(localList.size()-1)).getParent();
            if(parent!=null){
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }


    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        mNodeInfo = event.getSource();
        if(mNodeInfo == null){
            return;
        }


        //窗口内容有变化，判断是否有红包
        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){//2048
            List<AccessibilityNodeInfo> hbs = mNodeInfo.findAccessibilityNodeInfosByText("微信红包");

            if(hbs.size() > 0){
                AccessibilityNodeInfo curNodeInfo = hbs.get(hbs.size() - 1);
                curNodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }

        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){//32
            List<AccessibilityNodeInfo> popWindowList = mNodeInfo.findAccessibilityNodeInfosByText("開");
            if(popWindowList.size() > 0){
                AccessibilityNodeInfo accessibilityNodeInfo = popWindowList.get(popWindowList.size()-1);
                accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }

    }


    @Override
    public void onInterrupt() {
         System.out.println("服务onInterrupt");
    }


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        //在设置中开启了辅助服务时回调
        System.out.println("服务onServiceConnected");
    }



}
