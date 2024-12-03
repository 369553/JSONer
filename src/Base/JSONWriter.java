package Base;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JSONWriter{
    

    public JSONWriter(){
        
    }

//İŞLEM YÖNTEMLERİ:
    public String produceText(String key, Object obj, boolean putNewLineForLook, boolean isFirst){
        // isFirst : Gönderilen değişken ilk eleman ise 'true' olmalı
        boolean isNum = false;// Yazısı üretilmek istenen değişken bir sayı ise 'true' olmalıdır
        boolean isStr = false;// Değişken tipi yazı ise 'true' olmalı
        boolean isBool = false;// Değişken tipi 'boolean' ise 'true' olmalı
        boolean isArr = false;// Değişken tipi dizi ise 'true' olmalı
        boolean isByte = false;// Değişken tipi byte ise 'true' olmalı
        boolean isPri = false;// Temel bir veri tipi ise 'true' olmalı
        boolean isAllSame = false;// Eğer değişken bir dizi ise ve dizi içerisindeki tüm değerlerin tipi aynı ise 'true' olmalı
        boolean isNull = false;// Gelen değişken 'null' ise 'true' olmalı
        StringBuilder sB = new StringBuilder();//stringBuilder, üretilen JSON verisini tutmak için
        int len = -1;// Dizi gönderildiğinde dizi uzunluğunu tutmak için
        Class dTyp;// dataType = veri tipi
        if(putNewLineForLook && isFirst)
            sB.append("\n");
        if(key != null){
            sB.append("\"" + key + "\"");
            if(putNewLineForLook)
                sB.append(" ");
            sB.append(":");
            if(putNewLineForLook)
                sB.append(" ");
        }
        if(obj == null){
            isNull = true;
            sB.append("null");
            return sB.toString();
        }
        dTyp = obj.getClass();
        if(dTyp.isArray()){
            isArr = true;
//            System.err.println("JSONWriter . produceText //Bu değişken bir dizi");
            sB.append("[");
            try{
                dTyp = Array.get(obj, 0).getClass();
                len = Array.getLength(obj);
            }
            catch(ArrayIndexOutOfBoundsException | NullPointerException exc){
                System.err.println("Gönderilen dizide eleman yok");
                sB.append("]");
                return sB.toString();
            }
            for(int sayac = 0; sayac < len; sayac++){
                Object valInArr = Array.get(obj, sayac);
                sB.append(produceText(null, valInArr, putNewLineForLook, false));
                if(sayac < len - 1){
                    sB.append(",");
                if(putNewLineForLook)
                    sB.append(" ");// Burada, sonraki değer nesne ise yeni satır olması gerekebilir
                }
            }
            sB.append("]");
            return sB.toString();
        }
        
        // Önce dizi olmayan verilerin JSON metnini üret;
        // Sonra dizi için özyinelemeli olacak şekilde algoritma tasarla
        else if(dTyp == Integer.class || dTyp == Double.class || dTyp == Float.class || dTyp == Long.class || dTyp == Short.class || dTyp == BigInteger.class){
            isNum = true;
            isPri = true;
        }
        else if(dTyp == String.class || dTyp == Character.class){
            isStr = true;
            isPri = true;
        }
        else if(dTyp == Boolean.class){
            isBool = true;
            isPri = true;
        }
        else if(dTyp == Byte.class){
            // Burası ele alınmalı
            isByte = true;
            isPri = true;
        }
        else if(dTyp == Date.class || dTyp == LocalDateTime.class || dTyp == LocalDate.class || dTyp == LocalTime.class){
            
        }
        if(isPri){// Değişken temel bir veri tipinde ise;
            if(isStr)
                sB.append("\"");
            sB.append(obj);
            if(isStr)
                sB.append("\"");
            return sB.toString();
        }
        else{// Değişken bir nesne ise;
            sB.append("{");
            // Değerler alınmalı:
            {// Burası ayrı bir fonksiyonda yazılabilir, üzerinde düşün!
                Field[] fS = obj.getClass().getDeclaredFields();// fields, alanlar
                boolean isFirstObj = true;
                boolean lastFieldIsUnavailable = false;// Kendinden bir önceki alana erişilebilip, erişilemediğini öğrenmek için bir değişken; 4. sorunun çözümü için...
                boolean isFirstFieldCanAccess = false;// Erişilebilir ilk alanın sıra numarası sıfırdan farklı olduğu durumda, açılış parantezinden sonra virgül konması sorununu () çözmek için...
                int sayac = 0;
                for(Field val : fS){
                    try{
                        Object valOfField = val.get(obj);
                        if(lastFieldIsUnavailable && isFirstFieldCanAccess)
                            sB.append(",");
                        if(lastFieldIsUnavailable && sayac == fS.length - 1 && putNewLineForLook)
                            sB.append("\n");
                        sB.append(produceText(val.getName(), valOfField, putNewLineForLook, isFirstObj));
                        lastFieldIsUnavailable = false;
                        isFirstFieldCanAccess = true;
                    }
                    catch(IllegalArgumentException | IllegalAccessException exc){
                        // Hatâ olduğunu belirtmek için bir kayıt defterine not düşülebilir
                        if(sayac > 0){// Döngünün ilk adımı değilse
                            int currLen = sB.length();
                         int deleteNumber = 1;
                            if(sB.charAt(currLen - 1) == '\n'){// Son karakter olarak yeni satır konduysa
                                if(sB.charAt(currLen - 2) == ',')// Sondan bir önceki karakter olarak virgül konduysa
                                    deleteNumber++;
                                sB.delete(currLen - deleteNumber, currLen);
                            }
                            else if(sB.charAt(currLen - 1) == ',')
                                sB.delete(currLen - deleteNumber, currLen);
                        }
                        lastFieldIsUnavailable = true;
                        sayac++;
                        continue;
                    }
                    if(sayac == 0)
                        isFirstObj = false;
                    if(sayac < fS.length - 1){
                        sB.append(",");
                        if(putNewLineForLook){
                            if(isBasicDataType(val))
                                sB.append(" ");
                            else
                                sB.append("\n");
                        }
                    }
                    sayac++;
                }
            }
            if(putNewLineForLook)
                sB.append("\n");
            sB.append("}");
            return sB.toString();
        }
    }
    public String produceText(String key, Object obj){
        return produceText(key, obj, true, false);
    }
    public String produceTextFromMap(Map<String, Object> map, boolean putNewLineForSeeming, String nameOfVariableOnTheMap){
        int sayac = -1;
        boolean isFirst = true;
        StringBuilder buiText = new StringBuilder();
        if(nameOfVariableOnTheMap != null)
            buiText.append(nameOfVariableOnTheMap).append(":");
        if(putNewLineForSeeming)
            buiText.append("\n");
        buiText.append("{");
        for(String key : map.keySet()){
            String value = "";
            sayac++;
            if(sayac == 1)
                isFirst = false;
            if(map.get(key).getClass() == HashMap.class)
                value = produceTextFromMap((HashMap<String, Object>) map.get(key), putNewLineForSeeming, key);
            else
                value = produceText(key, map.get(key), putNewLineForSeeming, isFirst);
            buiText.append(value).append(",").append("\n");
        }
        buiText.deleteCharAt(buiText.length() - 2);
        if(putNewLineForSeeming)
            buiText.append("\n");
        buiText.append("}");
        return buiText.toString();
    }
    //ARKAPLAN İŞLEM YÖNTEMLERİ:
    private boolean isBasicDataType(Object val){
        Class dTyp = val.getClass();
        if(dTyp == Integer.class || dTyp == Double.class || dTyp == Float.class ||
                dTyp == Long.class || dTyp == Short.class || dTyp == String.class ||
                dTyp == Character.class || dTyp == Boolean.class || dTyp == Byte.class)
            return true;
        return false;
    }

//ERİŞİM YÖNTEMLERİ:
    
}