import io.github.wfouche.tulip.api.TulipApi;
import org.apache.commons.cli.*;

public class Benchmark01 {

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("c", "config", true, "Benchmark configuration file");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            String configFile = cmd.getOptionValue("config");
            TulipApi.runTulip(configFile, new Benchmark01UserFactory());

        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }

    }

}
