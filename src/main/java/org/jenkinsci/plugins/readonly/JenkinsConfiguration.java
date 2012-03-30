package org.jenkinsci.plugins.readonly;


import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.RootAction;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.URL;
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
import org.kohsuke.stapler.jelly.DefaultScriptInvoker;
import org.kohsuke.stapler.jelly.HTMLWriterOutput;
import org.kohsuke.stapler.jelly.JellyClassLoaderTearOff;
import org.kohsuke.stapler.jelly.JellyClassTearOff;
import org.kohsuke.stapler.jelly.JellyViewScript;
import org.xml.sax.SAXException;

/**
 * Display Jenkins configuration page in read-only form
 * 
 * @author Lucie Votypkova
 */
@Extension
public class JenkinsConfiguration implements RootAction {

    private static Script configScript;

    /**
    * Load the current content of configure.jelly (jelly script which display Jenkins configuration) and create 
    * script with permission READ to display it users without ADMINISTER permission. After that the script is compiled and returned.
    * This method is called only once.
    * 
    * @return compiled script with READ permission
    */
    public static Script generateScript() throws JellyException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
            Script result = null;
            MetaClass c = WebApp.getCurrent().getMetaClass(Hudson.getInstance().getClass());
            Script script = c.loadTearOff(JellyClassTearOff.class).findScript("configure.jelly");
            JellyViewScript s = (JellyViewScript) script;
            InputStream input = s.source.openConnection().getInputStream();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            int b = 0;
            while (b != -1) {
                b = input.read();
                if (b != -1) {
                    output.write(b);
                }
            }
            String outputConfig = output.toString();
            outputConfig = outputConfig.replace("it.ADMINISTER", "it.READ");
            JellyClassTearOff jelly = c.loadTearOff(JellyClassTearOff.class);
            Field field = jelly.getClass().getSuperclass().getDeclaredField("classLoader");
            field.setAccessible(true);
            JellyClassLoaderTearOff loader = (JellyClassLoaderTearOff) field.get(jelly);
            File file = new File("readonly.jelly");
            PrintStream stre = new PrintStream(file);
            stre.print(outputConfig);
            URL newUrl = file.toURI().toURL();
            result = loader.createContext().compileScript(newUrl);
            field.setAccessible(false);
            file.delete();
            return result;     
    }

    public String getIconFileName() {
        return "search.png";
    }

    public String getDisplayName() {
        return "Global configuration";
    }

    public String getUrlName() {
        return "configure-readonly";
    }

    /**
    * Do a transformation of html code which modify all formular's items read-only
    * 
    * @return return true if all is ok
    */
    public boolean transformToReadOnly() throws JellyException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, SAXException, ParserConfigurationException, TransformerConfigurationException, TransformerException {
        if (configScript == null) {
            configScript = generateScript();
        }
        DefaultScriptInvoker invoker = new DefaultScriptInvoker();
        StaplerRequest request = Stapler.getCurrentRequest();
        StaplerResponse response = Stapler.getCurrentResponse();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HTMLWriterOutput xmlOutput = HTMLWriterOutput.create(output);
        xmlOutput.useHTML(true);
        invoker.invokeScript(request, response, configScript, Hudson.getInstance(), xmlOutput);
        String page = ReadOnlyUtil.transformInputsToReadOnly(output.toString());
        response.getOutputStream().write(page.getBytes());
        return true;
    }
}
