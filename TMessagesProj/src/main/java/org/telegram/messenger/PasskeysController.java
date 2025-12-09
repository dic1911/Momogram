package org.telegram.messenger;

import android.content.Context;
import android.os.Build;
import android.os.CancellationSignal;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
//import androidx.credentials.CreateCredentialResponse;
//import androidx.credentials.CreatePublicKeyCredentialRequest;
//import androidx.credentials.Credential;
//import androidx.credentials.CredentialManager;
//import androidx.credentials.CredentialManagerCallback;
//import androidx.credentials.GetCredentialRequest;
//import androidx.credentials.GetCredentialResponse;
//import androidx.credentials.GetPublicKeyCredentialOption;
//import androidx.credentials.PrepareGetCredentialResponse;
//import androidx.credentials.exceptions.CreateCredentialCancellationException;
//import androidx.credentials.exceptions.CreateCredentialInterruptedException;
//import androidx.credentials.exceptions.GetCredentialCancellationException;
//import androidx.credentials.exceptions.GetCredentialException;
//import androidx.credentials.exceptions.GetCredentialInterruptedException;
//import androidx.credentials.exceptions.NoCredentialException;

import org.json.JSONObject;
import org.json.JSONStringer;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.LaunchActivity;

import java.util.Arrays;
import java.util.concurrent.Executors;

import kotlin.Result;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.JobCancellationException;

@RequiresApi(api = 28)
public class PasskeysController {

    public static void create(Context context, int currentAccount, Utilities.Callback2<TL_account.Passkey, String> done) {}

    public static Runnable login(Context context, int currentAccount, boolean clickedButton, Utilities.Callback3<Long, TLRPC.auth_Authorization, String> done) {
        return null;
    }

    public static <T> Continuation<T> ktxCallback(Utilities.Callback2<T, Throwable> done) {
        return ktxCallback(EmptyCoroutineContext.INSTANCE, done);
    }

    public static <T> Continuation<T> ktxCallback(CoroutineContext ctx, Utilities.Callback2<T, Throwable> done) {
        return new Continuation<T>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return ctx;
            }

            @Override
            public void resumeWith(@NonNull Object result) {
                if (result instanceof Result.Failure) {
                    done.run(null, ((Result.Failure) result).exception);
                } else {
                    done.run((T) result, null);
                }
            }
        };
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
