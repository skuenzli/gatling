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

import scala.concurrent.duration.{ Duration, DurationLong }

import io.gatling.core.action.builder.{ CustomPauseBuilder, ExpPauseBuilder, PauseBuilder }
import io.gatling.core.session.Expression
import io.gatling.core.validation.SuccessWrapper

trait Pauses[B] extends Execs[B] {

	/**
	 * Method used to define a pause based on a duration defined in the session
	 *
	 * @param durationExp Expression that when resolved, provides the pause duration
	 * @return a new builder with a pause added to its actions
	 */
	def pause(durationExp: Expression[Duration]): B = newInstance(new PauseBuilder(durationExp) :: actionBuilders)

	/**
	 * Method used to define a pause based on a duration defined in the session
	 *
	 * @param minDurationExp Expression that when resolved, provides the minimum pause duration
	 * @param maxDurationExp Expression that when resolved, provides the maximum pause duration
	 * @return a new builder with a pause added to its actions
	 */
	def pause(minDurationExp: Expression[Duration], maxDurationExp: Expression[Duration]): B = newInstance(new PauseBuilder(minDurationExp, Some(maxDurationExp)) :: actionBuilders)

	/**
	 * Method used to define a random pause in seconds
	 *
	 * @param minDuration the minimum duration of the pause
	 * @param maxDuration the maximum duration of the pause
	 * @return a new builder with a pause added to its actions
	 */
	def pause(minDuration: Duration, maxDuration: Duration): B = pause(minDuration, Some(maxDuration))

	/**
	 * Method used to define a uniformly-distributed random pause
	 *
	 * @param minDuration the minimum value of the pause
	 * @param maxDuration the maximum value of the pause
	 * @return a new builder with a pause added to its actions
	 */
	def pause(minDuration: Duration, maxDuration: Option[Duration] = None): B = newInstance(new PauseBuilder((Session) => minDuration.success, maxDuration.map(m => (Session) => m.success)) :: actionBuilders)

	/**
	 * Method used to define drawn from an exponential distribution with the specified mean duration.
	 *
	 * @param meanDuration the mean duration of the pause
	 * @return a new builder with a pause added to its actions
	 */
	def pauseExp(meanDuration: Duration): B = newInstance(new ExpPauseBuilder(meanDuration) :: actionBuilders)

	/**
	 * Define a pause with a custom strategy
	 *
	 * @param delayGenerator the strategy for computing the pauses, in milliseconds
	 * @return a new builder with a pause added to its actions
	 */
	def pauseCustom(delayGenerator: Expression[Long]): B = newInstance(new CustomPauseBuilder(delayGenerator) :: actionBuilders)

}