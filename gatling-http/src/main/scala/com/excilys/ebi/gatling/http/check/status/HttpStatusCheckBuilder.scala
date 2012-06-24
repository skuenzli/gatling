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
package com.excilys.ebi.gatling.http.check.status

import com.excilys.ebi.gatling.core.check.ExtractorFactory
import com.excilys.ebi.gatling.core.check.MatcherCheckBuilder
import com.excilys.ebi.gatling.core.util.StringHelper.EMPTY
import com.excilys.ebi.gatling.http.check.{ HttpExtractorCheckBuilder, HttpCheck }
import com.excilys.ebi.gatling.http.request.HttpPhase.StatusReceived
import com.ning.http.client.Response

import HttpStatusCheckBuilder.findExtractorFactory

/**
 * HttpStatusCheckBuilder class companion
 *
 * It contains DSL definitions
 */
object HttpStatusCheckBuilder {

	def status = new HttpStatusCheckBuilder

	private def findExtractorFactory: ExtractorFactory[Response, String, Int] = (response: Response) => (expression: String) => Some(response.getStatusCode)
}

/**
 * This class builds a response status check
 */
class HttpStatusCheckBuilder extends HttpExtractorCheckBuilder[Int, String](Session => EMPTY, StatusReceived) {

	def find = new MatcherCheckBuilder[HttpCheck[String], Response, String, Int](httpCheckBuilderFactory, findExtractorFactory)
}
