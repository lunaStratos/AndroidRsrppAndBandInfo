package com.example.signaltest

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.telephony.*
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.util.*

/*
* 2021-03-24
* create by lostman
* */

class MainActivity : AppCompatActivity() {

    //id 설정
    private lateinit var signalRowTxt:TextView
    private lateinit var signalDbm:TextView
    private lateinit var signalGrade:TextView
    private lateinit var signalNetworkType:TextView
    private lateinit var signalMHz:TextView
    private lateinit var signalCarrierName:TextView
    private lateinit var signalEtcRow:TextView


    override fun onStart() {
        super.onStart()

    }
    
    fun idBind(){
        //id 묶기
        signalRowTxt = findViewById<TextView>(R.id.signalRowTxt)
        signalNetworkType = findViewById<TextView>(R.id.signalNetworkType)
        signalDbm = findViewById<TextView>(R.id.signalDbm)
        signalGrade = findViewById<TextView>(R.id.signalGrade)
        signalMHz = findViewById<TextView>(R.id.signalMHz)
        signalCarrierName = findViewById<TextView>(R.id.signalCarrierName)
        signalEtcRow = findViewById<TextView>(R.id.signalEtcRow)

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
            
        //id 묶기
        idBind()

        //권한 permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val res = checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
            if (res != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), 123)
            }

            val res2 = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (res2 != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 123)
            }

            val res3 = checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)
            if (res3 != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_NETWORK_STATE), 123)
            }

            val res4 = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            if (res4 != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 123)
            }
        }

        //텔레폰메니져
        var telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        // 연결 확인
        val conMan = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mobile = conMan.getNetworkInfo(0)!!.state
        if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
            //모바일 연결 확인
        }

        val ci = telephonyManager.allCellInfo[0] as CellInfoLte

        // 네트워크 종류
        signalNetworkType.text = "네트워크 : " + networkType(telephonyManager.dataNetworkType)
        // 캐리어이름
        signalCarrierName.text = "사용 캐리어 : " + telephonyManager.networkOperatorName
        //네트워크 밴드
        signalMHz.text = "사용밴드 : " + networkBand(ci.cellIdentity.earfcn) + "MHz"
        signalEtcRow.text = ci.toString()

        // signal RSRP catch
        val phoneListener = object : PhoneStateListener() {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                super.onSignalStrengthsChanged(signalStrength)
                var mSignalStrength = signalStrength.cellSignalStrengths

                val signalStrength = 2 * signalStrength.gsmSignalStrength - 113
                signalRowTxt.text = mSignalStrength.toString()
                val resultSignal = mSignalStrength.toString().split(" ")[2].replace("rsrp=", "").toInt()
                signalDbm.text = resultSignal.toString()

                var signalGradeTxt = ""
                if(resultSignal >= -80){
                    signalGradeTxt = ("Excellent")
                }else if(resultSignal <= -80 && resultSignal >= -90){
                    signalGradeTxt = ("Good")
                }else if(resultSignal <= -90 && resultSignal >= -100){
                    signalGradeTxt = ("poor")
                }else if(resultSignal <= -100){
                    signalGradeTxt = ("No signal")
                }
                signalGrade.text = signalGradeTxt

            }
        }
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        //1초마다 알아내기
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            @RequiresApi(Build.VERSION_CODES.P)
            override fun run() {
                telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
            }
        }, 1000, 1000)


    }

    //네트워크 종류 알아내기
    private fun networkType(type: Int): String? {
        when (type) {
            TelephonyManager.NETWORK_TYPE_1xRTT -> return "1xRTT"
            TelephonyManager.NETWORK_TYPE_CDMA -> return "CDMA"
            TelephonyManager.NETWORK_TYPE_EDGE -> return "EDGE"
            TelephonyManager.NETWORK_TYPE_EHRPD -> return "eHRPD"
            TelephonyManager.NETWORK_TYPE_EVDO_0 -> return "EVDO rev. 0"
            TelephonyManager.NETWORK_TYPE_EVDO_A -> return "EVDO rev. A"
            TelephonyManager.NETWORK_TYPE_EVDO_B -> return "EVDO rev. B"
            TelephonyManager.NETWORK_TYPE_GPRS -> return "GPRS"
            TelephonyManager.NETWORK_TYPE_HSDPA -> return "HSDPA"
            TelephonyManager.NETWORK_TYPE_HSPA -> return "HSPA"
            TelephonyManager.NETWORK_TYPE_HSPAP -> return "HSPA+"
            TelephonyManager.NETWORK_TYPE_HSUPA -> return "HSUPA"
            TelephonyManager.NETWORK_TYPE_IDEN -> return "iDen"
            TelephonyManager.NETWORK_TYPE_LTE -> return "LTE"
            TelephonyManager.NETWORK_TYPE_UMTS -> return "UMTS"
            TelephonyManager.NETWORK_TYPE_UNKNOWN -> return "Unknown"
        }
        throw RuntimeException("New type of network")
    }

    // 네트워크 밴드 알아내기
    // 참조 : https://namu.wiki/w/LTE
    private fun networkBand(band: Int): String? {
        var result = ""
        when (band) {
            //SKT
            275 -> result = "2100"
            1350-> result = "1800"
            2500 -> result = "850"
            2850 -> result = "2600"
            3200 -> result = "2600"
            //KT
            475 -> result = "2100"
            1550-> result = "1800"
            1694 -> result = "1800"
            3743 -> result = "900"
            //LGT
            100 -> result = "2100"
            2600-> result = "850"
            3050-> result = "2600"
        }
        return result
        throw RuntimeException("New type of network")


    }

}