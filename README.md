# A13_DIRECTBOOT_MODE / TARGET ELEVATOR
### com.ndzl.targetelevator 2024

- RUN ON A14 on Dec.5, 2024
  - use https://cxnt48.com/emergency to set/unset an emergency
  - pull the audio recording using `adb pull /enterprise/usr/persist/mic-audio.3gp`
  - Direct boot mode (DBM) file writing and audio capturing works fine on A14
  - Overlay window linked to an emergency, not yet working in A14 DBM

EMERGENCY BUTTON PoC AND AUDIO RECORDING
- see the Release page for instructions

---

TESTING
- A BOOT-AWARE APP WITH FBE FILESYSTEM IN A13
- DATAWEDGE INTENT DELIVER TO BACKGROUND AND FOREGROUND SERVICE
- AUDIO RECORDING THROUGH SYSTEM MICROPHONE

## PURPOSE OF THIS APP IS SHOWING WHAT SCREEN LOCK MODE IS TRIGGERING THE CREDENTIAL ENCRYPTED STORAGE PROTECTION.
SCREEN LOCK MODES ARE

![image](https://user-images.githubusercontent.com/11386676/222977690-7414560a-5eca-484d-a048-0542781671a1.png)

ON A TC58 BSP 13-08-07 IT TURNS OUT THAT
- WITH **NONE, SWIPE, PATTERN** A BOOT-AWARE APP IS ABLE TO ACCESS BOTH THE DPS (DEVICE PROTECTED STORAGE) AND THE CES (CREDENTIAL ENCRYPTED STORAGE) BEFORE THE FIRST UNLOCK AFTER REBOOT
- WITH **PIN AND PASSWORD** A BOOT-AWARE APP *IS* ABLE TO ACCESS DPS WHILE IT *CANNOT* ACCESS CES BEFORE THE FIRST UNLOCK AFTER REBOOT

Here is a screenshot from the app. Where you spot an exception under the DPS section, that is failed attempt to access CES after boot. It was logged under DPS since that context is always available.

![image](https://user-images.githubusercontent.com/11386676/222977925-ca15cd47-b55b-41db-a0e7-2e550eb67dc4.png)

A11 ASKING THE USER ABOUT THE SECURE START-UP SETUP, it was not mandatory yet
No such option is available on A13. Secure start-up is always enabled with PIN or Password screen lock

![image](https://user-images.githubusercontent.com/11386676/223754949-4c1727da-fc80-4f2e-a74b-33d1a043cb0b.png)

