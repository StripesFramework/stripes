Stripes Read Me
http://stripes.mc4j.org/

Copyright 2005-2006 Tim Fennell

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Contents
--------
1. Overview
2. Features
3. Installation
4. Acknowledgements
5. Feedback

1. Overview
   --------
   Stripes is a Java based framework for developing web applications using JSPs.
   It is designed with goals of being easy to learn and easy to use. As such it
   requires minimal up-front configuration, and zero configuration per screen or
   form. Stripes uses features introduced in JDK 1.5, including annotations and
   generics, to make developing web applications much easier.
   
   Furthermore Stripes is designed to integrate with existing standards like JSTL
   and to work well with available toolkits like Display Tag.  As a result it does
   not re-invent, or force you to re-invent the wheel, or learn masses of new tags
   to become productive.
	
2. Features
   --------
   -> Form beans and Actions are combined into a single ActionBean class
   -> Configuration of ActionBeans is done 100% through convention and annotations (no XML!)
   -> ActionBeans provide clean, built in, support for multiple events per form
   -> Complex ActionBean/form properties are instantiated and assembled for you
   -> Annotation based Validation system puts Validation info in the ActionBean
   -> Type Conversion is performed in concert with Validation (where it should be)
   -> Full support for indexed properties/multi-row forms using Collections
   -> Form custom tags that feel like HTML tags (no unnecessary renaming of attributes)
   -> Form tags provide formatting capabilities similar to JSTL formatting tags
   -> Seamless handling of file uploads
   -> Direct to JSP navigation isn't frowned on
   -> Input validation, formatting and tags are all locale aware (but only if you want)
   -> JavaDoc, TagDoc and reference documentation that doesn't suck

3. Installation
   ------------
   -> Copy the following files into your classpath (preferably WEB-INF/lib
      for deployment)
        -> lib/stripes.jar
        -> lib/commons-logging.jar
        -> lib/cos.jar

   -> Copy 'lib/StripesResources.properties' into /WEB-INF/classes or in to another
      directory ensuring that your build system puts it into the web app classpath
        
   -> Include the following in your web.xml
   
		<filter>
			<display-name>Stripes Filter</display-name>
			<filter-name>StripesFilter</filter-name>
			<filter-class>net.sourceforge.stripes.controller.StripesFilter</filter-class>
		</filter>
	
		<filter-mapping>
			<filter-name>StripesFilter</filter-name>
			<url-pattern>*.jsp</url-pattern>
			<dispatcher>REQUEST</dispatcher>
		</filter-mapping>
	
		<filter-mapping>
			<filter-name>StripesFilter</filter-name>
			<servlet-name>StripesDispatcher</servlet-name>
			<dispatcher>REQUEST</dispatcher>
		</filter-mapping>
	
		<servlet>
			<servlet-name>StripesDispatcher</servlet-name>
			<servlet-class>net.sourceforge.stripes.controller.DispatcherServlet</servlet-class>
			<load-on-startup>1</load-on-startup>
		</servlet>
	
		<servlet-mapping>
			<servlet-name>StripesDispatcher</servlet-name>
			<url-pattern>*.action</url-pattern>
		</servlet-mapping>
	
	-> While the *.action mapping is recommended, you are free to map any
	   pattern you like to the Stripes Dispatcher Servlet in place of, or in
	   addition to, *.action.
	   
	-> Once you have your first ActionBean configured and working, you may wish
	   to add the following initialization parameter to the StripesFilter in
	   order to speed up the loading of Stripes.  The value of the parameter
	   should be a comma-separated list of locations in your classpath where
	   Stripes can expect to discover ActionBeans - by default it searches all
	   entries in your web application's classpath.
	   
		<init-param>
			<param-name>ActionResolver.UrlFilters</param-name>
			<param-value>/WEB-INF/classes</param-value>
		</init-param>

4. Acknowledgements
   ----------------
   Stripes is distributed with a small number of libraries on which it depends.
   Those libraries are:
   
   -> Jakarta Commons Logging (http://jakarta.apache.org/commons/logging/)
      Commons logging is used to provide Stripes with a logging mechanism that
      does not tie it to a specific logging implementation. In reality, most
      users will probably be using Log4J, and so will need to configure commons
      logging to point at Log4J.  A sample configuration file is included in
      the example application at: examples/src/commons-logging.properties
      Commons Logging is licensed under the Apache License Version 2.0, a copy
      of which is included in lib/commons-logging.license
      
   -> COS or com.oreilly.servlets by Jason Hunter (http://servlets.com/cos/)
      COS provides one of only a couple of good implementations to handle HTTP
      multipart uploads.  And it's the only one to do it without requiring a
      whole host of other dependent libraries (shame on you commons upload!).
      COS, distributed with Stripes, is licensed under a specific license agreed
      upon between the authors of COS and Stripes, that is less restrictive than
      the standard license.  Please read the license carefully.  It can be
      found at lib/cos.license
      
5. Feedback
   --------
   Your feedback on Stripes (hopefully constructive) is always welcome.  Please
   visit http://stripes.mc4j.org/ for links to browse and join mailing
   lists, file bugs and submit feature requests.
