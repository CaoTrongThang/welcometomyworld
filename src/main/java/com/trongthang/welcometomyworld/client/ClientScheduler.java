package com.trongthang.welcometomyworld.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ClientScheduler {
    private static final List<ClientTask> tasks = Collections.synchronizedList(new ArrayList<>());

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) return;

            // Create a copy to iterate while allowing concurrent modifications
            List<ClientTask> tasksCopy;
            synchronized (tasks) {
                tasksCopy = new ArrayList<>(tasks);
            }

            Iterator<ClientTask> iterator = tasksCopy.iterator();
            while (iterator.hasNext()) {
                ClientTask task = iterator.next();
                boolean shouldRemove = task.tick();

                if (shouldRemove) {
                    synchronized (tasks) {
                        tasks.remove(task);
                    }
                }
            }
        });
    }

    public static void schedule(Runnable task, int delayTicks) {
        synchronized (tasks) {
            tasks.add(new ClientTask(task, delayTicks));
        }
    }

    private static class ClientTask {
        private final Runnable task;
        private int remainingTicks;

        public ClientTask(Runnable task, int delayTicks) {
            this.task = task;
            this.remainingTicks = delayTicks;
        }

        public boolean tick() {
            if (--remainingTicks <= 0) {
                task.run();
                return true;
            }
            return false;
        }
    }
}
