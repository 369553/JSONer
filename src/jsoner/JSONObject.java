package jsoner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ReflectorRuntime.Reflector;
/**
 * JSON nesnesini temsil eden sınıftır
 * İçerisinde anahtar - değer ikilisi olarak veri barındırabilir
 * @author Mehmed Âkif SOLAK
 */
public class JSONObject implements Cloneable{
    private Map<String, Object> data = new HashMap<String, Object>();// JSON verisindeki değişkenler
    private StringBuilder jsonText = new StringBuilder();// JSON metni
    private boolean isUpdate = false;
    private static JSONWriter jsonWriter = new JSONWriter();

    public JSONObject(){}
    /**
     * JSON nesnesi oluştururken temel veri tipleri dışındaki verileri
     * {@code JSONObject} tipinde, liste tipindeki verileri {@code JSONArray}
     * tipinde verebilirsiniz
     * @param data {@code JSONObject} nesnesi verileri
     */
    public JSONObject(Map<String, ? extends Object> data){
        if(data == null)
            throw new IllegalArgumentException("Girilen veri haritası NULL");
        for(String key : data.keySet()){
            this.data.put(key, data.get(key));
        }
    }

//İŞLEM YÖNTEMLERİ:
    /**
     * JSON nesnesine metîn verisi eklemek için kullanılır
     * @param key Veri erişim anahtarı, {@code null} olamaz
     * @param value metîn verisi
     */
    public void addString(String key, String value){
        if((value != null && value instanceof String) || value == null){
            add(key, value);
            isUpdate = false;
        }
    }
    /**
     * JSON nesnesine sayı eklemek için kullanılır
     * @param <T> Sayının tipi, {@code Number}'ın alt sınıfı olmalıdır
     * @param key Veri erişim anahtarı, {@code null} olamaz
     * @param value Eklenmek istenen sayı
     */
    public <T extends Number> void addNumber(String key, T value){
        if((value != null && value instanceof Number) || value == null){
            add(key, value);
            isUpdate = false;
        }
    }
    /**
     * JSON nesnesine {@code boolean} tipinde veri eklemek için kullanılır
     * @param key Veri erişim anahtarı, {@code null} olamaz
     * @param value {@code boolean} tipinde veri
     */
    public void addBoolean(String key, Boolean value){
        if((value != null && value instanceof Boolean) || value == null){
            add(key, value);
            isUpdate = false;
        }
    }
    /**
     * JSON nesnesine {@code JSONObject} tipinde veri eklemek için kullanılır
     * @param key Veri erişim anahtarı, {@code null} olamaz
     * @param value {@code JSONObject} tipinde veri
     */
    public void addJSONObject(String key, JSONObject value){
        if(value != null && value instanceof JSONObject){
            if(value != this)
                add(key, (JSONObject)value);
            isUpdate = false;
        }
        else if(value == null){
            add(key, value);
            isUpdate = false;
        }
    }
    /**
     * JSON nesnesine {@code JSONArray} tipinde veri eklemek için kullanılır
     * @param key Veri erişim anahtarı, {@code null} olamaz
     * @param value {@code JSONArray} tipinde veri
     */
    public void addJSONArray(String key, JSONArray value){
        if(value != null && value instanceof JSONArray){
            add(key, (JSONArray)value);
            isUpdate = false;
        }
        else if(value == null){
            add(key, value);
            isUpdate = false;
        }
    }
    /**
     * JSONObject referans tabanlı olduğu için yeni bir nesne döndüren
     * {@clone} yöntemini destekliyor
     * Bu kapsamda yeni {@code Map} oluşturulur; nesne olanlar doğrudan eklenir
     * yanî "Shallow copy" modunda kopyalama yapılır
     * @return Yeni {@code JSONObject} nesnesi
     */
    @Override
    public Object clone(){
        Map cloned = new HashMap<String, Object>();
        for(Object key : data.keySet()){
            cloned.put(key, data.get(key));
        }
        return new JSONObject(cloned);
    }
    /**
     * Bu nesneyi başka sınıfın bir örneğini oluşturmak için kullanabilirsiniz
     * Nesne içerisindeki özellikler hedef sınıfın özellikleriyle aynı olmalıdır
     * Eksik özellik, doğrudan erişim yöntemi bulunmaması sorun değildir
     * Doğrudan erişim yöntemi yoksa, belirtilen biçimde "setter" yöntemi aranır
     * Hedef sınıfın parametresiz yapıcı yöntemi bulunmalıdır
     * @param <T> Hedef sınıfın tipi
     * @param targetClass Hedef sınıf
     * @param codeStyle Hedef sınıftaki "setter" yöntemi isminin kod biçimi
     * @return Hedef sınıfın yeni bir örneği veyâ {@code null}
     */
    public <T> T getThisObjectAsTargetType(Class<T> targetClass, Reflector.CODING_STYLE codeStyle){
        if(targetClass == null)
            return null;
        if(this.data == null)
            return null;
        return Reflector.getService().pruduceNewInjectedObject(targetClass, data, codeStyle);
    }
    /**
     * Verilen anahtarın işâret ettiği veri kaldırılır
     * @param name Veri erişim anahtarı, {@code null} olamaz
     */
    public void removeObjectByName(String name){
        int numberOfElements = this.data.size();
        this.data.remove(name);
        if(numberOfElements != this.data.size())
            this.isUpdate = false;
    }

//ARKAPLAN İŞLEM YÖNTEMLERİ:
    private void produceJSONText(){
        this.jsonText.delete(0, this.jsonText.length());
        String a = jsonWriter.produceJSONTextFromMap(this.data, false, null);
        if(a == null)
            System.err.println("Başarısız");
        else
            this.jsonText.append(jsonWriter.produceJSONTextFromMap(this.data, false, null));
        isUpdate = true;
    }
    private <T> T getObject(Class<T> targetClass, String name){// Verilen elemanı verilen tipte almak için..
        Object value = data.get(name);
        if(value == null)
            return null;
        T casted = null;
        try{
            casted = targetClass.cast(value);
            return casted;
        }
        catch(ClassCastException exc){
            System.err.println("Veri çevrilemedi : " + exc.toString());// --BU--SATIRI--SİL
            return null;
        }
    }
    private void add(String key, Object name){
        if(key == null)
            return;
        this.data.put(key, name);
    }

//ERİŞİM YÖNTEMLERİ:
    /**
     * {@code JSONObject} verisini döndürür
     * @return {@code Map} biçimindeki {@code JSONObject} verisi
     */
    protected Map<String, Object> getData(){
        return this.data;
    }
    /**
     * {@code JSONObject} nesnesinin JSON metnini üretir - döndürür
     * @return JSON metni
     */
    public String getJSONText(){
        if(!isUpdate)
            produceJSONText();
        return jsonText.toString();
    }
    /**
     * Nesne içerisindeki veri sayısı döndürülür
     * @return veri sayısı
     */
    public int getSize(){
        return this.data.size();
    }
    /**
     * Verilen anahtarın işâret ettiği metîn döndürülür
     * @param name Veri erişim anahtarı, {@code null} olamaz
     * @return İlgili veri veyâ {@code null}
     */
    public String getString(String name){
        return getObject(String.class, name);
    }
    /**
     * Verilen anahtarın işâret ettiği {@code boolean} verisi döndürülür
     * @param name Veri erişim anahtarı, {@code null} olamaz
     * @return İlgili veri veyâ {@code null}
     */
    public boolean getBoolean(String name){
        return getObject(Boolean.class, name);
    }
    /**
     * Verilen anahtarın işâret ettiği tamsayı döndürülür
     * @param name Veri erişim anahtarı, {@code null} olamaz
     * @return İlgili veri veyâ {@code null}
     */
    public int getInt(String name){
        return getObject(Integer.class, name);
    }
    /**
     * Verilen anahtarın işâret ettiği {@code double} verisi döndürülür
     * @param name Veri erişim anahtarı, {@code null} olamaz
     * @return İlgili veri veyâ {@code null}
     */
    public double getDouble(String name){
        return getObject(Double.class, name);
    }
    /**
     * Verilen anahtarın işâret ettiği {@code float} verisi döndürülür
     * @param name Veri erişim anahtarı, {@code null} olamaz
     * @return İlgili veri veyâ {@code null}
     */
    public float getFloat(String name){
        return getObject(Float.class, name);
    }
    /**
     * Verilen anahtarın işâret ettiği {@code long} verisi döndürülür
     * @param name Veri erişim anahtarı, {@code null} olamaz
     * @return İlgili veri veyâ {@code null}
     */
    public long getLong(String name){
        return getObject(Long.class, name);
    }
    /**
     * Verilen anahtarın işâret ettiği {@code short} verisi döndürülür
     * @param name Veri erişim anahtarı, {@code null} olamaz
     * @return İlgili veri veyâ {@code null}
     */
    public short getShort(String name){
        return getObject(Short.class, name);
    }
    /**
     * Verilen anahtarın işâret ettiği {@code char} verisi döndürülür
     * @param name Veri erişim anahtarı, {@code null} olamaz
     * @return İlgili veri veyâ {@code null}
     */
    public char getChar(String name){
        return getObject(Character.class, name);
    }
    /**
     * Verilen anahtarın işâret ettiği veri döndürülür
     * @param name Veri erişim anahtarı, {@code null} olamaz
     * @return İlgili veri veyâ {@code null}
     */
    public Object getAsObject(String name){
        return getObject(Object.class, name);
    }
    /**
     * Verilen anahtarın işâret ettiği {@code JSONObject} verisi döndürülür
     * @param name Veri erişim anahtarı, {@code null} olamaz
     * @return İlgili veri veyâ {@code null}
     * @throws IllegalArgumentException Verinin {@code Map} olması durumunda,
     * verideki anahtarlar {@code String} tipinde değilse, bu hatâ fırlatılır
     */
    public JSONObject getJSONObject(String name){
        Object val = data.get(name);
        if(val == null)
            return null;
        if(val instanceof Map){
            for(Object key : ((Map) val).keySet()){
                if(!(key instanceof String))
                    throw new IllegalArgumentException("Veri anahtarındaki anahtar değerlerinin metîn olması gerekiyor");
            }
            return new JSONObject((Map<String, Object>)val);
        }
        else if(val instanceof JSONObject)
            return (JSONObject) val;
        return null;
    }
    /**
     * Verilen anahtarın işâret ettiği {@code JSONArray} verisi döndürülür
     * @param name Veri erişim anahtarı, {@code null} olamaz
     * @return İlgili veri veyâ {@code null}
     */
    public JSONArray getJSONArray(String name){
        Object val = data.get(name);
        if(val != null){
            if(val instanceof List)
                return new JSONArray((List)val);
            else if(val instanceof JSONArray)
                return (JSONArray) val;
        }
        return null;
    }
}