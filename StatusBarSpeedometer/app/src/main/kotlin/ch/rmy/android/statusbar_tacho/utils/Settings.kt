package ch.rmy.android.statusbar_tacho.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.intl.Locale
import androidx.core.content.edit
import ch.rmy.android.statusbar_tacho.units.SpeedUnit
import ch.rmy.android.statusbar_tacho.views.GaugeScale
import ch.rmy.android.statusbar_tacho.views.ThemeId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object Settings {

    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        _isRunningFlow.value = preferences.getBoolean(PREF_SERVICE, false)
        _unitFlow.value = SpeedUnit.valueOf(preferences.getString(PREF_SPEED_UNIT, getDefaultUnit().name)!!)
        _themeIdFlow.value = ThemeId.entries.getOrElse(preferences.getInt(PREF_THEME, 0)) { ThemeId.DEFAULT }
        _gaugeScaleFlow.value = GaugeScale.entries.getOrElse(preferences.getInt(PREF_GAUGE_SCALE, 0)) { GaugeScale.FAST }
        _topSpeedFlow.value = preferences.getFloat(PREF_TOP_SPEED, -1f).takeUnless { it == -1f }
        _speedLimitFlow.value = preferences.getFloat(PREF_SPEED_LIMIT, 0f)
        _vibrationThreshold1Flow.value = preferences.getInt(PREF_VIBRATION_THRESHOLD_1, 27)
        _vibrationThreshold2Flow.value = preferences.getInt(PREF_VIBRATION_THRESHOLD_2, 47)
        _vibrationThreshold3Flow.value = preferences.getInt(PREF_VIBRATION_THRESHOLD_3, 57)
        _speedIntervalFlow.value = preferences.getInt(PREF_SPEED_INTERVAL, 8)
        _vibrationDurationFlow.value = preferences.getInt(PREF_VIBRATION_DURATION, 2000)
        _vibrationAmplitudeFlow.value = preferences.getInt(PREF_VIBRATION_AMPLITUDE, 50)
    }

    private lateinit var preferences: SharedPreferences

    private const val PREF = "pref"
    private const val PREF_SERVICE = "service"
    private const val PREF_SPEED_UNIT = "speed_unit"
    private const val PREF_FIRST_RUN = "first_run"
    private const val PREF_KEEP_UPDATING_WHILE_SCREEN_OFF = "keep_updating_while_screen_off"
    private const val PREF_THEME = "theme"
    private const val PREF_GAUGE_SCALE = "gauge_scale"
    private const val PREF_TOP_SPEED = "top_speed"
    private const val PREF_SPEED_LIMIT = "speed_limit"
    private const val PREF_VIBRATION_THRESHOLD_1 = "vibration_threshold_1"
    private const val PREF_VIBRATION_THRESHOLD_2 = "vibration_threshold_2"
    private const val PREF_VIBRATION_THRESHOLD_3 = "vibration_threshold_3"
    private const val PREF_SPEED_INTERVAL = "speed_interval"
    private const val PREF_VIBRATION_DURATION = "vibration_duration"
    private const val PREF_VIBRATION_AMPLITUDE = "vibration_amplitude"

    var isRunning: Boolean
        get() = _isRunningFlow.value
        set(running) {
            _isRunningFlow.value = running
            preferences.edit {
                putBoolean(PREF_SERVICE, running)
            }
        }

    private val _isRunningFlow = MutableStateFlow(false)

    @Stable
    val isRunningFlow = _isRunningFlow.asStateFlow()

    private val _unitFlow = MutableStateFlow(SpeedUnit.METERS_PER_SECOND)

    @Stable
    val unitFlow = _unitFlow.asStateFlow()

    var unit: SpeedUnit
        get() = _unitFlow.value
        set(value) = preferences.edit {
            _unitFlow.value = value
            putString(PREF_SPEED_UNIT, value.name)
        }

    private fun getDefaultUnit() = SpeedUnit.KILOMETERS_PER_HOUR

    var isFirstRun: Boolean
        get() = preferences.getBoolean(PREF_FIRST_RUN, true)
        set(value) = preferences.edit {
            putBoolean(PREF_FIRST_RUN, value)
        }

    var shouldKeepUpdatingWhileScreenIsOff: Boolean
        get() = preferences.getBoolean(PREF_KEEP_UPDATING_WHILE_SCREEN_OFF, false)
        set(value) = preferences.edit {
            putBoolean(PREF_KEEP_UPDATING_WHILE_SCREEN_OFF, value)
        }

    private val _themeIdFlow = MutableStateFlow(ThemeId.DEFAULT)

    @Stable
    val themeIdFlow = _themeIdFlow.asStateFlow()

    var themeId: ThemeId
        get() = _themeIdFlow.value
        set(value) = preferences.edit {
            _themeIdFlow.value = value
            putInt(PREF_THEME, value.ordinal)
        }

    private val _gaugeScaleFlow = MutableStateFlow(GaugeScale.FAST)

    @Stable
    val gaugeScaleFlow = _gaugeScaleFlow.asStateFlow()

    var gaugeScale: GaugeScale
        get() = _gaugeScaleFlow.value
        set(value) = preferences.edit {
            _gaugeScaleFlow.value = value
            putInt(PREF_GAUGE_SCALE, value.ordinal)
        }

    private val _topSpeedFlow = MutableStateFlow<Float?>(null)

    @Stable
    val topSpeedFlow = _topSpeedFlow.asStateFlow()

    var topSpeed: Float?
        get() = _topSpeedFlow.value
        set(value) = preferences.edit {
            _topSpeedFlow.value = value
            if (value != null) {
                putFloat(PREF_TOP_SPEED, value)
            } else {
                remove(PREF_TOP_SPEED)
            }
        }

    private val _speedLimitFlow = MutableStateFlow(0f)

    @Stable
    val speedLimitFlow = _speedLimitFlow.asStateFlow()

    var speedLimit: Float
        get() = _speedLimitFlow.value
        set(value) = preferences.edit {
            _speedLimitFlow.value = value
            putFloat(PREF_SPEED_LIMIT, value)
        }

    private val _vibrationThreshold1Flow = MutableStateFlow(0)

    @Stable
    val vibrationThreshold1Flow = _vibrationThreshold1Flow.asStateFlow()

    var vibrationThreshold1: Int
        get() = _vibrationThreshold1Flow.value
        set(value) = preferences.edit {
            _vibrationThreshold1Flow.value = value
            putInt(PREF_VIBRATION_THRESHOLD_1, value)
        }

    private val _vibrationThreshold2Flow = MutableStateFlow(0)

    @Stable
    val vibrationThreshold2Flow = _vibrationThreshold2Flow.asStateFlow()

    var vibrationThreshold2: Int
        get() = _vibrationThreshold2Flow.value
        set(value) = preferences.edit {
            _vibrationThreshold2Flow.value = value
            putInt(PREF_VIBRATION_THRESHOLD_2, value)
        }

    private val _vibrationThreshold3Flow = MutableStateFlow(0)

    @Stable
    val vibrationThreshold3Flow = _vibrationThreshold3Flow.asStateFlow()

    var vibrationThreshold3: Int
        get() = _vibrationThreshold3Flow.value
        set(value) = preferences.edit {
            _vibrationThreshold3Flow.value = value
            putInt(PREF_VIBRATION_THRESHOLD_3, value)
        }

    private val _speedIntervalFlow = MutableStateFlow(8)

    @Stable
    val speedIntervalFlow = _speedIntervalFlow.asStateFlow()

    var speedInterval: Int
        get() = _speedIntervalFlow.value
        set(value) = preferences.edit {
            _speedIntervalFlow.value = value
            putInt(PREF_SPEED_INTERVAL, value)
        }

    private val _vibrationDurationFlow = MutableStateFlow(2000)

    @Stable
    val vibrationDurationFlow = _vibrationDurationFlow.asStateFlow()

    var vibrationDuration: Int
        get() = _vibrationDurationFlow.value
        set(value) = preferences.edit {
            _vibrationDurationFlow.value = value
            putInt(PREF_VIBRATION_DURATION, value)
        }

    private val _vibrationAmplitudeFlow = MutableStateFlow(50)

    @Stable
    val vibrationAmplitudeFlow = _vibrationAmplitudeFlow.asStateFlow()

    var vibrationAmplitude: Int
        get() = _vibrationAmplitudeFlow.value
        set(value) = preferences.edit {
            _vibrationAmplitudeFlow.value = value
            putInt(PREF_VIBRATION_AMPLITUDE, value)
        }

    private val _permissionGrantedFlow = MutableStateFlow<Boolean?>(null)

    @Stable
    val permissionGrantedFlow  = _permissionGrantedFlow.asStateFlow()

    var hasPermission: Boolean?
        get() = _permissionGrantedFlow.value
        set(value) {
            _permissionGrantedFlow.value = value
        }

}
