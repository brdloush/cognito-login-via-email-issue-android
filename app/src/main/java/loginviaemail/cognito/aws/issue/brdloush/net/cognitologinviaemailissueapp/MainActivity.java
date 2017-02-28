package loginviaemail.cognito.aws.issue.brdloush.net.cognitologinviaemailissueapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private IssueService getIssueService(Activity activity) {
        return new IssueService();
    }

    public class LoginAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            getIssueService(MainActivity.this).performLogin(thisActivity());
            return null;
        }
    }

    public class ClearSharedPropsAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            getIssueService(MainActivity.this).clearSharedProps(thisActivity());
            return null;
        }
    }

    private Activity thisActivity() {
        return this;
    }

    public void tryLogin(View v) {
        new LoginAsyncTask().execute();
    }

    public void clearSharedProps(View v) {
        new ClearSharedPropsAsyncTask().execute();
    }

    public void setTimeoutingRefreshThreshold(View v) {
        getIssueService(this).setRefreshThreshold(Long.MAX_VALUE);
    }

    public void setDefaultRefreshThreshold(View v) {
        getIssueService(this).setRefreshThreshold(300*1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}
