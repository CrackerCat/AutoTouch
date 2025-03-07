package top.bogey.touch_tool.ui.picker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeNodeManager;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.bogey.touch_tool.MainAccessibilityService;
import top.bogey.touch_tool.MainApplication;
import top.bogey.touch_tool.database.bean.action.TextAction;
import top.bogey.touch_tool.databinding.FloatPickerWordBinding;
import top.bogey.touch_tool.utils.DisplayUtils;
import top.bogey.touch_tool.utils.FloatBaseCallback;

@SuppressLint("ViewConstructor")
public class WordPickerFloatView extends BasePickerFloatView {
    protected final FloatPickerWordBinding binding;
    private final TextAction textNode;

    private TreeNodeManager manager;

    private AccessibilityNodeInfo rootNode;
    private AccessibilityNodeInfo selectNode = null;

    private String selectKey = "";
    private String selectLevel = "";

    public WordPickerFloatView(@NonNull Context context, PickerCallback pickerCallback, TextAction textNode) {
        super(context, pickerCallback);
        this.textNode = textNode;

        binding = FloatPickerWordBinding.inflate(LayoutInflater.from(context), this, true);

        floatCallback = new WordPickerCallback();

        binding.saveButton.setOnClickListener(v -> {
            if (pickerCallback != null) {
                pickerCallback.onComplete(this);
            }
            dismiss();
        });

        binding.backButton.setOnClickListener(v -> dismiss());

        binding.titleText.setOnClickListener(v -> {
            CharSequence text = binding.titleText.getText();
            if (text != null && text.length() > 0) {
                if (text.toString().equals(selectKey)) {
                    binding.titleText.setText(selectLevel);
                } else {
                    binding.titleText.setText(selectKey);
                }
            } else {
                binding.titleText.setText(selectKey);
            }
        });

        binding.markBox.setOnClickListener(v -> showWordView(null, false));
    }

    public String getWord() {
        if (selectNode != null) {
            CharSequence text = binding.titleText.getText();
            if (text != null && text.length() > 0) return String.format("\"%s\"", text);
        }
        return "";
    }

    protected void markAll() {
        MainAccessibilityService service = MainApplication.getService();
        if (service != null) {
            if (manager == null) {
                manager = new TreeNodeManager();
                WordPickerTreeAdapter adapter = new WordPickerTreeAdapter(manager, this);
                binding.wordRecyclerView.setAdapter(adapter);
            }
            rootNode = service.getRootInActiveWindow();
            WordPickerTreeAdapter adapter = (WordPickerTreeAdapter) binding.wordRecyclerView.getAdapter();
            if (adapter != null) adapter.setRoot(rootNode);
        }
    }

