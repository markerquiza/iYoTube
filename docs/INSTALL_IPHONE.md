# Install IyoTube on Rokid AI Glasses using an iPhone

Install the IyoTube APK on green-display Rokid AI Glasses using Hi Rokid on your iPhone.

## Before you start

- Have the official **Hi Rokid** iPhone app installed and your glasses paired normally. Use [Rokid’s official app download page](https://global.rokid.com/pages/app-download).
- Update Hi Rokid through the App Store. Keep both devices adequately charged.
- Use a trusted Wi-Fi network for the first playback test. Both devices must be able to communicate with each other.
- This procedure does not require rooting the glasses, replacing firmware or jailbreaking the iPhone.

Community developers document **Hi Rokid → Home → Toolbox → Glasses app management** as an APK installation route. The feature and exact labels can vary with the iPhone app and glasses firmware. These are conditional instructions, not a claim that every Hi Rokid version supports APK upload. See the [community developer installation reference](https://github.com/xnohat/rokid-glass-browser#readme).

## 1. Download the APK on your iPhone

1. Open a successful build for the current version in [GitHub Actions](https://github.com/markerquiza/iYoTube/actions), or save the supplied APK directly to Files and continue to the installation section.
2. Sign in to GitHub if necessary.
3. Scroll to **Artifacts** and select **IyoTube-debug-apk**.
4. Save the downloaded ZIP to Files.
5. Open **Files → Browse → Downloads**, or the folder selected when saving.
6. Tap the ZIP to extract it.
7. Locate **app-debug.apk** in the extracted folder.

Download the APK from this repository’s build artifacts. The repository’s **Code → Download ZIP** button downloads source code, not the installable app.

Build artifacts expire according to GitHub retention settings. If this download has expired, open [Actions](https://github.com/markerquiza/iYoTube/actions), select a newer successful **Build Android APK** run, or use **Run workflow** to generate another build.

## 2. Transfer and install through Hi Rokid

1. Open **Hi Rokid** and confirm your glasses are connected.
2. Open **Home → Toolbox**.
3. Look for **Glasses app management**, **Install app on glasses**, or similar wording.
4. **If an Upload APK or local APK selection option is available**, tap it.
5. In the file picker, select the extracted **app-debug.apk**.
6. Follow the app’s transfer and installation prompts. Keep the devices nearby and powered on. If Hi Rokid requests joining the glasses’ Wi-Fi during the transfer, follow its instructions.
7. Confirm installation if prompted on the glasses.
8. Launch **IyoTube** through the app-management screen’s **Open** option if available, or through the glasses’ app menu.

**If no APK-upload option appears, stop at this point.** Record your Hi Rokid version and glasses firmware version, and share a screenshot of the Toolbox screen so the installation route can be checked. Do not assume an unrelated uploader or an iPhone configuration profile is required.

## 3. Play a video on the glasses

1. Open IyoTube and choose **Wi-Fi settings** to connect the glasses.
2. Choose **Search or open link**, enter a title or URL, and select **Go**.
3. The video uses the available screen area while preserving its proportions. Press **Back** to return to the menu.

Use **Voice search** to dictate a title if your firmware provides Android speech recognition.
