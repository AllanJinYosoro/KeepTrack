package com.example.keeptrack.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.keeptrack.MainActivity
import com.example.keeptrack.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TimerService : Service() {

    private val binder = TimerBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private val _secondsElapsed = MutableStateFlow(0L)
    val secondsElapsed: StateFlow<Long> = _secondsElapsed

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private var timerJob: Job? = null
    private var isAppInBackground = false

    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private var bubbleParams: WindowManager.LayoutParams? = null

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        isAppInBackground = false
                        hideFloatingBubble()
                    }
                    Lifecycle.Event.ON_STOP -> {
                        isAppInBackground = true
                        if (_isRunning.value) {
                            showFloatingBubble()
                        }
                    }
                    else -> {}
                }
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START -> startTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_NOT_STICKY
    }

    private fun startTimer() {
        if (!_isRunning.value) {
            _isRunning.value = true
            startForeground(NOTIFICATION_ID, createNotification())
            tick()
        }
    }

    private fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
        updateNotification()
        updateBubbleUI()
    }

    private fun resumeTimer() {
        if (!_isRunning.value) {
            _isRunning.value = true
            tick()
            updateNotification()
            updateBubbleUI()
        }
    }

    private fun stopTimer() {
        _isRunning.value = false
        timerJob?.cancel()
        _secondsElapsed.value = 0
        hideFloatingBubble()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun tick() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive && _isRunning.value) {
                delay(1000)
                _secondsElapsed.value++
                updateNotification()
                updateBubbleUI()
            }
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("计时进行中")
            .setContentText(formatTime(_secondsElapsed.value))
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Timer Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun showFloatingBubble() {
        if (bubbleView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }
        bubbleParams = params

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // Since we don't have a layout XML for the bubble yet, we'll create it programmatically or use a simple view
        // For simplicity in this step, let's create a simple layout programmatically
        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.argb(200, 0, 0, 0))
            setPadding(20, 20, 20, 20)
        }

        val timeText = TextView(this).apply {
            id = View.generateViewId()
            setTextColor(android.graphics.Color.WHITE)
            textSize = 18f
            text = formatTime(_secondsElapsed.value)
        }
        container.addView(timeText)

        val btnToggle = Button(this).apply {
            id = View.generateViewId()
            text = if (_isRunning.value) "暂停" else "开始"
            setOnClickListener {
                if (_isRunning.value) pauseTimer() else resumeTimer()
            }
        }
        container.addView(btnToggle)

        container.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(container, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        // If it's a click (minimal movement), bring app to front
                        if (Math.abs(event.rawX - initialTouchX) < 10 && Math.abs(event.rawY - initialTouchY) < 10) {
                            val intent = Intent(this@TimerService, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            startActivity(intent)
                        }
                        return true
                    }
                }
                return false
            }
        })

        bubbleView = container
        try {
            windowManager.addView(container, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideFloatingBubble() {
        bubbleView?.let {
            windowManager.removeView(it)
            bubbleView = null
        }
    }

    private fun updateBubbleUI() {
        if (!isAppInBackground) return
        
        serviceScope.launch {
            bubbleView?.let { container ->
                val timeText = (container as android.widget.LinearLayout).getChildAt(0) as TextView
                val btnToggle = container.getChildAt(1) as Button
                timeText.text = formatTime(_secondsElapsed.value)
                btnToggle.text = if (_isRunning.value) "暂停" else "开始"
            }
        }
    }

    private fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return "%02d:%02d:%02d".format(h, m, s)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        hideFloatingBubble()
    }

    companion object {
        const val ACTION_START = "com.example.keeptrack.START"
        const val ACTION_PAUSE = "com.example.keeptrack.PAUSE"
        const val ACTION_RESUME = "com.example.keeptrack.RESUME"
        const val ACTION_STOP = "com.example.keeptrack.STOP"

        private const val CHANNEL_ID = "TimerServiceChannel"
        private const val NOTIFICATION_ID = 1
    }
}
