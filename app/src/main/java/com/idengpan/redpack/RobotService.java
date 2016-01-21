package com.idengpan.redpack;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
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

    /**
     * 亮屏解锁
     */
    private void b() {
        ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(268435466, "bright").acquire();
        ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).newKeyguardLock("unLock").disableKeyguard();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        /*mNodeInfo = event.getSource();
        if (mNodeInfo == null) {
            return;
        }*/

        System.out.println("onAccessibilityEvent：" + Integer.toHexString(event.getEventType()) +";" + event.getText().toString());

        if(event.getText().size() == 0){
            isNewPacket = true;
        }
        try {
            if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {//0x40
                System.out.println("通知事件");
                List<CharSequence> v5 = event.getText();
                if (v5.size() > 0) {
                    String s = v5.get(0).toString();
                    if (s.contains("微信红包")) {
                        b();
                        isNewPacket = true;
                        this.time = System.currentTimeMillis();
                        Notification notification = (Notification) event.getParcelableData();
                        PendingIntent intent = notification.contentIntent;
                        intent.send();
                    }
                }
            }

            //窗口内容改变
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {//0x800
                System.out.println("窗口内容改变事件");
                long interval = System.currentTimeMillis() - this.time;
                System.out.println("间隔时间："+interval);
                if(!isNewPacket){
                    return;
                }
                if(interval <= 1500){
                    this.time = System.currentTimeMillis();
                    return;
                }
                List<AccessibilityNodeInfo> localList = getRootInActiveWindow().findAccessibilityNodeInfosByText("领取红包");
                if (localList != null && localList.size() > 0) {
                    AccessibilityNodeInfo recentNode =  localList.get(localList.size() - 1);
                    if (recentNode.getParent() != null) {
                        this.time = System.currentTimeMillis();
                        this.isNewPacket = true;
                        waitTime();
                        recentNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }else{
                        recentNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }

            //当显示一个PopWIndow时触发
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {//0x20
                System.out.println("POP弹出事件");
                AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
                if (rootNodeInfo != null) {
                    List<AccessibilityNodeInfo> pops = rootNodeInfo.findAccessibilityNodeInfosByText("发了一个红包");
                    if(pops != null && pops.size() >0){
                        this.isNewPacket = false;
                        this.time = System.currentTimeMillis();
                        pops.get(pops.size()-1).getParent().getChild(3).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInterrupt() {
        System.out.println("服务onInterrupt");
    }


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        RobotService.isWork = true;
        //在设置中开启了辅助服务时回调
        System.out.println("服务onServiceConnected");
    }

    private void waitTime() {
        try {
            Thread.sleep(200L);
        } catch (InterruptedException localInterruptedException) {
            localInterruptedException.printStackTrace();
        }
    }

}
