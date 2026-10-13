package dev.qbz.activator

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

class HookEntry : XposedModule() {

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        if (param.packageName != "net.jahez.lsposed" && param.packageName != "com.muzahid.gps") return
        hookDialogAndAuth()
    }

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        if (param.packageName != "net.jahez.lsposed" && param.packageName != "com.muzahid.gps") return
        val cl = param.classLoader ?: return
        hookValidationLogic(cl)
    }

    private fun hookDialogAndAuth() {
        // 1. Hook Activity.onResume: Inject valid subscription settings without killing the Activity
        try {
            val mResume = Activity::class.java.getDeclaredMethod("onResume")
            hook(mResume).intercept(object : XposedInterface.Hooker {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    val res = chain.proceed()
                    val act = chain.thisObject as? Activity ?: return res
                    val pkg = act.packageName

                    if (pkg == "net.jahez.lsposed") {
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
                                .apply()

                            val spFleets = act.getSharedPreferences("net.jahez.fleets.SharedPreferences", Context.MODE_PRIVATE)
                            spFleets.edit()
                                .putBoolean("isLoggedIn", true)
                                .putString("token", "1234")
                                .apply()
                        } catch (_: Throwable) {}
                    }
                    return res
                }
            })
        } catch (_: Throwable) {}

        // 2. Hook Dialog.show: DO NOT DISMISS! Allow dialog to show and pre-fill "1234" into EditText
        try {
            val mShow = Dialog::class.java.getDeclaredMethod("show")
            hook(mShow).intercept(object : XposedInterface.Hooker {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    // Let the dialog show normally so the user can interact with it
                    val res = chain.proceed()
                    val dialog = chain.thisObject as? Dialog
                    if (dialog != null) {
                        val pkg = dialog.context.packageName
                        val ownerPkg = dialog.ownerActivity?.packageName
                        if (pkg == "net.jahez.lsposed" || ownerPkg == "net.jahez.lsposed") {
                            try {
                                dialog.setOnShowListener { d ->
                                    try {
                                        val window = dialog.window ?: return@setOnShowListener
                                        findAndFillKey(window.decorView, "1234")
                                        Toast.makeText(dialog.context, "🔑 Enter key 1234 to activate", Toast.LENGTH_SHORT).show()
                                    } catch (_: Throwable) {}
                                }
                            } catch (_: Throwable) {}
                        }
                    }
                    return res
                }
            })
        } catch (_: Throwable) {}

        // 3. Hook String.equals to make key "1234" always match valid license checks
        try {
            val mEquals = String::class.java.getDeclaredMethod("equals", Any::class.java)
            hook(mEquals).intercept(object : XposedInterface.Hooker {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    val s1 = chain.thisObject as? String
                    val s2 = chain.args.getOrNull(0) as? String
                    if (s1 == "1234" || s2 == "1234") {
                        return true
                    }
                    return chain.proceed()
                }
            })
        } catch (_: Throwable) {}
    }

    private fun hookValidationLogic(classLoader: ClassLoader) {
        // 1. Hook O00oOOOo0o0OOo0 key validation methods in net.jahez.lsposed
        try {
            val kClass = classLoader.loadClass("O00oOOOo0o0OOo0")
            for (m in kClass.declaredMethods) {
                if (m.returnType == Boolean::class.javaPrimitiveType || m.returnType == java.lang.Boolean::class.java) {
                    hook(m).intercept(object : XposedInterface.Hooker {
                        override fun intercept(chain: XposedInterface.Chain): Any? {
                            val arg0 = chain.args.getOrNull(0)?.toString()
                            if (arg0 == "1234" || chain.args.isEmpty()) {
                                return true
                            }
                            return true // Always validate key as true
                        }
                    })
                }
            }
        } catch (_: Throwable) {}

        // 2. Hook XposedInit to bypass "Error in Xposed hooks"
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

        // 3. Hook PineXposed error suppressor
        try {
            val pineClass = classLoader.loadClass("net.jahez.lsposed.xposed.hooks.PineXposed")
            for (m in pineClass.declaredMethods) {
                if (m.name == "Hooks") {
                    hook(m).intercept(object : XposedInterface.Hooker {
                        override fun intercept(chain: XposedInterface.Chain): Any? = null
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
