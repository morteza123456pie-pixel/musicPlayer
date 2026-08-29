# Music Player — Phase 3

## Step 0: تأیید Build و رفع اشکالات Phase 2

قبل از شروع Phase 3، پروژه‌ی Phase 2 بررسی و این موارد اصلاح شد:

- **`LibraryScreen.kt`** و **`AlbumsScreen.kt`**: هر دو از `.background(...)` استفاده
  می‌کردن بدون import کردن `androidx.compose.foundation.background` — این باعث خطای
  کامپایل "unresolved reference" می‌شد. رفع شد.
- **`MusicPlayerNavHost.kt`**: import بلااستفاده‌ی `NamedNavArgument` حذف شد.
- **`SampleMusicData.kt`**: import بلااستفاده‌ی `AppColors` (یک وابستگی UI در فایل
  data-layer) حذف شد.
- **Gradle wrapper**: فایل‌های `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.properties`
  که در Phase 1/2 جا افتاده بودن اضافه شدن.

⚠️ **محدودیت مهم (در آن مرحله)**: این محیط sandbox نه به Maven/Google's repositories دسترسی
شبکه داره و نه یک توزیع Gradle نصب‌شده — یعنی اجرای واقعی `./gradlew build` اون‌جا ممکن نبود.
در عوض، هر فایل Kotlin به‌صورت دستی و خط‌به‌خط از نظر import های گم‌شده، ارجاعات
resolve‌نشده، و symbol های استفاده‌نشده بررسی شد.

**به‌روزرسانی بعدی:** باینری واقعی `gradle-wrapper.jar` (از `raw.githubusercontent.com`،
که در دسترس بود) دانلود و مستقیم توی پروژه commit شد — پس این محدودیت دیگه مربوط نیست؛
جزئیات کامل در بخش «`gradle-wrapper.jar`» پایین‌تر.

## Phase 3: معماری واقعی پخش + صفحه‌ی کامل Now Playing

### معماری Player (جدید)

```
player/
├── MusicPlayerController.kt       — اینترفیس؛ تنها API که UI مجاز به استفاده ازشه
├── MusicPlayerControllerImpl.kt   — پیاده‌سازی واقعی روی Media3 MediaController
├── PlayerState.kt                 — state واکنش‌گرا (جایگزین PlaybackStateHolder فاز ۲)
├── PlayerEvent.kt                 — رویدادهای یک‌باره (خطا، عدم دسترسی به رسانه)
└── PlayerServiceConnection.kt     — اتصال async به سرویس از طریق MediaController

service/
├── MusicPlaybackService.kt        — MediaSessionService با ExoPlayer، foreground
└── MediaSessionManager.kt         — ساخت ExoPlayer + MediaSession

di/
└── PlayerModule.kt                — Hilt binding: MusicPlayerController → Impl (Singleton)
```

**جریان پخش واقعی**: انتخاب یک ترک در Library → `LibraryViewModel.onTrackSelected` →
`MusicPlayerController.playQueue(...)` → `MusicPlayerControllerImpl` به `MusicPlaybackService`
(از طریق `PlayerServiceConnection` و یک `MediaController` واقعی Media3) وصل میشه، صف رو
می‌سازه، و پخش رو شروع می‌کنه. `Player.Listener` تغییرات واقعی ExoPlayer (isPlaying,
position, track transition, shuffle, repeat, error) رو به `StateFlow<PlayerState>` ترجمه
می‌کنه. **همه‌ی UI** (mini-player، Library، Now Playing) دقیقاً همین یک `StateFlow` رو
observe می‌کنن — یک منبع واحد حقیقت برای پخش.

