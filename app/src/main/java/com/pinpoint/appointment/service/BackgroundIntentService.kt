package com.pinpoint.appointment.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.location.Location
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk
import com.pinpoint.appointment.BaseApplication
import com.pinpoint.appointment.R
import com.pinpoint.appointment.api.ApiList
import com.pinpoint.appointment.api.RequestCode
import com.pinpoint.appointment.api.RestClient
import com.pinpoint.appointment.api.ServerConfig
import com.pinpoint.appointment.models.LoginHelper
import com.pinpoint.appointment.utils.Constants
import com.pinpoint.appointment.utils.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val NOTIF_CHANNEL_ID = "notification_location_update"
const val NOTIF_CHANNEL_NAME = "Notification Location Update"
const val LOCATION_UPDATE_SERVICE = 742

class BackgroundIntentService: IntentService("LocationUpdateService") {

    private val binder = LocationServiceBinder()

    inner class LocationServiceBinder : Binder() {
        val service: BackgroundIntentService
            get() = this@BackgroundIntentService
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    companion object{
        fun startLocationTracking(context: Context, app: Application? = null, serviceConnection: ServiceConnection? = null){
            if(!Util.isServiceRunning(context, BackgroundIntentService::class.java.name)) {
                val intent = Intent(context, BackgroundIntentService::class.java)
                context.startService(intent)

                serviceConnection?.apply {
                     app?.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                }
            }
        }

        fun stopLocationTracking(context: Context){
            if(Util.isServiceRunning(context, BackgroundIntentService::class.java.name))
                context.stopService(Intent(context, BackgroundIntentService::class.java))
        }
    }

    private val notificationManager: NotificationManager? by lazy(mode = LazyThreadSafetyMode.NONE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) getSystemService(NotificationManager::class.java)
        else null
    }

    private var updateCounter = 0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            locationResult?.lastLocation?.let {
                //locationUpdateHelper.postUpdate(it)
                Log.e("TBL", "location: ${Gson().toJson(it)}}")
                location = it
                Hawk.put(Constants.P_KEY_LOCATION, it)

                (application as BaseApplication).locationBus().send(it)

                updateCounter++
                if(updateCounter == 2){
                    sendLocationToServer()
                    updateCounter = 0
                }
            }
        }
    }

    @Inject
    lateinit var locationProvider: FusedLocationProviderClient
    @Inject
    lateinit var locationRequest: LocationRequest

    private val userId = LoginHelper.getInstance().userID
    private var location: Location? = null

    private val disposables = CompositeDisposable()

    private fun stopLocationUpdate(){
        locationProvider.removeLocationUpdates(locationCallback)
    }

    override fun onCreate() {
        super.onCreate()
        (application as BaseApplication).appComponent.inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        markForeground()
        //startLocationUpdates()
        //startStatusUpdate()
        setupBus()

        return Service.START_STICKY
    }

    private fun setupBus() {
        disposables.add(
                (application as BaseApplication).bus().toObservable().subscribe { obj ->
                    val action = when (obj) {
                        is String -> obj
                        else -> null
                    }

                    action?.apply {
                        when(this){
                            Constants.BUS_ACTION_START_LOCATION_TRACKING ->{
                                startLocationUpdates()
                            }
                            Constants.BUS_ACTION_STOP_LOCATION_TRACKING ->{
                                stopLocationUpdate()
                            }
                            Constants.BUS_ACTION_START_STATUS_UPDATE ->{
                                startStatusUpdate()
                            }
                            Constants.BUS_ACTION_STOP_STATUS_UPDATE ->{
                                cDisposable.clear()
                            }
                        }
                    }
                }
        )
    }

    override fun onHandleIntent(p0: Intent?) {}

    private fun markForeground() {
        startForeground(LOCATION_UPDATE_SERVICE, notification)
    }

    private fun startLocationUpdates() {
        locationProvider.removeLocationUpdates(locationCallback)
        locationProvider.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private val cDisposable = CompositeDisposable()
    private var requestQueue: RequestQueue? = null

    private fun startStatusUpdate(){
        cDisposable.add(
                Observable.interval(0, 2,TimeUnit.MINUTES)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            onUpdateStatus()
                        }, {
                            it.printStackTrace()
                        })
        )
    }

    private fun onUpdateStatus(){

        if(requestQueue == null)
            requestQueue = Volley.newRequestQueue(this)

        val param = JSONObject()
        param.put(ApiList.KEY_USERID, userId)
        param.put(ApiList.KEY_STATUS, "1")

        cDisposable.add(
            RestClient.instance.post(  ServerConfig.SERVER_URL + ApiList.APIs.setuserstatus.url, param, RequestCode.setuserstatus, requestQueue)
                    .subscribeOn(Schedulers.io())
                    .subscribe({},{})
        )
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        stopLocationUpdate()
        super.onDestroy()
    }

    fun stopForeground(){
        stopForeground(true)
    }

    private val notification: Notification
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                        NOTIF_CHANNEL_ID,
                        NOTIF_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_MIN
                )
                notificationManager?.createNotificationChannel(channel)
            }
            return NotificationCompat.Builder(applicationContext, NOTIF_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_logo))
                    .setContentTitle("PinPoint App")
                    .setContentText("This app is monitoring your location")
                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                    .setGroup("No Sound Group")
                    .setGroupSummary(false)
                    .setSound(null, AudioManager.STREAM_SYSTEM)
                    .setAutoCancel(true).build()
        }


    private fun sendLocationToServer(){
        location?.apply {
            val params = JSONObject()
            params.put(ApiList.KEY_USERID, userId)
            params.put(ApiList.KEY_LATITUDE, latitude)
            params.put(ApiList.KEY_LONGITUDE, longitude)
            params.put(ApiList.KEY_STATUS, "1")

            RestClient.getInstance().postForService(
                    this@BackgroundIntentService, Request.Method.POST,
                    ApiList.APIs.addfriendlocation.url, params, null,
                    RequestCode.addfriendlocation , false
            )
        }

    }

}
