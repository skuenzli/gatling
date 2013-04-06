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
package io.gatling.core.structure

import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.ProtocolConfigurationRegistry

import akka.actor.ActorRef

object ChainBuilder {
	def chainOf(actionBuilder: ActionBuilder*) = new ChainBuilder(actionBuilder.toList)
}

/**
 * This class defines chain related methods
 *
 * @param actionBuilders the builders that represent the chain of actions of a scenario/chain
 */
class ChainBuilder(val actionBuilders: List[ActionBuilder]) extends AbstractStructureBuilder[ChainBuilder] {

	private[core] def newInstance(actionBuilders: List[ActionBuilder]) = new ChainBuilder(actionBuilders)

	private[core] def getInstance = this

	/**
	 * Method that actually builds the scenario
	 *
	 * @param scenarioId the id of the current scenario
	 * @param next the action to be executed after the chain
	 * @return the first action of the scenario to be executed
	 */
	private[core] def build(next: ActorRef, protocolConfigurationRegistry: ProtocolConfigurationRegistry) = buildChainedActions(next, protocolConfigurationRegistry)
}