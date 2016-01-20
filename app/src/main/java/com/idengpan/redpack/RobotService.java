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


            if (System.currentTimeMillis() - this.time > 15000L) {
                this.isNewPacket = false;
            }

            //窗口内容改变
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {//0x800
                System.out.println("窗口内容改变事件");
                long interval = System.currentTimeMillis() - this.time;
                System.out.println("间隔时间："+interval);
                if(interval <= 1500 || !isNewPacket){
                    return;
                }
                List<AccessibilityNodeInfo> localList = getRootInActiveWindow().findAccessibilityNodeInfosByText("领取红包");
                if (localList != null && localList.size() > 0) {
                    AccessibilityNodeInfo recentNode =  localList.get(localList.size() - 1);
                    if (recentNode.getParent() != null) {
                        this.time = System.currentTimeMillis();
                        recentNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    } /*else {
                        recentNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }*/
                    this.isNewPacket = true;
                }
            }

            if(!isNewPacket)
                return;
            //当显示一个PopWIndow时触发
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {//0x20
                System.out.println("POP弹出事件");
                AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
                if (rootNodeInfo != null) {
                    List<AccessibilityNodeInfo> pops = rootNodeInfo.findAccessibilityNodeInfosByText("发了一个红包");
                    if(pops != null && pops.size() >0){
                        pops.get(pops.size()-1).getParent().getChild(3).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        //this.time = System.currentTimeMillis();
                        this.isNewPacket = false;
                    }
                }
            }





        } catch (Exception e) {
            e.printStackTrace();
        }


        //窗口内容有变化，判断是否有红包
        /*if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {//2048
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
*/
    }


    public void process(AccessibilityNodeInfo paramAccessibilityNodeInfo) {
        try {
            if (!this.isNewPacket) {
                return;
            }
            if (System.currentTimeMillis() - this.time > 15000L) {
                this.isNewPacket = false;
            }
        } catch (Exception localException) {
            localException.printStackTrace();
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
