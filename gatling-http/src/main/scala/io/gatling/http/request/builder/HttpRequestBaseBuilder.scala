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
package io.gatling.http.request.builder

import io.gatling.core.session.{ EL, Expression }

/**
 * HttpRequestActionBuilder class companion
 */
object HttpRequestBaseBuilder {

	/**
	 * This method is used in DSL to declare a new HTTP request
	 */
	def http(requestName: Expression[String]) = new HttpRequestBaseBuilder(requestName)
}

/**
 * Builder for HttpRequestActionBuilder
 *
 * @constructor creates an HttpRequestActionBuilder
 * @param requestName the name of the request
 */
class HttpRequestBaseBuilder(requestName: Expression[String]) {

	/**
	 * Starts the definition of an HTTP request with word DELETE
	 *
	 * @param url the url on which this request will be made
	 */
	def delete(url: String): DeleteHttpRequestBuilder = delete(EL.compile[String](url))

	/**
	 * Starts the definition of an HTTP request with word DELETE
	 *
	 * @param f the function returning the url of this request
	 */
	def delete(f: Expression[String]) = DeleteHttpRequestBuilder(requestName, f)

	/**
	 * Starts the definition of an HTTP request with word GET
	 *
	 * @param url the url on which this request will be made
	 */
	def get(url: String): GetHttpRequestBuilder = get(EL.compile[String](url))

	/**
	 * Starts the definition of an HTTP request with word GET
	 *
	 * @param f the function returning the url of this request
	 */
	def get(f: Expression[String]) = GetHttpRequestBuilder(requestName, f)

	/**
	 * Starts the definition of an HTTP request with word POST
	 *
	 * @param url the url on which this request will be made
	 */
	def post(url: String): PostHttpRequestBuilder = post(EL.compile[String](url))

	/**
	 * Starts the definition of an HTTP request with word POST
	 *
	 * @param f the function returning the url of this request
	 */
	def post(f: Expression[String]) = PostHttpRequestBuilder(requestName, f)

	/**
	 * Starts the definition of an HTTP request with word PUT
	 *
	 * @param url the url on which this request will be made
	 */
	def put(url: String): PutHttpRequestBuilder = put(EL.compile[String](url))

	/**
	 * Starts the definition of an HTTP request with word PUT
	 *
	 * @param f the function returning the url of this request
	 */
	def put(f: Expression[String]) = PutHttpRequestBuilder(requestName, f)

	/**
	 * Starts the definition of an HTTP request with word HEAD
	 *
	 * @param url the url on which this request will be made
	 */
	def head(url: String): HeadHttpRequestBuilder = head(EL.compile[String](url))

	/**
	 * Starts the definition of an HTTP request with word HEAD
	 *
	 * @param f the function returning the url of this request
	 */
	def head(f: Expression[String]) = HeadHttpRequestBuilder(requestName, f)
}

