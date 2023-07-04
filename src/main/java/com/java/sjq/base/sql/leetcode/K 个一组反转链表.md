

## 方法一:

相比于方法二两点在于:` head.next = fun1(recordNode, size);`递归结束返回的节点直接挂载在 `head.next` 了 



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
    public ListNode reverseKGroup(ListNode head, int k) {
        return fun1(head, k);

    }

     public ListNode fun1(ListNode head, int size){
        ListNode curNode=head;
        int  k = size;
        ListNode recordNode;
        ListNode newHead;

        while(curNode.next!=null && k>1){
            curNode = curNode.next;
            k--;
        }
        if(k>1){
            // 不翻转
            return head;
        } else {
            // 保存最后一个节点的下一个节点
            recordNode = curNode.next;
            // 断开连接
            curNode.next = null;
            // 翻转
            newHead= reverse(head);
        }
        if(recordNode!=null){
            head.next = fun1(recordNode, size);
        }
        return newHead;
    }

    public ListNode reverse(ListNode root){
        if(root==null){
            return root;
        }
        ListNode dummy = null;
        ListNode tmp = root.next;
        while(tmp!=null){
            root.next = dummy;
            dummy = root;
            root = tmp;
            tmp = tmp.next;
        }
        root.next = dummy;
        return root;
    }
}
```

## 方法二

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
    public ListNode reverseKGroup(ListNode head, int k) {
        ListNode dummyHead= new ListNode(-1, head);
        return fun(head, k, dummyHead);

    }


    public ListNode fun(ListNode head, int size, ListNode dummyHead){
        ListNode curNode=head;
        int  k = size;
        ListNode recordNode;
        ListNode newHead;

        while(curNode.next!=null && k>1){
            curNode = curNode.next;
            k--;
        }
        if(k>1){
            // 不翻转
            return head;
        } else {
            // 保存最后一个节点的下一个节点
            recordNode = curNode.next;
            // 断开连接
            curNode.next = null;
            // 翻转
            newHead= reverse(head);
        }
        dummyHead.next = newHead;
        head.next = recordNode;
        if(recordNode!=null){
            fun(recordNode, size, head);
        }
        return newHead;
    }

    public ListNode reverse(ListNode root){
        if(root==null){
            return root;
        }
        ListNode dummy = null;
        ListNode tmp = root.next;
        while(tmp!=null){
            root.next = dummy;
            dummy = root;
            root = tmp;
            tmp = tmp.next;
        }
        root.next = dummy;
        return root;
    }
}
```

