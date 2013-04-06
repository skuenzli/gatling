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

import scala.tools.nsc.io.Path.string2path

import io.gatling.core.config.GatlingFiles
import io.gatling.core.session.{ Expression, Session }
import io.gatling.core.validation.Validation
import com.ning.http.client.FilePart

class UploadedFile(paramKeyFunction: Expression[String], fileNameFunction: Expression[String], mimeType: String, charset: String) {

	def filePart(session: Session): Validation[FilePart] = {

		for {
			paramKey <- paramKeyFunction(session)
			fileName <- fileNameFunction(session)
		} yield {
			val path = GatlingFiles.requestBodiesDirectory / fileName
			val file = path.jfile

			require(file.exists, s"Uploaded file $path does not exist")
			require(file.isFile, s"Uploaded file $path is not a real file")
			require(file.canRead, s"Uploaded file $path can't be read")

			new FilePart(paramKey, file, mimeType, charset)
		}
	}
}