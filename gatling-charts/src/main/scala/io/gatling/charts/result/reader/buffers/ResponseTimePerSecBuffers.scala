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
package io.gatling.charts.result.reader.buffers

import scala.collection.mutable

import io.gatling.charts.result.reader.ActionRecord
import io.gatling.core.result.Group
import io.gatling.core.result.message.RequestStatus

trait ResponseTimePerSecBuffers {

	val responseTimePerSecBuffers = mutable.Map.empty[BufferKey, RangeBuffer]

	def getResponseTimePerSecBuffers(requestName: Option[String], group: Option[Group], status: Option[RequestStatus]): RangeBuffer = responseTimePerSecBuffers.getOrElseUpdate(computeKey(requestName, group, status), new RangeBuffer)

	def updateResponseTimePerSecBuffers(record: ActionRecord, group: Option[Group]) {
		getResponseTimePerSecBuffers(Some(record.request), group, Some(record.status)).update(record.executionStartBucket, record.responseTime)
	}

	def updateGroupResponseTimePerSecBuffers(start: Int, duration: Int, group: Group, status: RequestStatus) {
		getResponseTimePerSecBuffers(None, Some(group), Some(status)).update(start, duration)
	}
}