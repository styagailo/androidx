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

package androidx.navigation

@DslMarker
annotation class NavDeepLinkDsl

/**
 * Construct a new [NavDeepLink]
 */
fun navDeepLink(deepLinkBuilder: NavDeepLinkDslBuilder.() -> Unit): NavDeepLink =
        NavDeepLinkDslBuilder().apply(deepLinkBuilder).build()

/**
 * DSL for constructing a new [NavDeepLink]
 */
@NavDeepLinkDsl
class NavDeepLinkDslBuilder {
    private val builder = NavDeepLink.Builder()

    /**
     * The uri of the deep link
     */
    var uri: String? = null

    /**
     * Intent action for the deep link
     */
    var action: String? = null

    /**
     * MimeType for the deep link
     */
    var mimeType: String? = null

    internal fun build() = builder.apply {
        check(!(uri == null && action == null && mimeType == null)) {
            ("The NavDeepLink must have an uri, action, and/or mimeType.")
        }
        uri?.let { setUri(it) }
        action?.let { setAction(it) }
        mimeType?.let { setMimeType(it) }
    }.build()
}