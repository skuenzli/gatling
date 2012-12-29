/**
 * Copyright 2011-2012 eBusiness Information, Groupe Excilys (www.excilys.com)
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
package com.excilys.ebi.gatling.core.action

import com.excilys.ebi.gatling.core.session.Session

import akka.actor.ActorRef

/**
 * Top level abstraction in charge or executing concrete actions along a scenario, for example sending an HTTP request.
 * It is implemented as an Akka Actor that receives Session messages.
 */
trait Action extends BaseActor {

	def next: ActorRef

	def receive = {
		case session: Session => execute(session)
	}

	/**
	 * Core method executed when the Action received a Session message
	 *
	 * @param session the session of the virtual user
	 * @return Nothing
	 */
	def execute(session: Session)

	override def preRestart(reason: Throwable, message: Option[Any]) {
		error("Action " + this + " crashed, forwarding user to next one", reason)
		message match {
			case Some(session: Session) if (next != null) => next ! session.setFailed
			case _ =>
		}
	}
}