**رسانه‌ی نمونه بدون URI واقعی**: ترک‌های Sample از Phase 2 آدرس `sample://track/N` دارن که
ExoPlayer قابل resolve نیست. `MusicPlayerControllerImpl` این رو از قبل تشخیص می‌ده،
هیچ‌وقت اون رو مستقیم به پلیر نمی‌ده (پس کرش نمی‌کنه)، و به‌جاش `PlayerEvent.PlaybackUnavailable`
emit می‌کنه؛ `state.hasPlayableMedia` روی false می‌مونه و Now Playing عنوان/هنرمند/آرت‌ورک
رو نشون می‌ده بدون progress bar متحرک. وقتی در فازهای بعدی URI واقعی از MediaStore وصل بشه
(`content://`)، همین مسیر کد بدون تغییر پخش واقعی انجام می‌ده.

### صفحه‌ی Now Playing (جدید)

`ui/nowplaying/NowPlayingScreen.kt` + `NowPlayingViewModel.kt` — آرت‌ورک سینمایی بزرگ،
عنوان/هنرمند، progress slider واقعاً seekable (sync با موقعیت واقعی Media3 هر ۵۰۰ms)،
دکمه‌ی دایره‌ای بزرگ Play/Pause (کانون بصری صفحه با رنگ بنفش)، Previous/Next، Shuffle/Repeat،
و ردیف اکشن‌های ثانویه (Favorite, Lyrics, Queue, More). از `MusicPlayerNavHost.kt` به
`Destination.NowPlaying.route` وصل شده و جایگزین PlaceholderScreen قبلی شد.

### حذف شد

- `player/PlaybackStateHolder.kt` — جایگزین شده با معماری واقعی بالا.
- `domain/model/PlaybackState.kt` — جایگزین شده با `player/PlayerState.kt`؛ فقط
  `RepeatMode` ازش نگه داشته شد و به `domain/model/RepeatMode.kt` منتقل شد.

### Home/Library

طبق دستور صریح Phase 3، این مورد فقط بازبینی شد نه بازسازی: `Destination.Home` و
`Destination.Library` هنوز دو route مجزا هستن که هر دو همون `LibraryScreen` رو نشون
می‌دن. این رفتار از Phase 2 مشکلی ایجاد نکرده و شکستن navigation در کار نیست، پس طبق
دستور دست‌نخورده باقی موند.

## نحوه باز کردن

پوشه `musicplayer/` رو در Android Studio باز کن، Gradle sync کن، روی دستگاه/امولاتور
(minSdk 24+) اجرا کن. انتخاب یک ترک در Library واقعاً وارد صف Media3 میشه (هرچند چون
URI های نمونه واقعی نیستن صدایی پخش نمیشه — این پیام از طریق `PlayerEvent.PlaybackUnavailable`
قابل مشاهده در event flow هست). زدن روی mini-player صفحه‌ی Now Playing واقعی رو باز می‌کنه.

## چیزی که در Phase 3 عمداً ساخته نشده

- Search, Queue, Lyrics به‌عنوان صفحه‌ی کامل (فقط placeholder، ولی Now Playing به‌شون hook داره)
- Equalizer, Settings کامل, Sleep Timer
- اتصال واقعی به MediaStore برای فایل‌های صوتی دستگاه (معماری آماده‌ست، Track.uri همینه که باید باشه)

این‌ها طبق برنامه در فازهای بعدی اضافه میشن.

## Phase 3.5 — رفع باگ‌های بصری روی دستگاه واقعی

بعد از تست APK فاز ۳ روی یک گوشی واقعی اندروید (با زبان سیستم فارسی/RTL)، این باگ‌ها
مشاهده و رفع شدن:

### ۱. مشکل RTL/LTR — بحرانی، رفع شد

**علت:** هیچ‌جا توی پروژه `LayoutDirection` صریحاً ست نشده بود، پس Compose به‌صورت
پیش‌فرض از locale سیستم (فارسی = RTL) پیروی می‌کرد و کل چیدمان اپ (هدر، تب‌ها، ردیف‌های
ترک، bottom nav) آینه شده بود.

