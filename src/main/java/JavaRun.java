import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jmeter.threads.ThreadGroup;

import java.io.File;
import java.io.FileOutputStream;

public class JavaRun {
    public static void main(String[] argv) throws Exception {
        // set jmeterHome
        String jmeterHomePath = "/Users/xs/Desktop/TesterHome/Demo/6/code/apache-jmeter-5.1";
        File jmeterHome = new File(jmeterHomePath);
        String slash = System.getProperty("file.separator");

        if (jmeterHome.exists()) {
            File jmeterProperties = new File(jmeterHome.getPath() + slash + "bin" + slash + "jmeter.properties");
            if (jmeterProperties.exists()) {
                //JMeter Engine 引擎
                StandardJMeterEngine jmeter = new StandardJMeterEngine();

                //JMeter initialization (properties, log levels, locale, etc)
                JMeterUtils.setJMeterHome(jmeterHome.getPath());
                JMeterUtils.loadJMeterProperties(jmeterProperties.getPath());
                JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
                JMeterUtils.initLocale();


                // JMeter Test Plan, basically HashTree
                HashTree testPlanTree = new HashTree();

                // baidu sample
                HTTPSamplerProxy baiduSampler = new HTTPSamplerProxy();
                baiduSampler.setDomain("www.baidu.com");
                baiduSampler.setPort(80);
                baiduSampler.setPath("/");
                baiduSampler.setMethod("GET");
                baiduSampler.setName("Baidu Sample");
                baiduSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
                baiduSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());

                // Loop Controller Once
                LoopController loopController = new LoopController();
                loopController.setLoops(1);
                loopController.setFirst(true);
                loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
                loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
                loopController.initialize();

                // Thread Group
                ThreadGroup threadGroup = new ThreadGroup();
                threadGroup.setName("Java Run Thread Group");
                threadGroup.setNumThreads(1);
                threadGroup.setRampUp(1);
                threadGroup.setSamplerController(loopController);
                threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
                threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());

                // Test Plan
                TestPlan testPlan = new TestPlan("Create JMeter Script From Java Code");
                testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
                testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
                testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

                // Construct Test Plan from previously initialized elements
                testPlanTree.add(testPlan);
                HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
                threadGroupHashTree.add(baiduSampler);

                // save generated test plan to JMeter's .jmx file format
                SaveService.saveTree(testPlanTree, new FileOutputStream(jmeterHome + slash + "baidusample.jmx"));

                //add Summarizer output to get test progress in stdout like:
                // summary =      2 in   1.3s =    1.5/s Avg:   631 Min:   290 Max:   973 Err:     0 (0.00%)
                Summariser summer = null;
                String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
                if (summariserName.length() > 0) {
                    summer = new Summariser(summariserName);
                }


                // Store execution results into a .jtl file
                String logFile = jmeterHome + slash + "baidu.jtl";
                ResultCollector logger = new ResultCollector(summer);
                logger.setFilename(logFile);
                testPlanTree.add(testPlanTree.getArray()[0], logger);

                // Run Test Plan
                jmeter.configure(testPlanTree);
                jmeter.run();

                System.out.println("Test completed. See " + jmeterHome + slash + "baidu.jtl file for results");
                System.out.println("JMeter .jmx script is available at " + jmeterHome + slash + "baidusample.jmx");
                System.exit(0);

            }
        }

        System.err.println("jmeter.home property is not set or pointing to incorrect location");
        System.exit(1);

    }
}
