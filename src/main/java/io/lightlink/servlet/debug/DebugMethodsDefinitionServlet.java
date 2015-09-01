package io.lightlink.servlet.debug;

import io.lightlink.config.ConfigManager;
import io.lightlink.config.Script;
import io.lightlink.core.ScriptRunner;
import io.lightlink.servlet.AbstractLightLinkServlet;
import io.lightlink.translator.JSBeautifyPostProcessor;
import io.lightlink.utils.ClasspathScanUtils;
import io.lightlink.utils.Utils;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DebugMethodsDefinitionServlet extends AbstractLightLinkServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        resp.setContentType("application/javascript");

        String name = req.getRequestURI().substring(req.getContextPath().length() + req.getServletPath().length() + 1);
        String nameWithDot = name.replace('/', '.');

        StringBuilder sb = new StringBuilder();
        
        sb.append("LL.JsApi.define('" + nameWithDot + "');\n");

        sb.append(nameWithDot + " = function(p,$callback,$scope,$hints){\n");

        sb.append("  try{\n");
        sb.append("  var $hints = LL.HintManager.calcEffectiveHints($hints);\n");
        sb.append("  var $fnName = \""+nameWithDot+"\";\n");
        sb.append("  var $debugSession = new LL.DebugSession();\n");

        List<Script> scripts = new ConfigManager(getRootPackage(),getServletContext()).getConfigAndContent(name, "POST");

        for (Script script : scripts) {
            sb.append("    /***** " + script.getName() + " *****/\n");
            sb.append(script.getContent());
        }
        sb.append("\n");
        sb.append("  sql.query();\n");
        sb.append("  $debugSession.resp.writeProperty(\"success\", true);\n");
        sb.append("  tx.success();\n");
        sb.append("  }catch(e){\n");
        sb.append("   $debugSession.resp.writeProperty(\"success\", false);\n");
        sb.append("   $debugSession.resp.writeProperty(\"error\", ''+e);\n");
        sb.append("   tx.failure();\n");
        sb.append("   /*todo: error handling*/\n");
        sb.append("  }finally{\n");
        sb.append("    sql.releaseConnection()\n");
        sb.append("  }\n");
        sb.append("  $debugSession.resp.end();\n");
        sb.append("  var res = $debugSession.context.getBuffer();\n");
        sb.append(" try{\n");
        sb.append("  var resJS = JSON.parse(res);\n");
        sb.append("  if (resJS.success === false) {\n" +
                "        $hints.onServerSideException(resJS, $fnName, p, $callback,$scope, $hints);\n" +
                "        return;\n" +
                "    }\n");

        sb.append(" } catch(e){\n");
        sb.append("      $hints.onJSONParsingError(e, {responseText:res}, $fnName, p, $callback, $scope, $hints);\n\n");
        sb.append(" }\n");
        sb.append(" if (typeof $callback==\"function\")\n" +
                  "     if ($scope)" +
                  "           setTimeout(function(){$callback.apply($scope, [resJS]) },100); // no direct $callback call to reproduce real async behaviour\n");
        sb.append("     else " +
                "           setTimeout(function(){$callback(resJS)},100); ");
        sb.append("}\n");

        String res = sb.toString();
        res = new JSBeautifyPostProcessor().process(res);

        resp.getWriter().print(res);


    }



}
