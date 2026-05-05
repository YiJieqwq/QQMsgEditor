//QQMsgEditor 2026
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import androidx.recyclerview.widget.RecyclerView;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;

static boolean fullModify = getBoolean("msg_edit_config", "fullModify", true);
static Map<Long, String> modifiedRecords = new HashMap<>();

addMenuItem("修改消息(本地)", "menuModifyMsgNTForce");
addItem("完整内存修改 开/关", "toggleFullModify");
addItem("撤销全部文本修改", "undoAllModifications");

void toggleFullModify(int chatType, String peerUin, String name) {
    fullModify = !fullModify;
    putBoolean("msg_edit_config", "fullModify", fullModify);
    qqToast(2, "完整篡改已" + (fullModify ? "开启" : "关闭") + "，重启脚本或重新加载生效");
}

void undoAllModifications(int chatType, String peerUin, String name) {
    if (modifiedRecords.isEmpty()) {
        qqToast(1, "没有可撤销的修改");
        return;
    }
    android.app.Activity act = getNowActivity();
    if (act == null) {
        qqToast(1, "无法获取当前界面");
        return;
    }
    int count = 0;
    for (Map.Entry<Long, String> entry : modifiedRecords.entrySet()) {
        if (updateDataItem(act, entry.getKey(), entry.getValue())) count++;
    }
    modifiedRecords.clear();
    qqToast(2, "已撤销 " + count + " 条修改");
}

