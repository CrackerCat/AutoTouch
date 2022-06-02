package top.bogey.auto_touch.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.view.MotionEvent;

import top.bogey.auto_touch.MainActivity;
import top.bogey.auto_touch.MainApplication;
import top.bogey.auto_touch.utils.easy_float.EasyFloat;
import top.bogey.auto_touch.utils.easy_float.FloatCallback;

public class FloatBaseCallback implements FloatCallback {
    @Override
    public void onCreate(boolean succeed) {

    }

    @Override
    public void onShow(String tag) {
        EasyFloat.hideAll(tag);
        MainActivity activity = MainApplication.getActivity();
        if (activity != null){
            activity.moveTaskToBack(true);
        }
    }

    @Override
    public void onHide() {

    }

    @Override
    public void onDismiss() {
        if (!EasyFloat.showLast()){
            MainActivity activity = MainApplication.getActivity();
            if (activity != null){
                ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
                manager.moveTaskToFront(activity.getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);
            }
        }
    }

    @Override
    public void onTouch(MotionEvent event) {

    }

    @Override
    public void onDrag(MotionEvent event) {

    }

    @Override
    public void onDragEnd() {

    }
}
