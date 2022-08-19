package spontanicus.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ConfigParser {
    private static final Logger logger = Logger.getLogger("ConfigParser");


    public ParameterMap parseConfigFile(String path){
        Path pathToConfig = Paths.get(path);

        try{
            if(!Files.exists(pathToConfig)) {
                logger.log(Level.WARNING, "Config file could not be found for path \"" + pathToConfig.toAbsolutePath() + "\" could not be found. Returning empty configuration");
                return new ParameterMap();
            }
            else{
                return parseLines(Files.lines(pathToConfig));
            }

        } catch (IOException e) {
            logger.log(Level.WARNING, "Exception while reading config file");
            e.printStackTrace();
        }
        return new ParameterMap();
    }

    protected ParameterMap parseLines(Stream<String> lines){
        ParameterMap parameterMap = new ParameterMap();

        lines.forEach(line -> {
            line = filterContent(line);
            if(line.length()>0 && line.contains(":")) {
                String[] lineSplits = line.split(":", 2);
                String parameterName = lineSplits[0];
                String parameterValue = lineSplits[1];
                parameterName = parameterName.trim();
                parameterValue = parameterValue.trim();
                if (parameterValue.startsWith("\"") && parameterValue.endsWith("\""))
                    parameterValue = parameterValue.substring(1, parameterValue.length() - 1);

                parameterMap.set(parameterName, parameterValue);
            }
        });
        return parameterMap;
    }

    protected String filterContent(String line){
        line = line.trim();
        line = line.substring(0, findCommentStart(line));

        return line;
    }

    /**
     * Finds the start of the comment section in the provided string.
     * The comment starts with the first occurrence of a '#' character which is not between quotes (denoting a string value).
     * To remove the comment section from the provided string, you can use "line.substring(0, findCommentStart(line))".
     * This works even if no comment exists (in this case the string will remain unchanged).
     * @param line The line for which to find the start of the comment
     * @return The index of the '#' character denoting the start of the comment section, or the length of the line,
     * if no comment section can be found.
     */
    protected int findCommentStart(String line) {
        if(line.startsWith("#"))
            return 0;
        else
            return line.length();
    }

    protected boolean isValidParameterString(String s){
        Pattern whitSpacePattern = Pattern.compile("\\s");
        if(s.isBlank())
            return false;
        if(s.startsWith("\"")){
            return s.endsWith("\"");
        }
        return whitSpacePattern.matcher(s).find();
    }
}
