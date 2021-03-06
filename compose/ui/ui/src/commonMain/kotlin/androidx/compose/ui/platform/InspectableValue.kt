/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.ui.platform

/**
 * An empty [InspectorInfo] DSL.
 */
val NoInspectorInfo: InspectorInfo.() -> Unit = {}

/**
 * True, if this is a debug build
 */
// TODO(b/170125592): Replace this with an actual debug setting...
const val DebugInspectorInfo = true

/**
 * A compose value that is inspectable by tools. It gives access to private parts of a value.
 */
interface InspectableValue {

    /**
     * The elements of a compose value.
     */
    val inspectableElements: Sequence<ValueElement>
        get() = emptySequence()

    /**
     * Use this name as the reference name shown in tools of this value if there is no explicit
     * reference name given to the value.
     * Example: a modifier in a modifier list.
     */
    val nameFallback: String?
        get() = null

    /**
     * Use this value as a readable representation of the value.
     */
    val valueOverride: Any?
        get() = null
}

/**
 * A [ValueElement] describes an element of a compose value instance.
 * The [name] typically refers to a (possibly private) property name with its corresponding [value].
 */
data class ValueElement(val name: String, val value: Any?)

/**
 * A builder for an [InspectableValue].
 */
class InspectorInfo {
    /**
     * Provides a [InspectableValue.nameFallback].
     */
    var name: String? = null

    /**
     * Provides a [InspectableValue.valueOverride].
     */
    var value: Any? = null

    /**
     * Provides a [InspectableValue.inspectableElements].
     */
    val properties = ValueElementSequence()
}

/**
 * A builder for a sequence of [ValueElement].
 */
class ValueElementSequence : Sequence<ValueElement> {
    private val elements = mutableListOf<ValueElement>()

    override fun iterator(): Iterator<ValueElement> = elements.iterator()

    /**
     * Specify a sub element with name and value.
     */
    operator fun set(name: String, value: Any?) {
        elements.add(ValueElement(name, value))
    }
}

/**
 * Implementation of [InspectableValue] based on a builder [InspectorInfo] DSL.
 */
abstract class InspectorValueInfo(private val info: InspectorInfo.() -> Unit) : InspectableValue {
    private var _values: InspectorInfo? = null

    private val values: InspectorInfo
        get() {
            val valueInfo = _values ?: InspectorInfo().apply { info() }
            _values = valueInfo
            return valueInfo
        }

    override val nameFallback: String?
        get() = values.name

    override val valueOverride: Any?
        get() = values.value

    override val inspectableElements: Sequence<ValueElement>
        get() = values.properties
}

/**
 * Factory method for avoiding DSL allocation when no debug inspector info is needed.
 */
inline fun debugInspectorInfo(
    crossinline definitions: InspectorInfo.() -> Unit
): InspectorInfo.() -> Unit = if (DebugInspectorInfo) ({ definitions() }) else NoInspectorInfo
