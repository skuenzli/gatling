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
/**
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

import io.gatling.core.action.Bypassable
import io.gatling.core.config.ProtocolConfigurationRegistry
import io.gatling.core.session.{ Expression, Session }
import io.gatling.core.validation.{ Failure, Success }
import io.gatling.http.ahc.{ GatlingAsyncHandler, GatlingAsyncHandlerActor, GatlingHttpClient }
import io.gatling.http.cache.CacheHandling
import io.gatling.http.check.HttpCheck
import io.gatling.http.config.HttpProtocolConfiguration
import io.gatling.http.referer.RefererHandling
import io.gatling.http.request.builder.AbstractHttpRequestBuilder

import akka.actor.{ ActorRef, Props }

/**
 * HttpRequestAction class companion
 */
object HttpRequestAction {

	def apply(requestName: Expression[String], next: ActorRef, requestBuilder: AbstractHttpRequestBuilder[_], checks: List[HttpCheck], protocolConfigurationRegistry: ProtocolConfigurationRegistry) = {

		val httpConfig = protocolConfigurationRegistry.getProtocolConfiguration(HttpProtocolConfiguration.default)

		new HttpRequestAction(requestName, next, requestBuilder, checks, httpConfig)
	}
}

/**
 * This is an action that sends HTTP requests
 *
 * @constructor constructs an HttpRequestAction
 * @param requestName the name of the request
 * @param next the next action that will be executed after the request
 * @param requestBuilder the builder for the request that will be executed
 * @param checks the checks that will be performed on the response
 * @param protocolConfiguration the protocol specific configuration
 */
class HttpRequestAction(requestName: Expression[String], val next: ActorRef, requestBuilder: AbstractHttpRequestBuilder[_], checks: List[HttpCheck], protocolConfiguration: HttpProtocolConfiguration) extends Bypassable {

	val handlerFactory = GatlingAsyncHandler.newHandlerFactory(checks, protocolConfiguration)
	val asyncHandlerActorFactory = GatlingAsyncHandlerActor.newAsyncHandlerActorFactory(checks, next, protocolConfiguration) _

	def execute(session: Session) {

		val execution = for {
			resolvedRequestName <- requestName(session)
			request <- requestBuilder.build(session, protocolConfiguration)
		} yield (resolvedRequestName, request)

		execution match {
			case Success((resolvedRequestName, request)) =>

				val newSession = RefererHandling.storeReferer(request, session, protocolConfiguration)

				if (CacheHandling.isCached(protocolConfiguration, session, request)) {
					logger.info(s"Bypassing cached Request '$resolvedRequestName': Scenario '${session.scenarioName}', UserId #${session.userId}")
					next ! newSession

				} else {
					logger.info(s"Sending Request '$resolvedRequestName': Scenario '${session.scenarioName}', UserId #${session.userId}")
					val actor = context.actorOf(Props(asyncHandlerActorFactory(resolvedRequestName)(request, newSession)))
					val ahcHandler = handlerFactory(resolvedRequestName, actor)
					GatlingHttpClient.client.executeRequest(request, ahcHandler)
				}

			case Failure(message) =>
				logger.error(message)
				next ! session
		}
	}
}