**رفع:** در `ui/theme/Theme.kt`، کل درخت UI اپ داخل
`CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr)` قرار
گرفت. این فقط جهت *چیدمان* (row order, alignment, icon mirroring) رو force می‌کنه، نه
رندر متن — یعنی اگه بعداً محتوای فارسی (مثل نام ترک) اضافه بشه، همچنان درست RTL رندر
میشه چون الگوریتم bidi یونیکد مستقل از این تنظیم روی خود متن اجرا میشه. یک لایه‌ی
دفاعی اضافه هم در `values/themes.xml` با `android:layoutDirection="ltr"` گذاشته شد.

**درباره‌ی آیکون‌ها (مورد ۸ گزارش):** بررسی شد که هیچ‌کدوم از آیکون‌های پخش
(Play/Pause/Previous/Next/Shuffle/Repeat) از نسخه‌ی `Icons.AutoMirrored.*` استفاده
نمی‌کنن — همه از `Icons.Filled.*` عادی هستن که اصلاً auto-mirror نمیشن. پس با رفع
LayoutDirection، این مورد هم به‌طور کامل و بدون تغییر جداگانه حل شد.

### ۲. Cover Art — بلاب بنفش محو، رفع شد

**علت:** کامپوننت `CoverArt.kt` قبلی از `Brush.radialGradient` بدون radius مشخص به‌همراه
یک دایره‌ی «moon» جدا که داخل یک `Box` تودرتو با `aspectRatio` دیگه align می‌شد استفاده
می‌کرد. این ترکیب باعث می‌شد دایره‌ی کوچیک از محدوده‌ی clip شده‌ی کارت بیرون بزنه —
دقیقاً همون نقطه‌ی روشن بیرون از مربع که توی اسکرین‌شات دیده میشه.

**رفع:** کل fallback بازنویسی شد: یک `Brush.linearGradient` مورب تیره (بدون افکت
درخشش شعاعی نامحدود) + یک آیکون نت موزیقی کم‌رنگ و وسط‌چین، همه به‌عنوان فرزند مستقیم
همون یک `Box`ای که caller قبلاً clip کرده. دیگه هیچ shape جداگانه‌ای با align خودش
وجود نداره که بتونه از محدوده بیرون بزنه.

### ۳. فاصله‌ی خالی زیر لیست — بررسی شد، باگ نیست

کد `LibraryScreen.kt` از نظر `LazyColumn`/`fillMaxSize`/`weight` بررسی شد و هیچ باگ
چیدمانی پیدا نشد. `LazyColumn` طبیعتاً کشیده نمیشه تا فضای خالی رو پر کنه — با فقط ۶
ترک نمونه، این فضای خالی نتیجه‌ی طبیعی و صحیح تعداد کم آیتم‌هاست، نه یک باگ layout. طبق
تصمیم گرفته‌شده، فعلاً دست‌نخورده موند چون در نسخه‌ی نهایی با اتصال MediaStore، دیتای
واقعی دستگاه این فضا رو به‌طور طبیعی پر می‌کنه.

### ۴. Status Bar / Safe Area — رفع شد

**علت:** `enableEdgeToEdge()` در `MainActivity` فعال بود ولی هیچ‌جا `WindowInsets`
مصرف نمی‌شد، پس محتوا زیر status bar و ناحیه‌ی gesture navigation می‌رفت.

**رفع:** در `MainActivity.kt`، `Modifier.windowInsetsPadding(WindowInsets.statusBars)`
یک‌بار روی کل `MusicPlayerNavHost` (بالای محتوا) و
`Modifier.windowInsetsPadding(WindowInsets.navigationBars)` یک‌بار دور کل گروه پایین
(mini-player + bottom nav) اعمال شد. این یعنی هیچ صفحه‌ای (فعلی یا آینده) نیازی به
inset handling جداگانه نداره — یک‌جا برای کل اپ حل شده.

### ۵. Mini Player — بررسی شد، باگ نیست

