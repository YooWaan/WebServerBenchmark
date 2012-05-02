//
// Embbed Web Server
//
//   Property
//  ----------
//
//  server.port                  port number    
//  server.basedir               base dir path.
//  server.webapps               webapp dir name.
//  server.contextpath           context path of web app
//  server.maxthreads    
//  server.minthreads    
//
//
//  server.secure                true/false
//  server.secure.key.alias      alias
//  server.secure.key.password   keystore pasword
//  server.secure.key.file       keystore file path
//  server.secure.ssl.protocol   protocol
//
//
//   Dir
//  ----------
//
//  home.dir/
//    server.jar      jar file of this class
//    lib/            tomcat libs & jetty
//    webapps/
//        sample/
//           WEB-INF/
//              lib/webservlet.jar or your servlet.jar
//           indx.html
//
//
// @author wooyoowaan@gmail.com
//

package web.server;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.ServletException;

// TOMCAT
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
//import org.apache.catalina.connector.Connector;

// JETTY
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;

abstract public class WebServer {

	// server configuration
	public static final String PROPS_PORT         = "server.port";
	public static final String PROPS_BASEDIR      = "server.basedir";
	public static final String PROPS_WEBAPPS      = "server.webapps";
	public static final String PROPS_CONTEXTPATH  = "server.contextpath";

	public static final String PROPS_RESOURCE_BASE= "server.resourcebase";
	public static final String PROPS_WAR          = "server.war";
	public static final String PROPS_EXTRA_CLASSPATH = "server.extr.classpath";

	public static final String PROPS_MAX_THREADS  = "server.maxthreads";
	public static final String PROPS_MIN_THREADS  = "server.minthreads";

	// scurity properties
	public static final String PROPS_SECURE       = "server.secure";
	public static final String PROPS_KEY_ALIAS    = "server.secure.key.alias";
	public static final String PROPS_KEY_PASSWORD = "server.secure.key.password";
	public static final String PROPS_KEY_FILE     = "server.secure.key.file";
	public static final String PROPS_SSL_PROTOCOL = "server.secure.ssl.protocol";
	public static final String PROPS_CLIENT_AUTH  = "server.secure.client.auth";


	protected Properties _properties;

	/**
	 * start web server
	 */
	public void start() {
		doStart();
	}

	/**
	 * stop web server
	 */
	public void stop() {
		doStop();
	}

	abstract protected void doStart();
	abstract protected void doStop();

	protected void check(StateChecker checker) throws Exception {
		Thread thread = new Thread(checker);
		thread.start();
		thread.join();
	}

	/**
	 *
	 */
	protected int getInt(String name, int defaultValue) {
		String s = _properties.getProperty(name);
		return s == null ? defaultValue : Integer.parseInt(s) ;
	}

	/**
	 *
	 */
	protected String getProperty(String name, String defaultValue) {
		return _properties.getProperty(name, defaultValue);
	}

	/**
	 *
	 */
	protected boolean getBool(String name, boolean defaultFlag) {
		String s = _properties.getProperty(name, null);
		return s == null ? defaultFlag : Boolean.parseBoolean(s);
	}

	public static void main(String[] args) {
		Properties props = new Properties();
		boolean useTomcat = true;
		for (int i = 0; i < args.length ; i++) {
			String arg = args[i];
			if (arg.equals("-jetty")) {
				useTomcat = false;
			} else {
				String key = arg;
				i++;
				String value = args[i];
				props.setProperty(key, value);
			}
		}
		final WebServer server = useTomcat ? new TomcatServer(props) : new JettyServer(props) ;
		try {
			server.start();
			Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						server.stop();
					}
				});

			while (true) {}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

/**
 * Jetty Server
 */
class JettyServer extends WebServer {

	private Server _server;

	/**
	 *
	 */
	public JettyServer(Properties props) {
		_properties = props;
	}

	/**
	 *
	 */
	protected void doStart() {

		_server = new Server();

		int maxThreads = getInt(PROPS_MAX_THREADS, 200);
		int minThreads = getInt(PROPS_MIN_THREADS, 8);

		QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(minThreads);
        threadPool.setMaxThreads(maxThreads);
        this._server.setThreadPool(threadPool);

        Connector connector = null;
		if (getBool(PROPS_SECURE, false)) {
			SslSelectChannelConnector sslConnector = new SslSelectChannelConnector();

			sslConnector.setKeystore(getProperty(PROPS_KEY_FILE, "keystore/sha1.keystore"));
			sslConnector.setKeyPassword(getProperty(PROPS_KEY_PASSWORD, "password"));
			sslConnector.setProtocol(getProperty(PROPS_SSL_PROTOCOL, "TLS"));
			//sslConnector.setPassword();

			// trust ...

			connector = sslConnector;
			connector.setPort(getInt(PROPS_PORT, 8443));
		} else {
			connector = new SelectChannelConnector();
			connector.setPort(getInt(PROPS_PORT, 8080));
		}

        // set connectors
        this._server.setConnectors(new Connector[] { connector });



		//
		// setup
		//

        WebAppContext ctx = new WebAppContext(getProperty(PROPS_WEBAPPS,"webapps"),
											  getProperty(PROPS_CONTEXTPATH, "/sample"));
		ctx.setExtractWAR(true);
		ctx.setResourceBase(getProperty(PROPS_RESOURCE_BASE, "webapps/sample"));
		ctx.setWar(getProperty(PROPS_WAR, "/sample"));
		String extraClasspath = getProperty(PROPS_EXTRA_CLASSPATH, null);
		if (extraClasspath != null) {
			ctx.setExtraClasspath(getClassPath(extraClasspath));
		}
		ctx.setServer(_server);
        _server.setHandler(ctx);

		//
		// start
		//
		try {
			_server.start();
			check(new StateChecker() {
					public boolean isOK() {
						return _server.isRunning() || _server.isFailed();
					}
				});
		} catch (Exception e) {
			throw new IllegalStateException("Jetty Boot Failed", e);
		}

		if (_server.isFailed()) {
			throw new IllegalStateException("Jetty Boot Failed");
		}
	}

