Very simple android app to showcase [the android cognito SDK issue](https://github.com/aws/aws-sdk-android/issues/259) with refreshing of tokens
occuring when user was signed in using email+password combination.

Note: The app itself doesn't show much on the screen. For exceptions etc, observe logs via Console / Android Monitor / adb logcat.

**Things to try**:

1) Navigate into `IssueService` and provide valid values for `poolId`, `clientId`, `clientSecret`, `userEmail`, `username`, `password` in the constructor.

2) run the app, peform `Try login/api call`. You'll see md5s of tokens got filled in.

3) uncheck `App knows credentials` to simulate the situation where the app no longer knows the password.

4) click `Try login/api call` - everything should be working, as we still have tokens which didn't timeout

5) click `simulate timeouting refreshtreshold`. This will result in `cachedTokens.isValidForThreshold()`
   being evaluated as false in `CognitoUser` and therefore refresh of the token will be attempted. It will
   fail (more information in [the github issue](https://github.com/aws/aws-sdk-android/issues/259)).

6) (you might want to revert the refreshThreshold back to its original value by clicking "reset default refreshThreshold" button before doing any other tests)

You can also try the same scenario with `Login using email` checkbox uncheched. This will result in `username+password`
combination being used when signing in instead of `email+password`. When The user signs in using using username,
the mentioned scenario. Step 6 will actually result in access token being changed, while the refresh token remains the same (ie expected behavior).

