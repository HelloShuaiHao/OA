package cn.iocoder.yudao.module.agentx.service.audit;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AgentxAuditDesensitizeService {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(password|passwd|pwd)\\s*[:=]\\s*([^,\\s}]+)");
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(?i)(api[_-]?key|token|secret)\\s*[:=]\\s*([^,\\s}]+)");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\d)(1\\d{10})(?!\\d)");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(?<!\\w)(\\d{17}[0-9Xx])(?!\\w)");

    public String mask(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String masked = PASSWORD_PATTERN.matcher(input).replaceAll("$1=***");
        masked = API_KEY_PATTERN.matcher(masked).replaceAll("$1=***");
        masked = maskByPattern(masked, PHONE_PATTERN, 3, 7, "****");
        masked = maskByPattern(masked, ID_CARD_PATTERN, 6, 14, "********");
        return masked;
    }

    private String maskByPattern(String text, Pattern pattern, int keepLeft, int keepRightStart, String stars) {
        Matcher matcher = pattern.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String value = matcher.group(1);
            String replaced = value.substring(0, keepLeft) + stars + value.substring(keepRightStart);
            matcher.appendReplacement(buffer, replaced);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

}
