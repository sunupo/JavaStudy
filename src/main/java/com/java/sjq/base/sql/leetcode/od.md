

```java
package com.java.sjq.dataStructure;


import java.util.*;
import java.util.stream.Collectors;

class Main2 {



    static class Node{
        int id;
        int dis;
        int queueNum;

        int time; // 花费的时间 赶路 + 排队
        int cost;
        int finalTime;  // 做了核酸的时间点
        boolean selected;

        public Node(int id, int dis, int queueNum) {
            this.id = id;
            this.dis = dis;
            this.queueNum = queueNum;
            this.cost = dis2Cost(dis);
        }
    }
    public static void main(String[] args){
        openMouse();

    }

    public static void openMouse(){
        Scanner scanner  = new Scanner(System.in);
        String[] line1 = scanner.nextLine().split(" ");
        String[] line2 = scanner.nextLine().split(" ");
        int h1,m1,h2,m2;
        h1 = Integer.parseInt(line1[0]);
        m1 = Integer.parseInt(line1[1]);
        h2 = Integer.parseInt(line2[0]);
        m2 = Integer.parseInt(line2[1]);
        int n = Integer.parseInt(scanner.nextLine());
        List<Node> nodes = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            String[] line =scanner.nextLine().split(" ");
            nodes.add(new Node(Integer.parseInt(line[0]),Integer.parseInt(line[1]),Integer.parseInt(line[2])));
        }
        for (Node node : nodes) {
            setNodesForFilter(node, h1,m1,h2,m2);
        }
        List<Node> collect = nodes.stream().filter((node) -> node.selected == true).collect(Collectors.toList());
        List<Node> collect1 = collect.stream().sorted(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                if (o1.time > o2.time) {
                    return 1;
                } else if (o1.time < o2.time) {
                    return -1;
                } else {
                    if (o1.cost > o2.cost) {
                        return 1;
                    } else if (o1.cost < o2.cost) {
                        return -1;
//                 
                    } else {
                        if (o1.id > o2.id) {
                            return 1;
                        } else if (o1.id < o2.id) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                }

            }
        }).collect(Collectors.toList());
        System.out.println(collect1.size());
        collect.forEach((node)->{
            System.out.println(node.id+" "+node.time+" "+node.cost);
        });
        collect1.forEach((node)->{
            System.out.println(node.id+" "+node.time+" "+node.cost);
        });
    }
    // 不下班 做完核酸的时间
    static int setNodesForFilter(Node node,int h1, int m1, int h2, int m2){
        int wayTime = dis2Time(node.dis); // 赶路时间。
        int queueTime = getFinalQueueNum(node, h1, m1,h2,m2 ); //排队时间
        int finalTime = getLeftTime(8, 0, h1, m1)+ wayTime+queueTime;
        if(finalTime < 720){
            node.selected = true;
        }
        node.time = wayTime + queueTime;
        return 0;
    };

    // 通过这个人到核酸检测点的额时间，计算这个点位的排队人数
    static int getFinalQueueNum(Node node, int h1, int m1, int h2, int m2){
        int  res = 0;
        int wayTime = dis2Time(node.dis); // 赶路时间。
        // todo 计算这段时间内 新增了多少排队的人。
        // 计算任意时间区间，新增的人数。
        // 1 计算开始时间在那个区间   左闭右开[ 8,10)
        // 2 计算结束时间在那个区间
        // 3
        int begin = changeTimeToInt(h1, m1);
        int end = begin + wayTime;
        int addPeople = getWayAddPeople(begin) - getWayAddPeople(end);

        return Math.max(addPeople + node.queueNum - wayTime*1, 0) ;
    }

    static int getWayAddPeople(int begin){
        int addPeople1=0;
        if(begin< 0){
            addPeople1 +=         3*120 + 2*60 + 120*10 + 240 + 120 *20;
        }
        else if(begin<120){
            addPeople1 += 3*(120-begin) + 2*60 + 120*10 + 240 + 120 *20;
        }else if(begin<240){
            addPeople1 +=           240-begin +  120*10 + 240 + 120 *20;
        } else if(begin<360){
            addPeople1 +=                (360-begin)*10 + 240 + 120 *20;
        } else if(begin < 600){
            addPeople1 +=                         (240-begin) + 120 *20;
        } else if(begin <720){
            addPeople1 +=                                 (120-begin) *20;
        }else {
            addPeople1 = Integer.MAX_VALUE;
        }
        return addPeople1;
    }

    static int changeTimeToInt(int h, int m){
        return getLeftTime(8, 0, h, m);
    }
    static int dis2Time(int dis){
        return 10*dis;
    }
    static int dis2Cost(int dis){
        return 10*dis;
    }

    static int getLeftTime(int h1, int m1, int h2, int m2){
        return 60*(h2-h1) + (m2-m1);
    }
}

/*
10 30
14 50
3
1 10 10
2 1 1
3 2 4
 */
```

