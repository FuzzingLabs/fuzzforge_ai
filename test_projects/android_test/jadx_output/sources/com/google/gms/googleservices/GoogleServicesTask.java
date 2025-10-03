package com.google.gms.googleservices;

import androidx.core.app.NotificationCompat;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

/* loaded from: classes.dex */
public abstract class GoogleServicesTask extends DefaultTask {
    public static final String JSON_FILE_NAME = "google-services.json";
    private static final String OAUTH_CLIENT_TYPE_WEB = "3";
    private static final String STATUS_DISABLED = "1";
    private static final String STATUS_ENABLED = "2";
    private String buildType;
    private File intermediateDir;
    private ObjectFactory objectFactory;
    private List<String> productFlavors;

    @Input
    public abstract Property<String> getApplicationId();

    @Inject
    public GoogleServicesTask(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @OutputDirectory
    public File getIntermediateDir() {
        return this.intermediateDir;
    }

    @Input
    public String getBuildType() {
        return this.buildType;
    }

    @Input
    public List<String> getProductFlavors() {
        return this.productFlavors;
    }

    public void setIntermediateDir(File intermediateDir) {
        this.intermediateDir = intermediateDir;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    public void setProductFlavors(List<String> productFlavors) {
        this.productFlavors = productFlavors;
    }

    @TaskAction
    public void action() throws IOException {
        File quickstartFile = null;
        List<String> fileLocations = getJsonLocations(this.buildType, this.productFlavors);
        String searchedLocation = System.lineSeparator();
        Iterator it = this.objectFactory.fileCollection().from(new Object[]{fileLocations}).iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            File jsonFile = (File) it.next();
            String path = jsonFile.getPath();
            String lineSeparator = System.lineSeparator();
            StringBuilder sb = new StringBuilder(String.valueOf(searchedLocation).length() + String.valueOf(path).length() + String.valueOf(lineSeparator).length());
            sb.append(searchedLocation);
            sb.append(path);
            sb.append(lineSeparator);
            searchedLocation = sb.toString();
            if (jsonFile.isFile()) {
                quickstartFile = jsonFile;
                break;
            }
        }
        if (quickstartFile == null || !quickstartFile.isFile()) {
            throw new GradleException(String.format("File %s is missing. The Google Services Plugin cannot function without it. %n Searched Location: %s", JSON_FILE_NAME, searchedLocation));
        }
        Logger logger = getLogger();
        String valueOf = String.valueOf(quickstartFile.getPath());
        logger.info(valueOf.length() != 0 ? "Parsing json file: ".concat(valueOf) : new String("Parsing json file: "));
        deleteFolder(this.intermediateDir);
        if (!this.intermediateDir.mkdirs()) {
            String valueOf2 = String.valueOf(this.intermediateDir);
            StringBuilder sb2 = new StringBuilder(String.valueOf(valueOf2).length() + 25);
            sb2.append("Failed to create folder: ");
            sb2.append(valueOf2);
            throw new GradleException(sb2.toString());
        }
        JsonElement root = new JsonParser().parse(Files.newReader(quickstartFile, Charsets.UTF_8));
        if (!root.isJsonObject()) {
            throw new GradleException("Malformed root json");
        }
        JsonObject rootObject = root.getAsJsonObject();
        Map<String, String> resValues = new TreeMap<>();
        Map<String, Map<String, String>> resAttributes = new TreeMap<>();
        handleProjectNumberAndProjectId(rootObject, resValues);
        handleFirebaseUrl(rootObject, resValues);
        JsonObject clientObject = getClientForPackageName(rootObject);
        if (clientObject != null) {
            handleAnalytics(clientObject, resValues);
            handleMapsService(clientObject, resValues);
            handleGoogleApiKey(clientObject, resValues);
            handleGoogleAppId(clientObject, resValues);
            handleWebClientId(clientObject, resValues);
            File values = new File(this.intermediateDir, "values");
            if (!values.exists() && !values.mkdirs()) {
                String valueOf3 = String.valueOf(values);
                StringBuilder sb3 = new StringBuilder(String.valueOf(valueOf3).length() + 25);
                sb3.append("Failed to create folder: ");
                sb3.append(valueOf3);
                throw new GradleException(sb3.toString());
            }
            Files.asCharSink(new File(values, "values.xml"), Charsets.UTF_8, new FileWriteMode[0]).write(getValuesContent(resValues, resAttributes));
            return;
        }
        String str = (String) getApplicationId().get();
        StringBuilder sb4 = new StringBuilder(String.valueOf(str).length() + 44);
        sb4.append("No matching client found for package name '");
        sb4.append(str);
        sb4.append("'");
        throw new GradleException(sb4.toString());
    }

    private void handleFirebaseUrl(JsonObject rootObject, Map<String, String> resValues) throws IOException {
        JsonObject projectInfo = rootObject.getAsJsonObject("project_info");
        if (projectInfo == null) {
            throw new GradleException("Missing project_info object");
        }
        JsonPrimitive firebaseUrl = projectInfo.getAsJsonPrimitive("firebase_url");
        if (firebaseUrl != null) {
            resValues.put("firebase_database_url", firebaseUrl.getAsString());
        }
    }

    private void handleProjectNumberAndProjectId(JsonObject rootObject, Map<String, String> resValues) throws IOException {
        JsonObject projectInfo = rootObject.getAsJsonObject("project_info");
        if (projectInfo == null) {
            throw new GradleException("Missing project_info object");
        }
        JsonPrimitive projectNumber = projectInfo.getAsJsonPrimitive("project_number");
        if (projectNumber == null) {
            throw new GradleException("Missing project_info/project_number object");
        }
        resValues.put("gcm_defaultSenderId", projectNumber.getAsString());
        JsonPrimitive projectId = projectInfo.getAsJsonPrimitive("project_id");
        if (projectId != null) {
            resValues.put("project_id", projectId.getAsString());
            JsonPrimitive bucketName = projectInfo.getAsJsonPrimitive("storage_bucket");
            if (bucketName != null) {
                resValues.put("google_storage_bucket", bucketName.getAsString());
                return;
            }
            return;
        }
        throw new GradleException("Missing project_info/project_id object");
    }

    private void handleWebClientId(JsonObject clientObject, Map<String, String> resValues) {
        JsonObject oauthClientObject;
        JsonPrimitive clientType;
        JsonPrimitive clientId;
        JsonArray array = clientObject.getAsJsonArray("oauth_client");
        if (array != null) {
            int count = array.size();
            for (int i = 0; i < count; i++) {
                JsonElement oauthClientElement = array.get(i);
                if (oauthClientElement != null && oauthClientElement.isJsonObject() && (clientType = (oauthClientObject = oauthClientElement.getAsJsonObject()).getAsJsonPrimitive("client_type")) != null) {
                    String clientTypeStr = clientType.getAsString();
                    if (OAUTH_CLIENT_TYPE_WEB.equals(clientTypeStr) && (clientId = oauthClientObject.getAsJsonPrimitive("client_id")) != null) {
                        resValues.put("default_web_client_id", clientId.getAsString());
                        return;
                    }
                }
            }
        }
    }

    private void handleAnalytics(JsonObject clientObject, Map<String, String> resValues) throws IOException {
        JsonObject analyticsProp;
        JsonPrimitive trackingId;
        JsonObject analyticsService = getServiceByName(clientObject, "analytics_service");
        if (analyticsService == null || (analyticsProp = analyticsService.getAsJsonObject("analytics_property")) == null || (trackingId = analyticsProp.getAsJsonPrimitive("tracking_id")) == null) {
            return;
        }
        resValues.put("ga_trackingId", trackingId.getAsString());
        File xml = new File(this.intermediateDir, "xml");
        if (!xml.exists() && !xml.mkdirs()) {
            String valueOf = String.valueOf(xml);
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 25);
            sb.append("Failed to create folder: ");
            sb.append(valueOf);
            throw new GradleException(sb.toString());
        }
        Files.asCharSink(new File(xml, "global_tracker.xml"), Charsets.UTF_8, new FileWriteMode[0]).write(getGlobalTrackerContent(trackingId.getAsString()));
    }

