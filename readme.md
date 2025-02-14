# JSONer Kullanım Kılavuzu (v2.0.1)

## ERİŞİM

- JSON metînlerinden nesnelerin - dizilerin inşâ edilmesini sağlayan `JSONReader` sınıfının örneğine `JSONReader.getService()` statik yöntemi üzerinden erişebilirsiniz.

- Java nesnelerinden - dizilerinden JSON metni üretilmesini sağlayan JSONWriter sınıfının örneğini `new JSONWriter()` kodu ile oluşturabilir, bunu kullanabilirsiniz.

## JSON METNİNİ OKUMA:

- JSON metnîni girdi olarak vererek JSON metnindeki değişkenlerin Java değişkenine dönüştürülmüş hâlini elde edebilirsiniz, yâ dâ bu verilerle `JSONObject` nesnesi oluşturarak JSON içerisindeki değişkenlere daha kolay erişebilirsiniz.

#### 1) JSON Nesnesinden Oluşturulan Java Değişkenlerini "Map" Tipinde Alma:

- Bunun için `JSONReader` nesnesinin `readJSONObject(String text)` yöntemini kullanabilirsiniz.

- Misal, aşağıdaki JSON metnini okumak istersek;
  
  ```json
  {
      "name": "Mehmed Âkif",
      "id" : 17,
      "notes" : [89, 94, 84],
      "gender" : "MALE",
      "lessons": ["Matematik", "Fizik"]
  }
  ```

- Şu kodları yazabiliriz:
  
  ```java
  String jsonText = readFile("userData.json");// Yardımcı fonksiyon
  Map<String, Object> data = JSONReader.getService().
                             readJSONObject(jsonText);// Veriler okunur
  // Verileri yazdırmak için:
  data.forEach((key, value) ->
                          {System.out.println(key + " : " + value);});
  // Çıktı:
  // notes : [89, 94, 84]
  // gender : MALE
  // name : Mehmed Âkif
  // id : 17
  // lessons : [Matematik, Fizik]
  ```

#### 2) JSON Nesnesine "JSONObject" ile Daha Kolay Erişin

- `JSONObject` sınıfı bilhassa birden fazla JSON nesnesinin - dizisinin iç içe olduğu durumlarda veriye daha kolay erişilmesini sağlamak amacıyla yazılmıştır.

- Sınıf, bir JSON nesnesini simgeler. Sınıfın `getString(String name)` , `getInt(String name)` vb. yöntemleri kullanılarak nesnenin özellikleri Java değişkeninin veri tipine çevrilmiş şekilde getirilir.

- Dilerseniz elinizdeki bir `Map<String, Object>` tipindeki veriyle, dilerseniz de `JSONReader` örneğinin `readJSONObjectReturnJSONObject(String text)` yöntemiyle `JSONObject` nesnesi oluşturabilirsiniz:
  
  ```java
  JSONObject jobjUser = JSONReader.getService().
                      readJSONObjectReturnJSONObject(jsonText);
  System.out.println("Kullanıcının ismi : " +
                                          jobjUser.getString("name"));
  // Çıktı:
  // Kullanıcının ismi : Mehmed Âkif
  ```

- Eğer JSON nesnenizin bir özelliği dizi ise, bu durumda ilgili özelliği JSON dizisini simgeleyen bir `JSONArray` tipinde alabilirsiniz:
  
  ```java
  JSONArray jarrNotes = jobjUser.getJSONArray("notes");
  System.out.println(jarrNotes.getSize() + " adet not bilgisi bulundu");
  // Çıktı:
  // 3 adet not bilgisi bulundu.
  ```

#### 3) JSON Dizilerini Okuma

- Bunun için `JSONReader` nesnesinin `readJSONArray(String text)` yöntemini kullanabilirsiniz.

- Misal, aşağıdaki gibi bir JSON metnimizin olduğunu düşünelim:
  
  ```json
  [
      {
          "id":1,
          "categoryId":1,
          "quantityPerUnit":"5000ml",
          "productName":"Ayçiçek yağı",
          "unitPrice":220,
          "UnitsInStock":23
      },
      {
          "id":2,
          "categoryId":1,
          "quantityPerUnit":"2000ml",
          "productName":"Zeytinyağı",
          "unitPrice":420,
          "UnitsInStock":112
      },
  ]
  ```