void menuModifyMsgNTForce(Object mData) {
    final Object msgData = mData;
    final android.app.Activity act = getNowActivity();
    if (act == null) return;

    final String oldText = safeGetMsgText(msgData);
    final long elemId = extractElementId(msgData);

    new Handler(Looper.getMainLooper()).post(() -> {
        // 1. 构建底层圆角容器
        LinearLayout rootContainer = new LinearLayout(act);
        rootContainer.setOrientation(LinearLayout.VERTICAL);
        rootContainer.setPadding(65, 60, 65, 60);
        
        GradientDrawable rootBg = new GradientDrawable();
        rootBg.setShape(GradientDrawable.RECTANGLE);
        rootBg.setCornerRadius(45); // 更大的圆角，更现代
        rootBg.setColor(Color.WHITE);
        rootContainer.setBackground(rootBg);

        // 2. 标题栏
        TextView titleView = new TextView(act);
        titleView.setText("编辑消息内容");
        titleView.setTextSize(18);
        titleView.setTextColor(Color.parseColor("#1A1A1A"));
        titleView.getPaint().setFakeBoldText(true); // 标题加粗
        titleView.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 45);
        titleView.setLayoutParams(titleParams);
        rootContainer.addView(titleView);

        // 3. 输入框及灰色背景容器 (仿现代化输入框)
        LinearLayout editContainer = new LinearLayout(act);
        GradientDrawable editBg = new GradientDrawable();
        editBg.setShape(GradientDrawable.RECTANGLE);
        editBg.setCornerRadius(25);
        editBg.setColor(Color.parseColor("#F2F3F5")); // 柔和的浅灰色背景
        editContainer.setBackground(editBg);

        final EditText et = new EditText(act);
        et.setText(oldText);
        et.setHint("请输入修改内容...");
        et.setTextSize(15);
        et.setTextColor(Color.parseColor("#333333"));
        et.setHintTextColor(Color.parseColor("#A8A8A8"));
        et.setBackgroundColor(Color.TRANSPARENT); // 去除自带下划线
        et.setPadding(40, 35, 40, 35);
        et.setGravity(Gravity.TOP | Gravity.START);
        et.setMinLines(3);
        et.setMaxLines(6); // 限制最大高度，支持多行滑动
        
        editContainer.addView(et, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams ecParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ecParams.setMargins(0, 0, 0, 60);
        editContainer.setLayoutParams(ecParams);
        rootContainer.addView(editContainer);

        // 4. 底部按钮栏 (等宽分布)
        LinearLayout buttonBar = new LinearLayout(act);
        buttonBar.setOrientation(LinearLayout.HORIZONTAL);

        // 取消按钮 (使用 TextView 替代 Button，去除自带阴影和边距)
        TextView cancelBtn = new TextView(act);
        cancelBtn.setText("取消");
        cancelBtn.setTextSize(15);
        cancelBtn.setTextColor(Color.parseColor("#666666"));
        cancelBtn.setGravity(Gravity.CENTER);
        GradientDrawable cancelBg = new GradientDrawable();
        cancelBg.setShape(GradientDrawable.RECTANGLE);
        cancelBg.setCornerRadius(100); // 胶囊形状
        cancelBg.setColor(Color.parseColor("#F2F3F5"));
        cancelBtn.setBackground(cancelBg);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1); // 权重 1
        cancelParams.setMargins(0, 0, 20, 0);
        cancelBtn.setLayoutParams(cancelParams);
        cancelBtn.setPadding(0, 30, 0, 30);
        buttonBar.addView(cancelBtn);

        // 确定按钮
        TextView okBtn = new TextView(act);
        okBtn.setText("确定修改");
        okBtn.setTextSize(15);
        okBtn.setTextColor(Color.WHITE);
        okBtn.setGravity(Gravity.CENTER);
        okBtn.getPaint().setFakeBoldText(true);
        GradientDrawable okBg = new GradientDrawable();
        okBg.setShape(GradientDrawable.RECTANGLE);
        okBg.setCornerRadius(100); // 胶囊形状
        okBg.setColor(Color.parseColor("#0088FF")); // 经典的 QQ 蓝
        okBtn.setBackground(okBg);
        LinearLayout.LayoutParams okParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1); // 权重 1
        okParams.setMargins(20, 0, 0, 0);
        okBtn.setLayoutParams(okParams);
        okBtn.setPadding(0, 30, 0, 30);
        buttonBar.addView(okBtn);

        rootContainer.addView(buttonBar);

        // 5. 实例化自定义 Dialog
        android.app.Dialog dialog = new android.app.Dialog(act);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE); // 必须在 setContentView 前调用
        dialog.setContentView(rootContainer);

        // 6. 核心：修复系统默认黑边与窗口宽度
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            // 使用纯透明背景彻底干掉黑框
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            android.view.WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = (int) (act.getResources().getDisplayMetrics().widthPixels * 0.82); // 优雅的 82% 屏宽
            lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            lp.dimAmount = 0.5f; // 加上自然的背后半透明遮罩
            window.setAttributes(lp);
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        dialog.show();

        // 7. 绑定事件 (逻辑与之前完全一致)
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        okBtn.setOnClickListener(v -> {
            String newText = et.getText().toString();
            dialog.dismiss();

            if (fullModify) modifyMemoryFully(msgData, newText);
            else modifyMemorySurface(msgData, newText);

            boolean ok = updateDataItem(act, elemId, newText);
            if (ok) {
                modifiedRecords.put(elemId, oldText);
                qqToast(2, "篡改成功！");
            } else {
                qqToast(1, "未找到消息，请滑动或重试");
            }
        });
    });
}

// ---------- 核心逻辑方法（完全无修改，和原版一致） ----------
String safeGetMsgText(Object msgData) {
    if (msgData == null) return "";
    try {
        Field f = msgData.getClass().getField("msg");
        Object v = f.get(msgData);
        if (v != null) return v.toString();
    } catch (Exception e) { }
    try {
        Field dataField = msgData.getClass().getField("data");
        Object raw = dataField.get(msgData);
        if (raw != null) {
            Field elementsField = raw.getClass().getField("elements");
            List elements = (List) elementsField.get(raw);
            if (elements != null) {
                StringBuilder sb = new StringBuilder();
                for (Object el : elements) {
                    try {
                        Object textEl = el.getClass().getField("textElement").get(el);
                        if (textEl != null) {
                            Object c = textEl.getClass().getField("content").get(textEl);
                            if (c != null) sb.append(c.toString());
                        }
                    } catch (Exception ignored) { }
                }
                return sb.toString();
            }
        }
    } catch (Exception e) { }
    return "";
}

//版权所有 @CNYiJieqwq异界

