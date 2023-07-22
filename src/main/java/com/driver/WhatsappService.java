package com.driver;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service

public class WhatsappService {

    WhatsappRepository whatsappRepository = new WhatsappRepository();

    public String createUser(String name, String mobile) throws Exception{

        Optional<User> optionalUser = getUser(mobile);

        if(optionalUser.isPresent())
            throw new Exception("User already exists");

        User user = new User(name,mobile);
        whatsappRepository.adduser(user);

        return "SUCCESS";
    }

    public Optional<User> getUser(String mobile) {

        return whatsappRepository.getUser(mobile);
    }


    public Integer createMessage(String content) {

        return whatsappRepository.createMessage(content);
    }

    public Group createGroup(List<User> users) {

        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.
        // If group is successfully created, return group.

        //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
        //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.


        if (users.size() == 2)
            return whatsappRepository.createNewChat(users);

        return whatsappRepository.createGroup(users);
    }

    public Integer sendMessage(Message message, User sender, Group group) throws Exception {

        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.

        Optional<List<User>> optionalUsers = getUsersfromGroup(group);

        if(optionalUsers.isEmpty()){
            throw new Exception("Group does not exist");
        }

        List<User> users = optionalUsers.get();

        if(!users.contains(sender)){
            throw new Exception("You are not allowed to send message");
        }

        return whatsappRepository.sendMessage(sender,message,group);

    }

    private Optional<List<User>> getUsersfromGroup(Group group) {

        return whatsappRepository.getUsersfromGroup(group);
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {

        Optional<List<User>> optionalUsers = getUsersfromGroup(group);
        if(optionalUsers.isEmpty()){
            throw new Exception("Group does not exist");
        }

        List<User> users = optionalUsers.get();

        if(!users.contains(user)){
            throw new Exception("User is not a participant");
        }

        User curr_admin = getAdmin(group);

        if(!curr_admin.equals(approver)){
            throw new Exception("Approver does not have rights");
        }

        whatsappRepository.changeAdmin(user,group);
        return "SUCCESS";

    }

    private User getAdmin(Group group) {

        return whatsappRepository.getAdmin(group);
    }

    public Integer removeUser(User user) throws Exception {

        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)

        Optional<Group> optionalGroup = findUserGroup(user);

        if(optionalGroup.isEmpty())
            throw new Exception("User not found");

        User admin = getAdmin(optionalGroup.get());

        if(admin!=null && admin.equals(user))
            throw new Exception("Cannot remove admin");

        return whatsappRepository.removeUser(user,optionalGroup.get());
    }

    private Optional<Group> findUserGroup(User user) {

        return whatsappRepository.findUserGroup(user);
    }
}
