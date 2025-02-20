package values;

public final class Values {
	public static final String FILE_DIR = System.getProperty("user.home") + "\\.contacttracing\\";

	public static final String REGISTRAR_SERVICE = "RegistrarService";
	public static final int REGISTRAR_PORT = 1099;
	public static final String REGISTRAR_HOSTNAME = "localhost";

	public static final String MIXINGPROXY_SERVICE = "MixingProxyService";
	public static final int MIXINGPROXY_PORT = 1100;
	public static final String MIXINGPROXY_HOSTNAME = "localhost";

	public static final String MATCHINGSERVICE_SERVICE = "MatchingServiceService";
	public static final int MATCHINGSERVICE_PORT = 1101;
	public static final String MATCHINGSERVICE_HOSTNAME = "localhost";

	public static final int CRITICAL_PERIOD_IN_DAYS = 14;
}
