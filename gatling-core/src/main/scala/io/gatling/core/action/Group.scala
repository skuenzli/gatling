/**
 * Copyright 2011-2013 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.core.action

import io.gatling.core.result.message.RecordEvent
import io.gatling.core.result.writer.DataWriter
import io.gatling.core.session.{ Expression, Session }
import io.gatling.core.validation.{ Failure, Success }

import akka.actor.ActorRef

class Group(groupName: Expression[String], event: RecordEvent, val next: ActorRef) extends Chainable {

	def execute(session: Session) {
		val resolvedGroupName = groupName(session) match {
			case Success(name) => name
			case Failure(message) => logger.error(s"Could not resolve group name: $message"); "no-group-name"
		}

		DataWriter.group(session.scenarioName, resolvedGroupName, session.userId, event)
		next ! session
	}
}