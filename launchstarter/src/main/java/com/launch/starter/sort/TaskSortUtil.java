package com.launch.starter.sort;

import androidx.annotation.NonNull;

import com.launch.starter.task.Task;
import com.launch.starter.utils.LogUtils;
import com.launch.starter.utils.LaunchStarterUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 拓扑排序工具类
 */
public class TaskSortUtil {

    /**
     * 高优先级的Task
     */
    private static List<Task> sNewTasksHigh = new ArrayList<>();

    /**
     * 任务的有向无环图的拓扑排序
     *
     * @param originTasks
     * @param originTaskCls
     * @return
     */
    public static synchronized List<Task> getSortResult(List<Task> originTasks,
                                                        List<Class<? extends Task>> originTaskCls) {
        long makeTime = System.currentTimeMillis();

        Set<Integer> dependSet = new HashSet<>();
        Graph graph = new Graph(originTasks.size());
        for (int i = 0; i < originTasks.size(); i++) {
            Task task = originTasks.get(i);
            if (task.isSend() || task.dependsOn() == null || task.dependsOn().size() == 0) {
                continue;
            }

            // 遍历当前任务所依赖的所有任务，如果有依赖就有边
            for (Class dependedCls : task.dependsOn()) {
                int dependedIndex = getIndexOfTask(originTasks, originTaskCls, dependedCls);
                if (dependedIndex < 0) {
                    throw new IllegalStateException(task.getClass().getSimpleName() +
                            " depends on " + dependedCls.getSimpleName() + " can not be found in task list ");
                }
                dependSet.add(dependedIndex);
                // from dependedIndex to i ：被依赖的任务 --> 当前任务
                // 例：from InitUmengTask to InitBuglyTask  ==  InitUmengTask --> InitBuglyTask（InitBuglyTask dependsOn InitUmengTask）
                graph.addEdge(dependedIndex, i);
            }
        }

        // 拓扑排序
        List<Integer> topologicalSortList = graph.topologicalSort();

        // 获取 基于拓扑排序的结果干预顺序
        List<Task> newTasksAll = getResultTasks(originTasks, dependSet, topologicalSortList);

        LogUtils.i("起源任务顺序: ");
        // InitAMapTask--->InitStethoTask--->InitWeexTask--->InitBuglyTask--->InitFrescoTask--->InitJPushTask--->InitUmengTask--->GetDeviceIdTask
        LaunchStarterUtils.printSortTask(originTasks);

        // 依赖关系
        // InitBuglyTask dependsOn InitAMapTask,InitUmengTask
        // InitJPushTask dependsOn InitBuglyTask,GetDeviceIdTask
        // InitAMapTask--->InitUmengTask--->GetDeviceIdTask--->InitBuglyTask---> InitJPushTask

        LogUtils.i("拓扑排序的结果: ");
        // 0--->1--->2--->4--->6--->7--->3--->5
        // 入度为0，                                                              入度为1，                            入度为2
        // InitAMapTask--->InitStethoTask--->InitWeexTask--->InitFrescoTask--->  InitUmengTask--->GetDeviceIdTask---> InitBuglyTask--->InitJPushTask
        LaunchStarterUtils.printSortList(topologicalSortList);

        LogUtils.i("基于拓扑排序的结果干预顺序: ");
        // 被孩子依赖的，                                                         需要提升优先级的，  没有依赖的
        // InitAMapTask--->InitUmengTask--->GetDeviceIdTask--->InitBuglyTask---> InitStethoTask---> InitWeexTask--->InitFrescoTask---> InitJPushTask
        LaunchStarterUtils.printSortTask(newTasksAll);

        // 结束的顺序受以下影响： 当前任务在线程执行时间，等待任务结束时机
        // 例：InitUmengTask 结束后执行-> InitBuglyTask 结束后执行-> InitJPushTask

        LogUtils.i(" 任务的有向无环图的拓扑排序 消耗 makeTime：" + (System.currentTimeMillis() - makeTime));
        return newTasksAll;
    }

    /**
     * 获取 基于拓扑排序的结果干预顺序
     *
     * @param originTasks
     * @param dependSet
     * @param topologicalSortList
     * @return
     */
    @NonNull
    private static List<Task> getResultTasks(List<Task> originTasks,
                                             Set<Integer> dependSet, List<Integer> topologicalSortList) {
        List<Task> newTasksAll = new ArrayList<>(originTasks.size());
        // 被孩子依赖的
        List<Task> newTasksDepended = new ArrayList<>();
        // 需要提升优先级的（相对于没有依赖的先执行）
        List<Task> newTasksRunAsSoon = new ArrayList<>();
        // 没有依赖的
        List<Task> newTasksWithOutDepend = new ArrayList<>();

        for (int index : topologicalSortList) {
            if (dependSet.contains(index)) {
                newTasksDepended.add(originTasks.get(index));
            } else {
                Task task = originTasks.get(index);
                if (task.needRunAsSoon()) {
                    newTasksRunAsSoon.add(task);
                } else {
                    newTasksWithOutDepend.add(task);
                }
            }
        }

        // 顺序：被孩子依赖的 ---> 需要提升优先级的（相对于没有依赖的先执行）---> 没有依赖的
        sNewTasksHigh.addAll(newTasksDepended);
        sNewTasksHigh.addAll(newTasksRunAsSoon);
        newTasksAll.addAll(sNewTasksHigh);
        newTasksAll.addAll(newTasksWithOutDepend);
        return newTasksAll;
    }

    public static List<Task> getTasksHigh() {
        return sNewTasksHigh;
    }

    /**
     * 获取任务在任务列表中的index
     *
     * @param originTasks
     * @param clsOriginTasks
     * @param clsDepends
     * @return
     */
    private static int getIndexOfTask(List<Task> originTasks, List<Class<? extends Task>> clsOriginTasks, Class clsDepends) {
        int index = clsOriginTasks.indexOf(clsDepends);
        if (index >= 0) {
            return index;
        }

        // 仅仅是保护性代码
        final int size = originTasks.size();
        for (int i = 0; i < size; i++) {
            if (clsDepends.getSimpleName().equals(originTasks.get(i).getClass().getSimpleName())) {
                return i;
            }
        }
        return index;
    }
}
