package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WhatsappRepository {

    private Map<String,User> userMap = new HashMap<>();
    private Map<Integer,Message> messageMap = new HashMap<>();
    private Integer customGroupCount;
    private Integer messageId;
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;


    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>(); // map for messages in different groups
        this.groupUserMap = new HashMap<Group, List<User>>(); // map for users in different groups
        this.senderMap = new HashMap<Message, User>(); // map for message to user mapping
        this.adminMap = new HashMap<Group, User>(); // map for group to admin mapping
//        this.userMobile = new HashSet<>();
        this.userMap = new HashMap<String, User>(); // map for mobile number to user mapping
        this.messageMap = new HashMap<Integer, Message>(); // map for id to message mapping
        this.customGroupCount = 0; // count of groups
        this.messageId = 0; // incremental message ID
    }

    public Integer getCustomGroupCount(){
        return this.customGroupCount;
    }

    public void setCustomGroupCount(int customGroupCount) {
        this.customGroupCount = customGroupCount;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }



    public Optional<User> getUser(String mobile) {

        if(userMap.containsKey(mobile)) return Optional.of(userMap.get(mobile));
        return Optional.empty();
    }

    public void adduser(User user) {

        userMap.put(user.getMobile(), user);
        return;
    }

    public Integer createMessage(String content) {

        messageId++;
        Message message = new Message(messageId,content);
        messageMap.put(messageId,message);

        return messageId;
    }

    public Group createNewChat(List<User> users) {

        Group chat = new Group(users.get(1).getName(),2);
        this.groupUserMap.put(chat,users);
        return chat;
    }

    public Group createGroup(List<User> users) {

        this.customGroupCount++;
        String nameGroup = "Group " + this.customGroupCount;

        Group newGroup = new Group(nameGroup,users.size());
        this.groupUserMap.put(newGroup,users);
        this.adminMap.put(newGroup,users.get(0));

        return newGroup;
    }


    public Optional<List<User>> getUsersfromGroup(Group group) {

        if(this.groupUserMap.containsKey(group))
            return Optional.of(this.groupUserMap.get(group));

        return Optional.empty();
    }

    public Integer sendMessage(User sender, Message message, Group group) {

        List<Message> oldMessages = new ArrayList<>();
        if(this.groupMessageMap.containsKey(group))
            oldMessages = this.groupMessageMap.get(group);

        oldMessages.add(message);
        this.groupMessageMap.put(group,oldMessages);
        this.senderMap.put(message,sender);

        return oldMessages.size();
    }

    public User getAdmin(Group group) {

        return this.adminMap.get(group);
    }

    public void changeAdmin(User user, Group group) {

        this.adminMap.put(group,user);
        return;
    }

    public Optional<Group> findUserGroup(User user) {

        for (Group group : groupUserMap.keySet()){

            List<User> users = groupUserMap.get(group);
            if(users.contains(user)) return Optional.of(group);
        }

        return Optional.empty();
    }

    public Integer removeUser(User user, Group group) {

        List<User> groupUsers = groupUserMap.get(group);
        groupUsers.remove(user);
        groupUserMap.put(group,groupUsers);

        List<Message> msgsToDelete = getMessagesToDelete(user);

        deleteFromMessageMap(msgsToDelete);

        deleteFromGroupMessageMap(msgsToDelete,group);
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)

        return groupUsers.size() + groupMessageMap.get(group).size() + this.messageId;
    }

    private void deleteFromGroupMessageMap(List<Message> msgsToDelete, Group group) {

        List<Message> allMsgs = groupMessageMap.get(group);

        for(Message msg : msgsToDelete){

            if(allMsgs.contains(msg)){
                allMsgs.remove(msg);
            }
        }

        groupMessageMap.put(group,allMsgs);
        return;
    }

    private void deleteFromMessageMap(List<Message> msgsToDelete) {

        for(Message msg : msgsToDelete){
            if(messageMap.containsKey(msg.getId()))
                messageMap.remove(msg.getId());
        }
        return;
    }

    private List<Message> getMessagesToDelete(User user) {

        List<Message> msgs = new ArrayList<>();

        for(Message msg : senderMap.keySet()){

            if(senderMap.get(msg).equals(user)){
                msgs.add(msg);
            }
        }

        for(Message msg : msgs){
            if(senderMap.containsKey(msg))
                senderMap.remove(msg);
        }

        return msgs;
    }
}
