/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.katbot

import org.kitteh.irc.client.library.element.MessageReceiver
import java.util.regex.Pattern

private const val MAX_MESSAGE_LENGTH = 450

/**
 * @author yawkat
 */
class Template(data: String) {
    private val data =
            if (data.length > MAX_MESSAGE_LENGTH)
                data.substring(0, MAX_MESSAGE_LENGTH)
            else data

    private fun toTemplateExpression(name: String): String {
        // ${name}
        return "${'$'}{$name}"
    }

    fun set(name: String, value: String): Template {
        return Template(data.replace(toTemplateExpression(name), value))
    }

    fun set(name: String, value: () -> String): Template {
        if (data.contains(toTemplateExpression(name)) ) {
            return set(name, value.invoke())
        } else {
            return this
        }
    }

    fun setWithParameter(name: String, value: (String) -> String): Template {
        val quoted = Pattern.quote(name)
        val regex = "\\$\\{$quoted:([^}]*)\\}".toPattern()

        var r = this
        while (true) {
            val matcher = regex.matcher(r.data)
            if (!matcher.find()) return r
            // todo: prevent infinite loop
            val arg = matcher.group(1)
            val sub = value.invoke(arg)
            r = Template(r.data.substring(0, matcher.start()) + sub + r.data.substring(matcher.end()))
        }
    }

    fun finish(): String = data

    fun sendTo(vararg messageReceiver: MessageReceiver) {
        sendTo(messageReceiver.asList())
    }

    fun sendTo(messageReceivers: List<MessageReceiver>) {
        val msg = finish()
        if (msg.startsWith("/me ")) {
            val ctcp = "ACTION ${msg.substring(4)}"
            messageReceivers.forEach { it.sendCTCPMessage(ctcp) }
        } else {
            messageReceivers.forEach { it.sendMessage(msg) }
        }
    }

    fun setActorAndTarget(command: Command): Template {
        return set("actor", command.actor.nick)
                .set("target", (command.target ?: command.actor).nick)
    }

    fun sendAsReply(command: Command) {
        setActorAndTarget(command).sendTo(command.channel)
    }

    override fun toString(): String = data

    override fun equals(other: Any?) = other is Template && other.data == this.data
    override fun hashCode() = data.hashCode() + 1
}