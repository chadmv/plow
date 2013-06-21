package com.breakersoft.plow;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;


public final class TaskOnTaskBatch {

    public Job dependentJob;
    public Layer dependentLayer;
    public Job dependOnJob;
    public Layer dependOnLayer;

    public final List<TaskOnTaskBatchEntry> entries;

    public TaskOnTaskBatch(int size) {
        entries = Lists.newArrayListWithCapacity(size);
    }

    public void addEntry(UUID dependentTask, int dependentTaskNumber, UUID[] onIds, int[] onNumbers) {
        entries.add(new TaskOnTaskBatchEntry(dependentTask, dependentTaskNumber, onIds, onNumbers));
    }

    public int size() {
        return entries.size();
    }

    public static class TaskOnTaskBatchEntry {
        public UUID dependentTask;
        public int dependentTaskNumber;
        public UUID[] dependOnTasksIds;
        public int[] dependOnTaskNumbers;

        public TaskOnTaskBatchEntry(UUID dependentTask, int dependentTaskNumber, UUID[] dependOnTasksIds, int[] dependOnTaskNumbers) {
            this.dependentTask = dependentTask;
            this.dependentTaskNumber = dependentTaskNumber;
            this.dependOnTasksIds = dependOnTasksIds;
            this.dependOnTaskNumbers = dependOnTaskNumbers;
        }
    }
}