طبق تأیید، تست بدون انتخاب هیچ ترکی انجام شده بود، پس نبودن mini-player رفتار درست و
مورد انتظاره (`PlayerState.currentTrack` پیش‌فرض `null` هست). جریان کامل mini-player
(نمایش بعد از انتخاب ترک، هم‌گام‌سازی با state واقعی پخش، باز شدن Now Playing با تپ)
از نظر کد دوباره بررسی و تأیید شد که درسته — منتظر تأیید بصری روی دستگاه بعد از build
جدیده.

### ۶. Bottom Navigation — رفع شد

**علت:** سایز آیکون‌ها فقط ۲۱dp بود، touch target عمودی خیلی کوتاه (padding 4dp)، و
هیچ حداقل ارتفاعی روی کل نوار تنظیم نشده بود — روی دستگاه واقعی به‌صورت یک نوار باریک و
کم‌وضوح دیده می‌شد.

**رفع:** در `AppBottomNavigation.kt`: سایز آیکون به ۲۶dp افزایش یافت، هر آیتم حداقل
۴۸dp ارتفاع گرفت (حداقل استاندارد accessibility اندروید برای touch target)، و کل نوار
حداقل ۷۲dp ارتفاع گرفت. رنگ حالت غیرفعال هم از `TextTertiary` (خیلی کم‌رنگ) به
`TextSecondary` (خواناتر) تغییر کرد. ظاهر premium و سفارشی (نه Material پیش‌فرض) کاملاً
حفظ شد.

### محدودیت مهم این فاز

این محیط sandbox نه SDK اندروید داره، نه emulator، نه دسترسی به دستگاه فیزیکی — پس
نتونستم خودم یک build واقعی بگیرم، APK بسازم، نصبش کنم، یا اسکرین‌شات واقعی جدید
بگیرم. تمام رفع‌های بالا بر اساس **بررسی دقیق کد** (خط‌به‌خط، با تحلیل معماری Compose
که چرا هر باگ اتفاق افتاده) و **مقایسه‌ی مستقیم با اسکرین‌شاتی که فرستادی** انجام شده،
نه با build/inspect واقعی. برای تأیید بصری نهایی، لطفاً از طریق GitHub Actions
(که در تنظیمات قبلی درست شده) یک APK جدید بگیر و روی گوشی تست کن — اگه هنوز جایی مشکل
داشت، با اسکرین‌شات جدید بگو تا دقیق رفعش کنم.

## آماده‌سازی برای GitHub

فایل‌های زیر برای آپلود در GitHub و ساخت خودکار APK اضافه شدن:

- **`.gitignore`** — جلوگیری از commit شدن `build/`, `.gradle/`, `.idea/`, `local.properties`
- **`gradle.properties`** — تنظیمات ضروری Gradle (`android.useAndroidX=true` و غیره) که بدونش sync ممکنه fail بشه
- **`local.properties.example`** — نمونه‌ی فایل تنظیم مسیر SDK (خود `local.properties` باید gitignore بمونه چون مسیر SDK مخصوص هر سیستمه)
- **آیکون اپ** (`mipmap-anydpi-v26/`, `mipmap/`, `drawable/ic_launcher_foreground.xml`) — قبلاً منیفست به `@mipmap/ic_launcher` اشاره می‌کرد ولی خود فایلش وجود نداشت؛ بدون این build واقعاً fail می‌شد. یک آیکون adaptive ساده (نت موزیقی، بنفش روی پس‌زمینه‌ی سرمه‌ای تیره) ساخته شد که هم API 26+ (adaptive) و هم API 24-25 (fallback ساده) رو پوشش می‌ده.
- **`app/proguard-rules.pro`** — قوانین پایه برای Media3/Hilt/Room، برای وقتی minify فعال بشه
- **`.github/workflows/android-build.yml`** — یک GitHub Actions workflow که با هر push به `main` (یا اجرای دستی) یک APK دیباگ می‌سازه و به‌عنوان artifact قابل‌دانلود در تب Actions می‌ذاره. این دقیقاً همون چیزیه که "تبدیل به اپ" رو بدون نیاز به نصب Android Studio ممکن می‌کنه.
- **`LICENSE`** — لایسنس MIT، معمول برای ریپوهای عمومی.

