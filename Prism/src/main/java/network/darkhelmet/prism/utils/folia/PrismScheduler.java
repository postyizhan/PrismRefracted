package network.darkhelmet.prism.utils.folia;

import network.darkhelmet.prism.Prism;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class PrismScheduler {

    private static final Prism PLUGIN = Prism.getInstance();
    private static final boolean FOLIA = Prism.isFolia;
    
    private static Object getAsyncScheduler() {
        try {
            Method method = Server.class.getMethod("getAsyncScheduler");
            return method.invoke(Bukkit.getServer());
        } catch (Exception e) {
            return null;
        }
    }
    
    private static Object getRegionScheduler() {
        try {
            Method method = Server.class.getMethod("getRegionScheduler");
            return method.invoke(Bukkit.getServer());
        } catch (Exception e) {
            return null;
        }
    }
    
    private static Object getGlobalRegionScheduler() {
        try {
            Method method = Server.class.getMethod("getGlobalRegionScheduler");
            return method.invoke(Bukkit.getServer());
        } catch (Exception e) {
            return null;
        }
    }
    
    private static PrismTask runAtFixedRateAsync(Runnable runnable, long delay, long period) {
        try {
            Object scheduler = getAsyncScheduler();
            Method method = scheduler.getClass().getMethod("runAtFixedRate", Object.class, java.util.function.Consumer.class, 
                    long.class, long.class, TimeUnit.class);
            Object task = method.invoke(scheduler, PLUGIN, 
                    (java.util.function.Consumer<Object>) val -> runnable.run(), 
                    delay * 50, period * 50, TimeUnit.MILLISECONDS);
            return new PrismTask(task);
        } catch (Exception e) {
            Prism.warn("无法使用Folia异步调度器", e);
            return null;
        }
    }

    public static PrismTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period) {
        if (FOLIA) {
            PrismTask task = runAtFixedRateAsync(runnable, delay, period);
            if (task != null) {
                return task;
            }
            // 如果反射失败，则回退到Bukkit调度
            Prism.warn("Folia调度失败，回退到Bukkit调度");
        }
        return new PrismTask(Bukkit.getScheduler().runTaskTimerAsynchronously(PLUGIN, runnable, delay, period));
    }
    
    public static PrismTask runTaskLaterAsynchronously(Runnable runnable, long delay) {
        if (FOLIA) {
            try {
                Object scheduler = getAsyncScheduler();
                if (scheduler != null) {
                    Method method = scheduler.getClass().getMethod("runDelayed", Object.class, 
                            java.util.function.Consumer.class, long.class, TimeUnit.class);
                    Object task = method.invoke(scheduler, PLUGIN, 
                            (java.util.function.Consumer<Object>) val -> runnable.run(), 
                            delay * 50, TimeUnit.MILLISECONDS);
                    return new PrismTask(task);
                }
            } catch (Exception e) {
                // 失败时回退
            }
        }
        return new PrismTask(Bukkit.getScheduler().runTaskLaterAsynchronously(PLUGIN, runnable, delay));
    }

    public static PrismTask runTaskAsynchronously(Runnable runnable) {
        if (FOLIA) {
            try {
                Object scheduler = getAsyncScheduler();
                if (scheduler != null) {
                    Method method = scheduler.getClass().getMethod("runNow", Object.class, 
                            java.util.function.Consumer.class);
                    Object task = method.invoke(scheduler, PLUGIN, 
                            (java.util.function.Consumer<Object>) val -> runnable.run());
                    return new PrismTask(task);
                }
            } catch (Exception e) {
                // 失败时回退
            }
        }
        return new PrismTask(Bukkit.getScheduler().runTaskAsynchronously(PLUGIN, runnable));
    }

    public static void runTask(Runnable runnable) {
        if (FOLIA) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(PLUGIN, runnable);
        }
    }

    public static void run(Runnable runnable, Location location) {
        if (FOLIA) {
            try {
                Object scheduler = getRegionScheduler();
                if (scheduler != null) {
                    Method method = scheduler.getClass().getMethod("execute", Object.class, 
                            Location.class, Runnable.class);
                    method.invoke(scheduler, PLUGIN, location, runnable);
                    return;
                }
            } catch (Exception e) {
                // 失败时回退
            }
        }
        runnable.run();
    }

    public static void runTaskLater(Runnable runnable, Location location, long delay) {
        if (FOLIA) {
            try {
                Object scheduler = getRegionScheduler();
                if (scheduler != null) {
                    Method method = scheduler.getClass().getMethod("runDelayed", Object.class, 
                            Location.class, java.util.function.Consumer.class, long.class);
                    method.invoke(scheduler, PLUGIN, location, 
                            (java.util.function.Consumer<Object>) val -> runnable.run(), delay);
                    return;
                }
            } catch (Exception e) {
                // 失败时回退
            }
        }
        Bukkit.getScheduler().runTaskLater(PLUGIN, runnable, delay);
    }

    public static PrismTask scheduleSyncRepeatingTask(Runnable runnable, long delay, long period) {
        if (FOLIA) {
            try {
                Object scheduler = getGlobalRegionScheduler();
                if (scheduler != null) {
                    Method method = scheduler.getClass().getMethod("runAtFixedRate", Object.class, 
                            java.util.function.Consumer.class, long.class, long.class);
                    Object task = method.invoke(scheduler, PLUGIN, 
                            (java.util.function.Consumer<Object>) val -> runnable.run(), delay, period);
                    return new PrismTask(task);
                }
            } catch (Exception e) {
                // 失败时回退
            }
        }
        return new PrismTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(PLUGIN, runnable, delay, period));
    }

    public static PrismTask scheduleSyncRepeatingTask(Runnable runnable, Location location, long delay, long period) {
        if (FOLIA) {
            try {
                Object scheduler = getRegionScheduler();
                if (scheduler != null) {
                    Method method = scheduler.getClass().getMethod("runAtFixedRate", Object.class, 
                            Location.class, java.util.function.Consumer.class, long.class, long.class);
                    Object task = method.invoke(scheduler, PLUGIN, location, 
                            (java.util.function.Consumer<Object>) val -> runnable.run(), delay, period);
                    return new PrismTask(task);
                }
            } catch (Exception e) {
                // 失败时回退
            }
        }
        return new PrismTask(Bukkit.getScheduler().scheduleSyncRepeatingTask(PLUGIN, runnable, delay, period));
    }

    public static void cancelTasks() {
        if (FOLIA) {
            try {
                Object globalScheduler = getGlobalRegionScheduler();
                if (globalScheduler != null) {
                    Method method = globalScheduler.getClass().getMethod("cancelTasks", Object.class);
                    method.invoke(globalScheduler, PLUGIN);
                }
                
                Object asyncScheduler = getAsyncScheduler();
                if (asyncScheduler != null) {
                    Method method = asyncScheduler.getClass().getMethod("cancelTasks", Object.class);
                    method.invoke(asyncScheduler, PLUGIN);
                }
                return;
            } catch (Exception e) {
                // 失败时回退
            }
        }
        Bukkit.getScheduler().cancelTasks(PLUGIN);
    }
}
