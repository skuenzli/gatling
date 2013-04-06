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
package io.gatling.core.result.writer

import java.io.StringWriter

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import io.gatling.core.result.message.{ OK, RequestRecord }
import io.gatling.core.util.StringHelper.eol

@RunWith(classOf[JUnitRunner])
class FileDataWriterSpec extends Specification {

	import FileDataWriter._

	"file data writer" should {

		def logRecord(record: RequestRecord): String = new StringWriter().append(record).getBuffer.toString

		"log a standard request record" in {
			val record = new RequestRecord("scenario", 1, "requestName", 2L, 3L, 4L, 5L, OK, Some("message"))

			logRecord(record) must beEqualTo("ACTION\tscenario\t1\trequestName\t2\t3\t4\t5\tOK\tmessage" + eol)
		}

		"append extra info to request records" in {
			val extraInfo: List[String] = List("some", "extra info", "for the log")
			val record = new RequestRecord("scenario", 1, "requestName", 2L, 3L, 4L, 5L, OK, Some("message"), extraInfo)

			logRecord(record) must beEqualTo("ACTION\tscenario\t1\trequestName\t2\t3\t4\t5\tOK\tmessage\tsome\textra info\tfor the log" + eol)
		}

		"sanitize extra info so that simulation log format is preserved" in {
			FileDataWriter.sanitize("\nnewlines \n are\nnot \n\n allowed\n") must beEqualTo(" newlines   are not    allowed ")
			FileDataWriter.sanitize("\rcarriage returns \r are\rnot \r\r allowed\r") must beEqualTo(" carriage returns   are not    allowed ")
			FileDataWriter.sanitize("\ttabs \t are\tnot \t\t allowed\t") must beEqualTo(" tabs   are not    allowed ")
		}
	}
}
