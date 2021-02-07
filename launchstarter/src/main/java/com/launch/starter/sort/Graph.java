package com.launch.starter.sort;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

/**
 * 有向无环图的拓扑排序算法
 */
public class Graph {

    /**
     * 顶点数
     */
    private int mVerticesCount;
    /**
     * 被指向顶点表
     */
    private List<Integer>[] mPointedToVertexList;

    public Graph(int verticesCount) {
        this.mVerticesCount = verticesCount;
        mPointedToVertexList = new ArrayList[mVerticesCount];
        for (int i = 0; i < mVerticesCount; i++) {
            mPointedToVertexList[i] = new ArrayList<>();
        }
    }

    /**
     * 添加边
     *
     * from dependedIndex to i ：被依赖的任务 --> 当前任务
     * 例：from InitUmengTask to InitBuglyTask  ==  InitUmengTask --> InitBuglyTask（InitBuglyTask dependsOn InitUmengTask）
     *
     * @param from
     * @param to
     */
    public void addEdge(int from, int to) {
        mPointedToVertexList[from].add(to);
    }

    /**
     * 拓扑排序
     *
     * 对一个有向无环图进行拓扑排序，是将G中所有顶点排成一个线性序列，可以决定哪些子工程必须要先执行，哪些子工程要在某些工程执行后才可以执行。
     * 为了形象地反映出整个工程中各个子工程(任务)之间的先后关系，可用一个有向图来表示，图中的顶点代表子工程(任务)，
     * 图中的有向边代表活动的先后关系，即有向边的起点的活动是终点活动的前序活动，只有当起点活动完成之后，其终点活动才能进行。
     *
     * 拓扑排序的实现步骤：
     *  1.在有向图中选一个没有前驱，也就是入度为0，的顶点并且输出；
     *  2.从图中删除所有 该顶点 和 被它指向的顶点 的边；
     *  3.重复上述两步，直至所有顶点输出，或者当前图中不存在无前驱的顶点为止。后者代表我们的有向图是有环的，因此，也可以通过拓扑排序来判断一个图是否有环。
     */
    public Vector<Integer> topologicalSort() {
        int[] indegree = new int[mVerticesCount];
        // 初始化所有点的入度数量
        for (int i = 0; i < mVerticesCount; i++) {
            // InitUmengTask --> InitBuglyTask ：i 指的是 InitUmengTask ；mPointedToVertexList[i] 指的是 InitBuglyTask 等, 就是被指向的顶点集合
            ArrayList<Integer> ptvList = (ArrayList<Integer>) mPointedToVertexList[i];
            for (int node : ptvList) {
                // 被指向的顶点的入度数+1
                indegree[node]++;
            }
        }

        Queue<Integer> queue = new LinkedList<>();
        // 找出所有入度为0的顶点添加到 入度为0的队列
        for (int i = 0; i < mVerticesCount; i++) {
            if (indegree[i] == 0) {
                queue.add(i);
            }
        }
        int cnt = 0;
        Vector<Integer> topOrder = new Vector<>();
        while (!queue.isEmpty()) {
            // 从队列中弹出入度为0的顶点
            int p = queue.poll();
            topOrder.add(p);
            // 找到被 该入度为0的顶点 指向的顶点集合
            for (int node : mPointedToVertexList[p]) {
                // 把这个被指向的顶点的入度减1，如果入度变成了0，那么添加到 入度为0的队列 中
                if (--indegree[node] == 0) {
                    queue.add(node);
                }
            }
            cnt++;
        }

        // 检查是否有环，理论上拿出来的点的次数和点的数量应该一致，如果不一致，说明有环
        if (cnt != mVerticesCount) {
            throw new IllegalStateException("在图中存在一个循环");
        }
        return topOrder;
    }
}
