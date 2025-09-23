package com.azure.dto;


public class TaskDependencyDTO {
    private int id;
    private int predecessorId;
    private int successorId;
    private String type;
    private int lagDays;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPredecessorId() {
        return predecessorId;
    }

    public void setPredecessorId(int predecessorId) {
        this.predecessorId = predecessorId;
    }

    public int getSuccessorId() {
        return successorId;
    }

    public void setSuccessorId(int successorId) {
        this.successorId = successorId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLagDays() {
        return lagDays;
    }

    public void setLagDays(int lagDays) {
        this.lagDays = lagDays;
    }
}
