package com.vgdn1942.macrandomizer;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class SetMacAddress extends Activity {

    TextView mCurrentMac, mCurrentHost;
    Button mSetMac, mRandomeMac, mRestoreMac, mSetHost, mRestoreHost;

    private static final String wifiNvramFile = "/data/nvram/APCFG/APRDEB/WIFI";
    private static final String wifiNvramTmpFile = "/data/nvram/APCFG/APRDEB/WIFI_tmp";
    private static final String wifiNvramBakFile = "/data/nvram/APCFG/APRDEB/WIFI_bak";
    private static final String HOST_PROPERTY = "net.hostname";
    private static final String HOST_PROPERTY_SYS = "persist.sys.hostname";
    private static final String DEFAULT_HOST_NAME = "BV6000";

    private WifiManager mWifiManager;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.set_mac);

        mCurrentMac = (TextView) findViewById(R.id.current_mac);
        mCurrentHost = (TextView) findViewById(R.id.current_host);

        mSetMac = (Button) findViewById(R.id.set_mac_button);
        mRandomeMac = (Button) findViewById(R.id.randome_mac_button);
        mRestoreMac = (Button) findViewById(R.id.restore_mac_button);
        mSetHost = (Button) findViewById(R.id.set_host_button);
        mRestoreHost = (Button) findViewById(R.id.restore_host_button);

        mSetMac.setOnClickListener(mButtonListener);
        mRandomeMac.setOnClickListener(mButtonListener);
        mRestoreMac.setOnClickListener(mButtonListener);
        mSetHost.setOnClickListener(mButtonListener);
        mRestoreHost.setOnClickListener(mButtonListener);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }

    final OnClickListener mButtonListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == mSetMac) {
                final String curMac = mCurrentMac.getText().toString();
                final String secondCh = String.valueOf(curMac.charAt(1));
                if (curMac.length() < 12) {
                    Toast.makeText(SetMacAddress.this,
                            R.string.short_mac_error,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!curMac.matches("^([0-9A-Fa-f]{2})+$")) {
                    Toast.makeText(SetMacAddress.this,
                            R.string.hex_mac_error,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (secondCh.matches("[13579BbDdFf]+")) {
                    Toast.makeText(SetMacAddress.this,
                            R.string.odd_mac_error,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(SetMacAddress.this,
                        R.string.set_mac_ok,
                        Toast.LENGTH_SHORT).show();
                setMac(false, curMac);
                handleWifiStateChanged(true);
                return;
            }
            if (view == mRandomeMac) {
                Toast.makeText(SetMacAddress.this,
                        R.string.set_random_mac_ok,
                        Toast.LENGTH_SHORT).show();
                setMac(true, null);
                handleWifiStateChanged(true);
                return;
            }
            if (view == mRestoreMac) {
                if (isFileExist(wifiNvramBakFile)) {
                    copyTo(wifiNvramBakFile, wifiNvramFile);
                    copyTo(wifiNvramBakFile, wifiNvramTmpFile);
                    Toast.makeText(SetMacAddress.this,
                            R.string.restore_mac_ok,
                            Toast.LENGTH_SHORT).show();
                    handleWifiStateChanged(false);
                } else {
                    Toast.makeText(SetMacAddress.this,
                            R.string.restore_mac_error,
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
            if (view == mSetHost) {
                final String curHost = mCurrentHost.getText().toString();
                Toast.makeText(SetMacAddress.this,
                        R.string.set_host_ok,
                        Toast.LENGTH_SHORT).show();
                SystemProperties.set(HOST_PROPERTY, curHost);
                SystemProperties.set(HOST_PROPERTY_SYS, curHost);
                handleWifiStateChanged(false);
                return;
            }
            if (view == mRestoreHost) {
                Toast.makeText(SetMacAddress.this,
                        R.string.restore_host_ok,
                        Toast.LENGTH_SHORT).show();
                SystemProperties.set(HOST_PROPERTY, DEFAULT_HOST_NAME);
                SystemProperties.set(HOST_PROPERTY_SYS, DEFAULT_HOST_NAME);
                handleWifiStateChanged(false);
            }
        }
    };

    private void setMac(boolean isRandomMac, String bytesMac) {
        byte[] bytesToWrite;

        try (FileInputStream fileInput = new FileInputStream(wifiNvramFile);
             FileOutputStream fileOutput = new FileOutputStream(wifiNvramTmpFile)) {

            byte[] buffer = new byte[fileInput.available()];

            if (isRandomMac) {
                bytesToWrite = hexStringToByteArray(getRandomHexMac(12));
            } else {
                bytesToWrite = hexStringToByteArray(bytesMac);
            }

            // считываем из файла в буфер buffer:
            fileInput.read(buffer, 0, buffer.length);

            // записываем первые 4 байта из буфера в файл
            for (int i = 0; i < 4; i++) {
                fileOutput.write(buffer[i]);
            }

            // записываем 6 байт MAC адреса в файл
            fileOutput.write(bytesToWrite, 0, bytesToWrite.length);

            // записываем оставшиеся байты с 10 до конца из буфера в файл
            for (int i = 10; i < buffer.length; i++) {
                fileOutput.write(buffer[i]);
            }
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void copyTo(String nameFrom, String nameTo) {
        try (FileInputStream fileInput = new FileInputStream(nameFrom);
             FileOutputStream fileOutput = new FileOutputStream(nameTo)) {

            byte[] buffer = new byte[fileInput.available()];

            // считываем из файла в буфер buffer:
            fileInput.read(buffer, 0, buffer.length);

            // пишем из буфера buffer в файл:
            for (int i = 0; i < buffer.length; i++) {
                fileOutput.write(buffer[i]);
            }
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void handleWifiStateChanged(boolean copy) {
        final int state = mWifiManager.getWifiState();
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
            case WifiManager.WIFI_STATE_ENABLED:
                SystemClock.sleep(250);
                mWifiManager.setWifiEnabled(false);
                SystemClock.sleep(250);
                if (copy) {
                    copyTo(wifiNvramTmpFile, wifiNvramFile);
                }
                mWifiManager.setWifiEnabled(true);
                break;
            case WifiManager.WIFI_STATE_DISABLING:
            case WifiManager.WIFI_STATE_DISABLED:
            case WifiManager.WIFI_STATE_UNKNOWN:
                if (copy) {
                    copyTo(wifiNvramTmpFile, wifiNvramFile);
                }
                break;
        }
    }

    private static void backup() {
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return;
        }
        // получаем путь к SD
        String fileBackupSd = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "WIFI_bak";

        if (!isFileExist(wifiNvramBakFile)) {
            copyTo(wifiNvramFile, wifiNvramBakFile);
        }
        if (!isFileExist(fileBackupSd)) {
            copyTo(wifiNvramFile, fileBackupSd);
        }
    }

    public static void restoreOnBoot(Context context) {
        backup();
    }

    private static boolean isFileExist(String filePath) {
        return (new File(filePath)).exists();
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private String getRandomHexMac(int length) {
        String b = "null";
        do {
            String s = getRandomHexString(length);
            String c = String.valueOf(s.charAt(1));
            if (!c.matches("[13579BbDdFf]+")) {
                b = s;
            }
        } while (b.equals("null"));
        return b;
    }

    private String getRandomHexString(int length) {
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while (sb.length() < length) {
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, length);
    }

    /*
    private byte[] randomByte(int length) {
        byte[] array = new byte[length];
        new Random().nextBytes(array);
        return array;
    }
    */
}

