package com.phantom.onetapvideodownload.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.phantom.onetapvideodownload.R;
import com.phantom.utils.CheckPreferences;
import com.phantom.utils.Global;

public class DonateActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {
    private BillingProcessor mBillingProcessor;
    private String TAG = "DonateActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (CheckPreferences.getDonationStatus(this)) {
            setContentView(R.layout.activity_purchased);
            return;
        }

        setContentView(R.layout.activity_remove_ads);
        mBillingProcessor = new BillingProcessor(this, Global.PUBLIC_LICENSE_KEY, this);

        Button donateButton = (Button) findViewById(R.id.donate_button);
        donateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isAvailable = BillingProcessor.isIabServiceAvailable(DonateActivity.this);
                if (isAvailable) {
                    mBillingProcessor.purchase(DonateActivity.this, Global.DONATION_PRODUCT_IDS.get(0));
                } else {
                    new MaterialDialog.Builder(DonateActivity.this)
                            .title(R.string.google_services_not_found_title)
                            .content(R.string.google_services_not_found_summary)
                            .positiveText(R.string.okay)
                            .show();
                }
            }
        });

        Button restorePurchases = (Button) findViewById(R.id.restore_purchases);
        restorePurchases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isAvailable = BillingProcessor.isIabServiceAvailable(DonateActivity.this);
                if (isAvailable) {
                    if (mBillingProcessor.isInitialized() && mBillingProcessor.loadOwnedPurchasesFromGoogle()) {
                        onPurchaseHistoryRestored();
                        Toast.makeText(DonateActivity.this, getResources().getText(R.string.purchases_loaded), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DonateActivity.this, getResources().getText(R.string.google_services_not_found_title), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */
        Log.i(TAG, "Billing Processor Initialized");
        if (mBillingProcessor.loadOwnedPurchasesFromGoogle()) {
            onPurchaseHistoryRestored();
        }
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */
        new MaterialDialog.Builder(this)
                .title(R.string.donate_thanks_for_donation)
                .content(R.string.donate_thanks_for_donation_summary)
                .positiveText(R.string.okay)
                .show();

        CheckPreferences.setDonationStatus(this, true);
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        /*
         * Called when some error occurred. See Constants class for more details
         */
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
        boolean purchased = false;
        for (String productId : Global.DONATION_PRODUCT_IDS) {
            TransactionDetails transactionDetails = mBillingProcessor.getPurchaseTransactionDetails(productId);
            if (transactionDetails != null) {
                purchased = true;
                break;
            }
        }
        CheckPreferences.setDonationStatus(this, purchased);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (mBillingProcessor != null)
            mBillingProcessor.release();

        super.onDestroy();
    }
}
