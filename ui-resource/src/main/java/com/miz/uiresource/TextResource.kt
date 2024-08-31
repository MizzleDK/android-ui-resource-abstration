package com.miz.uiresource

import android.content.Context
import android.os.Parcelable
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalContext
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.util.MissingFormatArgumentException

@Immutable
sealed class TextResource : UiResource<String>(), Parcelable, Serializable {

    @Immutable
    @Parcelize
    data class RawString(val text: String) : TextResource()

    @Immutable
    @Parcelize
    data class Resource(
        @StringRes val id: Int,
        val formatArgs: List<TextResource> = emptyList(),
    ) : TextResource() {

        // Secondary constructor to allow a single argument
        constructor(
            @StringRes id: Int,
            formatArgs: TextResource,
        ) : this(
            id = id,
            formatArgs = listOf(formatArgs)
        )
    }

    @Immutable
    @Parcelize
    data class Quantity(
        @PluralsRes val id: Int,
        val quantity: Int,
        val formatArgs: List<TextResource>,
    ) : TextResource() {

        // Secondary constructor to allow for variable arguments
        constructor(
            @PluralsRes id: Int,
            quantity: Int,
            vararg formatArgs: TextResource,
        ) : this(
            id = id,
            quantity = quantity,
            formatArgs = formatArgs.toList()
        )
    }

    @Immutable
    @Parcelize
    data class Combined(
        val separator: CharSequence = "",
        val resources: List<TextResource>,
    ) : TextResource()

    @Immutable
    @Parcelize
    data class Lowercase(
        val text: TextResource
    ) : TextResource()

    @Immutable
    @Parcelize
    data class Uppercase(
        val text: TextResource
    ) : TextResource()

    @Immutable
    @Parcelize
    data class Number(
        val number: kotlin.Number
    ) : TextResource()

    override fun resolve(context: Context): String {
        return when (this) {
            is RawString -> text

            is Resource -> {
                try {
                    context.getString(
                        id, *formatArgs.map { it.resolve(context) }.toTypedArray()
                    )
                } catch (missingArgException: MissingFormatArgumentException) {
                    val argumentCount = context.countStringResourceArguments(id)
                    val resourceId = context.getResourceKey(id) ?: id
                    throw MissingFormatArgumentException(
                        "Missing arguments for string resource. " +
                                "Expected count: $argumentCount. " +
                                "Actual count: ${formatArgs.size}. " +
                                "String ID: $resourceId. String content: ${context.getString(id)}"
                    )
                }
            }

            is Quantity -> {
                try {
                    context.resources.getQuantityString(
                        id,
                        quantity,
                        *formatArgs.map {
                            it.resolve(context)
                        }.toTypedArray()
                    )
                } catch (missingArgException: MissingFormatArgumentException) {
                    val argumentCount = context.countQuantityStringResourceArguments(id, quantity)
                    val resourceId = context.getResourceKey(id) ?: id
                    throw IllegalArgumentException(
                        "Missing arguments for quantity string resource. " +
                                "Expected count: $argumentCount. Actual count: ${formatArgs.size}. " +
                                "Quantity string ID: $resourceId. " +
                                "Quantity string content: ${context.resources.getQuantityString(id, quantity)}"
                    )
                }
            }

            is Combined -> {
                resources.joinToString(
                    separator = separator
                ) { it.resolve(context) }
            }

            is Lowercase -> {
                text.resolve(context).lowercase()
            }

            is Uppercase -> {
                text.resolve(context).uppercase()
            }

            is Number -> {
                number.toString()
            }
        }
    }

    operator fun plus(other: TextResource): TextResource {
        return Combined(
            separator = " ",
            resources = listOf(this, other)
        )
    }

    companion object {
        val Empty = textResource("")
    }

}


// Helper functions
fun textResource(
    text: String
) = TextResource.RawString(text)

fun textResource(
    @StringRes id: Int,
    vararg formatArgs: TextResource
) = TextResource.Resource(id, formatArgs.toList())

fun textResource(
    @PluralsRes id: Int,
    quantity: Int,
    vararg formatArgs: TextResource
) = TextResource.Quantity(id, quantity, formatArgs.toList())

fun textResourceNumber(
    number: Number
) = TextResource.Number(number)


// Extension functions
fun TextResource?.orEmpty() = this ?: TextResource.Empty

fun TextResource.lowercase() = TextResource.Lowercase(this)

fun TextResource.uppercase() = TextResource.Uppercase(this)

@Composable
fun TextResource.resolve(): String {
    return this.resolve(LocalContext.current)
}