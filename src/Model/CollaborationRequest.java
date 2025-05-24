package Model;

import java.sql.Timestamp;

public class CollaborationRequest {
    private int requestId;
    private int senderId;
    private int receiverId;
    private String otherUserName; // Either sender or receiver name depending on context
    private String status;
    private Timestamp createdAt;
    
    public CollaborationRequest(int requestId, int senderId, int receiverId, String otherUserName, String status, Timestamp createdAt) {
        this.requestId = requestId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.otherUserName = otherUserName;
        this.status = status;
        this.createdAt = createdAt;
    }
    
    public int getRequestId() {
        return requestId;
    }
    
    public int getSenderId() {
        return senderId;
    }
    
    public int getReceiverId() {
        return receiverId;
    }
    
    public String getOtherUserName() {
        return otherUserName;
    }
    
    public String getStatus() {
        return status;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public boolean isPending() {
        return status.equals("pending");
    }
    
    public boolean isAccepted() {
        return status.equals("accepted");
    }
    
    public boolean isDeclined() {
        return status.equals("declined");
    }
} 