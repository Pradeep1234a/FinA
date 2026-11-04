# ProGuard rules for Expense Tracker

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltApplication { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-dontwarn dagger.hilt.android.**

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# kotlinx.serialization
-keep class kotlinx.serialization.** { *; }
-keep @kotlinx.serialization.Serializable class * { *; }

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# DataStore
-dontwarn androidx.datastore.**
-keep class androidx.datastore.** { *; }

# WorkManager
-dontwarn androidx.work.**
-keep class androidx.work.** { *; }

# Navigation
-dontwarn androidx.navigation.**
-keep class androidx.navigation.** { *; }

# Paging
-dontwarn androidx.paging.**
-keep class androidx.paging.** { *; }

# Coroutines
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# Date/Time
-dontwarn kotlinx.datetime.**
-keep class kotlinx.datetime.** { *; }

# threeTenBP
-dontwarn org.threeten.bp.**
-keep class org.threeten.bp.** { *; }

# Keep model classes
-keep class com.expensetracker.domain.model.** { *; }
-keep class com.expensetracker.data.local.entity.** { *; }

# Keep ViewModels
-keep class com.expensetracker.presentation.viewmodel.** { *; }

# Keep UseCases
-keep class com.expensetracker.domain.usecase.** { *; }

# Keep Repository implementations
-keep class com.expensetracker.data.repository.** { *; }

# Keep Mappers
-keep class com.expensetracker.data.mapper.** { *; }

# Keep DI modules
-keep class com.expensetracker.di.** { *; }

# Keep Theme
-keep class com.expensetracker.presentation.ui.theme.** { *; }

# Keep Utility classes
-keep class com.expensetracker.util.** { *; }

# Keep Activities
-keep class com.expensetracker.presentation.ui.** { *; }
-keep class com.expensetracker.presentation.ui.screen.** { *; }

# Keep generated Hilt components
-keep class dagger.hilt.android.internal.** { *; }

# Keep settings
-keep class com.expensetracker.data.local.preferences.** { *; }

# Keep WorkManager workers
-keep class com.expensetracker.data.local.work.** { *; }

# Keep Receivers
-keep class com.expensetracker.data.local.work.BootReceiver { *; }