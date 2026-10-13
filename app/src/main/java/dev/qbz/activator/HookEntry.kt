package dev.qbz.activator

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

class HookEntry : XposedModule() {

    companion object {
        private const val TAG = "QBZ_SHIELD"
    }

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        val pkg = param.packageName
        if (pkg == "net.jahez.fleets") {
            hookJahezSplashGuard()
        } else if (pkg == "net.jahez.lsposed") {
            hookQbzDialogAndLicense()
        }
    }

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        val pkg = param.packageName
        val cl = param.classLoader ?: return

        if (pkg == "net.jahez.fleets") {
            hookJahezDeepGuard(cl)
        } else if (pkg == "net.jahez.lsposed") {
            hookQbzValidation(cl)
        }
    }

    /**
     * 1. حماية تطبيق جاهز من التعليق على الشعار (Splash Hang Shield)
     */
    private fun hookJahezSplashGuard() {
        // حماية دالة createPackageContext من الانهيار الأمني
        try {
            val mCreatePkg = Context::class.java.getDeclaredMethod(
                "createPackageContext",
                String::class.java,
                Int::class.javaPrimitiveType
            )
            hook(mCreatePkg).intercept(object : XposedInterface.Hooker {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    return try {
                        chain.proceed()
                    } catch (e: Throwable) {
                        val ctx = chain.thisObject as? Context
                        ctx?.applicationContext ?: chain.thisObject
                    }
                }
            })
        } catch (_: Throwable) {}

        // حماية تحويل الأرقام (Double.parseDouble) من التسبب بانهيار شاشة البداية
        try {
            val mParseDouble = Double::class.java.getDeclaredMethod("parseDouble", String::class.java)
            hook(mParseDouble).intercept(object : XposedInterface.Hooker {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    val input = chain.args.getOrNull(0) as? String
                    return try {
                        chain.proceed()
                    } catch (_: Throwable) {
                        0.0
                    }
                }
            })
        } catch (_: Throwable) {}
    }

    private fun hookJahezDeepGuard(classLoader: ClassLoader) {
        // إجبار دالة MainActivity.onCreate على الإقلاع دائماً مهما حدث في هوك QBZ
        try {
            val actClass = classLoader.loadClass("net.jahez.fleets.MainActivity")
            for (m in actClass.declaredMethods) {
                if (m.name == "onCreate") {
                    hook(m).intercept(object : XposedInterface.Hooker {
                        override fun intercept(chain: XposedInterface.Chain): Any? {
                            return try {
                                chain.proceed()
                            } catch (t: Throwable) {
                                Log.e(TAG, "Prevented Jahez Splash Hang: ${t.message}", t)
                                null
                            }
                        }
                    })
                }
            }
        } catch (_: Throwable) {}

        // حماية معترض QBZ الداخلي (Ooo0ooo0oOoo00O) لضمان استدعاء proceed دائماً
        try {
            val hookerClass = classLoader.loadClass("Ooo0ooo0oOoo00O")
            for (m in hookerClass.declaredMethods) {
                if (m.name == "intercept") {
                    hook(m).intercept(object : XposedInterface.Hooker {
                        override fun intercept(chain: XposedInterface.Chain): Any? {
                            val innerChain = chain.args.getOrNull(0) as? XposedInterface.Chain
                            return try {
                                chain.proceed()
                            } catch (t: Throwable) {
                                Log.w(TAG, "QBZ Hooker crashed in Jahez, forcing proceed(): ${t.message}")
                                innerChain?.proceed()
                            }
                        }
                    })
                }
            }
        } catch (_: Throwable) {}
    }

    /**
     * 2. تفعيل تطبيق QBZ وإتاحة الديالوج للمفتاح 1234
     */
    private fun hookQbzDialogAndLicense() {
        // حقن بيانات الاشتراك السليم في ذاكرة QBZ
        try {
            val mResume = Activity::class.java.getDeclaredMethod("onResume")
            hook(mResume).intercept(object : XposedInterface.Hooker {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    val res = chain.proceed()
                    val act = chain.thisObject as? Activity ?: return res
                    if (act.packageName != "net.jahez.lsposed") return res

                    try {
                        val spSub = act.getSharedPreferences("net.jahez.lsposed.Subscription", Context.MODE_PRIVATE)
                        spSub.edit()
                            .putBoolean("isActive", true)
                            .putBoolean("isValid", true)
                            .putBoolean("success", true)
                            .putString("status", "active")
                            .putString("token", "1234")
                            .putString("login_key", "1234")
                            .putString("key", "1234")
                            .putString("refreshToken", "1234")
                            .putString("subsStartDate", "2024-01-01")
                            .putString("subsEndDate", "2099-12-31")
                            .putLong("subsEndTimestamp", 4102444800000L)
                            .putLong("tokenExpiry", 4102444800000L)
                            .putLong("tokenExpiryTime", 4102444800000L)
                            .apply()

                        val spSettings = act.getSharedPreferences("net.jahez.fleets.Settings", Context.MODE_PRIVATE)
                        spSettings.edit()
                            .putBoolean("isLoggedIn", true)
                            .putBoolean("isActive", true)
                            .putString("login_key", "1234")
                            .putString("pickup_distance", "15.0")
                            .putString("drop_distance", "20.0")
                            .putString("delivery_price", "0.0")
                            .putString("price", "0.0")
                            .apply()
                    } catch (_: Throwable) {}

                    return res
                }
            })
        } catch (_: Throwable) {}

        // إتاحة ظهور الديالوج مع التعبئة التلقائية للمفتاح 1234
        try {
            val mShow = Dialog::class.java.getDeclaredMethod("show")
            hook(mShow).intercept(object : XposedInterface.Hooker {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    val res = chain.proceed()
                    val dialog = chain.thisObject as? Dialog
                    if (dialog != null && dialog.context.packageName == "net.jahez.lsposed") {
                        dialog.setOnShowListener {
                            try {
                                val window = dialog.window ?: return@setOnShowListener
                                findAndFillKey(window.decorView, "1234")
                                Toast.makeText(dialog.context, "🔑 Key 1234 Ready", Toast.LENGTH_SHORT).show()
                            } catch (_: Throwable) {}
                        }
                    }
                    return res
                }
            })
        } catch (_: Throwable) {}
    }

    private fun hookQbzValidation(classLoader: ClassLoader) {
        // التحقق من صحة المفتاح 1234 كصالح دائماً
        try {
            val kClass = classLoader.loadClass("O00oOOOo0o0OOo0")
            for (m in kClass.declaredMethods) {
                if (m.returnType == Boolean::class.javaPrimitiveType || m.returnType == java.lang.Boolean::class.java) {
                    hook(m).intercept(object : XposedInterface.Hooker {
                        override fun intercept(chain: XposedInterface.Chain): Any? = true
                    })
                }
            }
        } catch (_: Throwable) {}

        // تجاوز خطأ الهوك في QBZ
        try {
            val xpClass = classLoader.loadClass("net.jahez.lsposed.xposed.hooks.XposedInit")
            for (m in xpClass.declaredMethods) {
                if (m.returnType == Boolean::class.javaPrimitiveType || m.returnType == java.lang.Boolean::class.java) {
                    hook(m).intercept(object : XposedInterface.Hooker {
                        override fun intercept(chain: XposedInterface.Chain): Any? = true
                    })
                }
            }
        } catch (_: Throwable) {}
    }

    private fun findAndFillKey(view: View, key: String) {
        if (view is EditText) {
            view.setText(key)
            view.setSelection(key.length)
            return
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findAndFillKey(view.getChildAt(i), key)
            }
        }
    }
}
