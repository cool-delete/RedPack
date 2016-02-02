package com.idengpan.redpack;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.*;


public class HongbaoService extends AccessibilityService {
    private AccessibilityNodeInfo mReceiveNode, mUnpackNode;

    private boolean mLuckyMoneyPicked, mLuckyMoneyReceived, mNeedUnpack, mNeedBack;

    private String lastContentDescription = "";
    private HongbaoSignature signature = new HongbaoSignature();

    private AccessibilityNodeInfo rootNodeInfo;

    private static final String WECHAT_DETAILS_EN = "Details";
    private static final String WECHAT_DETAILS_CH = "红包详情";
    private static final String WECHAT_BETTER_LUCK_EN = "Better luck next time!";
    private static final String WECHAT_BETTER_LUCK_CH = "手慢了";
    private static final String WECHAT_EXPIRES_CH = "红包已失效";
    private static final String WECHAT_VIEW_SELF_CH = "查看红包";
    private static final String WECHAT_VIEW_OTHERS_CH = "领取红包";
    private final static String WECHAT_NOTIFICATION_TIP = "[微信红包]";

    private boolean mMutex = false;

    public static Map<String, Boolean> watchedFlags = new HashMap<>();

    // -------------------------------2016年02月02日22:28:33------------------------------------
    //微信包名
    private static final String PACKAGE_MM = "com.tencent.mm";

    //支付宝包名
    private static final String PACKAGE_ALIPAY = "com.eg.android.AlipayGphone";

    private static final int MSG_NODE_CLICK = 0x110;

