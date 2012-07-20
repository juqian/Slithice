package jqian.util.log;


public class LogManager {
	private static Logger _logger = new FileLogger();
    public static Logger getLogger(){
    	return _logger;
    }
}
