# ProSySVPN native core provenance

- Application version: `3.1.0` (`1000005`)
- Android client baseline: current ProSySVPN tree aligned with the modern 2dust Android client architecture
- Native core source: official `2dust/v2rayNG` stable release `2.2.6`
- Upstream release commit: `15b4fff8e45da9bc0acaa5cc1d80a1d3531e8712`
- AndroidLibXrayLite gitlink: `3b5a9c858c4dc98b7079cefb1380537b6b5c155c`
- Xray stable line: `26.3.27`
- Supported ABIs: `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`
- Updated AAR SHA-256: `C91FB8C8F4DFF55D47DF3F70158F1778E1EA095B460BFED58A247EE5730DDC93`

The native libraries were taken from the official per-ABI APK assets published for release `2.2.6`. The previous AAR is retained under `tools/core-backups` for reproducibility and rollback, and is not packaged into the application.
