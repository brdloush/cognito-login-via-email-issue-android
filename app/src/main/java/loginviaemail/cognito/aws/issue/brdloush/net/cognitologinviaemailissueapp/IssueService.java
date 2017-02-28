package loginviaemail.cognito.aws.issue.brdloush.net.cognitologinviaemailissueapp;

import android.app.Activity;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoAccessToken;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoIdToken;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoRefreshToken;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.util.CognitoIdentityProviderClientConfig;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProviderClient;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by brdloush on 27.2.17.
 */
public class IssueService {
    private final String poolId;
    private final String clientId;
    private final String clientSecret;
    private final String userEmail;
    private final String username;
    private final String password;

    public IssueService() {
        this.poolId = "FIXME_PROVIDE_VALUE";
        this.clientId = "FIXME_PROVIDE_VALUE";
        this.clientSecret = "FIXME_PROVIDE_VALUE";

        this.userEmail = "FIXME_PROVIDE_VALUE";
        this.username = "FIXME_PROVIDE_VALUE";
        this.password = "FIXME_PROVIDE_VALUE";

    }

    public void performLogin(final Activity activity) {
        CognitoUserPool cognitoUserPool = createCognitoUserPool(activity);

        boolean useEmailForLogin = ((CheckBox)activity.findViewById(R.id.useEmailForLogin)).isChecked();
        String usernameOrEmail= useEmailForLogin ? userEmail : username;

        CognitoUser currentUser = cognitoUserPool.getCurrentUser();
        boolean isCurrentUserValid = currentUser != null && currentUser.getUserId() != null;
        CognitoUser user = isCurrentUserValid ? currentUser : cognitoUserPool.getUser(usernameOrEmail);

        boolean knowsPassword = ((CheckBox)activity.findViewById(R.id.knowsPasswd)).isChecked();
        String pass = knowsPassword ? password : null;

        user.getSession(getAuthHandler(activity, pass));
    }

    private AuthenticationHandler getAuthHandler(final Activity activity, final String pPassword) {
        return new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                CognitoAccessToken accessToken = userSession.getAccessToken();
                CognitoIdToken idToken = userSession.getIdToken();
                CognitoRefreshToken refreshToken = userSession.getRefreshToken();

                updateTokens(activity, accessToken, refreshToken);

                Log.d("IssueSvc.performLogin", "onSuccess");
                Log.d("IssueSvc.performLogin", "md5 accessToken="+md5(accessToken.getJWTToken()));
                Log.d("IssueSvc.performLogin", "md5 refreshToken="+md5(refreshToken.getToken()));
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                Log.d("IssueSvc", "getAuthenticationDetails - returning user="+userId+", pass="+pPassword);

                AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, pPassword, null);
                authenticationContinuation.setAuthenticationDetails(authenticationDetails);
                authenticationContinuation.continueTask();
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
                continuation.continueTask();
            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {
                Log.i("IssueSvc.performLogin", "authenticationChallenge");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("IssueSvc.performLogin", "onFailure",e);

            }
        };
    }

    private void updateTokens(final Activity activity, final CognitoAccessToken accessToken, final CognitoRefreshToken refreshToken) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EditText accessTokenEdit = (EditText) activity.findViewById(R.id.accessToken);
                EditText refreshTokenEdit = (EditText) activity.findViewById(R.id.refreshToken);

                String accessTokenMd5 = accessToken != null ? md5(accessToken.getJWTToken()) : "";
                String refreshTokenMd5 = refreshToken != null ? md5(refreshToken.getToken()) : "";

                accessTokenEdit.setText(accessTokenMd5);
                refreshTokenEdit.setText(refreshTokenMd5);
            }
        });
    }


    public CognitoUserPool createCognitoUserPool(Activity activity) {
        AmazonCognitoIdentityProviderClient identityProviderClient = new AmazonCognitoIdentityProviderClient(new AnonymousAWSCredentials(), new ClientConfiguration());
        identityProviderClient.setRegion(Region.getRegion(Regions.EU_WEST_1));
        return new CognitoUserPool(activity, poolId, clientId, clientSecret, identityProviderClient);
    }

    public void clearSharedProps(final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String csiLastUserKey = "CognitoIdentityProvider." + clientId + ".LastAuthUser";
                activity.getSharedPreferences("CognitoIdentityProviderCache", 0).edit().clear().commit();
                updateTokens(activity, null, null);
            }
        });
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.e("IssueSvc", "md5 err", e);
        }
        return "";
    }

    public void setRefreshThreshold(long refreshThreshold) {
        try {
            Field f = CognitoIdentityProviderClientConfig.class.getDeclaredField("refreshThreshold");
            f.setAccessible(true);
            f.set(null, refreshThreshold);
        } catch (Exception e) {
            Log.e("IssueSvc", "setTimeout err", e);
        }
    }
}
