/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.tools;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.MethodDeclaration;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Literal;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>A tool for extracting and documenting information related to the site structure of a
 * Stripes application. SiteStructureTool is an <tt>AnnotationProcessor</tt> (and it's own
 * factory) for use with <tt>apt</tt>, the Annotation Processing Tool. It is capable of processing
 * several of the annotations used with Stripes to extract information about what bean is bound
 * to which URL, the set of events handled and the possible resolutions.  This information can
 * then be printed to the screen, or output to a file in either text or xml format.</p>
 *
 * <p>The SiteStructureTool can be run through the command line, though it is somewhat awkward with
 * a large number of files, or a large classpath - it's command line is extremely similar to javac.
 * A command line might look like this:</p>
 *
 *<pre>
 * apt -classpath $CLASSPATH -nocompile \
 *     -factory net.sourceforge.stripes.tools.SiteStructureTool \
 *     -Astripes.output.file=sitemap.txt \
 *     -Astripes.output.format=text \
 *     src/net/sourceforge/stripes/examples/bugzooky/web/*.java
 *</pre>
 *
 * <p>SiteStructureTool modifies its behaviour based on two options. (Custom options are always
 * passed to apt prefixed with <tt>-A</tt>).  The first is <tt>stripes.output.file</tt>. This
 * names the file into which the output will be written. If this option is omitted then the output
 * is simply printed to the screen.  The second option is <tt>stripes.output.format</tt> which
 * controls (not surprisingly) the output format.  Valid values are 'text' and 'xml'. If this
 * value is omitted the default format is 'text' <i>unless</i> a filename is supplied which ends in
 * '.xml', in which case xml output will be produced.</p>
 *
 * <p>The easiest way to run the SiteStructureTool is with ant.  Unfortunately the latest release
 * of ant at the time of writing does not yet include an apt task.  When it does, running apt
 * through ant should become much simpler.  Until then, you can run apt using an ant target like
 * the example below:</p>
 *
 *<pre>
 * &lt;target name="apt" depends="compile"&gt;
 *   &lt;pathconvert property="cp" refid="build.class.path"/&gt;
 *   &lt;path id="srcfiles"&gt;
 *     &lt;fileset dir="${src.dir}" includes="**&zwj;/*.java"/&gt;
 *   &lt;/path&gt;
 *   &lt;pathconvert property="srcfiles" refid="srcfiles" pathsep=" "/&gt;
 *   &lt;exec executable="apt"&gt;
 *     &lt;arg line="-classpath ${cp} -nocompile"/&gt;
 *     &lt;arg line="-factory net.sourceforge.stripes.tools.SiteStructureTool"/&gt;
 *     &lt;arg line="-Astripes.output.file=sitemap.xml"/&gt;
 *     &lt;arg line="${srcfiles}"/&gt;
 *   &lt;/exec&gt;
 * &lt;/target&gt;
 *</pre>
 *
 * @author Tim Fennell
 * @since Stripes 1.1.2
 */
public class SiteStructureTool implements AnnotationProcessor, AnnotationProcessorFactory {
    /** Option name that controls the output format of the annotation processor. */
    public static final String FORMAT_PARAM = "-Astripes.output.format";

    /** Option name that controls the file to which output is written. */
    public static final String FILE_PARAM   = "-Astripes.output.file";

    /** Regular expression used to parse out return statements from a chunk of java source. */
    protected static final Pattern RETURN_PATTERN = Pattern.compile("return\\s+(new\\s+)?([^;]+);");

    private Set<AnnotationTypeDeclaration> typeDeclarations;
    private AnnotationProcessorEnvironment environment;
    private Map<String,ActionBeanInfo> infos = new TreeMap<String,ActionBeanInfo>();

    /**
     * AnnotationProcessorFactory interface method that returns the set of custom options
     * that are supported.  Currently returns the file name and format type parameter names.
     */
    public Collection<String> supportedOptions() {
        return Literal.set(FORMAT_PARAM, FILE_PARAM);
    }

    /**
     * AnnotationProcessorFactory interface method that returns the set of annotation class
     * names that are supported.  Currently returns the fully qualified names of the following
     * annotations: @UrlBinding, @DefaultHandler, @HandlesEvent.
     */
    public Collection<String> supportedAnnotationTypes() {
        return Literal.set(DefaultHandler.class.getName(),
                           HandlesEvent.class.getName(),
                           UrlBinding.class.getName());
    }