long extractElementId(Object msgData) {
    try {
        Field dataField = msgData.getClass().getField("data");
        Object raw = dataField.get(msgData);
        if (raw == null) return -1;
        Field elementsField = raw.getClass().getField("elements");
        List elements = (List) elementsField.get(raw);
        if (elements != null && !elements.isEmpty()) {
            Object el = elements.get(0);
            Field idField = el.getClass().getField("elementId");
            return (Long) idField.get(el);
        }
    } catch (Exception e) {}
    return -1;
}

void modifyMemoryFully(Object msgData, String newText) {
    try {
        try { msgData.getClass().getField("msg").set(msgData, newText); } catch (Exception e) {}
        Field dataField = msgData.getClass().getField("data");
        Object raw = dataField.get(msgData);
        if (raw != null) {
            Field elementsField = raw.getClass().getField("elements");
            List elements = (List) elementsField.get(raw);
            if (elements != null) {
                for (Object el : elements) {
                    if (el == null) continue;
                    try {
                        Field textElField = el.getClass().getField("textElement");
                        Object textEl = textElField.get(el);
                        if (textEl != null) {
                            Field contentField = textEl.getClass().getField("content");
                            contentField.set(textEl, newText);
                        }
                        Field extField = el.getClass().getField("extBufForUI");
                        extField.set(el, null);
                    } catch (Exception ignored) {}
                }
            }
            try { raw.getClass().getField("msg").set(raw, newText); } catch (Exception e) {}
        }
    } catch (Exception e) {}
}

void modifyMemorySurface(Object msgData, String newText) {
    try {
        Field f = msgData.getClass().getField("msg");
        f.set(msgData, newText);
    } catch (Exception e) {}
}

boolean updateDataItem(android.app.Activity act, long elemId, String newText) {
    try {
        RecyclerView rv = findChatRecyclerView(act);
        if (rv == null) return false;
        RecyclerView.Adapter adapter = rv.getAdapter();
        if (adapter == null) return false;

        Field differField = adapter.getClass().getDeclaredField("m");
        differField.setAccessible(true);
        Object differ = differField.get(adapter);
        if (differ == null) return false;

        Field listField = differ.getClass().getDeclaredField("d");
        listField.setAccessible(true);
        List msgList = (List) listField.get(differ);
        if (msgList == null) return false;

        for (int i = 0; i < msgList.size(); i++) {
            Object item = msgList.get(i);
            if (item != null && containsElementId(item, elemId)) {
                setCharSequenceField(item, "f1", newText);
                setCharSequenceField(item, "m1", newText);
                adapter.notifyItemChanged(i);
                return true;
            }
        }
        return false;
    } catch (Exception e) {
        return false;
    }
}

void setCharSequenceField(Object obj, String fieldName, String newText) {
    try {
        Field f = obj.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(obj, newText);
    } catch (Exception e) {}
}

boolean containsElementId(Object item, long elemId) {
    Object record = getFieldValue(item, "e");
    if (record != null) {
        try {
            Field elementsField = record.getClass().getField("elements");
            List elements = (List) elementsField.get(record);
            if (elements != null) {
                for (Object el : elements) {
                    Field idField = el.getClass().getField("elementId");
                    if ((Long) idField.get(el) == elemId) return true;
                }
            }
        } catch (Exception e) {}
    }
    return false;
}

Object getFieldValue(Object obj, String fieldName) {
    Class clazz = obj.getClass();
    while (clazz != null) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception e) {}
        clazz = clazz.getSuperclass();
    }
    return null;
}

RecyclerView findChatRecyclerView(android.app.Activity act) {
    View decor = act.getWindow().getDecorView();
    String targetClass = "com.tencent.aio.part.root.panel.content.firstLevel.msglist.mvx.vb.ui.adapter.a";
    return findRecyclerViewByAdapterClass(decor, targetClass);
}

RecyclerView findRecyclerViewByAdapterClass(View view, String adapterClassName) {
    if (view == null) return null;
    if (view instanceof RecyclerView) {
        RecyclerView rv = (RecyclerView) view;
        RecyclerView.Adapter adapter = rv.getAdapter();
        if (adapter != null && adapter.getClass().getName().equals(adapterClassName)) {
            return rv;
        }
    }
    if (view instanceof ViewGroup) {
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            RecyclerView result = findRecyclerViewByAdapterClass(group.getChildAt(i), adapterClassName);
            if (result != null) return result;
        }
    }
    return null;
}
