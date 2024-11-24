package com.nhstudio.isettings.quicksettings.iapp.extension;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.widget.Toast;

import java.lang.reflect.Method;

public class PhotorTool {

    public static void sendMail(Context context, String pro) {
        String version = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
        }
        String title = "Feedback iSettings " + "v" + version + pro;
        sendEmailMoree3(context, new String[]{"vunhiem96@gmail.com"}, title);
    }

    public static void sendShareMore(Context context, String filePath) {
        disableExposure();
        Intent share = new Intent(Intent.ACTION_SEND);
        // share.setName("text/plain");
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_TEXT,"Share backup inote");
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///" + filePath));
        context.startActivity(Intent.createChooser(share, "Share backup inote"));
    }

    public static void sendEmailMoree3(
            Context context,
            String[] addresses,
            String subject
    ) {

        disableExposure();
        Intent emailSelectorIntent = new Intent(Intent.ACTION_SENDTO);
        emailSelectorIntent.setData(Uri.parse("mailto:"));
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        // intent.type = "message/rfc822"
        // intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
//            if (uris.size > 0)
//                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)

        intent.putExtra(Intent.EXTRA_TEXT,
                "DEVICE INFORMATION" +
                        "\n\n" + new RateUtil().getDeviceInfo() + "\n\n");
        intent.setSelector(emailSelectorIntent);
//        if (intent.resolveActivity(context.getPackageManager()) != null) {
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Please send mail to vunhiem96@gmail.com", Toast.LENGTH_SHORT).show();
        }

//        } else {
//            Toast.makeText(context, "you need install gmail", Toast.LENGTH_SHORT).show();
//        }
    }

    private static void disableExposure() {
        if (PhotorTool.getSdkVersion() >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int getSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

}