    private void handleMapsService(JsonObject clientObject, Map<String, String> resValues) throws IOException {
        JsonObject mapsService = getServiceByName(clientObject, "maps_service");
        if (mapsService == null) {
            return;
        }
        String apiKey = getAndroidApiKey(clientObject);
        if (apiKey != null) {
            resValues.put("google_maps_key", apiKey);
            return;
        }
        throw new GradleException("Missing api_key/current_key object");
    }

    private void handleGoogleApiKey(JsonObject clientObject, Map<String, String> resValues) {
        String apiKey = getAndroidApiKey(clientObject);
        if (apiKey != null) {
            resValues.put("google_api_key", apiKey);
            resValues.put("google_crash_reporting_api_key", apiKey);
            return;
        }
        throw new GradleException("Missing api_key/current_key object");
    }

    private String getAndroidApiKey(JsonObject clientObject) {
        JsonArray array = clientObject.getAsJsonArray("api_key");
        if (array != null) {
            int count = array.size();
            for (int i = 0; i < count; i++) {
                JsonElement apiKeyElement = array.get(i);
                if (apiKeyElement != null && apiKeyElement.isJsonObject()) {
                    JsonObject apiKeyObject = apiKeyElement.getAsJsonObject();
                    JsonPrimitive currentKey = apiKeyObject.getAsJsonPrimitive("current_key");
                    if (currentKey != null) {
                        return currentKey.getAsString();
                    }
                }
            }
            return null;
        }
        return null;
    }