    /**
     * AnnotationProcessorFactory interface method that returns the SiteStructure annotation
     * processor. In reality all this method does is return the instance of the factory
     * on which it is invoked because the SiteStructureTool is both the factory and the processor.
     */
    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> set,
                                               AnnotationProcessorEnvironment env) {
        this.typeDeclarations = set;
        this.environment = env;
        return this;
    }

    /**
     * AnnotationProcessor interface method that is invoked to perform the processing of the
     * annotations discovered. Builds up a set of information about the ActionBeans discovered
     * and then prints it out according to the options passed in.
     */
    public void process() {
        // Process the URL Bindings First
        AnnotationTypeDeclaration typeDec = getTypeDeclaration(UrlBinding.class);
        Collection<Declaration> declarations = this.environment.getDeclarationsAnnotatedWith(typeDec);
        processUrlBindings(declarations);

        // Then the method level annotations
        typeDec = getTypeDeclaration(DefaultHandler.class);
        declarations = new HashSet<Declaration>();
        declarations.addAll(this.environment.getDeclarationsAnnotatedWith(typeDec));
        typeDec = getTypeDeclaration(HandlesEvent.class);
        declarations.addAll(this.environment.getDeclarationsAnnotatedWith(typeDec));
        processHandlerAnnotations(declarations);

        // Now decide where to put our output
        PrintStream out = null;
        String filename = getOption(FILE_PARAM);

        if (filename == null) {
            out = System.out;
        }
        else {
            try {
                out = new PrintStream(filename);
            }
            catch (FileNotFoundException fnfe) {
                throw new StripesRuntimeException("Could not open the requested output file " +
                    "for writing. Please check that file '" + filename + "' can be created " +
                     "and/or written to.", fnfe);
            }
        }

        // And in what format
        String format = getOption(FORMAT_PARAM);
        if (format == null) {
            if (filename != null && filename.endsWith("xml")) {
                format = "xml";
            }
            else {
                format = "text";
            }
        }

        // And then finally print out the output
        if ("text".equals(format)) {
            printTextFormat(out);
        }
        else if ("xml".equals(format)) {
            printXmlFormat(out);
        }
        else {
            throw new StripesRuntimeException("Unknown format requested: " + format + ". " +
                "Supported formats are 'text' and 'xml'.");
        }
    }

    /**
     * For the named option to apt, returns the value of the option if it was supplied, or
     * null if the option was not supplied.
     */
    protected String getOption(String name) {
        for (String option : this.environment.getOptions().keySet()) {
            if (option.startsWith(name)) {
                return option.split("=")[1];
            }
        }

        return null;
    }

    /**
     * Responsible for iterating through the collection of UrlBinding annotations and
     * adding information to the instance level map of class names to ActionBeanInfo objects.
     *
     * @param declarations a collection of Declarations annotated with UrlBinding.
     */
    protected void processUrlBindings(Collection<Declaration> declarations) {
        for (Declaration declaration : declarations) {
            ClassDeclaration classDec = (ClassDeclaration) declaration;

            ActionBeanInfo info = new ActionBeanInfo();
            info.setClassName(classDec.getQualifiedName());
            info.setUrlBinding(classDec.getAnnotation(UrlBinding.class));
            this.infos.put(info.getClassName(), info);
        }
    }

    /**
     * Responsible for iterating through the collection of declarations annotated with
     * either @DefaultHandler, @HandlesEvent or both. Finds the relevant ActionBeanInfo object
     * in the instance level map and adds the event information to it.
     *
     * @param declarations a collection of Declarations annotated with handler annotations.
     */
    protected void processHandlerAnnotations(Collection<Declaration> declarations) {
        for (Declaration declaration : declarations) {
            MethodDeclaration methodDec = (MethodDeclaration) declaration;
            ClassDeclaration classDec = (ClassDeclaration) methodDec.getDeclaringType();

            EventInfo event = new EventInfo();
            event.setMethodName(methodDec.getSimpleName());

            DefaultHandler defaultHandler = methodDec.getAnnotation(DefaultHandler.class);
            if (defaultHandler != null) {
                event.setDefaultEvent(true);
            }

            HandlesEvent handlesEvent = methodDec.getAnnotation(HandlesEvent.class);
            if (handlesEvent != null) {
                event.setName(handlesEvent.value());
            }

            // Now find the resolutions and add those to the event info
            SortedSet resolutions = getResolutions(methodDec);
            event.setResolutions(resolutions);

            ActionBeanInfo info = this.infos.get(classDec.getQualifiedName());
            info.addEvent(event);
        }
    }


    /**
     * Prints out the accumulated information in text format to the supplied print stream. This
     * produces a fairly human readable format that is not designed to be machine processed.
     */
    protected void printTextFormat(PrintStream out) {
        for (ActionBeanInfo info : this.infos.values()) {
            out.println("URL: " + info.getUrlBinding().value());
            out.println("    ActionBean: " + info.getClassName());

            printTextEvent(out, info.getDefaultEvent());
            for (EventInfo event : info.getEvents().values()) {
                printTextEvent(out, event);
            }
            out.println("--------------------------------------------------------------");
        }
    }

    /** Prints out a single event mapping in text format. Used by printTextFormat(). */
    protected void printTextEvent(PrintStream out, EventInfo event) {
        if (event != null) {
            out.print("    Event: ");
            out.print( (event.getName() == null) ? "<no name>" : event.getName() );
            out.println( event.isDefaultEvent() ? " (Default)" : "");

            for (String resolution : event.getResolutions()) {
                out.println("        Resolution: " + resolution);
            }
        }
    }

    /** Prints out the accumulated information in XML format. */
    protected void printXmlFormat(PrintStream out) {
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println();
        out.println("<stripes-application>");

        for (ActionBeanInfo info : this.infos.values()) {
            out.print("    <action-bean class=\"");
            out.print(info.getClassName());
            out.print("\" url-binding=\"");
            out.print(info.getUrlBinding().value());
            out.println("\">");

            // print out the events for this action bean
            printXmlEvent(out, info.getDefaultEvent());
            for (EventInfo event : info.getEvents().values()) {
                printXmlEvent(out, event);
            }

            out.println("    </action-bean>");
        }

        out.println("</stripes-application>");
    }

    protected void printXmlEvent(PrintStream out, EventInfo event) {
        if (event != null) {
            out.print("        <event name=\"");
            out.print( (event.getName() == null) ? "" : event.getName() );
            out.print("\" default=\"");
            out.print(event.isDefaultEvent());
            out.println("\">");

            for (String resolution : event.getResolutions()) {
                out.print("            <resolution>");
                out.print(resolution);
                out.println("</resolution>");
            }

            out.println("        </event>");
        }
    }

    /**
     * Examines the set of AnnotationTypeDeclarations that this annotation processor was
     * constructed with to locate the one representing the annotation class provided.
     *
     * @return the matching AnnotationTypeDeclaration or null if one was not found.
     */
    AnnotationTypeDeclaration getTypeDeclaration(Class<? extends Annotation> type) {
        for (AnnotationTypeDeclaration declaration : this.typeDeclarations) {
            if (declaration.getQualifiedName().equals(type.getName())) {
                return declaration;
            }
        }

        return null;
    }

    /**
     * Attempts to return the code associated with the block/unit of code that the declaration
     * represents.  This is done using some fairly basic rules, and is prone to error when
     * unmatched braces occur in comments, strings etc. This will also fail if for some reason
     * the source file is not readable.
     *
     * @param declaration
     */
    private String getCodeFragment(Declaration declaration) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(declaration.getPosition().file()));
            StringBuilder sb = new StringBuilder(512);

            // Skip ahead in the source file to the start position of the declaration
            int start = declaration.getPosition().line();
            for (int i=1; i<start; i++) {
                br.readLine();
            }

            // Read all the characters until we pass an open brace, and then a matching close brace
            int ch;
            int braceCount = 0;
            boolean done = false;

            while (!done && (ch = br.read()) != -1) {
                sb.appendCodePoint(ch);
                if (ch=='{') {
                    braceCount++;
                }
                else if (ch=='}') {
                    braceCount--;
                    done = braceCount == 0;
                }
            }

            return sb.toString();
        }
        catch (IOException ioe) {
            throw new RuntimeException("Ecountered an IOException while trying to read a " +
                    "fragment of source file: " + declaration.getPosition().file(), ioe);
        }
    }

    /**
     * Fetches the code fragment associated with a method, and scans it for return statements
     * that pass back resolutions.
     *
     * @param declaration a Declaration, usually representing a method
     * @return a sorted set of Strings representing each resolution
     */
    SortedSet<String> getResolutions(Declaration declaration) {
        String codeFragment = getCodeFragment(declaration);
        Matcher matcher = RETURN_PATTERN.matcher(codeFragment);
        SortedSet<String> resolutions = new TreeSet<String>();

        while(!matcher.hitEnd()) {
            if (matcher.find()) {
                resolutions.add(matcher.group(2));
            }
        }

        return resolutions;
    }

}
