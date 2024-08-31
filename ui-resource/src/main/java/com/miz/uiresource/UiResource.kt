package com.miz.uiresource

import android.content.Context
import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
sealed class UiResource<T> : Serializable {

    abstract fun resolve(context: Context): T

}