    private static void findStringByName(JsonObject jsonObject, String stringName, Map<String, String> resValues) {
        JsonPrimitive id = jsonObject.getAsJsonPrimitive(stringName);
        if (id != null) {
            resValues.put(stringName, id.getAsString());
        }
    }

    private JsonObject getClientForPackageName(JsonObject jsonObject) {
        JsonObject clientObject;
        JsonObject clientInfo;
        JsonObject androidClientInfo;
        JsonPrimitive clientPackageName;
        JsonArray array = jsonObject.getAsJsonArray("client");
        if (array != null) {
            int count = array.size();
            for (int i = 0; i < count; i++) {
                JsonElement clientElement = array.get(i);
                if (clientElement != null && clientElement.isJsonObject() && (clientInfo = (clientObject = clientElement.getAsJsonObject()).getAsJsonObject("client_info")) != null && (androidClientInfo = clientInfo.getAsJsonObject("android_client_info")) != null && (clientPackageName = androidClientInfo.getAsJsonPrimitive("package_name")) != null && ((String) getApplicationId().get()).equals(clientPackageName.getAsString())) {
                    return clientObject;
                }
            }
            return null;
        }
        return null;
    }

    private void handleGoogleAppId(JsonObject clientObject, Map<String, String> resValues) throws IOException {
        JsonObject clientInfo = clientObject.getAsJsonObject("client_info");
        if (clientInfo == null) {
            throw new GradleException("Client does not have client info");
        }
        JsonPrimitive googleAppId = clientInfo.getAsJsonPrimitive("mobilesdk_app_id");
        String googleAppIdStr = googleAppId == null ? null : googleAppId.getAsString();
        if (Strings.isNullOrEmpty(googleAppIdStr)) {
            throw new GradleException("Missing Google App Id. Please follow instructions on https://firebase.google.com/ to get a valid config file that contains a Google App Id");
        }
        resValues.put("google_app_id", googleAppIdStr);
    }

    private JsonObject getServiceByName(JsonObject clientObject, String serviceName) {
        JsonObject service;
        JsonPrimitive status;
        JsonObject services = clientObject.getAsJsonObject("services");
        if (services == null || (service = services.getAsJsonObject(serviceName)) == null || (status = service.getAsJsonPrimitive(NotificationCompat.CATEGORY_STATUS)) == null) {
            return null;
        }
        String statusStr = status.getAsString();
        if (STATUS_DISABLED.equals(statusStr)) {
            return null;
        }
        if (!STATUS_ENABLED.equals(statusStr)) {
            getLogger().warn(String.format("Status with value '%1$s' for service '%2$s' is unknown", statusStr, serviceName));
            return null;
        }
        return service;
    }

