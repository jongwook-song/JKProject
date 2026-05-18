package com.jkproject.JkProject.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ObjectUtil {

    public static final String NEW_LINE = "\r\n";
    public static final String STR_ELLIPSIS = "...";

    public static boolean isNull(Object object) {
        return object == null;
    }

    public static boolean isNotNull(Object object) {

        return !ObjectUtil.isNull(object);
    }

    public static boolean isNotNull(Object[] objects) {

        return (objects != null && objects.length > 0);
    }

    public static boolean isEmpty(Object object) {
        if (object == null) {
            return true;
        }

        if (object instanceof java.lang.String) {
            return ((String) object).length() == 0;
        } else if (object instanceof java.util.Collection) {
            return ((Collection<?>) object).size() == 0;
        } else if (object instanceof java.util.Map) {
            return ((Map<?, ?>) object).size() == 0;
        } else if (object instanceof Object[]) {
            return ((Object[]) object).length == 0;
        } else if (object instanceof java.lang.Integer) {
            return ((Integer) object).intValue() == 0;
        } else if (object instanceof java.util.List) {
            return ((List<?>) object).size() == 0;
        }

        return false;
    }

    public static boolean isNotEmpty(Object object) {

        return !ObjectUtil.isEmpty(object);
    }

    public static Object[] getIncluedObjects(Object[] obj, Object[] targetObj) {
        if (isEmpty(obj)) {
            return null;
        }
        if (isEmpty(targetObj)) {
            return null;
        }
        List<Object> incluedObjects = new ArrayList<>();
        for (int i = 0; i < obj.length; ++i) {
            if (isNull(obj)) {
                continue;
            }
            for (int j = 0; j < targetObj.length; ++j) {
                if (isNull(targetObj)) {
                    continue;
                }
                if (obj[i].equals(targetObj[j])) {
                    incluedObjects.add(targetObj[j]);
                }
            }
        }

        return Utils.toArray(incluedObjects);
    }

    public static int compareNullObject(Object o1, Object o2) {

        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 != null && o2 == null) {
            return 1;
        }
        if (o1 == null && o2 != null) {
            return -1;
        }

        return 2;
    }

    public static void merge(Object obj, Object update, boolean mergeNull) {
        merge(obj, update, mergeNull, false);
    }

    public static void merge(Object obj, Object update, boolean mergeNull, boolean currentClass) {

        if (!obj.getClass().isAssignableFrom(update.getClass())) {
            return;
        }

        Method[] methods = obj.getClass().getMethods();
        Method fromMethod = null;

        for (int i = 0; i < methods.length; i++) {

            fromMethod = methods[i];

            // 같은 타입의 클레스에 대해 비교하고자 할경우 사용.
            // fromMethod.getDeclaringClass().equals(obj.getClass())
            if (currentClass && !fromMethod.getDeclaringClass().equals(obj.getClass())) {
                continue;
            }

            if (fromMethod.getName().startsWith("get")) {

                String fromName = fromMethod.getName();
                String toName = fromName.replaceFirst("get", "set");

                try {
                    Method toMetod = obj.getClass().getMethod(toName, new Class[]{fromMethod.getReturnType()});
                    Object value = fromMethod.invoke(update, (Object[]) null);
                    if (ObjectUtil.isNotEmpty(value)) {
                        if (mergeNull) {
                            Object toValue = fromMethod.invoke(obj, (Object[]) null);
                            if (ObjectUtil.isEmpty(toValue)) {
                                toMetod.invoke(obj, new Object[]{value});
                            }
                        } else {
                            toMetod.invoke(obj, new Object[]{value});
                        }
                    }
                } catch (NoSuchMethodException e) {

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String encodePassword(String password, String algorithm) {
        byte[] unencodedPassword = password.getBytes();

        MessageDigest md = null;

        try {
            // first create an instance, given the provider
            md = MessageDigest.getInstance(algorithm);
        } catch (Exception e) {
            return password;
        }

        md.reset();

        // call the update method one or more times
        // (useful when you don't know the size of your data, eg. stream)
        md.update(unencodedPassword);

        // now calculate the hash
        byte[] encodedPassword = md.digest();

        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < encodedPassword.length; i++) {
            if (((int) encodedPassword[i] & 0xff) < 0x10) {
                buf.append("0");
            }

            buf.append(Long.toString((int) encodedPassword[i] & 0xff, 16));
        }

        return buf.toString();
    }


    public static String trim(String origString, String trimString) {
        int startPosit = origString.indexOf(trimString);
        if (startPosit != -1) {
            int endPosit = trimString.length() + startPosit;
            return origString.substring(0, startPosit) + origString.substring(endPosit);
        }
        return origString;
    }

    public static String[] getStringArray(String str, String strToken) {
        return getStringArray(str, strToken, false);
    }

    public static String[] getStringArray(String str, String strToken, boolean isTrim) {

        if (isNull(str)) {
            return null;
        }

        if (str.indexOf(strToken) != -1) {
            StringTokenizer st = new StringTokenizer(str, strToken);
            String[] stringArray = new String[st.countTokens()];
            for (int i = 0; st.hasMoreTokens(); i++) {
                if (isTrim) {
                    stringArray[i] = st.nextToken().trim();
                } else {
                    stringArray[i] = st.nextToken();
                }
            }
            return stringArray;
        }
        return new String[]{str};
    }

    public static int[] getIntegerArray(String str, String strToken) {
        return getIntegerArray(str, strToken, false);
    }

    public static int[] getIntegerArray(String str, String strToken, boolean isTrim) {

        if (isNull(str)) {
            return null;
        }

        if (str.indexOf(strToken) != -1) {
            StringTokenizer st = new StringTokenizer(str, strToken);
            int[] integerArray = new int[st.countTokens()];
            for (int i = 0; st.hasMoreTokens(); i++) {
                if (isTrim) {
                    integerArray[i] = Integer.parseInt(st.nextToken().trim());
                } else {
                    integerArray[i] = Integer.parseInt(st.nextToken());
                }
            }
            return integerArray;
        }
        return new int[]{Integer.parseInt(str)};
    }


    public static boolean isNotEmpty(String str) {
        return isNotEmpty(str, false);
    }

    public static boolean isNotEmpty(String str, boolean isTrim) {

        if (isTrim) {
            return !isEmpty(str, true);
        } else {
            return !isEmpty(str);
        }
    }

    public static boolean isEmpty(String str) {
        return isEmpty(str, false);
    }

    public static boolean isEmpty(String str, boolean isTrim) {
        if (isTrim) {
            return isEmptyTrimmed(str);
        } else {
            return (str == null || str.length() == 0);
        }
    }

    public static final boolean isEmptyTrimmed(String foo) {
        return (foo == null || foo.trim().length() == 0);
    }

    public static String replace(String szOriginal, String szOld, String szNew) {
        return replace(szOriginal, szOld, szNew, 0);
    }

    public static String replace(String szOriginal, String szOld, String szNew, int nReplaceCount) {
        if (szOriginal == null || szOld == null || szNew == null) {
            throw new IllegalArgumentException();
        }

        StringBuffer sbResult = new StringBuffer();
        int nFromIndex = 0, nToIndex = 0;
        int nOldLength = szOld.length();
        int i = 0;

        while ((nToIndex = szOriginal.indexOf(szOld, nFromIndex)) >= 0) {
            sbResult.append(szOriginal.substring(nFromIndex, nToIndex)).append(szNew);
            nFromIndex = nToIndex + nOldLength;

            if (nReplaceCount != 0 && ++i == nReplaceCount) {
                return sbResult.append(szOriginal.substring(nFromIndex)).toString();
            }
        }

        return sbResult.append(szOriginal.substring(nFromIndex)).toString();
    }

    public static boolean isPatternMatching(String str, String pattern) throws Exception {
        // if url has wild key, i.e. "*", convert it to ".*" so that we can
        // perform regex matching
        if (pattern.indexOf('*') >= 0) {
            pattern = pattern.replaceAll("\\*", ".*");
        }

        pattern = "^" + pattern + "$";

        return Pattern.matches(pattern, str);
    }



    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        int sz = str.length();
        if (sz == 0) {
            return false;
        }
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static List<String> getTokenFixSize(String lst, int fixSize) {
        List<String> alToken = new ArrayList<String>();

        if (lst != null) {
            if (lst.length() >= fixSize && fixSize > 0) {

                for (int i = 0; i < lst.length(); i += fixSize) {
                    if (i + fixSize < lst.length()) {
                        alToken.add(lst.substring(i, i + fixSize));
                    } else {
                        alToken.add(lst.substring(i, lst.length()));
                    }
                }

            } else {
                alToken.add(lst);
            }
        }

        return alToken;
    }

    public static List<String> getTokenFixSize(String lst) {
        return getTokenFixSize(lst, 2);
    }

    public static List<String> getAddTokensFixSize(String lst, int fixSize, boolean bExcludeLastToken) {
        List<String> tokens = getTokenFixSize(lst, fixSize);
        List<String> newTokens = new ArrayList<String>();

        String sumToken = "";
        for (int i = 0; tokens != null && i < tokens.size() - (bExcludeLastToken ? 1 : 0); i++) {
            sumToken = sumToken + tokens.get(i);
            newTokens.add(sumToken);
        }

        return newTokens;
    }

    public static List<String> getAddTokensFixSize(String lst, int fixSize) {
        return getAddTokensFixSize(lst, fixSize, false);
    }

    public static List<String> getAddTokensFixSize(String lst) {
        return getAddTokensFixSize(lst, 2, false);
    }

    public static List<String> getAddTokens(String lst, String separator, boolean bExcludeLastToken) {
        List<String> tokens = getTokens(lst, separator);
        List<String> newTokens = new ArrayList<String>();

        String sumToken = "";
        for (int i = 0; tokens != null && i < tokens.size() - (bExcludeLastToken ? 1 : 0); i++) {
            sumToken = sumToken + tokens.get(i) + separator;
            newTokens.add(sumToken);
        }

        return newTokens;
    }

    public static List<String> getAddTokens(String lst, String separator) {
        return getAddTokens(lst, separator, false);
    }

    public static List<String> getTokens(String lst, String separator, boolean includeNullString) {
        List<String> tokens = new ArrayList<String>();

        if (lst != null) {
            StringTokenizer st = new StringTokenizer(lst, separator);
            while (st.hasMoreTokens()) {
                try {
                    String en = st.nextToken().trim();
                    if (includeNullString || isNotNull(en)) {
                        tokens.add(en);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return tokens;
    }

    public static List<String> getTokens(String lst, String separator) {
        return getTokens(lst, separator, true);
    }

    public static List<String> getTokens(String lst) {
        return getTokens(lst, ",");
    }

    public static String convertToCamelCase(String targetString, char posChar) {
        StringBuffer result = new StringBuffer();
        boolean nextUpper = false;
        String allLower = targetString.toLowerCase();

        for (int i = 0; i < allLower.length(); i++) {
            char currentChar = allLower.charAt(i);
            if (currentChar == posChar) {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    currentChar = Character.toUpperCase(currentChar);
                    nextUpper = false;
                }
                result.append(currentChar);
            }
        }
        return result.toString();
    }

    public static String convertToCamelCase(String underScore) {
        return convertToCamelCase(underScore, '_');
    }

    public static String[] tokenizeToStringArray(String str, String delimiters) {
        return tokenizeToStringArray(str, delimiters, true, true);
    }

    public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens,
                                                 boolean ignoreEmptyTokens) {

        if (str == null) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (!ignoreEmptyTokens || token.length() > 0) {
                tokens.add(token);
            }
        }
        return toStringArray(tokens);
    }



    public static String[] toStringArray(Collection<String> collection) {
        if (collection == null) {
            return null;
        }
        return collection.toArray(new String[collection.size()]);
    }

    public static boolean isNull(String szInString) {
        return (szInString == null || szInString.trim().length() == 0);
    }

    public static boolean isNotNull(String szInString) {
        return (szInString != null && szInString.trim().length() > 0);
    }

    public static String getFileName(String szFileName) {
        return getFileName(szFileName, true);
    }

    public static String getFileName(String szFileName, boolean bFileSeparator) {
        String szName = szFileName;

        if (szFileName != null) {
            String cFileSeparator = (bFileSeparator ? File.separator : "/");

            int index = szFileName.lastIndexOf(cFileSeparator);

            if (index < (szFileName.length() - 1) && index != -1) {
                szName = szFileName.substring(index + 1);
            }
        }

        return szName;
    }

    public static String getFileType(String szFileName) {
        return getFileType(szFileName, false);
    }

    public static String getFileType(String szFileName, boolean toUpperCase) {
        if (szFileName != null) {
            int index = szFileName.lastIndexOf(".");

            if (index < (szFileName.length() - 1) && index != -1) {
                if (toUpperCase) {
                    return szFileName.substring(index + 1).toUpperCase();
                } else {
                    return szFileName.substring(index + 1);
                }
            } else {
                return "";
            }
        }

        return "";
    }

    public static boolean isEqualBit(int value1, int value2) {
        if ((value1 & value2) == value2)
            return true;
        else
            return false;
    }


    public static boolean isEqualText(String str1, String str2) {
        // null-safe 코드 ( null == null 은 값다고 본다)
        if (Optional.ofNullable(str1).orElse("").equalsIgnoreCase(Optional.ofNullable(str2).orElse(""))) {
            return true;
        } else {
            return false;
        }
    }

    private static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean isValidatedEmail(String email) {
        return isNotEmpty(email) && EMAIL_ADDRESS_PATTERN.matcher(email).find();
    }

    private static final Pattern DOMAIN_PATTERN = Pattern.compile("^([a-z0-9*]+(-[a-z0-9]+)*\\.)+[a-z]{2,}$", Pattern.CASE_INSENSITIVE);

    public static boolean isValidatedDomain(String domain) {
        return isNotEmpty(domain) && DOMAIN_PATTERN.matcher(domain).find();
    }

    private static final Pattern IP_PATTERN = Pattern.compile("^[0-9*]{1,3}[-]{0,1}[0-9*]{0,3}$", Pattern.CASE_INSENSITIVE);

    public static boolean isValidatedIP(String ips) {
        boolean result = false;
        if (isNotEmpty(ips)) {
            String[] arrIP = ips.split("\\.");
            for (String ip : arrIP) {
                result = true;
                if (!IP_PATTERN.matcher(ip).find()) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    protected static String[] fileSizeUnits = new String[]{"GB", "MB", "KB", "BYTE"};
    protected static double[] fileSizes = new double[]{1024 * 1024 * 1024, 1024 * 1024, 1024, 1};

    public static String getFileSizeAndUnit(double byteSize, String format) {
        double size = 0;
        int index = 0;

        for (int m = fileSizes.length; index < m; index++) {
            size = fileSizes[index];
            if (byteSize >= size) {
                break;
            }
        }
        if (index >= fileSizes.length) {
            index = fileSizes.length - 1;
        }

        return new DecimalFormat(format).format(byteSize / fileSizes[index]) + fileSizeUnits[index];
    }

    public static boolean equals(String source, String target, boolean useCaseSensitive) {
        if (source == null) {
            return false;
        }
        if (useCaseSensitive) {
            return source.equals(target);
        } else {
            return source.equalsIgnoreCase(target);
        }
    }

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String getStringParameter(String str, String defaultData) {
        return str != null ? str : defaultData;
    }

    public static String getStringParameter(Object strObject, String defaultData) {
        return strObject != null ? strObject.toString() : defaultData;
    }

    public static int getIntParameter(String str, int defaultData) {
        if (isEmpty(str)) {
            return defaultData;
        } else {
            return Integer.parseInt(str);
        }
    }

    public static boolean getBooleanParameter(String str, boolean defaultData) {
        if (isEmpty(str)) {
            return defaultData;
        } else {
            return Boolean.parseBoolean(str);
        }
    }


    /**
     * Map 에서 String 가져오기
     *
     * @param map
     * @param key
     * @return
     * @author chkim
     */
    public static String getMapVal(Map<String, ?> map, String key) {
        return getMapVal(map, key, null);
    }

    /**
     * Map 에서 String 가져오기
     *
     * @param map
     * @param key
     * @param
     * @return defaultValue
     * @author chkim
     */
    public static String getMapVal(Map<String, ?> map, String key, String defaultValue) {
        try {
            Object obj = map.get(key);
            if (obj == null) {
                return defaultValue;
            }
            return String.valueOf(obj);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Map 에서 int 가져오기
     *
     * @param map
     * @param key
     * @return
     * @author chkim
     */
    public static int getMapValInt(Map<String, Object> map, String key) {
        return getMapValInt(map, key, -1);
    }

    /**
     * Map 에서 int 가져오기
     *
     * @param map
     * @param key
     * @return defaultValue
     * @return
     * @author chkim
     */
    public static int getMapValInt(Map<String, Object> map, String key, int defaultValue) {
        try {
            return Integer.parseInt(getMapVal(map, key, String.valueOf(defaultValue)));
        } catch (Exception e) {
            return defaultValue;
        }
    }


    public static List<String> getContentSrcRelativeList(String str) {
        List<String> relativeList = new ArrayList<String>();
        try {
            String pattenrStr = "<[img|source|td][^>]*[src|url][=|\\(][\"']?([^>\"']+)[\"']?[^>]*>";
            Pattern nonValidPattern = Pattern.compile(pattenrStr);
            Matcher matcher = nonValidPattern.matcher(str);
            while (matcher.find()) {
                String tempStr = matcher.group(0);
                String tempArr[] = tempStr.split("seq");
                if (tempArr.length > 1) {
                    relativeList.add(tempArr[tempArr.length - 1]);
                }
            }
            return relativeList;
        } catch (Exception e) {
            return relativeList;
        }
    }

    public static Object convertMapToObject(Map<String, Object> map, Object obj) {
        return  convertMapToObject((HashMap<String, Object>) map, obj);
    }
    /**
     * Map을 Vo로 변환
     *
     * @param map
     * @param obj
     * @return
     */
    public static Object convertMapToObject(HashMap<String, Object> map, Object obj) {
        String keyAttribute = null;
        String setMethodString = "set";
        String methodString = null;
        Iterator itr = map.keySet().iterator();

        while (itr.hasNext()) {
            keyAttribute = (String) itr.next();
            methodString = setMethodString + keyAttribute.substring(0, 1).toUpperCase() + keyAttribute.substring(1);
            Method[] methods = obj.getClass().getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methodString.equals(methods[i].getName())) {
                    try {
                        methods[i].invoke(obj, map.get(keyAttribute));
                    } catch (Exception e) {
                        System.out.println("convertMapToObject Exception = " + e.getMessage());
                    }
                }
            }
        }
        return obj;
    }

    public static String convertMapToJsonString(HashMap<String,Object> map) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(map);
        }catch (Exception e) {
            System.out.println("convertMapToJsonString Exception = " + e.getMessage());
            return  "";
        }
    }

    public static String convertObjectToJsonString(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(obj);
        }catch (Exception e) {
            System.out.println("convertMapToJsonString Exception = " + e.getMessage());
            return  "";
        }
    }



    public static BigDecimal calBigDeciaml(BigDecimal price, BigDecimal ratio,  int scale) {
        return price.multiply(ratio).setScale(0, RoundingMode.DOWN);
    }

    public static BigDecimal calBigDeciaml(BigDecimal price, BigDecimal ratio) {
        return  calBigDeciaml(price, ratio, 0);
    }

    // 문자열을 Map으로 변환하는 메서드
    public static Map<String, String> parseToMap(String input) {
        Map<String, String> map = new HashMap<>();

        // "&"로 구분
        String[] pairs = input.split("&");

        for (String pair : pairs) {
            // "="로 구분하여 key와 value를 분리
            String[] keyValue = pair.split("=", 2); // "2"를 추가하여 "="이 포함되지 않을 경우 방지
            if (keyValue.length == 2) {
                // key = keyValue[0], value = keyValue[1]
                map.put(keyValue[0], keyValue[1]);
            } else {
                // "="이 없을 경우 value를 빈 문자열로 처리
                map.put(keyValue[0], "");
            }
        }

        return map;
    }

    public static Map<String, Object> jsonToMap(String result) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        Map<String, Object> returnMap = null;
        try {
            returnMap = mapper.readValue(result, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return returnMap;
    }

    public static <T> T jsonToObject(String result, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        try {
            return  mapper.readValue(result, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T convertRedisObject(Object object, TypeReference<T> typeReference) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

        if (object == null) {
            return null;
        }

        try {
            if (object instanceof String) {
                String jsonString = (String) object;
                // 빈 문자열이거나 null 문자열인 경우 처리
                if (jsonString.isEmpty()) {
                    return null;
                }
                return mapper.readValue(jsonString, typeReference);
            }
            return mapper.convertValue(object, typeReference);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 오류: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("객체 변환 오류: " + e.getMessage(), e);
        }
    }

}