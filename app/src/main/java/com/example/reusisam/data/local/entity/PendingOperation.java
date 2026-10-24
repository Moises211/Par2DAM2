package com.example.reusisam.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "operaciones_pendientes")
public class PendingOperation {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String collection;
    private String documentId;
    private String operation;
    private String payload;

    public PendingOperation(String collection, String documentId, String operation, String payload) {
        this.collection = collection;
        this.documentId = documentId;
        this.operation = operation;
        this.payload = payload;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getCollection() { return collection; }
    public String getDocumentId() { return documentId; }
    public String getOperation() { return operation; }
    public String getPayload() { return payload; }
}