	/**
	 *
	 */
	protected void doStop() {
		try {
			_server.stop();
			if (!_server.isStopped()) {
				check(new StateChecker() {
						public boolean isOK() {
							return _server.isStopped() || _server.isFailed();
						}
					});
			}
		} catch (Exception e) {
			throw new IllegalStateException("jetty stop failed");
		}
		if (_server.isFailed()) {
			throw new IllegalStateException("jetty stop failed");
		}
		_server = null;
	}


	private String getClassPath(String extraPath) {
		try {
			StringBuilder classPath = new StringBuilder();
			StringTokenizer st = new StringTokenizer(extraPath, ",;");
			while (st.hasMoreTokens()) {
				File dir = new File(st.nextToken());
				File[] flst = dir.listFiles(new FilenameFilter() {
						public boolean accept(File file, String name) {
							return name.endsWith(".jar") || name.endsWith(".zip") || name.endsWith(".class");
						}
					});
				for (File f : flst) {
					if (classPath.length() > 0) {
						classPath.append(',');
					}
					classPath.append(f.toURI().toURL());
				}
			}
			return classPath.toString();
		} catch (Exception e) {
			// ignore
		}
		return extraPath;
	}
}

/**
 * Tomcat
 */
class TomcatServer extends WebServer {

	private Tomcat _server;

	/**
	 *
	 */
	public TomcatServer(Properties props) {
		_properties = props;
	}

	/**
	 *
	 */
	protected void doStart() {
		_server = new Tomcat();
		File baseDir = new File(getProperty(PROPS_BASEDIR, "."));
		_server.setBaseDir(baseDir.getAbsolutePath());


		

		// https
		if (getBool(PROPS_SECURE, false)) {
			org.apache.catalina.connector.Connector connector = new org.apache.catalina.connector.Connector("HTTP/1.1");
			int port = getInt(PROPS_PORT, 8443);
			connector.setPort(port);
			connector.setSecure(true);
			File keyStoreFile = new File(baseDir, getProperty(PROPS_KEY_FILE, "keystore/sha1.keystore"));
			connector.setScheme("https");
			connector.setAttribute("keyAlias", getProperty(PROPS_KEY_ALIAS, "tomcat"));
			connector.setAttribute("keystoreFile", keyStoreFile.getAbsolutePath());
			connector.setAttribute("keystorePass", getProperty(PROPS_KEY_PASSWORD, "password"));
			connector.setAttribute("clientAuth", getProperty(PROPS_CLIENT_AUTH, "false"));
			connector.setAttribute("sslProtocol", getProperty(PROPS_SSL_PROTOCOL, "TLS"));
			connector.setAttribute("SSLEnabled", true);
			//_server.setConnector(connector);
			_server.getService().addConnector(connector);

			/*
			Connector defaultConnector = _server.getConnector();
			defaultConnector.setRedirectPort(port);
			System.out.println("Con [" + );
			for (Connector con : _server.getService().findConnectors()) {
				System.out.println(con);
				if (con != connector) {
					con.setRedirectPort(port);
				}
			}
			*/

		} else {
			_server.setPort(getInt(PROPS_PORT, 8080));
		}

		File webApps = new File(baseDir, getProperty(PROPS_WEBAPPS, "webapps"));
		_server.getHost().setAppBase(webApps.getAbsolutePath());

		try {
			String contextPath = getProperty(PROPS_CONTEXTPATH, "/sample");
			_server.addWebapp(contextPath, new File(webApps, contextPath).getAbsolutePath());
			_server.start();
		} catch (ServletException e) {
			throw new IllegalStateException("tomcat boot failed!!", e);
		} catch (LifecycleException e) {
			throw new IllegalStateException("tomcat boot failed!!", e);
		}
	}

	/**
	 *
	 */
	protected void doStop() {
		try {
			_server.stop();
		} catch (LifecycleException e) {
			throw new IllegalStateException("tomcat stop failed!!", e);
		}
	}

}

class StateChecker implements Runnable {

	public boolean isOK() {return true;}

	public void run() {
		while (!isOK()) {
			try {
				Thread.currentThread().sleep(1000);
			} catch (Exception e) {
				break;
			}
		}
	}
}