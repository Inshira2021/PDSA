package com.example.spooler;

public class Node {
    int jobId;
    String documentName;
    String status;
    Node next;
    Node prev;

    public Node(int jobId, String documentName, String status) {
        this.jobId = jobId;
        this.documentName = documentName;
        this.status = status;
        this.next = null;
        this.prev = null;
    }
}
