package test.SecurityChatApp;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import test.SecurityChatApp.common.Util9;

public class UserPWActivity extends AppCompatActivity {
    private EditText user_pw1;
    private EditText user_pw2;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userpw);

        user_pw1 = findViewById(R.id.user_pw1);
        user_pw2 = findViewById(R.id.user_pw2);
        saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(saveBtnClickListener);
    }


    Button.OnClickListener saveBtnClickListener = new View.OnClickListener() {
        public void onClick(final View view) {
            String pw1 = user_pw1.getText().toString().trim();
            if (pw1.length()<8) {
                Util9.showMessage(getApplicationContext(), "적어도 8글자를 입력해주세요.");
                return;
            }
            if (!pw1.equals(user_pw2.getText().toString().trim())) {
                Util9.showMessage(getApplicationContext(), "비밀번호가 일치하지 않습니다.");
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            user.updatePassword(pw1).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Util9.showMessage(getApplicationContext(), "비밀번호 변경됨");

                    InputMethodManager imm= (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(user_pw2.getWindowToken(), 0);

                    onBackPressed();
                }
            });
        }
    };
}
