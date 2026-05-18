package com.jkproject.JkProject.util;


import jakarta.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.RedirectView;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.Timestamp;
import java.util.*;

public class Utils {


    static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    public static Timestamp getNow() {
        return DateUtil.getNow();
    }

    public static Object[] concatenateWithDuplicate(Object[] sourceObjects, Object[] targetObjects) {
        if ((sourceObjects != null) && (targetObjects == null)) {
            return sourceObjects;
        }

        if ((sourceObjects == null) && (targetObjects != null)) {
            return targetObjects;
        }

        if ((sourceObjects == null) && (targetObjects == null)) {
            return null;
        }

        int sourceSize = sourceObjects.length;
        int targetSize = targetObjects.length;

        // Arrray 영역을 새로 잡는다.
        Object[] arrReturn = (Object[]) java.lang.reflect.Array.newInstance(sourceObjects.getClass().getComponentType(), sourceSize + targetSize);

        // 첫번째 Array 복사
        System.arraycopy(sourceObjects, 0, arrReturn, 0, sourceSize);

        // 그 뒤를 이어 두번째 Array 복사
        System.arraycopy(targetObjects, 0, arrReturn, sourceSize, targetSize);

        return arrReturn;
    }


    /**
     * Boolean 값을 int로 변환해 주는 함수이다.
     *
     * @param value int로 변환하고 싶은 booelan 값
     * @return value가 true이면, 1, false면 0
     */
    public static int getIntValue(boolean value) {
        return value ? 1 : 0;
    }

    public static String toString(Throwable t) {
        CharArrayWriter cw = new CharArrayWriter();
        PrintWriter pw = new PrintWriter(cw);
        t.printStackTrace(pw);
        return cw.toString();
    }

    private Utils() {
    }

    /**
     * 두개의 Object 배열을 합친다. merge :
     *
     * @param originalObjects
     * @param additionalObjects
     * @return
     */
    public static Object[] merge(Object[] originalObjects, Object[] additionalObjects) {
        if (ObjectUtil.isEmpty(originalObjects)) {
            if (ObjectUtil.isEmpty(additionalObjects)) {
                return null;
            } else {
                return additionalObjects;
            }
        } else if (ObjectUtil.isEmpty(additionalObjects)) {
            return originalObjects;
        }

        int nCount = originalObjects.length + additionalObjects.length;
        Object[] returnObjects = (Object[]) java.lang.reflect.Array.newInstance(originalObjects[0].getClass(), nCount);
        for (int i = 0; i < nCount; i++) {
            if (i < originalObjects.length) {
                returnObjects[i] = originalObjects[i];
            } else {
                returnObjects[i] = additionalObjects[i - originalObjects.length];
            }
        }

        return returnObjects;
    }

    /**
     * Bit AND연산에 대한 결과값을 리턴한다.
     *
     * @param source : 비교할 값
     * @param target : 비교대상
     * @return
     */
    public static boolean equalsBitAnd(long source, long target) {
        return (source & target) == target;
    }

    public static Object[] addObjectToArray(Object[] source, Object target) {
        if ((source != null) && (target == null)) {
            return source;
        }

        if ((source == null) && (target != null)) {
            return new Object[]{target};
        }

        if ((source == null) && (target == null)) {
            return null;
        }

        source = concatenateWithDuplicate(source, new Object[]{target});

        return source;
    }

