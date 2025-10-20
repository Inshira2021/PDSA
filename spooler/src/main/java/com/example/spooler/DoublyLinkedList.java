package com.example.spooler;

import java.util.LinkedList;
import java.util.List;

public class DoublyLinkedList {

    private Node head; // head pointer
    private List<Integer> removedJobIds; // stores IDs of removed jobs

    DoublyLinkedList() {
        head = null; // empty list
        removedJobIds = new LinkedList<>();
    }

    public void addLast(int jobId, String documentName, String status) {
        Node newNode = new Node(jobId, documentName, status);
        if (head == null) {
            head = newNode;
        } else {
            Node last = head;
            while (last.next != null) {
                last = last.next;
            }
            last.next = newNode;
            newNode.prev = last;
        }
    }

    public boolean removeJob(int jobId) {
        Node current = findJob(jobId);
        if (current == null) {
            return false;
        }
        unlink(current);
        removedJobIds.add(jobId);
        return true;
    }

    public boolean updateStatus(int jobId, String newStatus) {
        Node current = findJob(jobId);
        if (current == null) {
            return false;
        }
        current.status = newStatus;
        return true;
    }

    public boolean moveOrder(int jobId, int newPosition) {
        Node current = findJob(jobId);
        if (current == null || newPosition < 1) {
            return false;
        }

        int total = size();
        if (newPosition > total) {
            newPosition = total; // clamp to end
        }
        unlink(current);

        if (newPosition == 1) {
            // move to head
            current.next = head;
            if (head != null) {
                head.prev = current;
            }
            head = current;
        } else {
            // insert at given position
            Node temp = head;
            for (int i = 1; i < newPosition - 1 && temp.next != null; i++) {
                temp = temp.next;
            }

            current.next = temp.next;
            if (temp.next != null) {
                temp.next.prev = current;
            }
            temp.next = current;
            current.prev = temp;
        }
        return true;
    }

    public Node findJob(int jobId) {
        Node current = head;
        while (current != null) {
            if (current.jobId == jobId) {
                return current; // found
            }
            current = current.next;
        }
        return null; // not found
    }

    public int size() {
        int count = 0;
        Node current = head;
        while (current != null) {
            count++;
            current = current.next;
        }
        return count;
    }

    private void unlink(Node node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        }

        node.next = null;
        node.prev = null;
    }

    public List<Integer> getRemovedJobIds() {
        return removedJobIds;
    }

    public void clearAll() {
        head = null;
    }

    public Node getHead() {
        return head;
    }

    // Helper method to add multiple jobs from a List<JobInfo>
    /*public void addAll(List<JobInfo> jobs) {
        if (jobs == null) return;
        for (JobInfo job : jobs) {
            addLast(job);
        }
    }

    public void printList() {
        Node current = head;
        while (current != null) {
            System.out.println("Job ID: " + current.jobId +
                    ", Doc: " + current.documentName +
                    ", Status: " + current.status);
            current = current.next;
        }
    }
    //doublyLinkedList.printList();

    public void printList() {
        Node currentNode = head;
        while (currentNode != null) {
            System.out.println(currentNode.data);
            currentNode = currentNode.next;
        }
    }

    public void reorderList(int oldPos, int newPos) {
        if (oldPos == newPos) return;

        Node nodeToMove = getNodeAt(oldPos);
        if (nodeToMove == null) return;

        // Detach node
        if (nodeToMove.prev != null) nodeToMove.prev.next = nodeToMove.next;
        else head = nodeToMove.next;

        if (nodeToMove.next != null) nodeToMove.next.prev = nodeToMove.prev;

        // Insert at new position
        if (newPos <= 1) {
            nodeToMove.next = head;
            if (head != null) head.prev = nodeToMove;
            nodeToMove.prev = null;
            head = nodeToMove;
            return;
        }

        Node current = head;
        int idx = 1;
        while (current.next != null && idx < newPos - 1) {
            current = current.next;
            idx++;
        }

        // Insert after 'current'
        nodeToMove.next = current.next;
        if (current.next != null) current.next.prev = nodeToMove;
        current.next = nodeToMove;
        nodeToMove.prev = current;
    }

    private Node getNodeAt(int position) {
        Node current = head;
        int index = 1;
        while (current != null && index < position) {
            current = current.next;
            index++;
        }
        return current;
    }

    public Node getHead() {
        return head;
    }*/
    //setStatusMessage("Print command sent!", "info");
}