- Yukarıdaki JSON dizisini okumak için şu kodu yazabiliriz:
  
  ```java
  String text = readFile("db-products.json");// Yardımcı fonksiyon
  List data = JSONReader.getService().readJSONArray(text);// Veri okunur
  data.forEach(System.out::println);// Veri yazdırılır
  
  // Çıktı:
  // {unitPrice=220, id=1, quantityPerUnit=5000ml, categoryId=1,
  // productName=Ayçiçek yağı, UnitsInStock=23}
  // {unitPrice=420, id=2, quantityPerUnit=2000ml, categoryId=1,
  // productName=Zeytinyağı, UnitsInStock=112}
  ```

#### 4) JSON Dizilerine "JSONArray" ile Daha Kolay Erişin

- JSON dizisi içerisinde pek çok dizi ve nesne bulunabilir. Bunlara kolay erişebilmek için `JSONArray` sınıfını kullanabilirsiniz. Okunan verileri bu sınıfın örneğini oluşturmak için kullanabileceğiniz gibi, verileri doğrudan bu sınıf tipinde de alabilirsiniz:
  
  ```java
  JSONArray jarrProducts = new JSONArray(data);// Yöntem - 1
  jarrProducts = JSONReader.getService().
                      readJSONArrayReturnJSONArray(text);// Yöntem - 2
  System.out.println("Ürün sayısı : " + jarrProducts.getSize());
  
  // Çıktı:
  // Ürün sayısı : 2
  ```

- Veriyi `JSONArray` içerisinden ilgili veri tipinde almak için `getString(int index)`, `getDouble(int index)` gibi yöntemleri kullanabilirsiniz.

- Eğer bir elemanı bir başka sınıf tipinde bir nesne olarak almak istiyorsanız
  `getElementAsTargetType(Class<T> targetClass, int index, Reflector.CODING_STYLE codeStyle)` yöntemini kullanabilirsiniz.

- JSON verisi içerisindeki diziyi `JSONObject` veyâ `JSONArray` olarak da alabilirsiniz:
  
  ```java
  System.out.println("İlk ürünün ismi : " +
      jarrProducts.getJSONObject(0).getString("productName"));
  
  // Çıktı:
  // İlk ürünün ismi : Ayçiçek yağı
  ```

- ..

> ***NOT :*** JSON iki temel biçim içermektedir, nesne ve dizi. JSON dosyanızın kök biçiminin nesne mi, dizi mi olduğunu bilmiyorsanız `JSONReader` sınıfı içerisinde `isRootAnArray(String jsonText)` ve `isRootAnObject(String text)` yöntemlerini kullanarak JSON metninizin kökünün dizi ve nesne olup, olmadığını öğrenebilirsiniz. Detay için sınıf içerisindeki kılavuz notlarına (doc) bakınız.

## JSON METNİ OLUŞTURMA

- JSON metni oluşturmak için `JSONWriter` sınıfının bir örneğini oluşturun:
  
  ```java
  JSONWriter wrt = new JSONWriter();
  ```

- `JSONWriter` sınıfının `produceText(String key, Object obj)` sınıfını kullanarak ilgili veriyi tanımlayan JSON metnini oluşturabilirsiniz. Bu fonksiyona `JSONObject`, `JSONArray`, `Map<String, Object>` ve `List<Object>` tipinde veriler verebildiğiniz gibi **herhangi bir kullanıcı tanımlı sınıfın örneğini de verebilirsiniz**:
  
  ```java
  String jsonText = wrt.produceText(null, jobjUser);// Yöntem - 1
  wrt.produceText(null, injectedUser);// Yöntem - 2
  // injectedUser nesnesinin 'User' tipinde olduğunu düşünürsek..
  Map<String, Object> data = JSONReader.getService().
                  readJSONObject(text);
  jsonText = wrt.produceText(null, data);// Yöntem - 3
  System.out.println(jsonText);
  
  // Çıktı:
  //{"name":"Mehmed Âkif","id":17,"lessons":["Matematik","Fizik"],
  // "notes":[89,94,84],"gender":"MALE"}
  ```

