package com.freeteam01.androidnewsgroupreader.ModelsDatabase;

public class SubscribedNewsgroup {
    private String id;
    private String userId;
    private String serverId;
    private String name;

    public SubscribedNewsgroup(String userId, String serverId, String name) {
        this.userId = userId;
        this.serverId = serverId;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubscribedNewsgroup that = (SubscribedNewsgroup) o;

        if (!serverId.equals(that.serverId)) return false;
        return name.equals(that.name);
    }


    @Override
    public String toString() {
        return "SubscribedNewsgroup{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", serverId='" + serverId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