    private static String getGlobalTrackerContent(String ga_trackingId) {
        StringBuilder sb = new StringBuilder(String.valueOf(ga_trackingId).length() + 128);
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n    <string name=\"ga_trackingId\" translatable=\"false\">");
        sb.append(ga_trackingId);
        sb.append("</string>\n</resources>\n");
        return sb.toString();
    }

    private static String getValuesContent(Map<String, String> values, Map<String, Map<String, String>> attributes) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n");
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String name = entry.getKey();
            sb.append("    <string name=\"");
            sb.append(name);
            sb.append("\" translatable=\"false\"");
            if (attributes.containsKey(name)) {
                for (Map.Entry<String, String> attr : attributes.get(name).entrySet()) {
                    sb.append(" ");
                    sb.append(attr.getKey());
                    sb.append("=\"");
                    sb.append(attr.getValue());
                    sb.append("\"");
                }
            }
            sb.append(">");
            sb.append(entry.getValue());
            sb.append("</string>\n");
        }
        sb.append("</resources>\n");
        return sb.toString();
    }

    private static void deleteFolder(File folder) {
        if (!folder.exists()) {
            return;
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else if (!file.delete()) {
                    String valueOf = String.valueOf(file);
                    StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 18);
                    sb.append("Failed to delete: ");
                    sb.append(valueOf);
                    throw new GradleException(sb.toString());
                }
            }
        }
        if (!folder.delete()) {
            String valueOf2 = String.valueOf(folder);
            StringBuilder sb2 = new StringBuilder(String.valueOf(valueOf2).length() + 18);
            sb2.append("Failed to delete: ");
            sb2.append(valueOf2);
            throw new GradleException(sb2.toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static long countSlashes(String input) {
        return input.codePoints().filter(new IntPredicate() { // from class: com.google.gms.googleservices.GoogleServicesTask$$ExternalSyntheticLambda3
            @Override // java.util.function.IntPredicate
            public final boolean test(int i) {
                return GoogleServicesTask.lambda$countSlashes$0(i);
            }
        }).count();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ boolean lambda$countSlashes$0(int x) {
        return x == 47;
    }

    static List<String> getJsonLocations(String buildType, List<String> flavorNames) {
        List<String> fileLocations = new ArrayList<>();
        String flavorName = flavorNames.stream().reduce("", new BinaryOperator() { // from class: com.google.gms.googleservices.GoogleServicesTask$$ExternalSyntheticLambda0
            @Override // java.util.function.BiFunction
            public final Object apply(Object obj, Object obj2) {
                return GoogleServicesTask.lambda$getJsonLocations$1((String) obj, (String) obj2);
            }
        });
        fileLocations.add("");
        StringBuilder sb = new StringBuilder(String.valueOf(flavorName).length() + 5 + String.valueOf(buildType).length());
        sb.append("src/");
        sb.append(flavorName);
        sb.append("/");
        sb.append(buildType);
        fileLocations.add(sb.toString());
        StringBuilder sb2 = new StringBuilder(String.valueOf(buildType).length() + 5 + String.valueOf(flavorName).length());
        sb2.append("src/");
        sb2.append(buildType);
        sb2.append("/");
        sb2.append(flavorName);
        fileLocations.add(sb2.toString());
        String valueOf = String.valueOf(flavorName);
        fileLocations.add(valueOf.length() != 0 ? "src/".concat(valueOf) : new String("src/"));
        String valueOf2 = String.valueOf(buildType);
        fileLocations.add(valueOf2.length() != 0 ? "src/".concat(valueOf2) : new String("src/"));
        String capitalize = capitalize(buildType);
        StringBuilder sb3 = new StringBuilder(String.valueOf(flavorName).length() + 4 + String.valueOf(capitalize).length());
        sb3.append("src/");
        sb3.append(flavorName);
        sb3.append(capitalize);
        fileLocations.add(sb3.toString());
        String valueOf3 = String.valueOf(buildType);
        fileLocations.add(valueOf3.length() != 0 ? "src/".concat(valueOf3) : new String("src/"));
        String fileLocation = "src";
        for (String flavor : flavorNames) {
            String valueOf4 = String.valueOf(fileLocation);
            StringBuilder sb4 = new StringBuilder(String.valueOf(valueOf4).length() + 1 + String.valueOf(flavor).length());
            sb4.append(valueOf4);
            sb4.append("/");
            sb4.append(flavor);
            fileLocation = sb4.toString();
            fileLocations.add(fileLocation);
            StringBuilder sb5 = new StringBuilder(String.valueOf(fileLocation).length() + 1 + String.valueOf(buildType).length());
            sb5.append(fileLocation);
            sb5.append("/");
            sb5.append(buildType);
            fileLocations.add(sb5.toString());
            String valueOf5 = String.valueOf(fileLocation);
            String valueOf6 = String.valueOf(capitalize(buildType));
            fileLocations.add(valueOf6.length() != 0 ? valueOf5.concat(valueOf6) : new String(valueOf5));
        }
        return (List) fileLocations.stream().distinct().sorted(Comparator.comparing(new Function() { // from class: com.google.gms.googleservices.GoogleServicesTask$$ExternalSyntheticLambda2
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                long countSlashes;
                countSlashes = GoogleServicesTask.countSlashes((String) obj);
                return Long.valueOf(countSlashes);
            }
        }).reversed()).map(new Function() { // from class: com.google.gms.googleservices.GoogleServicesTask$$ExternalSyntheticLambda1
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return GoogleServicesTask.lambda$getJsonLocations$2((String) obj);
            }
        }).collect(Collectors.toList());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ String lambda$getJsonLocations$1(String a, String b) {
        String valueOf = String.valueOf(a);
        String valueOf2 = String.valueOf(a.length() == 0 ? b : capitalize(b));
        return valueOf2.length() != 0 ? valueOf.concat(valueOf2) : new String(valueOf);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ String lambda$getJsonLocations$2(String location) {
        if (location.isEmpty()) {
            String valueOf = String.valueOf(location);
            String valueOf2 = String.valueOf(JSON_FILE_NAME);
            return valueOf2.length() != 0 ? valueOf.concat(valueOf2) : new String(valueOf);
        }
        StringBuilder sb = new StringBuilder(String.valueOf(location).length() + 21);
        sb.append(location);
        sb.append('/');
        sb.append(JSON_FILE_NAME);
        return sb.toString();
    }

    public static String capitalize(String s) {
        if (s.length() == 0) {
            return s;
        }
        String valueOf = String.valueOf(s.substring(0, 1).toUpperCase());
        String valueOf2 = String.valueOf(s.substring(1).toLowerCase());
        return valueOf2.length() != 0 ? valueOf.concat(valueOf2) : new String(valueOf);
    }
}
