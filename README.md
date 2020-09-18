# cordova-plugin-sim

[![npm](https://img.shields.io/npm/v/cordova-plugin-sim.svg)](https://www.npmjs.com/package/cordova-plugin-sim)
[![Code Climate](https://codeclimate.com/github/pbakondy/cordova-plugin-sim/badges/gpa.svg)](https://codeclimate.com/github/pbakondy/cordova-plugin-sim)
![Platform](https://img.shields.io/badge/platform-android%20%7C%20ios%20%7C%20windows-lightgrey.svg)
[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=R7STJ6V2PNEMA)

This is a cordova plugin to get data from the SIM card like the carrier name, mcc, mnc and country code and other system dependent additional info.


## Installation

```
cordova plugin add https://github.com/zhangYiDa123/cordova-plugin-sim.git
```

## Supported Platforms

- Android


## Usage

```js

declare let cordova: any;
declare let window: any;
// 获取权限
window.plugins.sim.requestReadPermission((e: any) => { }, () => {});
window.plugins.sim.hasReadPermission((e: any) => { setTimeout(() => {this.getCodeInfo(); }, 30); }, () => {});

// 获取手机四码信息
getCodeInfo() {
    const successCallback = (e: any) => {
      this.deviceInfo.phoneCount = e.phoneCount;
      if (this.deviceInfo.phoneCount === 2) { // 双卡
      this.deviceInfo.systemVersion = e.systemVersion;
      this.deviceInfo.MEID = e.meid;
      this.deviceInfo.MAC = e.mac;
      this.deviceInfo.PHONE1 = e.phoneNumber;
      this.deviceInfo.IMEI1 = e.deviceId;
      this.deviceInfo.SIM1 = e.subscriberId;
      this.deviceInfo.PHONE2 = e.cards[1].phoneNumber;
      this.deviceInfo.IMEI2 = e.cards[1].deviceId;
      this.deviceInfo.SIM2 = e.cards[1].subscriberId;
    } else { // 单卡
      this.deviceInfo.PHONE1 = e.phoneNumber;
      this.deviceInfo.IMEI1 = e.deviceId;
      this.deviceInfo.SIM1 = e.subscriberId;
      this.deviceInfo.MAC = e.mac;
      this.deviceInfo.MEID = e.meid;
      this.deviceInfo.systemVersion = e.systemVersion;
    }
  };

    const errorCallback = (e: any) => {
      this.message = {
        message: '失败',
        time: 2000,
        state: true
      };
  };

  // 再次获取权限
    if (window.plugins) {
      Sim.getSimInfo().then(
        (info) => successCallback(info),
        (err) => errorCallback(err)
      );
  }
}
```
## Author

#### zhangyida

- https://github.com/zhangYiDa123/cordova-plugin-sim.git
