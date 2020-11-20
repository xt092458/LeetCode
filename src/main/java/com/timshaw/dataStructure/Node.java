package com.timshaw.dataStructure;

import java.util.LinkedList;
import java.util.List;

public class Node<T> {

    public T item;

    public Node next;

    public Node(T item,Node next){
        this.item = item;
        this.next = next;
    }

    public static void main(String[] args){
        Node one = new Node<Integer>(1,null);
        Node two = new Node<Integer>(2,null);
        Node three = new Node<Integer>(3,null);
        Node four = new Node<Integer>(4,null);
        Node five = new Node<String>("a",null);
        one.next=two;
        two.next=three;
        three.next=four;
        four.next=five;

        List linkList = new LinkedList<String>();
    }
}