    public void showWordView(AccessibilityNodeInfo nodeInfo, boolean selectTreeNode) {
        if (nodeInfo == null) {
            selectNode = null;
            selectKey = "";
            selectLevel = "";
            binding.markBox.setVisibility(INVISIBLE);
        } else {
            selectNode = nodeInfo;
            selectLevel = "lv/" + getNodeLevel(nodeInfo);
            selectKey = getNodeKey(nodeInfo);
            if (selectKey.isEmpty()) selectKey = selectLevel;

            binding.titleText.setText(selectKey);

            Rect rect = new Rect();
            nodeInfo.getBoundsInScreen(rect);
            int[] location = new int[2];
            getLocationOnScreen(location);
            ViewGroup.LayoutParams params = binding.markBox.getLayoutParams();
            params.width = Math.max(rect.width(), DisplayUtils.dp2px(getContext(), 30));
            params.height = Math.max(rect.height(), DisplayUtils.dp2px(getContext(), 40));
            binding.markBox.setLayoutParams(params);
            binding.markBox.setX(rect.left);
            binding.markBox.setY(rect.top - location[1]);
            binding.markBox.setVisibility(VISIBLE);

            WordPickerTreeAdapter adapter = (WordPickerTreeAdapter) binding.wordRecyclerView.getAdapter();
            if (selectTreeNode && adapter != null) {
                adapter.collapseAll();
                if (manager.size() > 0) {
                    TreeNode treeNode = manager.get(0);
                    TreeNode node = findTreeNode(treeNode, nodeInfo);
                    if (node != null) {
                        node.setSelected(true);
                        adapter.setSelectedNode(node);
                        TreeNode parent = node.getParent();
                        while (parent != null) {
                            TreeNode p = parent.getParent();
                            if (p != null) {
                                parent.setExpanded(true);
                                parent = p;
                            } else {
                                adapter.expandNode(parent);
                                parent = null;
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();

        if (event.getAction() == MotionEvent.ACTION_UP) {
            AccessibilityNodeInfo node = getNodeIn((int) x, (int) y);
            if (node != null) {
                showWordView(node, true);
            }
        }
        return true;
    }

    private TreeNode findTreeNode(TreeNode treeNode, Object value) {
        if (value.equals(treeNode.getValue())) return treeNode;
        for (TreeNode child : treeNode.getChildren()) {
            TreeNode node = findTreeNode(child, value);
            if (node != null) return node;
        }
        return null;
    }

    private String getNodeText(AccessibilityNodeInfo nodeInfo) {
        String name = "";
        if (nodeInfo != null) {
            String resourceName = nodeInfo.getViewIdResourceName();
            if (resourceName != null && !resourceName.isEmpty()) {
                name = resourceName;
            }
            CharSequence text = nodeInfo.getText();
            if (name.isEmpty() && text != null && text.length() > 0) {
                name = text.toString();
            }
        }
        return name;
    }

    private String getNodeKey(AccessibilityNodeInfo nodeInfo) {
        String key = getNodeText(nodeInfo);
        Pattern pattern = Pattern.compile(".+:(id/.+)");
        Matcher matcher = pattern.matcher(key);
        if (matcher.find() && matcher.group(1) != null) {
            key = matcher.group(1);
        }
        return key;
    }

    private String getNodeLevel(AccessibilityNodeInfo nodeInfo) {
        AccessibilityNodeInfo parent = nodeInfo.getParent();
        if (parent != null) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                AccessibilityNodeInfo child = parent.getChild(i);
                if (child != null && child.equals(nodeInfo)) {
                    String level = getNodeLevel(parent);
                    if (!level.isEmpty()) {
                        return level + "," + i;
                    } else {
                        return String.valueOf(i);
                    }
                }
            }
        }
        return "";
    }

    @Nullable
    private AccessibilityNodeInfo getNodeIn(int x, int y) {
        if (rootNode == null) return null;
        Map<Integer, AccessibilityNodeInfo> deepNodeInfo = new HashMap<>();
        findNodeIn(deepNodeInfo, 1, rootNode, x, y);
        int max = 0;
        AccessibilityNodeInfo node = null;
        for (Map.Entry<Integer, AccessibilityNodeInfo> entry : deepNodeInfo.entrySet()) {
            if (max == 0 || entry.getKey() > max) {
                max = entry.getKey();
                node = entry.getValue();
            }
        }
        return node;
    }

    private void findNodeIn(Map<Integer, AccessibilityNodeInfo> deepNodeInfo, int deep, @NonNull AccessibilityNodeInfo nodeInfo, int x, int y) {
        if (nodeInfo.getChildCount() == 0) return;
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo child = nodeInfo.getChild(i);
            if (child != null) {
                Rect rect = new Rect();
                child.getBoundsInScreen(rect);
                if (rect.contains(x, y)) {
                    deepNodeInfo.put(deep, child);
                    findNodeIn(deepNodeInfo, deep + 1, child, x, y);
                }
            }
        }
    }

    protected class WordPickerCallback extends FloatBaseCallback {
        @Override
        public void onCreate(boolean succeed) {
            super.onCreate(succeed);
            if (succeed) {
                markAll();
                if (textNode != null) showWordView(textNode.searchClickableNode(textNode.searchNodes(rootNode)), true);
            }
        }
    }

}
