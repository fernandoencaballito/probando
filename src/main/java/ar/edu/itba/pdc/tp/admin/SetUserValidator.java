package ar.edu.itba.pdc.tp.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SetUserValidator {
    private static String setCommandRegex = "SET\\s.+\\s.+\\r\\n";
    private static Pattern setCommandPattern = Pattern.compile(setCommandRegex);
    private static String urlRegex = "[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    private static Pattern urlPattern = Pattern.compile(urlRegex);

    private static final String usernameRegex = "[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*(@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,}))?";
    private static Pattern usernamePattern = Pattern.compile(usernameRegex);

    public static List<String> validate(String arg) {
        List<String> ans = null;
        if (setCommandPattern.matcher(arg).matches()) {
            arg = arg.trim();
            String[] values = arg.split("\\s");
            String username = values[1];
            String originUrl = values[2];
            if (urlPattern.matcher(originUrl).matches()) {
                if (usernamePattern.matcher(username).matches()) {
                    ans = new ArrayList<String>();
                    ans.add(username);
                    ans.add(originUrl);
                }

            }
        }
        return ans;
    }
}
