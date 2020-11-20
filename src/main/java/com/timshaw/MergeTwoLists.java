package com.timshaw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MergeTwoLists {
    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        List<Integer> newNodeList = new ArrayList<Integer>();
        while(l1.next != null){
            int val = l1.val;
            newNodeList.add(val);
            l1 = l1.next;
        }

        while(l2.next != null){
            int val = l2.val;
            newNodeList.add(val);
            l2 = l2.next;
        }

        Integer[] nodeArr = newNodeList.toArray(new Integer[newNodeList.size()]);
        Arrays.sort(nodeArr);

        ListNode firstNode = new ListNode(nodeArr[0]);
        for(int i = 1; i<nodeArr.length;i++){
            ListNode nextNode = new ListNode(nodeArr[i]);

        }
        return null;
    }

    public void setNext(int i,ListNode firstNode,ListNode nextNode){
        if(i == 0){
            firstNode.next = nextNode;
        }
    }
}

class ListNode {
      int val;
      ListNode next;
      ListNode() {}
      ListNode(int val) { this.val = val; }
      ListNode(int val, ListNode next) { this.val = val; this.next = next; }
  }
