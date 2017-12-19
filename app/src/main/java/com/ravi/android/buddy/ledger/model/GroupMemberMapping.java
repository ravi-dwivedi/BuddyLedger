package com.ravi.android.buddy.ledger.model;

/**
 * Created by ravi on 2/3/17.
 */

public class GroupMemberMapping {

    private long user_id;
    private String groupOperation;
    private String operation_date;
    private long group_id;

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public String getGroupOperation() {
        return groupOperation;
    }

    public void setGroupOperation(String groupOperation) {
        this.groupOperation = groupOperation;
    }

    public String getOperation_date() {
        return operation_date;
    }

    public void setOperation_date(String operation_date) {
        this.operation_date = operation_date;
    }

    public long getGroup_id() {
        return group_id;
    }

    public void setGroup_id(long group_id) {
        this.group_id = group_id;
    }
}