    public static <T> void addArrayToCollection(Collection<T> collections, T[] objects) {
        if (ObjectUtil.isEmpty(objects)) {
            return;
        }

        for (int i = 0; i < objects.length; i++) {
            collections.add(objects[i]);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] getArray(T object, int size) {
        if (size > 0 && object != null) {
            return (T[]) Array.newInstance(object.getClass(), size);
        }

        return null;
    }

    public static <T> List<T> toList(T[] objects) {
        if (ObjectUtil.isNotEmpty(objects)) {
            return Arrays.asList(objects);
        }

        return null;
    }

    public static <T> List<T> toArrayList(T[] objects) {
        List<T> list = new ArrayList<T>();
        if (ObjectUtil.isNotEmpty(objects)) {
            for (int i = 0, m = objects.length; i < m; i++) {
                list.add(objects[i]);
            }
            return list;
        }
        return null;
    }

    public static <T> List<T> toList(Map<String, T> map) {
        List<T> list = new ArrayList<T>();
        String[] keys = null;

        if (ObjectUtil.isNotEmpty(map)) {
            keys = map.keySet().toArray(new String[]{});
            for (int i = 0, m = keys.length; i < m; i++) {
                list.add(map.get(keys[i]));
            }
        }

        return list;
    }

    private static class ReverseComparator<T> implements Comparator<T>, Serializable {
        private static final long serialVersionUID = 1L;

        private Comparator<T> cmp = null;

        public ReverseComparator(Comparator<T> cmp) {
            this.cmp = cmp;
        }

        public int compare(T o1, T o2) {
            return cmp.compare(o2, o1);
        }
    }

    public static <T> Comparator<T> reverseOrder(Comparator<T> comparator) {
        return new ReverseComparator<T>(comparator);
    }

    public static String getFloatRoundString(float f, int decimalPoint) {
        int n = 1;

        if (f >= 100 && decimalPoint > 0) {
            decimalPoint = 0;
        }

        for (int i = 0; i < decimalPoint; i++) {
            n *= 10;
        }

        return decimalPoint > 0 ? String.valueOf(((float) Math.round(f * n) / n)) : String.valueOf(Math.round(f));
    }

    public static int getIntegerValue(Object object) {
        if (object instanceof BigDecimal) {
            return ((BigDecimal) object).intValue();
        } else if (object instanceof Integer) {
            return ((Integer) object).intValue();
        } else if (object instanceof Long) {
            return ((Long) object).intValue();
        } else {
            throw new RuntimeException("Not Expected Object Type [" + object.getClass().getName() + "]");
        }
    }

    public static <T> List<T>[] getSplitList(List<T> allList, int maxSize) {
        List<List<T>> result = new ArrayList<List<T>>();
        List<T> subList = null;

        if (allList == null || allList.size() == 0 || maxSize == 0) {
            result.add(new ArrayList<T>());
        } else if (maxSize < 0 || allList.size() < maxSize) {
            result.add(allList);
        } else {
            int numBatches = (allList.size() / maxSize) + 1;
            for (int index = 0; index < numBatches; index++) {
                int count = index + 1;
                int fromIndex = Math.max(((count - 1) * maxSize), 0);
                int toIndex = Math.min((count * maxSize), allList.size());

                subList = allList.subList(fromIndex, toIndex);
                if (subList != null && subList.size() > 0) {
                    result.add(subList);
                }
            }
        }

        return Utils.toArray(result);
    }

    public static String[][] getSplitArray(String[] source, int size) {
        String[][] result = null;
        String[] subArray = null;
        int index = 0;
        int resultSize = 0;
        int remainder = 0;
        if (ObjectUtil.isNotEmpty(source)) {
            remainder = source.length % size;
            resultSize = source.length / size + (remainder > 0 ? 1 : 0);
            result = new String[resultSize][];
            for (int i = 0; i < resultSize; i++) {
                if (remainder > 0 && (resultSize - 1) == i) {
                    subArray = new String[remainder];
                } else {
                    subArray = new String[size];
                }
                for (int j = 0, n = subArray.length; j < n; j++) {
                    subArray[j] = source[index];
                    index++;
                }
                result[i] = subArray;
            }
        }
        return result;
    }

    /**
     * findElementIndexInArray
     *
     * @param objects
     * @param element
     * @return index
     */
    public static int findElementIndexInArray(Object[] objects, Object element) {
        if (ObjectUtil.isNotEmpty(objects) && ObjectUtil.isNotEmpty(element)) {
            for (int i = 0, m = objects.length; i < m; i++) {
                if (objects[i] != null && objects[i].equals(element)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static <T> T[] toArray(T[] objects) {
        T[] result = null;

        if ((objects != null) && (objects.length > 0)) {
            int nSize = objects.length;
            result = getNewArrayInstance(objects[0], nSize);
            System.arraycopy(objects, 0, result, 0, nSize);
        }
        return result;
    }

    public static <T> T[] toArray(Collection<T> collection) {
        return toArray(collection, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Collection<T> collection, Class<T> targetClass) {
        T[] result = null;

        if (ObjectUtil.isNotEmpty(collection)) {
            Iterator<T> itr = collection.iterator();
            if (ObjectUtil.isEmpty(targetClass)) {
                targetClass = (Class<T>) itr.next().getClass();
            }
            result = getNewArrayInstance(targetClass, collection.size());
            collection.toArray(result);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] getNewArrayInstance(Class<T> sourceClass, int size) {
        if (size > 0) {
            return (T[]) Array.newInstance(sourceClass, size);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] getNewArrayInstance(T object, int size) {
        if (size > 0 && ObjectUtil.isNotEmpty(object)) {
            return (T[]) Array.newInstance(object.getClass(), size);
        }

        return null;
    }

    public static String lpad(String source, int length, String additional) {
        String result = source;

        if (ObjectUtil.isEmpty(result)) {
            result = "";
        }

        int templength = length - result.length();

        for (int i = 0; i < templength; i++) {
            result = additional + result;
        }
        return result;
    }


    public static String encodeAES128ByElectronic(String str, String aes128Key) throws Exception {
        byte[] keyData = aes128Key.getBytes();
        SecretKey secureKey = new SecretKeySpec(keyData, "AES");
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(aes128Key.getBytes()));
        byte[] encrypted = c.doFinal(str.getBytes(StandardCharsets.UTF_8));
        String enStr = new String(Base64.getEncoder().encode(encrypted));
        return enStr;
    }

    public static String AES128encrypt(String message, String keyString) throws Exception {
        byte[] keyBytes = new byte[16];
        int len = keyString.getBytes(StandardCharsets.UTF_8).length;

        if (len > keyBytes.length) len = keyBytes.length;
        System.arraycopy(keyString.getBytes(StandardCharsets.UTF_8), 0, keyBytes, 0, len);

        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] results = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return org.apache.commons.codec.binary.Base64.encodeBase64String(results);
    }

    public static String AES128URLDecrypt(String  message, String keyString) throws Exception {
        // 1. URL 디코딩
        String base64Encoded = URLDecoder.decode(message, StandardCharsets.UTF_8);
        // 2. Base64 디코딩
        byte[] encryptedBytes = Base64.getDecoder().decode(base64Encoded);

        Key key = new SecretKeySpec(keyString.getBytes(StandardCharsets.UTF_8), "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] result = cipher.doFinal(encryptedBytes);
        return new String(result, StandardCharsets.UTF_8);
    }


    public static Map<String, String> convertStringMap(Map<String, Object> param) {
        Map<String, String> convertMap = new HashMap<>();
        Iterator<String> keys = param.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            convertMap.put(key, String.valueOf(param.get(key)));
        }
        return convertMap;
    }


    public static HashMap<String, String> OKMapValue() {
        HashMap<String, String> returnOk = new HashMap<>();
        returnOk.put("isOK", "OK");
        return returnOk;
    }

    public static  String getCookie(Cookie[] cookies, String name) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return "";
    }

    public static RedirectView RedirectViewError(String active, long storeSeq, long storePriceSeq, int code, String error) {
        String domain = "";

        if (ObjectUtil.isEqualText(active, "local")) {
            domain = "http://localhost:3000/";
        }else if (ObjectUtil.isEqualText(active, "dev")) {
            domain = "https://dev.signalreport.co.kr/";
        }else {
            domain = "https://main.signalreport.co.kr/";
        }

        LOGGER.info("error Code {} || error {} ", code, error);

        String errorMsg;
        errorMsg = URLEncoder.encode(error, StandardCharsets.UTF_8);
        String urlCode = String.format("%sv-subScribeResult?code=fail&errorCode=%d&message=%s&storeSeq=%d&priceSeq=%d", domain, code, errorMsg, storeSeq, storePriceSeq);

        return new RedirectView(urlCode);
    }

    public static RedirectView RedirectViewMessage(String active, String message) {
        String domain = "";

        if (ObjectUtil.isEqualText(active, "local")) {
            domain = "http://localhost:3000/";
        }else if (ObjectUtil.isEqualText(active, "dev")) {
            domain = "https://dev.signalreport.co.kr/";
        }else {
            domain = "https://main.signalreport.co.kr/";
        }

        String urlCode = String.format("%sv-subScribeResult?%s", domain, message);

        return new RedirectView(urlCode);
    }

    public static String makeSignature(String method, String url, String timestamp, String accessKey, String secretKey) throws Exception {
        String space = " ";                    // 공백
        String newLine = "\n";                // 줄바꿈

        String message = method +
                space +
                url +
                newLine +
                timestamp +
                newLine +
                accessKey;

        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
        return org.apache.commons.codec.binary.Base64.encodeBase64String(rawHmac);
    }

    public static String getCouponProductId(long storeSeq, long priceSeq) {
        return String.format("SICL-%d%d", storeSeq, priceSeq);
    }
}
