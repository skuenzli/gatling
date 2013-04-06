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

import scala.annotation.tailrec
import scala.concurrent.forkjoin.ThreadLocalRandom

import akka.actor.{ ActorRef, Props }
import io.gatling.core.action.{ Switch, system }
import io.gatling.core.config.ProtocolConfigurationRegistry
import io.gatling.core.structure.ChainBuilder

class RandomSwitchBuilder(possibilities: List[(Int, ChainBuilder)]) extends ActionBuilder {

	require(possibilities.map(_._1).sum <= 100, "Can't build a random switch with percentage sum > 100")

	def build(next: ActorRef, protocolConfigurationRegistry: ProtocolConfigurationRegistry) = {

		val possibleActions = possibilities.map {
			case (percentage, possibility) =>
				val possibilityAction = possibility.build(next, protocolConfigurationRegistry)
				(percentage, possibilityAction)
		}

		val nextAction = () => {

			@tailrec
			def determineNextAction(index: Int, possibilities: List[(Int, ActorRef)]): ActorRef = possibilities match {
				case Nil => next
				case (percentage, possibleAction) :: others =>
					if (percentage >= index)
						possibleAction
					else
						determineNextAction(index - percentage, others)
			}

			determineNextAction(ThreadLocalRandom.current.nextInt(1, 101), possibleActions)
		}

		system.actorOf(Props(new Switch(nextAction, next)))
	}
}