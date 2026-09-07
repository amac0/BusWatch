# BusWatch: Play Store rollout, next steps

Status on September 7, 2026: code, legal documents, signed bundle, and store assets are
done. Everything below happens in Play Console. Work through it in order, because the
console blocks "Send for review" until every card is complete.

## Already done

- Privacy Policy and Terms published at
  https://amac0.github.io/BusWatch/privacy-policy.html and
  https://amac0.github.io/BusWatch/terms-of-service.html (sources: `PrivacyPolicy.md`,
  `TermsOfService.md`; rebuild HTML with `scripts/build-legal-html.sh`).
- In-app "About & privacy" screen linking both documents (a Play requirement).
- Signed release bundle: `../keystore/BusWatch-1.0.0-versionCode2.aab` (versionCode 2,
  versionName 1.0.0). Upload keystore and passwords are in `../keystore/`; back that
  folder up privately and never commit it. Rebuild with
  `source ../keystore/buswatch-upload.env && ./gradlew :app:bundleRelease`, bumping
  `versionCode` in `app/build.gradle.kts` first.
- Store assets in `docs/store-assets/`: `app-icon-512.png`, `feature-graphic.png`, and
  four 912 x 912 Wear OS screenshots. Listing text in `docs/store-listing.md`.

## Step 3: Create the app

1. Sign in at https://play.google.com/console with the Attention Feed, Inc. developer
   account.
2. Create app: name "BusWatch", default language English (UK), type App, Free. Accept the
   declarations.
3. Skip the "Set up your app" checklist; each item is covered below.

If the developer account's own verification (D-U-N-S number, address) is still pending,
the console says so here and the release waits on it.

## Step 4: App content

Policy and programs, App content. Complete every card:

- **Privacy policy:** `https://amac0.github.io/BusWatch/privacy-policy.html`.
- **Ads:** No, the app contains no ads.
- **App access:** all functionality is available without special access. There is no
  login and TfL data is public, so no test account is needed.
- **Content ratings:** questionnaire, category Utility, answer No to everything. Result is
  Everyone / PEGI 3.
- **Target audience and content:** 18 and over (or 13 to 17 plus 18 and over). Do not
  include under 13; that triggers the families policy.
- **News apps:** No. **COVID-19 contact tracing:** No. **Government apps:** No.
  **Financial features:** none. **Health:** none.
- **Advertising ID:** No, the app does not use it.
- **Data safety** (must match the privacy policy):
  - Collects or shares user data: Yes.
  - Encrypted in transit: Yes.
  - Deletion request mechanism: No, reason: the app stores no user data.
  - Data types: Location, tick both Approximate and Precise. For each: Collected yes,
    Shared yes, processed ephemerally yes, required (not optional), purpose App
    functionality. Nothing else. Do not tick Device or other IDs, App activity, or
    Crash logs.
  - Preview the summary. It should read: Location, shared with third parties,
    ephemerally processed, for app functionality.

## Step 5: Store listing

Grow, Store presence, Main store listing:

- App name, short description, full description: copy from the code blocks in
  `docs/store-listing.md` (full description is 1574 characters).
- App icon: `docs/store-assets/app-icon-512.png`.
- Feature graphic: `docs/store-assets/feature-graphic.png`.
- Wear OS screenshots: `screenshot-2-locating.png`, `screenshot-3-stop-list.png`,
  `screenshot-4-arrivals.png`. `screenshot-1-launcher.png` is optional; it shows other
  apps' icons. Leave phone screenshot slots empty. Save.

Store settings: category Travel and Local, contact email attnfeed@gmail.com, website
https://github.com/amac0/BusWatch.

## Step 6: Wear OS form factor

Release, Advanced settings, Form factors tab, Add form factor, Wear OS. Tick the opt-in
for Wear OS review. Without this the app stays hidden in the watch's Play Store even
after approval. The manifest already declares a standalone watch app, which the review
requires.

## Step 7: Release

1. Release, Testing, Internal testing, Create new release. When prompted to enrol in Play
   App Signing, accept; this registers the keystore as the upload key.
2. Upload `../keystore/BusWatch-1.0.0-versionCode2.aab`. The console should show
   version 1.0.0 (2) and flag it as Wear OS.
3. Release notes: paste the notes block from `docs/store-listing.md`. Save, Review
   release, Start rollout to internal testing.
4. Testers tab: create a list with your own Google account, copy the opt-in link, accept
   it on your phone, then install BusWatch on the Pixel Watch from the watch's Play
   Store. Check the stop list, arrivals, and the About screen's policy links. Grant
   location when asked, as reviewers will.
5. When satisfied: Release, Production, Create new release, Add from library, choose the
   same bundle, Review, Start rollout. The Wear OS review and policy review run on this
   submission. Expect a few days, longer for a first submission.

## Common rejection reasons

- Privacy policy link broken or not matching the Data safety form. Republish the docs
  page before any resubmission; reviewers open the link.
- No privacy policy link inside the app. The About screen provides it.
- Data safety form declaring less than the app does. Location must be declared as
  collected and shared.

## After launch

- Bump `versionCode` before every upload.
- Any change to `PrivacyPolicy.md` or `TermsOfService.md`: run
  `scripts/build-legal-html.sh`, commit, push main; GitHub Pages redeploys.
- Pre-existing failing unit test to fix: https://github.com/amac0/BusWatch/issues/1.
