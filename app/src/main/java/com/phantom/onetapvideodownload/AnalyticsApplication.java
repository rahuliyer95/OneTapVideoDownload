/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.phantom.onetapvideodownload;

import android.app.Application;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.evernote.android.job.JobManager;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.phantom.HookFetchJob;
import com.phantom.HookFetchJobCreator;
import com.phantom.utils.CheckPreferences;
import com.phantom.utils.Global;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
public class AnalyticsApplication extends Application implements BillingProcessor.IBillingHandler {
    private static boolean activityVisible;
    private Tracker mTracker;
    private BillingProcessor mBillingProcessor;

    @Override
    public void onCreate() {
        super.onCreate();

        JobManager.create(this).addJobCreator(new HookFetchJobCreator());
        int noOfJobRequests = JobManager.instance().getAllJobRequestsForTag(HookFetchJob.TAG).size();

        // No of Job Requests for HookFetchJob would be greater than 1 for older version of
        // application because of rescheduling of job again and again, causing large no. of wakelocks
        if (noOfJobRequests > 1) {
            JobManager.instance().cancelAllForTag(HookFetchJob.TAG);
            noOfJobRequests = 0;
        }

        if (noOfJobRequests == 0) {
            HookFetchJob.scheduleJob();
        }

        mBillingProcessor = new BillingProcessor(this, Global.PUBLIC_LICENSE_KEY, this);
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
            mTracker.enableAdvertisingIdCollection(true);
        }
        return mTracker;
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }


    @Override
    public void onBillingInitialized() {
        if (mBillingProcessor.loadOwnedPurchasesFromGoogle()) {
            onPurchaseHistoryRestored();
        }
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
    }

    @Override
    public void onPurchaseHistoryRestored() {
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
}