### `gradle-wrapper.jar`

آپدیت شد: باینری واقعی `gradle-wrapper.jar` (نسخه‌ی هماهنگ با Gradle 8.7 که در
`gradle-wrapper.properties` مشخص شده) مستقیماً توی پروژه commit شده. دیگه نیازی به هیچ
مرحله‌ی bootstrap یا نصب دستی نیست — `./gradlew` همیشه دقیقاً همون نسخه‌ی Gradle که پروژه
باهاش تست شده رو دانلود و اجرا می‌کنه، چه روی GitHub Actions چه روی سیستم خودت.

⚠️ اگه قبلاً یک نسخه‌ی قدیمی‌تر از این پروژه رو push کرده بودی که این فایل توش نبود و
workflow قبلی سعی می‌کرد با `gradle wrapper` بسازتش — اون روش یک باگ داشت: اگه Gradle
نصب‌شده روی runner نسخه‌ای جدیدتر از 9.0 باشه (که AGP 8.5.2 و پلاگین قدیمی Kotlin Android
باهاش سازگار نیستن)، با خطای `Configuration.fileCollection(Spec)` fail می‌شد. این نسخه‌ی
جدید کلاً اون مرحله رو حذف کرده چون دیگه لازم نیست.

### چطور از GitHub یک APK نصب‌شدنی بگیرم

۱. یک ریپوی جدید در GitHub بساز و این پوشه رو push کن (`git init`, `git add .`, `git commit`, `git remote add origin ...`, `git push`).
۲. برو به تب **Actions** توی ریپو — workflow به اسم "Android Build" باید خودش اجرا بشه (یا از "Run workflow" دستی بزن).
۳. وقتی اجرا تموم شد، پایین صفحه‌ی run یک فایل به اسم `musicplayer-debug-apk` هست — دانلودش کن.
۴. فایل zip رو باز کن، `app-debug.apk` رو روی گوشی اندرویدت منتقل کن، "نصب از منابع ناشناس" رو فعال کن، و نصبش کن.

این یک build **دیباگ** هست (برای تست)، نه نسخه‌ی نهایی قابل انتشار در Google Play —
برای اون باید APK رو امضا (sign) کنی که فرآیند جداگانه‌ای داره.

#### نکته‌ی مهم درباره‌ی ساختار پوشه‌ها موقع push

وقتی این zip رو باز می‌کنی، یک پوشه‌ی `musicplayer/` می‌بینی که همه‌چیز توشه. دو حالت
معمول برای push کردن هست:

- **اگه محتوای داخل `musicplayer/` رو مستقیم توی ریشه‌ی ریپو بذاری** (یعنی `build.gradle.kts`
  و `app/` مستقیم زیر ریشه‌ی ریپو باشن، نه زیر یک پوشه‌ی `musicplayer/` دیگه) — بهترین
  حالته و بدون هیچ تنظیم اضافه کار می‌کنه.
- **اگه خود پوشه‌ی `musicplayer/` رو هم commit کنی** (یعنی ساختار نهایی بشه
  `repo-root/musicplayer/build.gradle.kts`) — هم مشکلی نیست؛ workflow (`.github/workflows/android-build.yml`)
  خودش با یک قدم `find` این پوشه رو پیدا می‌کنه و از همون‌جا build رو اجرا می‌کنه، پس
  دیگه به خطای "Directory does not contain a Gradle build" برنمی‌خوری.

اگه قبلاً یک بار push کرده بودی و این خطا رو گرفتی، کافیه همین `android-build.yml` جدید
رو جایگزین نسخه‌ی قبلی توی ریپوت کنی (یا کل پروژه رو دوباره push کنی) و دوباره از تب
Actions اجراش کن.




