package com.jeevan.whatsapp.Data;

import java.util.ArrayList;

public class GroupData {

    private String groupTitle;
    private long groupCreatedDate;
    private String backgroundColor;
    private String groupId;
    private String groupCode;
    private String adminId;
    private ArrayList<String> members;

    public GroupData(){}

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

    public long getGroupCreatedDate() {
        return groupCreatedDate;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public void setGroupCreatedDate(long groupCreatedDate) {
        this.groupCreatedDate = groupCreatedDate;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }
}
