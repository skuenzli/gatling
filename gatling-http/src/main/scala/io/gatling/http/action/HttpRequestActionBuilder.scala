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
package io.gatling.http.action

import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.action.system
import io.gatling.core.config.ProtocolConfigurationRegistry
import io.gatling.core.session.Expression
import io.gatling.core.validation.SuccessWrapper
import io.gatling.http.check.HttpCheck
import io.gatling.http.check.status.HttpStatusCheckBuilder.status
import io.gatling.http.check.HttpCheckOrder.Status
import io.gatling.http.request.builder.AbstractHttpRequestBuilder

import akka.actor.{ ActorRef, Props }

object HttpRequestActionBuilder {

	/**
	 * This is the default HTTP check used to verify that the response status is 2XX
	 */
	val DEFAULT_HTTP_STATUS_CHECK = status.find.in(Session => (200 to 210).success).build

	def apply(requestName: Expression[String], requestBuilder: AbstractHttpRequestBuilder[_], checks: List[HttpCheck]) = {

		val resolvedChecks = checks
			.find(_.order == Status)
			.map(_ => checks)
			.getOrElse(HttpRequestActionBuilder.DEFAULT_HTTP_STATUS_CHECK :: checks)
			.sorted

		new HttpRequestActionBuilder(requestName, requestBuilder, resolvedChecks)
	}
}

/**
 * Builder for HttpRequestActionBuilder
 *
 * @constructor creates an HttpRequestActionBuilder
 * @param requestBuilder the builder for the request that will be sent
 * @param next the next action to be executed
 */
class HttpRequestActionBuilder(requestName: Expression[String], requestBuilder: AbstractHttpRequestBuilder[_], checks: List[HttpCheck]) extends ActionBuilder {

	private[gatling] def build(next: ActorRef, protocolConfigurationRegistry: ProtocolConfigurationRegistry): ActorRef = system.actorOf(Props(HttpRequestAction(requestName, next, requestBuilder, checks, protocolConfigurationRegistry)))
}
