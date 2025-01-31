# Momogram

---

Momogram is a feature-rich 3rd-party Telegram client, aiming to be the spiritual successor of the OG NekoX.

Built with unique customization and features, get ready and let Momogram take your Telegram chatting experience to the next level.


## New Features

### General
- Additional translation provider (Lingva)
- Allow chat preview for tablet mode if in portrait mode
- Disable auto login with Telegram account when opening certain links
- Filter languages shown in translate target
- Hide birthday reminders
- Hide various premium upselling annoyance
- In-app anti-spam measure ;)
- Searchable Momogram settings, just like official ones
- Only share "last seen" to the ones you want to check their "last seen"
- Override device performance class
- Remove tracking queries from URL for better privacy
- Restart the app automatically whenever critical problem occurs
- Uncheck "share my number" when adding contact by default

### Chat
- Auto select best video quality if available
- Ban button in join request list
- Block specified chat/channel from search results
- Copy image/sticker button in menu
- Detect data saver and use roaming auto download preset when enabled
- Enable spoiler for all media in specified chats
- Local only custom chat name
- New double tap actions
- Reply as quote by default
- Remove single emoji from recently used list
- Schedule send message after slow mode timer ends without having to wait in the same chat
- Search message by type
- Translate text messages before send
- Translate forwarding text message before send (without quote)
- Use marquee for long chat titles

### Customization
- Always show downloads button in chat list
- Custom folder icon, official and custom emoji(local only)
- Custom title text
- Disable remote chat wallpaper
- Hide unread counter on folder tabs
- Use account name as chat list title

### Mini-app / Webview
- Block bot without deleting history by default
- Comfirm before opening external links from webview
- Custom search engine for in-app browser
- Copy link button in menu
- Don't open bot when re-opening tab
- Don't send "/start" when restarting bot
- Hide bottom overlay when in chat or sharing
- Open webpages (ex. games) with mini-app mode
- Skip confirmation on closing tab
- Toggle to disable geolocation
- Toggle to disable safe browsing


... and more to come! check the [releases page](https://github.com/dic1911/Momogram/releases) for more new features!

---

## FAQ

#### How do I get notifications working?

Momogram has basic UnifiedPush support(enabled by default), but you need to install distributor in order to get it working.

Here are 3 easy choices for you:

- [ntfy](https://github.com/binwiederhier/ntfy-android/releases/latest)
- [Sunup](https://unifiedpush.org/users/distributors/sunup/)
- [UP-FCM Distributor (relies on Google, but faster)](https://codeberg.org/UnifiedPush/fcm-distributor/releases)

Just choose one of the distributor from above, install and open it at least once to finish setup, then choose it in the notification settings page of Momogram and you're good to go!

In case of UnifiedPush doesn't work well on your device, you can disable it from Momo general settings (or just search for UnifiedPush) and enable original background service.

Read [UnifiedPush document](https://unifiedpush.org/users/distributors/) for more info

#### Do you take feature requests?

Yes, but it depends, the more useful your suggestion is to the community, the more likely we'll consider implementing it even when it's a bit complex.

In case of a rejection, you may ask if a PR is acceptable.


#### I've encountered a bug!

First, join the discussion group and search around to see if anyone reported the same bug (or a test build with bug-fix exists).

Then, make sure you have the latest version installed (check the channel and the discussion group for latest fixes).

Then, if the issue appears in the official Telegram client too, please submit it to the officials, (be careful not to show Momogram in the description and screenshots, the official developers doesn't like us!).

Then, please *detail* your issue, submit it to our [linked disscussion group](https://t.me/momogram_update) with #bug.

If you experience a *crash*, you also need to click on the version number at the bottom of the settings and select "Enable Log", reproduce the bug again and send it to us.


#### Can you add support for the proxy protocol X(insert any name here)?

Nope. If you can manage to add it yourself without huge amount of modification, maybe we could talk.


#### Why Momogram as the name?

There are too many people that mixes us with Nekogram and the old good NekoX(Nekogram X), so I chose to rename the project.
And for "Momo", it's named after Momoi from Blue Archive, stripped out the "i" for easier pronunciation.


---

## Localization

Is Momogram not in your language, or the translation is incorrect or incomplete? Get involved in the translations on our [Weblate](https://hosted.weblate.org/engage/nekox_030/).

[![Translation status](https://hosted.weblate.org/widgets/nekox_030/-/horizontal-auto.svg)](https://hosted.weblate.org/engage/nekox_030/)

---


## Compilation Guide

**NOTE: Building on Windows is not supported.
Consider using a Linux VM, WSL or dual booting.**

**Important:**

0. Checkout all submodules
```
git submodule update --init --recursive
```

1. Install Android SDK and NDK (default location is $HOME/Android/SDK, otherwise you need to specify $ANDROID_HOME for it)

It is recommended to use [AndroidStudio](https://developer.android.com/studio) to install.

2. Install yasm
```shell
apt install -y yasm
```

3. Build native dependencies: `./run init libs`
4. Build external libraries and native code: `./run libs update`
5. Fill out `TELEGRAM_APP_ID` and `TELEGRAM_APP_HASH` in `local.properties`
6. Replace TMessagesProj/google-services.json if you want fcm to work.
7. Replace release.keystore with yours and fill out `ALIAS_NAME`, `KEYSTORE_PASS` and `ALIAS_PASS` in `local.properties` if you want a custom sign key.

`./gradlew assemble<Debug/Release>`


## Credits

<ul>
    <li>Telegram-FOSS: <a href="https://github.com/Telegram-FOSS-Team/Telegram-FOSS/blob/master/LICENSE">GPLv2</a></li>
    <li>Nekogram X: GPLv3</li>
    <li>Nekogram: <a href="https://gitlab.com/Nekogram/Nekogram/blob/master/LICENSE">GPLv2</a></li>
    <li>Nagram: <a href="https://github.com/NextAlone/Nagram/blob/master/LICENSE">GPLv3</a></li>
    <li>Cherrygram: <a href="https://github.com/NextAlone/Nagram/blob/master/LICENSE">GPLv2</a></li>
    <li>Telegram-Monet: <a href="https://github.com/mi-g-alex/Telegram-Monet/blob/main/LICENSE">EPL-2.0</a></li>
    <li>HanLP: <a href="https://github.com/hankcs/HanLP/blob/1.x/LICENSE">Apache License 2.0</a></li>
    <li>OpenCC: <a href="https://github.com/BYVoid/OpenCC/blob/master/LICENSE">Apache License 2.0</a></li>
    <li>opencc-data: <a href="https://github.com/nk2028/opencc-data">Apache License 2.0</a></li>
    <li>android-device-list: <a href="https://github.com/pbakondy/android-device-list/blob/master/LICENSE">MIT</a> </li>
    <li>JetBrains: for allocating free open-source licences for IDEs</li>
</ul>

[<img src=".github/jetbrains-variant-3.png" width="200"/>](https://jb.gg/OpenSource)


## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=dic1911/Momogram&type=Date)](https://star-history.com/#dic1911/Momogram&Date)