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
package io.gatling.core.result

case class Group(name: String, parent: Option[Group] = None) {
	val groups: List[String] = parent.map(parent => parent.name :: parent.groups).getOrElse(Nil)
	val path = RequestPath.path(name :: groups)

	override def equals(obj: Any) =
		obj match {
			case group: Group => group.path == path
			case _ => false
		}

	override val hashCode = path.hashCode

	override val toString = path
}

object RequestPath {
	val SEPARATOR = " / "

	def path(list: List[String]): String = list.reverse.mkString(SEPARATOR)
	def path(requestName: String, group: Option[Group]): String = path(requestName :: group.map(group => group.name :: group.groups).getOrElse(Nil))
}

