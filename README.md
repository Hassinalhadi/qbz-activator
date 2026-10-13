# QBZ Activator Pro (مشروع مستقل لتطبيق QBZ.apk)

مشروع Xposed/LSPosed مستقل وخفيف مخصص بالكامل للعمل مع تطبيق **QBZ.apk** (`net.jahez.lsposed`).

---

## 🌟 الميزات الأساسية للمشروع

1. **إلغاء نافذة التفعيل (Bypass Dialog)**:
   - إلغاء وحجب نافذة الديالوج ("dilgo") التي تطالب بإدخال مفتاح التفعيل ("Enter your key") نهائياً.
   - تجاوز دالة `showDialog()` و `Dialog.show()`.
2. **تخطي شاشة تسجيل الدخول تلقائياً (Auto-Bypass Login)**:
   - عند فتح `LoginActivity` أو `LoginActivityV2`، يتم توجيه التطبيق فوراً لفتح الواجهة الرئيسية `MainActivity`.
3. **حقن ترخيص VIP دائم (Lifetime Subscription)**:
   - حقن بيانات اشتراك مفعل حتى عام 2099 داخل ذاكرة التطبيق `net.jahez.lsposed.Subscription`.
4. **تجاوز خطأ الهوك**:
   - منع ظهور رسالة "Error in Xposed hooks" داخل تطبيق QBZ.
5. **بناء آلي عبر GitHub Actions**:
   - يحتوي المشروع على ملف `.github/workflows/build.yml` الذي يقوم ببناء ملف الـ APK تلقائياً عند رفعه إلى GitHub.

---

## 📦 محتويات المشروع

- `QBZ.apk`: ملف التطبيق الأصلي (الحزمة: `net.jahez.lsposed`).
- `app/src/main/java/dev/qbz/activator/HookEntry.kt`: محرك الهوك والتجاوز.
- `app/src/main/resources/META-INF/xposed/scope.list`: نطاق الحقن الموجه لتطبيق QBZ.
- `.github/workflows/build.yml`: ملف البناء التلقائي في السحاب.

---

## 🚀 كيفية الرفع إلى مستودع GitHub جديد

1. أنشئ مستودعاً جديداً على GitHub باسم `qbz-activator`.
2. داخل مجلد المشروع في جهازك، افتح موجه الأوامر (CMD أو Terminal) ونفذ:

```bash
git init
git branch -M main
git remote add origin https://github.com/Hassinalhadi/qbz-activator.git
git add .
git commit -m "Initial commit for QBZ Activator standalone project"
git push -u origin main --force
```

3. بعد الرفع، ادخل إلى تبويب **Actions** في مستودع GitHub وستجد ملف الـ APK جاهزاً للتحميل مباشرة.
