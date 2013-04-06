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
package io.gatling.core.action.builder

import io.gatling.core.action.{ While, system }
import io.gatling.core.config.ProtocolConfigurationRegistry
import io.gatling.core.session.Expression
import io.gatling.core.structure.ChainBuilder

import akka.actor.{ ActorRef, Props }

/**
 * @constructor create a new WhileAction
 * @param condition the function that determine the condition
 * @param loopNext chain that will be executed if condition evaluates to true
 */
class WhileBuilder(condition: Expression[Boolean], loopNext: ChainBuilder, counterName: String) extends ActionBuilder {

	def build(next: ActorRef, protocolConfigurationRegistry: ProtocolConfigurationRegistry) = {
		val whileActor = system.actorOf(Props(new While(condition, counterName, next)))
		val loopContent = loopNext.build(whileActor, protocolConfigurationRegistry)
		whileActor ! loopContent
		whileActor
	}
}