- Eğer nesneniz / diziniz başka bir nesnenin özelliği olmayacak ise, yanî kök olacaksa `key` parametresine `null` değerini verin.

- ..

> ***NOT :*** JSONWriter `produceText()` yöntemi metni varsayılan olarak tek satıra yazar. Bir başka yöntemde değişkenler alt alta yazılabiliyor olsa da, metni okunabilir JSON formatına getiren kod bu sürümde yoktur.

## JSONer'ı Kullanışlı Bir Araç Olarak Düşünün

- JSONer kitâplığının en önemli özelliğinden birisi elde edilen JSON verilerini bir sınıfın nesnesini inşâ etmek için kullanabiliyor olmanızdır. Eğer bir veri içerdiği özellik isimleri bakımından bir Java sınıfıyla aynı ise veyâ aynı isimde özellikleri varsa, bu veriyi bu Java sınıfının bir örneğinin üretmek için kullanabilirsiniz. `getThisObjectAsTargetType(Class<T> targetClass, Reflector.CODING_STYLE codeStyle)` özelliğini kullanın.

- Misal, şu kodların tanımladığı bir Java sınıfımız olsun:
  
  ```java
  public class User {
      public String name;
      public int id;
      public String[] lessons;
      public int[] notes;
      public GENDER gender;
      public enum GENDER{
          MALE,
          FEMALE
      }
      public User(){}
  ```

- Okuduğumuz verilerle bu sınıfın bir örneğini oluşturmak istersek, şunu çalıştıralım:
  
  ```java
  User injectedUser = jobjUser.getThisObjectAsTargetType(User.class,
                          Reflector.CODING_STYLE.CAMEL_CASE);
  ```

- Yukarıdaki kodda ilgili fonksiyonun ilk parametresi bir örneğini istediğimiz sınıfın kendisi olmalıdır. İkinci parametre ise ilgili sınıfın özelliklerinin `private` erişim belirteciyle sınırlandırılmış olması durumunda ilgili özelliklere verileri zerk edebilmek için gerekli "setter" yöntemlerinin hangi kodlama biçimine göre aranacağını bildiren bir enum bilgisidir. Bu özellik yazdığım kullanışlı `Reflector` kitâplığının bir özelliğidir; bu sebeple bu sınıf içerisindeki `enum` tip olan `CODING_STYLE` tipinde bir değer belirtilmesi gerekiyor.

- Bir örnek üzerinden gidecek olursak, User sınıfının `name` özelliği erişilemez durumda ise ilgili özelliğe verinin zerk edilmesi için "setter" yöntemi kullanılır. `CODING_STYLE` `CAMEL_CASE` seçilmişse `setName`  isminde, `SNAKE_CASE` seçilmişse `set_name` isminde bir setter yöntemi aranır; eğer bulunur ve başarılı bir şekilde çalıştırılırsa veri, hedef özelliğe zerk edilmiş (aktarılmış, enjekte edilmiş) olur. 

> ***NOT :*** JSONer dayanıklı bir kütüphâne olan `Reflector`'ı kullanmaktadır. Eğer verilerinizin bir kısmı hedef sınıfın özelliklerine aktarılamazsa, hatâ alınmaz. Ayrıca hedef veriniz ile mevcut verinizin tipi Java `auto-wrapping` sebebiyle farklı ise, hedef veri tipine dönüşüm gerçekleştirilir;
> 
> Java'da `Integer[].class` tipi otomatik olarak `int[].class` tipine dönüştürülemez; fakat JSONer'ın kullandığı `Reflector` kitâplığı bu dönüşümü desteklemektedir.
> 
> Ayrıca `Reflector`'da`enum` tipindeki verilerinizin JSON metninde metîn tipinde saklanmasından ötürü `enum` tipindeki özelliklere ilgili verilerin aktarılamaması sorunu için de çözüm sağlanır. `enum` tipindeki özelliklere veri aktarmak için hem `enum` tipindeki değişkenlerinizi, hem de `enum` elemanı ile aynı karakterdeki metni kullanabilirsiniz.
> 
> `User.GENDER.MALE` tipini seçmek için hem `User.GENDER.MALE` değişkenini, hem de `"MALE"` String değişkenini kullanabilirsiniz.

- ..
