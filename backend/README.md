# ProSyS VPN Android backend

این سرویس کاملاً مستقل از backend سایت است و فقط API سرویس رایگان اپ و صفحه حریم خصوصی را ارائه می‌کند.

## اجرا

```powershell
Copy-Item .env.example .env
npm install
npm start
```

سرویس فقط روی `127.0.0.1:3010` گوش می‌دهد. فایل `.env` و دیتابیس `data/free-devices.json` نباید داخل Git قرار بگیرند.

## Nginx روی دامنه اصلی

```nginx
location /api/free/ {
    proxy_pass http://127.0.0.1:3010;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}

location = /privacy {
    proxy_pass http://127.0.0.1:3010/privacy;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

backend سایت روی پورت 3000 باقی می‌ماند و این دو مسیر فقط به backend اندروید هدایت می‌شوند.
