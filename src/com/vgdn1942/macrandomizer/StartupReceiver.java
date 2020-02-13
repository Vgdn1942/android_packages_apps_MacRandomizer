package com.vgdn1942.macrandomizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent bootintent) {
        SetMacAddress.restoreOnBoot(context);
    }
}

