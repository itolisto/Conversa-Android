package ee.app.conversa;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.parse.ParseUser;

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
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(ActivitySplashScreen.this, ActivityMain.class);
            startActivity(intent);
        } else {
            setContentView(R.layout.activity_splash_screen);
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




