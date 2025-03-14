package top.bogey.touch_tool.database.bean.action;

import android.content.Context;
import android.os.Parcel;

import androidx.annotation.NonNull;

import top.bogey.touch_tool.MainAccessibilityService;
import top.bogey.touch_tool.R;
import top.bogey.touch_tool.database.bean.Behavior;
import top.bogey.touch_tool.database.bean.Task;
import top.bogey.touch_tool.database.data.TaskRunnable;
import top.bogey.touch_tool.database.data.TaskRunningInfo;

public class TaskAction extends Action {
    private String id;
    private transient String title;

    public TaskAction() {
        super(ActionType.TASK);
    }

    public TaskAction(String id, String title) {
        super(ActionType.TASK);
        this.id = id;
        this.title = title;
    }

    protected TaskAction(Parcel in) {
        super(ActionType.TASK);
        id = in.readString();
    }

    @Override
    public boolean isValid() {
        return id != null;
    }

    @Override
    public boolean checkCondition(MainAccessibilityService service) {
        return false;
    }

    @Override
    public boolean doAction(Task task, MainAccessibilityService service, TaskRunningInfo runningInfo) {
        if (!super.doAction(task, service, runningInfo)) return false;
        Task subTask = task.getSubTaskById(id);
        if (subTask == null) return false;
        return TaskRunnable.runTask(task, subTask, service, runningInfo);
    }

    @Override
    public String getDescription(Context context, Task task, Behavior behavior) {
        Task subTask = task.getSubTaskById(id);
        if (subTask == null) return "";
        if (context == null) return subTask.getTitle();
        return context.getString(R.string.action_task, subTask.getTitle());
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @NonNull
    @Override
    public String toString() {
        return title;
    }
}
