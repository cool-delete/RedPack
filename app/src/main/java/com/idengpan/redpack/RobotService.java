package com.idengpan.redpack;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class RobotService extends AccessibilityService {

    //整个窗口视图的快照
    private AccessibilityNodeInfo mNodeInfo = null;

    public static boolean isWork = false;
    private boolean isNewPacket = false;
    private long time = 0L;

    private void a() {
        try {
            Thread.sleep(1000L);
            Intent localIntent = new Intent(this, MainActivity.class);
            localIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            startActivity(localIntent);
            return;
        } catch (InterruptedException localInterruptedException) {
            localInterruptedException.printStackTrace();
        }
    }

    private void b() {
        ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(268435466, "bright").acquire();
        ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).newKeyguardLock("unLock").disableKeyguard();
    }

    private void processWeixin() {
        List localList = getRootInActiveWindow().findAccessibilityNodeInfosByText("领取红包");
        if (localList != null && localList.size() > 0) {
            AccessibilityNodeInfo parent = ((AccessibilityNodeInfo) localList.get(localList.size() - 1)).getParent();
            if (parent != null) {
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }


    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        mNodeInfo = event.getSource();
        if (mNodeInfo == null) {
            return;
        }

        int v8 = 0x1;
        try{
            if(event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED){//0x40
                List<CharSequence> v5 = event.getText();
                if(v5.size() > 0){
                    String s = v5.get(0).toString();
                    if(s.contains("微信红包")){
                        b();
                        isNewPacket = true;
                        this.time = System.currentTimeMillis();
                        Notification notification = (Notification)event.getParcelableData();
                        PendingIntent intent = notification.contentIntent;
                        intent.send();
                    }
                }
            }

            if(event.getEventType() ==  AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){//0x20
                AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
                if(rootNodeInfo != null){
                    process(rootNodeInfo);
                }
            }

            if(event.getEventType() == v8){

            }

        }catch(Exception e){
            e.printStackTrace();
        }
















        //窗口内容有变化，判断是否有红包
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {//2048
            List<AccessibilityNodeInfo> hbs = mNodeInfo.findAccessibilityNodeInfosByText("微信红包");

            if (hbs.size() > 0) {
                AccessibilityNodeInfo curNodeInfo = hbs.get(hbs.size() - 1);
                curNodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {//32
            List<AccessibilityNodeInfo> popWindowList = mNodeInfo.findAccessibilityNodeInfosByText("開");
            if (popWindowList.size() > 0) {
                AccessibilityNodeInfo accessibilityNodeInfo = popWindowList.get(popWindowList.size() - 1);
                accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }

    }


    public void process(AccessibilityNodeInfo paramAccessibilityNodeInfo)
    {
        try
        {
            if (!this.isNewPacket) {
                return;
            }
            if (System.currentTimeMillis() - this.time > 15000L) {
                this.isNewPacket = false;
            }
        }
        catch (Exception localException)
        {
            localException.printStackTrace();
        }
        processWeixin();
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

    private void waitTime() {
        try {
            Thread.sleep(200L);
            return;
        } catch (InterruptedException localInterruptedException) {
            localInterruptedException.printStackTrace();
        }
    }

}
