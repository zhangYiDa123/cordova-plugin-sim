// MCC and MNC codes on Wikipedia
// http://en.wikipedia.org/wiki/Mobile_country_code

// Mobile Network Codes (MNC) for the international identification plan for public networks and subscriptions
// http://www.itu.int/pub/T-SP-E.212B-2014

// class TelephonyManager
// http://developer.android.com/reference/android/telephony/TelephonyManager.html
// https://github.com/android/platform_frameworks_base/blob/master/telephony/java/android/telephony/TelephonyManager.java

// permissions
// http://developer.android.com/training/permissions/requesting.html

// Multiple SIM Card Support
// https://developer.android.com/about/versions/android-5.1.html

// class SubscriptionManager
// https://developer.android.com/reference/android/telephony/SubscriptionManager.html
// https://github.com/android/platform_frameworks_base/blob/master/telephony/java/android/telephony/SubscriptionManager.java

// class SubscriptionInfo
// https://developer.android.com/reference/android/telephony/SubscriptionInfo.html
// https://github.com/android/platform_frameworks_base/blob/master/telephony/java/android/telephony/SubscriptionInfo.java

// Cordova Permissions API
// https://cordova.apache.org/docs/en/latest/guide/platforms/android/plugin.html#android-permissions

package com.pbakondy;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.LOG;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;

import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.annotation.SuppressLint;

import java.util.List;
import java.net.NetworkInterface;
import java.util.Collections;
import java.lang.reflect.Method;
import java.lang.Class;
import java.lang.reflect.InvocationTargetException;
import java.lang.NoSuchMethodException;
import java.lang.IllegalAccessException;
import java.lang.ClassNotFoundException;

public class Sim extends CordovaPlugin {
  private static final String LOG_TAG = "CordovaPluginSim";
  private static final String GET_SIM_INFO = "getSimInfo";
  private static final String HAS_READ_PERMISSION = "hasReadPermission";
  private static final String REQUEST_READ_PERMISSION = "requestReadPermission";

    /*
      * 获取MEID
    */
    private String getMEID(){
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getMethod("get", String.class, String.class);

            String meid = (String) method.invoke(null, "ril.cdma.meid", "");
            if(!TextUtils.isEmpty(meid)){
                LOG.d(LOG_TAG,"getMEID meid: "+ meid);
                return meid;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            e.printStackTrace();
            LOG.w(LOG_TAG,"getMEID error : "+ e.getMessage());
        }
        return "";
    }
    /**
    * 获取系统版本号
    */
    public static String getSdkVersion() {
      return android.os.Build.VERSION.RELEASE;
    }

