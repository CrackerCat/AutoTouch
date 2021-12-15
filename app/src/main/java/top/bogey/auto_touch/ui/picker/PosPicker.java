package top.bogey.auto_touch.ui.picker;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import top.bogey.auto_touch.databinding.FloatFragmentPickerBgBinding;
import top.bogey.auto_touch.room.bean.Pos;

public class PosPicker extends NodePicker{
    private final List<PosPickerView> posViews = new ArrayList<>();
    private final List<Pos> posList = new ArrayList<>();
    private boolean addEnabled = true;

    public PosPicker(@NonNull Context context, PickerCallback pickerCallback, List<Pos> poses) {
        super(context, null, pickerCallback);
        FloatFragmentPickerBgBinding binding = FloatFragmentPickerBgBinding.inflate(LayoutInflater.from(context));
        layout = binding.getRoot();
        binding.closeButton.setOnClickListener(v -> {
            addEnabled = false;
            calculatePosList();
            if (pickerCallback != null){
                pickerCallback.call(this);
            }
            dismiss();
        });
        floatCallback = new TouchCallback();
        if (poses != null){
            posList.addAll(poses);
        }
    }

    @Override
    public void show(int gravity, int x, int y) {
        super.show(gravity, x, y);
        for (Pos pos : posList) {
            addPosView(pos.getX(), pos.getY());
        }
    }

    private void calculatePosList(){
        posList.clear();
        for (PosPickerView posView : posViews) {
            int[] location = posView.getLocation();
            posList.add(new Pos(location[0], location[1]));
        }
    }

    public List<Pos> getPosList() {
        return posList;
    }

    private void refreshPosViews(){
        for (int i = 0; i < posViews.size(); i++) {
            posViews.get(i).setIndex(i + 1);
        }
    }

    private void addPosView(int x, int y){
        PosPickerView posPickerView = new PosPickerView(context, nodePicker -> {
            PosPickerView picker = (PosPickerView) nodePicker;
            posViews.remove(picker);
            refreshPosViews();
        });
        posViews.add(posPickerView);
        posPickerView.setIndex(posViews.size());
        posPickerView.show(Gravity.START | Gravity.TOP, x, y);
    }

    private class TouchCallback extends FloatPickerShowCallback{
        private boolean drag;
        private float lastX, lastY;

        @Override
        public void touchEvent(@NonNull View view, @NonNull MotionEvent motionEvent) {
            super.touchEvent(view, motionEvent);
            float rawX = motionEvent.getRawX();
            float rawY = motionEvent.getRawY();
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drag = false;
                    lastX = rawX;
                    lastY = rawY;
                case MotionEvent.ACTION_MOVE:
                    float dx = rawX - lastX;
                    float dy = rawY - lastY;
                    if (!drag && dx * dx + dy * dy < 81) break;
                    drag = true;
                    lastX = rawX;
                    lastY = rawY;
                case MotionEvent.ACTION_UP:
                    if (!drag){
                        layout.postDelayed(() -> {
                            if (addEnabled){
                                addPosView((int) rawX, (int) rawY);
                            }
                        }, 200);
                    }
            }
        }

        @Override
        public void dismiss() {
            super.dismiss();
            for (PosPickerView posView : posViews) {
                posView.dismiss();
            }
        }
    }
}
