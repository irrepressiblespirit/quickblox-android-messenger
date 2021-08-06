package com.quickblox.sample.chat.java.utils.qb;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.chat.java.App;
import com.quickblox.sample.chat.java.repository.converters.DatabaseConvertor;
import com.quickblox.sample.chat.java.repository.models.User;
import com.quickblox.sample.chat.java.utils.chat.ChatHelper;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class QbDialogUtils {
    private static final String TAG = QbDialogUtils.class.getSimpleName();

    public static QBChatDialog createDialog(List<QBUser> users, String chatName) {
        if (isPrivateChat(users)) {
            QBUser currentUser = ChatHelper.getCurrentUser();
            users.remove(currentUser);
        }
        QBChatDialog dialog = DialogUtils.buildDialog(users.toArray(new QBUser[users.size()]));
        if (!TextUtils.isEmpty(chatName)) {
            dialog.setName(chatName);
        }
        return dialog;
    }

    private static boolean isPrivateChat(List<QBUser> users) {
        return users.size() == 2;
    }

    public static CompletableFuture<List<QBUser>> getAddedUsers(QBChatDialog dialog, List<QBUser> currentUsers) {
        return getQbUsersFromQbDialog(dialog).thenApply(result -> getAddedUsers(result, currentUsers));
    }

    public static List<QBUser> getAddedUsers(@NotNull List<QBUser> previousUsers, @NotNull List<QBUser> currentUsers) {
        List<QBUser> addedUsers = new ArrayList<>();
        for (QBUser currentUser : currentUsers) {
            boolean wasInChatBefore = false;
            for (QBUser previousUser : previousUsers) {
                if (currentUser.getId().equals(previousUser.getId())) {
                    wasInChatBefore = true;
                    break;
                }
            }
            if (!wasInChatBefore) {
                addedUsers.add(currentUser);
            }
        }

        QBUser currentUser = ChatHelper.getCurrentUser();
        addedUsers.remove(currentUser);

        return addedUsers;
    }

    public static CompletableFuture<List<QBUser>> getRemovedUsers(QBChatDialog dialog, List<QBUser> currentUsers) {
        return getQbUsersFromQbDialog(dialog).thenApply(result -> getRemovedUsers(result, currentUsers));
    }

    public static List<QBUser> getRemovedUsers(@NotNull List<QBUser> previousUsers, @NotNull List<QBUser> currentUsers) {
        List<QBUser> removedUsers = new ArrayList<>();
        for (QBUser previousUser : previousUsers) {
            boolean isUserStillPresented = false;
            for (QBUser currentUser : currentUsers) {
                if (previousUser.getId().equals(currentUser.getId())) {
                    isUserStillPresented = true;
                    break;
                }
            }
            if (!isUserStillPresented) {
                removedUsers.add(previousUser);
            }
        }

        QBUser currentUser = ChatHelper.getCurrentUser();
        removedUsers.remove(currentUser);

        return removedUsers;
    }

    public static void logDialogUsers(QBChatDialog qbDialog) {
        getDialogName(qbDialog).thenAccept(result -> Log.v(TAG, "Dialog " + result));
        logUsersByIds(qbDialog.getOccupants());
    }

    public static void logUsers(List<QBUser> users) {
        for (QBUser user : users) {
            Log.i(TAG, user.getId() + " " + user.getLogin());
        }
    }

    private static void logUsersByIds(List<Integer> users) {
        for (Integer id : users) {
            App.getInstance().getAppDatabase().userRepository().getUserById(id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableMaybeObserver<List<User>>() {
                        @Override
                        public void onSuccess(@NotNull List<User> users) {
                            if (!users.isEmpty()) {
                                QBUser user = DatabaseConvertor.convert(users.get(0));
                                Log.i(TAG, ((user != null && user.getId() != null) ? (user.getId() + " " + user.getLogin()) : "noId"));
                            }
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            Log.e(TAG, e.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            Log.i(TAG, "onComplete() method in logUsersByIds()");
                        }
                    });
        }
    }

    public static CompletableFuture<String> getDialogName(final QBChatDialog dialog) {
        CompletableFuture<String> promise = new CompletableFuture<>();
        if (dialog.getType().equals(QBDialogType.GROUP)) {
            promise.complete(dialog.getName());
            return promise;
        } else {
            // It's a private dialog, let's use opponent's name as chat name
            List<Integer> opponentsIds = dialog.getOccupants();
            QBUsers.getUsersByIDs(opponentsIds, new QBPagedRequestBuilder()).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                    if (!qbUsers.isEmpty()) {
                        CompletableFuture.runAsync(() -> getQbUsersAndLocallySaveByIds(opponentsIds));
                        StringBuilder occupantsNames = new StringBuilder();
                        Collections.sort(qbUsers, Comparator.comparing(user -> user.getId().intValue() != dialog.getUserId().intValue()));
                        for (QBUser user : qbUsers) {
                            occupantsNames.append(TextUtils.isEmpty(user.getFullName()) ? user.getLogin() : user.getFullName()).append(",");
                        }
                        occupantsNames.deleteCharAt(occupantsNames.lastIndexOf(","));
                        promise.complete(occupantsNames.toString());
                    } else {
                        promise.complete(dialog.getName());
                    }
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e(TAG, e.getMessage());
                }
            });
            return promise;
        }
    }

    public static CompletableFuture<List<QBUser>> getQbUsersAndLocallySaveByIds(@NotNull List<Integer> ids) {
        CompletableFuture<List<QBUser>> future = new CompletableFuture<>();
        QBUsers.getUsersByIDs(ids, new QBPagedRequestBuilder()).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                future.complete(qbUsers);
                if (qbUsers != null && !qbUsers.isEmpty()) {
                    App.getInstance().getAppDatabase().userRepository().getAllUsers()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DisposableMaybeObserver<List<User>>() {
                                @Override
                                public void onSuccess(@NotNull List<User> users) {
                                    List<Integer> ids = new ArrayList<>();
                                    for (User user : users) {
                                        ids.add(user.getId());
                                    }
                                    for (QBUser qbUser : qbUsers) {
                                        if (!ids.contains(qbUser.getId())) {
                                            App.getInstance().getAppDatabase().userRepository().putUser(DatabaseConvertor.convert(qbUser))
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(new DisposableSingleObserver<Long>() {
                                                        @Override
                                                        public void onSuccess(@NotNull Long aLong) {
                                                            Log.i(TAG, "User saved with id: " + aLong);
                                                        }

                                                        @Override
                                                        public void onError(@NotNull Throwable e) {
                                                            Log.e(TAG, e.getMessage());
                                                        }
                                                    });
                                        }
                                    }
                                }

                                @Override
                                public void onError(@NotNull Throwable e) {
                                    Log.e(TAG, e.getMessage());
                                }

                                @Override
                                public void onComplete() {
                                    Log.i(TAG, "User DB is empty !!!");
                                }
                            });

                }
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, e.getMessage());
            }
        });
        return future;
    }

    private static CompletableFuture<List<QBUser>> getQbUsersFromQbDialog(QBChatDialog dialog) {
        CompletableFuture<List<QBUser>> previousDialogUsers = new CompletableFuture<>();
        for (Integer id : dialog.getOccupants()) {
            App.getInstance().getAppDatabase().userRepository().getUserById(id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableMaybeObserver<List<User>>() {
                        @Override
                        public void onSuccess(@NotNull List<User> users) {
                            if (!users.isEmpty()) {
                                previousDialogUsers.complete(DatabaseConvertor.convertToQbUserList(users));
                            }
                            if (!previousDialogUsers.isDone()) {
                                previousDialogUsers.complete(null);
                            }
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            Log.e(TAG, e.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            previousDialogUsers.complete(null);
                        }
                    });
        }
        return previousDialogUsers;
    }

    public static List<Integer> getOccupantsIdsListFromString(String occupantIds) {
        List<Integer> occupantIdsList = new ArrayList<>();
        if (occupantIds != null) {
            String[] occupantIdsArray = occupantIds.split(",");
            for (String occupantId : occupantIdsArray) {
                occupantIdsList.add(Integer.valueOf(occupantId.trim()));
            }
        }
        return occupantIdsList;
    }

    public static String getOccupantsIdsStringFromList(Collection<Integer> occupantIdsList) {
        return TextUtils.join(",", occupantIdsList);
    }

    public static String getOccupantsNamesStringFromList(Collection<QBUser> qbUsers) {
        ArrayList<String> userNameList = new ArrayList<>();
        for (QBUser user : qbUsers) {
            if (TextUtils.isEmpty(user.getFullName())) {
                userNameList.add(user.getLogin());
            } else {
                userNameList.add(user.getFullName());
            }
        }
        return TextUtils.join(", ", userNameList);
    }

    public static QBChatDialog buildPrivateChatDialog(String dialogId, Integer recipientId) {
        QBChatDialog chatDialog = DialogUtils.buildPrivateDialog(recipientId);
        chatDialog.setDialogId(dialogId);

        return chatDialog;
    }
}