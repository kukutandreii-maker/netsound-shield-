package com.netsound.shield.services

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log

class GameVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "STOP") {
            stopVpn()
            return START_NOT_STICKY
        }

        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        try {
            val builder = Builder()
                .addAddress("10.1.10.1", 24)
                .addRoute("0.0.0.0", 0)
                .setSession("NetSoundShieldPUBG")

            val pubgPackages = listOf(
                "com.tencent.ig",
                "com.pubg.krmobile",
                "com.vng.pubgmobile"
            )

            for (pkg in pubgPackages) {
                try {
                    builder.addAllowedApplication(pkg)
                } catch (e: Exception) {
                    Log.w("VpnService", "Пакет $pkg не найден")
                }
            }

            vpnInterface = builder.establish()
            Log.d("VpnService", "VPN Фаервол запущен для PUBG!")
        } catch (e: Exception) {
            Log.e("VpnService", "Ошибка запуска VPN", e)
            stopSelf()
        }
    }

    private fun stopVpn() {
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            Log.e("VpnService", "Ошибка остановки", e)
        }
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
