package com.quickblox.sample.chat.java.repository.helpers;

import com.quickblox.sample.chat.java.App;
import com.quickblox.sample.chat.java.repository.models.User;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.schedulers.Schedulers;

public class UserRepositoryHelper {

    private static UserRepositoryHelper instance;

    public static synchronized UserRepositoryHelper getInstance() {
        if (instance == null) {
            instance = new UserRepositoryHelper();
        }
        return instance;
    }

    public CompletableFuture<Boolean> hasAllUsers(final List<Integer> usersIds) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        future.complete(true);
        App.getInstance().getAppDatabase().userRepository().getAllUsers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableMaybeObserver<List<User>>() {
                    @Override
                    public void onSuccess(@NotNull List<User> users) {
                        if (!users.isEmpty()) {
                            List<Integer> ids = new ArrayList<>();
                            for (User user : users) {
                                ids.add(user.getId());
                            }
                            for (Integer userId : usersIds) {
                                if (!ids.contains(userId)) {
                                    future.complete(false);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        future.complete(false);
                    }

                    @Override
                    public void onComplete() {
                        future.complete(false);
                    }
    });
        return future;
    }
}
