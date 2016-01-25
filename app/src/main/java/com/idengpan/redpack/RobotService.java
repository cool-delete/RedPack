package com.idengpan.redpack;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * 从微信主界面进入聊天界面以及从聊天界面返回到主界面都是<b></>TYPE_WINDOW_CONTENT_CHANGED</b></>事件。
 *
 */
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

        System.out.println("onAccessibilityEvent：" + Integer.toHexString(event.getEventType()) + ";" + event.getText().toString());

        if(event.getText().size() == 0){
            //isNewPacket = true;
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
                System.out.println("Window Content Change事件");
                //long interval = System.currentTimeMillis() - this.time;
                //System.out.println("间隔时间："+interval);

                /*if(interval <= 1500){
                    this.time = System.currentTimeMillis();
                    return;
                }*/

                if(isCanBack()){
                    isNewPacket = false;
                    performGlobalAction(GLOBAL_ACTION_BACK);
                }

                if(!isNewPacket){
                    return;
                }

                List<AccessibilityNodeInfo> localList = getRootInActiveWindow().findAccessibilityNodeInfosByText("领取红包");
                if (localList != null && localList.size() > 0) {
                    AccessibilityNodeInfo recentNode = localList.get(localList.size() - 1);
                    if (recentNode.getParent() != null) {
                        this.time = System.currentTimeMillis();
                        this.isNewPacket = true;
                        waitTime();//这里等待200ms，为了进入界面联网加载新的红包，否则抢的是旧的，容易失误
                        recentNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        System.out.println("Window Content Change点击事件");
                    }else{
                        recentNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }

            //当显示一个PopWIndow时触发   || 或者进入微信聊天列表
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {//0x20
                System.out.println("WINDOW_STATE_CHANGED事件");
                AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
                if (rootNodeInfo != null) {
                    List<AccessibilityNodeInfo> pops = rootNodeInfo.findAccessibilityNodeInfosByText("发了一个红包");
                    if(pops != null && pops.size() >0){
                        this.isNewPacket = false;
                        this.time = System.currentTimeMillis();
                        pops.get(pops.size()-1).getParent().getChild(3).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        System.out.println("WINDOW_STATE_CHANGED点击");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private final static int ENVELOPE_RETURN = 0x12;
    /**
     * @param info 当前节点
     * @param matchFlag 需要匹配的文字
     * @param type  操作的类型
     */
    public void recycle(AccessibilityNodeInfo info, String matchFlag, int type) {
        if (info != null) {
            if (info.getChildCount() == 0) {
                CharSequence desrc = info.getContentDescription();
                switch (type) {
                    case ENVELOPE_RETURN://返回
                        if (desrc != null && matchFlag.equals(info.getContentDescription().toString().trim())) {
                            if (info.isCheckable()) {
                                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            } else {
                                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                            }
                        }
                        break;
                }
            } else {
                int size = info.getChildCount();
                for (int i = 0; i < size ; i++){
                    AccessibilityNodeInfo childInfo = info.getChild(i);
                    if (childInfo != null) {
                        Log.e("idengpan", "index: " + i + " info" + childInfo.getClassName() + " : " + childInfo.getContentDescription()+" : "+info.getText());
                        recycle(childInfo, matchFlag, type);
                }
                }

            }
        }

    }



    //是否抢完红包了可以返回？
    private boolean isCanBack(){
        List<AccessibilityNodeInfo> localList = getRootInActiveWindow().findAccessibilityNodeInfosByText("查看我的红包记录");
        return localList != null && localList.size() > 0;
    }

    /*public boolean performGlobalAction(int action) {
        IAccessibilityServiceConnection connection =
                AccessibilityInteractionClient.getInstance().getConnection(mConnectionId);
        if (connection != null) {
            try {
                return connection.performGlobalAction(action);
            } catch (RemoteException re) {
                Log.w("idengpan", "不能执行Global Action", re);
            }
        }
        return false;
    }*/


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
