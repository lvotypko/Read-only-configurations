package org.jenkinsci.plugins.readonly;

import hudson.model.AbstractProject;
import hudson.model.ProminentProjectAction;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.jelly.DefaultScriptInvoker;      
import org.kohsuke.stapler.jelly.HTMLWriterOutput;
import org.kohsuke.stapler.jelly.JellyClassTearOff;
import org.xml.sax.SAXException;

/**
 * Display Job configuration page in read-only form
 * 
 * @author Lucie Votypkova
 */
public class JobConfiguration implements ProminentProjectAction {

    private AbstractProject<?, ?> project;

    public JobConfiguration(AbstractProject<?, ?> project) {
        this.project = project;
        
    }

    public String getIconFileName() {
        return "search.png";
    }

    public String getDisplayName() {
        return "Job configuration";
    }

    public String getUrlName() {
        return "configure-readonly";
    }
    
    /**
    * Do a transformation of html code which modify all formular's items read-only
    * 
    * @return return true if all is ok
    */
    @JavaScriptMethod
    public boolean transformToReadOnly() throws IOException, JellyException, SAXException, ParserConfigurationException, TransformerConfigurationException, TransformerException {
            DefaultScriptInvoker invoker = new DefaultScriptInvoker();
            StaplerRequest request = Stapler.getCurrentRequest();
            StaplerResponse response = Stapler.getCurrentResponse();
            MetaClass c = WebApp.get(request.getServletContext()).getMetaClass(project.getClass());
            ByteArrayOutputStream out = new ByteArrayOutputStream(); 
            HTMLWriterOutput xmlOutput = HTMLWriterOutput.create(out);
            Script script = c.loadTearOff(JellyClassTearOff.class).findScript("configure.jelly");            
            xmlOutput.useHTML(true);          
            invoker.invokeScript(request, response, script, project, xmlOutput);
            String charset = Charset.defaultCharset().name();
            String page = ReadOnlyUtil.transformInputsToReadOnly(out.toString(charset));          
            response.getOutputStream().write(page.getBytes());
            return true;
    }
    
}
