import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.fail;

public class FailWithSessionNotCreatedTest {

	// allowed version gap
	private static final int MIN_VERSION_GAP = 2;

	@BeforeClass
	public static void beforeClass() throws Exception {
		String osName = System.getProperty("os.name").toLowerCase();

		// Build ChromeDriver path (same as your original logic)
		String driverPath = Paths.get(System.getProperty("user.home")).resolve("Downloads")
				.resolve(osName.startsWith("windows") ? "chromedriver.exe" : "chromedriver").toAbsolutePath()
				.toString();

		System.setProperty("webdriver.chrome.driver", driverPath);

		String chromeDriverVersion = getChromeDriverVersion(driverPath);

		String chromeVersion = getInstalledChromeVersion();

		// System.err.println("Gatekeeper: ChromeDriver version: " + chromeDriverVersion);
		// System.err.println("Gatekeeper: Chrome version: " + chromeVersion);

		int driverMajor = parseMajor(chromeDriverVersion);
		int chromeMajor = parseMajor(chromeVersion);

		if (Math.abs(driverMajor - chromeMajor) >= MIN_VERSION_GAP) {
			throw new RuntimeException(
					"Chrome/ChromeDriver major version mismatch: " + chromeMajor + " / " + driverMajor);
		}

		// 3️⃣ Now safe to construct the driver
		ChromeDriver driver = null;
		try {
			ChromeOptions options = new ChromeOptions();
			driver = new ChromeDriver(options);
			System.err.println("ChromeDriver instance created safely after version check");
		} finally {
			if (driver != null)
				driver.quit();
		}
	}

	@Test
	public void test() {
		System.err.println("Test executed");
	}

	private static String getChromeDriverVersion(String driverPath) throws Exception {
		ProcessBuilder pb = new ProcessBuilder(driverPath, "--version");
		pb.redirectErrorStream(true);
		Process process = pb.start();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line = reader.readLine();
			if (line != null && line.startsWith("ChromeDriver")) {
				return line.split(" ")[1];
			}
		}
		throw new RuntimeException("Cannot determine ChromeDriver version from: " + driverPath);
	}

	private static String getInstalledChromeVersion() throws Exception {
		String os = System.getProperty("os.name").toLowerCase();
		String chromeCommand;
		if (os.contains("win")) {
			// Windows: try default path
			chromeCommand = "reg query \"HKEY_CURRENT_USER\\Software\\Google\\Chrome\\BLBeacon\" /v version";
			Process process = Runtime.getRuntime().exec(chromeCommand);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.contains("version")) {
						return line.trim().split("\\s+")[line.trim().split("\\s+").length - 1];
					}
				}
			}
		} else if (os.contains("mac")) {
			chromeCommand = "/Applications/Googlx`e\\ Chrome.app/Contents/MacOS/Google\\ Chrome --version";
			Process process = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", chromeCommand });
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				return reader.readLine().split(" ")[2];
			}
		} else {
			// Linux: assume google-chrome in PATH
			chromeCommand = "google-chrome --version";
			Process process = Runtime.getRuntime().exec(chromeCommand);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				return reader.readLine().split(" ")[2];
			}
		}
		throw new RuntimeException("Cannot determine installed Chrome version");
	}

	private static int parseMajor(String version) {
		Matcher m = Pattern.compile("(\\d+)").matcher(version);
		if (m.find())
			return Integer.parseInt(m.group(1));
		throw new RuntimeException("Cannot parse major version from: " + version);
	}
}