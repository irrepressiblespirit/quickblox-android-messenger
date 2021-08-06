package com.quickblox.sample.chat.java.repository.converters;


import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.sample.chat.java.repository.models.Dialog;
import com.quickblox.sample.chat.java.repository.models.User;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class DatabaseConvertor {

    public static QBChatDialog convert(Dialog dialog) {
        QBChatDialog chatDialog = new QBChatDialog();
        chatDialog.setDialogId(dialog.getId());
        chatDialog.setName(dialog.getName());
        chatDialog.setOccupantsIds(dialog.getOccupantsIds());
        chatDialog.setUnreadMessageCount(dialog.getUnreadMessageCount());
        chatDialog.setLastMessage(dialog.getLastMessage());
        chatDialog.setLastMessageDateSent(dialog.getLastMessageDateSent());
        chatDialog.setLastMessageUserId(dialog.getLastMessageUserId());
        chatDialog.setType(QBDialogType.parseByCode(dialog.getType()));
        chatDialog.setPhoto(dialog.getPhoto());
        chatDialog.setRoomJid(dialog.getRoomJid());
        chatDialog.setUserId(dialog.getUserId());
        return chatDialog;
    }

    public static QBUser convert(User user) {
        QBUser qbUser = new QBUser();
        qbUser.setId(user.getId());
        user.setFullName(user.getFullName());
        qbUser.setLogin(user.getLogin());
        qbUser.setPassword(user.getPassword());
        qbUser.setEmail(user.getEmail());
        qbUser.setExternalId(user.getExternalId());
        qbUser.setFacebookId(user.getFacebookId());
        qbUser.setTwitterId(user.getTwitterId());
        qbUser.setPhone(user.getPhone());
        qbUser.setWebsite(user.getWebsite());
        qbUser.setLastRequestAt(user.getLastRequestAt());
        return qbUser;
    }

    public static User convert(QBUser qbUser) {
        User user = new User();
        user.setId(qbUser.getId());
        user.setFullName(qbUser.getFullName());
        user.setLogin(qbUser.getLogin());
        user.setPassword(qbUser.getPassword());
        user.setEmail(qbUser.getEmail());
        user.setExternalId(qbUser.getExternalId());
        user.setFacebookId(qbUser.getFacebookId());
        user.setTwitterId(qbUser.getTwitterId());
        user.setPhone(qbUser.getPhone());
        user.setWebsite(qbUser.getWebsite());
        user.setLastRequestAt(qbUser.getLastRequestAt());
        return user;
    }

    public static List<User> convertToUserList(List<QBUser> userList) {
        List<User> users = new ArrayList<>();
        for (QBUser item : userList) {
            users.add(convert(item));
        }
        return users;
    }

    public static List<QBUser> convertToQbUserList(List<User> userList) {
        List<QBUser> users = new ArrayList<>();
        for (User user : userList) {
            users.add(convert(user));
        }
        return users;
    }

    public static Dialog convert(QBChatDialog chatDialog) {
        Dialog dialog = new Dialog();
        dialog.setId(chatDialog.getDialogId());
        dialog.setName(chatDialog.getName());
        dialog.setType(chatDialog.getType().getCode());
        dialog.setOccupantsIds(chatDialog.getOccupants());
        dialog.setUnreadMessageCount(chatDialog.getUnreadMessageCount() == null ? 0 : chatDialog.getUnreadMessageCount());
        dialog.setLastMessage(chatDialog.getLastMessage());
        dialog.setLastMessageDateSent(chatDialog.getLastMessageDateSent());
        dialog.setLastMessageUserId(chatDialog.getLastMessageUserId() == null ? 0 : chatDialog.getLastMessageUserId());
        dialog.setPhoto(chatDialog.getPhoto());
        dialog.setRoomJid(chatDialog.getRoomJid());
        dialog.setUserId(chatDialog.getUserId());
        return dialog;
    }

    public static List<Dialog> convertToDialogList(List<QBChatDialog> dialogs) {
        List<Dialog> dialogList = new ArrayList<>();
        for (QBChatDialog item : dialogs) {
            dialogList.add(convert(item));
        }
        return dialogList;
    }

    public static List<QBChatDialog> convertToChatDialogs(List<Dialog> dialogs) {
        List<QBChatDialog> listDialogs = new ArrayList<>();
        for (Dialog dialog : dialogs) {
            listDialogs.add(convert(dialog));
        }
        return listDialogs;
    }
}