    //是否能够不停点击咻咻的开关
    private boolean isCanCyclingClick = false;


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == MSG_NODE_CLICK){
                AccessibilityNodeInfo btnNode = (AccessibilityNodeInfo) msg.obj;
                btnNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    };

    /**
     * AccessibilityEvent的回调方法
     *
     * @param event 事件
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if(event.getPackageName().equals(PACKAGE_MM)){
            if (watchedFlags == null) return;

        /* 检测通知消息 */
            if (!mMutex) {
                if (watchedFlags.get("pref_watch_notification") && watchNotifications(event)) return;
                if (watchedFlags.get("pref_watch_list") && watchList(event)) return;
            }

            if (!watchedFlags.get("pref_watch_chat")) return;

            this.rootNodeInfo = event.getSource();

            if (rootNodeInfo == null) return;

            mReceiveNode = null;
            mUnpackNode = null;

            checkNodeInfo();

        /* 如果已经接收到红包并且还没有戳开 */
            if (mLuckyMoneyReceived && !mLuckyMoneyPicked && (mReceiveNode != null)) {
                mMutex = true;

                AccessibilityNodeInfo cellNode = mReceiveNode;
                cellNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                mLuckyMoneyReceived = false;
                mLuckyMoneyPicked = true;
            }
        /* 如果戳开但还未领取 */
            if (mNeedUnpack && (mUnpackNode != null)) {
                AccessibilityNodeInfo cellNode = mUnpackNode;
                cellNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                mNeedUnpack = false;
            }


            if (mNeedBack) {
                performGlobalAction(GLOBAL_ACTION_BACK);
                mMutex = false;
                mNeedBack = false;
            }
        }else if(event.getPackageName().equals(PACKAGE_ALIPAY)){
            Log.d("idengpan","全局"+event.getEventType()+"," + event.getClassName());
            if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
                //进入咻咻界面
                if("com.alipay.android.wallet.newyear.activity.MonkeyYearActivity".equals(event.getClassName())){
                    System.out.println("支付宝自动咻咻:" + getEventName(event.getEventType()) + "," + event.getClassName());
                    isCanCyclingClick = true;
                    AccessibilityNodeInfo btn = getButtonInfo(getRootInActiveWindow());
                    if(btn != null){
                        dontStopClick(btn);
                        //wait(100);
                        //btn.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }else if("android.app.Dialog".equals(event.getClassName())){//弹窗是Dialog
                    Log.d("idengpan","弹窗了吧"+event.getClassName()+","+event.getSource().getClassName());
                    isCanCyclingClick = false;
                    wait(1500);
                    performGlobalAction(GLOBAL_ACTION_BACK);
                }else{
                    Log.d("idengpan","不知道是啥？"+event.getClassName());
                    isCanCyclingClick = true;
                    if(timer != null){
                        timer.cancel();
                        timer = null;
                    }
                }
                return;
            }
            if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
                Log.d("idengpan", "咻咻内容改变:" + event.getClassName());
                if(!isCanCyclingClick){
                    isCanCyclingClick = true;
                }
            }else{
                Log.d("idengpan",event.getEventType()+"," + event.getClassName());
            }

        }

    }

    private Timer timer;
    private void dontStopClick(final AccessibilityNodeInfo btn){
        if(timer == null){
            timer= new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(isCanCyclingClick){
                        Message m = mHandler.obtainMessage(MSG_NODE_CLICK, btn);
                        mHandler.sendMessage(m);
                    }
                }
            },100,100);
        }
    }

    //筛选出咻咻的button，进行不停的点击
    private AccessibilityNodeInfo getButtonInfo(AccessibilityNodeInfo parent){
        if(parent != null && parent.getChildCount() > 0){
            for(int i = 0 ;i < parent.getChildCount() ;i++){
                AccessibilityNodeInfo node = parent.getChild(i);
                if("android.widget.Button".equals(node.getClassName())){
                    return node;
                }
            }
        }
        return null;
    }

    private void wait(int microSeconds){
        try {
            Thread.sleep(microSeconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getEventName(int type){
        switch(type){
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                return "窗口内容改变";
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                return "状态改变";
        }
        return "";
    }

    private boolean watchList(AccessibilityEvent event) {
        // Not a message
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || event.getSource() == null)
            return false;

        List<AccessibilityNodeInfo> nodes = event.getSource().findAccessibilityNodeInfosByText(WECHAT_NOTIFICATION_TIP);
        if (!nodes.isEmpty()) {
            AccessibilityNodeInfo nodeToClick = nodes.get(0);
            CharSequence contentDescription = nodeToClick.getContentDescription();
            if (contentDescription != null && !lastContentDescription.equals(contentDescription)) {
                nodeToClick.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                lastContentDescription = contentDescription.toString();
                return true;
            }
        }
        return false;
    }

    private boolean watchNotifications(AccessibilityEvent event) {
        // Not a notification
        if (event.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) return false;

        // Not a hongbao
        String tip = event.getText().toString();
        if (!tip.contains(WECHAT_NOTIFICATION_TIP)) return true;

        Parcelable parcelable = event.getParcelableData();
        if (parcelable instanceof Notification) {
            Notification notification = (Notification) parcelable;
            try {
                ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(268435466, "bright").acquire();
                ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).newKeyguardLock("unLock").disableKeyguard();
                notification.contentIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void onInterrupt() {
        isCanCyclingClick = false;
        if(timer != null){
            timer.cancel();
            timer = null;
        }
        Log.d("idengpan", "服务onInterrupt...");
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * 检查节点信息
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkNodeInfo() {
        if (this.rootNodeInfo == null) return;

        /* 聊天会话窗口，遍历节点匹配“领取红包”和"查看红包" */
        List<AccessibilityNodeInfo> nodes1 = this.findAccessibilityNodeInfosByTexts(this.rootNodeInfo, new String[]{
                WECHAT_VIEW_OTHERS_CH, WECHAT_VIEW_SELF_CH});

        if (!nodes1.isEmpty()) {
            AccessibilityNodeInfo targetNode = nodes1.get(nodes1.size() - 1);
            if (this.signature.generateSignature(targetNode)) {
                mLuckyMoneyReceived = true;
                mReceiveNode = targetNode;
                Log.d("sig", this.signature.toString());
            }
            return;
        }

        /* 戳开红包，红包还没抢完，遍历节点匹配“拆红包” */
        AccessibilityNodeInfo node2 = (this.rootNodeInfo.getChildCount() > 3) ? this.rootNodeInfo.getChild(3) : null;
        if (node2 != null && node2.getClassName().equals("android.widget.Button")) {
            mUnpackNode = node2;
            mNeedUnpack = true;
            return;
        }

        /* 戳开红包，红包已被抢完，遍历节点匹配“红包详情”和“手慢了” */
        if (mLuckyMoneyPicked) {
            List<AccessibilityNodeInfo> nodes3 = this.findAccessibilityNodeInfosByTexts(this.rootNodeInfo, new String[]{
                    WECHAT_BETTER_LUCK_CH, WECHAT_DETAILS_CH,
                    WECHAT_BETTER_LUCK_EN, WECHAT_DETAILS_EN, WECHAT_EXPIRES_CH});
            if (!nodes3.isEmpty()) {
                mNeedBack = true;
                mLuckyMoneyPicked = false;
            }
        }
    }

    /**
     * 将节点对象的id和红包上的内容合并
     * 用于表示一个唯一的红包
     *
     * @param node 任意对象
     * @return 红包标识字符串
     */
    private String getHongbaoText(AccessibilityNodeInfo node) {
        /* 获取红包上的文本 */
        String content;
        try {
            AccessibilityNodeInfo i = node.getParent().getChild(0);
            content = i.getText().toString();
        } catch (NullPointerException npe) {
            return null;
        }

        return content;
    }

    /**
     * 批量化执行AccessibilityNodeInfo.findAccessibilityNodeInfosByText(text).
     * 由于这个操作影响性能,将所有需要匹配的文字一起处理,尽早返回
     *
     * @param nodeInfo 窗口根节点
     * @param texts    需要匹配的字符串们
     * @return 匹配到的节点数组
     */
    private List<AccessibilityNodeInfo> findAccessibilityNodeInfosByTexts(AccessibilityNodeInfo nodeInfo, String[] texts) {
        for (String text : texts) {
            if (text == null) continue;

            List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText(text);

            if (!nodes.isEmpty()) return nodes;
        }
        return new ArrayList<>();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d("idengpan","辅助服务onServiceConnected");
        watchFlagsFromPreference();
    }

    private void watchFlagsFromPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Boolean changedValue = sharedPreferences.getBoolean(key, true);
                watchedFlags.put(key, changedValue);
            }
        });

        List<String> flagsList = Arrays.asList("pref_watch_notification", "pref_watch_list", "pref_watch_chat");
        for (String flag : flagsList) {
            watchedFlags.put(flag, sharedPreferences.getBoolean(flag, true));//这里我默认改为true，都监听
        }
    }


    @Override
    public void onDestroy() {
        isCanCyclingClick = false;
        if(timer != null){
            timer.cancel();
            timer = null;
        }
        Log.d("idengpan", "服务onDestroy了...");
        super.onDestroy();
        //onCreate();
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
