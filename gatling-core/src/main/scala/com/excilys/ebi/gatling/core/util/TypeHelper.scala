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
package com.excilys.ebi.gatling.core.util

import scalaz._
import scalaz.Scalaz._

object TypeHelper {

	def classCastExceptionMessage(value: Any, clazz: Class[_]) = "Can't cast value " + value + " of type " + value.getClass + " into " + clazz

	def as[T: ClassManifest](value: Any): Validation[String, T] = {
		val clazz = implicitly[ClassManifest[T]].erasure
		val valueClazz = value.getClass.getName match {
			case "java.lang.Boolean" => classOf[Boolean]
			case "java.lang.Integer" => classOf[Int]
			case "java.lang.Long" => classOf[Long]
			case "java.lang.Double" => classOf[Double]
			case "java.lang.Float" => classOf[Float]
			case _ => value.getClass
		}

		if (clazz == classOf[String])
			value.toString.asInstanceOf[T].success
		else if (clazz.isAssignableFrom(value.getClass) || clazz.isAssignableFrom(valueClazz))
			value.asInstanceOf[T].success
		else
			classCastExceptionMessage(value, clazz).failure
	}
}