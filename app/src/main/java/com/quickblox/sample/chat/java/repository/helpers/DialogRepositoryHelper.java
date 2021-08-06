package com.quickblox.sample.chat.java.repository.helpers;


import android.util.Log;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.sample.chat.java.App;
import com.quickblox.sample.chat.java.repository.converters.DatabaseConvertor;
import com.quickblox.sample.chat.java.repository.models.Dialog;
import com.quickblox.users.model.QBUser;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class DialogRepositoryHelper {

    private static final String TAG = DialogRepositoryHelper.class.getSimpleName();

    private static DialogRepositoryHelper instance;

    public static synchronized DialogRepositoryHelper getInstance() {
        if (instance == null) {
            instance = new DialogRepositoryHelper();
        }
        return instance;
    }

//    public boolean hasDialogWithId(String dialogId) {
//        App.getInstance().getAppDatabase().dialogRepository().getChatDialogById(dialogId)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<List<Dialog>>() {
//                    @Override
//                    public void accept(@NotNull List<Dialog> dialogs) throws Exception {
//
//                    }
//                });
//        Dialog dialog = App.getInstance().getAppDatabase().dialogRepository().getChatDialogById(dialogId);
//        return dialog != null;
//    }

    public CompletableFuture<Boolean> hasPrivateDialogWithUser(QBUser user) {
        return getPrivateDialogWithUser(user).thenApply(Objects::nonNull);
    }

    public CompletableFuture<Dialog> getPrivateDialogWithUser(QBUser user) {
        CompletableFuture<Dialog> future = new CompletableFuture<>();
        App.getInstance().getAppDatabase().dialogRepository().getDialogs()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableMaybeObserver<List<Dialog>>() {

                    @Override
                    public void onSuccess(@NotNull List<Dialog> dialogs) {
                        if (!dialogs.isEmpty()) {
                            for (Dialog dialog : dialogs) {
                                if (QBDialogType.PRIVATE.equals(QBDialogType.parseByCode(dialog.getType()))
                                        && dialog.getOccupantsIds().contains(user.getId())) {
                                    future.complete(dialog);
                                }
                            }
                        }
                        if (!future.isDone()) {
                            future.complete(null);
                        }
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        System.out.println(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        future.complete(null);
                    }
                });
        return future;
    }

    public void updateDialog(String dialogId, QBChatMessage qbChatMessage) {
        App.getInstance().getAppDatabase().dialogRepository().getChatDialogById(dialogId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableMaybeObserver<List<Dialog>>() {
                    @Override
                    public void onSuccess(@NotNull List<Dialog> dialogs) {
                        if (!dialogs.isEmpty()) {
                            QBChatDialog updatedDialog = DatabaseConvertor.convert(dialogs.get(0));
                            updatedDialog.setLastMessage(qbChatMessage.getBody());
                            updatedDialog.setLastMessageDateSent(qbChatMessage.getDateSent());
                            updatedDialog.setUnreadMessageCount(updatedDialog.getUnreadMessageCount() != null
                                    ? updatedDialog.getUnreadMessageCount() + 1 : 1);
                            updatedDialog.setLastMessageUserId(qbChatMessage.getSenderId());
                            App.getInstance().getAppDatabase().dialogRepository().updateDialog(DatabaseConvertor.convert(updatedDialog))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new DisposableSingleObserver<Integer>() {
                                        @Override
                                        public void onSuccess(@NotNull Integer integer) {
                                            Log.i(TAG, "Dialog has been updated by id: " + integer);
                                        }

                                        @Override
                                        public void onError(@NotNull Throwable e) {
                                            Log.e(TAG, e.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        Log.e(TAG, e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.i(TAG, "onComplete() method in updateDialog");
                    }
                });
    }
}
