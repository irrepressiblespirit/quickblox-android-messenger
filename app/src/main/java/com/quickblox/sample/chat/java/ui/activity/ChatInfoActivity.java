package com.quickblox.sample.chat.java.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.chat.java.App;
import com.quickblox.sample.chat.java.R;
import com.quickblox.sample.chat.java.managers.DialogsManager;
import com.quickblox.sample.chat.java.repository.converters.DatabaseConvertor;
import com.quickblox.sample.chat.java.repository.helpers.UserRepositoryHelper;
import com.quickblox.sample.chat.java.repository.models.User;
import com.quickblox.sample.chat.java.ui.adapter.UsersAdapter;
import com.quickblox.sample.chat.java.utils.ToastUtils;
import com.quickblox.sample.chat.java.utils.chat.ChatHelper;
import com.quickblox.sample.chat.java.utils.qb.QbDialogUtils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class ChatInfoActivity extends BaseActivity {
    private static final String TAG = ChatInfoActivity.class.getSimpleName();
    private static final String EXTRA_DIALOG = "extra_dialog";

    private ListView usersListView;
    private QBChatDialog qbDialog;
    private UsersAdapter userAdapter;
    private SystemMessagesListener systemMessagesListener = new SystemMessagesListener();
    private DialogsManager dialogsManager = new DialogsManager();
    private QBSystemMessagesManager systemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();

    public static void start(Context context, QBChatDialog qbDialog) {
        Intent intent = new Intent(context, ChatInfoActivity.class);
        intent.putExtra(EXTRA_DIALOG, qbDialog);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_info);
        qbDialog = (QBChatDialog) getIntent().getSerializableExtra(EXTRA_DIALOG);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(qbDialog.getName());
            getSupportActionBar().setSubtitle(getString(R.string.chat_info_subtitle, String.valueOf(qbDialog.getOccupants().size())));
        }
        usersListView = findViewById(R.id.list_chat_info_users);
        List<Integer> userIds = qbDialog.getOccupants();
        QbDialogUtils.getQbUsersAndLocallySaveByIds(userIds).thenAccept(result -> {
            App.getInstance().getAppDatabase().userRepository().getUsersByIds(userIds)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableMaybeObserver<List<User>>() {
                        @Override
                        public void onSuccess(@NotNull List<User> users) {
                            if (users != null && !users.isEmpty()) {
                                List<QBUser> qbUsers = DatabaseConvertor.convertToQbUserList(users);
                                userAdapter = new UsersAdapter(ChatInfoActivity.this, qbUsers);
                                usersListView = findViewById(R.id.list_chat_info_users);
                                usersListView.setAdapter(userAdapter);
                            }
                        }

                        @Override
                        public void onError(@NotNull Throwable e) {
                            Log.e(TAG, e.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            Log.i(TAG, "onComplete() method in onCreate");
                        }
                    });
            getDialog();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        systemMessagesManager.removeSystemMessageListener(systemMessagesListener);
    }

    @Override
    public void onResumeFinished() {
        systemMessagesManager.addSystemMessageListener(systemMessagesListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_chat_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_chat_info_action_add_people) {
            updateDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void getDialog() {
        String dialogID = qbDialog.getDialogId();
        ChatHelper.getInstance().getDialogById(dialogID, new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                qbDialog = qbChatDialog;
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setSubtitle(getString(R.string.chat_info_subtitle, String.valueOf(qbDialog.getOccupants().size())));
                }
                buildUserList();
            }

            @Override
            public void onError(QBResponseException e) {
                ToastUtils.shortToast(e.getMessage());
                finish();
            }
        });
    }

    private void buildUserList() {
        List<Integer> userIds = qbDialog.getOccupants();
        UserRepositoryHelper.getInstance().hasAllUsers(userIds).thenAccept(result -> {
            if (result) {
                App.getInstance().getAppDatabase().userRepository().getUsersByIds(userIds)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableMaybeObserver<List<User>>() {
                            @Override
                            public void onSuccess(@NotNull List<User> users) {
                                if (!users.isEmpty()) {
                                    List<QBUser> qbUsers = DatabaseConvertor.convertToQbUserList(users);
                                    userAdapter.clearList();
                                    userAdapter.addUsers(qbUsers);
                                }
                            }

                            @Override
                            public void onError(@NotNull Throwable e) {
                                Log.e(TAG, e.getMessage());
                            }

                            @Override
                            public void onComplete() {
                                Log.i(TAG, "onComplete() method in buildUserList");
                            }
                        });
            } else {
                ChatHelper.getInstance().getUsersFromDialog(qbDialog, new QBEntityCallback<ArrayList<QBUser>>() {
                    @Override
                    public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                        if (qbUsers != null) {
                            App.getInstance().getAppDatabase().userRepository().putUsers(DatabaseConvertor.convertToUserList(qbUsers).toArray(new User[qbUsers.size()]))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new DisposableSingleObserver<List<Long>>() {
                                        @Override
                                        public void onSuccess(@NotNull List<Long> longs) {
                                            Log.i(TAG, "Users saved by ids: " + longs);
                                        }

                                        @Override
                                        public void onError(@NotNull Throwable throwable) {
                                            Log.e(TAG, throwable.getMessage());
                                        }
                                    });
                            userAdapter.addUsers(qbUsers);
                        }
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.d(TAG, e.getMessage());
                    }
                });
            }
        });
    }

    private void updateDialog() {
        showProgressDialog(R.string.dlg_updating);
        Log.d(TAG, "Starting Dialog Update");
        ChatHelper.getInstance().getDialogById(qbDialog.getDialogId(), new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog updatedChatDialog, Bundle bundle) {
                Log.d(TAG, "Update Dialog Successful: " + updatedChatDialog.getDialogId());
                qbDialog = updatedChatDialog;
                hideProgressDialog();
                SelectUsersActivity.startForResult(ChatInfoActivity.this, ChatActivity.REQUEST_CODE_SELECT_PEOPLE, updatedChatDialog);
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "Dialog Loading Error: " + e.getMessage());
                hideProgressDialog();
                showErrorSnackbar(R.string.select_users_get_dialog_error, e, null);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult with resultCode: $resultCode requestCode: $requestCode");
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ChatActivity.REQUEST_CODE_SELECT_PEOPLE && data != null) {
                showProgressDialog(R.string.chat_info_updating);
                final List<QBUser> selectedUsers = (ArrayList<QBUser>) data.getSerializableExtra(SelectUsersActivity.EXTRA_QB_USERS);
                List<Integer> existingOccupants = qbDialog.getOccupants();
                final List<Integer> newUserIds = new ArrayList<>();

                for (QBUser user : selectedUsers) {
                    if (!existingOccupants.contains(user.getId())) {
                        newUserIds.add(user.getId());
                    }
                }

                ChatHelper.getInstance().getDialogById(qbDialog.getDialogId(), new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        qbDialog = qbChatDialog;
                        dialogsManager.sendMessageAddedUsers(qbChatDialog, newUserIds);
                        dialogsManager.sendSystemMessageAddedUser(systemMessagesManager, qbChatDialog, newUserIds);
                        updateDialog(selectedUsers);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        hideProgressDialog();
                        showErrorSnackbar(R.string.update_dialog_error, e, null);
                    }
                });
            }
        }
    }

    private void updateDialog(final List<QBUser> selectedUsers) {
        ChatHelper.getInstance().updateDialogUsers(qbDialog, selectedUsers, new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                qbDialog = qbChatDialog;
                hideProgressDialog();
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                hideProgressDialog();
                showErrorSnackbar(R.string.chat_info_add_people_error, e, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateDialog(selectedUsers);
                    }
                });
            }
        });
    }

    private class SystemMessagesListener implements QBSystemMessageListener {
        @Override
        public void processMessage(QBChatMessage qbChatMessage) {
            Log.d(TAG, "System Message Received: " + qbChatMessage.getId());
            if (qbChatMessage.getDialogId().equals(qbDialog.getDialogId())) {
                getDialog();
            }
        }

        @Override
        public void processError(QBChatException e, QBChatMessage qbChatMessage) {
            Log.d(TAG, "System Messages Error: " + e.getMessage() + "With MessageID: " + qbChatMessage.getId());
        }
    }
}