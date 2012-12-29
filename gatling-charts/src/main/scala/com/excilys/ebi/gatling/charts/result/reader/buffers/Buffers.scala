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
package com.excilys.ebi.gatling.charts.result.reader.buffers

import java.util.{ HashMap => JHashMap }

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.JavaConversions._

import com.excilys.ebi.gatling.charts.result.reader.ActionRecord
import com.excilys.ebi.gatling.core.result.Group

trait Buffers {

	@tailrec
	final def recursivelyUpdate(record: ActionRecord, group: Option[Group])(update: (ActionRecord, Option[Group]) => Unit) {
		update(record, group)

		group match {
			case Some(group) => recursivelyUpdate(record, group.parent)(update)
			case None => {}
		}
	}
}

class CountBuffer {
	val map: mutable.Map[Int, Int] = new JHashMap[Int, Int]

	def update(bucket: Int) { map += (bucket -> (map.getOrElse(bucket, 0) + 1)) }
}

class RangeBuffer {
	val map: mutable.Map[Int, (Int, Int)] = new JHashMap[Int, (Int, Int)]

	def update(bucket: Int, value: Int) {
		val (minValue, maxValue) = map.getOrElse(bucket, (Int.MaxValue, Int.MinValue))
		map += (bucket -> (value min minValue, value max maxValue))
	}
}

