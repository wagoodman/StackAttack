package com.wagoodman.stackattack.full;
import com.wagoodman.stackattack.StackAttackBase;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;



public class StackAttack extends StackAttackBase
{

	// LIBRARY VAR HOOKS
	public final Boolean isPaid = true;
	public final int mFreeVersionMaxPoints = -1;
	
	@Override
	public Boolean getIsPaid() { return isPaid; }
	
	@Override
	public int getFreeVersionMaxPoints() { return mFreeVersionMaxPoints; }

	
	// LICENSING

	public AlertDialog unlicensedDialog;
	
	static boolean licensed = true;
	static boolean didCheck = false;
	static boolean checkingLicense = false;
	static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAg"+
			"DOybCLjBDeDSybjBjr1N36fulaH1j18Hzlu+OqlANC/DSExAiKgSitXgN2e2uMAnnRscj2QEv/jTC"+
			"+B6+fhGy/6aQs2FR9GqGnJKjkrhJRnKkY6MATIpi5Vh49+uC8SbDqdojVg2rZVWUx7VGr/5Tl7PQKV"+
			"ymmENdUDiLy8ZGSR8hjOFu9YH0sF6x/JX31EgYraP6e4T72Q8KY6Szlf5833WewUXct67/LfQt+muZ"+
			"cZ+AQNS0xo9gK7UWZyUMhFbdZCBMlxJHQuOMvp7HBs6FmdGfzFLieM3+FcUZIVgAKXoe1Vbysbxnco"+
			"0VhgkmuYdKdKlaeHh5MO0X8oIFRM0wIDAQAB";

	LicenseCheckerCallback mLicenseCheckerCallback;
	LicenseChecker mChecker;

	Handler mHandler;

	SharedPreferences prefs;

	private static final byte[] SALT = new byte[] {42,84,72,-67,-89,-87,-89,-13,37,47,-92,-13,37,39,30,-55,32,32,-37,92,-43,30,-93,33,-32,-37,-99,-16,19,87,42};

	private void displayResult(final String result) {
	    mHandler.post(new Runnable() {
	        public void run() {
	            setProgressBarIndeterminateVisibility(false);
	        }
	    });
	}

	protected void doCheck() {

	    didCheck = false;
	    checkingLicense = true;
	    setProgressBarIndeterminateVisibility(true);

	    mChecker.checkAccess(mLicenseCheckerCallback);
	}

	protected void checkLicense() {

	    mHandler = new Handler();

	    // Try to use more data here. ANDROID_ID is a single point of attack.
	    String deviceId = Settings.Secure.getString(getContentResolver(),
	            Settings.Secure.ANDROID_ID);

	    // Library calls this when it's done.
	    mLicenseCheckerCallback = new MyLicenseCheckerCallback();
	    
	    // Construct the LicenseChecker with a policy.
	    mChecker = new LicenseChecker(this, new ServerManagedPolicy(this,
	            new AESObfuscator(SALT, getPackageName(), deviceId)),
	            BASE64_PUBLIC_KEY);

	    doCheck();
	}

	protected class MyLicenseCheckerCallback implements LicenseCheckerCallback {

	    public void allow() {
	        //Log.e("LICENSE", "Allow");
	        if (isFinishing()) {
	            // Don't update UI if Activity is finishing.
	            return;
	        }
	        
	        // Should allow user access.
	        licensed = true;
	        checkingLicense = false;
	        didCheck = true;

	    }

	    public void dontAllow() {
	        //Log.e("LICENSE", "DONT ALLOW!!!");
	        if (isFinishing()) {
	            // Don't update UI if Activity is finishing.
	            return;
	        }
	        
	        licensed = false;
	        checkingLicense = false;
	        didCheck = true;
	        
	        // Should not allow access. In most cases, the app should assume
	        // the user has access unless it encounters this. If it does,
	        // the app should inform the user of their unlicensed ways
	        // and then either shut down the app or limit the user to a
	        // restricted set of features.
	        // In this example, we show a dialog that takes the user to Market.

	        unlicensedDialog = dialogBuilder.setTitle("Unlicensed App")
            .setMessage("Sorry, it appears that this copy of the app may be unlicensed. If you feel this is in error please contact the developer.")
            .setPositiveButton("Buy It",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int which) {
                            Intent marketIntent = new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://market.android.com/details?id="
                                            + getPackageName()));
                            startActivity(marketIntent);
                            finish();
                        }
                    })
            .setNegativeButton("Quit",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int which) {
                            finish();
                        }
                    })

            .setCancelable(false)
            .setOnKeyListener(new DialogInterface.OnKeyListener() {
                public boolean onKey(DialogInterface dialogInterface,
                        int i, KeyEvent keyEvent) {
                    //Log.i("License", "Key Listener");
                    finish();
                    return true;
                }
            }).create();
	        
	        unlicensedDialog.show();
	    }

	    public void applicationError(int errorCode) {
	        if (isFinishing()) {
	            // Don't update UI if Activity is finishing.
	            return;
	        }
	        licensed = false;
	        checkingLicense = false;
	        didCheck = true;
	        // This is a polite way of saying the developer made a mistake
	        // while setting up or calling the license checker library.
	        // Please examine the error code and fix the error.
	        String result = String.format( "Woops! Seems that the developer messed up! ErrorCode:%d", errorCode);
	        displayResult(result);
	    }

	    public void allow(int reason) {
	    	allow();
	    }

	    public void dontAllow(int reason) {
	    	dontAllow();
	    }

	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check the license
        checkLicense();
    }
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    if (mChecker != null) {
	        //Log.i("LIcense", "distroy checker");
	        mChecker.onDestroy();
	    }
	}
	
	
	
}