    /**
     * Get the device's Mac Address.
     *
     * @return
     */
    public String getMacAddress() {
        // String macAddress = null;
        // WifiManager wm = (WifiManager) this.cordova.getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // macAddress = wm.getConnectionInfo().getMacAddress();
        //  if (macAddress == null || macAddress.length() == 0) {
        //     macAddress = "00:00:00:00:00:00";
        // }
        // return macAddress;
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
 
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
 
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }
 
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    };

  /**
     * 通过反射调取@hide的方法
     *
     * @param predictedMethodName 方法名
     * @param id                  参数
     * @return 返回方法调用的结果
     * @throws MethodNotFoundException 方法没有找到
     */
    private static String getReflexMethodWithId(Context context, String predictedMethodName, String id) throws MethodNotFoundException {
        String result = null;
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);
            Class<?>[] parameterTypes = getSimID.getParameterTypes();
            Object[] obParameter = new Object[parameterTypes.length];
            if (parameterTypes[0].getSimpleName().equals("int")) {
                obParameter[0] = Integer.valueOf(id);
            } else if (parameterTypes[0].getSimpleName().equals("long")) {
                obParameter[0] = Long.valueOf(id);
            } else {
                obParameter[0] = id;
            }
            Object ob_phone = getSimID.invoke(telephony, obParameter);
            if (ob_phone != null) {
                result = ob_phone.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MethodNotFoundException(predictedMethodName);
        }
        return result;
    };
  /**
    * 获取IMSI码
    * <p>需添加权限 {@code <uses-permission android:name="android.permission.READ_PHONE_STATE"/>}</p>
    *
    * @return IMSI码
    */
  @SuppressLint("HardwareIds")
  public static String getIMSI(Context context) {
      TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
      try {
          return tm != null ? tm.getSubscriberId() : null;
      } catch (Exception ignored) {
      }
      return null;
  };
  
  /**
    * 反射未找到方法
    */
  private static class MethodNotFoundException extends Exception {

      public static final long serialVersionUID = -3241033488141442594L;

      MethodNotFoundException(String info) {
          super(info);
      }
  };

    public static String getSecondIMSI(Context context) {
        int maxCount = 20;
        if (TextUtils.isEmpty(getIMSI(context))) {
            return null;
        }
        for (int i = 0; i < maxCount; i++) {
            String imsi = null;
            try {
                imsi = getReflexMethodWithId(context, "getSubscriberId", String.valueOf(i));
            } catch (MethodNotFoundException ignored) {
                ignored.printStackTrace();
            }
            if (!TextUtils.isEmpty(imsi) && !imsi.equals(getIMSI(context))) {
                return imsi;
            }
        }
        return null;
    };

  private CallbackContext callback;

  @SuppressLint("HardwareIds")
  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    callback = callbackContext;

    if (GET_SIM_INFO.equals(action)) {
      Context context = this.cordova.getActivity().getApplicationContext();

      TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

      // dual SIM detection with SubscriptionManager API
      // requires API 22
      // requires permission READ_PHONE_STATE
      JSONArray sims = null;
      Integer phoneCount = null;
      Integer activeSubscriptionInfoCount = null;
      Integer activeSubscriptionInfoCountMax = null;

      try {
        // TelephonyManager.getPhoneCount() requires API 23
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
          phoneCount = manager.getPhoneCount();
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {

          if (simPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {

            SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            activeSubscriptionInfoCount = subscriptionManager.getActiveSubscriptionInfoCount();
            activeSubscriptionInfoCountMax = subscriptionManager.getActiveSubscriptionInfoCountMax();

            sims = new JSONArray();

            List<SubscriptionInfo> subscriptionInfos = subscriptionManager.getActiveSubscriptionInfoList();
            for (SubscriptionInfo subscriptionInfo : subscriptionInfos) {

              CharSequence carrierName = subscriptionInfo.getCarrierName();
              String countryIso = subscriptionInfo.getCountryIso();
              int dataRoaming = subscriptionInfo.getDataRoaming();  // 1 is enabled ; 0 is disabled
              CharSequence displayName = subscriptionInfo.getDisplayName();
              String iccId = subscriptionInfo.getIccId();
              int mcc = subscriptionInfo.getMcc();
              int mnc = subscriptionInfo.getMnc();
              String number = subscriptionInfo.getNumber();
              int simSlotIndex = subscriptionInfo.getSimSlotIndex();
              int subscriptionId = subscriptionInfo.getSubscriptionId();

              boolean networkRoaming = subscriptionManager.isNetworkRoaming(simSlotIndex);

              String deviceId = null;

              // TelephonyManager.getDeviceId(slotId) requires API 23
              if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                deviceId = manager.getDeviceId(simSlotIndex);
              }

              JSONObject simData = new JSONObject();

              simData.put("carrierName", carrierName.toString());
              simData.put("displayName", displayName.toString());
              simData.put("countryCode", countryIso);
              simData.put("mcc", mcc);
              simData.put("mnc", mnc);
              simData.put("isNetworkRoaming", networkRoaming);
              simData.put("isDataRoaming", (dataRoaming == 1));
              simData.put("simSlotIndex", simSlotIndex);
              simData.put("phoneNumber", number);
              if (deviceId != null) {
                simData.put("deviceId", deviceId);
              }
              simData.put("simSerialNumber", iccId);
              simData.put("subscriptionId", subscriptionId);

              if(simSlotIndex == 0){
                simData.put("subscriberId", manager.getSubscriberId());
              } else {
                simData.put("subscriberId", getSecondIMSI(context));
              }

              sims.put(simData);

            }
          }
        }
      } catch (JSONException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }

      String phoneNumber = null;
      String countryCode = manager.getSimCountryIso();
      String simOperator = manager.getSimOperator();
      String carrierName = manager.getSimOperatorName();

      String deviceId = null;
      String deviceSoftwareVersion = null;
      String simSerialNumber = null;
      String subscriberId = null;

      int callState = manager.getCallState();
      int dataActivity = manager.getDataActivity();
      int networkType = manager.getNetworkType();
      int phoneType = manager.getPhoneType();
      int simState = manager.getSimState();

      boolean isNetworkRoaming = manager.isNetworkRoaming();

      if (simPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
        phoneNumber = manager.getLine1Number();
        deviceId = manager.getDeviceId();
        deviceSoftwareVersion = manager.getDeviceSoftwareVersion();
        simSerialNumber = manager.getSimSerialNumber();
        subscriberId = manager.getSubscriberId();
      }

      String mcc = "";
      String mnc = "";

      if (simOperator.length() >= 3) {
        mcc = simOperator.substring(0, 3);
        mnc = simOperator.substring(3);
      }

      JSONObject result = new JSONObject();

      result.put("carrierName", carrierName);
      result.put("countryCode", countryCode);
      result.put("mcc", mcc);
      result.put("mnc", mnc);

      result.put("callState", callState);
      result.put("dataActivity", dataActivity);
      result.put("networkType", networkType);
      result.put("phoneType", phoneType);
      result.put("simState", simState);

      result.put("isNetworkRoaming", isNetworkRoaming);
      result.put("mac", getMacAddress());
      result.put("meid", getMEID());
      result.put("systemVersion",getSdkVersion());
      if (phoneCount != null) {
        result.put("phoneCount", (int)phoneCount);
      }
      if (activeSubscriptionInfoCount != null) {
        result.put("activeSubscriptionInfoCount", (int)activeSubscriptionInfoCount);
      }
      if (activeSubscriptionInfoCountMax != null) {
        result.put("activeSubscriptionInfoCountMax", (int)activeSubscriptionInfoCountMax);
      }

      if (simPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
        result.put("phoneNumber", phoneNumber);
        result.put("deviceId", deviceId);
        result.put("deviceSoftwareVersion", deviceSoftwareVersion);
        result.put("simSerialNumber", simSerialNumber);
        result.put("subscriberId", subscriberId);
      }

      if (sims != null && sims.length() != 0) {
        result.put("cards", sims);
      }

      callbackContext.success(result);

      return true;
    } else if (HAS_READ_PERMISSION.equals(action)) {
      hasReadPermission();
      return true;
    } else if (REQUEST_READ_PERMISSION.equals(action)) {
      requestReadPermission();
      return true;
    } else {
      return false;
    }
  }

  private void hasReadPermission() {
    this.callback.sendPluginResult(new PluginResult(PluginResult.Status.OK,
      simPermissionGranted(Manifest.permission.READ_PHONE_STATE)));
  }

  private void requestReadPermission() {
    requestPermission(Manifest.permission.READ_PHONE_STATE);
  }

  private boolean simPermissionGranted(String type) {
    if (Build.VERSION.SDK_INT < 23) {
      return true;
    }
    return cordova.hasPermission(type);
  }

  private void requestPermission(String type) {
    LOG.i(LOG_TAG, "requestPermission");
    if (!simPermissionGranted(type)) {
      cordova.requestPermission(this, 12345, type);
    } else {
      this.callback.success();
    }
  }

  @Override
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException
  {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      this.callback.success();
    } else {
      this.callback.error("Permission denied");
    }
  }
}
