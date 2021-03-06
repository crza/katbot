/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.katbot

import at.yawk.katbot.paste.LocalUrlShortener
import com.google.inject.ImplementedBy
import java.net.URI

/**
 * @author yawkat
 */
@ImplementedBy(LocalUrlShortener::class)
interface UrlShortener {
    fun shorten(uri: URI): URI
}