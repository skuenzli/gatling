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
package io.gatling.recorder.config

object ConfigurationConstants {

	val FILTER_STRATEGY = "recorder.filters.filterStrategy"
	val PATTERNS = "recorder.filters.patterns"
	val PATTERNS_TYPE = "recorder.filters.patternsType"

	val AUTOMATIC_REFERER = "recorder.http.automaticReferer"
	val FOLLOW_REDIRECT = "recorder.http.followRedirect"

	val LOCAL_PORT = "recorder.proxy.port"
	val LOCAL_SSL_PORT = "recorder.proxy.sslPort"

	val PROXY_HOST = "recorder.proxy.outgoing.host"
	val PROXY_USERNAME = "recorder.proxy.outgoing.username"
	val PROXY_PASSWORD = "recorder.proxy.outgoing.password"
	val PROXY_PORT = "recorder.proxy.outgoing.port"
	val PROXY_SSL_PORT = "recorder.proxy.outgoing.sslPort"

	val ENCODING = "recorder.simulation.encoding"
	val SIMULATION_OUTPUT_FOLDER = "recorder.simulation.outputFolder"
	val REQUEST_BODIES_FOLDER = "recorder.simulation.requestBodiesFolder"
	val SIMULATION_PACKAGE = "recorder.simulation.package"
	val SIMULATION_CLASS_NAME = "recorder.simulation.className"
}
