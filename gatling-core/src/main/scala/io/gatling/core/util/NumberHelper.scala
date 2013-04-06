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
<<<<<<< HEAD:gatling-core/src/main/scala/com/excilys/ebi/gatling/core/util/NumberHelper.scala
package com.excilys.ebi.gatling.core.util

import java.util.Random
import scala.math.round
import org.apache.commons.math3.distribution.ExponentialDistribution
;
=======
package io.gatling.core.util
>>>>>>> aeebfe4eedcc6c9d57c47728471b7387abebcb30:gatling-core/src/main/scala/io/gatling/core/util/NumberHelper.scala

import scala.math.round
import scala.concurrent.forkjoin.ThreadLocalRandom

import org.apache.commons.math3.distribution.ExponentialDistribution

object NumberHelper {

<<<<<<< HEAD:gatling-core/src/main/scala/com/excilys/ebi/gatling/core/util/NumberHelper.scala
  /**
   * Used to generate random pause durations
   */
  val RANDOM = new Random


=======
>>>>>>> aeebfe4eedcc6c9d57c47728471b7387abebcb30:gatling-core/src/main/scala/io/gatling/core/util/NumberHelper.scala
	/**
	 * Create a function that generates uniformly-distributed random longs between the values min and max.
	 * @param min is the minimum value of the uniform distribution
	 * @param max is the maximum value of the uniform distribution
	 * @return
	 */
	def createUniformRandomLongGenerator(min: Long, max: Long): () => Long = () => 
		if (min == 0L && max == 0L) 0L
		else ThreadLocalRandom.current.nextLong(min, max)

	/**
	 * Create a function that generates exponentially-distributed random doubles with the provided mean.
	 *
	 * @param avg is the desired mean of the exponential distribution
	 * @return
	 */
	def createExpRandomDoubleGenerator(avg: Double): () => Double = {
		val dist: ExponentialDistribution = new ExponentialDistribution(avg)
		() => dist.sample()
	}

	/**
	 * Create a function that generates exponentially-distributed random longs with the provided mean.
	 *
	 * @param mean is the desired mean of the exponential distribution
	 * @return
	 */
	def createExpRandomLongGenerator(mean: Double): () => Long = {
		val generator = createExpRandomDoubleGenerator(mean)
		() => round(generator())
	}

	def formatNumberWithSuffix(n: Long) = {
		val suffix = n % 10 match {
			case _ if (11 to 13) contains n % 100 => "th"
			case 1 => "st"
			case 2 => "nd"
			case 3 => "rd"
			case _ => "th"
		}

		n + suffix
	}
}
