package com.quickblox.sample.chat.java.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.chat.java.App;
import com.quickblox.sample.chat.java.R;
import com.quickblox.sample.chat.java.repository.converters.DatabaseConvertor;
import com.quickblox.sample.chat.java.repository.models.User;
import com.quickblox.sample.chat.java.utils.SharedPrefsHelper;
import com.quickblox.sample.chat.java.utils.ValidationUtils;
import com.quickblox.sample.chat.java.utils.chat.ChatHelper;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends BaseActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private static final int UNAUTHORIZED = 401;
    private static final String DRAFT_LOGIN = "draft_login";
    private static final String DRAFT_PASSWORD = "draftPassword1$";

    private EditText loginEt;
    private EditText passwordEt;
    private TextView loginHint;
    private TextView passwordHint;
    private TextView btnLogin;
    private CheckBox chbSave;
    private LinearLayout rootView;
    private LinearLayout hidableHolder;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        prepareListeners();
        fillViews();
        defineFocusedBehavior();
    }

    private void initViews() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(20f);
        }
        loginHint = findViewById(R.id.tv_login_hint);
        passwordHint = findViewById(R.id.tv_username_hint);
        btnLogin = findViewById(R.id.tv_btn_login);
        loginEt = findViewById(R.id.et_login);
        passwordEt = findViewById(R.id.et_user_name);
        chbSave = findViewById(R.id.chb_login_save);
        btnLogin = findViewById(R.id.tv_btn_login);
        hidableHolder = findViewById(R.id.ll_hidable_holder);
        rootView = findViewById(R.id.root_view_login_activity);
    }

    private void fillViews() {
        String draftLogin = SharedPrefsHelper.getInstance().get(DRAFT_LOGIN, null);
        String draftUserName = SharedPrefsHelper.getInstance().get(DRAFT_PASSWORD, null);

        if (!TextUtils.isEmpty(draftLogin)) {
            loginEt.setText(draftLogin);
        }
        if (!TextUtils.isEmpty(draftUserName)) {
            passwordEt.setText(draftUserName);
        }

        validateFields();
    }

    private void defineFocusedBehavior() {
        loginHint.setVisibility(View.GONE);
        passwordHint.setVisibility(View.GONE);

        loginEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (hasFocus) {
                        loginEt.setTranslationZ(10f);
                    } else {
                        loginEt.setTranslationZ(0f);
                    }
                }
                if (ValidationUtils.isLoginValid(LoginActivity.this, loginEt)) {
                    loginHint.setVisibility(View.GONE);
                } else {
                    loginHint.setVisibility(View.VISIBLE);
                }
            }
        });

        passwordEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (hasFocus) {
                        passwordEt.setTranslationZ(10f);
                    } else {
                        passwordEt.setTranslationZ(0f);
                    }
                }
                if (ValidationUtils.isLoginValid(LoginActivity.this, passwordEt)) {
                    passwordHint.setVisibility(View.GONE);
                } else {
                    passwordHint.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void prepareListeners() {
        rootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                chbSave.setChecked(!chbSave.isChecked());

                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (chbSave.isChecked()) {
                    if (vibrator != null) {
                        vibrator.vibrate(80);
                    }
                } else {
                    if (vibrator != null) {
                        vibrator.vibrate(250);
                    }
                }
                return true;
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnLogin.isActivated()) {
                    showProgressDialog(R.string.dlg_login);
                    prepareUser();
                }
            }
        });

        loginEt.addTextChangedListener(new TextWatcherListener(loginEt));
        passwordEt.addTextChangedListener(new TextWatcherListener(passwordEt));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_login_app_info:
                AppInfoActivity.start(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Boolean validateFields() {
        Boolean loginValid = ValidationUtils.isLoginValid(this, loginEt);
        Boolean userNameValid = ValidationUtils.isPasswordValid(this, passwordEt);

        if (loginValid) {
            loginHint.setVisibility(View.GONE);
        } else {
            loginHint.setVisibility(View.VISIBLE);
        }

        if (userNameValid) {
            passwordHint.setVisibility(View.GONE);
        } else {
            passwordHint.setVisibility(View.VISIBLE);
        }

        if (loginValid && userNameValid) {
            btnLogin.setActivated(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnLogin.setElevation(0F);
                btnLogin.setTranslationZ(10F);
            }
            return true;
        } else {
            btnLogin.setActivated(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnLogin.setElevation(0F);
                btnLogin.setTranslationZ(0F);
            }
            return false;
        }
    }

    private void saveDrafts() {
        SharedPrefsHelper.getInstance().save(DRAFT_LOGIN, loginEt.getText().toString());
        SharedPrefsHelper.getInstance().save(DRAFT_PASSWORD, passwordEt.getText().toString());
    }

    private void clearDrafts() {
        SharedPrefsHelper.getInstance().save(DRAFT_LOGIN, "");
        SharedPrefsHelper.getInstance().save(DRAFT_PASSWORD, "");
    }

    private void prepareUser() {
        QBUser qbUser = new QBUser();
        qbUser.setLogin(loginEt.getText().toString().trim());
        qbUser.setPassword(passwordEt.getText().toString().trim());
        signIn(qbUser);
    }

    private void signIn(final QBUser user) {
        showProgressDialog(R.string.dlg_login);
        ChatHelper.getInstance().login(user, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser userFromRest, Bundle bundle) {
                if (userFromRest != null && userFromRest.getId() != null && userFromRest.getId().compareTo(user.getId()) == 0) {
                    loginToChat(user);
                } else {
                    //Need to set password NULL, because server will update user only with NULL password
                    user.setPassword(null);
                    updateUser(user);
                }
            }

            @Override
            public void onError(QBResponseException e) {
                if (e.getHttpStatusCode() == UNAUTHORIZED) {
                    signUp(user);
                } else {
                    hideProgressDialog();
                    showErrorSnackbar(R.string.login_chat_login_error, e, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            signIn(user);
                        }
                    });
                }
            }
        });
    }

    private void updateUser(final QBUser user) {
        ChatHelper.getInstance().updateUser(user, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle bundle) {
                loginToChat(user);
            }

            @Override
            public void onError(QBResponseException e) {
                hideProgressDialog();
                showErrorSnackbar(R.string.login_chat_login_error, e, null);
            }
        });
    }

    private void loginToChat(final QBUser user) {
        ChatHelper.getInstance().loginToChat(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                SharedPrefsHelper.getInstance().saveQbUser(user);
                if (!chbSave.isChecked()) {
                    clearDrafts();
                }
                App.getInstance().getAppDatabase().userRepository().getUserById(user.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableMaybeObserver<List<User>>() {
                            @Override
                            public void onSuccess(@NotNull List<User> users) {
                                if (users == null || users.isEmpty()) {
                                    App.getInstance().getAppDatabase().userRepository().putUser(DatabaseConvertor.convert(user))
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new DisposableSingleObserver<Long>() {
                                                @Override
                                                public void onSuccess(@NotNull Long aLong) {
                                                    Log.i(TAG, "User is saved to DB by next id: " + aLong);
                                                }

                                                @Override
                                                public void onError(@NotNull Throwable throwable) {
                                                    Log.e(TAG, throwable.getMessage());
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
                                Log.i(TAG, "onComplete() method in loginToChat");
                            }
                        });
                DialogsActivity.start(LoginActivity.this);
                finish();
                hideProgressDialog();
            }

            @Override
            public void onError(QBResponseException e) {
                hideProgressDialog();
                showErrorSnackbar(R.string.login_chat_login_error, e, null);
            }
        });
    }

    private void signUp(final QBUser newUser) {
        SharedPrefsHelper.getInstance().removeQbUser();
        QBUsers.signUp(newUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle bundle) {
                hideProgressDialog();
                signIn(newUser);
            }

            @Override
            public void onError(QBResponseException e) {
                hideProgressDialog();
                showErrorSnackbar(R.string.login_sign_up_error, e, null);
            }
        });
    }

    private class TextWatcherListener implements TextWatcher {
        private EditText editText;
        private Timer timer = new Timer();

        private TextWatcherListener(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text = s.toString().replace("  ", " ");
            if (!editText.getText().toString().equals(text)) {
                editText.setText(text);
                editText.setSelection(text.length());
            }
            validateFields();
        }

        @Override
        public void afterTextChanged(Editable s) {
            timer.cancel();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    saveDrafts();
                }
            }, 300);
        }
    }
}