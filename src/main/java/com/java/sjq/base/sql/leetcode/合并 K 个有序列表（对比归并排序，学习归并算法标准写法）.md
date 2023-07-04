# 23. 合并 K 个升序链表

https://leetcode.cn/problems/merge-k-sorted-lists/solutions/219756/he-bing-kge-pai-xu-lian-biao-by-leetcode-solutio-2/

给你一个链表数组，每个链表都已经按升序排列。

请你将所有链表合并到一个升序链表中，返回合并后的链表。

**示例 1：**

```
输入：lists = [[1,4,5],[1,3,4],[2,6]]
输出：[1,1,2,3,4,4,5,6]
解释：链表数组如下：
[
  1->4->5,
  1->3->4,
  2->6
]
将它们合并到一个有序链表中得到。
1->1->2->3->4->4->5->6

```

**示例 2：**

```
输入：lists = []
输出：[]

```

**示例 3：**

```
输入：lists = [[]]
输出：[]

```

**提示：**

-   `k == lists.length`
-   `0 <= k <= 10^4`
-   `0 <= lists[i].length <= 500`
-   `-10^4 <= lists[i][j] <= 10^4`
-   `lists[i]` 按 **升序** 排列
-   `lists[i].length` 的总和不超过 `10^4`

### 归并1

![image-20230412114527477](../../../../../../../../../../../AppData/Roaming/Typora/typora-user-images/image-20230412114527477.png)

```java
class Solution {
    public ListNode mergeKLists(ListNode[] lists) {
        return merge(lists, 0, lists.length - 1);
    }

    public ListNode merge(ListNode[] lists, int l, int r) {
        if (l == r) {
            return lists[l];
        }
        if (l > r) {
            return null;
        }
        int mid = (l + r) >> 1;
        return mergeTwoLists(merge(lists, l, mid), merge(lists, mid + 1, r));
    }

    public ListNode mergeTwoLists(ListNode a, ListNode b) {
        if (a == null || b == null) {
            return a != null ? a : b;
        }
        ListNode head = new ListNode(0);
        ListNode tail = head, aPtr = a, bPtr = b;
        while (aPtr != null && bPtr != null) {
            if (aPtr.val < bPtr.val) {
                tail.next = aPtr;
                aPtr = aPtr.next;
            } else {
                tail.next = bPtr;
                bPtr = bPtr.next;
            }
            tail = tail.next;
        }
        tail.next = (aPtr != null ? aPtr : bPtr);
        return head.next;
    }
}
```
### 归并2

```java

class Solution {
    public ListNode mergeKLists(ListNode[] lists) {
        return merge(lists, 0, lists.length - 1);
    }

    public ListNode merge(ListNode[] lists, int l, int r) {
        if (l == r) {
            return lists[l];
        }
        if (l > r) {
            return null;
        }
        int mid = (l + r) >> 1;
        ListNode  aPtr = merge(lists, l, mid);
        ListNode  bPtr = merge(lists, mid + 1, r);
        if( aPtr==null ||  bPtr==null){
            return  aPtr!=null?  aPtr:bPtr;
        }
        ListNode head = new ListNode(0);
        ListNode tail = head;
        while (aPtr != null && bPtr != null) {
            if (aPtr.val < bPtr.val) {
                tail.next = aPtr;
                aPtr = aPtr.next;
            } else {
                tail.next = bPtr;
                bPtr = bPtr.next;
            }
            tail = tail.next;
        }
        tail.next = (aPtr != null ? aPtr : bPtr);

        return head.next;
    }

    public ListNode mergeTwoLists(ListNode a, ListNode b) {
        if (a == null || b == null) {
            return a != null ? a : b;
        }
        ListNode head = new ListNode(0);
        ListNode tail = head, aPtr = a, bPtr = b;
        while (aPtr != null && bPtr != null) {
            if (aPtr.val < bPtr.val) {
                tail.next = aPtr;
                aPtr = aPtr.next;
            } else {
                tail.next = bPtr;
                bPtr = bPtr.next;
            }
            tail = tail.next;
        }
        tail.next = (aPtr != null ? aPtr : bPtr);
        return head.next;
    }
}

```

### 优先级队列3（堆）

![image-20230412114627844](../../../../../../../../../../../AppData/Roaming/Typora/typora-user-images/image-20230412114627844.png)

```java
 /**
  * Definition for singly-linked list.
  * public class ListNode {
  *     int val;
  *     ListNode next;
  *     ListNode() {}
  *     ListNode(int val) { this.val = val; }
  *     ListNode(int val, ListNode next) { this.val = val; this.next = next; }
  * }
  */
 class Solution {
     public ListNode mergeKLists(ListNode[] lists) {
         PriorityQueue<ListNode> q = new PriorityQueue<>((a,b)->{
             // val ∈[-10^4, 10^4]
             return a.val - b. val;
         });
         ListNode dummy = new ListNode();
         ListNode head = dummy;
         for(int i=0; i<lists.length; i++){
             if(lists[i]!=null) q.offer(lists[i]);
         }
         while(!q.isEmpty()){
             ListNode poll = q.poll();
             dummy.next = poll;
             dummy = poll;
             if(poll.next!=null) {
                 q.add(poll.next);
             }
         }
         return head.next;

     }
 }

```