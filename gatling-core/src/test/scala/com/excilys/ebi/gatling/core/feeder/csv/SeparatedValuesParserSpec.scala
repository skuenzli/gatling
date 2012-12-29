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
package com.excilys.ebi.gatling.core.feeder.csv

import scala.tools.nsc.io.File

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import com.excilys.ebi.gatling.core.Predef.tsv
import com.excilys.ebi.gatling.core.config.GatlingConfiguration

@RunWith(classOf[JUnitRunner])
class SeparatedValuesParserSpec extends Specification {

	GatlingConfiguration.setUp()

	"tsv" should {

		"handle file without escape char" in {
			val data = tsv(File("src/test/resources/sample1.tsv"))

			data must beEqualTo(Array(Map("foo" -> "hello", "bar" -> "world")))
		}

		"handle file with escape char" in {
			val data = tsv(File("src/test/resources/sample2.tsv"), "'")

			data must beEqualTo(Array(Map("foo" -> "hello", "bar" -> "world")))
		}
	}
}