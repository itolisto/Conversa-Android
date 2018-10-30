package ee.app.conversa;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.parse.ParseUser;

import androidx.appcompat.app.AppCompatActivity;

/**
 * SplashScreenActivity
 * 
 * Displays splash screen for 1.2 seconds.
 * 
 */

public class ActivitySplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(ActivitySplashScreen.this, ActivityMain.class);
                    startActivity(intent);
                }
            }, 1200);
        } else {
            if (ConversaApp.getInstance(this).getPreferences().getShowTutorial()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent go = new Intent(ActivitySplashScreen.this, ActivityTutorial.class);
                        startActivity(go);
                    }
                }, 1200);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent go = new Intent(ActivitySplashScreen.this, ActivitySignIn.class);
                        startActivity(go);
                    }
                }, 1200);
            }
        }
    }

}




