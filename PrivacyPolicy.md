# BusWatch Privacy Policy

**Effective Date:** September 7, 2026

---

## Contents

1. [Who We Are](#1-who-we-are)
2. [What BusWatch Does](#2-what-buswatch-does)
3. [Data the App Uses](#3-data-the-app-uses)
4. [Permissions](#4-permissions)
5. [Third-Party Services](#5-third-party-services)
6. [What We Do Not Do](#6-what-we-do-not-do)
7. [Data Retention](#7-data-retention)
8. [Your Rights and Choices](#8-your-rights-and-choices)
9. [Children's Privacy](#9-childrens-privacy)
10. [Changes to This Policy](#10-changes-to-this-policy)
11. [Contact Us](#11-contact-us)
12. [Changelog](#12-changelog)

---

## 1. Who We Are

BusWatch is a Wear OS app made by Attention Feed, Inc. ("AttentionFeed," "we," "us,"
or "our"). It shows live London bus arrival times on your watch.

This policy covers only the BusWatch app. Our websites and other services are covered
by the [AttentionFeed Privacy Policy](https://attentionfeed.com/privacy/). When we say
"you," we mean the person using BusWatch.

---

## 2. What BusWatch Does

BusWatch finds the bus stops closest to you, shows the routes that serve each stop, and
shows live arrival predictions for the stop you pick. It has no account, no sign-in,
and no servers of our own. Everything happens between your watch, Google's location
services on the watch, and Transport for London's public data service.

---

## 3. Data the App Uses

### 3.1 Your Location

BusWatch reads your watch's location while the app is open. It uses the location to:

- Find bus stops within about 500 metres of you
- Refresh the list of stops when you move more than about 200 metres
- Decide whether the stop you last used is still close enough (within 500 metres) to
  show first

To find stops, the app sends your latitude and longitude to Transport for London's
(TfL) public data service. That request also carries your watch's IP address, standard
request headers, and the API key we registered with TfL so the app is allowed to use
the service.

We never receive your location. BusWatch has no backend, so your location goes from
your watch to TfL and nowhere else. The app does not keep a history of where you have
been.

### 3.2 Your Last Selected Stop

To open quickly on the stop you used last time, the app stores three values on your
watch:

- The identifier of the stop
- The stop's latitude and longitude (the stop's position, not yours)

These values never leave the watch. They are replaced when you pick a different stop
and are removed when you clear the app's data or uninstall the app.

### 3.3 Bus Data

When you view a stop, the app asks TfL for arrivals at that stop by sending the stop's
identifier. TfL's response contains public timetable and prediction data, not data
about you.

---

## 4. Permissions

| Permission | Why the app asks for it |
|------------|-------------------------|
| Precise location | Find stops near you and keep the list current as you move |
| Approximate location | Fallback if precise location is unavailable |
| Internet | Fetch stops and arrivals from Transport for London |
| Keep the watch awake | Keep arrival times updating while you are looking at them |

You can revoke location permission at any time in your watch's settings. Without it,
BusWatch cannot find nearby stops.

---

## 5. Third-Party Services

| Service | What it receives | Why | Their privacy policy |
|---------|------------------|-----|----------------------|
| **Transport for London (TfL) Unified API** | Your latitude and longitude, the stop identifier you select, your IP address, standard request headers, and our API key | Find nearby stops and fetch live arrivals | [TfL Privacy and Cookies](https://tfl.gov.uk/corporate/privacy-and-cookies/) |
| **Google Play services (location)** | Your location, processed on the watch to give the app a position | Provide the device location the app reads | [Google Privacy Policy](https://policies.google.com/privacy) |
| **Google Play** | Whatever Google collects when you install or update an app | Distribute the app | [Google Privacy Policy](https://policies.google.com/privacy) |

We do not control how these services handle data they receive. Their policies apply to
that processing.

---

## 6. What We Do Not Do

- We do not run servers for BusWatch and do not receive any data from it.
- We do not use analytics, advertising, or crash-reporting services in the app.
- We do not set cookies or use tracking identifiers.
- We do not sell or share personal data.
- Release builds of the app do not write network traffic to logs. Development builds
  used only by us do.

---

## 7. Data Retention

| Data | Where it lives | How long |
|------|----------------|----------|
| Your location | In memory on the watch while the app is open; sent to TfL per request | Not kept by the app; TfL's retention applies to their logs |
| Last selected stop (identifier and stop position) | On your watch | Until you pick another stop, clear the app's data, or uninstall |

We hold nothing about you, so there is nothing for us to retain.

---

## 8. Your Rights and Choices

Because BusWatch stores data only on your own watch and we hold none of it, the usual
requests to access, correct, or delete data are things you can do yourself:

- **Stop location use:** revoke the location permission in your watch's settings.
- **Delete stored data:** clear the app's data in your watch's settings, or uninstall
  the app.
- **Questions or concerns:** email us at attnfeed@gmail.com. We aim to respond within
  30 days.

Depending on where you live, you may also have the right to lodge a complaint with your
local data protection authority.

---

## 9. Children's Privacy

BusWatch is not directed at children under 13 and shows only public transport
information. We do not knowingly collect personal data from anyone, children included.

---

## 10. Changes to This Policy

We may update this policy from time to time. When we do, we will update the effective
date at the top of this page, publish the new version at the same address, and record
the change in the [Changelog](#12-changelog). Continued use of the app after a change
takes effect means you accept the update.

---

## 11. Contact Us

**Email:** attnfeed@gmail.com

**Source code and issues:** https://github.com/amac0/BusWatch

---

## 12. Changelog

| Date | Summary of Changes |
|------|-------------------|
| September 7, 2026 | Initial version |
