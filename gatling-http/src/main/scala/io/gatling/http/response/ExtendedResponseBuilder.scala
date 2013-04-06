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
package io.gatling.http.response

import java.security.MessageDigest

import scala.math.max

import io.gatling.core.session.Session
import io.gatling.core.util.TimeHelper.{ computeTimeMillisFromNanos, nowMillis }
import io.gatling.http.check.HttpCheck
import io.gatling.http.check.checksum.ChecksumCheck
import io.gatling.http.config.HttpProtocolConfiguration
import io.gatling.http.check.HttpCheckOrder.Body
import com.ning.http.client.{ HttpResponseBodyPart, HttpResponseHeaders, HttpResponseStatus, Request }

object ExtendedResponseBuilder {

	def newExtendedResponseBuilder(checks: List[HttpCheck], protocolConfiguration: HttpProtocolConfiguration): ExtendedResponseBuilderFactory = {

		val checksumChecks = checks.collect {
			case checksumCheck: ChecksumCheck => checksumCheck
		}

		val storeBodyParts = !protocolConfiguration.responseChunksDiscardingEnabled || checks.exists(_.order == Body)
		(request: Request, session: Session) => new ExtendedResponseBuilder(request, session, checksumChecks, storeBodyParts)
	}
}

class ExtendedResponseBuilder(request: Request, session: Session, checksumChecks: List[ChecksumCheck], storeBodyParts: Boolean) {

	private var status: HttpResponseStatus = _
	private var headers: HttpResponseHeaders = _
	private val bodies = new java.util.ArrayList[HttpResponseBodyPart]
	private var checksums = Map.empty[String, MessageDigest]
	val _executionStartDate = nowMillis
	var _requestSendingEndDate = 0L
	var _responseReceivingStartDate = 0L
	var _executionEndDate = 0L

	def accumulate(status: HttpResponseStatus) = {
		this.status = status
		this
	}

	def accumulate(headers: HttpResponseHeaders) = {
		this.headers = headers
		this
	}

	def updateRequestSendingEndDate(nanos: Long) = {
		_requestSendingEndDate = computeTimeMillisFromNanos(nanos)
		this
	}

	def updateResponseReceivingStartDate(nanos: Long) = {
		_responseReceivingStartDate = computeTimeMillisFromNanos(nanos)
		this
	}

	def computeExecutionEndDateFromNanos(nanos: Long) = {
		_executionEndDate = computeTimeMillisFromNanos(nanos)
		this
	}

	def accumulate(bodyPart: Option[HttpResponseBodyPart]) = {
		bodyPart.map { part =>
			for (check <- checksumChecks) {
				val algorithm = check.algorithm
				checksums.getOrElse(algorithm, {
					val md = MessageDigest.getInstance(algorithm)
					checksums += (algorithm -> md)
					md
				}).update(part.getBodyByteBuffer)
			}

			if (storeBodyParts) {
				bodies.add(part)
			}
		}
		this
	}

	def build: ExtendedResponse = {
		// time measurement is imprecise due to multi-core nature
		// ensure request doesn't end before starting
		_requestSendingEndDate = max(_requestSendingEndDate, _executionStartDate)
		// ensure response doesn't start before request ends
		_responseReceivingStartDate = max(_responseReceivingStartDate, _requestSendingEndDate)
		// ensure response doesn't end before starting
		_executionEndDate = max(_executionEndDate, _responseReceivingStartDate)
		val response = Option(status).map(status => status.provider.prepareResponse(status, headers, bodies))
		new ExtendedResponse(request, response, checksums, _executionStartDate, _requestSendingEndDate, _responseReceivingStartDate, _executionEndDate)
	}
}