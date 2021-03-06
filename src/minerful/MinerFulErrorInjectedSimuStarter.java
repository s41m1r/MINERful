package minerful;

import minerful.concept.constraint.TaskCharRelatedConstraintsBag;
import minerful.errorinjector.params.ErrorInjectorCmdParameters;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.logparser.LogParser;
import minerful.logparser.StringLogParser;
import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.tracemaker.MinerFulTracesMaker;
import minerful.tracemaker.params.TracesMakerCmdParameters;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;


public class MinerFulErrorInjectedSimuStarter extends MinerFulSimuStarter {
	
	@Override
	public Options setupOptions() {
    	Options cmdLineOptions = new Options();
    	
    	Options systemOptions = SystemCmdParameters.parseableOptions(),
    			minerfulOptions = MinerFulCmdParameters.parseableOptions(),
				tracesMakerOptions = TracesMakerCmdParameters.parseableOptions(),
    			errorInjectorOptions = ErrorInjectorCmdParameters.parseableOptions(),
    			viewOptions = ViewCmdParameters.parseableOptions();
    	
    	for (Object opt: systemOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: minerfulOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: tracesMakerOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: viewOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: errorInjectorOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	
    	return cmdLineOptions;
	}
	
    /**
     * @param args the command line arguments:
     * 	[regular expression]
     *  [number of strings]
     *  [minimum number of characters per string]
     *  [maximum number of characters per string]
     *  [alphabet]...
     */
    public static void main(String[] args) {
    	MinerFulErrorInjectedSimuStarter minErrSimuSta = new MinerFulErrorInjectedSimuStarter();
    	Options cmdLineOptions = minErrSimuSta.setupOptions();
    	
        ViewCmdParameters viewParams =
        		new ViewCmdParameters(
        				cmdLineOptions,
        				args);
    	TracesMakerCmdParameters tracesMakParams =
    			new TracesMakerCmdParameters(
    					cmdLineOptions,
    					args);
        MinerFulCmdParameters minerFulParams =
        		new MinerFulCmdParameters(
        				cmdLineOptions,
    					args);
        ErrorInjectorCmdParameters errorInjexParams =
        		new ErrorInjectorCmdParameters(
        				cmdLineOptions,
        				args);
        SystemCmdParameters systemParams =
        		new SystemCmdParameters(
        				cmdLineOptions,
    					args);
       
        if (systemParams.help) {
        	systemParams.printHelp(cmdLineOptions);
        	System.exit(0);
        }
        
        configureLogging(systemParams.debugLevel);
        
        String[] testBedArray = new MinerFulTracesMaker().makeTraces(tracesMakParams);
    	testBedArray = MinerFulErrorInjectedTracesMakerStarter.injectErrors(testBedArray, tracesMakParams, errorInjexParams);
    	
		try {
			LogParser stringLogParser = new StringLogParser(testBedArray, ClassificationType.NAME);

	        // minerSimuStarter.mine(testBedArray, minerFulParams, tracesMakParams, systemParams);
	        TaskCharRelatedConstraintsBag bag = new MinerFulMinerStarter().mine(stringLogParser, minerFulParams, viewParams, systemParams, tracesMakParams.alphabet);
	        
	        MinerFulProcessViewerStarter proViewStarter = new MinerFulProcessViewerStarter(); 
	        proViewStarter.print(bag, viewParams, systemParams, stringLogParser);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
    }
}