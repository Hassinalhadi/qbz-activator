package dev.qbz.activator

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

class HookEntry : XposedModule() {

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        if (param.packageName != "net.jahez.lsposed" && param.packageName != "com.muzahid.gps") return
        hookLifecycle()
    }

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        if (param.packageName != "net.jahez.lsposed" && param.packageName != "com.muzahid.gps") return
        val cl = param.classLoader ?: return
        hookApp(param.packageName, cl)
    }

    private fun hookLifecycle() {
        // 1. Hook Activity.onResume to auto-inject subscription and bypass login screens
        try {
            val mResume = Activity::class.java.getDeclaredMethod("onResume")
            hook(mResume).intercept(object : XposedInterface.Hooker {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    val res = chain.proceed()
                    val act = chain.thisObject as? Activity ?: return res
                    val pkg = act.packageName

                    // Target 1: net.jahez.lsposed (QBZ.apk)
                    if (pkg == "net.jahez.lsposed") {
                        try {
                            val spSub = act.getSharedPreferences("net.jahez.lsposed.Subscription", Context.MODE_PRIVATE)
                            spSub.edit()
                                .putBoolean("isActive", true)
                                .putBoolean("isValid", true)
                                .putBoolean("success", true)
                                .putString("status", "active")
                                .putString("token", "VIP_SANED_ACTIVE_TOKEN")
                                .putString("refreshToken", "VIP_SANED_REFRESH_TOKEN")
                                .putString("subsStartDate", "2024-01-01")
                                .putString("subsEndDate", "2099-12-31")
                                .putLong("subsEndTimestamp", 4102444800000L)
                                .putLong("tokenExpiry", 4102444800000L)
                                .putLong("tokenExpiryTime", 4102444800000L)
                                .putLong("refreshTokenExpiry", 4102444800000L)
                                .putLong("refreshTokenExpiryTime", 4102444800000L)
                                .apply()

                            val spSettings = act.getSharedPreferences("net.jahez.fleets.Settings", Context.MODE_PRIVATE)
                            spSettings.edit()
                                .putBoolean("isLoggedIn", true)
                                .putBoolean("isActive", true)
                                .apply()

                            val spFleets = act.getSharedPreferences("net.jahez.fleets.SharedPreferences", Context.MODE_PRIVATE)
                            spFleets.edit()
                                .putBoolean("isLoggedIn", true)
                                .putString("token", "VIP_SANED_ACTIVE_TOKEN")
                                .apply()
                        } catch (_: Throwable) {}

                        // Auto-bypass login activity directly to MainActivity
                        if (act.javaClass.name.contains("LoginActivity")) {
                            try {
                                val intent = Intent().apply {
                                    setClassName("net.jahez.lsposed", "net.jahez.lsposed.ui.main.MainActivity")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                act.startActivity(intent)
                                act.finish()
                            } catch (_: Throwable) {}
                        }

                        try {
                            Toast.makeText(act, "⚡ QBZ VIP Activated! (License Bypassed)", Toast.LENGTH_SHORT).show()
                        } catch (_: Throwable) {}
                    }

                    // Target 2: com.muzahid.gps
                    if (pkg == "com.muzahid.gps") {
                        try {
                            val sp = act.getSharedPreferences("gps", Context.MODE_PRIVATE)
                            sp.edit()
                                .putString("get", "VIP_ACTIVE")
                                .putBoolean("first_login_shown", true)
                                .putBoolean("KEY_FIRST_LOGIN", false)
                                .putString("KEY_TOKEN", "VIP_ACTIVE_TOKEN")
                                .apply()
                        } catch (_: Throwable) {}
                        try {
                            Toast.makeText(act, "⚡ GPS VIP Activated!", Toast.LENGTH_SHORT).show()
                        } catch (_: Throwable) {}
                    }

                    return res
                }
            })
        } catch (_: Throwable) {}

        // 2. Hook Dialog.show to dismiss any activation prompt or key alert
        try {
            val mShow = Dialog::class.java.getDeclaredMethod("show")
            hook(mShow).intercept(object : XposedInterface.Hooker {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    val dialog = chain.thisObject as? Dialog
                    if (dialog != null) {
                        try {
                            val pkg = dialog.context.packageName
                            val ownerPkg = dialog.ownerActivity?.packageName
                            if (pkg == "net.jahez.lsposed" || ownerPkg == "net.jahez.lsposed" ||
                                pkg == "com.muzahid.gps" || ownerPkg == "com.muzahid.gps") {
                                dialog.setOnShowListener { d ->
                                    try {
                                        d.dismiss()
                                    } catch (_: Throwable) {}
                                }
                                try {
                                    dialog.dismiss()
                                } catch (_: Throwable) {}
                                return null
                            }
                        } catch (_: Throwable) {}
                    }
                    return chain.proceed()
                }
            })
        } catch (_: Throwable) {}
    }

    private fun hookApp(packageName: String, classLoader: ClassLoader) {
        if (packageName == "net.jahez.lsposed") {
            // Suppress showDialog method if invoked
            try {
                val dlgClass = classLoader.loadClass("Ooo0O0oOooOOOO0")
                for (m in dlgClass.declaredMethods) {
                    if (m.name == "showDialog") {
                        hook(m).intercept(object : XposedInterface.Hooker {
                            override fun intercept(chain: XposedInterface.Chain): Any? = null
                        })
                    }
                }
            } catch (_: Throwable) {}

            // Suppress Xposed hooks error check
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

        if (packageName == "com.muzahid.gps") {
            try {
                val vmClass = classLoader.loadClass("com.muzahid.gps.ui.viewmodel.MainViewModel")
                for (m in vmClass.declaredMethods) {
                    if (m.name == "isXposed") {
                        hook(m).intercept(object : XposedInterface.Hooker {
                            override fun intercept(chain: XposedInterface.Chain): Any? {
                                val liveData = chain.proceed()
                                if (liveData != null) {
                                    try {
                                        for (sm in liveData.javaClass.methods) {
                                            if (sm.name == "postValue" || sm.name == "setValue") {
                                                sm.isAccessible = true
                                                sm.invoke(liveData, java.lang.Boolean.TRUE)
                                            }
                                        }
                                    } catch (_: Throwable) {}
                                }
                                return liveData
                            }
                        })
                    }
                }
            } catch (_: Throwable) {}

            try {
                val lpClass = classLoader.loadClass("androidx.core.location.location_providor")
                for (m in lpClass.declaredMethods) {
                    when (m.name) {
                        "l", "geoapi" -> {
                            hook(m).intercept(object : XposedInterface.Hooker {
                                override fun intercept(chain: XposedInterface.Chain): Any? = null
                            })
                        }
                        "i", "h", "g" -> {
                            hook(m).intercept(object : XposedInterface.Hooker {
                                override fun intercept(chain: XposedInterface.Chain): Any? = true
                            })
                        }
                    }
                }
            } catch (_: Throwable) {}
        }
    }
}
