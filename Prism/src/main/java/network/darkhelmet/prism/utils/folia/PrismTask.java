package network.darkhelmet.prism.utils.folia;

import network.darkhelmet.prism.Prism;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class PrismTask {

    private static final boolean FOLIA = Prism.isFolia;

    private final Object task;
    private final int taskId;

    /**
     * 创建一个Folia任务
     * @param task 调度任务
     */
    public PrismTask(Object foliaTask) {
        this.task = foliaTask;
        this.taskId = -1;
    }

    public PrismTask(BukkitTask task) {
        this.task = task;
        this.taskId = task.getTaskId();
    }

    public PrismTask(int taskId) {
        this.task = null;
        this.taskId = taskId;
    }

    public void cancel() {
        if (FOLIA) {
            try {
                // 反射调用cancel方法
                task.getClass().getMethod("cancel").invoke(task);
            } catch (Exception e) {
                Prism.warn("取消Folia任务失败", e);
            }
        } else {
            if (task != null) {
                ((BukkitTask) task).cancel();
            } else {
                Bukkit.getScheduler().cancelTask(taskId);
            }
        }
    }

    public boolean isCancelled() {
        if (task == null) {
            throw new IllegalStateException("Task is created by id");
        }
        if (FOLIA) {
            try {
                // 反射调用isCancelled方法
                return (boolean) task.getClass().getMethod("isCancelled").invoke(task);
            } catch (Exception e) {
                Prism.warn("检查Folia任务状态失败", e);
                return false;
            }
        } else {
            return ((BukkitTask) task).isCancelled();
        }
    }

    public boolean isActive() {
        if (task == null) {
            throw new IllegalStateException("Task is created by id");
        }
        if (FOLIA) {
            try {
                // 反射调用getExecutionState方法
                Object state = task.getClass().getMethod("getExecutionState").invoke(task);
                // 检查状态不是FINISHED和CANCELLED
                return !state.toString().equals("FINISHED") && !state.toString().equals("CANCELLED");
            } catch (Exception e) {
                Prism.warn("检查Folia任务活动状态失败", e);
                return false;
            }
        } else {
            final int taskId = ((BukkitTask) task).getTaskId();
            final BukkitScheduler scheduler = Bukkit.getScheduler();
            return scheduler.isCurrentlyRunning(taskId) || scheduler.isQueued(taskId);
        }
    }
}
