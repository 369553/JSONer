package jsoner;

import Services.UnicodeHelper;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Mehmet Âkif SOLAK
 * JSON okuma işlemlerini yapmak için bir sınıf
 * @version 2.0.5
 */
public class JSONReader{
    enum dType{
      NUM,
      STR,
      ARR,
      OBJ,
      BOOL,
      NULL
    };
    private Locale loc = Locale.getDefault();
    private static JSONReader service;
    private HashMap<Character, dType> sCtType;//start Character to Type, başlangıç karakterine göre değişkenin tipi
    private HashMap<dType, Character> typeTE;//Type to End, değişkenin tipine göre kabûl edilen bitiş karakteri
    private char[] starts;//Kabûl edilen başlangıç karakterleri

    public JSONReader(){
        sCtType = new HashMap<Character, dType>();
        typeTE = new HashMap<dType, Character>();
        sCtType.put(Character.valueOf('['), dType.ARR);
        sCtType.put(Character.valueOf('{'), dType.OBJ);
        typeTE.put(dType.OBJ, Character.valueOf('}'));
        typeTE.put(dType.ARR, Character.valueOf(']'));
        starts = new char[2];
        starts[0] = '{';
        starts[1] = '[';
    }

//İŞLEM YÖNTEMLERİ:
    /**
     * JSON dizisi metni okunur, nesnelere aktarılır
     * Temel veri tiplerini doğrudan destekler
     * JSON nesnesini {@code Map} tipinde döndürür
     * @param text JSON metni
     * @return Okunan dizi ({@code ArrayList} olarak) veyâ {@code null}
     */
    public ArrayList<Object> readJSONArray(String text){
        if(text == null)
            return null;
        if(text.isEmpty())
            return null;
//        System.err.println("gelen text:\n" + text);
        ArrayList<Object> arr = new ArrayList<Object>();// Okunan değerleri eklemek için değişken
        char[] tx = text.trim().toCharArray();
        int sayac = -1;// Kaçıncı harfte olduğumuzu anlamak için değişken
        boolean isPC = false;// isProcessCompleted, değer alma işlemi bittiyse
        boolean isTkng = false;// isTaking, Değer alınıyorken 'true' olmalıdır
        boolean isIn = false;// İmleç parantez içindeyse 'true' olmalıdır
        dType type = dType.OBJ;//Okunmakta olan verinin tipi
        boolean iPWT = false;//isPairedWithTrue, değişken değeri 'true' ifâdesiyle eşleşmeye başladıysa 'true' olmalıdır.
        boolean iPWF = false;//isPairedWithFalse, değişken değeri 'false' ifâdesiyle eşleşmeye başladıysa 'true' olmalıdır.
        boolean iPWN = false;//isPairedWithNull, değişken değeri 'null' ifâdesiyle eşleşmeye başladıysa 'true' olmalıdır.
        int t = 0;//Değişkenin değer kısmında sayı ve yazı hâricinde bir değer gelmeye başlandığında kaçıncı harf alındığını tutmak için kullanılıyor
        boolean isStarted = false;// Yeni bir değişken okunmaya başlandıysa 'true' olmalıdır
        boolean nextOneStartedForNum = false;// Sayıdan sonra virgül gelerek yeni elemanın okunmasına geçildiyse
        boolean isArrStarted = false;// Dizi okunmaya başlandığında 'true' olmalıdır
        boolean isDotTaked = false;//Değişkenin veri tipi sayıysa ve eğer bir kez '.' işâreti geldiyse 'true' olmalıdır
        boolean isValueTaked = false;// Değişken değeri alındıysa 'true' değerinde olmalıdır.
        boolean isEscCDet = false;// isEscapeCharacterDetected, Kaçış karakteri tespit edildiyse 'true' olmalıdır
        int cycleForEscC = 1;// cycleForEscapeCharacter, Kaçış karakteri tespîti durumunda döngünün boşa dönmesi gereken miktârını belirtir
        int ctFIdle = 0;// counterForIdle, döngünün ne kadar boşa dönmesi gerektiğini belirtir
        if(tx[0] != '[')//Köşeli parantez ile başlamıyorsa okumayı bırak
            return null;
        StringBuilder sVal = new StringBuilder();// Değer metînsel ifâde ise her harfi atamak için değişken
        StringBuilder oVal = new StringBuilder();// Değer metînsel bir ifâde değilse her harfi atamak için değişken
        for(char c : tx){
            sayac++;
//            System.err.println("sayac : " + sayac);
            Object val = null;// Her döngüde okunan eleman
            if(ctFIdle == 1){
                ctFIdle--;
            }
            else if(ctFIdle > 0){
                ctFIdle--;
                continue;
            }
            if(isEscCDet && cycleForEscC > 0){//Tırnak içerisindeki kaçış karakterlerini atlamak için... İkinci şart '\b' kaçış karakteri için boşa dönme işleminin yapılmaması gerekmesinden dolayıdır
                if(cycleForEscC == 1)// Son kez boşa dönülmesi gerekiyorsa;
                    isEscCDet = false;
                cycleForEscC--;// Boşa dönmesi gereken döngü sayısını azalt
                continue;
            }
            if(!isArrStarted){// Dizi okunmaya başlamadıysa;
                if(c == '['){// Başlangıç karakterine kadar boşa dön
                    isArrStarted = true;
                    isStarted = true;
                    isTkng = false;
                }
                continue;
            }
            if(nextOneStartedForNum){
                nextOneStartedForNum = false;
                isStarted = true;
                isValueTaked = false;
                isTkng = false;
                isPC = false;
                isIn = false;
                type = dType.OBJ;
                if(c == '\n')
                    continue;
            }
            if(!isStarted){
                if(c == ','){// Yeni bir değerin okunmasına geçildi
                    isStarted = true;
                    isValueTaked = false;
                    isTkng = false;
                    isPC = false;
                    isIn = false;
                    type = dType.OBJ;
                }
                continue;// Bu satır boşluk(' ') için de çalışır
            }
            else if(!isTkng){// BAŞLANGIÇ TESPİTİ (virgül geçilmiş; fakat değer okunmaya başlanmamış)
//                System.out.println("sayac : " + sayac + "\tkarakter:" + c);
                if(c == ' ' || c == '\t' || c == '\n'){// Okunan karakter boşluksa veyâ yeni satırsa veyâ 4'lü boşluksa boşa dön
                    continue;
                }
                if(c == '['){// Yeni değerin tipi dizi imiş
                    type = dType.ARR;// Veri tipini 'dizi' olarak işâretle
                    String sub = text.substring(sayac, sayac + findCorneredBracket(text.substring(sayac)) + 1);// Aranan karakter son karakter olduğundan ve 'subString' yöntemi üst sınırdaki son değeri almadığından bir değer büyüğünü yazıyoruz
//                    System.out.println("subText:\n" + sub + "\n\n");
                    Object subVal = readJSONArray(sub);
                    ctFIdle = sub.length() - 1;
                    isPC = true;
                    isStarted = false;
                    arr.add(subVal);
                    continue;
                }
                else if(c == '"'){// Yeni değerin tipi metîn imiş
                    isTkng = true;// Değer alınmaya başlandı, olarak işâretle
                    isIn = true;// İmleç parantez içinde, olarak işâretle
                    type = dType.STR;// Veri tipini 'metîn' olarak işâretle
                }
                else if(c == '{'){
                    type = dType.OBJ;// Veri tipini 'nesne' olarak işâretle
                    String sub = text.substring(sayac, sayac + findCurveBracket(text.substring(sayac)) + 1);// Aranan karakter son karakter olduğundan ve 'subString' yöntemi üst sınırdaki son değeri almadığından bir değer büyüğünü yazıyoruz
                    Object subVal = readJSONObject(sub);
                    ctFIdle = sub.length() - 1;
                    isPC = true;
                    isStarted = false;
                    arr.add(subVal);
                    continue;
                }
                else if(small(c) == 't' && t == 0){// Değer 't' ile başladıysa 'true' olabilir
                    iPWT = true;
                    oVal.append(c);
                    isTkng = true;
                    t++;
                    type = dType.BOOL;
                }
                else if(small(c) == 'f' && t == 0){// Değer 'f' ile başladıysa 'false' olabilir
                    iPWF = true;
                    oVal.append(c);
                    isTkng = true;
                    t++;
                    type = dType.BOOL;
                }
                else if(small(c) == 'n' && t == 0){// Değer 'n' ile başladıysa 'null' olabilir
                    iPWN = true;
                    oVal.append(c);
                    isTkng = true;
                    t++;
                    type = dType.NULL;
                }
                else if(isNumber(c) || c == '-' || c == '+' || c == '*'){// SAYI BAŞLANGIÇ TESPİTİ
                    type = dType.NUM;
                    oVal.append(c);
                    isTkng = true;
                }
                else// Hiçbir başlangıç örüntüsüne uymuyorsa okumayı durdur
                    break;
                continue;
            }
            if(isTkng){
                if(type == dType.NUM){
                    if(c == '.' && !isDotTaked){
                        oVal.append(c);
                        isDotTaked = true;
                    }
                    else if(isNumber(c))
                        oVal.append(c);
                    else if(c == ',' || c == ' ' || c == '\n' || c == ']' || c == '\t'){
                        isValueTaked = true;
                        if(c == ',')
                            nextOneStartedForNum = true;
                    }
                    else
                        break;
                }
                else if(type == dType.STR){
                    if(isIn){
                        if(c == '"'){// Eğer tırnak açılmışken tırnak okunduysa
                            if(tx[sayac - 1] != '\\'){// Öncesinde kaçış karakteri yoksa kapanış tırnak işâreti bulundu, demektir
                                isValueTaked = true;// Yeni değerin okunmasına başlanmadığını belirt
                            }
                            else
                                sVal.append(c);
                        }
                        else if(c == '\\'){// Kaçış karakteri için özel durum
                            boolean isEscape = false;
                            char spec = tx[sayac + 1];
                            HashMap<String, Object> result = getValueAndEmptyCycleTimeIfIsNonFunctionalEscape(spec);
                            if((Boolean) result.get("result")){// Kaçış karakteri tespit edildiyse;
                                // Özel durumlar = u, b, f
                                isEscCDet = true;// Boşa dönmek için kaçış karakteri tespit edildiğini işâretle
                                cycleForEscC = (Integer) result.get("cycle");// Boşa dönme miktarını işâretle
                                if(spec != 'u' && spec != 'b' && spec != 'f'){// Bunlar gönderilen fonksiyonda tamâmen ele alınmış özel karakterler
                                    sVal.append(String.valueOf(result.get("value")));
                                }
                                else{// Diğer durumlar : u, b, f
                                    switch(spec){
                                        case 'u' :{
                                            if(tx.length > sayac + 4){// Hatâ oluşmaması için;
                                                char[] uni = new char[4];
                                                uni[0] = tx[sayac + 2];
                                                uni[1] = tx[sayac + 3];
                                                uni[2] = tx[sayac + 4];
                                                uni[3] = tx[sayac + 5];
                                               sVal.append(getUnicodeCharacter(String.valueOf(uni)));
                                            }
                                            else{
                                                sVal.append(c);
                                                isEscCDet = false;// 'u' karakterinden sonra 4 hex değer olmadığı için boşa dönmeyi iptal et
                                            }
                                            break;
                                        }
                                        case 'b' :{
                                            //.;.
                                            break;
                                        }
                                        case 'f' :{
                                            //.;.
                                            break;
                                        }
                                    }
                                }
                                continue;
                            }
                        }
                        else
                            sVal.append(c);
                    }
                    // Buraya yazılması gereken bir kod yok, gibi görünüyor
                }
                else if(t != 0){
                    if(iPWN){
                        if(isPairingForNull(c, t))
                            oVal.append(c);
                        else
                            break;
                        if(t == 3)
                            isValueTaked = true;
                    }
                    else if(iPWT){
                        if(isPairingForTrue(c, t))
                            oVal.append(c);
                        else
                            break;
                        if(t == 3)
                            isValueTaked = true;
                    }
                    else if(iPWF){
                        if(isPairingForFalse(c, t))
                            oVal.append(c);
                        else
                            break;
                        if(t == 4)
                            isValueTaked = true;
                    }
                    t++;
                }
            }
            if(isValueTaked && !isPC){// Değer okuma bitmiş; işlem tamâmlanmamış (değer kaydedilmemiş)
                switch(type){
                    case STR : {
                        val = sVal.toString();
                        sVal.replace(0, sVal.length(), "");// Metîn değeri oluşturucusunu sıfırla
                        isPC = true;// İşlem tamâmlandı, olarak işâretle
                        break;
                    }
                    case NUM : {
                        String[] numbers = oVal.toString().split("\\.");
                        int digitNumber = numbers[0].length();
                        boolean isABigNumber = false;
                        boolean isADoubleNumber = false;
                        if(digitNumber > 9)
                            isABigNumber = true;
                        if(numbers.length == 2)
                            isADoubleNumber = true;
                        if(isADoubleNumber){
                            double num = Double.valueOf(oVal.toString());
                            val = num;
                        }
                        else if(isABigNumber){
                            BigInteger num = BigInteger.valueOf(Long.valueOf(numbers[0]).longValue());
                            val = num;
                        }
                        else{
                            int num = Integer.valueOf(numbers[0]);
                            val = num;
                        }
                        isPC = true;
                        isDotTaked = false;
                        break;
                    }
                    case BOOL : {
                        val = Boolean.valueOf(oVal.toString().toLowerCase());
                        iPWF = false;
                        iPWT = false;
                        isPC = true;
                        break;
                    }
                    case NULL : {
                        if(oVal.toString().toLowerCase().equals("null"))
                            val = null;
                        isPC = true;
                    }
                }
                if(isPC){
                    arr.add(val);// Listeye okunan değeri ekle
                }
                t = 0;
                oVal.replace(0, oVal.length(), "");
                isStarted = false;
                // .;. : Sıfırlanması gereken bir değişken varsa, sıfırla
            }
            if(!isStarted){
                if(c == ']' && !isIn)
                    break;
            }
        }
        return arr;
    }
    /**
     * Nesne tipindeki JSON metni okunur, Java nesnelerine aktarılır
     * JSON dizisi {@code List} tipinde, JSON nesnesi {@code Map} tipinde döndürülür
     * @param text JSON metni
     * @return Okunan JSON metnindeki değişkenler veyâ {@code null}
     */
    public HashMap<String, Object> readJSONObject(String text){
        int sayac = -1;//imlecin konumu
        int uz = text.length();//uzunluk, metnin uzunluğu
        int lengthOfArrOrObjValDet = 0;//Değişkenin değeri dizi ise veyâ nesne ise bu dizi veyâ nesne değer olarak alındıktan sonra döngünün nereye kadar boş döneceğini (işlem yapmadan) belirtmek için kullanılıyor bi iznillâh
        int countForArrOrObjValDet = 0;//Yukarıdaki işlem için gerekli bir sayaç
        int takedAtt = 0;//Alınan özellik sayısı, başka deyişle okunup, kaydedilen değişken sayısı
        int t = 0;//Değişkenin değer kısmında sayı ve yazı hâricinde bir değer gelmeye başlandığında kaçıncı harf alındığını tutmak için kullanılıyor
        ArrayList<HashMap<String, Object>> values = new ArrayList<HashMap<String, Object>>();//Okunan değerleri aktarmak için bir değişken
        char[] tx = text.trim().toCharArray();//Metnin başındaki ve sonundaki boşlukları çıkar, karakter dizisine dönüştür
        boolean isInside = false;//Listedeki herhangi bir değerin içerisindeyse,
                                //başka deyişle tırnak içerisini okuyorsa 'true' olmalıdır
        boolean isStarted = false;//Değişken okunmaya başladıysa 'true' olmalıdır.
        boolean isNameTaked = false;//Değişken ismi alındıysa 'true' değerinde olmalıdır.
        boolean isValueTaked = false;//Değişken değeri alındıysa 'true' değerinde olmalıdır.
        boolean isNameEmpty = false;//Eğer değişkenin ismi yoksa (yanî çift tırnak içerisi boşsa)
        boolean isEO = false;//Çift tırnak içerisinde kaçış karakteriyle berâber tırnak açıldıysa ve kapanmadıysa 'true' olmalıdır
        boolean iIS = false;//isInSecond, 'nesne' veri tipinde anahtar değerinden sonra iki nokta üstü üste (':') işâreti geldiyse 'true' olmalı
        boolean nTAnyC = true;//notTakedAnyCharacter, iIS = 'true' iken (yanî ikinci kısma geçildikten sonra) ve değer alınmadıysa ve (boşluklar hâriç) ilk karakter tırnak değilse (yanî boşluk veyâ yeni satır dışında bir karakter okunduysa) 'true' olmalıdır.
        boolean isDotTaked = false;//Değişkenin veri tipi sayıysa ve eğer bir kez '.' işâreti geldiyse 'true' olmalıdır
        boolean iSPC = false;//isSavingProcessCompleted, kaydetme işlemi sonlandıysa 'true' olmalıdır, bu bit döngüden çıkıldığında değişken okumasının sağlıklı şekilde bitip, bitmediğini işâret eder.
        boolean cBN = false;//can'tBeNumber, değer sayı ile başlamadıysa artık sayı olmadığı kesinleştiğinden bu bit 'true' olmalıdır
        boolean vSAB = false;//value started as BOOLEAN, eğer değişken değeri sayı ve yazı değilse ve ilgili kısma girildiğinde değer 'true' yâ dâ 'false' olarak okunmaya başladıysa 'true' olmalıdır.
        boolean vSAN = false;//value started as NULL, yukarıdakine benzer olarak ilgili yerde değer 'NULL' olarak okunmaya başladıysa 'true' olmalıdır.
        boolean iPWT = false;//isPairedWithTrue, değişken değeri 'true' ifâdesiyle eşleşmeye başladıysa 'true' olmalıdır.
        boolean iPWF = false;//isPairedWithFalse, değişken değeri 'false' ifâdesiyle eşleşmeye başladıysa 'true' olmalıdır.
        boolean iPWN = false;//isPairedWithNull, değişken değeri 'null' ifâdesiyle eşleşmeye başladıysa 'true' olmalıdır.
        boolean nextOneStarted = false;//Değişken kaydedilme alanına girildiğinde okunan karakter ',' ise yeni değişken okumaya geçilebilir, tekrar virgül beklenilmez; bunu ifâde etmek için kullanılan değişken
        boolean isEscCDet = false;//isEscapeCharacterDetected, eğer tırnak içerisinde kaçış karakteri tespit edildiyse bu değişken 'true' olmalıdır (tırnak kaçış karakteri hâriç ('\"'))
        boolean arrValDet = false;//arrayValueDetected, değişkenin değeri yeni bir dizi ise 'true' olmalıdır.
        boolean subObjDet = false;//subObjectDetected, değişkenin değeri bir değişken ise 'true' olmalıdır.
        StringBuilder name = new StringBuilder();//Değişkenin ismi
        StringBuilder value = new StringBuilder();//Değişken değeri yazı ise saklamak için değişken
        StringBuilder oValue = new StringBuilder();//Değişken değeri yazı değilse okunan karakterleri buraya ekle
        dType type = dType.OBJ;//Okunmakta olan verinin tipi
        int cycleForEscC = 1;// cycleForEscapeCharacter, Kaçış karakteri tespit edildiğinde döngünün kaç kez boşa dönmesi gerektiğini belirtir
        HashMap<String, Object> obj = new HashMap<String, Object>();
        if(tx == null)
            return null;
        if(tx.length < 3)
            return null;
        for(char c : tx){//78. karakterde ve o karakter de ':' ve iIS = true
            sayac++;
//            System.err.println("sayac : " + sayac);
            if(arrValDet || subObjDet){
                if(subObjDet){
//                    System.err.println("Alt değer değişken, sayac : " + sayac);
//                    System.out.println("lengthOfArrOrObjValDet : " + lengthOfArrOrObjValDet);
                }
//                System.out.println("Atlama alanına girildi.\tcount : " + countForArrValDet);
                if(countForArrOrObjValDet != lengthOfArrOrObjValDet){
//                System.out.println("..döngü : " + countForArrOrObjValDet);
                    countForArrOrObjValDet++;
                    continue;
                }
                else{
                    arrValDet = false;
                    subObjDet = false;
                    countForArrOrObjValDet = 0;
                    lengthOfArrOrObjValDet = 0;
                }
            }
            if(isEscCDet && cycleForEscC > 0){//Tırnak içerisindeki kaçış karakterlerini atlamak için... İkinci şart '\b' kaçış karakteri için boşa dönme işleminin yapılmaması gerekmesinden dolayıdır
                if(cycleForEscC == 1)// Son kez boşa dönülmesi gerekiyorsa;
                    isEscCDet = false;
                cycleForEscC--;// Boşa dönmesi gereken döngü sayısını azalt
                continue;
            }
            if(((c == '\t' || c =='\n' || c == ' ') && !isInside) && !nextOneStarted){
                if(t != 0){
                    continue;
                }
                if(sayac + 1 < uz){
                    if(isNumber(tx[sayac - 1]) && isNumber(tx[sayac + 1])){
                        break;
                    }
                    else if(tx[sayac - 1] == '.' && isNumber(tx[sayac + 1])){
                        break;
                    }
                }
                continue;
            }
            if((takedAtt > 0 && !isStarted) || nextOneStarted){
                /*if(nextOneStarted){
                    System.out.println("Daha önce özellik alınmış");
                    System.out.println("Okunan karakter : " + c + "\tsonraki karakter : " + tx[sayac + 1]);
                }*/
//                System.out.println("Başa dönüldü ve önceki değişken kaydedilmiş");
                if(c == ',' || nextOneStarted){
                    //Yeni nesne okumaya geçmeden önce çevre değişkenlerini sıfırla:
                    oValue.replace(0, oValue.length(), "");
                    value.replace(0, value.length(), "");
                    name.replace(0, name.length(), "");
                    type = dType.OBJ;
                    isValueTaked = false;
                    isNameTaked = false;
                    iPWT = false;
                    iPWF = false;
                    iPWN = false;
                    isInside = false;
                    isNameEmpty = false;
                    isEO = false;
                    iIS = false;
                    nTAnyC = true;
                    isDotTaked = false;
                    iSPC = false;
                    cBN = false;
                    vSAB = false;
                    vSAN = false;
                    t = 0;
                    isStarted = true;
                    nextOneStarted = false;
                    if(c != '"')
                        continue;
                }
            }
            if(!isStarted){//Henüz değişken okunmaya başlanmadıysa...
                if(c == ' ')
                    continue;//Eğer gerekli - gereksiz boşluk varsa hatâ verme, sonraki harfle devâm et
                if(!(c == starts[0] || c == starts[1]))//Başlangıç karakterleriyle başlamıyorsa döngüden çık
                    break;
                //Kabûl edilen karakter geldiyse:
                type = sCtType.get(Character.valueOf(starts[0]));
//                System.out.println("Gelenin tipi : " + type.name());
                isStarted = true;
                continue;
            }
            
            if(!isNameTaked && !isInside){//İsim bilgisi henüz alınmadıysa ve çift tırnak içerisinde değilsek
                if(c == ' ')
                    continue;
                if(c != '"' && type == dType.OBJ)// 'nesne' veri tipi için bu durumda tırnak işâreti dışındaki karakteri kabûl etme
                    break;
                //Eğer veri tipi dizi ise (dType.ARR) beklenen karakter süslü parantez olmalı; özyinelemeli şekilde fonksiyon burada ayrılmalı
                isInside = true;
                continue;
            }
            if(isInside){//Çift tırnak içerisindeysek...
                if(!isNameTaked){//İsim alınması işlemi henüz bitmediyse...
                    if(c == '"'){//İkinci (kapanış) tırnak işâreti geldiyse...
                        if(tx[sayac - 1] == '\\'){//Bu tırnak işâretinin ismin bir parçası olup, olmadığını sorgula.
                            if(isEO)
                                isEO = false;
                            isEO = true;
                            name.append(c);
                            continue;
                    }
                        if(name.length() == 0)//Tırnak içerisinde hiçbir şey yoksa...
                            isNameEmpty = true;//İsim değeri boş bitini 'true' yap.
                        isNameTaked = true;//İsim alınma işleminin bittiğini işâretle
                        isInside = false;//Tırnak içerisinde olmadığımızı işâretle
                        isEO = false;
                        continue;
                    }
                    //!: Bu noktada : isim içerisinde boşluk kabûl edilmiyorsa boşluk değeri isme eklenmemeli.
                    name.append(c);//Alınan harfi değişken ismine ekle
                    continue;
                }
                if(c == '"'){//Birden sonraki tırnak işâreti geldiyse...
                    if(tx[sayac - 1] == '\\'){//Bu tırnak işâretinin metnin bir parçası olup, olmadığını sorgula.
                        isEO = !isEO;
                        value.append(c);
                        continue;
                    }
                    isInside = false;
                    isValueTaked = true;
                    isEO = false;
                    continue;
                }
                if(c == '\\'){
                    boolean isEscape = false;
                    char spec = tx[sayac + 1];
                    HashMap<String, Object> result = getValueAndEmptyCycleTimeIfIsNonFunctionalEscape(spec);
                    if(((Boolean)result.get("result"))){// Eğer kaçış karakteri tespit edildiyse;
                        // Özel durumlar = u, b, f
                        isEscCDet = true;// Boşa dönmek için kaçış karakteri tespit edildiğini işâretle
                        cycleForEscC = (Integer) result.get("cycle");// Boşa dönme miktarını işâretle
                        if(spec != 'u' && spec != 'b' && spec != 'f'){// Bunlar gönderilen fonksiyonda tamâmen ele alınmış özel karakterler
                            value.append(String.valueOf(result.get("value")));
                        }
                        else{// Diğer durumlar : u, b, f
                            switch(spec){
                                case 'u' :{
                                    if(tx.length > sayac + 4){// Hatâ oluşmaması için;
                                        char[] uni = new char[4];
                                        uni[0] = tx[sayac + 2];
                                        uni[1] = tx[sayac + 3];
                                        uni[2] = tx[sayac + 4];
                                        uni[3] = tx[sayac + 5];
                                       value.append(getUnicodeCharacter(String.valueOf(uni)));
                                    }
                                    else{
                                        value.append(c);
                                        isEscCDet = false;// 'u' karakterinden sonra 4 hex değer olmadığı için boşa dönmeyi iptal et
                                    }
                                    break;
                                }
                                case 'b' :{
                                    //.;.
                                    break;
                                }
                                case 'f' :{
                                    //.;.
                                    break;
                                }
                            }
                        }
                        continue;
                    }
                }
                value.append(c);
                continue;
            }
            if(c == ' ')//Boşluk geldiyse yoksay; sayı okunurken boşluk geldiyse de yoksalıyor
                continue;
            if(!iIS){//Henüz ikinci kısma geçilmediyse (iki nokta işâreti gelmediyse...
                if(c != ':'){//İki nokta işâretinden başka işâret geldiyse...
                    break;//Kabûl etme.
                }
                iIS = true;
                continue;
            }
            if(c == '['){
//                System.out.println("Başlangıç sırası : " + sayac);
//                System.out.println("Bitiş sırası : " + (sayac + findCurveBracket(text.substring(sayac))));
//                System.out.println("text.sub : " + text.substring(sayac, sayac + findCurveBracket(text.substring(sayac))));
//                int sonNokta = findCurveBracket(text.substring(sayac));
//                System.err.println("sonNokta : " + sonNokta);
                String subText = text.substring(sayac, sayac + findCorneredBracket(text.substring(sayac)) + 1);
//                System.out.println("subText:\n" + subText + "\n\n");
//                System.out.println("sayac : " + sayac + "\tdizi kapanış mevkîsi : " + findCorneredBracket(text.substring(sayac)));
//                System.out.println("dizi kapanış simgesi mevkîsi : " + findCorneredBracket(text.substring(sayac)));
//                System.out.println("Alt dizi gönderilen parametre : " + subText);
                ArrayList<Object> dizi = readJSONArray(subText);
//                if(dizi != null)
//                    System.err.println("dizi != null");
//                System.out.println("dizi.uzunluk : " + dizi.size());
//                System.out.println("dizi.uz : " + dizi.size());
//                for(HashMap<String, Object> aa : dizi){
//                    System.out.println("altözellik saysısı : " + aa.keySet().size());
//                }
                obj.put(name.toString(), dizi);
                arrValDet = true;
                lengthOfArrOrObjValDet = subText.length() - 1;
                iSPC = true;
                takedAtt++;
                isStarted = false;
                values.add(obj);
                continue;
            }
            if(c == '"' && !isValueTaked && nTAnyC){//İkinci kısma geçildi VE henüz değişken değeri alınma işlemi bitmedi VE hiçbir karakter alınmadı VE ilk karakter tırnak işâretiyse...
                isInside = true;
                type = dType.STR;
                continue;
            }
            if(!isValueTaked && nTAnyC)//Değer alınmadı VE ilk değer tırnak işâreti değilse karakter alındığını 'nTAnyC' değişkeni aracılığıyla bildir
                nTAnyC = false;
            if(type == dType.NUM && !isValueTaked){
                //Kabûl edilebilir iki seçenek : sayı veyâ nokta (nokta bir def'aya mahsus kabûl edilebilir):
                //Veyâ sonlanma...
                if(isNumber(c)){
                    oValue.append(c);
                    continue;
                }
                else if(c == '.' && !isDotTaked){
                    oValue.append(c);
                    isDotTaked = true;
                    continue;
                }
                else if(c == ',' || c == '}'){
                    isValueTaked = true;
//                    continue;
                }
                else
                    break;//Sayıdan sonra nokta geldiyse ve sonrasında başka nokta geldiyse VEYÂ sayıdan sonra nokta geldiyse ve sonrasında sayı dışında ve sonlanma işâreti dışında bir şey geldiyse okumayı sonlandır
            }
            if(isNumber(c) && !isValueTaked && !cBN){//Okunan karakter sayı ise
                type = dType.NUM;
                oValue.append(c);
                continue;
            }
            if(!isValueTaked){//
                cBN = true;//Gelen karakter rakam olmadığından dolayı değer artık sayı olamaz.
                if(small(c) == 't' && t == 0){//Gelen İLK harf 't' ise ('true' ile eşleşme başladıysa)...
                    oValue.append(c);
                    type = dType.BOOL;
                    iPWT = true;
                }
                else if(small(c) == 'f' && t == 0){//Gelen İLK harf 'f' ise ('false' ile eşleşme başladıysa)...
                    oValue.append(c);
                    type = dType.BOOL;
                    iPWF = true;
                }
                else if(small(c) == 'n' && t == 0){//Gelen İLK harf 'n' ise ('null' ile eşleşme başladıysa)...
                    oValue.append(c);
                    type = dType.NULL;
                    iPWN = true;
                }
                else if(t == 0){//BURADA evvelce 'break' yapılıyordu; bu hâle getirmemiz bir hatâ doğuruyorsa düzelt
//                    System.out.println("parametre:\n" + text.substring(sayac));
                    String subText = text.substring(sayac, sayac + findCurveBracket(text.substring(sayac)) + 1);// Aranan karakter son karakter olduğundan ve 'subString' yöntemi üst sınırdaki son değeri almadığından bir değer büyüğünü yazıyoruz
//                    System.out.println("subText : " + subText);
                    Object subVal = readJSONObject(subText);
                    subObjDet = true;
                    lengthOfArrOrObjValDet = subText.length() - 1;
                    iSPC = true;
                    takedAtt++;
                    isStarted = false;
                    obj.put(name.toString(), subVal);
                    continue;
                }
                if(t != 0){
                    if(iPWN){
                        if(isPairingForNull(c, t))
                            oValue.append(c);
                        else
                            break;
                        if(t == 3)
                            isValueTaked = true;
                    }
                    else if(iPWT){
                        if(isPairingForTrue(c, t))
                            oValue.append(c);
                        else
                            break;
                        if(t == 3)
                            isValueTaked = true;
                    }
                    else if(iPWF){
                        if(isPairingForFalse(c, t))
                            oValue.append(c);
                        else
                            break;
                        if(t == 4)
                            isValueTaked = true;
                    }
                }
                t++;
                continue;
            }
            //Değişken okumayı bitirme işlemleri (yeni değişkeni okumaya geçmek için):
            if(isValueTaked && !iSPC){
                switch(type){
                    case NUM :{
                        String[] numbers = oValue.toString().split("\\.");
                        int digitNumber = numbers[0].length();
                        boolean isABigNumber = false;
                        boolean isADoubleNumber = false;
                        if(digitNumber > 9)
                            isABigNumber = true;
                        if(numbers.length == 2)
                            isADoubleNumber = true;
                        if(isADoubleNumber){
                            double num = Double.valueOf(oValue.toString());
                            obj.put(name.toString(), num);
                        }
                        else if(isABigNumber){
                            BigInteger num = BigInteger.valueOf(Long.valueOf(numbers[0]).longValue());
                            obj.put(name.toString(), num);
                        }
                        else{
                            int num = Integer.valueOf(numbers[0]);
                            obj.put(name.toString(), num);
                        }
                        iSPC = true;
                        break;
                    }
                    case STR :{
                        obj.put(name.toString(), value.toString());
                        iSPC = true;
                        break;
                    }
                    case BOOL :{
                        String a = oValue.toString();
                        if(a.toLowerCase().equals("false"))
                            obj.put(name.toString(), false);
                        else if(a.toLowerCase().equals("true"))
                            obj.put(name.toString(), true);
                        else
                            break;
                        iSPC = true;
                        break;
                    }
                    case NULL :{
                        if(oValue.toString().toLowerCase().equals("null"))
                            obj.put(name.toString(), null);
                        else
                            break;
                        iSPC = true;
                        break;
                    }
                }
                if(iSPC){
                    values.add(obj);
                    takedAtt++;
                    isStarted = false;
                }
                if(c == '}'){
//                    System.out.println("Süslü parantez kapandı : " + sayac);
                    break;
                }
                if(c == ',')
                    nextOneStarted = true;
            }
        }
            
        return obj;
    }
    /**
     * Okunan JSON nesnesini simgeleyen {@code JSONObject} nesnesi döndürülür
     * @param text JSON metni
     * @return JSON nesnesini simgeleyen {@code JSONObject} veyâ {@code null}
     */
    public JSONObject readJSONObjectReturnJSONObject(String text){
        Map read = readJSONObject(text);
        return (read != null ? new JSONObject(read) : null);
    }
    /**
     * Okunan JSON dizisini simgeleyen {@code JSONArray} nesnesi döndürülür
     * @param text JSON metni
     * @return JSON dizisini simgeleyen {@code JSONArray} veyâ {@code null}
     */
    public JSONArray readJSONArrayReturnJSONArray(String text){
        List read = readJSONArray(text);
        return (read != null ? new JSONArray(read) : null);
    }
    /**
     * Verilen JSON metninin kök değişkeninin nesne olup, olmadığını döndürür
     * Eğer JSON metni {@code '{'} ile başlıyorsa {@code true} döndürülür.
     * @param text JSON metni
     * @return Kök değişken nesne ise {@code true}, diğer durumda {@code false}
     */
    public boolean isRootAnObject(String text){
        return detectTypeOfRootVariable(text, true);
    }
    /**
     * Verilen JSON metninin kök değişkeninin nesne olup, olmadığını döndürür
     * Eğer JSON metni {@code '{'} ile başlıyorsa {@code true} döndürülür.
     * @param text JSON metni
     * @return Kök değişken nesne ise {@code true}, diğer durumda {@code false}
     */
    public boolean isRootAnArray(String text){
        return detectTypeOfRootVariable(text, false);
    }
    // ARKAPLAN İŞLEM YÖNTEMLERİ:
    private HashMap<String, Object> getValueAndEmptyCycleTimeIfIsNonFunctionalEscape(char c){// Kaçış simgesinden sonra gelen karakteri alır ve gerekli bilgileri döndürür:
        /*
            NOT : Fonksiyonellik ifâde eden '\b' kaçış karakteri bu fonksiyonda ele alınmıyor!
            Sonraki karakterlerin okunmasını gerektiren 'u' için değer yerleştirme yapılmıyor; sadece tespit ve boşa dönme sayısı çalıştırılıyor
            result : Kaçış karakteri JSON tarafından tanınan bir kaçış karakteri ise 'true' olmalıdır;
            cycle : JSON dosyasını harf harf okuyan fonksiyonun boşa dönmesi gereken döngü sayısı
            value : Kaçış karakterinden sonra gelen karakterin (yanî girdi olarak verilen karakterin) gerçekte ifâde ettiği metînsel değer
        */
        HashMap<String, Object> results = new HashMap<String, Object>();
        String value = null;
        int cycle = 1;
        boolean isEsc = false;// isEscape, eğer kaçış karakteri ise (JSON formatının tanıdığı kaçış karakterlerinden ise) 'true' olmalıdır.
        switch(c){
            case '"' :{
                value = String.valueOf(c);
                isEsc = true;
                break;
            }
            case '\\' :{
                value = String.valueOf(c);
                isEsc = true;
                break;
            }
            case '/' :{
                value = String.valueOf(c);
                isEsc = true;
                break;
            }
            case 'b' :{
                isEsc = true;
                cycle = 0;
                //.;.
                break;
            }
            case 'f' :{
                isEsc = true;
                cycle = 0;// Şimdilik hatâ vermemesi için eklendi, değiştirilmesi lazım
                //.;.
                break;
            }
            case 'n' :{
                value = "\n";
                isEsc = true;
                break;
            }
            case 'r' :{
                value = "\n";
                isEsc = true;
                break;
            }
            case 't' :{
                value = "\t";
                isEsc = true;
                break;
            }
            case 'u' :{
//                Burada 'value' atanmıyor; dönülen yerde bunun çalıştırılması lazım : getUnicodeCharacter();
                isEsc = true;
                cycle = 5;
                break;
            }
        }
        if(isEsc){
            results.put("value", value);
            results.put("cycle", cycle);
            results.put("value", value);
        }
        results.put("result", isEsc);
        return results;
    }
    private boolean isNumber(char c){
        switch(c){
            case '0' :{
                return true;
            }
            case '1' :{
                return true;
            }
            case '2' :{
                return true;
            }
            case '3' :{
                return true;
            }
            case '4' :{
                return true;
            }
            case '5' :{
                return true;
            }
            case '6' :{
                return true;
            }
            case '7' :{
                return true;
            }
            case '8' :{
                return true;
            }
            case '9' :{
                return true;
            }
        }
        return false;
    }
    private char small(char c){
        String s = String.valueOf(c);
        s = s.toLowerCase(loc);
        return s.charAt(0);
    }
    private boolean isPairingForNull(char c, int numberOfCharacter){
        if(numberOfCharacter == 0)
            if(small(c) == 'n')
                return true;
        if(numberOfCharacter == 1)
            if(small(c) == 'u')
                return true;
        if(numberOfCharacter == 2)
            if(small(c) == 'l')
                return true;
        if(numberOfCharacter == 3)
            if(small(c) == 'l')
                return true;
        return false;
    }
    private boolean isPairingForTrue(char c, int numberOfCharacter){
        if(numberOfCharacter == 0)
            if(small(c) == 't')
                return true;
        if(numberOfCharacter == 1)
            if(small(c) == 'r')
                return true;
        if(numberOfCharacter == 2)
            if(small(c) == 'u')
                return true;
        if(numberOfCharacter == 3)
            if(small(c) == 'e')
                return true;
        return false;
    }
    private boolean isPairingForFalse(char c, int numberOfCharacter){
        if(numberOfCharacter == 0)
            if(small(c) == 'f')
                return true;
        if(numberOfCharacter == 1)
            if(small(c) == 'a')
                return true;
        if(numberOfCharacter == 2)
            if(small(c) == 'l')
                return true;
        if(numberOfCharacter == 3)
            if(small(c) == 's')
                return true;
        if(numberOfCharacter == 4)
            if(small(c) == 'e')
                return true;
        return false;
    }
    private int splitOnOutsideOfQuotes(String text, char searched, char startCharOfDeep, char endCharOfDeep){
        return splitOnOutsideOfQutes(text, searched, startCharOfDeep, endCharOfDeep, false, true);
    }
    private int findCorneredBracket(String text){
        return splitOnOutsideOfQuotes(text, ']', '[', ']');// Aranan karakterin mevkîsini döndürüyor
    }
    private int findCurveBracket(String text){
        return splitOnOutsideOfQuotes(text, '}', '{', '}');
    }
    private int splitOnOutsideOfQutes(String text, char searched, char startCharOfDeep, char endCharOfDeep, boolean startDeepAs1, boolean isEscapeCharacterValid){
        // Bu fonksiyon tırnak işâretini aramak için kullanılamaz!
        ArrayList<String> list = new ArrayList<String>();
        int deep = 0;// Derinliği gösteren değişken
        char[] tx = text.toCharArray();
        boolean isInQuotes = false;// İmleç parantez içerisindeyse 'true' olmalıdır
        int sayac = -1;// İmleç konumu
        /*KULLAN*/boolean isStartCharSpecial = false;// Başlama karakteri özel karakterse 'true' olmalıdır; bu, başlama karakteri öncesinde kaçış karakteri olup, olmadığını denetlemek içindir
        /*KULLAN*/boolean isEndCharSpecial = false;
        boolean isDeepChangedNow = false;// Eğer derinlik döngünün mevcut adımında değişti ise 'true' olmalıdır.
        if(startDeepAs1)
            deep = 1;
        for(char ch : tx){
            sayac++;
//            System.out.println("sayac : " + sayac + "\tderinlik : " + deep);
            isDeepChangedNow = false;
            if(ch == '"'){
                if(tx[sayac - 1] == '\\' && isEscapeCharacterValid)// Tırnak işâretinden evvel açış karakteri geldiyse ve kaçış karakterleri dikkate alınıyorsa
                    continue;// Sonraki harfe geç
                else{
                    isInQuotes = !isInQuotes;// Tırnak içerisinde olma durumunu değiştir
                    continue;// Sonraki harfe geç
                }
            }
            if(!isInQuotes){// Tırnak içerisinde değilse;
                if(ch == startCharOfDeep){
                    if(isStartCharSpecial){// Başlama karakteri özel karakter ise öncesinde kaçış karakteri olup olmadığına bak
                        if(isEscapeCharacterValid && tx[sayac - 1] == '\\')
                            ;// Sonraki harfe geç, bu harfi dikkate alma
                        else{
                            deep++;// Derinliği arttır.
                            isDeepChangedNow = true;
                        }
                    }
                    else{
                        deep++;// Derinliği arttır.
                        isDeepChangedNow = true;
                    }
                }
                else if(ch == endCharOfDeep){// Derinlik sonlandırma karakteri için tespit kod bölümü.
                    if(isEndCharSpecial){// Başlama karakteri özel karakter ise öncesinde kaçış karakteri olup olmadığına bak
                        if(isEscapeCharacterValid && tx[sayac - 1] == '\\')
                            ;// Sonraki harfe geç, bu harfi dikkate alma
                        else{
                            deep--;// Derinliği azalt.
                            isDeepChangedNow = true;
                        }
                    }
                    else{
                        deep--;// Derinliği azalt.
                        isDeepChangedNow = true;
                    }
                }
                if(ch == searched){// Aranan karakter bulundu ise;
                    if(deep == 0)// Derinlik sıfır ise;
                        break;
                    if(isDeepChangedNow){// Derinlik döngünün bu adımında değişti ise;
                        if(ch == startCharOfDeep && deep == 1)// Aranan karakter aynı zamânda derinlik başlangıç karakteri ise;
                            break;
                        if(ch == endCharOfDeep && deep == -1)// Aranan karakter aynı zamânda derinlik sonlandırıcı karakter ise;
                            break;
                    }
                }
            }
        }
        return sayac;
    }
    private String getUnicodeCharacter(String fourHexValue){// Eğer ki gelen ifâde geçerli bir unicode kodu değilse, sayı olmayan ilk kısmı döndür
        return UnicodeHelper.getCharacterFromUnicodeAfterU(fourHexValue);
    }
    private boolean detectTypeOfRootVariable(String text, boolean lookForRootIsObject){
        text = text.trim();
        char[] tx = text.toCharArray();
        char startTarget = (lookForRootIsObject ? '{' : '[');
        for(int sayac = 0; sayac < tx.length; sayac++){
            if(tx[sayac] == '\n' || tx[sayac] == '\t' || tx[sayac] == '\r')
                continue;
            return tx[sayac] == startTarget;
        }
        return false;
    }

//ERİŞİM YÖNTEMLERİ:
    /**
     * JSON okuma hizmetini kullanmak için {@code JSONReader} döndürülür
     * @return {@code JSONReader}
     */
    public static JSONReader getService(){
        if(service == null){
            service = new JSONReader();
        }
        return service;
    }
}