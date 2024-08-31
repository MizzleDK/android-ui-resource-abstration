package com.miz.uiresource

import android.content.Context
import android.content.res.Resources
import androidx.annotation.AnyRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

/**
 * Counts the number of arguments required by a string resource.
 *
 * @param stringResId The ID of the string resource.
 * @return The number of arguments required by the string resource.
 */
fun Context.countStringResourceArguments(@StringRes stringResId: Int): Int {
    return countStringResourceArgs(getString(stringResId))
}

/**
 * Counts the number of arguments required by a quantity string resource.
 *
 * @param pluralsResId The ID of the quantity string resource.
 * @return The number of arguments required by the quantity string resource.
 */
fun Context.countQuantityStringResourceArguments(@PluralsRes pluralsResId: Int, quantity: Int): Int {
    return countStringResourceArgs(resources.getQuantityString(pluralsResId, quantity))
}

/**
 * Counts the number of arguments in a string by finding simple and positional placeholders for
 * string value arguments, integer (decimal) value arguments and floating point value arguments.
 */
private fun countStringResourceArgs(string: String): Int {
    var count = 0
    val pattern = Regex("%(\\d+\\$)?[sdf]") // Matches %s, %d, %f, %1$s, %2$d, etc.
    pattern.findAll(string).forEach { _ -> count++ }
    return count
}

/**
 * Returns the resource key for a given resource ID.
 * For example: R.string.app_name -> app_name
 */
fun Context.getResourceKey(@AnyRes resId: Int): String? {
    try {
        return resources.getResourceEntryName(resId)
    } catch (e: Resources.NotFoundException) {
        e.printStackTrace()
        return null
    }
}
