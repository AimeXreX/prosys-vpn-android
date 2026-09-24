# ProSyS VPN Android 1.0.0

نسخهٔ شخصی‌سازی‌شدهٔ v2rayNG برای Android با شناسهٔ بستهٔ `top.prosysvpn.android` است. سورس پایه از v2rayNG و هستهٔ Xray گرفته شده و مجوز GPL-3.0 پروژه حفظ شده است.

## خروجی‌ها

- `dist/ProSySVPN_1.0.0_Universal.apk`: نصب مستقیم روی arm64-v8a، armeabi-v7a، x86 و x86_64
- `dist/ProSySVPN_1.0.0_PlayStore.aab`: فایل مناسب انتشار در Google Play یا فروشگاه سازگار با AAB
- `dist/SHA256SUMS.txt`: هش کنترل صحت فایل‌ها

APK و AAB با کلید موجود در `signing/prosys-release.jks` امضا شده‌اند. رمز و تنظیم محلی آن در `V2rayNG/signing.properties` است. هر دو فایل را در دو محل امن و خارج از سرور بکاپ بگیرید؛ بدون همین کلید، انتشار آپدیت روی همان package ممکن نیست.

## رفتار سرویس رایگان

1. اپ یک شناسهٔ ناشناس پایدار را در خود دستگاه از `ANDROID_ID` با SHA-256 می‌سازد؛ شناسهٔ خام ارسال نمی‌شود.
2. backend فقط HMAC این شناسه را ذخیره و در 3x-ui برای همان دستگاه یک client می‌سازد.
3. client دارای 0.5 GiB (۵۱۲ MiB) حجم، `reset=1`، اولین انقضا در ساعت 00:00 تهران و `limitIp=1` است. 3x-ui 3.5.0 در هر تمدید روزانه ترافیک را صفر و expiry را یک روز جلو می‌برد.
4. اپ وضعیت را دوره‌ای و subscription را هر 60 دقیقه به‌روز می‌کند. با تمام‌شدن حجم، اتصال رایگان متوقف و دکمه‌های خرید سایت و ربات نمایش داده می‌شوند.
5. subscription رایگان فقط از proxy سایت و با هدر توکن دستگاه تحویل می‌شود. QR، کپی، ویرایش، حذف و export پروفایل مدیریت‌شده در UI غیرفعال است.
6. کاربران همچنان می‌توانند کانفیگ و subscription شخصی خود را اضافه، ویرایش و export کنند.

## backend مستقل اندروید

backend اندروید در پوشه `backend` قرار دارد، روی پورت داخلی 3010 اجرا می‌شود و فایل `.env`، دیتابیس و وابستگی‌های آن از سایت جدا هستند. تنظیمات production نمونه:

```dotenv
NODE_ENV=production
PORT=3010
APP_URL=https://vpn.prosysvpn.top
DEMO_MODE=false

XUI_BASE_URL=https://PANEL-DOMAIN:PORT/PANEL-PATH
XUI_API_TOKEN=YOUR_3X_UI_API_TOKEN
XUI_FREE_INBOUND_IDS=1,2
XUI_FREE_PUBLIC_SUB_URL=https://SUB-DOMAIN/SUB-PATH

FREE_DAILY_GB=0.5
FREE_RESET_OFFSET_MINUTES=210
FREE_DEVICE_HMAC_SECRET=GENERATE_A_SEPARATE_RANDOM_SECRET_OF_AT_LEAST_32_CHARACTERS
FREE_DEVICE_DB_PATH=./data/free-devices.json

PRO_SYS_SITE_URL=https://vpn.prosysvpn.top/
PRO_SYS_BOT_URL=https://t.me/Prosyssellbot
```

`XUI_FREE_PUBLIC_SUB_URL` باید ریشهٔ لینک subscription و بدون `subId` انتهایی باشد. فقط مسیرهای `/api/free/` و `/privacy` در reverse proxy دامنه به پورت 3010 هدایت می‌شوند؛ سایت روی پورت 3000 باقی می‌ماند. نمونه Nginx در `backend/README.md` آمده است.

در 3x-ui 3.5.0 موارد زیر را کنترل کنید:

- API Token فعال و `XUI_BASE_URL` از سرور سایت قابل دسترسی باشد.
- inboundهای رایگان فعال و دارای sniffing/routing مناسب باشند.
- IP Limit و Access Log لازم برای تشخیص IP در پنل فعال باشند تا `limitIp=1` اعمال شود.
- آدرس subscription عمومی پنل فقط از HTTPS معتبر استفاده کند.

فایل `data/free-devices.json` بخشی از دیتای production است و باید بکاپ شود. ذخیره‌سازی فایل فعلی برای یک process Node طراحی شده؛ اگر چند replica اجرا می‌کنید، این جدول باید به دیتابیس مشترک و دارای unique constraint روی `installationHash` منتقل شود.

## API افزوده‌شده

- `POST /api/free/register` با `installationId` و توکن قبلی اختیاری
- `GET /api/free/status` با هدر `X-ProSys-Device`
- `GET /api/free/subscription` با همان هدر و `Cache-Control: no-store`
- `/privacy` سیاست حریم خصوصی فارسی/انگلیسی اپ

## ساخت نسخهٔ بعدی

JDK 21 و Android SDK لازم است. هستهٔ `libv2ray.aar` و کتابخانه‌های native در `V2rayNG/app/libs` قرار دارند.

```powershell
cd V2rayNG
.\gradlew.bat :app:testPlaystoreDebugUnitTest
.\gradlew.bat :app:assemblePlaystoreRelease :app:bundlePlaystoreRelease
```

برای هر آپدیت، `versionCode` و `versionName` را در `V2rayNG/app/build.gradle.kts` افزایش دهید و همان `prosys-release.jks` را استفاده کنید. آپدیت داخلی v2rayNG از منو حذف شده است؛ نسخهٔ Play Store از کانال فروشگاه به‌روز می‌شود.

## محدودیت ضد اشتراک‌گذاری

روی Android معمولی، کانفیگ رایگان از UI قابل مشاهده/کپی/export نیست، endpoint subscription توکن دستگاه می‌خواهد و 3x-ui اتصال هم‌زمان را به یک IP محدود می‌کند. با این حال جلوگیری صددرصدی روی دستگاه root‌شده یا اپ دستکاری‌شده از نظر فنی ممکن نیست، چون هستهٔ VPN در نهایت باید credential اتصال را در حافظه دریافت کند. شناسهٔ دستگاه نیز در کلاینت قابل جعل است؛ برای مقابله با سوءاستفادهٔ سازمان‌یافته باید rate limit، تشخیص الگو و مانیتورینگ سمت سرور هم فعال باشد.

## انتشار و مجوز

v2rayNG تحت GPL-3.0 است. هنگام توزیع APK/AAB باید متن مجوز و سورس متناظر همین نسخه و تغییرات ProSyS را نیز در دسترس دریافت‌کنندگان قرار دهید. برای Google Play علاوه بر AAB، فرم VpnService، Data safety، سیاست حریم خصوصی و اطلاعات حساب توسعه‌دهنده باید در Play Console تکمیل شوند.
