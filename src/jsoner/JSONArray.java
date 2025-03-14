package jsoner;

import ReflectorRuntime.Reflector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * JSON dizisini temsil eden sınıftır
 * İçerisinde liste olarak veri barındırabilir
 * @author Mehmed Âkif SOLAK
 */
public class JSONArray implements Iterable<Object>, Cloneable{
    private List<Object> data = new ArrayList<Object>();
    private StringBuilder jsonText = new StringBuilder();
    private boolean isUpdate = false;
    private static JSONWriter jsonWriter = new JSONWriter();

    public JSONArray(){}
    /**
     * Verilen liste ile {@code JSONArray} nesnesi oluşturulur
     * Referans tabanlı çalışır
     * @param data JSON dizisini temsîl eden veri
     */
    public JSONArray(List<? extends Object> data){
        if(data == null)
            throw new IllegalArgumentException("Girilen veri listesi NULL");
        for(Object element : data){
            this.data.add(element);
        }
    }

// İŞLEM YÖNTEMLERİ:
    /**
     * JSONArray referans tabanlı olduğu için yeni bir nesne döndüren
     * {@code clone} yöntemini destekliyor
     * Yeni bir liste oluşturulur; nesneler doğrudan eklenir;
     * Yanî "Shallow copy" modunda kopyalama yapılır
     * @return Yeni {@code JSONArray} nesnesi
     */
    @Override
    public Object clone(){
        List cloned = new ArrayList<Object>();
        for(Object obj : data){
            cloned.add(obj);
        }
        return new JSONArray(cloned);
    }
    /**
     * Listenin en sonuna metîn ekleme işlemi yapılır
     * @param value Eklenmek istenen metîn
     */
    public void addString(String value){
        if((value != null && value instanceof String) || value == null){
            add(value);
            isUpdate = false;
        }
    }
    /**
     * Listenin en sonuna sayı ekleme işlemi yapılır
     * @param <T> Sayının tipi, {@code Number}'ın alt sınıfı olmalıdır
     * @param value Eklenmek istenen sayı
     */
    public <T extends Number> void addNumber(T value){
        if((value != null && value instanceof Number) || value == null){
            add(value);
            isUpdate = false;
        }
    }
    /**
     * Listenin en sonuna {@code boolean} verisi ekler
     * @param value Eklenmek istenen {@code boolean} verisi
     */
    public void addBoolean(Boolean value){
        if((value != null && value instanceof Boolean) || value == null){
            add(value);
            isUpdate = false;
        }
    }
    /**
     * Listenin en sonuna {@code JSONObject} nesnesi ekler
     * Verilen {@code JSONObject} değişkeni referans tabanlı eklenir
     * @param value Eklenmek istenen {@code JSONObject} tipindeki nesne
     */
    public void addJSONObject(JSONObject value){
        if(value != null && value instanceof JSONObject){
            add((JSONObject)value);
            isUpdate = false;
        }
        else if(value == null){
            add(value);
            isUpdate = false;
        }
    }
    /**
     * Listenin en sonuna {@code JSONArray} verisi ekler
     * Verilen {@code JSONArray} değişkeni referans tabanlı eklenir
     * @param value Eklenmek istenen {@code JSONArray} tipindeki nesne
     */
    public void addJSONArray(JSONArray value){
        if(value != null && value instanceof JSONArray){
            if(value != this)
                add((JSONArray)value);
            isUpdate = false;
        }
        else if(value == null){
            add(value);
            isUpdate = false;
        }
    }
    /**
     * İlgili {@code JSONObject} nesnesi hedef sınıf örneğine dönüştürülür
     * İlgili sıradaki elemanın {@code JSONObject} tipinde olması gerekir
     * {@code JSONObject} {@code getThisObjectAsTargetType} yöntemi çalıştırılır
     * {@see JSONObject.getThisObjectAsTargetType}
     * @param <T> Hedef sınıfın tipi
     * @param targetClass Hedef sınıf
     * @param index Verinin listedeki indisi, indis sıfırdan başlar
     * @param codeStyle Hedef sınıftaki "setter" yöntemi isminin kod biçimi
     * @return Hedef sınıfın yeni bir örneği veyâ {@code null}
     */
    public <T> T getElementAsTargetType(Class<T> targetClass, int index, Reflector.CODING_STYLE codeStyle){
        if(targetClass == null)
            return null;
        if(index < 0 || index >= data.size())
            return null;
        Object obj = this.data.get(index);
        if(!obj.getClass().equals(JSONObject.class))
            return null;
        return ((JSONObject) obj).getThisObjectAsTargetType(targetClass, codeStyle);
    }
    /**
     * Verilen indisteki eleman listeden kaldırılır.
     * @param index İndis (sıra numarası); sıfırdan başlar
     */
    public void removeElementOnIndex(int index){
        if(index < 0 || index >= data.size())
            return;
        int numberOfElements = this.data.size();
        this.data.remove(index);
        if(numberOfElements != this.data.size())
            this.isUpdate = false;
    }
    //ARKAPLAN İŞLEM YÖNTEMLERİ:
    private <T> T getObject(Class<T> targetClass, int index){
        if(index < 0 || index >= data.size())
            return null;//throw new IllegalArgumentException("Yanlış sıra numarası girildi.");
        Object value = data.get(index);
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
    private void add(Object name){
        this.data.add(name);
    }
    //ARKAPLAN İŞLEM YÖNTEMLERİ:
    private void produceJSONText(){
        this.jsonText.delete(0, this.jsonText.length());
        this.jsonText.append(jsonWriter.produceText(null, this.data));
        isUpdate = true;
    }

// ERİŞİM YÖNTEMLERİ:
    /**
     * {@code JSONArray} verisini döndürür
     * @return {@code List} biçimindeki {@code JSONArray} verisi
     */
    protected List<Object> getData(){
        return this.data;
    }
    /**
     * Bu JSON dizisinin karşılığı olan JSON metni üretilir - döndürülür
     * @return JSON metni
     */
    public String getJSONText() {
        if(!isUpdate)
            produceJSONText();
        return jsonText.toString();
    }
    /**
     * @return Liste uzunluğu
     */
    public int getSize(){
        return this.data.size();
    }
    /**
     * Verilen indisteki metîn verisi döndürülür
     * @param index İndis (sıra numarası); sıfırdan başlar
     * @return Verilen sıradaki metîn verisi veyâ {@code null}
     */
    public String getString(int index){
        return getObject(String.class, index);
    }
    /**
     * Verilen indisteki {@code boolean} verisi döndürülür
     * @param index İndis (sıra numarası); sıfırdan başlar
     * @return Verilen sıradaki {@code boolean} verisi veyâ {@code null}
     */
    public boolean getBoolean(int index){
        return getObject(Boolean.class, index);
    }
    /**
     * Verilen indisteki tamsayı verisi döndürülür
     * @param index İndis (sıra numarası); sıfırdan başlar
     * @return Verilen sıradaki {@code int} verisi veyâ {@code null}
     */
    public int getInt(int index){
        return getObject(Integer.class, index);
    }
    /**
     * Verilen indisteki {@code double} verisi döndürülür
     * @param index İndis (sıra numarası); sıfırdan başlar
     * @return Verilen sıradaki {@code double} verisi veyâ {@code null}
     */
    public double getDouble(int index){
        return getObject(Double.class, index);
    }
    /**
     * Verilen indisteki {@code float} verisi döndürülür
     * @param index İndis (sıra numarası); sıfırdan başlar
     * @return Verilen sıradaki {@code float} verisi veyâ {@code null}
     */
    public float getFloat(int index){
        return getObject(Float.class, index);
    }
    /**
     * Verilen indisteki {@code long} verisi döndürülür
     * @param index İndis (sıra numarası); sıfırdan başlar
     * @return Verilen sıradaki {@code long} verisi veyâ {@code null}
     */
    public long getLong(int index){
        return getObject(Long.class, index);
    }
    /**
     * Verilen indisteki {@code short} verisi döndürülür
     * @param index İndis (sıra numarası); sıfırdan başlar
     * @return Verilen sıradaki {@code short} verisi veyâ {@code null}
     */
    public short getShort(int index){
        return getObject(Short.class, index);
    }
    /**
     * Verilen indisteki {@code char} verisi döndürülür
     * @param index İndis (sıra numarası); sıfırdan başlar
     * @return Verilen sıradaki {@code char} verisi veyâ {@code null}
     */
    public char getChar(int index){
        return getObject(Character.class, index);
    }
    /**
     * Verilen indisteki veri döndürülür
     * @param index İndis (sıra numarası); sıfırdan başlar
     * @return Verilen sıradaki eleman veyâ {@code null}
     */
    public Object getAsObject(int index){
        return getObject(Object.class, index);
    }
    /**
     * Verilen indisteki {@code JSONObject} verisi döndürülür
     * @param index İndis (sıra numarası); sıfırdan başlar
     * @return Verilen sıradaki {@code JSONObject} verisi veyâ {@code null}
     * @throws IllegalArgumentException Verinin {@code Map} olması durumunda,
     * verideki anahtarlar {@code String} tipinde değilse, bu hatâ fırlatılır
     */
    public JSONObject getJSONObject(int index) throws IllegalArgumentException{
        Object val = data.get(index);
        if(val == null)
            return null;
        if(val instanceof Map){
            for(Object key : ((Map) val).keySet()){
                if(!(key instanceof String))
                    throw new IllegalArgumentException("Veri anahtarındaki anahtar değerlerinin metîn olması gerekiyor");
            }
            return new JSONObject((Map) val);
        }
        else if(val instanceof JSONObject)
            return (JSONObject) val;
        return null;
    }
    /**
     * Verilen indisteki {@code JSONArray} verisi döndürülür
     * @param index İndis (sıra numarası); sıfırdan başlar
     * @return Verilen sıradaki {@code JSONArray} verisi veyâ {@code null}
     */
    public JSONArray getJSONArray(int index){
        Object val = data.get(index);
        if(val != null){
            if(val instanceof List)
                return new JSONArray((List)val);
            if(val instanceof JSONArray)
                return (JSONArray) val;
        }
        return null;
    }
    /**
     * Verileri sırayla dolaşabilmek için liste {@code Iterator} nesnesi
     * @return {@code Iterator} nesnesi
     */
    @Override
    public Iterator<Object> iterator(){
        return data.iterator();
    }
}