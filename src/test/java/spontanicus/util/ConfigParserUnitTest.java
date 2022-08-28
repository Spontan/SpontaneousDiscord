package spontanicus.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigParserUnitTest {

    private ConfigParser parser;

    @BeforeEach
    public void before(){
        parser = new ConfigParser();
    }

    @Test
    public void findComment(){
        assertThat(parser.findCommentStart("#comment")).isEqualTo(0);
    }

    @Test
    public void findNoComment(){
        assertThat(parser.findCommentStart("")).isEqualTo(0);
        assertThat(parser.findCommentStart("text")).isEqualTo(4);
        assertThat(parser.findCommentStart("\"quote\"")).isEqualTo(7);
        assertThat(parser.findCommentStart("\"#nocomment\"")).isEqualTo(12);
        assertThat(parser.findCommentStart("\"#nocomment")).isEqualTo(11);
        assertThat(parser.findCommentStart("\"quote\"text\"#comment")).isEqualTo(20);
    }

    @Test
    public void loadParametersFromFile(){
        ParameterMap parameters = parser.parseConfigFile("src/test/config.yml");
        assertThat(parameters.get("guildId")).isEqualTo("1234567890");
        assertThat(parameters.get("activity")).isEqualTo("all your streams!");
    }


    // not used for now since the comment finding method is very simple right now
    //@Test
    public void findFirstComment(){
        assertThat(parser.findCommentStart("#comment#nocomment")).isEqualTo(0);
        assertThat(parser.findCommentStart("text#comment#nocomment")).isEqualTo(4);
        assertThat(parser.findCommentStart("#comment\"#nocomment")).isEqualTo(0);
    }

}
