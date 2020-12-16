package test.SecurityChatApp;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.Executor;

@RequiresApi(api = Build.VERSION_CODES.M)
public class SplashActivity extends AppCompatActivity
{
    private final int SPLASH_DISPLAY_LENGTH = 200;

    private static final String KEY_NAME = "Security";
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    public boolean pass = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //FirebaseAuth.getInstance().signOut();

        fingerprint();

        if(pass == true)
        {
            Run();
        }
        else
        {
            fingerprint();
        }
    }


    public boolean fingerprint()
    {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this,
                executor, new BiometricPrompt.AuthenticationCallback()
        {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString)
            {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        R.string.auth_error_message, Toast.LENGTH_SHORT)
                        .show();
                pass = false;
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result)
            {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(),
                        R.string.auth_success_message, Toast.LENGTH_SHORT).show();
                pass = true;
                Run();

            }

            @Override
            public void onAuthenticationFailed()
            {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), R.string.auth_fail_message,
                        Toast.LENGTH_SHORT)
                        .show();
                pass = false;
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("지문 인증")
                .setSubtitle("지문을 인증해주세요.")
                .setNegativeButtonText("취소")
                .setDeviceCredentialAllowed(false)
                .build();
        //지문인증
        if(pass == false)
        {
            biometricPrompt.authenticate(promptInfo);
        }
        else
        {
            return true;
        }
        return true;
    }



    public void Run()
    {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Intent mainIntent = null;
                if (FirebaseAuth.getInstance().getCurrentUser() == null)
                {
                    mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                } else
                {
                    mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                }
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }


}