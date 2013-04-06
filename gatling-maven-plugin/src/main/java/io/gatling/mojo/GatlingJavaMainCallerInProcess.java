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
package io.gatling.mojo;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import scala_maven_executions.JavaMainCallerInProcess;

/**
 * This class will call a java main method via reflection. Modified to suit
 * Gatling's needs.
 * 
 * @author J. Suereth
 *         <p/>
 *         Note: a -classpath argument *must* be passed into the jvmargs.
 */
public class GatlingJavaMainCallerInProcess extends JavaMainCallerInProcess {

	public GatlingJavaMainCallerInProcess(AbstractMojo requester, String mainClassName, String classpath, String[] args) throws Exception {
		super(requester, mainClassName, classpath, null, args);

		// Pull out classpath and create class loader
		ArrayList<URL> urls = new ArrayList<URL>();
		for (String path : classpath.split(File.pathSeparator)) {
			try {
				urls.add(new File(path).toURI().toURL());
			} catch (MalformedURLException e) {
				requester.getLog().error(e);
			}
		}

		// FIXME why not child of current classloader?
		// FIXME what about old classloader?
		Thread.currentThread().setContextClassLoader(new URLClassLoader(urls.toArray(new URL[urls.size()])));
	}

	@Override
	// In process, ignore jvm args
	public void addJvmArgs(String... args) {
	}

	@Override
	// Not used, @see #run()
	public boolean run(boolean displayCmd, boolean throwFailure) throws Exception {
		throw new UnsupportedOperationException("boolean run(boolean displayCmd, boolean throwFailure) is not supported, call int run() instead");
	}

	public int run() throws Exception {
		return runGatling(mainClassName, args);
	}

	private int runGatling(String mainClassName, List<String> args) throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class<?> mainClass = cl.loadClass(mainClassName);
		Method runGatlingMethod = mainClass.getMethod("runGatling", String[].class);
		String[] argArray = args.toArray(new String[args.size()]);
		return (Integer) runGatlingMethod.invoke(null, new Object[] { argArray });
	}

}
