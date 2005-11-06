Stripes Read Me
http://stripes.sourceforge.net

Copyright (C) 2005 Tim Fennell

This library is free software; you can redistribute it and/or modify it
under the terms of the GNU Lesser General Public License as published by the
Free Software Foundation; either version 2.1 of the License, or (at your
option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
for more details.

You should have received a copy of the license with this software. If not,
it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html

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
   -> Configuration of ActionBeans is done 100% through Annotations (no XML!)
   -> ActionBeans provide clean, built in, support for multiple events per form
   -> Complex ActionBean properties are instantiated and assembled for you
   -> Annotation based Validation system puts Validation info in the ActionBean
   -> Type Conversion is performed in concert with Validation (where it should be)
   -> Full support for indexed properties/multi-row forms using Collections
   -> Form custom tags that feel like HTML tags (no unnecessary renaming of attributes)
   -> Form tags provide formatting capabilities similar to JSTL formatting tags
   -> Seemless handling of file uploads
   -> Direct to JSP navigation isn't frowned on (it's encouraged!)
   -> Input validation, formatting and tags are all locale aware (but only if you want)
   -> Cleans up and fixes many of the small annoyances in other web frameworks
   -> JavaDoc and TagDoc that doesn't suck

3. Installation
   ------------
   -> Copy the following files into your classpath (preferably WEB-INF/lib
      for deployment)
        -> lib/stripes.jar
        -> lib/commons-logging.jar
        -> lib/cos.jar
        -> lib/ognl-2.6.7.jar
        
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
			<param-value>*WEB-INF/classes</param-value>
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
      
   -> OGNL or Object Graph Navigation Language (http://www.ognl.org/)
      Ognl is a far more powerful alternative to libraries such as the Jakarta
      commons BeanUtils.  It is used with several custom plug-ins to provide
      seemless instantiation and navigation of deep object webs on ActionBeans.
      OGNL is licensed under a custom, but simple, free license. A copy of
      the license can be found at lib/ognl.license
      
5. Feedback
   --------
   Your feedback on Stripes (hopefully constructive) is always welcome.  Please
   visit http://stripes.sourceforge.net for links to browse and join mailing
   lists, file bugs and submit feature requests.
